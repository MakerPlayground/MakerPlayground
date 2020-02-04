package io.makerplayground.generator.source;

import io.makerplayground.device.actual.*;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.DeviceConnection;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.makerplayground.generator.source.RpiPythonCodeUtility.*;

public class RpiPythonInteractiveCode {

    final StringBuilder builder = new StringBuilder();

    private final Project project;
    private final ProjectConfiguration configuration;
    private final List<List<ProjectDevice>> projectDeviceGroup;

    private RpiPythonInteractiveCode(Project project) {
        this.project = project;
        this.configuration = project.getProjectConfiguration();
        this.projectDeviceGroup = project.getAllDevicesGroupBySameActualDevice();
    }

    public static SourceCodeResult generateCode(Project project) {
        // Check if all used devices are assigned.
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }
        if (project.getProjectConfiguration().isUseHwSerial()) {
            return new SourceCodeResult(SourceCodeError.INTERACTIVE_MODE_NEED_HW_SERIAL, "-");
        }
        RpiPythonInteractiveCode generator = new RpiPythonInteractiveCode(project);
        generator.appendHeader();
        generator.appendGenMessageFunction();
        generator.appendProcessCommand();
        generator.appendMainFunction();
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendGenMessageFunction() {
        builder.append(NEW_LINE);

        builder.append("def gen_message():").append(NEW_LINE);
        builder.append(INDENT).append("retval = ''").append(NEW_LINE);

        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            for (ProjectDevice projectDevice: projectDeviceList) {
                if (project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isPresent()) {
                    ActualDevice actualDevice = project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).get();
                    for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                        if (genericDevice == projectDevice.getGenericDevice()) {
                            Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);
                            if (!compatibility.getDeviceCondition().isEmpty() || !compatibility.getDeviceValue().isEmpty()) {
                                String variableName = parseDeviceVariableName(projectDeviceList);
                                builder.append(INDENT).append("retval += f'\"").append(projectDevice.getName()).append("\"");
                                // condition
                                compatibility.getDeviceCondition().forEach((condition, parameterConstraintMap) -> {
                                    if ("Compare".equals(condition.getName())) {    // TODO: compare with name is dangerous
                                        return;
                                    }
                                    builder.append(" { int(").append(variableName).append(".").append(condition.getFunctionName()).append("()) }");
                                });
                                // value
                                compatibility.getDeviceValue().forEach((value, constraint) -> builder.append(" { ").append(parseValueVariableTerm(projectDeviceList, value)).append(" }"));
                                builder.append("\\n'").append(NEW_LINE);
                            }
                        }
                    }
                }
            }
        }
        builder.append(INDENT).append("return retval").append(NEW_LINE);
    }

    private void appendMainFunction() {

        builder.append(NEW_LINE);
        builder.append("async def main():").append(NEW_LINE);
        builder.append(INDENT).append("try:").append(NEW_LINE);

        /* Setup */
        builder.append(INDENT).append(INDENT).append("MP.unsetAllPins()").append(NEW_LINE);

        // TODO: instantiate cloud platform
//            for (CloudPlatform cloudPlatform: project.getCloudPlatformUsed()) {
//                String cloudPlatformLibName = cloudPlatform.getLibName();
//                String specificCloudPlatformLibName = project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform);
//
//                List<String> cloudPlatformParameterValues = cloudPlatform.getUnmodifiableCloudParameterMap().stream()
//                        .map(param -> "\"" + project.getCloudPlatformParameter(cloudPlatform, param) + "\"").collect(Collectors.toList());
//                builder.append(cloudPlatformLibName).append("* ").append(parseCloudPlatformVariableName(cloudPlatform))
//                        .append(" = new ").append(specificCloudPlatformLibName)
//                        .append("(").append(String.join(", ", cloudPlatformParameterValues)).append(");").append(NEW_LINE);
//            }
        builder.append(NEW_LINE);
        for (List<ProjectDevice> projectDeviceList: projectDeviceGroup) {
            if (projectDeviceList.isEmpty()) {
                throw new IllegalStateException();
            }
            Optional<ActualDevice> actualDeviceOptional = configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDeviceList.get(0));
            if (actualDeviceOptional.isEmpty()) {
                throw new IllegalStateException();
            }
            ActualDevice actualDevice = actualDeviceOptional.get();

            List<String> args = new ArrayList<>();

            DeviceConnection connection = project.getProjectConfiguration().getDeviceConnection(projectDeviceList.get(0));
            if (connection != DeviceConnection.NOT_CONNECTED) {
                Map<Connection, Connection> connectionMap = connection.getConsumerProviderConnections();
                for (Connection connectionConsume: actualDevice.getConnectionConsumeByOwnerDevice(projectDeviceList.get(0))) {
                    Connection connectionProvide = connectionMap.get(connectionConsume);
                    for (int i=connectionConsume.getPins().size()-1; i>=0; i--) {
                        Pin pinConsume = connectionConsume.getPins().get(i);
                        Pin pinProvide = connectionProvide.getPins().get(i);
                        if (pinConsume.getFunction().get(0) == PinFunction.NO_FUNCTION) {
                            continue;
                        }
                        List<PinFunction> possibleFunctionConsume = pinConsume.getFunction().get(0).getPossibleConsume();
                        for (PinFunction function: possibleFunctionConsume) {
                            if (pinProvide.getFunction().contains(function)) {
                                if (PIN_FUNCTION_WITH_CODES.contains(function)) {
                                    if (!pinProvide.getCodingName().isEmpty()) {
                                        args.add(pinProvide.getCodingName());
                                    } else {
                                        args.add(pinProvide.getRefTo());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            // property for the generic device
            for (Property p : actualDevice.getProperty()) {
                ProjectDevice projectDevice = configuration.getRootDevice(projectDeviceList.get(0));
                Object value = configuration.getPropertyValue(projectDevice, p);
                if (value == null) {
                    throw new IllegalStateException("Property hasn't been set");
                }
                switch (p.getDataType()) {
                    case INTEGER:
                    case DOUBLE:
                        args.add(String.valueOf(((NumberWithUnit) value).getValue()));
                        break;
                    case INTEGER_ENUM:
                    case BOOLEAN_ENUM:
                        args.add(String.valueOf(value));
                        break;
                    case STRING:
                    case ENUM:
                        args.add("\"" + value + "\"");
                        break;
                    case AZURE_COGNITIVE_KEY:
                        AzureCognitiveServices acs = (AzureCognitiveServices) value;
                        args.add("\"" + acs.getLocation().toLowerCase() + "\"");
                        args.add("\"" + acs.getKey1() + "\"");
                        break;
                    case AZURE_IOTHUB_KEY:
                        AzureIoTHubDevice azureIoTHubDevice = (AzureIoTHubDevice) value;
                        args.add("\"" + azureIoTHubDevice.getConnectionString() + "\"");
                        break;
                    default:
                        throw new IllegalStateException("Property (" + value + ") hasn't been supported yet");
                }
            }

//            // Cloud Platform instance
//            CloudPlatform cloudPlatform = actualDevice.getCloudConsume();
//            if (cloudPlatform != null) {
//                args.add(parseCloudPlatformVariableName(cloudPlatform));
//            }

            builder.append(INDENT).append(INDENT).append(parseDeviceVariableName(projectDeviceList))
                    .append(" = ").append(actualDevice.getMpLibrary(project.getSelectedPlatform()))
                    .append("(").append(String.join(", ", args)).append(")").append(NEW_LINE);
        }
        builder.append(NEW_LINE);
        /* End Setup */

        /* Loop */
        builder.append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("while True:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("await asyncio.sleep(0)").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("MP.update()").append(NEW_LINE);
        /* End Loop */

        /* Handle Interrupt */
        builder.append(INDENT).append("except KeyboardInterrupt:").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("MP.cleanup()").append(NEW_LINE);
        /* End Handle Interrupt */

        builder.append(NEW_LINE);

        builder.append("if __name__ == '__main__':").append(NEW_LINE);
        builder.append(INDENT).append("MPInteractive.start(update_fn=main, action_fn=processCommand, message_fn=gen_message)").append(NEW_LINE);
    }

    private void appendHeader() {
        builder.append("import time").append(NEW_LINE);
        builder.append("import asyncio").append(NEW_LINE);
        builder.append("from MakerPlayground import MP, MPInteractive").append(NEW_LINE);

        // generate include
        Stream<String> device_libs = project.getAllDeviceUsed().stream()
                .filter(projectDevice -> configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isPresent())
                .map(projectDevice -> configuration.getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).orElseThrow().getMpLibrary(project.getSelectedPlatform()));
        Stream<String> cloud_libs = project.getCloudPlatformUsed().stream()
                .flatMap(cloudPlatform -> Stream.of(cloudPlatform.getLibName(), project.getSelectedController().getCloudPlatformLibraryName(cloudPlatform)));
        Stream.concat(device_libs, cloud_libs).distinct().sorted().forEach(s -> builder.append(parseImportStatement(s)).append(NEW_LINE));
        builder.append(NEW_LINE);
    }

    private List<ProjectDevice> searchGroup(ProjectDevice projectDevice) {
        Optional<List<ProjectDevice>> projectDeviceOptional = projectDeviceGroup.stream().filter(projectDeviceList -> projectDeviceList.contains(projectDevice)).findFirst();
        if (projectDeviceOptional.isEmpty()) {
            throw new IllegalStateException("Device that its value is used in the project must be exists in the device group.");
        }
        return projectDeviceOptional.get();
    }

    private void appendProcessCommand() {
        builder.append(NEW_LINE);;

        builder.append("def processCommand(cmd):").append(NEW_LINE);
//        builder.append(INDENT).append("MPInteractive.log(cmd)").append(NEW_LINE);
        builder.append(INDENT).append("args = [x.replace('\"', '') for x in cmd.split()]").append(NEW_LINE);

        boolean firstIf = true;
        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            if (projectDevice.getGenericDevice().hasAction()) {
                String variableName = parseDeviceVariableName(searchGroup(projectDevice));
                builder.append(INDENT).append(firstIf ? "if " : "elif ").append("args[0] == \"").append(projectDevice.getName()).append("\":").append(NEW_LINE);
                firstIf = false;

                if (project.getProjectConfiguration().getActualDevice(projectDevice).isPresent()) {
                    ActualDevice actualDevice = project.getProjectConfiguration().getActualDevice(projectDevice).get();
                    for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                        Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);

                        AtomicInteger j = new AtomicInteger();
                        compatibility.getDeviceAction().forEach((action, parameterConstraintMap) -> {
                            builder.append(INDENT + INDENT + (j.getAndIncrement() == 0 ? "if " : "elif ")).append("args[1] == \"").append(action.getName()).append("\" and len(args) == ").append(action.getParameter().size() + 2).append(":").append(NEW_LINE);
                            if (action.getParameter().size() == 1 && action.getParameter().get(0).getDataType() == DataType.RECORD) {
                                builder.append(INDENT).append(INDENT).append(INDENT).append("rec = dict()").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("entries = args[2][1:-1].split('][')").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("for entry in entries:").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("key, value = entry.split(',')").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("rec[key] = value").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName()).append("(rec)").append(NEW_LINE);
                            } else {
                                List<String> taskParameter = new ArrayList<>();
                                for (int i=0; i<action.getParameter().size(); i++) {
                                    Parameter parameter = action.getParameter().get(i);
                                    switch (parameter.getDataType()) {
                                        case DOUBLE:
                                            taskParameter.add("float(args[" + (i+2) + "])");
                                            break;
                                        case INTEGER:
                                            taskParameter.add("int(args[" + (i+2) + "])");
                                            break;
                                        default:
                                            taskParameter.add("args[" + (i+2) + "]");
                                            break;
                                    }

                                }
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName())
                                        .append("(").append(String.join(", ", taskParameter)).append(")").append(NEW_LINE);
                            }
                        });
                    }
                }
            }
        }
        builder.append(NEW_LINE);
    }
}
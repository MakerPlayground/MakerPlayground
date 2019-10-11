package io.makerplayground.ui.explorer;

import io.makerplayground.device.actual.*;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.DeviceMapperResult;
import io.makerplayground.generator.source.ArduinoCodeGenerator;
import io.makerplayground.generator.source.SourceCodeError;
import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.generator.source.Utility;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import io.makerplayground.util.AzureCognitiveServices;
import io.makerplayground.util.AzureIoTHubDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InteractiveCodeGenerator {

    public static SourceCodeResult generateCode(InteractiveModel interactiveModel, Project project) {
        // Check if all used devices are assigned.
        if (DeviceMapper.validateDeviceAssignment(project) != DeviceMapperResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }

        InteractiveCodeGenerator generator = new InteractiveCodeGenerator(interactiveModel, project);
        generator.appendHeader();
        generator.appendGlobalVariable();
        generator.appendDeviceInstantiation();
        generator.appendSetupFunction();
        generator.appendProcessCommand();
        generator.appendLoopFunction();
        return new SourceCodeResult(generator.sb.toString());
    }

    private static final String INDENT = "    ";
    private static final String NEW_LINE = "\n";

    private final InteractiveModel interactiveModel;
    private final Project project;
    private final List<ProjectDevice> devices;
    private final StringBuilder sb = new StringBuilder();

    private InteractiveCodeGenerator(InteractiveModel interactiveModel, Project project) {
        this.interactiveModel = interactiveModel;
        this.project = project;
        // TODO: checked
        this.devices = project.getDevice().stream()
                .filter(projectDevice -> !projectDevice.isMergeToOtherDevice() &&
                        projectDevice.getActualDevice().getDeviceType() != DeviceType.VIRTUAL)
                .collect(Collectors.toUnmodifiableList());
    }

    private void appendHeader() {
        sb.append("#include <Arduino.h>").append(NEW_LINE);
        devices.stream().filter(ProjectDevice::isActualDeviceSelected)
                .map(projectDevice -> projectDevice.getActualDevice().getMpLibrary(project.getPlatform()))
                .distinct()
                .forEach(s -> sb.append("#include \"").append(s).append(".h\"").append(NEW_LINE));
        sb.append(NEW_LINE);
    }

    private void appendGlobalVariable() {
        sb.append("uint8_t statusCode = 0;").append(NEW_LINE);
        sb.append("unsigned long lastSendTime = 0;").append(NEW_LINE);
        sb.append("unsigned long currentTime = 0;").append(NEW_LINE);
        sb.append("const int SEND_INTERVAL = 100;").append(NEW_LINE);
        sb.append("char serialBuffer[128];").append(NEW_LINE);
        sb.append("uint8_t serialBufferIndex = 0;").append(NEW_LINE);
        sb.append("char* commandArgs[10];").append(NEW_LINE);
        sb.append(NEW_LINE);
    }

    private void appendDeviceInstantiation() {
        for (ProjectDevice projectDevice : devices) {
            // skip device that share actual device with other project device
            if (!projectDevice.isActualDeviceSelected()) {
                continue;
            }
            sb.append(projectDevice.getActualDevice().getMpLibrary(project.getPlatform()))
                    .append(" ").append(ArduinoCodeGenerator.parseDeviceVariableName(projectDevice));
            List<String> args = new ArrayList<>();
            if (!projectDevice.getActualDevice().getConnectivity().contains(Peripheral.NOT_CONNECTED)) {
                // port
                for (Peripheral p : projectDevice.getActualDevice().getConnectivity()) {
                    if ((p.getConnectionType() != ConnectionType.I2C) && (p.getConnectionType() != ConnectionType.MP_I2C)
                            && (p.getConnectionType() != ConnectionType.UART)) {
                        List<DevicePort> port = projectDevice.getDeviceConnection().get(p);
                        if (port == null) {
                            throw new IllegalStateException("Port hasn't been selected!!!");
                        }
                        // prefer alias name over the actual port name if existed as the latter is used for displaying to the user
                        for (DevicePort devicePort : port) {
                            if (p.isI2C1() || p.isI2C() || p.isSPI() || p.isRS485()) {
                                continue;
                            }
                            if (!devicePort.getAlias().isEmpty()) {
                                if (p.isDual()) {
                                    args.addAll(devicePort.getAlias());
                                } else if (p.isSecondaryPortOnly()) {
                                    args.add(devicePort.getAlias().get(1));
                                } else {    // normal WIRE or port's primary pin
                                    args.add(devicePort.getAlias().get(0));
                                }
                            } else {
                                args.add(devicePort.getName());
                            }
                        }
                    }
                }
            }
            // property for the generic device
            for (Property p : projectDevice.getActualDevice().getProperty()) {
                Object value = projectDevice.getPropertyValue(p);
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

            if (!args.isEmpty()) {
                sb.append("(").append(String.join(", ", args)).append(");").append(NEW_LINE);
            } else {
                sb.append(";").append(NEW_LINE);
            }
        }
        sb.append(NEW_LINE);
    }

    private void appendSetupFunction() {
        sb.append("void setup() {").append(NEW_LINE);
        sb.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);
        sb.append(NEW_LINE);
        for (ProjectDevice projectDevice : devices) {
            if (!projectDevice.isActualDeviceSelected()) {
                continue;
            }
            String variableName = ArduinoCodeGenerator.parseDeviceVariableName(projectDevice);
            sb.append(INDENT).append("statusCode = ").append(variableName).append(".init();").append(NEW_LINE);
            sb.append(INDENT).append("if (statusCode != 0) {").append(NEW_LINE);
//            sb.append(INDENT).append(INDENT).append("MP_ERR(\"").append(projectDevice.getName()).append("\", status_code);").append(NEW_LINE);
            sb.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            sb.append(INDENT).append("}").append(NEW_LINE);
            sb.append(NEW_LINE);
        }
        sb.append("}").append(NEW_LINE);
        sb.append(NEW_LINE);
    }

    private void appendProcessCommand() {
        sb.append("void processCommand() {").append(NEW_LINE);
        sb.append(INDENT).append("uint8_t i = 0, argsCount = 0, length = strlen(serialBuffer);").append(NEW_LINE);
        sb.append(INDENT).append("while (i < length) {").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("if (serialBuffer[i] == '\"') {").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("commandArgs[argsCount] = &serialBuffer[i];").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("argsCount++;").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("while (i < length && serialBuffer[i] != '\"') {").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[i] = '\\0';").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        sb.append(INDENT).append("}").append(NEW_LINE);
        sb.append(NEW_LINE);

        boolean firstCondition = true;
        for (ProjectDevice projectDevice : devices) {
            if (projectDevice.getGenericDevice().hasAction()) {
                String variableName = ArduinoCodeGenerator.parseDeviceVariableName(projectDevice);
                sb.append(INDENT).append(firstCondition ? "if " : "else if ").append("(strcmp_P(commandArgs[0], (PGM_P) F(\"")
                        .append(variableName).append("\")) == 0) {").append(NEW_LINE);
                firstCondition = false;

                List<Action> actions = projectDevice.getGenericDevice().getAction();
                for (int j=0; j<actions.size(); j++) {
                    Action action = actions.get(j);
                    sb.append(j == 0 ? INDENT + INDENT + "if " : "else if ").append("(strcmp_P(commandArgs[1], (PGM_P) F(\"")
                            .append(action.getName()).append("\")) == 0 && argsCount == ").append(action.getParameter().size() + 2)
                            .append(") {").append(NEW_LINE);

                    List<String> taskParameter = new ArrayList<>();
                    for (int i=0; i<action.getParameter().size(); i++) {
                        Parameter parameter = action.getParameter().get(i);
                        switch (parameter.getDataType()) {
                            case DOUBLE:
                                taskParameter.add("atof(commandArgs[" + (i+2) + "])");
                                break;
                            case INTEGER:
                                taskParameter.add("atoi(commandArgs[" + (i+2) + "])");
                                break;
                            default:
                                taskParameter.add("commandArgs[" + (i+2) + "]");
                                break;
                        }

                    }
                    sb.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName())
                            .append("(").append(String.join(", ", taskParameter)).append(");").append(NEW_LINE);

                    sb.append(INDENT).append(INDENT).append("} ");
                }

                sb.append(NEW_LINE).append(INDENT).append("}").append(NEW_LINE);
            }
        }
        sb.append("}").append(NEW_LINE);
        sb.append(NEW_LINE);
    }

    private void appendLoopFunction() {
        sb.append("void loop() {").append(NEW_LINE);

        sb.append(INDENT).append("currentTime = millis();").append(NEW_LINE);
        sb.append(NEW_LINE);

        for (ProjectDevice projectDevice : devices) {
            String variableName = ArduinoCodeGenerator.parseDeviceVariableName(projectDevice);
            sb.append(INDENT).append(variableName).append(".update(currentTime);").append(NEW_LINE);
        }
        sb.append(NEW_LINE);

        sb.append(INDENT).append("if (currentTime - lastSendTime >= SEND_INTERVAL) {").append(NEW_LINE);
        for (ProjectDevice projectDevice : devices) {
            if (projectDevice.getGenericDevice().hasCondition() || projectDevice.getGenericDevice().hasValue()) {
                String variableName = ArduinoCodeGenerator.parseDeviceVariableName(projectDevice);
                sb.append(INDENT).append(INDENT).append("Serial.print(F(\"").append(variableName).append("\"));").append(NEW_LINE);
                // condition
                for (Action condition : projectDevice.getGenericDevice().getCondition()) {
                    if (condition.getName().equals("Compare")) {    // TODO: compare with name is dangerous
                        continue;
                    }
                    sb.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                    sb.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".").append(condition.getFunctionName()).append("());").append(NEW_LINE);
                }
                // value
                Set<Value> supportedValue = projectDevice.getActualDevice().getSupportedValue().get(projectDevice.getGenericDevice()).keySet();
                for (Value value : supportedValue) {
                    sb.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                    sb.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".get").append(value.getName()).append("());").append(NEW_LINE);
                }
                sb.append(INDENT).append(INDENT).append("Serial.println();").append(NEW_LINE);
                sb.append(NEW_LINE);
            }
        }
        sb.append(INDENT).append(INDENT).append("lastSendTime = millis();").append(NEW_LINE);
        sb.append(INDENT).append("}").append(NEW_LINE);

        sb.append(INDENT).append("while (Serial.available() > 0) {").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = Serial.read();").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("if (serialBuffer[serialBufferIndex] == '\\r' || serialBuffer[serialBufferIndex] == '\\n') {").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = '\\0';").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("processCommand();").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("serialBufferIndex = 0;").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        sb.append(INDENT).append(INDENT).append("serialBufferIndex++;").append(NEW_LINE);
        sb.append(INDENT).append("}").append(NEW_LINE);

        sb.append("}").append(NEW_LINE);
    }
}

package io.makerplayground.generator.source;

import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import io.makerplayground.project.ProjectDevice;

import java.util.List;

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
        generator.appendTest();
//        generator.appendGlobalVariable();
//        generator.builder.append(getInstanceVariablesCode(project, generator.projectDeviceGroup));
//        generator.builder.append(getSetupFunctionCode(project, generator.projectDeviceGroup, false));
//        generator.appendProcessCommand();
//        generator.appendLoopFunction();
//        System.out.println(generator.builder.toString());
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendHeader() {
        builder.append("import time").append(NEW_LINE);
        builder.append("from MakerPlayground import MP, InteractiveServer").append(NEW_LINE);
    }

    private void appendTest() {
        builder.append(NEW_LINE);
        builder.append("def do_action(message):").append(NEW_LINE);
        builder.append(INDENT).append("print(message)").append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("def gen_message():").append(NEW_LINE);
        builder.append(INDENT).append("print(time.time())").append(NEW_LINE);
        builder.append(INDENT).append("return str(time.time())").append(NEW_LINE);
        builder.append(NEW_LINE);
        builder.append("InteractiveServer.start(do_action_fn=do_action, message_fn=gen_message)").append(NEW_LINE);
    }
}

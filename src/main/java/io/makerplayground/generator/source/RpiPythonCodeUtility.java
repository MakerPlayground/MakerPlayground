package io.makerplayground.generator.source;

import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;

import java.util.List;
import java.util.stream.Collectors;

public class RpiPythonCodeUtility {

    static final String INDENT = "    ";
    static final String NEW_LINE = "\n";

    static String parseImportStatement(String libName) {
        return "from " + libName + " import " + libName;
    }

    static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "MP.current_" + nodeElement.getName().replace(" ", "_");
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

//    static String parseSceneFunctionName(NodeElement node) {
//        if (node instanceof Begin) {
//            return "scene_" + node.getName().replace(" ", "_");
//        } else if (node instanceof Scene) {
//            return "scene_" + node.getName().replace(" ", "_");
//        }
//        throw new IllegalStateException("Not support scene function name for {" + node + "}");
//    }

//    static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
//        if (nodeBeforeConditions instanceof Begin) {
//            return "beginScene_conditions";
//        } else if (nodeBeforeConditions instanceof Scene) {
//            return parseSceneFunctionName(nodeBeforeConditions) + "_conditions";
//        } else if (nodeBeforeConditions instanceof Condition) {
//            throw new IllegalStateException("Not support condition function name for condition after condition {" + nodeBeforeConditions + "}");
//        }
//        throw new IllegalStateException("Not support condition function name for {" + nodeBeforeConditions + "}");
//    }

//    static String parseCloudPlatformVariableName(CloudPlatform cloudPlatform) {
//        return "_" + cloudPlatform.getLibName().replace(" ", "_");
//    }

//    static String parseDeviceName(ProjectConfiguration configuration, ProjectDevice projectDevice) {
//        if (configuration.getIdenticalDevice(projectDevice).isPresent()) {
//            return "_" + configuration.getIdenticalDevice(projectDevice).orElseThrow().getName().replace(" ", "_").replace(".", "_");
//        } else if (configuration.getActualDevice(projectDevice).isPresent()) {
//            return "_" + projectDevice.getName().replace(" ", "_").replace(".", "_");
//        } else {
//            throw new IllegalStateException("Actual device of " + projectDevice.getName() + " hasn't been selected!!!");
//        }
//    }

    static String parseValueVariableTerm(List<ProjectDevice> projectDeviceList, Value value) {
        return parseDeviceVariableName(projectDeviceList) + ".get" + value.getName().replace(' ', '_').replace(".", "_") + "()";
    }

//    static String parseProjectValue(ProjectConfiguration configuration,  ProjectDevice projectDevice, Value value) {
//        return parseDeviceVariableName(configuration, projectDevice) + ".get" + value.getName().replace(" ", "_") + "()";
//    }

//    private String parseDeviceTaskVariableName(ProjectDevice device) {
//        return parseDeviceName(device) + "_Task";
//    }

    static String parseDeviceVariableName(List<ProjectDevice> projectDeviceList) {
        return "MP.devices['" + projectDeviceList.stream().map(ProjectDevice::getName).map(s -> s.replace(' ', '_')).collect(Collectors.joining("_")) + "']";
//        return "MP.devices['" + parseDeviceName(configuration, projectDevice) + "']";
    }

//    static String parseDeviceExpressionVariableName(ProjectConfiguration configuration, ProjectDevice device) {
//        return "MP.expressions['" + parseDeviceName(configuration, device) + "']";
//    }

    static String parseNodeFunctionName(NodeElement node) {
        if (node instanceof Scene) {
            return "scene_" + node.getNameSanitized();
        } else if (node instanceof Begin) {
            return "begin_" + node.getNameSanitized();
        } else if (node instanceof Delay) {
            return "delay_" + node.getNameSanitized();
        } else if (node instanceof Condition) {
            return "condition_" + node.getNameSanitized();
        }
        throw new IllegalStateException("Not support scene function displayName for {" + node + "}");
    }

    static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
        if (nodeBeforeConditions instanceof Begin || nodeBeforeConditions instanceof Scene || nodeBeforeConditions instanceof Delay || nodeBeforeConditions instanceof Condition) {
            return parseNodeFunctionName(nodeBeforeConditions) + "_options";
        }
        throw new IllegalStateException("Not support condition function displayName for {" + nodeBeforeConditions + "}");
    }
}

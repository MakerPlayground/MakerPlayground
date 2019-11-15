package io.makerplayground.generator.source;

import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;

import java.util.List;
import java.util.stream.Collectors;

class ArduinoCodeUtility {
    static String parseDeviceVariableName(List<ProjectDevice> projectDeviceList) {
        if (projectDeviceList.isEmpty()) {
            throw new IllegalStateException("Cannot get device name if there is no devices");
        }
        return "_" + projectDeviceList.stream().map(ProjectDevice::getName).map(s -> s.replace(' ', '_')).collect(Collectors.joining("_"));
    }

    static String parseValueVariableTerm(List<ProjectDevice> projectDeviceList, Value value) {
        return parseDeviceVariableName(projectDeviceList) + ".get" + value.getName().replace(' ', '_') + "()";
    }

    static String parseCloudPlatformVariableName(CloudPlatform cloudPlatform) {
        return "_" + cloudPlatform.getLibName().replace(' ', '_');
    }

    static String parseIncludeStatement(String libName) {
        return "#include \"" + libName + ".h\"";
    }

    static String parseSceneFunctionName(NodeElement node) {
        if (node instanceof Scene) {
            return "scene_" + ((Scene) node).getName().replace(' ', '_');
        } else if (node instanceof Begin) {
            return "scene_" + ((Begin) node).getName().replace(' ', '_');
        }
        throw new IllegalStateException("Not support scene function displayName for {" + node + "}");
    }

    static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "current_" + ((Begin) nodeElement).getName().replace(' ', '_');
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

    static String parseConditionFunctionName(NodeElement nodeBeforeConditions) {
        if (nodeBeforeConditions instanceof Begin || nodeBeforeConditions instanceof Scene) {
            return parseSceneFunctionName(nodeBeforeConditions) + "_conditions";
        } else if (nodeBeforeConditions instanceof Condition) {
            throw new IllegalStateException("Not support condition function displayName for condition after condition {" + nodeBeforeConditions + "}");
        }
        throw new IllegalStateException("Not support condition function displayName for {" + nodeBeforeConditions + "}");
    }
}

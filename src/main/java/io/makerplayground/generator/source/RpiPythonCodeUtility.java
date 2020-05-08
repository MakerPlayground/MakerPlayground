/*
 * Copyright (c) 2020. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.generator.source;

import io.makerplayground.device.actual.PinFunction;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RpiPythonCodeUtility {

    static final String INDENT = "    ";
    static final String NEW_LINE = "\n";

    static final Set<PinFunction> PIN_FUNCTION_WITH_CODES = Set.of(
            PinFunction.DIGITAL_IN, PinFunction.DIGITAL_OUT,
            PinFunction.ANALOG_IN, PinFunction.ANALOG_OUT,
            PinFunction.PWM_OUT,
            PinFunction.INTERRUPT_LOW, PinFunction.INTERRUPT_HIGH, PinFunction.INTERRUPT_CHANGE, PinFunction.INTERRUPT_RISING, PinFunction.INTERRUPT_FALLING,
            PinFunction.HW_SERIAL_RX, PinFunction.HW_SERIAL_TX, PinFunction.SW_SERIAL_RX, PinFunction.SW_SERIAL_TX
    );

    static String parseImportStatement(String libName) {
        return "from " + libName + " import " + libName;
    }

    static String parsePointerName(NodeElement nodeElement) {
        if (nodeElement instanceof Begin) {
            return "MP.current_" + nodeElement.getName().replace(" ", "_");
        }
        throw new IllegalStateException("No pointer to function for Scene and Condition");
    }

    static String parseValueVariableTermForInteractive(List<ProjectDevice> projectDeviceList, Value value) {
        if (value.getType() == DataType.IMAGE) {
            return "MPImage.get_preview_base64(" + parseValueVariableTerm(projectDeviceList, value) + ")";
        } else {
            return parseValueVariableTerm(projectDeviceList, value);
        }
    }

    static String parseValueVariableTerm(List<ProjectDevice> projectDeviceList, Value value) {
        return parseDeviceVariableName(projectDeviceList) + ".get" + value.getName().replace(' ', '_').replace(".", "_") + "()";
    }

    static String parseDeviceVariableName(List<ProjectDevice> projectDeviceList) {
        return "MP.devices[\"" + projectDeviceList.stream().map(ProjectDevice::getName).map(s -> s.replace(' ', '_')).collect(Collectors.joining("_")) + "\"]";
    }

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

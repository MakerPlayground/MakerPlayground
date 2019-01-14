/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

package io.makerplayground.device.actual;

import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.Constraint;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class IntegratedActualDevice extends ActualDevice {
    IntegratedActualDevice(String model, Map<Platform, String> classnames, Map<Platform, List<String>> externalLibraries,
                           List<DevicePort> port, List<Peripheral> connectivity,
                           Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedAction,
                           Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedCondition,
                           Map<GenericDevice, Map<Value, Constraint>> supportedValue) {
        super("", "", model, "", 0, 0, DeviceType.INTEGRATED, "", null, FormFactor.NONE
                , classnames, externalLibraries, null, port, connectivity
                , supportedAction, supportedCondition, supportedValue, Collections.emptyList()
                , Collections.emptyMap(), Collections.emptyList());
    }
}

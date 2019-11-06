/*
 * Copyright (c) 2019. The Maker Playground Authors.
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

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.CloudPlatform;
import io.makerplayground.device.actual.Compatibility;
import io.makerplayground.device.actual.Platform;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DataType;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.generator.devicemapping.ProjectLogic;
import io.makerplayground.generator.devicemapping.ProjectMappingResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InteractiveArduinoCodeGenerator extends ArduinoCodeGenerator {

    private InteractiveArduinoCodeGenerator(Project project) {
        super(project);
    }

    static SourceCodeResult generateCode(Project project) {
        // Check if all used devices are assigned.
        if (ProjectLogic.validateDeviceAssignment(project) != ProjectMappingResult.OK) {
            return new SourceCodeResult(SourceCodeError.NOT_SELECT_DEVICE_OR_PORT, "-");
        }
        if (!Utility.validateDeviceProperty(project)) {
            return new SourceCodeResult(SourceCodeError.MISSING_PROPERTY, "-");   // TODO: add location
        }

        InteractiveArduinoCodeGenerator generator = new InteractiveArduinoCodeGenerator(project);
        generator.appendHeader(project.getUnmodifiableProjectDevice(), project.getAllCloudPlatforms());
        generator.appendGlobalVariable();
        generator.appendInstanceVariables(project.getAllCloudPlatforms(), project.getUnmodifiableProjectDevice());
        generator.appendSetupFunction();
        generator.appendProcessCommand();
        generator.appendLoopFunction();
//        System.out.println(generator.builder.toString());
        return new SourceCodeResult(generator.builder.toString());
    }

    private void appendGlobalVariable() {
        builder.append("uint8_t statusCode = 0;").append(NEW_LINE);
        builder.append("unsigned long lastSendTime = 0;").append(NEW_LINE);
//        builder.append("unsigned long currentTime = 0;").append(NEW_LINE);
        builder.append("const int SEND_INTERVAL = 100;").append(NEW_LINE);
        builder.append("char serialBuffer[256];").append(NEW_LINE);
        builder.append("uint8_t serialBufferIndex = 0;").append(NEW_LINE);
        builder.append("char* commandArgs[10];").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    @Override
    void appendSetupFunction() {
        builder.append("void setup() {").append(NEW_LINE);
        builder.append(INDENT).append("Serial.begin(115200);").append(NEW_LINE);

        if (project.getSelectedPlatform().equals(Platform.ARDUINO_ESP32)) {
            builder.append(INDENT).append("analogSetWidth(10);").append(NEW_LINE);
        }

        for (CloudPlatform cloudPlatform : project.getAllCloudPlatforms()) {
            String cloudPlatformVariableName = parseCloudPlatformVariableName(cloudPlatform);
            builder.append(INDENT).append("status_code = ").append(cloudPlatformVariableName).append("->init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(cloudPlatform.getDisplayName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            if (configuration.getIdenticalDevice(projectDevice).isPresent()) {
                continue;
            }
            String variableName = parseDeviceVariableName(configuration, projectDevice);
            builder.append(INDENT).append("status_code = ").append(variableName).append(".init();").append(NEW_LINE);
            builder.append(INDENT).append("if (status_code != 0) {").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("MP_ERR(\"").append(projectDevice.getName()).append("\", status_code);").append(NEW_LINE);
            builder.append(INDENT).append(INDENT).append("while(1);").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
            builder.append(NEW_LINE);
        }
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendProcessCommand() {
        builder.append("void processCommand() {").append(NEW_LINE);
        builder.append(INDENT).append("uint8_t i = 0, argsCount = 0, length = strlen(serialBuffer);").append(NEW_LINE);
        builder.append(INDENT).append("while (i < length) {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("if (serialBuffer[i] == '\"') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("commandArgs[argsCount] = &serialBuffer[i];").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("argsCount++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("while (i < length && serialBuffer[i] != '\"') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[i] = '\\0';").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("i++;").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);
        builder.append(NEW_LINE);

        boolean firstCondition = true;
        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            if (projectDevice.getGenericDevice().hasAction()) {
                String variableName = ArduinoCodeGenerator.parseDeviceVariableName(configuration, projectDevice);
                builder.append(INDENT).append(firstCondition ? "if " : "else if ").append("(strcmp_P(commandArgs[0], (PGM_P) F(\"")
                        .append(projectDevice.getName()).append("\")) == 0) {").append(NEW_LINE);
                firstCondition = false;

                if (project.getProjectConfiguration().getActualDevice(projectDevice).isPresent()) {
                    ActualDevice actualDevice = project.getProjectConfiguration().getActualDevice(projectDevice).get();
                    for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                        Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);

                        AtomicInteger j= new AtomicInteger();
                        compatibility.getDeviceAction().forEach((action, parameterConstraintMap) -> {
                            builder.append(j.getAndIncrement() == 0 ? INDENT + INDENT + "if " : "else if ").append("(strcmp_P(commandArgs[1], (PGM_P) F(\"")
                                    .append(action.getName()).append("\")) == 0 && argsCount == ").append(action.getParameter().size() + 2)
                                    .append(") {").append(NEW_LINE);

                            if (action.getParameter().size() == 1 && action.getParameter().get(0).getDataType() == DataType.RECORD) {
                                builder.append(INDENT).append(INDENT).append(INDENT).append("Record rec;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("uint8_t j = 0, keyValueCount = 0, length_rec = strlen(commandArgs[2]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("char* keyValues[20];").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && keyValueCount < 20) {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("if (commandArgs[2][j++] == '[') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("keyValues[keyValueCount++] = &(commandArgs[2][j]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && commandArgs[2][j] != ',') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("j++;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("commandArgs[2][j++] = '\\0';").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("keyValues[keyValueCount++] = &(commandArgs[2][j]);").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("while (j < length_rec && commandArgs[2][j] != ']') {").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("j++;").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("commandArgs[2][j++] = '\\0';").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("rec.put(keyValues[keyValueCount-2], atof(keyValues[keyValueCount-1]));").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append("}").append(NEW_LINE);
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName()).append("(rec);").append(NEW_LINE);
                            } else {
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
                                builder.append(INDENT).append(INDENT).append(INDENT).append(variableName).append(".").append(action.getFunctionName())
                                        .append("(").append(String.join(", ", taskParameter)).append(");").append(NEW_LINE);
                            }

                            builder.append(INDENT).append(INDENT).append("} ");
                        });
                    }
                }

                builder.append(NEW_LINE).append(INDENT).append("}").append(NEW_LINE);
            }
        }
        builder.append("}").append(NEW_LINE);
        builder.append(NEW_LINE);
    }

    private void appendLoopFunction() {
        builder.append("void loop() {").append(NEW_LINE);

        builder.append(INDENT).append("currentTime = millis();").append(NEW_LINE);
        builder.append(NEW_LINE);

        // allow all cloudplatform maintains their own tasks (e.g. connection)
        for (CloudPlatform cloudPlatform : project.getAllCloudPlatforms()) {
            builder.append(INDENT).append(parseCloudPlatformVariableName(cloudPlatform)).append("->update(currentTime);").append(NEW_LINE);
        }

        for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
            if (configuration.getIdenticalDevice(projectDevice).isPresent()) {
                continue;
            }
            String variableName = ArduinoCodeGenerator.parseDeviceVariableName(configuration, projectDevice);
            builder.append(INDENT).append(variableName).append(".update(currentTime);").append(NEW_LINE);
        }
        builder.append(NEW_LINE);

        if (!project.getUnmodifiableProjectDevice().isEmpty()) {
            builder.append(INDENT).append("if (currentTime - lastSendTime >= SEND_INTERVAL) {").append(NEW_LINE);
            for (ProjectDevice projectDevice : project.getUnmodifiableProjectDevice()) {
                if (project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).isPresent()) {
                    ActualDevice actualDevice = project.getProjectConfiguration().getActualDeviceOrActualDeviceOfIdenticalDevice(projectDevice).get();
                    for (GenericDevice genericDevice: actualDevice.getCompatibilityMap().keySet()) {
                        if (genericDevice == projectDevice.getGenericDevice()) {
                            Compatibility compatibility = actualDevice.getCompatibilityMap().get(genericDevice);
                            if (!compatibility.getDeviceCondition().isEmpty() || !compatibility.getDeviceValue().isEmpty()) {
                                String variableName = ArduinoCodeGenerator.parseDeviceVariableName(configuration, projectDevice);
                                builder.append(INDENT).append(INDENT).append("Serial.print(F(\"").append(projectDevice.getName()).append("\"));").append(NEW_LINE);
                                // condition
                                compatibility.getDeviceCondition().forEach((condition, parameterConstraintMap) -> {
                                    if (condition.getName().equals("Compare")) {    // TODO: compare with name is dangerous
                                        return;
                                    }
                                    builder.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                                    builder.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".").append(condition.getFunctionName()).append("());").append(NEW_LINE);
                                });
                                // value
                                compatibility.getDeviceValue().forEach((value, constraint) -> {
                                    builder.append(INDENT).append(INDENT).append("Serial.print(F(\" \"));").append(NEW_LINE);
                                    builder.append(INDENT).append(INDENT).append("Serial.print(").append(variableName).append(".get").append(value.getName()).append("());").append(NEW_LINE);
                                });
                                builder.append(INDENT).append(INDENT).append("Serial.println();").append(NEW_LINE);
                                builder.append(NEW_LINE);
                            }
                        }
                    }
                }
            }
            builder.append(INDENT).append(INDENT).append("lastSendTime = millis();").append(NEW_LINE);
            builder.append(INDENT).append("}").append(NEW_LINE);
        }

        builder.append(INDENT).append("while (Serial.available() > 0) {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = Serial.read();").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("if (serialBuffer[serialBufferIndex] == '\\r' || serialBuffer[serialBufferIndex] == '\\n') {").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBuffer[serialBufferIndex] = '\\0';").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("processCommand();").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("serialBufferIndex = 0;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append(INDENT).append("break;").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("}").append(NEW_LINE);
        builder.append(INDENT).append(INDENT).append("serialBufferIndex++;").append(NEW_LINE);
        builder.append(INDENT).append("}").append(NEW_LINE);

        builder.append("}").append(NEW_LINE);
    }
}
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

package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionDeserializer extends JsonDeserializer<Condition> {
    ObjectMapper mapper = new ObjectMapper();
    Project project;

    ConditionDeserializer(Project project) {
        this.project = project;

        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserSetting.class, new UserSettingDeserializer(project));
        mapper.registerModule(module);
    }

    @Override
    public Condition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = mapper.readTree(jsonParser);
        String name = node.get("name").asText();

        List<UserSetting> settings = mapper.readValue(node.get("setting").traverse(), new TypeReference<List<UserSetting>>() {});
        List<UserSetting> deviceSettings = settings.stream()
                .filter(setting -> project.getUnmodifiableProjectDevice().contains(setting.getDevice()))
                .collect(Collectors.toUnmodifiableList());
        List<UserSetting> virtualDeviceSettings = settings.stream()
                .filter(setting -> VirtualProjectDevice.devices.contains(setting.getDevice()))
                .collect(Collectors.toUnmodifiableList());

        JsonNode positionNode = node.get("position");
        double top = positionNode.get("top").asDouble();
        double left = positionNode.get("left").asDouble();
        double width = positionNode.get("width").asDouble();
        double height = positionNode.get("height").asDouble();

        return new Condition(top, left, width, height, name, deviceSettings, virtualDeviceSettings, project);
    }
}

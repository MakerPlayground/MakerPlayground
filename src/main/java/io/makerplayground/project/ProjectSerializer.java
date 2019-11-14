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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.makerplayground.version.ProjectVersionControl;

import java.io.IOException;

/**
 * Created by nuntipat on 6/25/2017 AD.
 */
public class ProjectSerializer extends JsonSerializer<Project> {

    @Override
    public void serialize(Project project, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("projectVersion", ProjectVersionControl.CURRENT_VERSION);
        jsonGenerator.writeStringField("projectName", project.getProjectName());

//        jsonGenerator.writeObjectFieldStart("controller");
//        jsonGenerator.writeStringField("platform", project.getSelectedPlatform().name());
//        if (project.getSelectedController() != null)
//            jsonGenerator.writeStringField("device", project.getSelectedController().getId());
//        else
//            jsonGenerator.writeStringField("device", "");
//        jsonGenerator.writeEndObject();

//        jsonGenerator.writeArrayFieldStart("cloudplatform");
//        for (CloudPlatform cloudPlatform : project.getCloudPlatformUsed()) {
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeStringField("name", cloudPlatform.name());
//            jsonGenerator.writeArrayFieldStart("parameter");
//            for (String parameterName : cloudPlatform.getParameter()) {
//                jsonGenerator.writeStartObject();
//                jsonGenerator.writeStringField("name", parameterName);
//                jsonGenerator.writeStringField("value", Objects.requireNonNullElse(
//                        project.getCloudPlatformParameter(cloudPlatform, parameterName), ""));
//                jsonGenerator.writeEndObject();
//            }
//            jsonGenerator.writeEndArray();
//            jsonGenerator.writeEndObject();
//        }
//        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("devices");
        for(ProjectDevice device : project.getUnmodifiableProjectDevice()) {
            mapper.writeValue(jsonGenerator, device);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("begins");
        for(Begin begin: project.getBegin()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", begin.getName());
            jsonGenerator.writeNumberField("top", begin.getTop());
            jsonGenerator.writeNumberField("left", begin.getLeft());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("scenes");
        for(Scene scene : project.getUnmodifiableScene()) {
            mapper.writeValue(jsonGenerator, scene);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("conditions");
        for(Condition condition : project.getUnmodifiableCondition()) {
            mapper.writeValue(jsonGenerator, condition);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("delays");
        for(Delay delay : project.getUnmodifiableDelay()) {
            mapper.writeValue(jsonGenerator, delay);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("lines");
        for(Line line : project.getUnmodifiableLine()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("source", line.getSource().getName());
            jsonGenerator.writeStringField("destination", line.getDestination().getName());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeObjectField("projectConfiguration", project.getProjectConfiguration());

        jsonGenerator.writeEndObject();
    }
}

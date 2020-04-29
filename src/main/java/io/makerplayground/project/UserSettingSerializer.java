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
import com.fasterxml.jackson.databind.SerializerProvider;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.term.*;

import java.io.IOException;
import java.util.Map;

public class UserSettingSerializer extends JsonSerializer<UserSetting> {

    @Override
    public void serialize(UserSetting userSetting, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("device", userSetting.getDevice().getName());
        if (userSetting.getAction() != null) {
            jsonGenerator.writeStringField("action", userSetting.getAction().getName());
        }
        if (userSetting.getCondition() != null) {
            jsonGenerator.writeStringField("condition", userSetting.getCondition().getName());
        }

        jsonGenerator.writeArrayFieldStart("valueMap");
        for (Map.Entry<Parameter, Expression> v : userSetting.getParameterMap().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", v.getKey().getName());
            jsonGenerator.writeStringField("type", v.getValue().getClass().getSimpleName());    // TODO: can be removed
            jsonGenerator.writeObjectField("value", v.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("expression");
        for (Map.Entry<Value, Expression> entry : userSetting.getExpression().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", entry.getKey().getName());
            jsonGenerator.writeBooleanField("enable", userSetting.getExpressionEnable().get(entry.getKey()));
            jsonGenerator.writeStringField("type", entry.getValue().getClass().getSimpleName());
            jsonGenerator.writeArrayFieldStart("expression");
            for (Term term : entry.getValue().getTerms()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("type", term.getType().name());
                if (term instanceof NumberWithUnitTerm) {
                    NumberWithUnit numberWithUnit = ((NumberWithUnitTerm) term).getValue();
                    jsonGenerator.writeObjectFieldStart("value");
                    jsonGenerator.writeStringField("value", String.valueOf(numberWithUnit.getValue()));
                    jsonGenerator.writeStringField("unit", numberWithUnit.getUnit().name());
                    jsonGenerator.writeEndObject();
                } else if (term instanceof StringTerm) {
                    jsonGenerator.writeStringField("value", ((StringTerm) term).getValue());
                } else if (term instanceof OperatorTerm) {
                    jsonGenerator.writeStringField("value", ((OperatorTerm) term).getValue().name());
                } else if (term instanceof ValueTerm) {
                    jsonGenerator.writeObjectFieldStart("value");
                    jsonGenerator.writeStringField("name", ((ValueTerm) term).getValue().getDevice().getName());
                    jsonGenerator.writeStringField("value", ((ValueTerm) term).getValue().getValue().getName());
                    jsonGenerator.writeEndObject();
                } else {
                    throw new IllegalStateException("Unknown term has been founded " + term);
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();

            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}

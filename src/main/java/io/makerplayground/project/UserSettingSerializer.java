package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.expression.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by USER on 13-Jul-17.
 */
public class UserSettingSerializer extends StdSerializer<UserSetting> {
    public UserSettingSerializer() {
        this(null);
    }

    public UserSettingSerializer(Class<UserSetting> t) {
        super(t);
    }

    @Override
    public void serialize(UserSetting userSetting, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("device", userSetting.getDevice().getName());
        jsonGenerator.writeStringField("action", userSetting.getAction().getName());

        jsonGenerator.writeArrayFieldStart("valueMap");
        for (Map.Entry<Parameter, Object> v : userSetting.getValueMap().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", v.getKey().getName());
            if (v.getValue() instanceof CustomNumberExpression) {
                jsonGenerator.writeStringField("type", "CustomNumberExpression");
            } else if (v.getValue() instanceof NumberWithUnit) {
                jsonGenerator.writeStringField("type", "NumberWithUnit");
            } else if (v.getValue() instanceof ProjectValue) {
                jsonGenerator.writeStringField("type", "ProjectValue");
            }
            jsonGenerator.writeObjectField("value", v.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("expression");
        for (Map.Entry<Value, Expression> entry : userSetting.getExpression().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", entry.getKey().getName());
            jsonGenerator.writeBooleanField("enable", entry.getValue().isEnable());
            if (entry.getValue() instanceof NumberInRangeExpression) {
                jsonGenerator.writeStringField("type", "simple");
            } else {
                throw new IllegalStateException("Unknown expression type");
            }
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

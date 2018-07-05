package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.*;
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
        for (Map.Entry<Parameter, Expression> v : userSetting.getValueMap().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", v.getKey().getName());
            jsonGenerator.writeStringField("type", v.getValue().getClass().getSimpleName());
            jsonGenerator.writeObjectField("value", v.getValue());
            if (v.getValue() instanceof CustomNumberExpression) {
                jsonGenerator.writeNumberField("maxValue", ((CustomNumberExpression) v.getValue()).getMaxValue());
                jsonGenerator.writeNumberField("minValue", ((CustomNumberExpression) v.getValue()).getMinValue());
            }
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("expression");
        for (Map.Entry<Value, Expression> entry : userSetting.getExpression().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("name", entry.getKey().getName());
            jsonGenerator.writeBooleanField("enable", entry.getValue().isEnable());
            jsonGenerator.writeStringField("type", entry.getValue().getClass().getSimpleName());
//            if (entry.getValue() instanceof NumberInRangeExpression) {
//                jsonGenerator.writeStringField("type", "numberInRange");
//            } else {
//                throw new IllegalStateException("Unknown expression type");
//            }
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

package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class ConditionSerializer extends StdSerializer<Condition> {
    public ConditionSerializer() {
        this(null);
    }

    public ConditionSerializer(Class<Condition> t) {
        super(t);
    }

    @Override
    public void serialize(Condition condition, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", condition.getName());

        jsonGenerator.writeArrayFieldStart("setting");
        for (UserSetting setting : condition.getSetting()) {
            mapper.writeValue(jsonGenerator,setting);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeObjectFieldStart("position");
        jsonGenerator.writeNumberField("top",condition.getTop());
        jsonGenerator.writeNumberField("left",condition.getLeft());
        jsonGenerator.writeNumberField("width",condition.getWidth());
        jsonGenerator.writeNumberField("height",condition.getHeight());
        jsonGenerator.writeEndObject();

        if (condition.getTimeCondition().isPresent()) {
            TimeCondition timeCondition = condition.getTimeCondition().get();
            jsonGenerator.writeObjectFieldStart("time_condition");
            jsonGenerator.writeNumberField("duration", timeCondition.getDuration());
            jsonGenerator.writeStringField("unit", timeCondition.getUnit().name());
            jsonGenerator.writeStringField("type", timeCondition.getType().name());
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndObject();
    }
}

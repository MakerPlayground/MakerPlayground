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

        jsonGenerator.writeArrayFieldStart("setting");
        for (UserSetting setting : condition.getSetting()) {
            mapper.writeValue(jsonGenerator,setting);
        }
        jsonGenerator.writeEndArray();

        if (condition.getSourceNode() != null) {
            jsonGenerator.writeStringField("sourceNode", condition.getSourceNode().getName());
        } else {
            jsonGenerator.writeStringField("sourceNode", "");
        }

        if (condition.getDestNode() != null) {
            jsonGenerator.writeStringField("destNode", condition.getDestNode().getName());
        } else {
            jsonGenerator.writeStringField("destNode", "");
        }

        jsonGenerator.writeEndObject();
    }
}

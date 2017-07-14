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

        jsonGenerator.writeObjectFieldStart("position");
        jsonGenerator.writeNumberField("top",condition.getTop());
        jsonGenerator.writeNumberField("left",condition.getLeft());
        jsonGenerator.writeNumberField("width",condition.getWidth());
        jsonGenerator.writeNumberField("height",condition.getHeight());
        jsonGenerator.writeNumberField("sourcePortX",condition.getSourcePortX());
        jsonGenerator.writeNumberField("sourcePortY",condition.getSourcePortY());
        jsonGenerator.writeNumberField("destPortX",condition.getDestPortX());
        jsonGenerator.writeNumberField("destPortY",condition.getDestPortY());
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}

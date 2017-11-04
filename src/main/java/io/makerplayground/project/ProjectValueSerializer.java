package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ProjectValueSerializer extends StdSerializer<ProjectValue> {
    public ProjectValueSerializer() {
        this(null);
    }

    public ProjectValueSerializer(Class<ProjectValue> t) {
        super(t);
    }

    @Override
    public void serialize(ProjectValue projectValue, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", projectValue.getDevice().getName());
        jsonGenerator.writeStringField("value", projectValue.getValue().getName());
        jsonGenerator.writeEndObject();
    }
}

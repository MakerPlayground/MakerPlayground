package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class LineSerializer extends JsonSerializer<Line> {
    @Override
    public void serialize(Line line, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("source", line.getSource().getName());
        jsonGenerator.writeStringField("destination", line.getDestination().getName());

        jsonGenerator.writeEndObject();
    }
}


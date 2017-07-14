package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.device.Parameter;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class NodeElementSerializer extends StdSerializer<NodeElement> {
    public NodeElementSerializer() { this(null); }

    public NodeElementSerializer(Class<NodeElement> t) { super(t); }

    @Override
    public void serialize(NodeElement nodeElement, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("top",nodeElement.getTop());
        jsonGenerator.writeObjectField("left",nodeElement.getLeft());
        jsonGenerator.writeObjectField("width",nodeElement.getWidth());
        jsonGenerator.writeObjectField("height",nodeElement.getHeight());
        jsonGenerator.writeObjectField("sourcePortX",nodeElement.getSourcePortX());
        jsonGenerator.writeObjectField("sourcePortY",nodeElement.getSourcePortY());
        jsonGenerator.writeObjectField("destPortX",nodeElement.getDestPortX());
        jsonGenerator.writeObjectField("destPortY",nodeElement.getDestPortY());

        jsonGenerator.writeEndObject();
    }
}


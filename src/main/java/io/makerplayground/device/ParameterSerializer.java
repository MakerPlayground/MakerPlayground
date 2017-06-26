package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by nuntipat on 6/25/2017 AD.
 */
public class ParameterSerializer extends StdSerializer<Parameter> {
    public ParameterSerializer() {
        this(null);
    }

    public ParameterSerializer(Class<Parameter> t) {
        super(t);
    }

    @Override
    public void serialize(Parameter parameter, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", parameter.getName());
        jsonGenerator.writeObjectField("default", parameter.getDefaultValue());
        jsonGenerator.writeObjectField("constraint", parameter.getConstraint());
        jsonGenerator.writeObjectField("type", parameter.getParameterType());
        jsonGenerator.writeObjectField("control", parameter.getControlType());
        jsonGenerator.writeEndObject();
    }
}

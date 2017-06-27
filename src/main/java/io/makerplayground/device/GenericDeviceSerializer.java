package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 *
 * Created by nuntipat on 6/25/2017 AD.
 */
public class GenericDeviceSerializer extends StdSerializer<GenericDevice> {
    public GenericDeviceSerializer() {
        this(null);
    }

    public GenericDeviceSerializer(Class<GenericDevice> t) {
        super(t);
    }

    @Override
    public void serialize(GenericDevice genericDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", genericDevice.getName());

        jsonGenerator.writeArrayFieldStart("action");
        for (Action action : genericDevice.getAction()) {
            jsonGenerator.writeObject(action);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeStringField("defaultAction", genericDevice.getDefaultAction().getName());

        jsonGenerator.writeArrayFieldStart("value");
        for (Value value : genericDevice.getValue()) {
            jsonGenerator.writeObject(value);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}

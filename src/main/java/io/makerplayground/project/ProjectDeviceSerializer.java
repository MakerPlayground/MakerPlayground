package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class ProjectDeviceSerializer extends StdSerializer<ProjectDevice> {
    public ProjectDeviceSerializer() { this(null); }

    public ProjectDeviceSerializer(Class<ProjectDevice> t) { super(t); }

    @Override
    public void serialize(ProjectDevice projectDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name",projectDevice.getName());
        jsonGenerator.writeStringField("genericDevice",projectDevice.getGenericDevice().getName());
        jsonGenerator.writeObjectFieldStart("actualDevice");
        if (projectDevice.getActualDevice() != null) {
            jsonGenerator.writeStringField("brand", projectDevice.getActualDevice().getBrand());
            jsonGenerator.writeStringField("model", projectDevice.getActualDevice().getModel());
        } else {
            jsonGenerator.writeStringField("brand", "");
            jsonGenerator.writeStringField("model", "");
        }
        jsonGenerator.writeEndObject();

        jsonGenerator.writeEndObject();
    }
}

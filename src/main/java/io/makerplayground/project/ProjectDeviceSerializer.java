package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.helper.Peripheral;

import java.io.IOException;
import java.util.Map;

/**
 * Created by USER on 13-Jul-17.
 */
public class ProjectDeviceSerializer extends StdSerializer<ProjectDevice> {
    public ProjectDeviceSerializer() { this(null); }

    public ProjectDeviceSerializer(Class<ProjectDevice> t) { super(t); }

    @Override
    public void serialize(ProjectDevice projectDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("name", projectDevice.getName());
        jsonGenerator.writeStringField("genericDevice", projectDevice.getGenericDevice().getName());
        jsonGenerator.writeBooleanField("autoselect", projectDevice.isAutoSelectDevice());

        if (projectDevice.getActualDevice() != null) {
            jsonGenerator.writeStringField("actualDevice", projectDevice.getActualDevice().getId());
        } else {
            jsonGenerator.writeStringField("actualDevice", "");
        }

        jsonGenerator.writeArrayFieldStart("actualDeviceConnection");
        for (Map.Entry<Peripheral, Peripheral> connection : projectDevice.getDeviceConnection().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("devicePeripheral", connection.getKey().name());
            jsonGenerator.writeStringField("controllerPeripheral", connection.getValue().name());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        if (projectDevice.getDependentDevice() != null) {
            jsonGenerator.writeStringField("dependentDevice", projectDevice.getDependentDevice().getId());
        } else {
            jsonGenerator.writeStringField("dependentDevice", "");
        }

        jsonGenerator.writeArrayFieldStart("dependentDeviceConnection");
        for (Map.Entry<Peripheral, Peripheral> connection : projectDevice.getDeviceConnection().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("devicePeripheral", connection.getKey().name());
            jsonGenerator.writeStringField("controllerPeripheral", connection.getValue().name());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();


        jsonGenerator.writeEndObject();
    }
}

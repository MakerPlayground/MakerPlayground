package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.makerplayground.device.DevicePort;
import io.makerplayground.device.Property;
import io.makerplayground.helper.Peripheral;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by USER on 13-Jul-17.
 */
public class ProjectDeviceSerializer extends StdSerializer<ProjectDevice> {
    public ProjectDeviceSerializer() { this(null); }

    public ProjectDeviceSerializer(Class<ProjectDevice> t) { super(t); }

    @Override
    public void serialize(ProjectDevice projectDevice, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
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
        for (Map.Entry<Peripheral, List<DevicePort>> connection : projectDevice.getDeviceConnection().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("devicePeripheral", connection.getKey().name());
            //jsonGenerator.writeStringField("controllerPeripheral", connection.getValue().getName());
            jsonGenerator.writeArrayFieldStart("controllerPeripheral");
            for (DevicePort devicePort : connection.getValue()) {
                mapper.writeValue(jsonGenerator, devicePort.getName());
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        if (projectDevice.getDependentDevice() != null) {
            jsonGenerator.writeStringField("dependentDevice", projectDevice.getDependentDevice().getId());
        } else {
            jsonGenerator.writeStringField("dependentDevice", "");
        }

        jsonGenerator.writeArrayFieldStart("dependentDeviceConnection");
        for (Map.Entry<Peripheral, List<DevicePort>> connection : projectDevice.getDependentDeviceConnection().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("devicePeripheral", connection.getKey().name());
            //jsonGenerator.writeStringField("controllerPeripheral", connection.getValue().getName());
            jsonGenerator.writeArrayFieldStart("controllerPeripheral");
            for (DevicePort devicePort : connection.getValue()) {
                mapper.writeValue(jsonGenerator, devicePort.getName());
            }
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("property");
        if (projectDevice.getActualDevice() != null) {
            for (Property property : projectDevice.getActualDevice().getProperty()) {
                String value = projectDevice.getPropertyValue(property);
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("name", property.getName());
                jsonGenerator.writeStringField("value", value);
                jsonGenerator.writeEndObject();
            }
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}

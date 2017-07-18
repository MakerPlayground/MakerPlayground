package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.Device;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.helper.Peripheral;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuntipat on 7/18/2017 AD.
 */
public class ProjectDeviceDeserializer extends StdDeserializer<ProjectDevice> {

    public ProjectDeviceDeserializer() {
        this(null);
    }

    public ProjectDeviceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ProjectDevice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(node.get("genericDevice").asText());
        boolean autoSelect = node.get("autoselect").asBoolean();

        String actualDeviceId = node.get("actualDevice").asText();
        Device actualDevice = null;
        if (actualDeviceId.isEmpty()) {
            actualDevice = DeviceLibrary.INSTANCE.getActualDevice(actualDeviceId);
        }

        Map<Peripheral, Peripheral> actualDeviceConnection = new HashMap<>();
        for  (JsonNode connection : node.get("actualDeviceConnection")) {
            Peripheral source = Peripheral.valueOf(connection.get("devicePeripheral").asText());
            Peripheral dest = Peripheral.valueOf(connection.get("controllerPeripheral").asText());
            actualDeviceConnection.put(source, dest);
        }

        String dependentDeviceId = node.get("actualDevice").asText();
        Device dependentDevice = null;
        if (dependentDeviceId.isEmpty()) {
            dependentDevice = DeviceLibrary.INSTANCE.getActualDevice(dependentDeviceId);
        }

        Map<Peripheral, Peripheral> dependentDeviceConnection = new HashMap<>();
        for  (JsonNode connection : node.get("dependentDeviceConnection")) {
            Peripheral source = Peripheral.valueOf(connection.get("devicePeripheral").asText());
            Peripheral dest = Peripheral.valueOf(connection.get("controllerPeripheral").asText());
            dependentDeviceConnection.put(source, dest);
        }

        return new ProjectDevice(name, genericDevice, autoSelect, actualDevice, actualDeviceConnection
                , dependentDevice, dependentDeviceConnection);
    }
}

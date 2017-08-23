package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.helper.DeviceType;
import io.makerplayground.helper.FormFactor;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nititorn on 7/7/2017.
 */
public class DeviceDeserializer extends StdDeserializer<Device> {

    public DeviceDeserializer() {
        this(null);
    }

    public DeviceDeserializer(Class<Device> t) {
        super(t);
    }

    @Override
    public Device deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String id = node.get("id").asText();
        String brand = node.get("brand").asText();
        String model = node.get("model").asText();
        String url = node.get("url").asText();
        double width = node.get("width").asDouble();
        double height = node.get("height").asDouble();
        double v = node.get("v").asDouble();
        double i = node.get("i").asDouble();
        double w = node.get("w").asDouble();
        Device.Dependency dependency;
        Device.Dependency category;

        if (node.get("dependency").asText().isEmpty())
            dependency = null;
        else
            dependency = Device.Dependency.valueOf(node.get("dependency").asText());

        if (node.get("category").asText().isEmpty())
            category = null;
        else
            category = Device.Dependency.valueOf(node.get("category").asText());

        DeviceType type = DeviceType.valueOf(node.get("type").asText());
        FormFactor formFactor = FormFactor.valueOf(node.get("formfactor").asText());
        Set<Platform> platform = EnumSet.copyOf((List<Platform>) mapper.readValue(node.get("platform").traverse()
                , new TypeReference<List<Platform>>() {}));
        List<DevicePort> port = mapper.readValue(node.get("port").traverse()
                , new TypeReference<List<DevicePort>>() {});
        List<Peripheral> connectivity = mapper.readValue(node.get("connectivity").traverse()
                , new TypeReference<List<Peripheral>>() {});

        Map<GenericDevice, Integer> supportedDevice = new HashMap<>();
        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction = new HashMap<>();
        Map<GenericDevice, Map<Value, Constraint>> supportedDeviceValue = new HashMap<>();
        for (JsonNode deviceNode : node.get("compatibility")) {
            String deviceName = deviceNode.get("name").asText();
            System.out.println(deviceName);
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            Map<Action, Map<Parameter, Constraint>> supportedAction = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {
                String actionName = actionNode.get("name").asText();
                System.out.println(actionName);
                Action action = genericDevice.getAction(actionName);
                System.out.println(action);
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    System.out.println(parameterName);
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    System.out.println(constraint);
                    Parameter parameter = action.getParameter(parameterName);
                    supportedParam.put(parameter, constraint);
                }
                supportedAction.put(action, supportedParam);
            }
            supportedDeviceaction.put(genericDevice, supportedAction);

            Map<Value, Constraint> supportedValue = new HashMap<>();
            for (JsonNode valueNode : deviceNode.get("value")) {
                String valueName = valueNode.get("name").asText();
                //System.out.println(actionName);
                Constraint constraint = mapper.treeToValue(valueNode.get("constraint"), Constraint.class);
                //System.out.println(constraint);
                Value value = genericDevice.getValue(valueName);
                supportedValue.put(value, constraint);
            }
            supportedDeviceValue.put(genericDevice, supportedValue);

            supportedDevice.put(genericDevice, deviceNode.get("count").asInt());
        }

//        Map<String, List<String>> dependency = new HashMap<>();
//        for (JsonNode dependencyNode : node.get("dependency")) {
//            String name = dependencyNode.get("name").asText();
//            List<String> device = new ArrayList<>();
//            for (JsonNode deviceNode : dependencyNode.get("device")) {
//                device.add(deviceNode.asText());
//            }
//            dependency.put(name, device);
//        }

        return new Device(id, brand, model, url, width, height, type, formFactor, platform, port, connectivity
                , supportedDevice, supportedDeviceaction, supportedDeviceValue, dependency, category, v, i ,w);
    }
}
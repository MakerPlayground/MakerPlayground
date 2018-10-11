package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.generator.diagram.WiringMethod;
import io.makerplayground.helper.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nititorn on 7/7/2017.
 */
public class ActualDeviceDeserializer extends StdDeserializer<ActualDevice> {

    public ActualDeviceDeserializer() {
        this(null);
    }

    public ActualDeviceDeserializer(Class<ActualDevice> t) {
        super(t);
    }

    @Override
    public ActualDevice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String id = node.get("id").asText();
        String brand = node.get("brand").asText();
        String model = node.get("model").asText();
        String url = node.get("url").asText();
        double width = node.get("width").asDouble();
        double height = node.get("height").asDouble();
        ActualDevice.Dependency dependency;
        ActualDevice.Dependency category;

        if (node.get("dependency").asText().isEmpty())
            dependency = null;
        else
            dependency = ActualDevice.Dependency.valueOf(node.get("dependency").asText());

        if (node.get("category").asText().isEmpty())
            category = null;
        else
            category = ActualDevice.Dependency.valueOf(node.get("category").asText());

        String mpLibrary = node.get("classname").asText();
        List<String> externalLibrary = mapper.readValue(node.get("library_dependency").traverse(),
                new TypeReference<List<String>>() {});

        DeviceType type = DeviceType.valueOf(node.get("type").asText());
        WiringMethod wiringMethod = null;
        String pioBoardId = "";
        if (type == DeviceType.CONTROLLER) {
            if (node.has("pio_boardid")) {
                pioBoardId = node.get("pio_boardid").asText();
            } else {
                throw new IllegalStateException("Missing field 'pio_boardid' for device id = " + id);
            }
            if (node.has("wiring_method")) {
                wiringMethod = WiringMethod.valueOf(node.get("wiring_method").asText());
            } else {
                throw new IllegalStateException("Missing field 'wiring_method' for device id = " + id);
            }
        }
        FormFactor formFactor = FormFactor.valueOf(node.get("formfactor").asText());
        Set<Platform> platform = EnumSet.copyOf((List<Platform>) mapper.readValue(node.get("platform").traverse()
                , new TypeReference<List<Platform>>() {}));
        CloudPlatform cloudPlatform = null;
        if (node.has("cloud_platform")) {
            cloudPlatform = CloudPlatform.valueOf(node.get("cloud_platform").asText());
        }
        List<DevicePort> port = mapper.readValue(node.get("port").traverse()
                , new TypeReference<List<DevicePort>>() {});
        List<Peripheral> connectivity = mapper.readValue(node.get("connectivity").traverse()
                , new TypeReference<List<Peripheral>>() {});
        List<Property> property = mapper.readValue(node.get("property").traverse()
                , new TypeReference<List<Property>>() {});


        Map<GenericDevice, Integer> supportedDevice = new HashMap<>();
        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction = new HashMap<>();
        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceCondition = new HashMap<>();
        Map<GenericDevice, Map<Value, Constraint>> supportedDeviceValue = new HashMap<>();
        readCompatibilityField(mapper, node, supportedDevice, supportedDeviceaction, supportedDeviceCondition, supportedDeviceValue);

        List<IntegratedActualDevice> integratedDevices = new ArrayList<>();
        if (node.has("integrated_device")) {
            for (JsonNode deviceNode : node.get("integrated_device")) {
                String integratedDeviceName = deviceNode.get("name").asText();
                String integratedLibrary = deviceNode.get("classname").asText();
                List<String> integratedExternalLibrary = mapper.readValue(deviceNode.get("library_dependency").traverse(),
                        new TypeReference<List<String>>() {});

                List<DevicePort> integratedPort = mapper.readValue(deviceNode.get("port").traverse()
                        , new TypeReference<List<DevicePort>>() {});
                List<Peripheral> integratedConnectivity = mapper.readValue(deviceNode.get("connectivity").traverse()
                        , new TypeReference<List<Peripheral>>() {});

                Map<GenericDevice, Integer> integratedSupportedDevice = new HashMap<>();
                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceaction = new HashMap<>();
                Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> integratedSupportedDeviceCondition = new HashMap<>();
                Map<GenericDevice, Map<Value, Constraint>> integratedSupportedDeviceValue = new HashMap<>();
                readCompatibilityField(mapper, deviceNode, integratedSupportedDevice, integratedSupportedDeviceaction
                        , integratedSupportedDeviceCondition, integratedSupportedDeviceValue);

                integratedDevices.add(new IntegratedActualDevice(integratedDeviceName, integratedLibrary, integratedExternalLibrary, integratedPort, integratedConnectivity
                        , integratedSupportedDevice, integratedSupportedDeviceaction, integratedSupportedDeviceCondition, integratedSupportedDeviceValue));
            }
        }

        Map<CloudPlatform, CloudPlatformLibrary> supportedCloudPlatform = new HashMap<>();
        if (type == DeviceType.CONTROLLER) {
            if (node.has("support_cloudplatform") && node.get("support_cloudplatform").isArray()) {
                for (JsonNode cloudPlatformNode : node.get("support_cloudplatform")) {
                    CloudPlatform cloudPlatformKey = mapper.treeToValue(cloudPlatformNode.get("cloud_platform"), CloudPlatform.class);
                    String className = mapper.treeToValue(cloudPlatformNode.get("classname"), String.class);
                    List<String> cloudPlatformDependency = mapper.readValue(cloudPlatformNode.get("library_dependency").traverse(), new TypeReference<List<String>>() {});
                    CloudPlatformLibrary cloudPlatformLibrary = new CloudPlatformLibrary(className, cloudPlatformDependency);
                    supportedCloudPlatform.put(cloudPlatformKey, cloudPlatformLibrary);
                }
            }
        }

        return new ActualDevice(id, brand, model, url, width, height, type, pioBoardId, wiringMethod, formFactor, mpLibrary, externalLibrary,
                platform, cloudPlatform, port, connectivity, supportedDevice, supportedDeviceaction,
                supportedDeviceCondition, supportedDeviceValue, dependency, category, property, supportedCloudPlatform, integratedDevices);
    }

    private void readCompatibilityField(ObjectMapper mapper, JsonNode node, Map<GenericDevice, Integer> supportedDevice, Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction, Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceCondition, Map<GenericDevice, Map<Value, Constraint>> supportedDeviceValue) throws JsonProcessingException {
        for (JsonNode deviceNode : node.get("compatibility")) {
            String deviceName = deviceNode.get("name").asText();
            GenericDevice genericDevice = DeviceLibrary.INSTANCE.getGenericDevice(deviceName);

            Map<Action, Map<Parameter, Constraint>> supportedAction = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {
                String actionName = actionNode.get("name").asText();
                Action action = genericDevice.getAction(actionName);
                if (action == null) {
                    continue;
                }
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    Parameter parameter = action.getParameter(parameterName);
                    supportedParam.put(parameter, constraint);
                }
                supportedAction.put(action, supportedParam);
            }
            supportedDeviceaction.put(genericDevice, supportedAction);

            Map<Action, Map<Parameter, Constraint>> supportedCondition = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {  // the node name hasn't been changed
                String actionName = actionNode.get("name").asText();
                Action condition = genericDevice.getCondition(actionName);
                if (condition == null) {
                    continue;
                }
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    Parameter parameter = condition.getParameter(parameterName);
                    supportedParam.put(parameter, constraint);
                }
                supportedCondition.put(condition, supportedParam);
            }
            supportedDeviceCondition.put(genericDevice, supportedCondition);

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
    }
}
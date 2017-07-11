package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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

        String brand = node.get("brand").asText();
        String model = node.get("model").asText();
        String url = node.get("url").asText();

        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportedDeviceaction = new HashMap<>();
        for (JsonNode deviceNode : node.get("supportdevice")) {
            String deviceName = deviceNode.get("name").asText();
            System.out.println(deviceName);
            // find device
            GenericDevice genericDevice = null;
            System.out.println(DeviceLibrary.INSTANCE);
            for (GenericDevice tempdevice : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
                if (tempdevice.getName().equals(deviceName)) {
                    genericDevice = tempdevice;
                    break;
                }
            }
            for (GenericDevice tempdevice : DeviceLibrary.INSTANCE.getGenericOutputDevice()) {
                if (tempdevice.getName().equals(deviceName)) {
                    genericDevice = tempdevice;
                    break;
                }
            }

            Map<Action, Map<Parameter, Constraint>> supportedAction = new HashMap<>();
            for (JsonNode actionNode : deviceNode.get("action")) {
                String actionName = actionNode.get("name").asText();
                System.out.println(actionName);
                // find action with name=actionName
                Action action = null;
                for (Action tempaction : genericDevice.getAction()) {
                    if (tempaction.getName().equals(actionName)) {
                        action = tempaction;
                        break;
                    }
                }
                Map<Parameter, Constraint> supportedParam = new HashMap<>();
                for (JsonNode parameterNode : actionNode.get("parameter")) {
                    String parameterName = parameterNode.get("name").asText();
                    System.out.println(parameterName);
                    Constraint constraint = mapper.treeToValue(parameterNode.get("constraint"), Constraint.class);
                    System.out.println(constraint);
                    // find parameter with name=parameterName
                    Parameter parameter = null;
                    for (Parameter tempparam : action.getParameter()) {
                        if (tempparam.getName().equals(parameterName)) {
                            parameter = tempparam;
                            break;
                        }
                    }
                    supportedParam.put(parameter, constraint);
                }
                supportedAction.put(action, supportedParam);
            }
            supportedDeviceaction.put(genericDevice, supportedAction);
        }

        Map<GenericDevice, Map<Value, Constraint>>  supportedDevicevalue = new HashMap<>();
        for (JsonNode deviceNode : node.get("supportvalue")) {
            String deviceName = deviceNode.get("name").asText();
            System.out.println(deviceName);
            // find device
            GenericDevice genericDevice = null;
            for (GenericDevice tempdevice : DeviceLibrary.INSTANCE.getGenericInputDevice()) {
                if (tempdevice.getName().equals(deviceName)) {
                    genericDevice = tempdevice;
                    break;
                }
            }
            Map<Value, Constraint> supportedValue = new HashMap<>();
            for (JsonNode valueNode : deviceNode.get("value")) {
                String actionName = valueNode.get("name").asText();
                System.out.println(actionName);
                Constraint constraint = mapper.treeToValue(valueNode.get("constraint"), Constraint.class);
                System.out.println(constraint);
                // find action with name=actionName
                Value value = null;
                for (Value tempaction : genericDevice.getValue()) {
                    if (tempaction.getName().equals(actionName)) {
                        value = tempaction;
                        break;
                    }
                }
                supportedValue.put(value, constraint);
            }
            supportedDevicevalue.put(genericDevice, supportedValue);
        }

        return new Device(brand, model, url, supportedDeviceaction, supportedDevicevalue);
//        Map<Parameter, Constraint> supportparam = new HashMap<>();
//        Constraint constraint = mapper.treeToValue(node.get("constraint"), Constraint.class);
//        supportparam.put(param, constraint);
//
//        List<Parameter> paramList = new ArrayList<>();
//        JsonNode paramListNode = node.get("parameter");
//        JsonNode actionListNode = node.get("action");
//        if (!actionListNode.isArray()) {
//            System.out.println("Format error!!!");
//        }
//        for (JsonNode paramNode : paramListNode) {
//            Parameter param = mapper.treeToValue(paramNode, Parameter.class);
//            paramList.add(param);
//        }
//
//        if (supportparamNode.)
//        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> supportmap = new HashMap<>();
//
//        return Device(brand, model, url,);
    }
}
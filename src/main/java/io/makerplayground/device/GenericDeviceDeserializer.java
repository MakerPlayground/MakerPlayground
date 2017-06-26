package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuntipat on 6/25/2017 AD.
 */
public class GenericDeviceDeserializer extends StdDeserializer<GenericDevice> {
    public GenericDeviceDeserializer() {
        this(null);
    }

    public GenericDeviceDeserializer(Class<GenericDevice> t) {
        super(t);
    }

    @Override
    public GenericDevice deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();

        List<Action> actionList = new ArrayList<>();
        JsonNode actionListNode = node.get("action");
        if (!actionListNode.isArray()) {
            System.out.println("Format error!!!");
        }
        for (JsonNode actionNode : actionListNode) {
            Action action = mapper.treeToValue(actionNode, Action.class);
            actionList.add(action);
        }

        String defaultActionName = node.get("defaultAction").asText();
        Action defaultAction = null;
        for (Action action : actionList) {
            if (action.getName().equals(defaultActionName)) {
                defaultAction = action;
                break;
            }
        }
        if (defaultAction == null) {
            System.out.println("Format error!!!");
        }

        List<Value> valueList = new ArrayList<>();
        JsonNode valueListNode = node.get("value");
        if (!valueListNode.isArray()) {
            System.out.println("Format error!!!");
        }
        for (JsonNode valueNode : valueListNode) {
            Value value = mapper.treeToValue(valueNode, Value.class);
            valueList.add(value);
        }

        return new GenericDevice(name, actionList, defaultAction, valueList);
    }
}

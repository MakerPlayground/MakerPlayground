package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.Action;
import io.makerplayground.device.Parameter;
import io.makerplayground.device.Value;
import io.makerplayground.helper.NumberWithUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UserSettingDeserializer extends StdDeserializer<UserSetting> {

    private ObservableList<ProjectDevice> inputDevice;
    private ObservableList<ProjectDevice> outputDevice;

    public UserSettingDeserializer(ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) {
        this(null);
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
    }

    public UserSettingDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UserSetting deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        ProjectDevice projectDevice = null;
        Action action = projectDevice.getGenericDevice().getAction(node.get("action").asText());

        Map<Parameter, Object> valueMap = new HashMap<>();
        for (JsonNode parameterNode : node.get("valueMap")) {
            Parameter parameter = action.getParameter(parameterNode.get("name").asText());
            Object object = null;
            if (parameterNode.get("value").isObject()) {
                object = mapper.treeToValue(parameterNode.get("value"), NumberWithUnit.class);
            } else {
                object = parameterNode.get("value").asText();
            }
            valueMap.put(parameter, object);
        }

        Map<Value, ObservableList<Expression>> expressionMap = new HashMap<>();
        for (JsonNode valueNode : node.get("expression")) {
            Value value = projectDevice.getGenericDevice().getValue(valueNode.get("name").asText());
            ObservableList<Expression> expressionList = FXCollections.observableArrayList();
            for (JsonNode expressionNode : valueNode.get("expression")) {
                expressionList.add(mapper.treeToValue(expressionNode, Expression.class));
            }
        }

        return new UserSetting(projectDevice, action, valueMap, expressionMap);
    }
}

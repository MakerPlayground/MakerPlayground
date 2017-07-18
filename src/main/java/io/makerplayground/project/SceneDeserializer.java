package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SceneDeserializer extends StdDeserializer<Scene> {

    private ObservableList<ProjectDevice> inputDevice;
    private ObservableList<ProjectDevice> outputDevice;

    public SceneDeserializer(ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) {
        this(null);
    }

    public SceneDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Scene deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        UserSettingDeserializer deserializer = new UserSettingDeserializer(inputDevice, outputDevice);
        for (JsonNode sceneSettingNode : node.get("userSetting")) {
            //setting.add(mapper.treeToValue(sceneSettingNode, UserSetting.class));
            setting.add(deserializer.deserialize(sceneSettingNode.traverse(), deserializationContext));
        }

        double delay = node.get("delay").asDouble();
        Scene.DelayUnit delayUnit = Scene.DelayUnit.valueOf(node.get("delayUnit").asText());

        double top = node.get("top").asDouble();
        double left = node.get("top").asDouble();
        double width = node.get("top").asDouble();
        double height = node.get("top").asDouble();

        return new Scene(top, left, width, height, name, setting, delay, delayUnit);
    }
}


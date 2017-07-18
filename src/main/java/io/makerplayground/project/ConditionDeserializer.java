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
public class ConditionDeserializer extends StdDeserializer<Condition> {

    private ObservableList<ProjectDevice> inputDevice;
    private ObservableList<ProjectDevice> outputDevice;
    private ObservableList<Scene> scene;

    public ConditionDeserializer(ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice, ObservableList<Scene> scene) {
        this(null);
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
        this.scene = scene;
    }

    public ConditionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Condition deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();

        List<UserSetting> setting = new ArrayList<>();
        UserSettingDeserializer deserializer = new UserSettingDeserializer(inputDevice, outputDevice);
        for (JsonNode sceneSettingNode : node.get("userSetting")) {
            setting.add(deserializer.deserialize(sceneSettingNode.traverse(), deserializationContext));
        }

        Scene source = null;
        if (!node.get("sourceNode").asText().isEmpty()) {
            source = scene.stream().filter(scene1 -> scene1.getName().equals(node.get("sourceNode").asText())).findFirst().get();
        }
        Scene dest = null;
        if (!node.get("destNode").asText().isEmpty()) {
            dest = scene.stream().filter(scene1 -> scene1.getName().equals(node.get("destNode").asText())).findFirst().get();
        }

        double top = node.get("top").asDouble();
        double left = node.get("left").asDouble();
        double width = node.get("width").asDouble();
        double height = node.get("height").asDouble();

        return new Condition(top, left, width, height, name, setting, source, dest);
    }
}

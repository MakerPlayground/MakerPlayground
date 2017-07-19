package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.device.Value;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by nuntipat on 7/18/2017 AD.
 */
public class ProjectValueDeserializer extends StdDeserializer<ProjectValue> {

    private ObservableList<ProjectDevice> inputDevice;
    private ObservableList<ProjectDevice> outputDevice;

    public ProjectValueDeserializer(ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) {
        this(null);
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
    }

    public ProjectValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ProjectValue deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String deviceName = node.get("device").asText();
        ProjectDevice device = null;
        for (ProjectDevice projectDevice : inputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }
        for (ProjectDevice projectDevice : outputDevice) {
            if (deviceName.equals(projectDevice.getName()))
                device = projectDevice;
        }

        Value value = device.getGenericDevice().getValue(node.get("value").asText());

        return new ProjectValue(device, value);
    }
}
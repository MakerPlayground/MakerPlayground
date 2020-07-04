package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineDeserializer extends JsonDeserializer<Line> {
    private ObjectMapper mapper = new ObjectMapper();
    private Project project;

    public LineDeserializer(Project project)
    {
        this.project = project;
        SimpleModule module = new SimpleModule();
        mapper.registerModule(module);
    }
    @Override
    public Line deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = mapper.readTree(jsonParser);

        String sourceName = node.get("source").asText();
        String destinationName = node.get("destination").asText();
        List<UserSetting> allSettings = new ArrayList<UserSetting>();

        NodeElement source = new Scene(0,0,0,0,sourceName,allSettings,project);
        NodeElement destination = new Scene(0,0,0,0,destinationName,allSettings,project);

        return new Line(source,destination,project);
    }
}

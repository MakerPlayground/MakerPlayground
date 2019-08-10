package io.makerplayground.device.actual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.util.List;

import static io.makerplayground.util.DeserializerHelper.createArrayNodeIfMissing;
import static io.makerplayground.util.DeserializerHelper.throwIfMissingField;

public class PinTemplateDeserializer extends JsonDeserializer<PinTemplate> {
    @Override
    public PinTemplate deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        YAMLMapper mapper = new YAMLMapper();
        JsonNode node = mapper.readTree(jsonParser);

        throwIfMissingField(node, "pin_name", "pin");

        String displayName = node.get("pin_name").asText();
        String codingName = node.has("coding_name") ? node.get("coding_name").asText() : "";

        VoltageLevel voltageLevel = node.has("voltage_level") ? VoltageLevel.valueOf(node.get("voltage_level").asText()) : null;

        createArrayNodeIfMissing(node, "pin_function");
        List<PinFunction> functions;
        if (node.get("pin_function").isArray()) {
            functions = mapper.readValue(node.get("pin_function").traverse(), new TypeReference<List<PinFunction>>() {});
        } else {
            functions = List.of(PinFunction.valueOf(node.get("pin_function").asText()));
        }

        return new PinTemplate(displayName, codingName, voltageLevel, functions);
    }
}

package io.makerplayground.device.shared;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.constraint.ConstraintDeserializer;

import java.io.IOException;

public class ValueDeserializer extends JsonDeserializer<Value> {
    ObjectMapper mapper = new ObjectMapper();

    @Override
    public Value deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        DataType dataType = mapper.treeToValue(node.get("dataType"), DataType.class);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Constraint.class, new ConstraintDeserializer(dataType));
        mapper.registerModule(module);
        Constraint constraint = mapper.readValue(node.get("constraint").traverse(), Constraint.class);

        return new Value(name, dataType, constraint);
    }
}

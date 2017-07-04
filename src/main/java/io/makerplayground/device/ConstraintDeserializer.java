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
 *
 * Created by nuntipat on 7/2/2017 AD.
 */
public class ConstraintDeserializer extends StdDeserializer<Constraint> {
    public ConstraintDeserializer() {
        this(null);
    }

    public ConstraintDeserializer(Class<Constraint> t) {
        super(t);
    }

    @Override
    public Constraint deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (!node.isArray()) {
            throw new IllegalStateException("JSON format error!!!");
        }
        if (node.size() == 0) {
            return Constraint.NONE;
        } else if (node.get(0).isObject()) {
            List<Constraint.Value> valueList = new ArrayList<>();
            for (JsonNode jn : node) {
                valueList.add(mapper.treeToValue(jn, Constraint.Value.class));
            }
            return Constraint.createNumericConstraint(valueList);
        } else {
            List<String> valueList = new ArrayList<>();
            for (JsonNode jn : node) {
                valueList.add(jn.asText());
            }
            return Constraint.createCategoricalConstraint(valueList);
        }
    }
}

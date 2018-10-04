package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;

import java.io.IOException;

public class PropertyDeserializer extends StdDeserializer<Property> {

    public PropertyDeserializer() {
        this(null);
    }

    public PropertyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Property deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = deserializationContext.readValue(jsonParser, JsonNode.class);

        String name = node.get("name").asText();
        Constraint constraint = mapper.treeToValue(node.get("constraint"), Constraint.class);
        DataType dataType = mapper.treeToValue(node.get("datatype"), DataType.class);
        ControlType controlType = mapper.treeToValue(node.get("controltype"), ControlType.class);

        Object defaultValue = null;
        switch (dataType) {
            case STRING:
                defaultValue = node.get("value").asText();
                break;
            case DOUBLE:
                defaultValue = new NumberWithUnit(node.get("value").asDouble()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
                break;
            case ENUM:
                defaultValue = node.get("value").asText();
                break;
            case INTEGER_ENUM:
                defaultValue = node.get("value").asInt();
                break;
            case INTEGER:
                defaultValue = new NumberWithUnit(node.get("value").asInt()
                        , Unit.valueOf(node.get("constraint").get("unit").asText()));
                break;
            case VALUE: // TODO: prove we don't have to do anything
                break;
            default:
                throw(new IllegalStateException("Format error!!!"));
        }

        return new Property(name, defaultValue, constraint, dataType, controlType);
    }
}

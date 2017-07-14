package io.makerplayground.device;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.PinType;
import io.makerplayground.helper.Platform;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class ProcessorDeserializer extends StdDeserializer<Processor> {
    public ProcessorDeserializer() {
        this(null);
    }

    public ProcessorDeserializer(Class<Processor> t) {
        super(t);
    }

    @Override
    public Processor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        String name = node.get("name").asText();
        Platform platform = mapper.treeToValue(node.get("platform"), Platform.class);
        Map<String, Map<Peripheral, PinType>> port = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> portIterator = node.get("port").fields();
        while (portIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = portIterator.next();
            String portName = entry.getKey();
            port.put(portName, new HashMap<>());
            for (JsonNode peripheralNode : entry.getValue()) {
                Peripheral peripheral = Peripheral.valueOf(peripheralNode.get("type").asText());
                PinType pinType = PinType.valueOf(peripheralNode.get("subtype").asText());
                //Peripheral peripheral = mapper.treeToValue(entry.getValue().get("type"), Peripheral.class);
                //PinType pinType = mapper.treeToValue(entry.getValue().get("subtype"), PinType.class);
                port.get(portName).put(peripheral, pinType);
            }
        }

        return new Processor(name, platform, port);
    }

}

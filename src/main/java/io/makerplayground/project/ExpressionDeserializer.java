package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Created by nuntipat on 7/18/2017 AD.
 */
public class ExpressionDeserializer extends StdDeserializer<Expression> {
    private ObservableList<ProjectDevice> inputDevice;
    private ObservableList<ProjectDevice> outputDevice;

    public ExpressionDeserializer(ObservableList<ProjectDevice> inputDevice, ObservableList<ProjectDevice> outputDevice) {
        this(null);
        this.inputDevice = inputDevice;
        this.outputDevice = outputDevice;
    }

    public ExpressionDeserializer(Class<Project> t) {
        super(t);
    }

    @Override
    public Expression deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ProjectValueDeserializer projectValueDeserializer = new ProjectValueDeserializer(inputDevice, outputDevice);
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        Unit unit = Unit.valueOf(node.get("unit").asText());
        Operator operator = Operator.valueOf(node.get("operator").asText());

        Object firstOperand;
        if (node.get("firstOperand").isObject()) {
            firstOperand = projectValueDeserializer.deserialize(node.get("firstOperand").traverse(), deserializationContext);
        } else {
            firstOperand = node.get("firstOperand").asDouble();
        }

        Object secondOperand;
        if (node.get("secondOperand").isObject()) {
            secondOperand = projectValueDeserializer.deserialize(node.get("secondOperand").traverse(), deserializationContext);
        } else {
            secondOperand = node.get("secondOperand").asDouble();
        }

        OperandType operandType = OperandType.valueOf(node.get("secondOperand").asText());

        return new Expression(unit, operator, firstOperand, secondOperand, operandType);
    }
}

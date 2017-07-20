package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class ExpressionSerializer extends StdSerializer<Expression> {
    public ExpressionSerializer() {
        this(null);
    }

    public ExpressionSerializer(Class<Expression> t) {
        super(t);
    }

    @Override
    public void serialize(Expression expression, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeObjectField("unit", expression.getUnit());
        jsonGenerator.writeObjectField("operator", expression.getOperator());
        if (expression.getFirstOperand() instanceof  ProjectValue) {
            jsonGenerator.writeObjectFieldStart("firstOperand");
            jsonGenerator.writeObjectField("device", ((ProjectValue) expression.getFirstOperand()).getDevice().getName());
            jsonGenerator.writeObjectField("value", ((ProjectValue) expression.getFirstOperand()).getValue().getName());
            jsonGenerator.writeEndObject();
        } else {
            jsonGenerator.writeNumberField("firstOperand", (double) expression.getFirstOperand());
        }
        if (expression.getSecondOperand() instanceof  ProjectValue) {
            jsonGenerator.writeObjectFieldStart("secondOperand");
            jsonGenerator.writeObjectField("device", ((ProjectValue) expression.getSecondOperand()).getDevice().getName());
            jsonGenerator.writeObjectField("value", ((ProjectValue) expression.getSecondOperand()).getValue().getName());
            jsonGenerator.writeEndObject();
        } else {
            jsonGenerator.writeNumberField("secondOperand", (double) expression.getSecondOperand());
        }

        jsonGenerator.writeEndObject();
    }
}

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

        jsonGenerator.writeObjectField("unit",expression.getUnit());
        jsonGenerator.writeObjectField("operator",expression.getOperator());
        jsonGenerator.writeObjectField("firstOperand",expression.getFirstOperand());
        jsonGenerator.writeObjectField("secondOperand",expression.getSecondOperand());
        jsonGenerator.writeObjectField("operandType",expression.getOperandType());

        jsonGenerator.writeEndObject();
    }
}

package io.makerplayground.device.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.makerplayground.project.expression.*;

public class RecordEntry {
    private final String field;
    private final String type; // Remain this field for json serializer
    private final Expression value;

    public RecordEntry() {
        this("", new NumberWithUnitExpression(NumberWithUnit.ZERO));
    }

    public RecordEntry(String field, Expression value) {
        this.field = field;
        this.type = value.getClass().getSimpleName();
        this.value = value;
        if (!(value instanceof NumberWithUnitExpression)
                && !(value instanceof ProjectValueExpression)
                && !(value instanceof ValueLinkingExpression)
                && !(value instanceof CustomNumberExpression)) {
            throw new IllegalArgumentException("Expression must be one of the supported expression type in NumberWithUnitExpressionControl");
        }
    }

    @JsonCreator
    public RecordEntry(String field, String type, Expression value) {
        this(field, value);
    }

    public String getField() {
        return field;
    }

    public Expression getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}

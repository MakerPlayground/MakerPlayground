package io.makerplayground.project.expression;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.makerplayground.device.shared.Record;
import io.makerplayground.project.term.RecordTerm;

public class RecordExpression extends Expression {

    public RecordExpression(Record record) {
        super(Type.RECORD);
        terms.add(new RecordTerm(record));
    }

    public RecordExpression(RecordExpression expression) {
        super(expression);
    }

    @JsonIgnore
    public Record getRecord() {
        return ((RecordTerm) terms.get(0)).getValue();
    }

    @Override
    public Expression deepCopy() {
        return new RecordExpression(this);
    }
}

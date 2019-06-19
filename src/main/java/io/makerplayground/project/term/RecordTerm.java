package io.makerplayground.project.term;


import io.makerplayground.device.shared.Record;

public class RecordTerm extends Term {
    public RecordTerm(Record record) {
        super(Type.RECORD, record);
    }

    @Override
    public Record getValue() {
        return (Record) this.value;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }
}

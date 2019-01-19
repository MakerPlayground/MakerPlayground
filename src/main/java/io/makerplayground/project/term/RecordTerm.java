package io.makerplayground.project.term;

import io.makerplayground.device.shared.Record;

import java.util.stream.Collectors;

public class RecordTerm extends Term {
    public RecordTerm(Record record) {
        super(Type.RECORD, record);
    }

    @Override
    public String toCCode() {
        Record record = (Record) value;
        return "Record(" + record.getEntryList().stream()
                    .map(entry -> "Entry(\"" + entry.getField() + "\", " + entry.getValue().translateToCCode() + ")")
                    .collect(Collectors.joining(",")) + ")";
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

package io.makerplayground.device.shared;

import java.util.Collections;
import java.util.List;

public class Record {
    private final List<RecordEntry> entryList;

    public Record() {
        this.entryList = Collections.emptyList();
    }

    public Record(List<RecordEntry> entryList) {
        this.entryList = Collections.unmodifiableList(entryList);
    }

    public List<RecordEntry> getEntryList() {
        return entryList;
    }
}

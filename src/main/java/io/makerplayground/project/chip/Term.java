package io.makerplayground.project.chip;

import io.makerplayground.project.chip.ChipType;

public abstract class Term {
    private final ChipType type;
    protected final Object value;

    public Term(ChipType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ChipType getType() {
        return type;
    }

    public abstract Object getValue();
}

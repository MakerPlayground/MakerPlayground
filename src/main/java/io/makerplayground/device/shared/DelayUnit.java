package io.makerplayground.device.shared;

public enum DelayUnit {
    SECOND, MILLISECOND;

    @Override
    public String toString() {
        switch (this) {
            case SECOND:
                return "s";
            case MILLISECOND:
                return "ms";
            default:
                throw new IllegalStateException();
        }
    }
}

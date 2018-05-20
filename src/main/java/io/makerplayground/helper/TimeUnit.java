package io.makerplayground.helper;

public enum TimeUnit {
    MilliSecond, Second;

    @Override
    public String toString() {
        switch(this) {
            case MilliSecond: return "ms";
            case Second: return "s";
            default: throw new IllegalArgumentException();
        }
    }
}

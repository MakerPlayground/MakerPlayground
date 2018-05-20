package io.makerplayground.project;

public enum TimeConditionType {
    BEFORE("Less than"), AFTER("Wait for");

    private final String conditionName;

    TimeConditionType(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getConditionName() {
        return conditionName;
    }
}

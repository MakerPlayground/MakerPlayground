package io.makerplayground.project;

public enum DiagramError {
    NONE(""),
    DIAGRAM_MULTIPLE_SCENE("there shouldn't be connection from the same node to multiple scene"),
    DIAGRAM_CHAIN_CONDITION("condition can't be connected together"),
    DIAGRAM_CONDITION_IGNORE("this condition will always be ignored"),
    SCENE_INVALID_NAME("name shouldn't be empty"),
    SCENE_INVALID_PARAM("some parameters are invalid"),
    CONDITION_EMPTY("there isn't any condition"),
    CONDITION_INVALID_PARAM("some parameters are invalid"),
    CONDITION_INVALID_EXPRESSION("some expression are invalid"),
    CONDITION_NO_ENABLE_EXPRESSION("at least one expression should be enabled for each devices");

    private String tooltip;

    DiagramError(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String toString() {
        return tooltip;
    }
}

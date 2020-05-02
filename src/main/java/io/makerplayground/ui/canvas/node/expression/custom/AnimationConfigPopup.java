package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.device.shared.AnimatedValue;
import io.makerplayground.project.ProjectValue;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import org.controlsfx.control.PopOver;

public class AnimationConfigPopup extends PopOver {
    private final AnimationConfigPane configPane;

    public AnimationConfigPopup(AnimatedValue initialValue, boolean allowString, ObservableList<ProjectValue> projectValues) {
        configPane = new AnimationConfigPane(initialValue, allowString, projectValues);
        configPane.setPadding(new Insets(20, 20, 20, 20));
        setDetachable(false);
        setContentNode(configPane);
    }

    public ReadOnlyObjectProperty<AnimatedValue> animatedValueProperty() {
        return configPane.animatedValueProperty();
    }
}

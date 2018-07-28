package io.makerplayground.ui.canvas.node.expressioncontrol;

import io.makerplayground.helper.NumberWithUnit;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.HBox;

public abstract class NumberWithUnitControl extends HBox {
    public abstract NumberWithUnit getValue();
    public abstract void setValue(NumberWithUnit numberWithUnit);
    public abstract ObjectProperty<NumberWithUnit> valueProperty();
}

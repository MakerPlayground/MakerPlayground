package io.makerplayground.ui.canvas;

import javafx.beans.property.BooleanProperty;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public interface Selectable {
    BooleanProperty selectedProperty();
    boolean isSelected();
    void setSelected(boolean b);
}

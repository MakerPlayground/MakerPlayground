package io.makerplayground.viewmodel;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.makerplayground.scope.AppScope;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DiagramTabViewModel implements ViewModel {

    private StringProperty canvasText = new SimpleStringProperty("test");

    @InjectScope
    private AppScope appScope;

    public StringProperty textProperty() {
        return canvasText;
    }
}

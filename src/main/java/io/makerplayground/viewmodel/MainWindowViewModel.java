package io.makerplayground.viewmodel;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.makerplayground.scope.AppScope;
import javafx.beans.property.BooleanProperty;

public class MainWindowViewModel implements ViewModel {

    @InjectScope
    private AppScope appScope;

    public BooleanProperty diagramEditorShowingProperty() {
        return appScope.diagramTabSelectedProperty();
    }

    public BooleanProperty deviceConfigShowingProperty() {
        return appScope.deviceConfigTabSelectedProperty();
    }

    public BooleanProperty deviceMonitorShowingProperty() {
        return appScope.deviceMonitorTabSelectedProperty();
    }

}

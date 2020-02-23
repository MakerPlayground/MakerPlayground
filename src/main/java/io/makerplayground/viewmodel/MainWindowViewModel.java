package io.makerplayground.viewmodel;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.makerplayground.scope.AppScope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MainWindowViewModel implements ViewModel {

    @InjectScope
    private AppScope appScope;

    private final BooleanProperty diagramEditorShowing = new SimpleBooleanProperty(false);
    private final BooleanProperty deviceConfigShowing = new SimpleBooleanProperty(true);
    private final BooleanProperty deviceMonitorShowing = new SimpleBooleanProperty(false);

    public boolean isDiagramEditorShowing() {
        return diagramEditorShowing.get();
    }

    public BooleanProperty diagramEditorShowingProperty() {
        return diagramEditorShowing;
    }

    public void setDiagramEditorShowing(boolean diagramEditorShowing) {
        this.diagramEditorShowing.set(diagramEditorShowing);
    }

    public boolean isDeviceConfigShowing() {
        return deviceConfigShowing.get();
    }

    public BooleanProperty deviceConfigShowingProperty() {
        return deviceConfigShowing;
    }

    public void setDeviceConfigShowing(boolean deviceConfigShowing) {
        this.deviceConfigShowing.set(deviceConfigShowing);
    }

    public boolean isDeviceMonitorShowing() {
        return deviceMonitorShowing.get();
    }

    public BooleanProperty deviceMonitorShowingProperty() {
        return deviceMonitorShowing;
    }

    public void setDeviceMonitorShowing(boolean deviceMonitorShowing) {
        this.deviceMonitorShowing.set(deviceMonitorShowing);
    }
}

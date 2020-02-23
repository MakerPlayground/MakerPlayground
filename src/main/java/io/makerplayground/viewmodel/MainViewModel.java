package io.makerplayground.viewmodel;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import io.makerplayground.scope.AppScope;
import io.makerplayground.version.SoftwareVersion;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class MainViewModel implements ViewModel {

    @InjectScope
    private AppScope appScope;

    private StringProperty title = new SimpleStringProperty();

    public void initialize() {
        updateTitleText();
        appScope.currentProjectProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getInteractiveModel().stop();
            }
            updateTitleText();
        });
    }

    private void updateTitleText() {
        if (appScope.getCurrentProject().getFilePath().isEmpty()) {
            title.set(SoftwareVersion.getCurrentVersion().getBuildName() + " - Untitled Project");
        } else {
            title.set(SoftwareVersion.getCurrentVersion().getBuildName() + " - " + appScope.getCurrentProject().getFilePath());
        }
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public Window getWindow() {
        return appScope.getRootStage().getScene().getWindow();
    }

    public void closeRequest(WindowEvent event) {
        appScope.requestClose(event);
    }
}

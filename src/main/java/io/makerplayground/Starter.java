package io.makerplayground;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.project.Project;
import io.makerplayground.scope.AppScope;
import io.makerplayground.view.MainView;
import io.makerplayground.viewmodel.MainViewModel;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Starter extends Application {
    public static void main(String...args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        DeviceLibrary.INSTANCE.loadDeviceFromFiles();

        AppScope appScope = new AppScope();
        appScope.setRootStage(stage);

        List<String> parameters = getParameters().getUnnamed();
        if (!parameters.isEmpty()) {
            File f = new File(parameters.get(parameters.size() - 1));
            appScope.setCurrentProject(Project.loadProject(f).orElseGet(Project::new));
        } else {
            appScope.setCurrentProject(new Project());
        }

        ViewTuple<MainView, MainViewModel> viewTuple = FluentViewLoader.fxmlView(MainView.class)
                .providedScopes(appScope)
                .load();
        initializeStage(stage, viewTuple.getView(), viewTuple.getViewModel());
    }

    private void initializeStage(Stage stage, Parent root, MainViewModel viewModel) {
        Scene scene = new Scene(root,960,600);
        stage.getIcons().addAll(new Image(Starter.class.getResourceAsStream("/icons/taskbar/logo_taskbar_16.png"))
                , new Image(Starter.class.getResourceAsStream("/icons/taskbar/logo_taskbar_32.png"))
                , new Image(Starter.class.getResourceAsStream("/icons/taskbar/logo_taskbar_48.png"))
                , new Image(Starter.class.getResourceAsStream("/icons/taskbar/logo_taskbar_256.png")));
        stage.titleProperty().bind(viewModel.titleProperty());
        stage.setScene(scene);

        // prevent the window from being too small (primaryStage.setMinWidth(800) doesn't work as this function take
        // into account the title bar which is platform dependent so the window is actually a little bit larger than
        // 800x600 initially so we use primaryStage.getWidth/Height() to get the actual size and lock it)
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());

        // close program
        stage.setOnCloseRequest(viewModel::closeRequest);
        stage.show();
    }
}

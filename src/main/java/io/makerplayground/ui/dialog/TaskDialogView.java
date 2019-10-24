package io.makerplayground.ui.dialog;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.io.IOException;
import java.util.function.BooleanSupplier;

public class TaskDialogView<T extends Task> extends UndecoratedDialog {
    private final AnchorPane anchorPane = new AnchorPane();

    @FXML private Label progress;
    @FXML private ProgressBar progressBar;
    @FXML private ImageView imgView;
    private final RotateTransition rt;

    public TaskDialogView(Window owner, T task, String taskName) {
        super(owner);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/TaskDialogView.fxml"));
        fxmlLoader.setRoot(anchorPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        rt = new RotateTransition();
        rt.setNode(imgView);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);

        Image errorImage = new Image(getClass().getResourceAsStream("/icons/Error-uploading.png"));
        Image successImage = new Image(getClass().getResourceAsStream("/icons/Success.png"));
        Image workingImage = new Image(getClass().getResourceAsStream("/icons/Uploading.png"));

        progress.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        imgView.setImage(workingImage);

        task.setOnScheduled(event -> rt.play());
        task.setOnCancelled(event -> rt.stop());
        task.setOnFailed(event -> {
            imgView.setImage(errorImage);
            progress.setTextFill(Color.RED);
            rt.stop();
        });
        task.setOnSucceeded(event1 -> {
            imgView.setImage(successImage);
            rt.stop();
            imgView.setRotate(0);
        });
        setContent(anchorPane);
        setClosingPredicate(() -> task.getState() == Worker.State.SUCCEEDED || task.getState() == Worker.State.FAILED);
    }
}

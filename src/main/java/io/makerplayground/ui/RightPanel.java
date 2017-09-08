package io.makerplayground.ui;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.generator.UploadTask;
import io.makerplayground.helper.SingletonUploadClick;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.helper.SingletonWiringDiagram;
import io.makerplayground.helper.UploadResult;
import io.makerplayground.project.Project;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceView;
import io.makerplayground.ui.devicepanel.ConfigActualDeviceViewModel;
import io.makerplayground.ui.devicepanel.DevicePanelView;
import io.makerplayground.ui.devicepanel.DevicePanelViewModel;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {

    private final Project project;

    public RightPanel(Project project) {
        this.project = project;
        //File f = new File("src/main/resources/css/RightPanel.css");
        //getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
        getStylesheets().add(RightPanel.class.getResource("/css/RightPanel.css").toExternalForm());
        initView();
    }

    private void initView() {
        setStyle("-fx-background-color : #4d5666");
        setMaxWidth(330.0);
        DevicePanelViewModel devicePanelViewModel = new DevicePanelViewModel(project);
        DevicePanelView devicePanelView = new DevicePanelView(devicePanelViewModel);
        Button configureBtn = new Button("Configure Device");

        configureBtn.setOnAction(event -> {
            if (project.getAllDevice().size() == 0) {
                ErrorDialogView errorDialogView = new ErrorDialogView("There is no device yet");
                errorDialogView.showAndWait();
            }
            else {
                ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
                ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
                configActualDeviceView.showAndWait();
            }
        });


        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(event -> {
            if (!DeviceMapper.autoAssignDevices(project)) {
                ErrorDialogView errorDialogView = new ErrorDialogView("Not enough port");
                errorDialogView.showAndWait();
                return;
            }
            Sourcecode code = Sourcecode.generateCode(project, false);
            if (code.getError() != null) {
                ErrorDialogView errorDialogView = new ErrorDialogView(code.getError().getDescription());
                errorDialogView.showAndWait();
            } else {
                SingletonWiringDiagram.getInstance().setOpenTime();
                GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
                GenerateView generateView = new GenerateView(generateViewModel);
                generateView.showAndWait();
            }
        });
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction(event -> {
            SingletonUploadClick.getInstance().click();
//            Dialog dialog = new Dialog();
//
//            Label label = new Label("Upload");
//            GridPane.setRowIndex(label, 0);
//            GridPane.setColumnIndex(label, 0);
//
//            ProgressBar progressBar = new ProgressBar(0);
//            progressBar.setMaxWidth(Double.MAX_VALUE);
//            GridPane.setRowIndex(progressBar, 0);
//            GridPane.setColumnIndex(progressBar, 1);
//
//            Label progress = new Label();
//            GridPane.setRowIndex(progress, 1);
//            GridPane.setColumnIndex(progress, 1);
//
//            TextArea textArea = new TextArea();
//            textArea.setPrefRowCount(3);
//            textArea.setEditable(false);
//
//            TitledPane detailPane = new TitledPane("More details", textArea);
//            detailPane.setExpanded(false);
//            // resize when collapse the dialog according to this post on stackoverflow
//            // https://stackoverflow.com/questions/36581662/dialog-doesnt-resize-when-titledpane-expanded
//            detailPane.setAnimated(false);
//            detailPane.expandedProperty().addListener((obs, oldValue, newValue) -> {
//                Platform.runLater(() -> {
//                    detailPane.requestLayout();
//                    detailPane.getScene().getWindow().sizeToScene();
//                });
//            });
//            GridPane.setRowIndex(detailPane, 2);
//            GridPane.setColumnIndex(detailPane, 0);
//            GridPane.setColumnSpan(detailPane, 2);
//
////            Button okButton = new Button("Ok");
////            okButton.setDisable(true);
////            GridPane.setRowIndex(okButton, 3);
////            GridPane.setColumnIndex(okButton, 0);
////            GridPane.setColumnSpan(okButton, 2);
////            okButton.setOnAction(event1 -> dialog.close());
//
//            GridPane gridPane = new GridPane();
//            gridPane.setPadding(new Insets(20, 20, 0, 20));
//            gridPane.getStylesheets().add(RightPanel.class.getResource("/css/UploadDialog.css").toExternalForm());
//            gridPane.setHgap(10);
//            gridPane.setVgap(0);
//            gridPane.getChildren().addAll(label, progressBar, progress, detailPane/*, okButton*/);
//
//            ButtonType buttonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
//            dialog.setTitle("Upload");
//            dialog.getDialogPane().getButtonTypes().add(buttonType);
//            dialog.getDialogPane().lookupButton(buttonType).setDisable(true);
//            dialog.getDialogPane().setContent(gridPane);
//            dialog.show();

            UploadTask uploadTask = new UploadTask(project);

            UploadDialogView uploadDialogView = new UploadDialogView(uploadTask);
            uploadDialogView.progressProperty().bind(uploadTask.progressProperty());
            uploadDialogView.descriptionProperty().bind(uploadTask.messageProperty());
            uploadDialogView.logProperty().bind(uploadTask.logProperty());
            uploadDialogView.show();

            new Thread(uploadTask).start();
        });

        final Hyperlink hpl = new Hyperlink("Feedback");

        hpl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                SingletonUtilTools.getInstance().setAll("FEEDBACK");

                String s = "http://www.makerplayground.io/";
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(URI.create(s));
                } catch (IOException ev) {
                    ev.printStackTrace();
                }
            }
        });


        VBox projectButton = new VBox();
        projectButton.setStyle("-fx-background-color : #313644");
        projectButton.setSpacing(2.0);
        projectButton.getChildren().addAll(configureBtn, generateBtn, uploadBtn, hpl);
        projectButton.setAlignment(Pos.CENTER);
        projectButton.setPadding(new Insets(20.0,20.0,20.0,20.0));

        configureBtn.prefWidthProperty().bind(projectButton.widthProperty());
        generateBtn.prefWidthProperty().bind(projectButton.widthProperty());
        uploadBtn.prefWidthProperty().bind(projectButton.widthProperty());

        AnchorPane.setLeftAnchor(devicePanelView,0.0);
        AnchorPane.setRightAnchor(devicePanelView,0.0);
        AnchorPane.setTopAnchor(devicePanelView,0.0);
        AnchorPane.setBottomAnchor(devicePanelView,120.0);
        AnchorPane.setLeftAnchor(projectButton,0.0);
        AnchorPane.setRightAnchor(projectButton,0.0);
        AnchorPane.setBottomAnchor(projectButton,0.0);

        getChildren().addAll(devicePanelView, projectButton);
    }

    public static class ProgressForm {
        private final Stage dialogStage;
        private final ProgressIndicator pin = new ProgressIndicator();

        public ProgressForm() {
            dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            pin.setProgress(-1F);

            final HBox hb = new HBox();
            hb.setSpacing(5);
            hb.setAlignment(Pos.CENTER);
            hb.getChildren().addAll(pin);

            Scene scene = new Scene(hb);
            dialogStage.setScene(scene);
        }

        public Stage getDialogStage() {
            return dialogStage;
        }
    }


}

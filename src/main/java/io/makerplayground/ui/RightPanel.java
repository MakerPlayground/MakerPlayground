package io.makerplayground.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.generator.Sourcecode;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private enum UploadResult {OK, CANT_FIND_PIO, NOT_ENOUGH_PORT, CANT_GENERATE_CODE, UNKNOWN_ERROR, CANT_FIND_BOARD}

    ;
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
            ConfigActualDeviceViewModel configActualDeviceViewModel = new ConfigActualDeviceViewModel(project);
            ConfigActualDeviceView configActualDeviceView = new ConfigActualDeviceView(configActualDeviceViewModel);
            configActualDeviceView.showAndWait();
        });

        //configureBtn.setOnAction(event -> DeviceMapper.getSupportedDeviceList(project));

        Button generateBtn = new Button("Generate Project");
        generateBtn.setOnAction(event -> {
            if (!DeviceMapper.autoAssignDevices(project)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough port", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            Sourcecode code = Sourcecode.generateCode(project, false);
            if (code.getError() != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, code.getError().getDescription(), ButtonType.OK);
                alert.showAndWait();
            } else {
                GenerateViewModel generateViewModel = new GenerateViewModel(project, code);
                GenerateView generateView = new GenerateView(generateViewModel);
                generateView.showAndWait();
            }
        });
        Button uploadBtn = new Button("Upload");
        uploadBtn.setOnAction(event -> {
            Dialog dialog = new Dialog();

            Label label = new Label("Upload");
            GridPane.setRowIndex(label, 0);
            GridPane.setColumnIndex(label, 0);

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setMaxWidth(Double.MAX_VALUE);
            GridPane.setRowIndex(progressBar, 0);
            GridPane.setColumnIndex(progressBar, 1);

            Label progress = new Label();
            GridPane.setRowIndex(progress, 1);
            GridPane.setColumnIndex(progress, 1);

            TextArea textArea = new TextArea();
            textArea.setPrefRowCount(3);
            textArea.setEditable(false);

            TitledPane detailPane = new TitledPane("More details", textArea);
            detailPane.setExpanded(false);
            // resize when collapse the dialog according to this post on stackoverflow
            // https://stackoverflow.com/questions/36581662/dialog-doesnt-resize-when-titledpane-expanded
            detailPane.setAnimated(false);
            detailPane.expandedProperty().addListener((obs, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    detailPane.requestLayout();
                    detailPane.getScene().getWindow().sizeToScene();
                });
            });
            GridPane.setRowIndex(detailPane, 2);
            GridPane.setColumnIndex(detailPane, 0);
            GridPane.setColumnSpan(detailPane, 2);

//            Button okButton = new Button("Ok");
//            okButton.setDisable(true);
//            GridPane.setRowIndex(okButton, 3);
//            GridPane.setColumnIndex(okButton, 0);
//            GridPane.setColumnSpan(okButton, 2);
//            okButton.setOnAction(event1 -> dialog.close());

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(20, 20, 0, 20));
            gridPane.getStylesheets().add(RightPanel.class.getResource("/css/UploadDialog.css").toExternalForm());
            gridPane.setHgap(10);
            gridPane.setVgap(0);
            gridPane.getChildren().addAll(label, progressBar, progress, detailPane/*, okButton*/);

            ButtonType buttonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(buttonType);
            dialog.getDialogPane().lookupButton(buttonType).setDisable(true);
            dialog.getDialogPane().setContent(gridPane);
            dialog.show();

            Task<UploadResult> uploadTask = new Task<UploadResult>() {
                @Override
                protected UploadResult call() throws Exception {
                    updateProgress(0, 1);
                    updateMessage("Checking project");
                    if (!DeviceMapper.autoAssignDevices(project)) {
                        updateMessage("Error: not enough port available");
                        return UploadResult.NOT_ENOUGH_PORT;
                    }
                    Sourcecode sourcecode = Sourcecode.generateCode(project, true);
                    if (sourcecode.getError() != null) {
                        updateMessage("Error: " + sourcecode.getError().getDescription());
                        return UploadResult.CANT_GENERATE_CODE;
                    }

                    updateProgress(0.25, 1);
                    updateMessage("Preparing to generate project");
                    List<String> library = null;
                    String platform = project.getController().getPlatform().getPlatformioId();
                    String code = sourcecode.getCode();
                    library = project.getAllDeviceTypeUsed().stream()
                            .map(genericDevice -> "MP_" + genericDevice.getName().replace(" ", "_"))
                            .collect(Collectors.toList());
                    //System.out.println(code);
                    //System.out.println(library);
                    Path currentRelativePath = Paths.get("");
                    String path = currentRelativePath.toAbsolutePath().toString();
                    //System.out.println("Current relative path is: " + path);
                    try {
                        FileUtils.deleteDirectory(new File(path + File.separator + "upload" + File.separator + "project"));
                        FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project"));

                        currentRelativePath = Paths.get("");
                        path = currentRelativePath.toAbsolutePath().toString();
                        System.out.println("Current relative path is: " + path);

                        ProcessBuilder builder = new ProcessBuilder("pio", "init", "--board", platform);
                        builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
                        builder.redirectErrorStream(true);
                        Process p = builder.start();
                        Scanner s = new Scanner(p.getInputStream());
                        while (s.hasNextLine()) {
//                        System.out.println(s.nextLine());
                        }
                        s.close();
                        try {
                            int result = p.waitFor();
                        } catch (InterruptedException e) {
                            return UploadResult.UNKNOWN_ERROR;
                        }

                        //Runtime.getRuntime().exec("pio init --board "+platform);
                        //System.out.println(platform);
                    } catch (IOException e) {
                        updateMessage("Error: Can't find platformio");
                        return UploadResult.CANT_FIND_PIO;
                    }

                    updateProgress(0.5, 1);
                    updateMessage("Generate source files and libraries");
                    try {
                        FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "src"));
                        FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib"));

                        // generate source file
                        FileWriter fw = new FileWriter(path + File.separator + "upload" + File.separator + "project" + File.separator + "src" + File.separator + "main.cpp");
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(code);
                        bw.close();
                        fw.close();

                        // copy library files
                        for (String x : library) {
                            FileUtils.forceMkdir(new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x));
                            File sourcecpp = new File(path + File.separator + "lib" + File.separator + x + ".cpp");
                            File destcpp = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".cpp");
                            File sourceh = new File(path + File.separator + "lib" + File.separator + x + ".h");
                            File desth = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x + ".h");
                            Files.copy(sourcecpp.toPath(), destcpp.toPath());
                            Files.copy(sourceh.toPath(), desth.toPath());
                        }
                    } catch (IOException e) {
                        updateMessage("Error: Missing some libraries");
                        return UploadResult.UNKNOWN_ERROR;
                    }

                    updateProgress(0.75, 1);
                    updateMessage("Uploading to board");
                    try {
                        ProcessBuilder builder = new ProcessBuilder("platformio", "run", "--target", "upload");
                        builder.directory(new File("upload" + File.separator + "project").getAbsoluteFile()); // this is where you set the root folder for the executable to run with
                        builder.redirectErrorStream(true);
                        Process p = builder.start();
                        Scanner s = new Scanner(p.getInputStream());
                        while (s.hasNextLine()) {
                            System.out.println(s.nextLine());
                        }
                        s.close();
                        try {
                            int result = p.waitFor();
                            if (result == 1) {
                                updateMessage("Error: Can't find board. Please check connection.");
                                return UploadResult.CANT_FIND_BOARD;
                            }
                        } catch (InterruptedException e) {
                            return UploadResult.UNKNOWN_ERROR;
                        }
                    } catch (IOException e) {
                        return UploadResult.CANT_FIND_PIO;
                    }

                    updateProgress(1, 1);
                    updateMessage("Done");
                    return UploadResult.OK;
                }
            };

            // Auto close if there is no error, otherwise we keep it open to allow user to see error message
            uploadTask.setOnSucceeded(event1 -> {
                UploadResult result = uploadTask.getValue();
                if (result == UploadResult.OK) {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1),
                            event2 -> dialog.close()));
                    timeline.play();
                } else {
                    //okButton.setDisable(false);
                    detailPane.setExpanded(true);
                    dialog.getDialogPane().lookupButton(buttonType).setDisable(false);
                }
            });

            progressBar.progressProperty().bind(uploadTask.progressProperty());
            progress.textProperty().bind(uploadTask.messageProperty());
            new Thread(uploadTask).start();
        });

        VBox projectButton = new VBox();
        projectButton.setStyle("-fx-background-color : #313644");
        projectButton.setSpacing(2.0);
        projectButton.getChildren().addAll(configureBtn, generateBtn, uploadBtn);
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

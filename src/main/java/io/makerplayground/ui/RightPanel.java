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
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by Mai.Manju on 12-Jun-17.
 */
public class RightPanel extends AnchorPane {
    private enum UploadResult {OK, CANT_FIND_PIO};
    private final Project project;

    public RightPanel(Project project){
        this.project = project;
        File f = new File("src/main/resources/css/RightPanel.css");
        getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
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
            DeviceMapper.autoAssignDevices(project);
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
        uploadBtn.setOnAction((ActionEvent event) -> {
            DeviceMapper.autoAssignDevices(project);
            Sourcecode sourcecode = Sourcecode.generateCode(project, true);

            if (sourcecode.getError() != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, sourcecode.getError().getDescription(), ButtonType.OK);
                alert.showAndWait();
            } else {
                // OPEN progress bar
                Dialog dialog = new Dialog();
                ProgressIndicator pb = new ProgressIndicator();
                dialog.getDialogPane().getButtonTypes().add(new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
                dialog.getDialogPane().setContent(pb);
                dialog.show();


                Task<UploadResult> uploadTask = new Task<UploadResult>() {
                    @Override
                    protected UploadResult call() throws Exception {
                        List<String> library = null;
                        String platform = project.getController().getPlatform().getPlatformioId();
                        String code = sourcecode.getCode();
                        library = project.getAllDeviceTypeUsed().stream()
                                .map(genericDevice -> "MP_" + genericDevice.getName().replace(" ", "_"))
                                .collect(Collectors.toList());
                        library.add("MPU6050");
                        library.add("I2Cdev");
                        // TODO: call platform io here
                        System.out.println(code);
                        System.out.println(library);
                        Path currentRelativePath = Paths.get("");
                        String path = currentRelativePath.toAbsolutePath().toString();
                        System.out.println("Current relative path is: " + path);
                        try {
                            FileUtils.deleteDirectory(new File(path + File.separator +  "upload" + File.separator +  "project"));
                            FileUtils.forceMkdir(new File(path + File.separator +  "upload" + File.separator +  "project"));

                            currentRelativePath = Paths.get("");
                            path = currentRelativePath.toAbsolutePath().toString();
                            System.out.println("Current relative path is: " + path);

                            ProcessBuilder builder = new ProcessBuilder( "pio", "init", "--board", platform);
                            builder.directory( new File( "upload" + File.separator + "project" ).getAbsoluteFile() ); // this is where you set the root folder for the executable to run with
                            builder.redirectErrorStream(true);
                            Process p = builder.start();
                            Scanner s = new Scanner(p.getInputStream());
                            while (s.hasNextLine()) {
                                System.out.println(s.nextLine());
                            }
                            s.close();
                            try {
                                int result = p.waitFor();
                            } catch (InterruptedException e) {
                                return UploadResult.CANT_FIND_PIO;
                            }

                            //Runtime.getRuntime().exec("pio init --board "+platform);
                            System.out.println(platform);
                            FileUtils.forceMkdir(new File(path + File.separator +  "upload" + File.separator +  "project" + File.separator +  "src"));
                            FileUtils.forceMkdir(new File(path + File.separator +  "upload" + File.separator +  "project" + File.separator +  "lib"));
                        } catch (IOException e) {
                            return UploadResult.CANT_FIND_PIO;
                        }
                        FileWriter fw = null;
                        BufferedWriter bw = null;
                        try {
                            fw = new FileWriter(path + File.separator + "upload" + File.separator + "project" + File.separator +  "src" + File.separator + "main.cpp");
                            bw = new BufferedWriter(fw);
                            bw.write(code);
                            bw.close();
                            fw.close();
                        } catch (IOException e) {
                            return UploadResult.CANT_FIND_PIO;
                        }
                        for (String x : library) {
                            try {
                                FileUtils.forceMkdir(new File(path + File.separator +  "upload" + File.separator +  "project" + File.separator +  "lib" + File.separator + x));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            File sourcecpp = new File(path + File.separator +  "lib" + File.separator + x +".cpp");
                            File destcpp = new File(path + File.separator + "upload" + File.separator + "project"+ File.separator + "lib" + File.separator + x + File.separator + x+".cpp");
                            File sourceh = new File(path + File.separator + "lib" + File.separator + x+".h");
                            File desth = new File(path + File.separator + "upload" + File.separator + "project" + File.separator + "lib" + File.separator + x + File.separator + x +".h");
                            try {
                                Files.copy(sourcecpp.toPath(),destcpp.toPath());
                                Files.copy(sourceh.toPath(),desth.toPath());
                            } catch (IOException e) {
                                return UploadResult.CANT_FIND_PIO;
                            }
                        }
                        try {
                            ProcessBuilder builder = new ProcessBuilder( "platformio", "run", "--target", "upload");
                            builder.directory( new File( "upload" + File.separator + "project" ).getAbsoluteFile() ); // this is where you set the root folder for the executable to run with
                            builder.redirectErrorStream(true);
                            Process p = builder.start();
                            Scanner s = new Scanner(p.getInputStream());
                            while (s.hasNextLine()) {
                                System.out.println(s.nextLine());
                            }
                            s.close();
                            try {
                                int result = p.waitFor();
                            } catch (InterruptedException e) {
                                return UploadResult.CANT_FIND_PIO;
                            }
                        } catch (IOException e) {
                            return UploadResult.CANT_FIND_PIO;
                        }
                        return UploadResult.OK;
                    }
                };

                uploadTask.setOnSucceeded(event1 -> {
                    UploadResult result = uploadTask.getValue();
                    if (result == UploadResult.OK) {
                        pb.setProgress(1);
                    }
                    if (result == UploadResult.CANT_FIND_PIO) {
                        System.out.println("Can't find PIO");
                    }
                    dialog.close();
                });

                new Thread(uploadTask).start();
            }
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

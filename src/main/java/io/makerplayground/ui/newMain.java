package io.makerplayground.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.generator.Sourcecode;
import io.makerplayground.helper.Singleton;
import io.makerplayground.helper.SingletonLaunch;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.project.Project;
import io.makerplayground.ui.canvas.CanvasView;
import io.makerplayground.ui.canvas.CanvasViewModel;
import io.makerplayground.ui.devicepanel.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Natkanok Poksappaiboon on 12/6/2018 AD.
 */
public class newMain extends SplitPane {

    private final Project project;
   /* @FXML
    private Accordion contollerRight;*/
    @FXML
    private Button deviceBtn;
    @FXML
    private Button diagramBtn;
    @FXML
    private Button configBtn;
    @FXML
    private Button generateBtn;
    MainWindow newWinDow;
    DeviceSelectorView newDeviceSelector;
    ConfigActualDeviceView newConfigActualDeviceView;
    private ObservableList<ControlAddDevicePane> outputDevice;
    private ObservableList<ControlAddDevicePane> inputDevice;
    private ObservableList<ControlAddDevicePane> connectivityDevice;
    //private final GenerateViewModel viewModel;
   //private final ObservableList<TableDataList> observableTableList;
    ConfigActualDeviceViewModel newviewModel;
    GenerateViewModel newGenerateViewModel;
    //TableDataList newTable;
     Sourcecode sourcecode;
    GenerateView newGenerateView;

    public newMain(Project project) {
        this.project = project;
        newWinDow= new MainWindow(project);
        newDeviceSelector = new DeviceSelectorView();
        newviewModel = new ConfigActualDeviceViewModel(project);
        newConfigActualDeviceView = new ConfigActualDeviceView(newviewModel);
        newGenerateViewModel = new GenerateViewModel(project,sourcecode);
        newGenerateView = new  GenerateView(newGenerateViewModel);

        SingletonLaunch.getInstance().launchProgram();
        getStylesheets().add(RightPanel.class.getResource("/css/LeftPanel.css").toExternalForm());
        initView();
    }

    private void initView() {
        //setStyle("-fx-background-color : #12062C");
        //setDividerPositions(0.18, 0.82);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/NewMainWindow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDividerPositions(0.15, 0.85);
        this.setStyle("-fx-background-color : #FFFFFF ");
       // newDevice.setStyle("-fx-background-color : #5AC1AA");
        /*contollerRight.setExpandedPane(newDevice);
        contollerRight.expandedPaneProperty().addListener((observable, oldValue, newValue) ->{
            boolean checkAccordion = true; // This value will change to false if there's (at least) one pane that is in "expanded" state, so we don't have to expand anything manually
            for(TitledPane pane: contollerRight.getPanes()) {
                if(pane.isExpanded()) {
                    checkAccordion = false;

                }
            }
            /* Here we already know whether we need to expand the old pane again */
            /*if((checkAccordion == true) && (oldValue != null)) {
                Platform.runLater(() -> {
                    setDividerPositions(0.2, 0.8);
                    contollerRight.setExpandedPane(oldValue);
                });
            }
            //this.*/
            deviceBtn.setStyle("  -fx-font-size:18px;\n" +
                "    -fx-border-color: #5AC1AA transparent transparent #5AC1AA;\n" +
                "    -fx-background-color : #5AC1AA;\n" +
                "    -fx-border-color : #5AC1AA;\n" +
                "    -fx-text-fill: white;\n" +
                "    -fx-cursor: hand;");
            deviceBtn.setOnAction(event -> { newMain.this.getItems().remove(1);
                setDividerPositions(0.15, 0.85);
                this.setStyle("-fx-background-color : #FFFFFF");
                diagramBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                generateBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                configBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                deviceBtn.setStyle("  -fx-font-size:18px;\n" +
                        "    -fx-border-color: #5AC1AA transparent transparent #5AC1AA;\n" +
                        "    -fx-background-color : #5AC1AA;\n" +
                        "    -fx-border-color : #5AC1AA;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-cursor: hand;");
                newMain.this.getItems().add(newDeviceSelector);});

            diagramBtn.setOnAction(event -> { newMain.this.getItems().remove(1);
                setDividerPositions(0.15, 0.85);
                deviceBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                configBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                diagramBtn.setStyle("  -fx-font-size:18px;\n" +
                        "    -fx-border-color: #5AC1AA transparent transparent #5AC1AA;\n" +
                        "    -fx-background-color : #5AC1AA;\n" +
                        "    -fx-border-color : #5AC1AA;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-cursor: hand;");
                generateBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                newMain.this.getItems().add(newWinDow);});

            configBtn.setOnAction(event -> { newMain.this.getItems().remove(1);
                setDividerPositions(0.15, 0.85);
                this.setStyle("-fx-background-color : #F3F3F3");
                configBtn.setStyle("  -fx-font-size:18px;\n" +
                        "    -fx-border-color: #5AC1AA transparent transparent #5AC1AA;\n" +
                        "    -fx-background-color : #5AC1AA;\n" +
                        "    -fx-border-color : #5AC1AA;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-cursor: hand;");
                diagramBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                deviceBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                generateBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                newMain.this.getItems().add(newConfigActualDeviceView);

            });

            generateBtn.setOnAction(event -> { newMain.this.getItems().remove(1);
                setDividerPositions(0.15, 0.85);
                this.setStyle("-fx-background-color : #F2F2F2");
                generateBtn.setStyle("  -fx-font-size:18px;\n" +
                        "    -fx-border-color: #5AC1AA transparent transparent #5AC1AA;\n" +
                        "    -fx-background-color : #5AC1AA;\n" +
                        "    -fx-border-color : #5AC1AA;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-cursor: hand;");
                configBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                diagramBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                deviceBtn.setStyle(" -fx-background-color : #231F20;\n" +
                        "    -fx-border-color: #231F20 transparent transparent #5AC1AA;\n" +
                        "    -fx-border-width : 8px;\n" +
                        "    -fx-font-size: 14px;\n" +
                        "    -fx-font-weight: bold;\n" +
                        "    -fx-text-fill: #5AC1AA;");
                newMain.this.getItems().add(newGenerateView);

            });

            /*if (newValue == newDevice){
                newMain.this.getItems().remove(1);
                setDividerPositions(0.2, 0.8);
                this.setStyle("-fx-background-color : #FFFFFF");
                newMain.this.getItems().add(newDeviceSelector);
                //newWinDow = new MainWindow(project);
            }
            else if (newValue == newDiagram){
                newMain.this.getItems().remove(1);
                setDividerPositions(0.2, 0.8);
                newDiagram.setStyle("-fx-background-color : white");
                newMain.this.getItems().add(newWinDow);
            }
            else if (newValue == newCounfigure){
                newMain.this.getItems().remove(1);
                setDividerPositions(0.2, 0.8);
                newCounfigure.setStyle("-fx-background-color : #5AC1AA");
                this.setStyle("-fx-background-color : #F3F3F3");
                newMain.this.getItems().add(newConfigActualDeviceView);
            }
            else if (newValue == newGenerate){
                newMain.this.getItems().remove(1);
                setDividerPositions(0.2, 0.8);
                newGenerate.setStyle("-fx-background-color : #5AC1AA");
                this.setStyle("-fx-background-color : #F2F2F2");
                newMain.this.getItems().add(newGenerateView);
            }
            //newMain.this.getItems().remove(1);
        });
       // RightPanel rightPanel = new RightPanel(project);

        //CanvasViewModel canvasViewModel = new CanvasViewModel(project);
        //CanvasView canvasView = new CanvasView(canvasViewModel);*/

        setDividerPositions(0.15, 0.85);
        getItems().addAll(newDeviceSelector);
    }

    public Project getProject() {
        return project;
    }
}

package io.makerplayground.ui;

import io.makerplayground.generator.Diagram;

import io.makerplayground.generator.MPDiagram;
import io.makerplayground.helper.Platform;
import io.makerplayground.helper.SingletonWiringDiagram;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import io.makerplayground.ui.devicepanel.TableDataList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javafx.stage.Window;

import java.io.IOException;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends Dialog {
    @FXML private TextArea codeTextArea;
    @FXML private TableView<TableDataList> deviceTable;
    @FXML private TableColumn<TableDataList,String> nameColumn;
    @FXML private TableColumn<TableDataList,String> brandColumn;
    @FXML private TableColumn<TableDataList,String> modelColumn;
    @FXML private TableColumn<TableDataList,String> pinColumn;
//    @FXML private TableColumn<TableDataList,Hyperlink> urlColumn;
    @FXML private ScrollPane diagramScrollPane;
    @FXML private Tab simulateTab;
    @FXML private Tab codeDeviceTableTab;

    private final GenerateViewModel viewModel;

    public GenerateView(GenerateViewModel viewModel) {
        this.viewModel = viewModel;
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        setTitle("  Generate Project");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GenerateView.fxml"));
        fxmlLoader.setRoot(this.getDialogPane());
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initView();
    }

    private void initView() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> {
            SingletonWiringDiagram.getInstance().setCloseTime();
            window.hide();
        });

        Pane wiringDiagram;
        Platform platform = viewModel.getProject().getPlatform();
        if (platform == Platform.ARDUINO || platform == Platform.GROVE_ARDUINO) {
            wiringDiagram = new Diagram(viewModel.getProject());
        } else if (platform == Platform.MP_ARDUINO) {
            wiringDiagram = new MPDiagram(viewModel.getProject());
        } else {
            throw new IllegalStateException("Found unsupported platform(" + platform + ")");
        }

        diagramScrollPane.setContent(wiringDiagram);
        codeTextArea.setText(viewModel.getCode());
        codeTextArea.setEditable(false);

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

//        TableView table = new TableView();
//        TableColumn nameColumn = new TableColumn("Name");
//        TableColumn brandColumn = new TableColumn("Brand");
//        TableColumn modelColumn = new TableColumn("Model");
//        TableColumn pinColumn = new TableColumn("Pin");
//        TableColumn urlColumn = new TableColumn("URL");

        nameColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("name"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("model"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("pin"));
//        urlColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,Hyperlink>("url"));

           //TODO: Set urlColumn on Action ( go to website )

        deviceTable.setItems(viewModel.getObservableTableList());
        //deviceTable.getColumns().addAll(nameColumn,brandColumn,modelColumn,pinColumn,urlColumn);
//        TextArea code = new TextArea();
//        code.setText(viewModel.getCode());

//        test.getChildren().addAll(wiringDiagram,table, code);
//        scrollPane.setContent(test);
//        getDialogPane().setContent(scrollPane);
    }


}

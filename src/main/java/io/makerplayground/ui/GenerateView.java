package io.makerplayground.ui;

import io.makerplayground.generator.Diagram;
import io.makerplayground.ui.devicepanel.TableDataList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends Dialog {
    private final GenerateViewModel viewModel;

    public GenerateView(GenerateViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        ScrollPane scrollPane = new ScrollPane();
        VBox test = new VBox();

        Diagram wiringDiagram = new Diagram(viewModel.getProject());

        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        TableView table = new TableView();
        table.setEditable(false);
        TableColumn nameColumn = new TableColumn("Name");
        TableColumn brandColumn = new TableColumn("Brand");
        TableColumn modelColumn = new TableColumn("Model");
        TableColumn pinColumn = new TableColumn("Pin");
        TableColumn urlColumn = new TableColumn("URL");

        nameColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("name"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("model"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,String>("pin"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<TableDataList,Hyperlink>("url"));

           //TODO: Set urlColumn on Action ( go to website )

        table.setItems(viewModel.getObservableTableList());
        table.getColumns().addAll(nameColumn,brandColumn,modelColumn,pinColumn,urlColumn);
        TextArea code = new TextArea();
        code.setText(viewModel.getCode());

        test.getChildren().addAll(wiringDiagram,table, code);
        scrollPane.setContent(test);
        getDialogPane().setContent(scrollPane);
    }


}

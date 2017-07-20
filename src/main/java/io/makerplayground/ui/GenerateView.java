package io.makerplayground.ui;

import io.makerplayground.generator.Diagram;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends Dialog {
    @FXML private TextArea codeTextArea;
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
        window.setOnCloseRequest(event -> window.hide());
        Diagram wiringDiagram = new Diagram(viewModel.getProject());
        diagramScrollPane.setContent(wiringDiagram);
        codeTextArea.setText(viewModel.getCode());
//        ScrollPane scrollPane = new ScrollPane();
//        VBox test = new VBox();
//
//        Diagram wiringDiagram = new Diagram(viewModel.getProject());
//
//        TextArea code = new TextArea();
//        code.setText(viewModel.getCode());
//        codeTextArea.setText(viewModel.getCode());
//
//        test.getChildren().addAll(wiringDiagram, code);
//        scrollPane.setContent(test);
//        getDialogPane().setContent(scrollPane);
    }
}

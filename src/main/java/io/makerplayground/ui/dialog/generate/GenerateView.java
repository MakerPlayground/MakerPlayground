package io.makerplayground.ui.dialog.generate;

import io.makerplayground.generator.Diagram;
import io.makerplayground.generator.MPDiagram;
import io.makerplayground.helper.Platform;
import io.makerplayground.ui.dialog.UndecoratedDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends UndecoratedDialog {
    private final TabPane tabPane = new TabPane();
    @FXML private TextArea codeTextArea;
    @FXML private TableView<TableDataList> deviceTable;
    @FXML private TableColumn<TableDataList,String> nameColumn;
    @FXML private TableColumn<TableDataList,String> brandColumn;
    @FXML private TableColumn<TableDataList,String> modelColumn;
    @FXML private TableColumn<TableDataList,String> pinColumn;
    @FXML private ScrollPane diagramScrollPane;
    @FXML private Tab simulateTab;
    @FXML private Tab codeDeviceTableTab;

    private final GenerateViewModel viewModel;

    public GenerateView(Window owner, GenerateViewModel viewModel) {
        super(owner);
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/generate/GenerateView.fxml"));
        fxmlLoader.setRoot(tabPane);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initView();
        setContent(tabPane);
    }

    private void initView() {
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

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));

        deviceTable.setItems(viewModel.getObservableTableList());
    }


}

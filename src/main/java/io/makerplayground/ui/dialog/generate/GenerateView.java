/*
 * Copyright (c) 2018. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.dialog.generate;

import io.makerplayground.generator.diagram.WiringDiagram;
import io.makerplayground.ui.dialog.UndecoratedDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends TabPane {
    @FXML private TextArea codeTextArea;
    @FXML private TableView<TableDataList> deviceTable;
    @FXML private TableColumn<TableDataList,String> nameColumn;
    @FXML private TableColumn<TableDataList,String> brandColumn;
    @FXML private TableColumn<TableDataList,String> modelColumn;
    @FXML private TableColumn<TableDataList,String> pinColumn;
    @FXML private ScrollPane diagramScrollPane;

    private final GenerateViewModel viewModel;

    public GenerateView(GenerateViewModel viewModel) {
        this.viewModel = viewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/generate/GenerateView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!viewModel.hasError()) {
            initView();
        }
    }

    private void initView() {
        Pane wiringDiagram = WiringDiagram.make(viewModel.getProject());

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

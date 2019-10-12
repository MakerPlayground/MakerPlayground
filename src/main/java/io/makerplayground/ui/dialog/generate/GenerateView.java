/*
 * Copyright (c) 2019. The Maker Playground Authors.
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class GenerateView extends TabPane {
    @FXML private TextArea codeTextArea;
    @FXML private TableView<TableDataList> deviceTable;
    @FXML private TableColumn<TableDataList,String> nameColumn;
    @FXML private TableColumn<TableDataList,String> brandColumn;
    @FXML private TableColumn<TableDataList,String> modelColumn;
    @FXML private TableColumn<TableDataList,String> pinColumn;
    @FXML private ScrollPane diagramScrollPane;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomDefaultButton;

    public static final double DEFAULT_ZOOM_SCALE = 0.5;
    public static final int DEFAULT_TAB_INDEX = 0;

    private final GenerateViewModel viewModel;

    private DoubleProperty scale = new SimpleDoubleProperty(DEFAULT_ZOOM_SCALE);
    private OnZoomChanged onZoomChanged = null;
    private OnTabIndexChanged onTabIndexChanged = null;

    public interface OnZoomChanged {
        void onChanged(double newScale);
    }

    public interface OnTabIndexChanged {
        void onChanged(int newTabIndex);
    }

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
        scale.addListener((observable, oldValue, newValue) -> {
            if (onZoomChanged != null) {
                onZoomChanged.onChanged(newValue.doubleValue());
            }
        });
        zoomInButton.setOnAction(event -> scale.set(scale.get() + 0.1));
        zoomOutButton.setOnAction(event -> scale.set(Math.max(0.1, scale.get() - 0.1)));
        zoomDefaultButton.setOnAction(event -> scale.set(0.5));

        this.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (onTabIndexChanged != null) {
                onTabIndexChanged.onChanged(newValue.intValue());
            }
        });

        Pane wiringDiagram = WiringDiagram.make(viewModel.getProject());
        wiringDiagram.scaleXProperty().bind(scale);
        wiringDiagram.scaleYProperty().bind(scale);

        diagramScrollPane.setContent(new Group(wiringDiagram));
        codeTextArea.setText(viewModel.getCode());
        codeTextArea.setEditable(false);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        brandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        pinColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));

        deviceTable.setItems(viewModel.getObservableTableList());
    }

    public void setZoomLevel(double scale) {
        this.scale.set(scale);
    }

    public void setTabIndex(int tabIndex) {
        this.getSelectionModel().select(tabIndex);
    }

    public void setOnZoomLevelChanged(OnZoomChanged onZoomChanged) {
        this.onZoomChanged = onZoomChanged;
    }

    public void setOnTabIndexChanged(OnTabIndexChanged onTabIndexChanged) {
        this.onTabIndexChanged = onTabIndexChanged;
    }
}

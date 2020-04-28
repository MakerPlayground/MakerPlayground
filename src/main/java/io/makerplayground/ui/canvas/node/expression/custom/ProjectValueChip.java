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

package io.makerplayground.ui.canvas.node.expression.custom;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;

public class ProjectValueChip extends Chip<ProjectValue> {

    @FXML private ComboBox<ProjectValue> comboBox;
    @FXML private Rectangle background;

    private static final String TEXT_CSS = "-fx-font-size: 10; -fx-fill: white; -fx-font-weight: bold; -fx-text-alignment: center;";
    private static final String COMBOBOX_LISTVIEW_TEXT_CSS = "-fx-font-size: 10; -fx-text-alignment: center;";

    public ProjectValueChip(ProjectValue initialValue, ObservableList<ProjectValue> projectValues) {
        super(initialValue, Term.Type.VALUE, projectValues);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ProjectValueChip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        comboBox.itemsProperty().set(getChoices());
        if (getValue() != null) {
            comboBox.setValue(getValue());
        }
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setGraphicTextGap(0);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle(COMBOBOX_LISTVIEW_TEXT_CSS);
                    setGraphic(text);
                    setPrefHeight(30);
                }
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setGraphicTextGap(0);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle(TEXT_CSS);
                    setGraphic(text);
                }
            }
        });

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));

        // update width of the background based on the combobox width
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> background.setWidth(newValue.getWidth()));

        getChoices().addListener((InvalidationListener) c -> {
            ProjectValue selectedValue = comboBox.getSelectionModel().getSelectedItem();
            if (!comboBox.getItems().contains(selectedValue)) {
                comboBox.getSelectionModel().clearSelection();
            }
        });
    }

    @Override
    public ValueTerm getTerm() {
        return new ValueTerm(getValue());
    }
}

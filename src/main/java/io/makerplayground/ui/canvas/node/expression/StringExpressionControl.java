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

package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.Parameter;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.ProjectValueExpression;
import io.makerplayground.project.expression.SimpleStringExpression;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;

public class StringExpressionControl extends HBox {

    private final Parameter parameter;
    private final List<ProjectValue> projectValues;

    private ReadOnlyObjectWrapper<Expression> expression = new ReadOnlyObjectWrapper<>();

    public StringExpressionControl(Parameter p, List<ProjectValue> projectValues, Expression expression) {
        this.parameter = p;
        this.projectValues = projectValues;
        this.expression.set(expression);
        initView();
    }

    private void initView() {
        getChildren().clear();
        getStylesheets().add(getClass().getResource("/css/canvas/node/expressioncontrol/StringExpressionControl.css").toExternalForm());

        RadioMenuItem numberRadioButton = new RadioMenuItem("Text");
        RadioMenuItem valueRadioButton = new RadioMenuItem("Value");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(numberRadioButton, valueRadioButton);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(numberRadioButton, valueRadioButton);

        ImageView configButton = new ImageView(new Image(getClass().getResourceAsStream("/css/canvas/node/expressioncontrol/advance-setting-press.png")));
        configButton.setFitWidth(25);
        configButton.setPreserveRatio(true);
        configButton.setStyle("-fx-cursor:hand;");
        configButton.setOnMousePressed(event -> contextMenu.show(configButton, Side.BOTTOM, 0, 0));

        if (getExpression() instanceof SimpleStringExpression) {
            TextField textField = new TextField();
            textField.textProperty().addListener((observable, oldValue, newValue) -> expression.set(new SimpleStringExpression(newValue)));
            textField.setText(((SimpleStringExpression) getExpression()).getString());
            toggleGroup.selectToggle(numberRadioButton);
            getChildren().add(textField);
        } else if (getExpression() instanceof ProjectValueExpression) {
            ComboBox<ProjectValue> comboBox = new ComboBox<>(FXCollections.observableArrayList(projectValues));
            comboBox.setCellFactory(param -> new ListCell<>(){
                @Override
                protected void updateItem(ProjectValue item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                    }
                }
            });
            comboBox.setButtonCell(new ListCell<>(){
                @Override
                protected void updateItem(ProjectValue item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText("");
                    } else {
                        setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                    }
                }
            });
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> expression.set(new ProjectValueExpression(newValue)));
            ProjectValue projectValue = ((ProjectValueExpression) getExpression()).getProjectValue();
            if (projectValue != null) {
                comboBox.getSelectionModel().select(projectValue);
            }
            getChildren().add(comboBox);
        } else {
            throw new IllegalStateException();
        }

        getChildren().add(configButton);
        setSpacing(5);

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == numberRadioButton) {
                expression.set(new SimpleStringExpression(""));
            } else if (newValue == valueRadioButton) {
                expression.set(new ProjectValueExpression());
            }
            initView();
        });
    }

    public Expression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<Expression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }
}

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

package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.Action;
import io.makerplayground.device.CategoricalConstraint;
import io.makerplayground.device.Parameter;
import io.makerplayground.helper.ControlType;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SliderNumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SpinnerNumberWithUnitExpressionControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;

import java.util.List;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class SceneDevicePropertyWindow extends PopOver {
    private final SceneDeviceIconViewModel viewModel;

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label actionLabel;
    private ComboBox<Action> actionComboBox;

    public SceneDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + viewModel.getImageName() + ".png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(viewModel.getName());
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        actionLabel = new Label("Action");
        GridPane.setRowIndex(actionLabel, 0);
        GridPane.setColumnIndex(actionLabel, 0);

        actionComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.getGenericDevice().getAction()));
        actionComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Action> call(ListView<Action> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Action item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        actionComboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(Action item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        actionComboBox.getSelectionModel().select(viewModel.getAction());
        // bind action selected to the view model
        viewModel.actionProperty().bind(actionComboBox.getSelectionModel().selectedItemProperty());
        GridPane.setRowIndex(actionComboBox, 0);
        GridPane.setColumnIndex(actionComboBox, 1);

        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(actionLabel, actionComboBox);

        // add listener to update property sheet when the condition selected has changed
        viewModel.actionProperty().addListener((observable, oldValue, newValue) -> redrawProperty());
        redrawProperty();

        // arrange title and property sheet
        VBox mainPane = new VBox();
        mainPane.getStylesheets().add(this.getClass().getResource("/css/canvas/node/usersetting/DevicePropertyWindow.css").toExternalForm());
        mainPane.getChildren().addAll(titleHBox, propertyPane);
        mainPane.setSpacing(5.0);
        mainPane.setPadding(new Insets(20, 20, 20, 20));

        setDetachable(false);
        setContentNode(mainPane);
    }

    private void redrawProperty() {
        propertyPane.getChildren().retainAll(actionLabel, actionComboBox);

        List<Parameter> params = viewModel.getAction().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            name.setMinHeight(25);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
            GridPane.setRowIndex(name, i+1);
            GridPane.setColumnIndex(name, 0);
            GridPane.setValignment(name, VPos.TOP);

            Node control = null;
            if (p.getDataType() == DataType.VALUE) {
                ObservableList<ProjectValue> list = FXCollections.observableArrayList(viewModel.getProjectValue());
                ComboBox<ProjectValue> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new ProjectValueExpression(newValue)));
                if (viewModel.getParameterValue(p) != null) {
                    comboBox.setValue(((ProjectValueExpression) viewModel.getParameterValue(p)).getProjectValue());
                }
                comboBox.setCellFactory(param -> new ListCell<>() {
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
                control = comboBox;
            } else if (p.getControlType() == ControlType.SLIDER) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                SliderNumberWithUnitExpressionControl expressionControl = new SliderNumberWithUnitExpressionControl(
                        p,
                        viewModel.getProjectValue(),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.TEXTBOX) {
                TextField textField = new TextField();
                textField.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
                textField.setText(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
                control = textField;
            } else if (p.getControlType() == ControlType.DROPDOWN) {
                ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                ComboBox<String> comboBox = new ComboBox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
                comboBox.getSelectionModel().select(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
                control = comboBox;
            } else if (p.getControlType() == ControlType.SPINBOX) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                SpinnerNumberWithUnitExpressionControl expressionControl = new SpinnerNumberWithUnitExpressionControl(
                        p,
                        viewModel.getProjectValue(),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else {
                throw new IllegalStateException("Found unknown control type " + p);
            }
            GridPane.setRowIndex(control, i+1);
            GridPane.setColumnIndex(control, 1);
            GridPane.setHalignment(control, HPos.LEFT);
            GridPane.setFillWidth(control, false);
            propertyPane.getChildren().addAll(name, control);
        }
    }
}

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

import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.RealTimeClock;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.NumericTextFieldWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SliderNumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SpinnerNumberWithUnitExpressionControl;
import io.makerplayground.ui.control.AutoResizeCombobox;
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
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.PopOver;

import java.util.List;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class SceneDevicePropertyWindow extends GridPane {
    private final SceneDeviceIconViewModel viewModel;

    private Label actionLabel;
    private AutoResizeCombobox<Action> actionComboBox;
    private static final RowConstraints rowConstraints = new RowConstraints(16, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

    public SceneDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        // Create ComboBox for user to select a condition
        actionLabel = new Label("Action");
        GridPane.setRowIndex(actionLabel, 0);
        GridPane.setColumnIndex(actionLabel, 0);

        actionComboBox = new AutoResizeCombobox<>(FXCollections.observableArrayList(viewModel.getGenericDevice().getAction()));
        actionComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Action action) {
                return action.getName();
            }

            @Override
            public Action fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
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
        actionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setAction(newValue);
            redrawProperty();
        });
        GridPane.setRowIndex(actionComboBox, 0);
        GridPane.setColumnIndex(actionComboBox, 1);
        GridPane.setHalignment(actionComboBox, HPos.LEFT);
        GridPane.setFillWidth(actionComboBox, false);

//        getChildren().addAll(actionLabel, actionComboBox);
//        getRowConstraints().add(rowConstraints);

        // try to align the label on the first column of every device while allowing a long label to enlarge
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(60);
        getColumnConstraints().add(labelColumn);

        setHgap(10);
        setVgap(5);
        setPadding(new Insets(4, 6, 4, 10));

        redrawProperty();
    }

    private void redrawProperty() {
        getChildren().clear();
        getRowConstraints().clear();

        getChildren().addAll(actionLabel, actionComboBox);
        getRowConstraints().add(rowConstraints);

        List<Parameter> params = viewModel.getAction().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            name.setMinHeight(16);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
            GridPane.setRowIndex(name, i+1);
            GridPane.setColumnIndex(name, 0);
            GridPane.setValignment(name, VPos.TOP);

            Node control = null;
            if (p.getControlType() == ControlType.SLIDER || p.getControlType() == ControlType.SPINBOX) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                NumericTextFieldWithUnitExpressionControl expressionControl = new NumericTextFieldWithUnitExpressionControl(
                        p,
                        viewModel.getProjectValue(),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
//            } else if (p.getControlType() == ControlType.TEXTBOX) {
//                TextField textField = new TextField();
//                textField.textProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
//                textField.setText(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
//                control = textField;
            } else if (p.getControlType() == ControlType.DROPDOWN) {
                ObservableList<String> list = FXCollections.observableArrayList(((CategoricalConstraint) p.getConstraint()).getCategories());
                AutoResizeCombobox<String> comboBox = new AutoResizeCombobox<>(list);
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new SimpleStringExpression(newValue)));
                comboBox.getSelectionModel().select(((SimpleStringExpression) viewModel.getParameterValue(p)).getString());
                control = comboBox;
//            } else if (p.getControlType() == ControlType.SPINBOX) {
//                if (viewModel.getParameterValue(p) == null) {
//                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
//                }
//                SpinnerNumberWithUnitExpressionControl expressionControl = new SpinnerNumberWithUnitExpressionControl(
//                        p,
//                        viewModel.getProjectValue(),
//                        viewModel.getParameterValue(p)
//                );
//                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
//                control = expressionControl;
            } else if (p.getControlType() == ControlType.DATETIMEPICKER) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new SimpleRTCExpression(RealTimeClock.getDefault()));
                }
                RTCExpressionControl expressionControl = new RTCExpressionControl((SimpleRTCExpression) viewModel.getParameterValue(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else {
                throw new IllegalStateException("Found unknown control type " + p);
            }
            GridPane.setRowIndex(control, i+1);
            GridPane.setColumnIndex(control, 1);
            GridPane.setHalignment(control, HPos.LEFT);
            GridPane.setFillWidth(control, false);

            getChildren().addAll(name, control);
            getRowConstraints().add(rowConstraints);
        }
    }
}

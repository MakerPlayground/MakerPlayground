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

package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.ConditionalExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SliderNumberWithUnitExpressionControl;
import io.makerplayground.ui.canvas.node.expression.valuelinking.SpinnerNumberWithUnitExpressionControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
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

import java.util.EnumSet;
import java.util.List;

/**
 * Created by tanyagorn on 6/20/2017.
 */
public class ConditionDevicePropertyWindow extends PopOver {
    private final SceneDeviceIconViewModel viewModel;

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label conditionLabel;
    private ComboBox<Condition> conditionComboBox;

    public ConditionDevicePropertyWindow(SceneDeviceIconViewModel viewModel) {
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
        conditionLabel = new Label("Condition");
        GridPane.setRowIndex(conditionLabel, 0);
        GridPane.setColumnIndex(conditionLabel, 0);

        conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.getGenericDevice().getCondition()));
        conditionComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Condition> call(ListView<Condition> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Condition item, boolean empty) {
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
        conditionComboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(Condition item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getName());
                }
            }
        });
        conditionComboBox.getSelectionModel().select(viewModel.getCondition());
        conditionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setCondition(newValue);
            redrawProperty();
        });
        GridPane.setRowIndex(conditionComboBox, 0);
        GridPane.setColumnIndex(conditionComboBox, 1);

        propertyPane.setHgap(10);
        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(conditionLabel, conditionComboBox);

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
        propertyPane.getChildren().retainAll(conditionLabel, conditionComboBox);

        List<Parameter> params = viewModel.getCondition().getParameter();
        for (int i=0; i<params.size(); i++) {
            Parameter p = params.get(i);

            Label name = new Label(p.getName());
            name.setMinHeight(25);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
            GridPane.setRowIndex(name, i+1);
            GridPane.setColumnIndex(name, 0);
            GridPane.setValignment(name, VPos.TOP);

            Node control = null;
            if (p.getControlType() == ControlType.SLIDER) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                SliderNumberWithUnitExpressionControl expressionControl = new SliderNumberWithUnitExpressionControl(
                        p,
                        viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.TEXTBOX) {
                StringChipField stringChipField = new StringChipField((ComplexStringExpression) viewModel.getParameterValue(p)
                        , viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)));
                stringChipField.expressionProperty().addListener((observableValue, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = stringChipField;
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
                        viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.DATETIMEPICKER) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new SimpleRTCExpression(RealTimeClock.getDefault()));
                }
                RTCExpressionControl expressionControl = new RTCExpressionControl((SimpleRTCExpression) viewModel.getParameterValue(p));
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.IMAGE_SELECTOR) {
                ComboBox<ProjectValue> comboBox = new ComboBox<>(FXCollections.observableArrayList(viewModel.getProjectValue(EnumSet.of(DataType.IMAGE))));
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
                comboBox.valueProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, new ImageExpression(newValue)));
                ProjectValue projectValue = ((ImageExpression) viewModel.getParameterValue(p)).getProjectValue();
                if (projectValue != null) {
                    comboBox.getSelectionModel().select(projectValue);
                }
                control = comboBox;
            } else {
                throw new IllegalStateException("Found unknown control type " + p);
            }
            GridPane.setRowIndex(control, i+1);
            GridPane.setColumnIndex(control, 1);
            GridPane.setHalignment(control, HPos.LEFT);
            GridPane.setFillWidth(control, false);
            propertyPane.getChildren().addAll(name, control);
        }

        if (viewModel.getCondition().getName().equals("Compare")) {    // TODO: compare with condition name may be dangerous
            List<Value> values = viewModel.getValue();
            for (int i=0; i<values.size(); i++) {
                Value value = values.get(i);
                createExpressionControl(i, value);
            }
        }
    }

    private void createExpressionControl(int i, Value value) {
        Expression expression = viewModel.getExpression(value);

        CheckBox enableCheckbox = new CheckBox(value.getName());
        enableCheckbox.setSelected(viewModel.isExpressionEnable(value));
        enableCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> viewModel.setExpressionEnable(value, newValue));
        GridPane.setValignment(enableCheckbox, VPos.TOP);
        GridPane.setRowIndex(enableCheckbox, i+1);
        GridPane.setColumnIndex(enableCheckbox, 0);

        ConditionalExpressionControl expressionControl = new ConditionalExpressionControl(viewModel.getProjectDevice()
                , value, viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)), expression/*, viewModel.isExpressionEnable(value)*/);
        expressionControl.disableProperty().bind(enableCheckbox.selectedProperty().not());
        expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setExpression(value, newValue));
        GridPane.setRowIndex(expressionControl, i+1);
        GridPane.setColumnIndex(expressionControl, 1);

        propertyPane.getChildren().addAll(enableCheckbox, expressionControl);
    }
}

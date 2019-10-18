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

import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.CategoricalConstraint;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.RTCExpressionControl;
import io.makerplayground.ui.canvas.node.expression.RecordExpressionControl;
import io.makerplayground.ui.canvas.node.expression.StringExpressionControl;
import io.makerplayground.ui.canvas.node.expression.custom.MultiFunctionNumericControl;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
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
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
        customName.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(customName, Priority.ALWAYS);

        ImageView interactiveStartImageView = new ImageView(new Image(getClass().getResourceAsStream("/css/interactive-start.png")));
        interactiveStartImageView.setFitWidth(20);
        interactiveStartImageView.setFitHeight(20);
        Tooltip sendActionButtonTooltip = new Tooltip();
        sendActionButtonTooltip.setShowDelay(Duration.millis(250));
        sendActionButtonTooltip.setText("Test on real device");
        Button sendActionButton = new Button();
        sendActionButton.setTooltip(sendActionButtonTooltip);
        sendActionButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0px;");
        sendActionButton.setGraphic(interactiveStartImageView);
        sendActionButton.setDisable(!viewModel.getProject().getInteractiveModel().isInitialized());
        sendActionButton.setOnAction(event -> viewModel.getProject().getInteractiveModel().sendCommand(viewModel.getUserSetting()));

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName, sendActionButton);
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
        actionComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            viewModel.setAction(newValue);
            redrawProperty();
        });
        GridPane.setRowIndex(actionComboBox, 0);
        GridPane.setColumnIndex(actionComboBox, 1);

        propertyPane.setHgap(10);
        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(actionLabel, actionComboBox);

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
            GridPane.setRowIndex(name, i + 1);
            GridPane.setColumnIndex(name, 0);
            GridPane.setValignment(name, VPos.TOP);

            Node control = null;
            if (p.getControlType() == ControlType.SLIDER) {
                // TODO: this can't ever be null isn't it?
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                MultiFunctionNumericControl expressionControl = new MultiFunctionNumericControl(
                        p,
                        viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        CustomNumberExpression.of(viewModel.getParameterValue(p))
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
                // TODO: this can't ever be null isn't it?
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new NumberWithUnitExpression((NumberWithUnit) p.getDefaultValue()));
                }
                MultiFunctionNumericControl expressionControl = new MultiFunctionNumericControl(
                        p,
                        viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER)),
                        CustomNumberExpression.of(viewModel.getParameterValue(p))
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
            } else if (p.getControlType() == ControlType.TEXTBOX_WITH_TEXT_SELECTOR) {
                StringExpressionControl expressionControl = new StringExpressionControl(
                        p,
                        viewModel.getProjectValue(Set.of(DataType.STRING)),
                        viewModel.getParameterValue(p)
                );
                expressionControl.expressionProperty().addListener((observable, oldValue, newValue) -> viewModel.setParameterValue(p, newValue));
                control = expressionControl;
            } else if (p.getControlType() == ControlType.RECORD) {
                if (viewModel.getParameterValue(p) == null) {
                    viewModel.setParameterValue(p, new RecordExpression(new Record()));
                }
                // a fake parameter to make value linking works as expect
                RecordExpressionControl expressionControl = new RecordExpressionControl(viewModel.getProjectValue(EnumSet.of(DataType.DOUBLE, DataType.INTEGER))
                        , (RecordExpression) viewModel.getParameterValue(p));
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

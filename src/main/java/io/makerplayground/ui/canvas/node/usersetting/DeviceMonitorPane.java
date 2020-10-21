package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.IntegerCategoricalConstraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.InteractiveModel;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.expression.*;
import io.makerplayground.ui.canvas.node.expression.custom.StringChipField;
import io.makerplayground.util.PathUtility;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;

public class DeviceMonitorPane extends VBox {

    public DeviceMonitorPane(InteractiveModel interactiveModel, ProjectDevice projectDevice) {
        // Create title layout
        Image img = new Image(PathUtility.getGenericDeviceIconAsStream(projectDevice.getGenericDevice()));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label(projectDevice.getName());
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // content
        GridPane propertyPane = new GridPane();
        int currentRow = 0;
        for (Condition condition : projectDevice.getGenericDevice().getCondition()) {
            if (condition.getName().equals("Compare")) {
                continue;
            }

            Label nameLabel = new Label(condition.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            Label valueLabel = new Label();

            Optional<ReadOnlyBooleanProperty> conditionProperty = interactiveModel.getConditionProperty(projectDevice, condition);
            if (conditionProperty.isPresent()) {
                BooleanBinding deviceValidBinding = Bindings.createBooleanBinding(() -> interactiveModel.isDeviceValid(projectDevice), conditionProperty.get());
                valueLabel.textProperty().bind(Bindings.when(deviceValidBinding)
                        .then(conditionProperty.get().asString())
                        .otherwise("unavailable"));
                Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime value");
                tooltip.setShowDelay(Duration.millis(250));
                valueLabel.tooltipProperty().bind(Bindings.when(deviceValidBinding).then((Tooltip) null).otherwise(tooltip));
            } else {
                valueLabel.setText("unavailable");
            }
            GridPane.setRowIndex(valueLabel, currentRow);
            GridPane.setColumnIndex(valueLabel, 1);

            propertyPane.getChildren().addAll(nameLabel, valueLabel);
            currentRow++;

            List<Parameter> params = condition.getParameter();
            for (int i=0; i<params.size(); i++) {
                Parameter p = params.get(i);

                Label name = new Label(p.getName());
                name.setMinHeight(25);  // TODO: find better way to center the label to the height of 1 row control when the control spans to multiple rows
                GridPane.setRowIndex(name, currentRow);
                GridPane.setColumnIndex(name, 0);
                GridPane.setValignment(name, VPos.TOP);

                Node control = null;
                if (p.getControlType() == ControlType.DROPDOWN && p.getDataType() == DataType.INTEGER_ENUM) {
                    ObservableList<Integer> list = FXCollections.observableArrayList(((IntegerCategoricalConstraint) p.getConstraint()).getCategories());
                    ComboBox<Integer> comboBox = new ComboBox<>(list);
                    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        interactiveModel.setAndSendConditionParameterCommand(projectDevice, condition, p, new SimpleIntegerExpression(newValue));
                    });
                    comboBox.getSelectionModel().select(list.get(0));
                    control = comboBox;
                } else if (p.getControlType() == ControlType.DROPDOWN && p.getDataType() == DataType.STRING_INT_ENUM) {
                    Map<String, Integer> map = ((StringIntegerCategoricalConstraint) p.getConstraint()).getMap();
                    ObservableList<String> list = FXCollections.observableArrayList(map.keySet());
                    ComboBox<String> comboBox = new ComboBox<>(list);
                    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                        interactiveModel.setAndSendConditionParameterCommand(projectDevice, condition, p, new SimpleIntegerExpression(map.get(newValue)));
                    });
                    comboBox.getSelectionModel().select(list.get(0));
                    control = comboBox;
                } else if (p.getControlType() == ControlType.TEXTBOX && p.getDataType() == DataType.STRING) {
                    StringChipField chipField = new StringChipField(new ComplexStringExpression(""), FXCollections.observableList(interactiveModel.getProjectValues()));
                    chipField.expressionProperty().addListener((observable, oldValue, newValue) -> {
                        interactiveModel.setAndSendConditionParameterCommand(projectDevice, condition, p, newValue);
                    });
                    control = chipField;
                } else {
                    throw new IllegalStateException("Found unknown control type " + p);
                }
                GridPane.setRowIndex(control, currentRow);
                GridPane.setColumnIndex(control, 1);
                GridPane.setHalignment(control, HPos.LEFT);
                GridPane.setFillWidth(control, false);

                propertyPane.getChildren().addAll(name, control);
                currentRow++;
            }
        }
        for (Value value : projectDevice.getGenericDevice().getValue()) {
            Label nameLabel = new Label(value.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            Label valueLabel = new Label();
            if (List.of(DataType.INTEGER, DataType.DOUBLE).contains(value.getType())) {
                Unit unit = ((NumericConstraint) value.getConstraint()).getUnit();
                Optional<ReadOnlyStringProperty> valueProperty = interactiveModel.getValueProperty(projectDevice, value);
                if (valueProperty.isPresent()) {
                    BooleanBinding deviceValidBinding = Bindings.createBooleanBinding(() -> interactiveModel.isDeviceValid(projectDevice), valueProperty.get());
                    valueLabel.textProperty().bind(Bindings.when(deviceValidBinding)
                            .then(valueProperty.get().concat(" " + unit))
                            .otherwise("unavailable"));
                    Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime value");
                    tooltip.setShowDelay(Duration.millis(250));
                    valueLabel.tooltipProperty().bind(Bindings.when(deviceValidBinding).then((Tooltip) null).otherwise(tooltip));
                } else {
                    valueLabel.setText("unavailable");
                }
            }
            else {
                Optional<ReadOnlyStringProperty> valueProperty = interactiveModel.getValueProperty(projectDevice, value);
                if (valueProperty.isPresent()) {
                    BooleanBinding deviceValidBinding = Bindings.createBooleanBinding(() -> interactiveModel.isDeviceValid(projectDevice), valueProperty.get());
                    valueLabel.textProperty().bind(Bindings.when(deviceValidBinding)
                            .then(valueProperty.get())
                            .otherwise("unavailable"));
                    Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime value");
                    tooltip.setShowDelay(Duration.millis(250));
                    valueLabel.tooltipProperty().bind(Bindings.when(deviceValidBinding).then((Tooltip) null).otherwise(tooltip));
                } else {
                    valueLabel.setText("unavailable");
                }
            }
            GridPane.setRowIndex(valueLabel, currentRow);
            GridPane.setColumnIndex(valueLabel, 1);

            propertyPane.getChildren().addAll(nameLabel, valueLabel);
            currentRow++;
        }
        propertyPane.setHgap(10);
        propertyPane.setVgap(5);

        // arrange title and property sheet
        getStylesheets().add(this.getClass().getResource("/css/canvas/node/usersetting/DevicePropertyWindow.css").toExternalForm());
        getChildren().addAll(titleHBox, propertyPane);
        setSpacing(5.0);
    }
}
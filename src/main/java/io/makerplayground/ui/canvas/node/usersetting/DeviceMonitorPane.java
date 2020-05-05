package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.generic.ControlType;
import io.makerplayground.device.shared.*;
import io.makerplayground.device.shared.constraint.IntegerCategoricalConstraint;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.device.shared.constraint.StringIntegerCategoricalConstraint;
import io.makerplayground.project.InteractiveModel;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.expression.SimpleIntegerExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DeviceMonitorPane extends VBox {

    public DeviceMonitorPane(InteractiveModel interactiveModel, ProjectDevice projectDevice, ActualDevice actualDevice) {
        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons-3/" + projectDevice.getGenericDevice().getName() + ".png"));
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
        List<Condition> conditions = new ArrayList<>(projectDevice.getGenericDevice().getCondition());
        if (Objects.nonNull(actualDevice)) {
            conditions.retainAll(actualDevice.getCompatibilityMap().get(projectDevice.getGenericDevice()).getDeviceCondition().keySet());
        }
        for (Condition condition : conditions) {
            if (condition.getName().equals("Compare")) {
                continue;
            }

            Label nameLabel = new Label(condition.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            Label valueLabel = new Label();
            interactiveModel.getConditionProperty(projectDevice, condition)
                    .ifPresentOrElse(valueProperty -> valueLabel.textProperty().bind(valueProperty.asString()), () -> {
                        valueLabel.setText("unavailable");
                        Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime status");
                        tooltip.setShowDelay(Duration.millis(250));
                        valueLabel.setTooltip(tooltip);
                    });
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
        List<Value> values = new ArrayList<>(projectDevice.getGenericDevice().getValue());
        if (Objects.nonNull(actualDevice)) {
            values.retainAll(actualDevice.getCompatibilityMap().get(projectDevice.getGenericDevice()).getDeviceValue().keySet());
        }
        for (Value value : values) {
            Label nameLabel = new Label(value.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            Unit unit = ((NumericConstraint) value.getConstraint()).getUnit();
            Label valueLabel = new Label();
            interactiveModel.getValueProperty(projectDevice, value)
                    .ifPresentOrElse(valueProperty -> {
                        if (unit == Unit.NOT_SPECIFIED) {
                            valueLabel.textProperty().bind(valueProperty.asString());
                        } else {
                            valueLabel.textProperty().bind(valueProperty.asString().concat(" " + unit));
                        }
                    } , () -> {
                        valueLabel.setText("unavailable");
                        Tooltip tooltip = new Tooltip("Restart the interactive mode to see realtime value");
                        tooltip.setShowDelay(Duration.millis(250));
                        valueLabel.setTooltip(tooltip);
                    });
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
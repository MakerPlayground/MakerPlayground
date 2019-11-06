package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.shared.Condition;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.InteractiveModel;
import io.makerplayground.project.ProjectDevice;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DeviceMonitorPane extends VBox {

    public DeviceMonitorPane(InteractiveModel interactiveModel, ProjectDevice projectDevice) {
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
        for (Condition condition : projectDevice.getGenericDevice().getCondition()) {
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
        }
        for (Value value : projectDevice.getGenericDevice().getValue()) {
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
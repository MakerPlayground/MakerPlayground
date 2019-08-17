package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.device.shared.constraint.NumericConstraint;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.explorer.InteractiveModel;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        for (Action condition : projectDevice.getGenericDevice().getCondition()) {
            if (condition.getName().equals("Compare")) {
                continue;
            }

            Label nameLabel = new Label(condition.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            Label valueLabel = new Label();
            valueLabel.textProperty().bind(interactiveModel.getConditionProperty(projectDevice, condition).asString());
            GridPane.setRowIndex(valueLabel, currentRow);
            GridPane.setColumnIndex(valueLabel, 1);

            propertyPane.getChildren().addAll(nameLabel, valueLabel);
            currentRow++;
        }
        for (Value value : projectDevice.getGenericDevice().getValue()) {
            Label nameLabel = new Label(value.getName());
            GridPane.setRowIndex(nameLabel, currentRow);
            GridPane.setColumnIndex(nameLabel, 0);

            ReadOnlyDoubleProperty valueProperty = interactiveModel.getValueProperty(projectDevice, value);
            Unit unit = ((NumericConstraint) value.getConstraint()).getUnit();
            Label valueLabel = new Label();
            valueLabel.textProperty().bind(new StringBinding() {
                {
                    super.bind(valueProperty);
                }

                @Override
                protected String computeValue() {
                    if (unit == Unit.NOT_SPECIFIED) {
                        return String.valueOf(valueProperty.get());
                    } else {
                        return String.valueOf(valueProperty.get()) + unit;
                    }
                }
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

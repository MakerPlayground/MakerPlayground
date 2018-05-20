package io.makerplayground.ui.canvas;

import io.makerplayground.device.*;
import io.makerplayground.helper.*;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.TimeCondition;
import io.makerplayground.project.TimeConditionType;
import io.makerplayground.project.expression.Expression;
import io.makerplayground.project.expression.SimpleExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class TimeConditionPropertyWindow extends PopOver {
    private final TimeCondition timeCondition;

    // Create a grid pane as a main layout for this property sheet
    private final GridPane propertyPane = new GridPane();

    private Label conditionLabel;
    private ComboBox<TimeConditionType> conditionComboBox;

    public TimeConditionPropertyWindow(TimeCondition timeCondition) {
        this.timeCondition = timeCondition;
        initView();
    }

    private void initView() {
        // Create title layout
        Image img = new Image(getClass().getResourceAsStream("/icons/colorIcons/Time.png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setPreserveRatio(true);

        Label customName = new Label("Time");
        customName.setMaxWidth(Region.USE_COMPUTED_SIZE);

        HBox titleHBox = new HBox();
        titleHBox.getChildren().addAll(imageView, customName);
        titleHBox.setAlignment(Pos.CENTER_LEFT);
        titleHBox.setSpacing(10);

        // Create ComboBox for user to select a condition
        conditionLabel = new Label("Condition");
        GridPane.setRowIndex(conditionLabel, 0);
        GridPane.setColumnIndex(conditionLabel, 0);

        conditionComboBox = new ComboBox<>(FXCollections.observableList(List.of(TimeConditionType.values())));
        conditionComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TimeConditionType> call(ListView<TimeConditionType> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(TimeConditionType item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getConditionName());
                        }
                    }
                };
            }
        });
        conditionComboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(TimeConditionType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getConditionName());
                }
            }
        });
        conditionComboBox.getSelectionModel().select(timeCondition.getType());
        // bind action selected to the view model
        timeCondition.typeProperty().bind(conditionComboBox.getSelectionModel().selectedItemProperty());
        GridPane.setRowIndex(conditionComboBox, 0);
        GridPane.setColumnIndex(conditionComboBox, 1);

        propertyPane.setVgap(5);
        propertyPane.getChildren().addAll(conditionLabel, conditionComboBox);

        // add listener to update property sheet when the condition selected has changed
        timeCondition.typeProperty().addListener((observable, oldValue, newValue) -> redrawProperty());
        redrawProperty();

        // arrange title and property sheet
        VBox mainPane = new VBox();
        mainPane.getStylesheets().add(this.getClass().getResource("/css/DevicePropertyWindow.css").toExternalForm());
        mainPane.getChildren().addAll(titleHBox, propertyPane);
        mainPane.setSpacing(5.0);
        mainPane.setPadding(new Insets(20, 20, 20, 20));

        setDetachable(false);
        setContentNode(mainPane);
    }

    private void redrawProperty() {
        propertyPane.getChildren().retainAll(conditionLabel, conditionComboBox);

        Label name = new Label("Duration");
        GridPane.setRowIndex(name, 1);
        GridPane.setColumnIndex(name, 0);

        Unit unit = null;
        if (timeCondition.getUnit() == TimeUnit.Second) {
            unit = Unit.SECOND;
        } else if (timeCondition.getUnit() == TimeUnit.MilliSecond) {
            unit = Unit.MILLISECOND;
        }

        SpinnerWithUnit spinner = new SpinnerWithUnit(0, 1000000000, timeCondition.getDuration()    // TODO: find valid max value
                , unit, FXCollections.observableArrayList(List.of(Unit.SECOND, Unit.MILLISECOND)));
        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            timeCondition.setDuration(newValue.getValue());
            if (newValue.getUnit() == Unit.SECOND) {
                timeCondition.setUnit(TimeUnit.Second);
            } else if (newValue.getUnit() == Unit.MILLISECOND) {
                timeCondition.setUnit(TimeUnit.MilliSecond);
            }
        });

        GridPane.setRowIndex(spinner, 1);
        GridPane.setColumnIndex(spinner, 1);
        propertyPane.getChildren().addAll(name, spinner);
    }
}

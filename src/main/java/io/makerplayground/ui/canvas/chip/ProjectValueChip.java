package io.makerplayground.ui.canvas.chip;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ProjectValueChip extends Chip<ProjectValue> {

    private static final Color BACKGROUND_COLOR = Color.DARKRED;
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public ProjectValueChip(ProjectValue initialValue, ObservableList<ProjectValue> projectValues) {
        super(initialValue, Term.Type.VALUE, projectValues);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
//        background.setWidth(80);
//        background.setHeight(30);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().setValue(BACKGROUND_COLOR);

        ComboBox<ProjectValue> comboBox = new ComboBox<>(getChoices());
        if (getValue() != null) {
            comboBox.setValue(getValue());
        }
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle("-fx-font-size: 10;");
                    setGraphic(text);
                    setPrefHeight(30);
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
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle("-fx-font-size: 10;");
                    setGraphic(text);
                }
            }
        });
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));

        getChildren().addAll(background, comboBox);
        StackPane.setMargin(comboBox, new Insets(5, 10, 5, 10));
        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        }));
        setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
    }
}

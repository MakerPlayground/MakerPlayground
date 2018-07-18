package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;

public class ProjectValueChip extends Chip<ProjectValue> {

//    private static final Color BACKGROUND_COLOR = Color.DARKRED;
//    private static final Color BACKGROUND_COLOR = Color.valueOf("E2E2E2");
    private static final Color BACKGROUND_COLOR = Color.valueOf("081e42");
    @FXML
    private ComboBox comboBox;
    @FXML
    private Rectangle background;
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public ProjectValueChip(ProjectValue initialValue, ObservableList<ProjectValue> projectValues) {
        super(initialValue, Term.Type.VALUE, projectValues);
    }

    @Override
    protected void initView() {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ProjectValueChip.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);

            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //Rectangle background = new Rectangle();
        //background.setArcWidth(20);
       // background.setArcHeight(20);
       // background.fillProperty().setValue(BACKGROUND_COLOR);

        ComboBox<ProjectValue> comboBox = new ComboBox<>(getChoices());
        if (getValue() != null) {
            comboBox.setValue(getValue());
        }
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setId("text1");
                    /*text.setStyle("-fx-font-size: 10;" +
                            "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;");*/
                    setGraphic(text);
                    setPrefHeight(30);
                }
            }
        });
        comboBox.setButtonCell(new ListCell<>(){
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setId("text1");
                    /*text.setStyle("-fx-font-size: 10;" +
                            "-fx-background-color: transparent;" +
                            "-fx-border-color: transparent;");*/
                    setGraphic(text);
                }
            }
        });
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));
        /*comboBox.setStyle("-fx-background-color: transparent;" +
                "-fx-border-color: transparent;");*/

        //getChildren().addAll(background, comboBox);
       // StackPane.setMargin(comboBox, new Insets(5, 10, 5, 10));
        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            background.setWidth(newValue.getWidth());
            background.setHeight(newValue.getHeight());
        }));
        setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
    }

    @Override
    public ValueTerm getTerm() {
        return new ValueTerm(getValue());
    }
}

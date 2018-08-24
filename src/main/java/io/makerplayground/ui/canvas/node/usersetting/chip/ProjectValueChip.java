package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class ProjectValueChip extends Chip<ProjectValue> {

    @FXML private ComboBox<ProjectValue> comboBox;
    @FXML private Rectangle background;

    private static final String TEXT_CSS = "-fx-font-size: 10; -fx-fill: white; -fx-font-weight: bold; -fx-text-alignment: center;";
    private static final String COMBOBOX_LISTVIEW_TEXT_CSS = "-fx-font-size: 10; -fx-text-alignment: center;";

    public ProjectValueChip(ProjectValue initialValue, List<ProjectValue> projectValues) {
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

        comboBox.getItems().addAll(FXCollections.observableArrayList(getChoices()));
        if (getValue() != null) {
            comboBox.setValue(getValue());
        }
        comboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setGraphicTextGap(0);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle(COMBOBOX_LISTVIEW_TEXT_CSS);
                    setGraphic(text);
                    setPrefHeight(30);
                }
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                setGraphicTextGap(0);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                if (empty) {
                    setGraphic(null);
                } else {
                    Text text = new Text(item.getDevice().getName() + "'s\n" + item.getValue().getName());
                    text.setStyle(TEXT_CSS);
                    setGraphic(text);
                }
            }
        });
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));

        // update width of the background based on the combobox width
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> background.setWidth(newValue.getWidth()));
    }

    @Override
    public ValueTerm getTerm() {
        return new ValueTerm(getValue());
    }

    @Override
    protected void updateChipStyle(boolean selected) {
        if (!selected) {
            background.setFill(Color.web("#C5534F"));
        } else {
            background.setFill(Color.web("#C5534F").darker());
        }
    }
}

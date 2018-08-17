package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.StringTerm;
import io.makerplayground.project.term.Term;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;

public class StringChip extends Chip<String> {
    //private static final Color BACKGROUND_COLOR = Color.DARKORANGE;
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.ORANGERED;

    @FXML private Rectangle background;
    @FXML private TextField input;

    public StringChip() {
        super("", Term.Type.STRING);
    }

    public StringChip(String initialValue) {
        super(initialValue, Term.Type.STRING);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/StringChip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Rectangle background = new Rectangle();
        //background.setWidth(40);
        //background.setHeight(20);
        //background.setArcWidth(20);
        //background.setArcHeight(20);
        //background.fillProperty().setValue(BACKGROUND_COLOR);
//        background.fillProperty().bind(Bindings.when(selectedProperty())
//                .then(BACKGROUND_COLOR_SELECTED).otherwise(BACKGROUND_COLOR));

        //TextField input = new TextField();
        //input.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        //input.setAlignment(Pos.BASELINE_CENTER);
        //input.setPrefSize(40, 20);
        //input.setMaxSize(40, 20);
        input.textProperty().addListener((observable, oldValue, newValue) -> setValue(newValue));

        //getChildren().addAll(background, input);
    }

    @Override
    public StringTerm getTerm() {
        return new StringTerm(getValue());
    }
}

package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.OperatorTerm;
import io.makerplayground.project.term.Term;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;

public class OperatorChip extends Chip<OperatorTerm.OP> {
//    private static final Color BACKGROUND_COLOR = Color.DARKGREEN;
    private static final Color BACKGROUND_COLOR = Color.valueOf("072454");
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.GREEN;

    public OperatorChip(OperatorTerm.OP initialValue) {
        super(initialValue, Term.Type.OPERATOR);
    }
    @FXML private Rectangle background;
    @FXML private TextField input;
    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/Operator.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Rectangle background = new Rectangle();
        //background.setWidth(25);
       // background.setHeight(20);
       // background.setArcWidth(20);
        //background.setArcHeight(20);
       // background.fillProperty().setValue(BACKGROUND_COLOR);

        //Text text = new Text(getValue().toString());
        //text.setFill(Color.WHITE);
        //getChildren().addAll(background, text);
        //setMaxSize(25,20);
    }

    @Override
    public OperatorTerm getTerm() {
        return new OperatorTerm(getValue());
    }

    @Override
    public OperatorTerm.OP getValue() {
        return super.getValue();
    }

    @Override
    public ObjectProperty<OperatorTerm.OP> valueProperty() {
        throw new UnsupportedOperationException("OperatorChip should'h not be edited");
    }

    @Override
    public void setValue(OperatorTerm.OP value) {
        throw new UnsupportedOperationException("OperatorChip should'h not be edited");
    }
}

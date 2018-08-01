package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Term;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class LabelChip extends Chip<String> {

//    private static final Color BACKGROUND_COLOR = Color.DARKRED;
    private static final Color BACKGROUND_COLOR = Color.valueOf("081e42");
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public LabelChip(String msg) {
        this(msg, Term.Type.STRING);
    }

    private LabelChip(String msg, Term.Type type) {
        super(msg, type);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setWidth(80);
        background.setHeight(30);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().setValue(BACKGROUND_COLOR);

        Text text = new Text(getValue());
        text.setFill(Color.WHITE);
        getChildren().addAll(background, text);
    }

    @Override
    public Term getTerm() {
        throw new IllegalStateException("Unsupported Operation");
    }
}

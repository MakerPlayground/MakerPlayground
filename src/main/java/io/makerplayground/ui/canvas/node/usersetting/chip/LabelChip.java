package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.term.Term;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;

public class LabelChip extends Chip<String> {

//    private static final Color BACKGROUND_COLOR = Color.DARKRED;
 //   private static final Color BACKGROUND_COLOR = Color.valueOf("081e42");
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;
    @FXML private Rectangle background;
    @FXML private Text text;
    public LabelChip(String msg) {
        this(msg, Term.Type.STRING);
    }

    private LabelChip(String msg, Term.Type type) {
        super(msg, type);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/Labelchip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }


       // background.setWidth(80);
       // background.setHeight(30);
        //background.setArcWidth(20);
       // background.setArcHeight(20);
        // background.fillProperty().setValue(BACKGROUND_COLOR);

       // text.setFill(Color.WHITE);
        //getChildren().addAll(background, text);
    }

    @Override
    public Term getTerm() {
        throw new IllegalStateException("Unsupported Operation");
    }
}

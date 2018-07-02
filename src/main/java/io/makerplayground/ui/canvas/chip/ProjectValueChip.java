package io.makerplayground.ui.canvas.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.term.Term;
import io.makerplayground.project.term.ValueTerm;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ProjectValueChip extends Chip<ProjectValue> {

    private static final Color BACKGROUND_COLOR = Color.DARKRED;
//    private static final Color BACKGROUND_COLOR_SELECTED = Color.RED;

    public ProjectValueChip(ProjectValue initialValue) {
        this(initialValue, Term.Type.VALUE);
    }

    public ProjectValueChip(ProjectValue initialValue, Term.Type type) {
        super(initialValue, type);
    }

    @Override
    protected void initView() {
        Rectangle background = new Rectangle();
        background.setWidth(80);
        background.setHeight(30);
        background.setArcWidth(20);
        background.setArcHeight(20);
        background.fillProperty().setValue(BACKGROUND_COLOR);

        TextField input = new TextField();
        input.setText(String.valueOf("value"));
        input.setAlignment(Pos.BASELINE_CENTER);
        input.setPrefSize(30, 20);
        input.setMaxSize(30, 20);
//        input.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue) {
//                try {
//                    double value = Double.parseDouble(input.getText());
//                    setValue(new NumberWithUnit(value, getValue().getUnit()));  // TODO: shouldn't hardcoded unit
//                } catch (NumberFormatException e) {
//                    input.setText(String.valueOf(getValue()));
//                }
//            }
//        });

        getChildren().addAll(background, input);
        setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
    }
}

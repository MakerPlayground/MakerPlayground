package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.term.NumberWithUnitTerm;
import io.makerplayground.project.term.Term;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.text.DecimalFormat;

import java.io.IOException;

public class NumberWithUnitChip extends Chip<NumberWithUnit> {

    @FXML private Rectangle background;
    @FXML private TextField input;

    private static final DecimalFormat df = new DecimalFormat("#.##");

    public NumberWithUnitChip(NumberWithUnit initialValue) {
        super(initialValue, Term.Type.NUMBER);
    }

    @Override
    protected void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/NumberWithUnitChip.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        input.setText(df.format(getValue().getValue())); // TODO: display unit
        input.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    double value = Double.parseDouble(input.getText());
                    setValue(new NumberWithUnit(value, getValue().getUnit()));  // TODO: shouldn't hardcoded unit
                } catch (NumberFormatException e) {
                    input.setText(df.format(getValue().getValue()) + getValue().getUnit());
                }
            }
        });

        // update width of the background based on the combobox width
        layoutBoundsProperty().addListener((observable, oldValue, newValue) -> background.setWidth(newValue.getWidth()));
    }

    @Override
    public Term getTerm() {
        return new NumberWithUnitTerm(getValue());
    }
}

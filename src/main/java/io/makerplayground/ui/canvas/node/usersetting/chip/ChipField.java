package io.makerplayground.ui.canvas.node.usersetting.chip;

import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.*;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

public class ChipField extends HBox {
    @FXML
    private HBox mainPane;
    @FXML
    private Button backspaceBtn;

    private final List<ProjectValue> projectValues;

    private final ObjectProperty<CustomNumberExpression> expressionProperty;

    public ChipField(ObjectProperty<CustomNumberExpression> expressionProperty, List<ProjectValue> projectValues) {
        this.projectValues = projectValues;
        this.expressionProperty = expressionProperty;
        initView();
        initEvent();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/usersetting/chip/ChipField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize chip based on expression
        List<Term> listTerm = expressionProperty.get().getTerms();
        for (int i=0; i<listTerm.size(); i++) {
            addChipUI(listTerm.get(i), i);
        }

        showHighlight(!expressionProperty.get().isValid());
    }

    private void initEvent() {
        // add/remove chip when expression changed
        expressionProperty.get().getTerms().addListener((ListChangeListener<? super Term>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    throw new UnsupportedOperationException();
                } else if (c.wasUpdated()) {
                    throw new UnsupportedOperationException();
                } else {
                    for (Term ignored : c.getRemoved()) {
                        removeChipUI(c.getFrom());
                    }
                    for (Term addedItem : c.getAddedSubList()) {
                        addChipUI(addedItem, c.getFrom());
                    }
                }
            }
            showHighlight(!expressionProperty.get().isValid());
        });

        backspaceBtn.setOnMouseReleased(event -> {
            if (expressionProperty.get().getTerms().size() > 0) {
                expressionProperty.get().getTerms().remove(expressionProperty.get().getTerms().size() - 1);
            }
        });
    }

    public void addChip(Term t) {
        this.expressionProperty.get().getTerms().add(t);
    }

    private void addChipUI(Term t, int index) {
        Chip chip;
        if (t instanceof NumberWithUnitTerm) {
            NumberWithUnitChip numChip = new NumberWithUnitChip(((NumberWithUnitTerm) t).getValue());
            numChip.valueProperty().addListener((ob, o, n) -> expressionProperty.get().getTerms().set(index, new NumberWithUnitTerm(n)));
            chip = numChip;
        } else if (t instanceof StringTerm) {
            StringChip strChip = new StringChip(((StringTerm) t).getValue());
            strChip.valueProperty().addListener((ob, o, n) -> expressionProperty.get().getTerms().set(index, new StringTerm(n)));
            chip = strChip;
        } else if (t instanceof OperatorTerm) {
            chip = new OperatorChip(((OperatorTerm) t).getValue());
        } else if (t instanceof ValueTerm) {
            ProjectValueChip pvChip = new ProjectValueChip(((ValueTerm) t).getValue(), projectValues);
            pvChip.valueProperty().addListener((ob, o, n) -> expressionProperty.get().getTerms().set(index, new ValueTerm(n)));
            chip = pvChip;
        } else {
            throw new IllegalStateException();
        }
        mainPane.getChildren().add(index, chip);
    }

    // Remove chip when underlying expression has changed
    private void removeChipUI(int index) {
        mainPane.getChildren().remove(index);
    }

    private void showHighlight(boolean b) {
        if (b) {
            mainPane.setStyle("-fx-effect: dropshadow(gaussian, #c25a5a, 10.0 , 0.5, 0.0 , 0.0);");
        } else {
            mainPane.setStyle("-fx-effect: dropshadow(gaussian, derive(black,75%), 0.0 , 0.0, 0.0 , 0.0);");
        }
    }
}

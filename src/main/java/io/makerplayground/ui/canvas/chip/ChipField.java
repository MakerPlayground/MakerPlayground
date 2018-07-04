package io.makerplayground.ui.canvas.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class ChipField extends HBox {
    @FXML
    private FlowPane mainPane;

    @FXML
    private Button backspaceBtn;

    private final ObservableList<ProjectValue> projectValues;

    private final ObjectProperty<CustomNumberExpression> expressionProperty;

    public ChipField(ObjectProperty<CustomNumberExpression> expressionProperty, ObservableList<ProjectValue> projectValues) {
        this.projectValues = projectValues;
        this.expressionProperty = expressionProperty;
        initView();
        initEvent();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChipField.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // initialize chip based on expression
        List<Term> listTerm = expressionProperty.get().getTerms();
        Platform.runLater(() -> IntStream.range(0, listTerm.size())
            .forEach(idx -> addChipUI(listTerm.get(idx), idx)));
//        setFocusTraversable(true);
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
        });

        backspaceBtn.setOnMouseReleased(this::handleBackspace);
    }

    public void addChip(Term t) {
        this.expressionProperty.get().getTerms().add(t);
    }

    private void addChipUI(Term t, int index) {
        Chip chip;
        if (t instanceof NumberWithUnitTerm) {
            chip = new NumberWithUnitChip((NumberWithUnit) t.getValue());
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                expressionProperty.get().getTerms().set(index, new NumberWithUnitTerm((NumberWithUnit) newValue));
            });
        } else if (t instanceof StringTerm) {
            chip = new StringChip((String) t.getValue());
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                expressionProperty.get().getTerms().set(index, new StringTerm((String) newValue));
            });
        } else if (t instanceof OperatorTerm) {
            chip = new OperatorChip((OperatorTerm.OP) t.getValue());
        } else if (t instanceof ValueTerm) {
            chip = new ProjectValueChip((ProjectValue) t.getValue(), projectValues);
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                expressionProperty.get().getTerms().set(index, new ValueTerm((ProjectValue) newValue));
            });
        } else {
            throw new IllegalStateException();
        }
        mainPane.getChildren().add(index, chip);
    }

    // Remove chip when underlying expression has changed
    private void removeChipUI(int index) {
        mainPane.getChildren().remove(index);
    }

    private void handleBackspace(MouseEvent mouseEvent) {
        if (expressionProperty.get().getTerms().size() > 0) {
            expressionProperty.get().getTerms().remove(expressionProperty.get().getTerms().size() - 1);
        }
    }
}

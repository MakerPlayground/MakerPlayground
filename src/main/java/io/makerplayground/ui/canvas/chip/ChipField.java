package io.makerplayground.ui.canvas.chip;

import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Operator;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.CustomNumberExpression;
import io.makerplayground.project.term.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;


import java.io.IOException;

public class ChipField extends HBox {
    @FXML
    private FlowPane mainPane;

    @FXML
    private Button backspaceBtn;

    private final ObservableList<ProjectValue> projectValues;

    private final ObjectProperty<CustomNumberExpression> expressionProperty = new SimpleObjectProperty<>(new CustomNumberExpression());

    public ChipField(CustomNumberExpression expression, ObservableList<ProjectValue> projectValues) {
        this.projectValues = projectValues;
        initView();
        initEvent();
        this.expressionProperty.get().getTerms().addAll(expression.getTerms());
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
        expressionProperty.get().getTerms().forEach((term) -> Platform.runLater(() -> addChip(term)));

        setFocusTraversable(true);
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
                        addChipUI(addedItem);
                    }
                }
            }
        });

        backspaceBtn.setOnMouseReleased(this::handleBackspace);
    }

    public void addChip(Term t) {
        this.expressionProperty.get().getTerms().add(t);
    }

    private void addChipUI(Term t) {
        Chip chip;
        if (t instanceof NumberWithUnitTerm) {
            chip = new NumberWithUnitChip((NumberWithUnit) t.getValue());
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                int index = mainPane.getChildren().indexOf(chip);
                expressionProperty.get().getTerms().set(index, new NumberWithUnitTerm((NumberWithUnit) newValue));
            });
        } else if (t instanceof StringTerm) {
            chip = new StringChip((String) t.getValue());
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                int index = mainPane.getChildren().indexOf(chip);
                expressionProperty.get().getTerms().set(index, new StringTerm((String) newValue));
            });
        } else if (t instanceof OperatorTerm) {
            chip = new OperatorChip((OperatorTerm.OP) t.getValue());
        } else if (t instanceof ValueTerm) {
            chip = new ProjectValueChip((ProjectValue) t.getValue(), projectValues);
            chip.valueProperty().addListener((observable, oldValue, newValue) -> {
                int index = mainPane.getChildren().indexOf(chip);
                expressionProperty.get().getTerms().set(index, new ValueTerm((ProjectValue) newValue));
            });
        } else {
            throw new IllegalStateException();
        }
        mainPane.getChildren().add(chip);
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

    public ObjectProperty<CustomNumberExpression> expressionProperty() {
        return expressionProperty;
    }
}

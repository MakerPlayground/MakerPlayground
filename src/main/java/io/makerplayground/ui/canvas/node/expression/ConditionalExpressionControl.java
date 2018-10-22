package io.makerplayground.ui.canvas.node.expression;

import io.makerplayground.device.shared.NumberWithUnit;
import io.makerplayground.device.shared.Unit;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.project.expression.ConditionalExpression;
import io.makerplayground.project.term.*;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConditionalExpressionControl extends VBox {

    private final ReadOnlyObjectWrapper<ConditionalExpression> expression;
    private final List<Term> projectValueTerm;
    private final List<ExpressionRow> expressionRows;

    public ConditionalExpressionControl(ConditionalExpression expression, List<ProjectValue> projectValues) {
        this.expression = new ReadOnlyObjectWrapper<>(expression);
        this.projectValueTerm = projectValues.stream().map(ValueTerm::new).collect(Collectors.toList());
        this.expressionRows = new ArrayList<>();

        Button addButton = new Button("+");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> {
            ExpressionRow expressionRow = new ExpressionRow(Operator.GREATER_THAN, NumberWithUnitTerm.ZERO);
            expressionRows.add(expressionRow);
            createExpressionRowControl(expressionRow);
        });
        getChildren().add(addButton);

        List<Term> terms = expression.getTerms();
        int i = 0;
        while (i < terms.size()) {
            Operator operator = ((OperatorTerm) terms.get(i++)).getValue();
            Term lowTerm = terms.get(i++);
            ExpressionRow expressionRow = new ExpressionRow(operator, lowTerm);
            expressionRows.add(expressionRow);
            createExpressionRowControl(expressionRow);
        }

        setAlignment(Pos.CENTER);
        setSpacing(5);
    }


    private void createExpressionRowControl(ExpressionRow expressionRow) {
        final HBox hbox = new HBox();
        hbox.setSpacing(5);

        ComboBox<Term> lowValueCombobox = new ComboBox<>(FXCollections.observableArrayList(projectValueTerm));
        if (expressionRow.getLowTerm() != null) {
            lowValueCombobox.setValue(expressionRow.getLowTerm());
        }
        lowValueCombobox.setEditable(true);
        lowValueCombobox.setConverter(termStringConverter);
        lowValueCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
            expressionRow.setLowTerm(newValue);
            invalidate();
        });

        ComboBox<Operator> operatorComboBox = new ComboBox<>(FXCollections.observableArrayList(ConditionalExpression.OPERATORS));
        if (expressionRow.getOperator() != null) {
            operatorComboBox.setValue(expressionRow.getOperator());
        }
        operatorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            expressionRow.setOperator(newValue);
            invalidate();
        });

        Button removeButton = new Button("-");
        removeButton.setOnAction(event -> {
            getChildren().remove(hbox);
            expressionRows.remove(expressionRow);
            invalidate();
        });

        hbox.getChildren().addAll(operatorComboBox, lowValueCombobox, removeButton);
        getChildren().add(getChildren().size() - 1, hbox);
    }

    private void invalidate() {
        List<Term> terms = expressionRows.stream().map(ExpressionRow::toTerm).flatMap(Collection::stream).collect(Collectors.toList());
        expression.set(new ConditionalExpression(expression.get().getProjectDevice(), expression.get().getValue(), terms));
    }

    public ConditionalExpression getExpression() {
        return expression.get();
    }

    public ReadOnlyObjectProperty<ConditionalExpression> expressionProperty() {
        return expression.getReadOnlyProperty();
    }

    private final StringConverter<Term> termStringConverter = new StringConverter<>() {
        @Override
        public String toString(Term t) {
            return t == null ? "" : t.toString();
        }

        @Override
        public Term fromString(String s) {
            try {
                double d = NumberFormat.getInstance().parse(s).doubleValue();
                return new NumberWithUnitTerm(new NumberWithUnit(d, Unit.NOT_SPECIFIED));
            } catch (ParseException|NullPointerException e) {
                Optional<Term> term = projectValueTerm.stream().filter(t -> t.toString().equals(s)).findAny();
                return term.orElse(NumberWithUnitTerm.ZERO);
            }
        }
    };

    private static class ExpressionRow {
        private Operator operator;
        private Term lowTerm;

        public ExpressionRow(Operator operator, Term lowTerm) {
            this.operator = operator;
            this.lowTerm = lowTerm;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public Term getLowTerm() {
            return lowTerm;
        }

        public void setLowTerm(Term lowTerm) {
            this.lowTerm = lowTerm;
        }

        public List<Term> toTerm() {
            return List.of(new OperatorTerm(operator), lowTerm);
        }
    }
}

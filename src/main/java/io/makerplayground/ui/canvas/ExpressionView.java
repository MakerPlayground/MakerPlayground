package io.makerplayground.ui.canvas;

import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.Arrays;
import java.util.List;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionView extends HBox {
    private final ExpressionViewModel expressionViewModel;
    private Button b;

    public ExpressionView(ExpressionViewModel expressionViewModel) {
        this.expressionViewModel = expressionViewModel;

        setSpacing(2);
        setAlignment(Pos.CENTER_LEFT);

        ComboBox<Operator> comboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.values()));
        comboBox.getSelectionModel().select(expressionViewModel.getOperator());
        expressionViewModel.operatorProperty().bind(comboBox.getSelectionModel().selectedItemProperty());

        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(expressionViewModel.getMin(), expressionViewModel.getMax(), expressionViewModel.getFirstOperandAsDouble());
        Spinner<Double> spinnerFrom = new Spinner<>(factory);
        spinnerFrom.setEditable(true);
        spinnerFrom.visibleProperty().bind(expressionViewModel.literalModeProperty());
        spinnerFrom.managedProperty().bind(spinnerFrom.visibleProperty());
        expressionViewModel.firstOperandAsDoubleProperty().bind(spinnerFrom.getValueFactory().valueProperty());
        TextFormatter<Double> formatter = new TextFormatter<>(factory.getConverter(), factory.getValue());
        spinnerFrom.getEditor().setTextFormatter(formatter);
        factory.valueProperty().bindBidirectional(formatter.valueProperty());

        ComboBox<ProjectValue> comboBoxFrom = new ComboBox<>(FXCollections.observableList(expressionViewModel.getAvailableValue()));
        comboBoxFrom.setCellFactory(new Callback<ListView<ProjectValue>, ListCell<ProjectValue>>() {
            @Override
            public ListCell<ProjectValue> call(ListView<ProjectValue> param) {
                return new ListCell<ProjectValue>() {
                    @Override
                    protected void updateItem(ProjectValue item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                        }
                    }
                };
            }
        });
        comboBoxFrom.setButtonCell(new ListCell<ProjectValue>(){
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getDevice().getName() + "'s " + item.getValue().getName());
                }
            }
        });

        comboBoxFrom.getSelectionModel().select(expressionViewModel.getFirstOperandAsValue());
        comboBoxFrom.visibleProperty().bind(expressionViewModel.literalModeProperty().not());
        comboBoxFrom.managedProperty().bind(comboBoxFrom.visibleProperty());
        expressionViewModel.firstOperandAsValueProperty().bind(comboBoxFrom.getSelectionModel().selectedItemProperty());

        ComboBox<OperandType> comboBoxType = new ComboBox<>(FXCollections.observableList(Arrays.asList(OperandType.values())));
        comboBoxType.getSelectionModel().select(expressionViewModel.getOperandType());
        expressionViewModel.operandTypeProperty().bind(comboBoxType.getSelectionModel().selectedItemProperty());

        Label l = new Label("to");
        l.visibleProperty().bind(expressionViewModel.betweenModeProperty());
        l.managedProperty().bind(l.visibleProperty());
        l.setId("toLabel");
        l.setAlignment(Pos.CENTER);

        SpinnerValueFactory<Double> factory2 = new SpinnerValueFactory.DoubleSpinnerValueFactory(expressionViewModel.getMin(), expressionViewModel.getMax(), expressionViewModel.getSecondOperandAsDouble());
        Spinner<Double> spinnerTo = new Spinner<>(factory2);
        spinnerTo.setEditable(true);
        spinnerTo.visibleProperty().bind(expressionViewModel.literalModeProperty().and(expressionViewModel.betweenModeProperty()));
        spinnerTo.managedProperty().bind(spinnerTo.visibleProperty());
        expressionViewModel.secondOperandAsDoubleProperty().bind(spinnerTo.getValueFactory().valueProperty());
        TextFormatter<Double> formatter2 = new TextFormatter<>(factory2.getConverter(), factory2.getValue());
        spinnerTo.getEditor().setTextFormatter(formatter2);
        factory2.valueProperty().bindBidirectional(formatter2.valueProperty());

        ComboBox<ProjectValue> comboBoxTo = new ComboBox<>(FXCollections.observableList(expressionViewModel.getAvailableValue()));
        comboBoxTo.getSelectionModel().select(expressionViewModel.getSecondOperandAsValue());
        comboBoxTo.visibleProperty().bind(expressionViewModel.literalModeProperty().not().and(expressionViewModel.betweenModeProperty()));
        comboBoxTo.managedProperty().bind(comboBoxTo.visibleProperty());
        expressionViewModel.secondOperandAsValueProperty().bind(comboBoxTo.getSelectionModel().selectedItemProperty());

        comboBoxTo.setCellFactory(new Callback<ListView<ProjectValue>, ListCell<ProjectValue>>() {
            @Override
            public ListCell<ProjectValue> call(ListView<ProjectValue> param) {
                return new ListCell<ProjectValue>() {
                    @Override
                    protected void updateItem(ProjectValue item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getDevice().getName() + " " + item.getValue().getName());
                        }
                    }
                };
            }
        });
        comboBoxTo.setButtonCell(new ListCell<ProjectValue>(){
            @Override
            protected void updateItem(ProjectValue item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item.getDevice().getName() + " " + item.getValue().getName());
                }
            }
        });

        ComboBox<Unit> unitComboBox = new ComboBox<>(FXCollections.observableArrayList(expressionViewModel.getAvailableUnit()));
        unitComboBox.getSelectionModel().select(expressionViewModel.getUnit());
        unitComboBox.visibleProperty().bind(expressionViewModel.literalModeProperty());
        unitComboBox.managedProperty().bind(unitComboBox.visibleProperty());
        expressionViewModel.unitProperty().bind(unitComboBox.getSelectionModel().selectedItemProperty());

        b = new Button("-");

        getChildren().addAll(comboBox, comboBoxType, spinnerFrom, comboBoxFrom, l, spinnerTo, comboBoxTo, unitComboBox, b);
    }

    public void setOnRemovedBtnPressed(EventHandler<ActionEvent> e) {
        b.setOnAction(e);
    }

    public ExpressionViewModel getExpressionViewModel() {
        return expressionViewModel;
    }
}

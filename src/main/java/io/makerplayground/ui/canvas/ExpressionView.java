package io.makerplayground.ui.canvas;

import com.fasterxml.jackson.databind.deser.impl.PropertyValue;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.ProjectValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionView extends HBox {
    private final ExpressionViewModel expressionViewModel;
    private Button b;

    public ExpressionView(ExpressionViewModel expressionViewModel) {
        this.expressionViewModel = expressionViewModel;

        ComboBox<Operator> comboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.values()));
        comboBox.getSelectionModel().select(expressionViewModel.getOperator());
        expressionViewModel.operatorProperty().bind(comboBox.getSelectionModel().selectedItemProperty());

        Spinner<Double> spinnerFrom = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, expressionViewModel.getFirstOperandAsDouble()));
        spinnerFrom.setEditable(true);
        spinnerFrom.visibleProperty().bind(expressionViewModel.literalModeProperty());
        spinnerFrom.managedProperty().bind(spinnerFrom.visibleProperty());
        expressionViewModel.firstOperandAsDoubleProperty().bind(spinnerFrom.getValueFactory().valueProperty());

        ComboBox<ProjectValue> comboBoxFrom = new ComboBox<>(FXCollections.observableList(expressionViewModel.getAvailableValue()));
        comboBoxFrom.getSelectionModel().select(expressionViewModel.getFirstOperandAsValue());
        comboBoxFrom.visibleProperty().bind(expressionViewModel.literalModeProperty().not());
        comboBoxFrom.managedProperty().bind(comboBoxFrom.visibleProperty());
        expressionViewModel.firstOperandAsValueProperty().bind(comboBoxFrom.getSelectionModel().selectedItemProperty());

        Label l = new Label("to");
        l.visibleProperty().bind(expressionViewModel.betweenModeProperty());
        l.managedProperty().bind(l.visibleProperty());

        Spinner<Double> spinnerTo = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, expressionViewModel.getSecondOperandAsDouble()));
        spinnerTo.setEditable(true);
        spinnerTo.visibleProperty().bind(expressionViewModel.literalModeProperty().and(expressionViewModel.betweenModeProperty()));
        spinnerTo.managedProperty().bind(spinnerTo.visibleProperty());
        expressionViewModel.secondOperandAsDoubleProperty().bind(spinnerTo.getValueFactory().valueProperty());

        ComboBox<ProjectValue> comboBoxTo = new ComboBox<>(FXCollections.observableList(expressionViewModel.getAvailableValue()));
        comboBoxTo.getSelectionModel().select(expressionViewModel.getSecondOperandAsValue());
        comboBoxTo.visibleProperty().bind(expressionViewModel.literalModeProperty().not().and(expressionViewModel.betweenModeProperty()));
        comboBoxTo.managedProperty().bind(comboBoxTo.visibleProperty());
        expressionViewModel.secondOperandAsValueProperty().bind(comboBoxTo.getSelectionModel().selectedItemProperty());

        ComboBox<Unit> unitComboBox = new ComboBox<>(FXCollections.observableArrayList(Unit.values()));
        unitComboBox.getSelectionModel().select(expressionViewModel.getUnit());
        expressionViewModel.unitProperty().bind(unitComboBox.getSelectionModel().selectedItemProperty());

        b = new Button("-");

        getChildren().addAll(comboBox, spinnerFrom, comboBoxFrom, l, spinnerTo, comboBoxTo, unitComboBox, b);
    }

    public void setOnRemovedBtnPressed(EventHandler<ActionEvent> e) {
        b.setOnAction(e);
    }

    public ExpressionViewModel getExpressionViewModel() {
        return expressionViewModel;
    }
}

package io.makerplayground.ui.canvas;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.helper.OperandType;
import io.makerplayground.helper.Operator;
import io.makerplayground.helper.Unit;
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
    private final Button b;

    public ExpressionView(ExpressionViewModel expressionViewModel) {
        this.expressionViewModel = expressionViewModel;

            ComboBox<Operator> comboBox = new ComboBox<>(FXCollections.observableArrayList(Operator.values()));
            comboBox.getSelectionModel().select(expressionViewModel.getOperator());
            expressionViewModel.operatorProperty().bind(comboBox.getSelectionModel().selectedItemProperty());

            Spinner<Integer> spinner = new Spinner<>(0,100,50); //TODO: create real constraint
            spinner.setEditable(true);
            expressionViewModel.firstOperandProperty().bind(spinner.getValueFactory().valueProperty());

            ComboBox<Unit> unitComboBox = new ComboBox<>(FXCollections.observableArrayList(Unit.values()));
//            comboBox.getSelectionModel().select(1);
            expressionViewModel.unitProperty().bind(unitComboBox.getSelectionModel().selectedItemProperty());

            b = new Button("-");

            getChildren().addAll(comboBox,spinner,unitComboBox, b);


    }

    public void setOnRemovedBtnPressed(EventHandler<ActionEvent> e) {
        b.setOnAction(e);
    }

    public ExpressionViewModel getExpressionViewModel() {
        return expressionViewModel;
    }
}

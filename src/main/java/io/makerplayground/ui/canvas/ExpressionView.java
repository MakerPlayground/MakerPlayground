package io.makerplayground.ui.canvas;

import io.makerplayground.helper.Operator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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



        b = new Button("-");

        getChildren().addAll(comboBox, b);
    }

    public void setOnRemovedBtnPressed(EventHandler<ActionEvent> e) {
        b.setOnAction(e);
    }

    public ExpressionViewModel getExpressionViewModel() {
        return expressionViewModel;
    }
}

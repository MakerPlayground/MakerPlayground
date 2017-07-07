package io.makerplayground.ui.canvas;

import io.makerplayground.device.Value;
import io.makerplayground.project.Expression;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionControl extends VBox{

    private final DynamicViewModelCreator<Expression, ExpressionViewModel> dynamicViewModelCreator;
    private final DynamicViewCreator<VBox, ExpressionViewModel, ExpressionView> dynamicViewCreator;

    public ExpressionControl(ObservableList<Expression> expressionsList, ObservableList<Value> values) {
        dynamicViewModelCreator = new DynamicViewModelCreator<>(expressionsList, expression -> new ExpressionViewModel(expression, values));
        dynamicViewCreator = new DynamicViewCreator<>(dynamicViewModelCreator, this, expressionViewModel -> {
            ExpressionView expressionView = new ExpressionView(expressionViewModel);
            expressionView.setOnRemovedBtnPressed(event -> expressionsList.remove(expressionView.getExpressionViewModel().getExpression()));
            return expressionView;
        }, new NodeConsumer<VBox, ExpressionView>() {
            @Override
            public void addNode(VBox parent, ExpressionView node) {
                if (parent.getChildren().isEmpty()) {
                    parent.getChildren().add( node);
                } else {
                    parent.getChildren().add(parent.getChildren().size() - 1, node);
                }
            }

            @Override
            public void removeNode(VBox parent, ExpressionView node) {
                parent.getChildren().remove(node);
            }
        });

        Button button = new Button("+");
        button.setOnAction(event -> {
            Expression e =new Expression();
            expressionsList.add(e);
        });
        getChildren().add(button);
    }
}

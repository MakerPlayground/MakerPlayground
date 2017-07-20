package io.makerplayground.ui.canvas;

import io.makerplayground.device.Value;
import io.makerplayground.project.Expression;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.ProjectValue;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Created by USER on 07-Jul-17.
 */
public class ExpressionControl extends VBox{

    private SimpleListProperty<Expression> expressionsList;

    public ExpressionControl(Value v, List<ProjectValue> values) {
        expressionsList = new SimpleListProperty<>(FXCollections.observableArrayList());

        setSpacing(2.0);
        setAlignment(Pos.TOP_CENTER);

        DynamicViewModelCreator<Expression, ExpressionViewModel> dynamicViewModelCreator = new DynamicViewModelCreator<>(expressionsList, expression -> new ExpressionViewModel(v, expression, values));
        DynamicViewCreator<VBox, ExpressionViewModel, ExpressionView> dynamicViewCreator = new DynamicViewCreator<>(dynamicViewModelCreator, this, expressionViewModel -> {
            ExpressionView expressionView = new ExpressionView(expressionViewModel);
            expressionView.setOnRemovedBtnPressed(event -> expressionsList.remove(expressionView.getExpressionViewModel().getExpression()));
            return expressionView;
        }, new NodeConsumer<VBox, ExpressionView>() {
            @Override
            public void addNode(VBox parent, ExpressionView node) {
                if (parent.getChildren().isEmpty()) {
                    parent.getChildren().add(node);
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
            Expression e = new Expression();
            expressionsList.add(e);
        });
        getChildren().add(button);
    }

    public ObservableList<Expression> getExpressionsList() {
        return expressionsList.get();
    }

    public SimpleListProperty<Expression> expressionsListProperty() {
        return expressionsList;
    }

    public void setExpressionsList(ObservableList<Expression> expressionsList) {
        this.expressionsList.clear();
        this.expressionsList.addAll(expressionsList);
    }
}

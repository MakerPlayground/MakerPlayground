package io.makerplayground.ui.canvas.node.usersetting;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;

public class NumberWithUnitPopOver extends PopOver {

    public NumberWithUnitPopOver() {
        FlowPane operandChipPane = new FlowPane();
        operandChipPane.setPrefSize(150, 150);
        operandChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        FlowPane operatorChipPane = new FlowPane();
        operatorChipPane.setPrefSize(150, 150);
        operatorChipPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        SplitPane splitPane = new SplitPane(operandChipPane, operatorChipPane);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.5);

        setContentNode(splitPane);
    }

}

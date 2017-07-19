package io.makerplayground.ui;

import io.makerplayground.generator.Diagram;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateView extends Dialog {
    private final GenerateViewModel viewModel;

    public GenerateView(GenerateViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        ScrollPane scrollPane = new ScrollPane();
        VBox test = new VBox();

        Diagram wiringDiagram = new Diagram(viewModel.getProject());

        test.getChildren().add(wiringDiagram);
        scrollPane.setContent(test);
        getDialogPane().setContent(scrollPane);
    }
}

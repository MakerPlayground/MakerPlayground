package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.makerplayground.viewmodel.CanvasViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/CanvasView.fxml")
public class CanvasView implements FxmlView<CanvasViewModel>, Initializable {

    @FXML
    private Label label;

    @InjectViewModel
    private CanvasViewModel vm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        label.textProperty().bindBidirectional(vm.textProperty());
    }
}

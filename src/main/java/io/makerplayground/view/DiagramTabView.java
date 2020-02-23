package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.makerplayground.viewmodel.DiagramTabViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/DiagramTabView.fxml")
public class DiagramTabView implements FxmlView<DiagramTabViewModel>, Initializable {

    @InjectViewModel
    private DiagramTabViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

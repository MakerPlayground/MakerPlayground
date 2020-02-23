package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import io.makerplayground.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/MainView.fxml")
public class MainView implements FxmlView<MainViewModel>, Initializable {

    @InjectViewModel
    private MainViewModel viewModel;

    @FXML
    private BorderPane root;

    @FXML
    private AnchorPane toolbar;

    @FXML
    private BorderPane mainWindow;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

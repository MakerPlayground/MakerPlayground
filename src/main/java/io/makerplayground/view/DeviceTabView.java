package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import io.makerplayground.viewmodel.DeviceTabViewModel;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/DeviceTabView.fxml")
public class DeviceTabView implements FxmlView<DeviceTabViewModel>, Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

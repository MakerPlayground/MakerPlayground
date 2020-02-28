package io.makerplayground.view;

import de.saxsys.mvvmfx.FxmlPath;
import de.saxsys.mvvmfx.FxmlView;
import io.makerplayground.viewmodel.DeviceMonitorTabViewModel;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

@FxmlPath("/io/makerplayground/view/DeviceMonitorTabView.fxml")
public class DeviceMonitorTabView implements FxmlView<DeviceMonitorTabViewModel>, Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

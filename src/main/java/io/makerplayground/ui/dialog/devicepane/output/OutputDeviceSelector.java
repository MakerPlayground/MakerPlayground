package io.makerplayground.ui.dialog.devicepane.output;

import io.makerplayground.project.UserSetting;
import io.makerplayground.ui.canvas.node.SceneViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceSelector extends PopOver {
    @FXML FlowPane flowPane;
    public OutputDeviceSelector(SceneViewModel viewModel) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/dialog/devicepane/output/OutputDeviceSelector.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewModel.getProjectOutputDevice().stream().filter(device -> {
            for (UserSetting userSetting : viewModel.getStateDevice()) {
                if (userSetting.getDevice().getName().equals(device.getName())) {
                    return false;
                }
            }
            return true;
        }).forEachOrdered(device -> {
            OutputDeviceIconSelectorView outputIconView = new OutputDeviceIconSelectorView(device);
            flowPane.getChildren().add(outputIconView);
            outputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                viewModel.getScene().addDevice(device);
                flowPane.getChildren().remove(outputIconView);
            });
        });

    }
}

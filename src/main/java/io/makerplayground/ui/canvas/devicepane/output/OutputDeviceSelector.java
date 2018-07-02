package io.makerplayground.ui.canvas.devicepane.output;

import io.makerplayground.project.UserSetting;
import io.makerplayground.ui.canvas.node.SceneViewModel;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceSelector extends PopOver {

    public OutputDeviceSelector(SceneViewModel viewModel) {
        FlowPane flowPane = new FlowPane();
        setContentNode(flowPane);
        flowPane.setMaxWidth(245.0);
        flowPane.setMinWidth(245.0);
        flowPane.setPadding(new Insets(10.0,10.0,10.0,10.));
        flowPane.setHgap(5.0);

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

        setDetachable(false);
        setArrowLocation(ArrowLocation.TOP_LEFT);
    }
}

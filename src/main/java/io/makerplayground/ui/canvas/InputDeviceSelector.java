package io.makerplayground.ui.canvas;

import io.makerplayground.project.TimeCondition;
import io.makerplayground.project.UserSetting;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceSelector extends PopOver {

    public InputDeviceSelector(ConditionViewModel viewModel) {
        FlowPane flowPane = new FlowPane();
        setContentNode(flowPane);
        flowPane.setMaxWidth(245.0);
        flowPane.setMinWidth(245.0);
        flowPane.setPadding(new Insets(10.0,10.0,10.0,10.));
        flowPane.setHgap(5.0);

        // add timer icon only when time condition is not exist
        if (!viewModel.getCondition().getTimeCondition().isPresent()) {
            TimerIconView timeIcon = new TimerIconView();
            flowPane.getChildren().add(timeIcon);
            timeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                viewModel.getCondition().setTimeCondition(new TimeCondition());
                flowPane.getChildren().remove(timeIcon);
            });
        }

        // add icon of input's devices which haven't been included in the condition in the popover
        viewModel.getProjectInputDevice().stream().filter(device -> {
            for (UserSetting userSetting : viewModel.getConditionDevice()) {
                if (userSetting.getDevice() == device) {
                    return false;
                }
            }
            return true;
        }).forEachOrdered(device -> {
            InputDeviceIconSelectorView inputIconView = new InputDeviceIconSelectorView(device);
            flowPane.getChildren().add(inputIconView);
            inputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                viewModel.getCondition().addDevice(device);
                flowPane.getChildren().remove(inputIconView);
            });
        });

    }

}

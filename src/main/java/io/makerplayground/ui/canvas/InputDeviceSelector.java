package io.makerplayground.ui.canvas;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

import java.util.function.Predicate;

/**
 * Created by USER on 05-Jul-17.
 */
public class InputDeviceSelector extends PopOver {
    private final ConditionViewModel viewModel;

    public InputDeviceSelector(ConditionViewModel viewModel) {
        this.viewModel = viewModel;

        FilteredList<ProjectDevice> projectDeviceList = viewModel.getProjectInputDevice().filtered(new Predicate<ProjectDevice>() {
            @Override
            public boolean test(ProjectDevice projectDevice) {
                boolean found = true;
                for (UserSetting userSetting : viewModel.getConditionDevice()) {
                    if (userSetting.getDevice().getName().equals(projectDevice.getName())) {
                        found = false;
                        break;
                    }
                }
                return found;
            }
        });

        FlowPane flowPane = new FlowPane();
        setContentNode(flowPane);

        for (ProjectDevice p : projectDeviceList) {
            InputDeviceIconSelectorView inputIconView = new InputDeviceIconSelectorView(p);
            flowPane.getChildren().add(inputIconView);
            inputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    viewModel.getCondition().addDevice(p);
                    flowPane.getChildren().remove(inputIconView);
                }
            });
        }

    }

}

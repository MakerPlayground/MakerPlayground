package io.makerplayground.ui.canvas;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

import java.util.function.Predicate;


/**
 * Created by tanyagorn on 6/26/2017.
 */
public class OutputDeviceSelector extends PopOver {
    private final SceneViewModel viewModel;

    public OutputDeviceSelector(SceneViewModel viewModel) {
        this.viewModel = viewModel;

        FilteredList<ProjectDevice> projectDeviceList = viewModel.getProjectOutputDevice().filtered(new Predicate<ProjectDevice>() {
            @Override
            public boolean test(ProjectDevice projectDevice) {
                boolean found = true;
                for (UserSetting userSetting : viewModel.getStateDevice()) {
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
        flowPane.setMaxWidth(245.0);
        flowPane.setMinWidth(245.0);
        flowPane.setPadding(new Insets(10.0,10.0,10.0,10.));
        flowPane.setHgap(5.0);

        for (ProjectDevice p : projectDeviceList) {
            OutputDeviceIconSelectorView outputIconView = new OutputDeviceIconSelectorView(p);
            flowPane.getChildren().add(outputIconView);
            outputIconView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    viewModel.getState().addDevice(p);
                    flowPane.getChildren().remove(outputIconView);
                }
            });
        }
    }
}

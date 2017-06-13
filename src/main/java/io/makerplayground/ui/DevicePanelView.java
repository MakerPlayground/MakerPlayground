package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelView extends Accordion {

    private final DevicePanelViewModel viewModel;
    private final ViewFactory<DevicePanelIconViewModel, DevicePanelIcon> viewFactory = new ViewFactory<DevicePanelIconViewModel, DevicePanelIcon>() {
        @Override
        public DevicePanelIcon newInstance(DevicePanelIconViewModel devicePanelIconViewModel) {
            DevicePanelIcon icon = new DevicePanelIcon(devicePanelIconViewModel);
            icon.setOnAction(event -> viewModel.removeDevice(devicePanelIconViewModel));
            return icon;
        }
    };
    private final NodeConsumer<FlowPane, DevicePanelIcon> nodeConsumer = new NodeConsumer<FlowPane, DevicePanelIcon>() {
        @Override
        public void addNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().add(node);
        }

        @Override
        public void removeNode(FlowPane parent, DevicePanelIcon node) {
            parent.getChildren().remove(node);
        }
    };

    public DevicePanelView(DevicePanelViewModel viewModel) {
        this.viewModel = viewModel;
        initView();
    }

    private void initView() {
        ResourceBundle labels = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());

        FlowPane mcuPane = new FlowPane();
        // TODO: add code for mcu

        FlowPane inputPane = new FlowPane();
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> inputViewCreator =
                new DynamicViewCreator<>(viewModel.getInputChildViewModel(), inputPane, viewFactory, nodeConsumer);

        Button newInputDeviceButton = new Button("+");
        newInputDeviceButton.setOnAction(event -> {
            // TODO: copy from output pane
        });
        inputPane.getChildren().add(newInputDeviceButton);

        FlowPane outputPane = new FlowPane();
        DynamicViewCreator<FlowPane, DevicePanelIconViewModel, DevicePanelIcon> outputViewCreator =
                new DynamicViewCreator<>(viewModel.getOutputChildViewModel(), outputPane, viewFactory, nodeConsumer);

        Button newOutputDeviceButton = new Button("+");
        newOutputDeviceButton.setOnAction(event -> {
            // TODO: invoke device selector
//            DeviceSelectorView  selectorView = new DeviceSelectorView();
//            selectorView.show(getScene().getWindow());
            //viewModel.addDevice();
        });
        outputPane.getChildren().add(newOutputDeviceButton);

        TitledPane t1 = new TitledPane(labels.getString("Microcontroller"), mcuPane);
        TitledPane t2 = new TitledPane(labels.getString("Output Device"), outputPane);
        TitledPane t3 = new TitledPane(labels.getString("Input Device"), inputPane);
        getPanes().addAll(t1, t2, t3);

        setMinWidth(200);
    }

}

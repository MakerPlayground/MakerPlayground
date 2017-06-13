package io.makerplayground.ui;

import io.makerplayground.device.Action;
import io.makerplayground.project.DeviceSetting;
import io.makerplayground.project.DiagramState;
import io.makerplayground.uihelper.Filter;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.ViewModelFactory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateViewModel {

    private final DiagramState diagramState;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    private final SimpleDoubleProperty x;
    private final SimpleDoubleProperty y;


    private final DynamicViewModelCreator<DeviceSetting, StateDeviceIconViewModel> dynamicViewModelCreator;

    public StateViewModel(DiagramState diagramState) {
        this.diagramState = diagramState;
        this.name = new SimpleStringProperty(diagramState.getName());
        this.delay = new SimpleDoubleProperty(diagramState.getDelayDuration());
        this.x = new SimpleDoubleProperty(diagramState.getTopLeft().getX());
        this.y = new SimpleDoubleProperty(diagramState.getTopLeft().getY());

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(diagramState.getUnmodifiableDeviceSetting(), new ViewModelFactory<DeviceSetting, StateDeviceIconViewModel>() {
            @Override
            public StateDeviceIconViewModel newInstance(DeviceSetting deviceSetting) {
                return new StateDeviceIconViewModel(deviceSetting);
            }
        }, new Filter<DeviceSetting>() {
            @Override
            public boolean apply(DeviceSetting deviceSetting) {
                return (deviceSetting.getAction().getType() == Action.ActionType.Active);
            }
        });
        // TODO: Do the same for inactive - unchanged
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public double getDelay() {
        return delay.get();
    }

    public SimpleDoubleProperty delayProperty() {
        return delay;
    }

    public DynamicViewModelCreator<DeviceSetting, StateDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() {
        return x.get();
    }

    public SimpleDoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public SimpleDoubleProperty yProperty() {
        return y;
    }
}

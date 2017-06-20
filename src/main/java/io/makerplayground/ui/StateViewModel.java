package io.makerplayground.ui;

import io.makerplayground.device.ActionType;
import io.makerplayground.project.State;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.ModelFilter;
import io.makerplayground.uihelper.ViewModelFactory;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateViewModel {

    private final State state;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    //private final SimpleDoubleProperty x;
    //private final SimpleDoubleProperty y;


    private final DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> dynamicViewModelCreatorActive;
    private final DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> dynamicViewModelCreatorInactive;

    public StateViewModel(State state) {
        this.state = state;
        this.name = new SimpleStringProperty(state.getName());
        this.delay = new SimpleDoubleProperty(state.getDelay());
        //this.x = new SimpleDoubleProperty(state.getPosition().getX());
        //this.y = new SimpleDoubleProperty(state.getPosition().getY());

        this.dynamicViewModelCreatorActive = new DynamicViewModelCreator<>(state.getSetting(), new ViewModelFactory<UserSetting, StateDeviceIconViewModel>() {
            @Override
            public StateDeviceIconViewModel newInstance(UserSetting userSetting) {
                return new StateDeviceIconViewModel(userSetting);
            }
        }, new ModelFilter<UserSetting>() {
            @Override
            public boolean apply(UserSetting userSetting) {
                return (userSetting.getAction().getType() == ActionType.Active);
            }
        });
        this.dynamicViewModelCreatorInactive = new DynamicViewModelCreator<>(state.getSetting(), new ViewModelFactory<UserSetting, StateDeviceIconViewModel>() {
            @Override
            public StateDeviceIconViewModel newInstance(UserSetting userSetting) {
                return new StateDeviceIconViewModel(userSetting);
            }
        }, new ModelFilter<UserSetting>() {
            @Override
            public boolean apply(UserSetting userSetting) {
                return (userSetting.getAction().getType() == ActionType.Inactive);
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

    public DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> getDynamicViewModelCreatorActive() {
        return dynamicViewModelCreatorActive;
    }
    public DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> getDynamicViewModelCreatorInactive() {
        return dynamicViewModelCreatorInactive;
    }
    public double getX() {
        return state.getPosition().getX();
    }

    public DoubleProperty xProperty() {
        return state.getPosition().xProperty();
    }

    public double getY() {
        return state.getPosition().getY();
    }

    public DoubleProperty yProperty() {
        return state.getPosition().yProperty();
    }
}

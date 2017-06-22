package io.makerplayground.ui;

import io.makerplayground.device.ActionType;
import io.makerplayground.project.State;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.DynamicViewModelCreatorBuilder;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateViewModel {

    private final State state;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    private final SimpleDoubleProperty x;
    private final SimpleDoubleProperty y;

    private final DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> dynamicViewModelCreatorActive;
    private final DynamicViewModelCreator<UserSetting, StateDeviceIconViewModel> dynamicViewModelCreatorInactive;

    public StateViewModel(State state) {
        this.state = state;
        this.name = new SimpleStringProperty(state.getName());
        this.delay = new SimpleDoubleProperty(state.getDelay());
        this.x = new SimpleDoubleProperty(state.getPosition().getX());
        this.y = new SimpleDoubleProperty(state.getPosition().getY());

        this.dynamicViewModelCreatorActive = new DynamicViewModelCreatorBuilder<UserSetting, StateDeviceIconViewModel>()
                .setModel(state.getSetting())
                .setViewModelFactory(StateDeviceIconViewModel::new)
                .setFilter(userSetting -> userSetting.getAction().getType() == ActionType.Active)
                .createDynamicViewModelCreator();
        this.dynamicViewModelCreatorInactive = new DynamicViewModelCreatorBuilder<UserSetting, StateDeviceIconViewModel>()
                .setModel(state.getSetting())
                .setViewModelFactory(StateDeviceIconViewModel::new)
                .setFilter(userSetting -> userSetting.getAction().getType() == ActionType.Inactive)
                .createDynamicViewModelCreator();
        // TODO: Do the same for unchanged

//        for (UserSetting setting : state.getSetting()) {
//            setting.actionProperty().addListener(new ChangeListener<Action>() {
//                @Override
//                public void changed(ObservableValue<? extends Action> observable, Action oldValue, Action newValue) {
//                    StateViewModel.this.dynamicViewModelCreatorActive.invalidate(setting);
//                    StateViewModel.this.dynamicViewModelCreatorInactive.invalidate(setting);
//                }
//            });
//        }
//        state.getSetting().addListener(new ListChangeListener<UserSetting>() {
//            @Override
//            public void onChanged(Change<? extends UserSetting> c) {
//                while (c.next()) {
//                    if (c.wasPermutated()) {
//                        throw new UnsupportedOperationException();
//                    } else if (c.wasUpdated()) {
//                        throw new UnsupportedOperationException();
//                    } else {
//                        for (UserSetting removedItem : c.getRemoved()) {
//                            //removedItem.actionProperty().removeListener();
//                        }
//                        for (UserSetting adddedItem : c.getAddedSubList()) {
//                            adddedItem.actionProperty().addListener(new ChangeListener<Action>() {
//                                @Override
//                                public void changed(ObservableValue<? extends Action> observable, Action oldValue, Action newValue) {
//                                    StateViewModel.this.dynamicViewModelCreatorActive.invalidate(adddedItem);
//                                    StateViewModel.this.dynamicViewModelCreatorInactive.invalidate(adddedItem);
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//        });
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

package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconViewModel;
import io.makerplayground.ui.canvas.helper.DynamicViewModelCreator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class SceneViewModel {
    private final Scene scene;
    //private final SimpleStringProperty name;
    private final SimpleDoubleProperty delay;
    private final Project project;

    private final DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> dynamicViewModelCreator;

    private final BooleanProperty hasDeviceToAdd;

    public SceneViewModel(Scene scene, Project project) {
        this.scene = scene;
        //this.name = new SimpleStringProperty(scene.getName());
        this.delay = new SimpleDoubleProperty(scene.getDelay());
        this.delay.addListener((observable, oldValue, newValue) -> scene.setDelay(newValue.doubleValue()));
        this.project = project;

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(scene.getSetting(), userSetting -> new SceneDeviceIconViewModel(userSetting, scene, project));

        hasDeviceToAdd = new SimpleBooleanProperty(scene.getSetting().size() != project.getOutputDevice().size());
        // TODO: find a better way (now since only actuator and connectivity can be in the scene so we track these two)
        this.project.getActuator().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(scene.getSetting().size() != project.getOutputDevice().size());
        });
        this.project.getConnectivity().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(scene.getSetting().size() != project.getOutputDevice().size());
        });
        // when we remove something from the scene
        this.scene.getSetting().addListener((InvalidationListener) observable -> {
            hasDeviceToAdd.set(scene.getSetting().size() != project.getOutputDevice().size());
        });
    }

    public String getName() {
        return scene.getName();
    }

//    public StringProperty nameProperty() {
//        return scene.nameProperty();
//    }

    public boolean setName(String name) {
        if (!isNameDuplicate(name)) {
            scene.setName(name);
            return true;
        } else {
            return false;
        }
    }

    public double getDelay() {
        return delay.get();
    }

    public void setDelay(double delay) {
        this.delay.set(delay);
    }

    public SimpleDoubleProperty delayProperty() {
        return delay;
    }

    public Scene.DelayUnit getDelayUnit() {
        return scene.getDelayUnit();
    }

    public void setDelayUnit(Scene.DelayUnit unit) {
        scene.setDelayUnit(unit);
    }

    public ObjectProperty<Scene.DelayUnit> delayUnitProperty() {
        return scene.delayUnitProperty();
    }

    public DynamicViewModelCreator<UserSetting, SceneDeviceIconViewModel> getDynamicViewModelCreator() {
        return dynamicViewModelCreator;
    }

    public double getX() {
        return scene.getLeft();
    }

    public DoubleProperty xProperty() {
        return scene.leftProperty();
    }

    public double getY() {
        return scene.getTop();
    }

    public DoubleProperty yProperty() {
        return scene.topProperty();
    }

    public List<ProjectDevice> getProjectOutputDevice() {
        return project.getOutputDevice();
    }

    public Scene getScene() {
        return scene;
    }

    public ObservableList<UserSetting> getStateDevice() {
        return scene.getSetting();
    }

    public void removeStateDevice(ProjectDevice projectDevice) {
        scene.removeDevice(projectDevice);
    }

    public BooleanProperty hasDeviceToAddProperty() {
        return hasDeviceToAdd;
    }

    // Check if current scene's name is duplicated with other scenes
    // return true when this name cannot be used
    public boolean isNameDuplicate(String newName) {
        for (Scene scene : project.getScene()) {
            //System.out.println("name value = " + scene.getName() + " new name = " + newName);
            if ((this.scene != scene) && scene.getName().equals(newName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasConnectionFrom(NodeElement other) {
        return project.hasLine(other, scene);
    }

    public boolean hasConnectionTo(NodeElement other) {
        return project.hasLine(scene, other);
    }

    public final DiagramError getError() {
        return scene.getError();
    }

    public final ReadOnlyObjectProperty<DiagramError> errorProperty() {
        return scene.errorProperty();
    }
}

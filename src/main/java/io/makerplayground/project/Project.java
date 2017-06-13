package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.InputDevice;
import io.makerplayground.device.OutputDevice;
import io.makerplayground.ui.StateDeviceIconViewModel;
import io.makerplayground.ui.StateViewModel;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.Filter;
import io.makerplayground.uihelper.ViewModelFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
public class Project {
    private final DirectedGraph<DiagramState, DiagramCondition> diagram;  // TODO: make the graph observable some how
    private final ObservableList<ProjectDevice> outputDevice;
    private final ObservableList<ProjectDevice> inputDevice;
    private final ObservableList<DiagramState> observableDiagram;

    private final DynamicViewModelCreator<DiagramState, StateViewModel> dynamicViewModelCreator;

    private int numDevice = 1; // TODO: to be removed

    public Project() {
        diagram = new SimpleDirectedGraph<DiagramState, DiagramCondition>((v1, v2) -> new DiagramCondition());
        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();
        observableDiagram = FXCollections.observableArrayList();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);

        this.dynamicViewModelCreator = new DynamicViewModelCreator<>(observableDiagram, new ViewModelFactory<DiagramState, StateViewModel>() {
            @Override
            public StateViewModel newInstance(DiagramState diagramState) {
                return new StateViewModel(diagramState);
            }
        });

}
    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(OutputDevice device) {
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + String.valueOf(numDevice), device);

        for (DiagramState state : diagram.vertexSet()) {
            state.getDeviceSetting().add(new DeviceSetting(projectDevice));
        }

        // TODO: add logic to create a running number per device category
        outputDevice.add(projectDevice);
        numDevice++;
    }

    public boolean removeOutputDevice(ProjectDevice device) {
        for (DiagramState state : diagram.vertexSet()) {
            state.removeDevice(device);
        }

        return outputDevice.remove(device);
    }

    private final ObservableList<ProjectDevice> unmodifiableInputDevice;

    public ObservableList<ProjectDevice> getInputDevice() {
        return unmodifiableInputDevice;
    }

    public void addInputDevice(InputDevice device) {
        // TODO: add logic to create a running number per device category
        inputDevice.add(new ProjectDevice(device.getName() + String.valueOf(numDevice), device));
        numDevice++;
    }

    public boolean removeInputDevice(ProjectDevice device) {
        return inputDevice.remove(device);
    }

    public void addState() {
        DiagramState state = new DiagramState("state" + (observableDiagram.size() + 1));
        // Add every output device to be displayed in new state
        for (ProjectDevice projectDevice: outputDevice) {
            DeviceSetting e = new DeviceSetting(projectDevice);
            state.getDeviceSetting().add(e);
        }
        diagram.addVertex(state);
        observableDiagram.add(state);
    }

    public void removeState(DiagramState diagramState) {
        diagram.removeVertex(diagramState);
        observableDiagram.remove(diagramState);
    }

    public DirectedGraph<DiagramState, DiagramCondition> getDiagram() {
        return diagram;
    }
}

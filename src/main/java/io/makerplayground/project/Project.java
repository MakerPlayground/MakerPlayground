package io.makerplayground.project;

import io.makerplayground.device.GenericDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 *
 * Created by Nuntipat Narkthong on 6/2/2017 AD.
 */
public class Project {
    private final DirectedGraph<State, Condition> diagram;  // TODO: make the graph observable some how
    private final ObservableList<ProjectDevice> inputDevice;
    private final ObservableList<ProjectDevice> outputDevice;
    private final ObservableList<State> state;
    private final ObservableList<Condition> condition;

    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;

    private int numDevice = 1; // TODO: to be removed

    public Project() {
        diagram = new SimpleDirectedGraph<State, Condition>((v1, v2) -> new Condition());

        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();
        state = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
    }

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(GenericDevice device) {
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + String.valueOf(numDevice), device);

        for (State state : diagram.vertexSet()) {
            state.getSetting().add(new UserSetting(projectDevice));
        }

        // TODO: add logic to create a running number per device category
        outputDevice.add(projectDevice);
        numDevice++;
    }

    public boolean removeOutputDevice(ProjectDevice device) {

        for (State state : diagram.vertexSet()) {
            state.removeDevice(device);
        }

        return outputDevice.remove(device);
    }

    private final ObservableList<ProjectDevice> unmodifiableInputDevice;

    public ObservableList<ProjectDevice> getInputDevice() {
        return unmodifiableInputDevice;
    }

    public void addInputDevice(GenericDevice device) {
        // TODO: add logic to create a running number per device category
        inputDevice.add(new ProjectDevice(device.getName() + String.valueOf(numDevice), device));
        numDevice++;
    }

    public boolean removeInputDevice(ProjectDevice device) {
        return inputDevice.remove(device);
    }

    public void addState() {
        State state = new State();
        state.setName("state" + (this.state.size() + 1));
        // Add every output device to be displayed in new state
        for (ProjectDevice projectDevice: outputDevice) {
            UserSetting e = new UserSetting(projectDevice);
            state.getSetting().add(e);
        }
        diagram.addVertex(state);
        this.state.add(state);
    }

    public void removeState(State state) {
        diagram.removeVertex(state);
        this.state.remove(state);
    }

    public ObservableList<State> getState() {
        return state;
    }

    public DirectedGraph<State, Condition> getDiagram() {
        return diagram;
    }
}

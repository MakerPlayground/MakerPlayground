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

    private final ObservableList<ProjectDevice> unmodifiableInputDevice;
    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;
    private final ObservableList<State> unmodifiableState;
    private final ObservableList<Condition> unmodifiableCondition;

    private int numDevice = 1; // TODO: to be removed

    public Project() {
        diagram = new SimpleDirectedGraph<>((v1, v2) -> new Condition());

        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();
        state = FXCollections.observableArrayList();
        condition = FXCollections.observableArrayList();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
        unmodifiableState = FXCollections.unmodifiableObservableList(state);
        unmodifiableCondition = FXCollections.unmodifiableObservableList(condition);
    }

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(GenericDevice device) {
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + String.valueOf(numDevice), device);

        for (State s : state) {
            s.addDevice(projectDevice);
        }

        // TODO: add logic to create a running number per device category
        outputDevice.add(projectDevice);
        numDevice++;
    }

    public boolean removeOutputDevice(ProjectDevice device) {
        for (State s : state) {
            s.removeDevice(device);
        }

        return outputDevice.remove(device);
    }

    public ObservableList<ProjectDevice> getInputDevice() {
        return unmodifiableInputDevice;
    }

    public void addInputDevice(GenericDevice device) {
        ProjectDevice projectDevice = new ProjectDevice(device.getName() + String.valueOf(numDevice), device);

        for (State s : state) {
            s.addDevice(projectDevice);
        }

        // TODO: add logic to create a running number per device category
        inputDevice.add(projectDevice);
        numDevice++;
    }

    public boolean removeInputDevice(ProjectDevice device) {
        for (State s : state) {
            s.removeDevice(device);
        }

        return outputDevice.remove(device);
    }

    public ObservableList<State> getState() {
        return unmodifiableState;
    }

    public State addState() {
        State s = new State();
        // TODO: check for duplicate name
        s.setName("state" + (state.size() + 1));

        // Add every output device to be displayed in new state
        for (ProjectDevice projectDevice: outputDevice) {
            s.addDevice(projectDevice);
        }

        diagram.addVertex(s);
        state.add(s);
        return s;
    }

    public void removeState(State s) {
        // TODO: remove edge connect to that state
        diagram.removeVertex(s);
        state.remove(s);
    }

    // TODO: add method to manage condition
}

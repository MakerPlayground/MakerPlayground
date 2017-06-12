package io.makerplayground.project;

import io.makerplayground.device.InputDevice;
import io.makerplayground.device.OutputDevice;
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

    private int numDevice = 1; // TODO: to be removed

    public Project() {
        diagram = new SimpleDirectedGraph<DiagramState, DiagramCondition>((v1, v2) -> new DiagramCondition());

        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
    }

    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(OutputDevice device) {
        // TODO: add DeviceSetting object to every DiagramState
        // TODO: add logic to create a running number per device category
        outputDevice.add(new ProjectDevice(device.getName() + String.valueOf(numDevice), device));
        numDevice++;
    }

    public boolean removeOutputDevice(ProjectDevice device) {
        // TODO: remove DeviceSetting object from every DiagramState
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
}

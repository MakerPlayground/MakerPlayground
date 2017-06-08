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
    private final DirectedGraph<DiagramVertex, DiagramEdge> diagram;  // TODO: make the graph observable some how
    private final ObservableList<ProjectDevice> outputDevice;
    private final ObservableList<ProjectDevice> inputDevice;

    private final ObservableList<ProjectDevice> unmodifiableOutputDevice;
    private final ObservableList<ProjectDevice> unmodifiableInputDevice;

    private int numDevice = 1; // TODO: to be removed

    public Project() {
        diagram = new SimpleDirectedGraph<DiagramVertex, DiagramEdge>((v1, v2) -> new DiagramEdge());

        outputDevice = FXCollections.observableArrayList();
        inputDevice = FXCollections.observableArrayList();

        unmodifiableOutputDevice = FXCollections.unmodifiableObservableList(outputDevice);
        unmodifiableInputDevice = FXCollections.unmodifiableObservableList(inputDevice);
    }

    public ObservableList<ProjectDevice> getOutputDevice() {
        return unmodifiableOutputDevice;
    }

    public void addOutputDevice(OutputDevice device) {
        // TODO: add logic to create a running number per device category
        outputDevice.add(new ProjectDevice(device.getName() + String.valueOf(numDevice), device));
        numDevice++;
    }

    public boolean removeOutputDevice(ProjectDevice device) {
        return outputDevice.remove(device);
    }

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

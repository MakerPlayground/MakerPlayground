package io.makerplayground.project;

import io.makerplayground.device.GenericDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        int maxCount = 0;
        // Find all the same genericDevice's name in outputDevice list
        // to get count for creating running number
        List<ProjectDevice> deviceSameType = outputDevice.stream()
                .filter(d -> d.getGenericDevice().getName().equals(device.getName()))
                .collect(Collectors.toList());
        for (ProjectDevice d : deviceSameType) {
            if (d.getName().contains(device.getName())) {
                // Extract number from string for creating running number
                Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
                Matcher matcher = lastIntPattern.matcher(d.getName());
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt >= maxCount) {
                        maxCount = lastNumberInt;
                    }
                }
            }
        }

        ProjectDevice projectDevice = new ProjectDevice(device.getName() + (maxCount+1), device);
        for (State state : diagram.vertexSet()) {
            state.getSetting().add(new UserSetting(projectDevice));
        }

        outputDevice.add(projectDevice);
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
        int maxCount = 0;
        // Find all the same genericDevice's name in outputDevice list
        // to get count for creating running number
        List<ProjectDevice> deviceSameType = inputDevice.stream()
                .filter(d -> d.getGenericDevice().getName().equals(device.getName()))
                .collect(Collectors.toList());
        for (ProjectDevice d : deviceSameType) {
            if (d.getName().contains(device.getName())) {
                // Extract number from string for creating running number
                Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
                Matcher matcher = lastIntPattern.matcher(d.getName());
                if (matcher.find()) {
                    String someNumberStr = matcher.group(1);
                    int lastNumberInt = Integer.parseInt(someNumberStr);
                    if (lastNumberInt >= maxCount) {
                        maxCount = lastNumberInt;
                    }
                }
            }
        }

        // TODO: Add to condition

        inputDevice.add(new ProjectDevice(device.getName() + (maxCount+1), device));
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

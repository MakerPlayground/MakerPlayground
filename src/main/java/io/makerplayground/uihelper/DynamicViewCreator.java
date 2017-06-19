package io.makerplayground.uihelper;

import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class DynamicViewCreator<T extends Parent, U,  V extends Node> {
    private final DynamicViewModelCreator<?, U> modelLoader;
    private final T parent;
    private final ViewFactory<U, V> viewFactory;
    private final NodeConsumer<T, V> nodeConsumer;

    private final Map<U, V> nodeMap;

    public DynamicViewCreator(DynamicViewModelCreator<?, U> modelLoader, T parent, ViewFactory<U, V> viewFactory, NodeConsumer<T, V> nodeConsumer) {
        this.modelLoader = modelLoader;
        this.parent = parent;
        this.viewFactory = viewFactory;
        this.nodeConsumer = nodeConsumer;

        this.nodeMap = new HashMap<>();

        for (U controller : modelLoader.getControllerMap().values()) {
            addNode(controller);
        }

        modelLoader.getControllerMap().addListener((MapChangeListener<Object, U>) change -> {
            if (change.wasRemoved()) {
                removeNode(change.getValueRemoved());
            } else if (change.wasAdded()) {
                addNode(change.getValueAdded());
            }
            // TODO: add case for update
        });
    }

    private void addNode(U controller) {
        V node = viewFactory.newInstance(controller);
        nodeMap.put(controller, node);
        nodeConsumer.addNode(parent, node);
    }

    private void removeNode(U controller) {
        V node = nodeMap.remove(controller);
        nodeConsumer.removeNode(parent, node);
    }
}

package io.makerplayground.ui.canvas.helper;

import javafx.scene.Node;
import javafx.scene.Parent;

public interface NodeAdder <T extends Parent, U extends Node> {
    void addNode(T parent, U node);
}

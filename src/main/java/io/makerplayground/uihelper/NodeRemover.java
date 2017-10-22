package io.makerplayground.uihelper;

import javafx.scene.Node;
import javafx.scene.Parent;

public interface NodeRemover <T extends Parent, U extends Node> {
    void removeNode(T parent, U node);
}

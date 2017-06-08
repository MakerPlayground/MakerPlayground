package io.makerplayground.uihelper;

import javafx.scene.Node;
import javafx.scene.Parent;

/**
 *
 * Created by Nuntipat Narkthong on 6/9/2017 AD.
 */
public interface NodeConsumer<T extends Parent, U extends Node> {
    void addNode(T parent, U node);
    void removeNode(T parent, U node);
}

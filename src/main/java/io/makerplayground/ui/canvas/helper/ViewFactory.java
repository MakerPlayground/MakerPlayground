package io.makerplayground.ui.canvas.helper;

import javafx.scene.Node;

/**
 *
 * Created by Nuntipat Narkthong on 6/8/2017 AD.
 */
public interface ViewFactory<T, U extends Node> {
    U newInstance(T t);
}

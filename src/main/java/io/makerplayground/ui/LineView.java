package io.makerplayground.ui;

import javafx.beans.binding.Bindings;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;

/**
 * Created by USER on 20-Jun-17.
 */
public class LineView extends Path{

    private final LineViewModel viewModel;

    public LineView(LineViewModel viewModel) {
        this.viewModel = viewModel;
        Bindings.bindContentBidirectional(this.getElements(),viewModel.getPoint());
    }


}

package io.makerplayground.ui.canvas;

import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Arc;

import java.io.IOException;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class BeginSceneView extends InteractiveNode {
    private final HBox beginHBox = new HBox();
    @FXML private Arc outPort;
    @FXML private Label labelHBox;

    private final BeginSceneViewModel beginSceneViewModel;

    public BeginSceneView(BeginSceneViewModel beginSceneViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.beginSceneViewModel = beginSceneViewModel;

        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BeginScene.fxml"));
        fxmlLoader.setRoot(beginHBox);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(beginHBox);
        makeMovable(labelHBox);

        // bind begin's location to the model
        translateXProperty().bindBidirectional(beginSceneViewModel.xProperty());
        translateYProperty().bindBidirectional(beginSceneViewModel.yProperty());

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , beginSceneViewModel.getBegin(), null
                    , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                    + (outPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                    + (outPort.getBoundsInLocal().getHeight() / 2)));
        });

        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null) {
                showHighlight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , beginSceneViewModel.getBegin(), interactivePane.getDestNode()
                        , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (outPort.getBoundsInLocal().getHeight() / 2)));
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !beginSceneViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHighlight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHighlight(false));

        // TODO: Consume the event to avoid the interactive pane from accepting it and deselect every node
        setOnMousePressed(Event::consume);
    }
}

package io.makerplayground.ui.canvas;

import io.makerplayground.ui.canvas.event.SceneEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public class InteractivePane extends ScrollPane {
    private final Pane content = new Pane();
    private final Group group = new Group();
    private DoubleProperty scale;

    public InteractivePane() {
        // a pane to add content into
        content.setStyle("-fx-background-color: red");
        content.setPrefSize(1000, 1000);

        // wrap content in a group to scroll based on visual bounds according to ScrollPane's javadoc
        group.getChildren().add(content);
        setContent(group);

        scale = new SimpleDoubleProperty(1);
        // when scale value changed, we scale content and move scroll position to maintain center
        scale.addListener((observable, oldValue, newValue) -> {
            Point2D scrollOffset = figureScrollOffset(group, this);
            double oldScaleFactor = content.getScaleX();

            content.setScaleX(scale.get());
            content.setScaleY(scale.get());

            repositionScroller(group, this, scale.get() / oldScaleFactor, scrollOffset);
        });
    }

    // https://stackoverflow.com/questions/16680295/javafx-correct-scaling/16682180#16682180
    private Point2D figureScrollOffset(Node scrollContent, ScrollPane scroller) {
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);

        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);

        return new Point2D(scrollXOffset, scrollYOffset);
    }

    // https://stackoverflow.com/questions/16680295/javafx-correct-scaling/16682180#16682180
    private void repositionScroller(Node scrollContent, ScrollPane scroller, double scaleFactor, Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }

    // listener attaches to every node to automatically move the scrollbar when it moves out of viewport
    private final EventHandler<SceneEvent> sceneMovedHandler =  (event) -> {
        double viewportWidth = getViewportBounds().getWidth();
        double viewportHeight = getViewportBounds().getHeight();
        double extraWidth = group.getLayoutBounds().getWidth() - viewportWidth;
        double extraHeight = group.getLayoutBounds().getHeight() - viewportHeight;

        double left = getHvalue() * extraWidth; // getViewportBounds().getMinX()
        double right = left + viewportWidth;
        double top = getVvalue() * extraHeight;
        double bottom = top + viewportHeight;

        Bounds newValue = event.getSource().getBoundsInParent();

        if (left > newValue.getMinX() * scale.get()) {
            setHvalue(newValue.getMinX() * scale.get() / extraWidth);
        } else if (right < newValue.getMaxX() * scale.get()) {
            setHvalue((newValue.getMaxX() * scale.get() - viewportWidth) / extraWidth);
        } else if (top > newValue.getMinY() * scale.get()) {
            setVvalue(newValue.getMinY() * scale.get() / extraHeight);
        } else if (bottom < newValue.getMaxY() * scale.get()) {
            setVvalue((newValue.getMaxY() * scale.get() - viewportHeight) / extraHeight);
        }
    };

    public void addChildren(Node n) {
        content.getChildren().add(n);
        // auto scroll when child leave current viewport
        n.addEventHandler(SceneEvent.SCENE_MOVED, sceneMovedHandler);
    }

    public void removeChildren(Node n) {
        content.getChildren().remove(n);
        n.removeEventHandler(SceneEvent.SCENE_MOVED, sceneMovedHandler);
    }

    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale.set(scale);
    }
}

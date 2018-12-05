package io.makerplayground.generator.diagram;

import io.makerplayground.project.Project;
import javafx.scene.layout.Pane;

public class WiringDiagram {

    public static Pane make(Project project) {
        Pane wiringDiagram;
        switch(project.getController().getWiringMethod()) {
            case GROVE:
            case WIRE_AND_BREADBOARD:
            case KIDBRIGHT:
                wiringDiagram = new WireAndBreadboardDiagram(project);
                break;
            case MP_HEXAGON:
                wiringDiagram = new MPHexagonDiagram(project);
                break;
            case MP_RECTANGLE:
                wiringDiagram = new MPRectangleDiagram(project);
                break;
            case MP_RECTANGLE_TINY:
                wiringDiagram = new MPRectangleTinyDiagram(project);
                break;
            default:
                throw new IllegalStateException("Wiring method not found");
        }
        return wiringDiagram;
    }
}

package io.makerplayground.generator.diagram;

import io.makerplayground.project.Project;
import javafx.scene.layout.Pane;

public class WiringDiagram {

    // TODO: this method and the wiring_method json field are deprecated in flavor of the unified connection diagram in the next release
    public static Pane make(Project project) {
        Pane wiringDiagram;
        switch(project.getController().getWiringMethod()) {
            case WIRE_AND_BREADBOARD:
            case KIDBRIGHT:
                wiringDiagram = new WireAndBreadboardDiagram(project);
                break;
            case MP:
            case INEX:
            case GROVE:
                wiringDiagram = new JSTDiagram(project);
                break;
            default:
                throw new IllegalStateException("Wiring method not found");
        }
        return wiringDiagram;
    }
}

package io.makerplayground.generator.diagram;

import io.makerplayground.helper.Platform;
import io.makerplayground.project.Project;
import javafx.scene.layout.Pane;

public class WiringDiagram {

    public static Pane make(Project project) {
        Pane wiringDiagram;
        Platform platform = project.getPlatform();
        if (platform == Platform.ARDUINO || platform == Platform.GROVE_ARDUINO || platform == Platform.ESP32) {
            wiringDiagram = new WireDiagram(project);
        } else if (platform == Platform.MP_ARDUINO || platform == Platform.MP_ESP32) {
            wiringDiagram = new MPArduinoDiagram(project);
        } else {
            throw new IllegalStateException("Found unsupported platform(" + platform + ")");
        }
        return wiringDiagram;
    }
}

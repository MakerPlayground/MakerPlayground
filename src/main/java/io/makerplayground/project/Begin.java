package io.makerplayground.project;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class Begin extends NodeElement{
    private String name;

    public Begin(Project project) {
        super(200,20,85, 70, project);
    }

    public Begin(String name, double top, double left, Project project) {
        super(top, left, 85, 70, project);
        this.name = name;
    }

    protected Begin(double top, double left, double width, double height, Project project) {
        super(top, left, width, height, project);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    @Override
    protected DiagramError checkError() {
        if (name == null || name.isBlank()) {
            return DiagramError.BEGIN_INVALID_NAME;
        }
        return DiagramError.NONE;
    }

    public IntegerBinding getBeginCountBinding() {
        return Bindings.size(project.getBegin());
    }
}

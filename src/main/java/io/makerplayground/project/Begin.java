package io.makerplayground.project;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class Begin extends NodeElement{
    public Begin(Project project) {
        super(200,20,85, 50, project);
    }

    public Begin(double top, double left, Project project) {
        super(top, left, 85, 50, project);
    }

    @Override
    protected DiagramError checkError() {
        return DiagramError.NONE;
    }
}

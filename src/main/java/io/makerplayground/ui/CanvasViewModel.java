package io.makerplayground.ui;

import io.makerplayground.project.DiagramState;
import io.makerplayground.project.Project;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.ViewModelFactory;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {

    protected final Project project;
    private final DynamicViewModelCreator<DiagramState, StateViewModel> paneStateViewModel;


    public CanvasViewModel(Project project) {
        this.project = project;
        this.paneStateViewModel = new DynamicViewModelCreator<DiagramState, StateViewModel>(project.getObservableDiagram(), new ViewModelFactory<DiagramState, StateViewModel>() {
            @Override
            public StateViewModel newInstance(DiagramState diagramState) {
                return new StateViewModel(diagramState);
            }
        });
    }

    public DynamicViewModelCreator<DiagramState, StateViewModel> getPaneStateViewModel() {
        return paneStateViewModel;
    }
}

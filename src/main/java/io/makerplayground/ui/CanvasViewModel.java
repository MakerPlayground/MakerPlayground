package io.makerplayground.ui;

import io.makerplayground.project.State;
import io.makerplayground.project.Project;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.ViewModelFactory;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {

    protected final Project project;
    private final DynamicViewModelCreator<State, StateViewModel> paneStateViewModel;


    public CanvasViewModel(Project project) {
        this.project = project;
        this.paneStateViewModel = new DynamicViewModelCreator<State, StateViewModel>(project.getState(), new ViewModelFactory<State, StateViewModel>() {
            @Override
            public StateViewModel newInstance(State state) {
                return new StateViewModel(state);
            }
        });
    }

    public DynamicViewModelCreator<State, StateViewModel> getPaneStateViewModel() {
        return paneStateViewModel;
    }
}

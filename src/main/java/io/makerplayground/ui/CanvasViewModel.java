package io.makerplayground.ui;

import io.makerplayground.project.State;
import io.makerplayground.project.Project;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.DynamicViewModelCreatorBuilder;
import io.makerplayground.uihelper.ViewModelFactory;

/**
 *
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {

    protected final Project project;
    private final DynamicViewModelCreator<State, StateViewModel> paneStateViewModel;


    public CanvasViewModel(Project project) {
        this.project = project;
        this.paneStateViewModel = new DynamicViewModelCreatorBuilder<State, StateViewModel>()
                .setModel(project.getState())
                .setViewModelFactory(StateViewModel::new)
                .createDynamicViewModelCreator();
    }

    public DynamicViewModelCreator<State, StateViewModel> getPaneStateViewModel() {
        return paneStateViewModel;
    }
}

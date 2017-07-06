package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import io.makerplayground.project.Project;
import io.makerplayground.project.State;
import io.makerplayground.uihelper.DynamicViewModelCreator;

/**
 *
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasViewModel {
    protected final Project project;
    private final DynamicViewModelCreator<State, SceneViewModel> paneStateViewModel;
    private final DynamicViewModelCreator<Condition, ConditionViewModel> conditionViewModel;


    public CanvasViewModel(Project project) {
        this.project = project;
        this.paneStateViewModel = new DynamicViewModelCreator<>(project.getState(), state -> new SceneViewModel(state, project));
        this.conditionViewModel = new DynamicViewModelCreator<>(project.getCondition(), condition -> new ConditionViewModel(condition, project));
    }

    public void connectState(String state1, String state2) {
        State source = null;
        State dest = null;
        for(State s : project.getState()){
            if(s.getName().equalsIgnoreCase(state1)){
                dest = s;
            }
            if(s.getName().equalsIgnoreCase(state2)){
                source = s;
            }
        }
        // TODO: bug
        //project.addCondition(dest, source);
    }

    public DynamicViewModelCreator<Condition, ConditionViewModel> getConditionViewModel() {
        return conditionViewModel;
    }

    public DynamicViewModelCreator<State, SceneViewModel> getPaneStateViewModel() {
        return paneStateViewModel;
    }

    public Project getProject() {
        return project;
    }
}

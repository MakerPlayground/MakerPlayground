package io.makerplayground.ui.dialog.generate;

import io.makerplayground.generator.Sourcecode;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;
    private final Sourcecode code;
    private final ObservableList<TableDataList> observableTableList;

    public GenerateViewModel(Project project, Sourcecode code) {
        this.project = project;
        this.code = code;

        this.observableTableList = FXCollections.observableArrayList();
        for (ProjectDevice projectDevice : project.getAllDeviceUsed()) {
            observableTableList.add(new TableDataList(projectDevice));
        }
    }

    public Project getProject() {
        return project;
    }

    public String getCode() {
        return code.getCode();
    }

    public ObservableList<TableDataList> getObservableTableList() { return observableTableList; }
}

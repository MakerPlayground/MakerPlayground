package io.makerplayground.ui;

import io.makerplayground.generator.CodeGenerator;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.devicepanel.TableDataList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;
    private final String code;
    private final ObservableList<TableDataList> observableTableList;

    public GenerateViewModel(Project project) {
        this.project = project;
        this.code = CodeGenerator.generateCode(project);
        this.observableTableList = FXCollections.observableArrayList();

        for (ProjectDevice projectDevice : project.getAllDevice()) {
            observableTableList.add(new TableDataList(projectDevice));
        }
    }

    public Project getProject() {
        return project;
    }

    public String getCode() {
        return code;
    }

    public ObservableList<TableDataList> getObservableTableList() { return observableTableList; }
}

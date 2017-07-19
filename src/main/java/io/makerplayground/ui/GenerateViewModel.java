package io.makerplayground.ui;

import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;

    public GenerateViewModel(Project project) {
        this.project = project;
        DeviceMapper.autoAssignDevices(project);
    }

    public Project getProject() {
        return project;
    }
}

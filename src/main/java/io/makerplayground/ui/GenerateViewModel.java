package io.makerplayground.ui;

import io.makerplayground.generator.CodeGenerator;
import io.makerplayground.generator.DeviceMapper;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;
    private final String code;

    public GenerateViewModel(Project project) {
        this.project = project;
        this.code = CodeGenerator.generateCode(project);
    }

    public Project getProject() {
        return project;
    }

    public String getCode() {
        return code;
    }
}

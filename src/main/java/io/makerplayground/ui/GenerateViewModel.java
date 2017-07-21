package io.makerplayground.ui;

import io.makerplayground.generator.Sourcecode;
import io.makerplayground.project.Project;

import javax.xml.transform.Source;

/**
 * Created by tanyagorn on 7/19/2017.
 */
public class GenerateViewModel {
    private final Project project;
    private final Sourcecode code;

    public GenerateViewModel(Project project, Sourcecode code) {
        this.project = project;
        this.code = code;
    }

    public Project getProject() {
        return project;
    }

    public String getCode() {
        return code.getCode();
    }
}

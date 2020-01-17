package io.makerplayground.ui;

import io.makerplayground.generator.source.SourceCodeResult;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import javafx.concurrent.Task;

public abstract class ProjectExportTask extends Task<ExportResult> {

    protected final Project project;
    protected final SourceCodeResult sourcecode;
    protected final ProjectConfiguration configuration;
    protected final String zipFilePath;
    protected ExportResult exportResult;

    public ProjectExportTask(Project project, SourceCodeResult sourcecode, String zipFilePath) {
        this.project = project;
        this.sourcecode = sourcecode;
        this.configuration = project.getProjectConfiguration();
        this.zipFilePath = zipFilePath;
    }

    public ExportResult getExportResult() {
        return exportResult;
    }
}

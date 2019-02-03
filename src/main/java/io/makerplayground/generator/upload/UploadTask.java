package io.makerplayground.generator.upload;

import io.makerplayground.project.Project;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;

public abstract class UploadTask extends Task<UploadResult> {

    protected final Project project;
    protected final ReadOnlyStringWrapper log;

    public UploadTask(Project project) {
        this.project = project;
        this.log = new ReadOnlyStringWrapper();
    }

    @Override
    abstract protected UploadResult call();

    public ReadOnlyStringProperty logProperty() {
        return log.getReadOnlyProperty();
    }
}

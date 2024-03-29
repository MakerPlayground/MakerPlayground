/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.upload;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectConfiguration;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;

public abstract class UploadTask extends Task<UploadResult> {

    protected final Project project;
    protected final ProjectConfiguration configuration;
    protected final ReadOnlyStringWrapper log;
    protected final ReadOnlyStringWrapper fullLog;
    protected final StringBuilder logBuilder;

    protected final ObjectProperty<UploadStatus> uploadStatus;

    protected final UploadTarget uploadTarget;
    protected final boolean interactiveUpload;

    protected UploadTask(Project project, UploadTarget uploadTarget, boolean isInteractiveUpload) {
        this.project = project;
        this.configuration = project.getProjectConfiguration();
        this.log = new ReadOnlyStringWrapper();
        this.fullLog = new ReadOnlyStringWrapper();
        this.logBuilder = new StringBuilder();
        this.log.getReadOnlyProperty().addListener((observable, oldValue, newValue) -> {
            logBuilder.append(newValue);
            fullLog.set(logBuilder.toString());
        });
        this.uploadStatus = new SimpleObjectProperty<>(UploadStatus.IDLE);
        this.uploadTarget = uploadTarget;
        this.interactiveUpload = isInteractiveUpload;
    }

    @Override
    protected UploadResult call() {
        Platform.runLater(() -> uploadStatus.set(interactiveUpload
                        ? UploadStatus.STARTING_INTERACTIVE
                        : UploadStatus.UPLOADING));
        try {
            return doUpload();
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> uploadStatus.set(UploadStatus.UPLOAD_FAILED));
            return UploadResult.UNKNOWN_ERROR;
        }
    }

    abstract protected UploadResult doUpload();

    @Override
    protected void succeeded() {
        Platform.runLater(() -> uploadStatus.set(
                UploadResult.OK.equals(this.getValue())
                        ? UploadStatus.UPLOAD_DONE
                        : UploadStatus.UPLOAD_FAILED
        ));
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        Platform.runLater(() -> uploadStatus.set(UploadStatus.UPLOAD_FAILED));
    }

    public ReadOnlyStringWrapper fullLogProperty() {
        return fullLog;
    }

    public Project getProject() {
        return project;
    }

    public UploadTarget getUploadTarget() {
        return uploadTarget;
    }

    public ObjectProperty<UploadStatus> uploadStatusProperty() {
        return uploadStatus;
    }

    public boolean isInteractiveUpload() {
        return interactiveUpload;
    }

    public static UploadTask createCodeUploadTask(Project project, UploadTarget uploadTarget, boolean isInteractive) {
        switch (project.getSelectedPlatform()) {
            case ARDUINO_AVR8:
            case ARDUINO_ESP32:
            case ARDUINO_ESP8266:
            case ARDUINO_ATSAMD21:
            case ARDUINO_ATSAMD51:
            case ARDUINO_K210:
                if (project.getSelectedPlatform().getSupportUploadModes().contains(UploadMode.SERIAL_PORT)) {
                    return new ArduinoUploadTask(project, uploadTarget, isInteractive);
                }
                break;
            case RASPBERRYPI:
                if (project.getSelectedPlatform().getSupportUploadModes().contains(UploadMode.RPI_ON_NETWORK)) {
                    return new RaspberryPiUploadTask(project, uploadTarget, isInteractive);
                }
                break;
            case MICROPYTHON_ESP32:
            case MICROPYTHON_K210:
                if (project.getSelectedPlatform().getSupportUploadModes().contains(UploadMode.SERIAL_PORT)) {
                    return new MicroPythonUploadTask(project, uploadTarget, isInteractive);
                }
                break;
        }
        throw new IllegalStateException("No upload method for current platform");
    }

    public static UploadTask createFirmwareFlashTask(Project project, UploadTarget uploadTarget) {
        switch (project.getSelectedPlatform()) {
            case MICROPYTHON_ESP32:
                if (project.getSelectedPlatform().getSupportUploadModes().contains(UploadMode.SERIAL_PORT)) {
                    return new ESP32MicroPythonFirmwareFlashTask(project, uploadTarget);
                }
                break;
            case MICROPYTHON_K210:
                if (project.getSelectedPlatform().getSupportUploadModes().contains(UploadMode.SERIAL_PORT)) {
                    return new K210MicroPythonFirmwareFlashTask(project, uploadTarget);
                }
                break;
        }
        throw new IllegalStateException("No upload method for current platform");
    }
}

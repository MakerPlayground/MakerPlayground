package io.makerplayground.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.project.Project;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class AutoProjectBackupThread extends Thread {
    private volatile boolean shouldStop = false;
    private final ObjectProperty<Project> project;
    private static final long autoSaveInterval = 45000;
    private static final int maxBackupFiles = 5;

    public AutoProjectBackupThread(ObjectProperty<Project> project) {
        this.project = project;
    }

    public void stopThread() {
        shouldStop = true;
        interrupt();
    }

    private boolean hasUnsavedModification() {
        ObjectMapper mapper = new ObjectMapper();
        String newContent;
        try {
            newContent = mapper.writeValueAsString(project.get());
        } catch (JsonProcessingException e) {
            return true;
        }

        Optional<Path> path = AutoProjectBackupUtil.getLatestBackupFilePath();
        if (path.isEmpty()) {
            return true;
        }

        String oldContent;
        try {
            oldContent = new String(Files.readAllBytes(path.get()));
        } catch (IOException e) {
            return true;
        }

        return !oldContent.equals(newContent);
    }

    @Override
    public void run() {
        while (!shouldStop) {
            if (hasUnsavedModification()) {
                // delete old backup in a rolling manner
                try {
                    List<Path> backupFiles = AutoProjectBackupUtil.getBackupFilePaths();
                    if (backupFiles.size() >= maxBackupFiles) {
                        for (int i=0; i<backupFiles.size()-maxBackupFiles+1; i++) {
                            Files.delete(backupFiles.get(i));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // do nothing as auto project backup doesn't affect the main functionality
                }

                // create new backup
                try {
                    Path backupPath = AutoProjectBackupUtil.newBackupFilePath();
                    Files.createDirectories(backupPath.getParent());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(backupPath.toFile(), project.get());
                } catch (Exception e) {
                    e.printStackTrace();
                    // do nothing as auto project backup doesn't affect the main functionality
                }
            }

            try {
                Thread.sleep(autoSaveInterval);
            } catch (InterruptedException e) {
                // do nothing and exit
            }
        }
    }
}

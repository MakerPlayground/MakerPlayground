package io.makerplayground.ui;

import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileOpenHelper {
    private static final FileOpenHelper INSTANCE = new FileOpenHelper();
    private final List<String> files = Collections.synchronizedList(new ArrayList<>());
    private Runnable callback;

    private FileOpenHelper() {
    }

    public static FileOpenHelper getInstance() {
        return INSTANCE;
    }

    public OpenFilesHandler getOpenFilesHandler() {
        return new FileOpenEventHandler();
    }

    public List<String> getAndClearFiles() {
        synchronized (files) {
            List<String> result = new ArrayList<>(files);
            files.clear();
            return result;
        }
    }

    public void setCallback(Runnable r) {
        callback = r;
    }

    private class FileOpenEventHandler implements OpenFilesHandler {
        @Override
        public void openFiles(OpenFilesEvent e) {
            for (File file : e.getFiles()) {
                files.add(file.getAbsolutePath());
            }
            if (callback != null) {
                callback.run();
            }
        }
    }
}

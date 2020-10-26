package io.makerplayground.ui;

import io.makerplayground.util.OSInfo;

import java.awt.*;

/*
 * Minimal launcher class to catch the initial FILE_OPEN events on macOS,
 * before the application is set up
 * Adapt from: https://github.com/eschmar/javafx-custom-file-ext-boilerplate
 */
public class Launcher {
    static {
        if (OSInfo.getOs() == OSInfo.OS.MAC) {
            Desktop.getDesktop().setOpenFileHandler(FileOpenHelper.getInstance().getOpenFilesHandler());
        }
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}

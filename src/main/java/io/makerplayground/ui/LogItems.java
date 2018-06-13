package io.makerplayground.ui;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogItems {
    enum LogLevel {VERBOSE,INFO,DEBUG,WARNING,ERROR}

    private final LogLevel level;
    private final String tag;
    private final String message;

    public LogItems(String level, String tag, String message) {
        this.level = LogLevel.valueOf(level);
        this.tag = tag;
        this.message = message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }


}

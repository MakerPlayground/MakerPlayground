package io.makerplayground.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AutoProjectBackupUtil {
    private static final Path backupDirectory = Path.of(PathUtility.MP_WORKSPACE, "autobackup");

    public static Path newBackupFilePath() {
        return Path.of(backupDirectory.toString(), "backup_" + Instant.now().getEpochSecond() + ".mp");
    }

    public static List<Path> getBackupFilePaths() {
        try (Stream<Path> paths = Files.list(backupDirectory)) {
            return paths.filter(p -> p.toString().endsWith(".mp")).sorted().toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static Optional<Path> getLatestBackupFilePath() {
        List<Path> paths = getBackupFilePaths();
        if (paths.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(paths.get(paths.size() - 1));
        }
    }

    public static void deleteAllBackupFile() {
        try {
            for (Path p : getBackupFilePaths()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // do nothing as auto project backup doesn't affect the main functionality
        }
    }
}

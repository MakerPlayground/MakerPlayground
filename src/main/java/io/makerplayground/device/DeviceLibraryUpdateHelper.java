package io.makerplayground.device;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.util.PathUtility;
import io.makerplayground.version.ComparableVersion;
import io.makerplayground.version.DeviceLibraryVersion;
import io.makerplayground.version.SoftwareVersion;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

public class DeviceLibraryUpdateHelper {
    public static final String VERSION_CHECKING_URL = "https://makerplayground.z23.web.core.windows.net/library/version.json";
    public static final String LIB_DOWNLOAD_BASEURL = "https://makerplayground.blob.core.windows.net/release/library/";
    private static DeviceLibraryVersion lastestCompatibleVersion;

    public static Optional<DeviceLibraryVersion> getVersionOfLibraryAtPath(String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            DeviceLibraryVersion version = mapper.readValue(new File(path + "/version.json"), DeviceLibraryVersion.class);
            return Optional.of(version);
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static void fetchLatestCompatibleVersion(Runnable runnable) {
        new Thread(() -> {
            List<DeviceLibraryVersion> libraryVersions = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                libraryVersions = mapper.readValue(new URL(VERSION_CHECKING_URL), new TypeReference<List<DeviceLibraryVersion>>(){});
            } catch (UnknownHostException | ConnectException e) {
                // exception can normally be thrown when there is no internet connectivity
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (libraryVersions != null) {
                ComparableVersion currentMPVersion = new ComparableVersion(SoftwareVersion.getCurrentVersion().getVersionString());
                for (var version : libraryVersions) {
                    if (currentMPVersion.compareTo(new ComparableVersion(version.getMinimumMPVersion())) >= 0) {
                        lastestCompatibleVersion = version;
                        break;
                    }
                }
            }

            Platform.runLater(runnable);
        }).start();
    }

    public static Optional<DeviceLibraryVersion> getLatestCompatibleVersion() {
        return Optional.ofNullable(lastestCompatibleVersion);
    }

    public static Task<Void> launchDownloadUpdateFileTask() {
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                updateProgress(0, 1);
                Optional<DeviceLibraryVersion> latestVersion = getLatestCompatibleVersion();
                if (latestVersion.isPresent()) {
                    try {
                        String filePath = getUpdateFilePath().get();
                        URL url = new URL(latestVersion.get().getDownloadURL());
                        URLConnection connection = url.openConnection();
                        long contentLength = connection.getContentLengthLong();
                        try (InputStream input = new BufferedInputStream(connection.getInputStream());
                             OutputStream output = FileUtils.openOutputStream(new File(filePath), false)) {
                            byte[] buffer = new byte[4096];
                            long count = 0;
                            int n;
                            updateProgress(count, contentLength);
                            while (IOUtils.EOF != (n = input.read(buffer))) {
                                output.write(buffer, 0, n);
                                count += n;
                                updateProgress(count, contentLength);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        new Thread(downloadTask).start();
        return downloadTask;
    }

    public static Optional<String> getUpdateFilePath() {
        return getLatestCompatibleVersion().map((latestVersion) -> PathUtility.MP_WORKSPACE + "/" + latestVersion.getVersion() + ".zip");
    }

    public static boolean isUpdateFileAvailable() {
        Optional<DeviceLibraryVersion> latestVersion = getLatestCompatibleVersion();
        if (latestVersion.isPresent()) {
            String filePath = getUpdateFilePath().get();
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
            try {
                String checksum = new DigestUtils(MessageDigestAlgorithms.SHA_256).digestAsHex(file);
                return checksum.equals(latestVersion.get().getChecksum());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}

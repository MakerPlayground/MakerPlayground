package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.util.PathUtility;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DeviceLibraryVersion {
    private static DeviceLibraryVersion currentVersion;
    private static DeviceLibraryVersion lastestCompatibleVersion;
    private static final String VERSION_CHECKING_URL = "https://makerplayground.z23.web.core.windows.net/library/version.json";
    private static final String LIB_DOWNLOAD_BASEURL = "https://makerplayground.blob.core.windows.net/release/library/";

    static {
        // get local library version
        reloadCurrentVersion();
    }

    public static void reloadCurrentVersion() {
        ObjectMapper mapper = new ObjectMapper();
        if (DeviceLibrary.getLibraryPath().isPresent()) {
            try {
                currentVersion = mapper.readValue(new File(DeviceLibrary.getLibraryPath().get() + "/version.json"), DeviceLibraryVersion.class);
            } catch (Exception e) {
                System.err.println("Can't open version file at " + DeviceLibrary.getLibraryPath().get() + "/version.json");
            }
        } else {
            System.err.println("Library directory could not be found");
        }
    }

    public static Optional<DeviceLibraryVersion> getCurrentVersion() {
        return Optional.ofNullable(currentVersion);
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
                    if (currentMPVersion.compareTo(new ComparableVersion(version.minimumMPVersion)) >= 0) {
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
                Optional<DeviceLibraryVersion> latestVersion = getLatestCompatibleVersion();
                if (latestVersion.isPresent()) {
                    String filePath = PathUtility.MP_WORKSPACE + "/" + latestVersion.get().getVersion() + ".zip";
                    try {
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

    public static boolean isUpdateFileAvailable() {
        Optional<DeviceLibraryVersion> latestVersion = DeviceLibraryVersion.getLatestCompatibleVersion();
        if (latestVersion.isPresent()) {
            String filePath = PathUtility.MP_WORKSPACE + "/" + latestVersion.get().getVersion() + ".zip";
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

    private final String version;
    private final String minimumMPVersion;
    private final Date releaseDate;
    private final String checksum;

    private DeviceLibraryVersion(@JsonProperty("version") String version, @JsonProperty("min_mp_version") String minimumMPVersion,
                                 @JsonProperty("release-date") Date releaseDate, @JsonProperty("sha256") String checksum) {
        this.version = version;
        this.minimumMPVersion = minimumMPVersion;
        this.releaseDate = releaseDate;
        this.checksum = checksum;
    }

    public String getVersion() {
        return version;
    }

    public String getMinimumMPVersion() {
        return minimumMPVersion;
    }

    public String getDownloadURL() {
        return LIB_DOWNLOAD_BASEURL + version + ".zip";
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getChecksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return "DeviceLibraryVersion{" +
                "version='" + version + '\'' +
                ", minimumMPVersion='" + minimumMPVersion + '\'' +
                ", releaseDate=" + releaseDate +
                '}';
    }
}

package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.makerplayground.device.DeviceLibraryUpdateHelper;

import java.util.Date;

public class DeviceLibraryVersion {
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
        return DeviceLibraryUpdateHelper.LIB_DOWNLOAD_BASEURL + version + ".zip";
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

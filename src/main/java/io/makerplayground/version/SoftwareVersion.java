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

package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.util.OSInfo;
import javafx.scene.control.Alert;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class SoftwareVersion implements Comparable<SoftwareVersion> {
    private static SoftwareVersion currentVersion;
    private static final String URL = "https://makerplayground.z23.web.core.windows.net/version";

    public static SoftwareVersion getCurrentVersion() {
        if (currentVersion == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(SoftwareVersion.class.getResourceAsStream("/version.json")))) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    currentVersion = mapper.readValue(reader, SoftwareVersion.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // it may be better to return an optional and alert user outside of this method but this error should
                // only happen during development so it is ok
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Can't read version information from version.json");
                alert.showAndWait();
            }
        }

        return currentVersion;
    }

    public static Optional<SoftwareVersion> getLatestVersionInfo() {
        SoftwareVersion latestVersion = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            latestVersion = mapper.readValue(new URL(URL + "/software_" + getCurrentVersion().getChannel() + ".json"), SoftwareVersion.class);
        } catch (UnknownHostException | SSLException | SocketException e) {
            // exception can normally be thrown when there is no internet connectivity
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(latestVersion);
    }

    private enum Platform { windows, @JsonProperty("windows-full") windows_full, linux, macos, @JsonProperty("macos-full") macos_full }
    private enum Channel {stable, nightly, internal}

    private String buildName;
    private String version;
    private Channel channel;
    private Map<Platform, DownloadInfo> downloadInfo;
    private Date releaseDate;

    @JsonCreator
    private SoftwareVersion(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version, @JsonProperty("channel") Channel channel
            , @JsonProperty("download_url") Map<Platform, DownloadInfo> downloadInfo, @JsonProperty("release_date") java.util.Date releaseDate) {
        this.buildName = buildName;
        this.version = version;
        this.channel = channel;
        this.downloadInfo = downloadInfo;
        this.releaseDate = releaseDate;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getVersionString() {
        return version;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getDownloadURL() {
        if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
            return downloadInfo.get(Platform.windows).getUrl();
        } else if (OSInfo.getOs() == OSInfo.OS.MAC) {
            return downloadInfo.get(Platform.macos).getUrl();
        } else {
            return "https://www.makerplayground.io";
        }
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public int compareTo(SoftwareVersion o) {
        return new ComparableVersion(version).compareTo(new ComparableVersion(o.version));
    }

    public static class DownloadInfo {
        private final String url;
        private final String checksum;

        @JsonCreator
        public DownloadInfo(@JsonProperty("url") String url, @JsonProperty("checksum") String checksum) {
            this.url = url;
            this.checksum = checksum;
        }

        public String getUrl() {
            return url;
        }

        public String getChecksum() {
            return checksum;
        }
    }
}

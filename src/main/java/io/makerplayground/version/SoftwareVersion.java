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
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;

public class SoftwareVersion implements Comparable<SoftwareVersion> {
    private static SoftwareVersion currentVersion;
    private static final String URL = "https://makerplayground.z23.web.core.windows.net/version";

    public static SoftwareVersion getCurrentVersion() {
        if (currentVersion == null) {
            File file = new File("version.json");
            if (!file.exists()) {
                // it may be better to return an optional and alert user outside of this method but this error should
                // only happen during development so it is ok
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Can't read version information from " + file.getAbsolutePath());
                alert.showAndWait();
            } else {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    currentVersion = mapper.readValue(file, SoftwareVersion.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return currentVersion;
    }

    public static Optional<SoftwareVersion> getLatestVersionInfo() {
        SoftwareVersion latestVersion = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            latestVersion = mapper.readValue(new URL(URL + "/software_" + getCurrentVersion().getChannel() + ".json"), SoftwareVersion.class);
        } catch (UnknownHostException|ConnectException e) {
            // exception can normally be thrown when there is no internet connectivity
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(latestVersion);
    }

    private enum Channel {stable, nightly, internal}

    private String buildName;
    private String version;
    private Channel channel;
    private String downloadURL;
    private Date releaseDate;

    @JsonCreator
    private SoftwareVersion(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version, @JsonProperty("channel") Channel channel
            , @JsonProperty("download_url") String downloadURL, @JsonProperty("release_date") java.util.Date releaseDate) {
        this.buildName = buildName;
        this.version = version;
        this.channel = channel;
        this.downloadURL = downloadURL;
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
        return downloadURL;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public int compareTo(SoftwareVersion o) {
        return new ComparableVersion(version).compareTo(new ComparableVersion(o.version));
    }
}

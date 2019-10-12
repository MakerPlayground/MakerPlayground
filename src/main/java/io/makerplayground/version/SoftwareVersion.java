/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class SoftwareVersion implements Comparable<SoftwareVersion> {
    private static SoftwareVersion currentVersion;
    private static final String URL = "http://mprepo.azurewebsites.net/current_version"; // or "http://mprepo.azurewebsites.net/devtest/current_version"

    public static SoftwareVersion getCurrentVersion() {
        if (currentVersion == null) {
            try {
                Path path = Paths.get("version.txt");
                // TODO: it may be better to return an optional and alert user outside of this method but this error is rarely happen so it is ok
                if (!Files.exists(path)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Can't read version information from " + path.toAbsolutePath().toString());
                    alert.showAndWait();
                } else {
                    List<String> versionFile = Files.readAllLines(Paths.get("version.txt"));
                    if (versionFile.size() == 2) {
                        String[] version = versionFile.get(0).split("=");
                        String[] releaseDate = versionFile.get(1).split("=");
                        if (version.length == 2 && version[0].equals("version")
                                && releaseDate.length == 2 && releaseDate[0].equals("release-date")) {
                            currentVersion = new SoftwareVersion("Maker Playground " + version[1], version[1], "http://makerplayground.io"
                                    , new Date(LocalDate.parse(releaseDate[1], DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay()));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return currentVersion;
    }

    public static Optional<SoftwareVersion> getLatestVersionInfo() {
        SoftwareVersion latestVersion = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            latestVersion = mapper.readValue(new URL(URL), SoftwareVersion.class);
        } catch (UnknownHostException|ConnectException e) {
            // exception can normally be thrown when there is no internet connectivity
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(latestVersion);
    }

    private String buildName;
    private String version;
    private String downloadURL;
    private Date releaseDate;

    @JsonCreator
    private SoftwareVersion(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version
            , @JsonProperty("download_url") String downloadURL, @JsonProperty("release_date") java.util.Date releaseDate) {
        this.buildName = buildName;
        this.version = version;
        this.downloadURL = downloadURL;
        this.releaseDate = releaseDate;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getVersionString() {
        return version;
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

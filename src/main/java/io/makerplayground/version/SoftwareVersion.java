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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.makerplayground.util.OSInfo;
import javafx.scene.control.Alert;
import lombok.Getter;
import org.semver4j.Semver;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class SoftwareVersion implements Comparable<SoftwareVersion> {
    private static SoftwareVersion currentVersion;
    private static final String URL = "https://api.github.com/repos/MakerPlayground/MakerPlayground/releases";

    public static SoftwareVersion getCurrentVersion() {
        if (currentVersion == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(SoftwareVersion.class.getResourceAsStream("/version.json")))) {
                ObjectMapper mapper = new ObjectMapper();
                currentVersion = mapper.readValue(reader, SoftwareVersion.class);
                currentVersion.channel = currentVersion.version.isStable() ? Channel.stable : Channel.nightly;
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
            JsonNode root = mapper.readTree(new URL(URL));
            boolean allowPrerelease = getCurrentVersion().getChannel() != Channel.stable;
            if (root.isArray()) {
                // find the latest release based on the prerelease field and our current release channel
                for (int i = 0; i < root.size(); i++) {
                    JsonNode n = root.get(i);
                    if (n.hasNonNull("prerelease") && n.get("prerelease").asBoolean() == allowPrerelease) {
                        try {
                            String version = n.get("tag_name").asText();
                            Channel channel = allowPrerelease ? Channel.stable : Channel.nightly;
                            Date releaseDate = Date.from(Instant.parse(n.get("published_at").asText()));
                            latestVersion = new SoftwareVersion("Maker Playground " + version, version, channel, releaseDate);
                        } catch (Exception e) {
                            continue;
                        }
                        break;
                    }
                }
            }
        } catch (UnknownHostException | SSLException | SocketException e) {
            // exception can normally be thrown when there is no internet connectivity
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(latestVersion.versionString);
        return Optional.ofNullable(latestVersion);
    }

    private enum Channel {stable, nightly, internal}

    @Getter
    private String buildName;
    @Getter
    private String versionString;
    @Getter
    private Semver version;
    @Getter
    private Channel channel;
    @Getter
    private Date releaseDate;

    @JsonCreator
    private SoftwareVersion(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version
            , @JsonProperty("channel") Channel channel, @JsonProperty("release_date") java.util.Date releaseDate) {
        this.buildName = buildName;
        this.versionString = version;
        this.version = version.startsWith("v") ? Semver.parse(version.substring(1)) : Semver.parse(version);
        Objects.requireNonNull(this.version);
        this.channel = channel;
        this.releaseDate = releaseDate;
    }

    public String getDownloadURL() {
        if (OSInfo.getOs() == OSInfo.OS.WINDOWS) {
            return "https://github.com/MakerPlayground/MakerPlayground/releases/download/" + versionString + "/MakerPlayground-" + versionString + ".exe";
        } else if (OSInfo.getOs() == OSInfo.OS.MAC) {
            return "https://github.com/MakerPlayground/MakerPlayground/releases/download/" + versionString + "/MakerPlayground-" + versionString + ".app.zip";
        } else if (OSInfo.getOs() == OSInfo.OS.UNIX) {
            return "https://github.com/MakerPlayground/MakerPlayground/releases/download/" + versionString + "/MakerPlayground-" + versionString + ".AppImage";
        } else {
            return "https://www.makerplayground.io";
        }
    }

    @Override
    public int compareTo(SoftwareVersion o) {
        return version.compareTo(o.version);
    }
}

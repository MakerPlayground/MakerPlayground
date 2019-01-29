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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;

public class SoftwareVersion implements Comparable<SoftwareVersion> {
    public static final SoftwareVersion CURRENT_VERSION = new SoftwareVersion("Maker Playground v0.4.0-beta4", "0.4.0-beta4"
            , "http://makerplayground.io", new Date(1548755398)); // Jan 29, 2019
    private static final String URL = "http://mprepo.azurewebsites.net/current_version"; // or "http://mprepo.azurewebsites.net/devtest/current_version"

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

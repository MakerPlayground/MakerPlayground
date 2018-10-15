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

public class SoftwareVersionControl {
    public static final String CURRENT_BUILD_NAME = "Maker Playground v0.3.0-beta1";
    public static final String CURRENT_VERSION = "0.3.0-beta1";

    private static final String URL = "http://mprepo.azurewebsites.net/current_version"; // or "http://mprepo.azurewebsites.net/devtest/current_version"

    public static Optional<SoftwareVersionControl> getLatestVersionInfo() {
        SoftwareVersionControl latestVersion = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            latestVersion = mapper.readValue(new URL(URL), SoftwareVersionControl.class);
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
    private SoftwareVersionControl(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version
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
}

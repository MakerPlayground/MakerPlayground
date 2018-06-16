package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Optional;

public class Version {
    public static final String CURRENT_BUILD_NAME = "Maker Playground v0.2.2";
    public static final String CURRENT_VERSION = "0.2.2";

    private static final String URL = "http://mprepo.azurewebsites.net/current_version"; // or "http://mprepo.azurewebsites.net/devtest/current_version"

    public static Optional<Version> getLatestVersionInfo() {
        Version latestVersion = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            latestVersion = mapper.readValue(new URL(URL), Version.class);
        } catch (UnknownHostException e) {
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
    private Version(@JsonProperty("build_name") String buildName, @JsonProperty("version") String version
            , @JsonProperty("download_url") String downloadURL, @JsonProperty("release_date") Date releaseDate) {
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

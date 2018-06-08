package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Version {
    public static final String CURRENT_BUILD_NAME = "Maker Playground v0.2";
    public static final String CURRENT_VERSION = "0.2";
    private static URL newest_version_URL;
    static {
        try {
            newest_version_URL = new URL("http://mprepo.azurewebsites.net/current_version");
//            newest_version_URL = new URL("http://mprepo.azurewebsites.net/devtest/current_version");
            version_obj = getInstance();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @JsonIgnore
    private static Version version_obj;

    private String build_name;
    private String version;
    private String download_url;
    private Date release_date;

    public static boolean isCurrentVersion() {
        if (version_obj == null) {
            return true;
        }
        return CURRENT_VERSION.equals(version_obj.version);
    }

    private static Version getInstance() {
        try {
            if(version_obj == null) {
                ObjectMapper mapper = new ObjectMapper();
                version_obj = mapper.readValue(newest_version_URL, Version.class);
                return version_obj;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setVersion(String version) {
        this.version = version;
    }

    private void setBuild_name(String build_name) {
        this.build_name = build_name;
    }

    private void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public static String getDownload_url() {
        if (version_obj == null) {
            return version_obj.download_url;
        }
        return version_obj.download_url;
    }

    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }
}

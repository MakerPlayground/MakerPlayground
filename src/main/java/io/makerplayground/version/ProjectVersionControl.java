package io.makerplayground.version;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ProjectVersionControl {

    public static final String CURRENT_VERSION = "0.3";

    public static boolean isConvertibleToCurrentVersion(String projectVersion) {
        if (CURRENT_VERSION.equals(projectVersion)) {
            return true;
        }
        else if ("0.2".equals(projectVersion)) {
            return false;
        }
        return false;
    }

    public static String readProjectVersion(File selectedFile) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(selectedFile);
            return node.get("projectVersion").asText("0.2");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void convertToCurrentVersion(File selectedFile) {
        throw new UnsupportedOperationException("Implementation needed");
    }
}

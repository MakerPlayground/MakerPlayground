package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.helper.ConnectionType;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.helper.Platform;
import io.makerplayground.project.*;
import io.makerplayground.project.expression.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Sourcecode {

    public enum Error {
        NONE(""),
        SCENE_ERROR("Missing required parameter in some scenes"),
        MISSING_PROPERTY("Missing required device's property"),
        NOT_FOUND_SCENE_OR_CONDITION("Can't find any scene or condition connect to the begin node"),
        MULT_DIRECT_CONN_TO_SCENE("Found multiple direct connection to the same scene"),
        NEST_CONDITION("Multiple condition are connected together"),
        SHORT_CIRCUIT("Some conditions are not reachable"),
        CONDITION_ERROR("Missing required parameter in some conditions"),
        MORE_THAN_ONE_CLOUD_PLATFORM("Only one cloud platform (e.g. Blynk or NETPIE) is allowed"),
        NOT_SELECT_DEVICE_OR_PORT("Some devices and/or their ports haven't been selected");

        private final String description;

        Error(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private String code;
    private Error error;
    private String location;

    Sourcecode(String code) {
        this.code = code;
    }

    Sourcecode(Error error, String location) {
        this.error = error;
        this.location = location;
    }

    public String getCode() {
        return code;
    }

    public Error getError() {
        return error;
    }

    public String getLocation() {
        return location;
    }


}

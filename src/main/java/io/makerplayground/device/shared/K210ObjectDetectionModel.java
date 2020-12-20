package io.makerplayground.device.shared;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class K210ObjectDetectionModel {
    private final String name;
    private final String modelFilename;
    private final int address;
    private final String modelSize;
    private final String anchor;
    private final double threshold;
    private final double nmsThreshold;
    private final int numAnchor;
    private final String outputShape;
    private final List<String> className;

    @JsonCreator
    private K210ObjectDetectionModel(@JsonProperty("name") String name, @JsonProperty("model_filename") String modelFilename
            , @JsonProperty("address") int address, @JsonProperty("model_size") String modelSize
            , @JsonProperty("anchor") String anchor, @JsonProperty("threshold") double threshold
            , @JsonProperty("nms_threshold") double nmsThreshold, @JsonProperty("num_anchor") int numAnchor
            , @JsonProperty("output_shape") String outputShape, @JsonProperty("class") List<String> className) {
        this.name = name;
        this.modelFilename = modelFilename;
        this.address = address;
        this.modelSize = modelSize;
        this.anchor = anchor;
        this.threshold = threshold;
        this.nmsThreshold = nmsThreshold;
        this.numAnchor = numAnchor;
        this.outputShape = outputShape;
        this.className = className;
    }
}

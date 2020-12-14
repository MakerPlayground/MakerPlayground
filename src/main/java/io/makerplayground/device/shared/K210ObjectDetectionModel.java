package io.makerplayground.device.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class K210ObjectDetectionModel {
    String name;
    @JsonProperty("model_filename") String modelFilename;
    int address;
    @JsonProperty("model_size") String modelSize;
    String anchor;
    double threshold;
    @JsonProperty("nms_threshold") double nmsThreshold;
    @JsonProperty("num_anchor") int numAnchor;
    @JsonProperty("output_shape") String outputShape;
    @JsonProperty("class") List<String> className;
}

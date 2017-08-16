package io.makerplayground.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tutorial {
    private String topic;
    private String description;
    private String img;

    @JsonCreator
    Tutorial(@JsonProperty("topic") String topic,@JsonProperty("description") String description,@JsonProperty("img") String img){
        this.topic = topic;
        this.description = description;
        this.img = img;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }

    public String getImg() {
        return img;
    }

    @Override
    public String toString() {
        return "Tutorial{" +
                "topic='" + topic + '\'' +
                ", description='" + description + '\'' +
                ", img='" + img + '\'' +
                '}';
    }
}

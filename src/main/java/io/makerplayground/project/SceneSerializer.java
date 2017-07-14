package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by USER on 13-Jul-17.
 */
public class SceneSerializer extends StdSerializer<Scene> {
   public SceneSerializer() { this(null); }

   public SceneSerializer(Class<Scene> t) { super(t); }

   @Override
    public void serialize(Scene scene, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
       ObjectMapper mapper = new ObjectMapper();

       jsonGenerator.writeStartObject();

       jsonGenerator.writeStringField("name",scene.getName());

       jsonGenerator.writeArrayFieldStart("userSetting");
       for (UserSetting setting : scene.getSetting()) {
          mapper.writeValue(jsonGenerator, setting);
       }
       jsonGenerator.writeEndArray();

       jsonGenerator.writeObjectField("delay",scene.getDelay());
       jsonGenerator.writeObjectField("delayUnit",scene.getDelayUnit());

       jsonGenerator.writeObjectFieldStart("position");
       jsonGenerator.writeNumberField("top",scene.getTop());
       jsonGenerator.writeNumberField("left",scene.getLeft());
       jsonGenerator.writeNumberField("width",scene.getWidth());
       jsonGenerator.writeNumberField("height",scene.getHeight());
       jsonGenerator.writeNumberField("sourcePortX",scene.getSourcePortX());
       jsonGenerator.writeNumberField("sourcePortY",scene.getSourcePortY());
       jsonGenerator.writeNumberField("destPortX",scene.getDestPortX());
       jsonGenerator.writeNumberField("destPortY",scene.getDestPortY());
       jsonGenerator.writeEndObject();

       jsonGenerator.writeEndObject();
   }
}

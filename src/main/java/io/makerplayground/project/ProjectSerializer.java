package io.makerplayground.project;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Created by nuntipat on 6/25/2017 AD.
 */
public class ProjectSerializer extends StdSerializer<Project> {
    public ProjectSerializer() {
        this(null);
    }

    public ProjectSerializer(Class<Project> t) {
        super(t);
    }

    @Override
    public void serialize(Project project, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("projectName", project.getProjectName());

//        jsonGenerator.writeObjectFieldStart("processor");
//        jsonGenerator.writeStringField("name",project.getMicrocontroller().getName());
//        jsonGenerator.writeEndObject();

        jsonGenerator.writeArrayFieldStart("inputDevice");
        for(ProjectDevice inputDevice : project.getInputDevice()) {
            mapper.writeValue(jsonGenerator,inputDevice);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("outputDevice");
        for(ProjectDevice outputDevice : project.getOutputDevice()) {
            mapper.writeValue(jsonGenerator,outputDevice);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("scene");
        for(Scene scene : project.getScene()) {
            mapper.writeValue(jsonGenerator,scene);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("condition");
        for(Condition condition : project.getCondition()) {
            mapper.writeValue(jsonGenerator,condition);
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeArrayFieldStart("line");
        for(Line line : project.getLine()) {
            jsonGenerator.writeStartObject();

            if (line.getSource() instanceof Scene)
                jsonGenerator.writeStringField("source", ((Scene) line.getSource()).getName());
            else if (line.getSource() instanceof Condition)
                jsonGenerator.writeStringField("source", ((Condition) line.getSource()).getName());

            if (line.getDestination() instanceof Scene)
                jsonGenerator.writeStringField("destination", ((Scene) line.getSource()).getName());
            else if (line.getDestination() instanceof Condition)
                jsonGenerator.writeStringField("destination", ((Condition) line.getSource()).getName());

            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}

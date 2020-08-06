package io.makerplayground.project;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.ObservableList;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class DiagramClipboardDataDeserializer extends JsonDeserializer<DiagramClipboardData> {

    ObjectMapper mapper = new ObjectMapper();
    Project project;

    public DiagramClipboardDataDeserializer(Project project){
        this.project = project;

        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserSetting.class, new UserSettingDeserializer(project));
        module.addDeserializer(Scene.class, new SceneDeserializer(project));
        module.addDeserializer(Condition.class, new ConditionDeserializer(project));
        module.addDeserializer(Delay.class, new DelayDeserializer(project));
        module.addDeserializer(ProjectDevice.class, new ProjectDeviceDeserializer());
        mapper.registerModule(module);
    }

    @Override
    public DiagramClipboardData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode root = mapper.readTree(jsonParser);

        String copyProjectName = root.get("project").asText();

        addNewVariable(root);

        List<ProjectDevice> deviceList = mapper.readValue(root.get("devices").traverse(),new TypeReference<List<ProjectDevice>>() {});

        Map<String,String> reNameDevices = addNewDevice(deviceList,copyProjectName);
        root = modifiedDeviceNameJson(root,reNameDevices);

        List<Scene> sceneList = mapper.readValue(root.get("scenes").traverse(),new TypeReference<List<Scene>>() {});
        List<Condition> conditionList = mapper.readValue(root.get("conditions").traverse(),new TypeReference<List<Condition>>() {});
        List<Delay> delayList = mapper.readValue(root.get("delays").traverse(),new TypeReference<List<Delay>>() {});
        List<Line> lineList = new ArrayList<>();
        List<NodeElement> elements = new ArrayList<>();
        elements.addAll(sceneList);
        elements.addAll(conditionList);
        elements.addAll(delayList);
        for (JsonNode lines :root.get("lines")) {
            NodeElement source = null;
            NodeElement destination = null;

            for (NodeElement element : elements) {
                if (source == null) {
                    if (element.getName().equalsIgnoreCase(lines.get("source").get("name").asText())) {
                        source = element;
                    }
                }
                if (destination == null) {
                    if (element.getName().equalsIgnoreCase(lines.get("destination").get("name").asText())) {
                        destination = element;
                    }
                }
                if (destination != null & source != null) {
                    Line line = new Line(source,destination,project);
                    lineList.add(line);
                    break;
                }
            }
        }
        return new DiagramClipboardData(sceneList,conditionList,delayList,lineList,copyProjectName);
    }


    JsonNode modifiedDeviceNameJson(JsonNode jsonNode,  Map<String,String> newDevices) {
        for (String device : newDevices.keySet()) {

            for (JsonNode sceneNode : jsonNode.get("scenes") ) {
                for (JsonNode settingNode : sceneNode.get("setting")) {
                    String name = settingNode.get("device").asText();
                    if(name.equalsIgnoreCase(device)) {
                        ((ObjectNode)settingNode).put("device", newDevices.get(device));
                    }
                    else if (name.equalsIgnoreCase("Memory")) {
                        for (JsonNode valueMap : settingNode.get("valueMap")) {
                            if (valueMap.get("type").asText().equalsIgnoreCase("CustomNumberExpression")) {
                                for (JsonNode terms : valueMap.get("value").get("terms")) {
                                    if(terms.get("value").get("name").asText().equalsIgnoreCase(device)){
                                        ((ObjectNode)terms.get("value")).put("name", newDevices.get(device));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (JsonNode sceneNode : jsonNode.get("conditions") ) {
                for (JsonNode settingNode : sceneNode.get("setting")) {
                    String name = settingNode.get("device").asText();
                    if (name.equalsIgnoreCase(device)) {
                        ((ObjectNode)settingNode).put("device", newDevices.get(device));
                    }
                }
            }
        }
        return jsonNode;
    }

    Map<String,String> addNewDevice(List<ProjectDevice> deviceList, String projectName) {
        ObservableList<ProjectDevice> projectDeviceSet = project.getUnmodifiableProjectDevice();
        Map<String,String> projectDeviceMap = new HashMap<>();
        if (projectDeviceSet.isEmpty()) {
            for (ProjectDevice projectDevice : deviceList) {
                ProjectDevice newProjectDevice = new ProjectDevice(projectDevice.getName(),projectDevice.getGenericDevice());
                project.addDevice(newProjectDevice);
            }
        } else {
            List<String> projectDeviceName = project.getProjectDeviceName();

            for (ProjectDevice projectDevice : deviceList) {
                if (!projectDeviceName.contains(projectDevice.getName())) {
                    ProjectDevice newProjectDevice = new ProjectDevice(projectDevice.getName(),projectDevice.getGenericDevice());
                    project.addDevice(newProjectDevice);
                } else {
                    for(ProjectDevice pr : projectDeviceSet) {
                        if(projectDevice.getName().equalsIgnoreCase(pr.getName())) {
                            if(!projectDevice.getGenericDevice().getName().equalsIgnoreCase(pr.getGenericDevice().getName())) {
                                String name = projectDevice.getName()+"_"+projectName.replace(" ","_");
                                int occurrences = Collections.frequency(projectDeviceName, name);
                                while (projectDeviceName.contains(name+"_("+(occurrences)+")")) {
                                    occurrences ++;
                                }
                                if(occurrences != 0) {
                                    name = name + "_(" + (occurrences) + ")";
                                }
                                projectDevice.setName(name);
                                projectDeviceMap.put(pr.getName(),projectDevice.getName());
                                ProjectDevice newProjectDevice = new ProjectDevice(name,projectDevice.getGenericDevice());
                                project.addDevice(newProjectDevice);
                            }
                        }
                    }
                }
            }
        }

        return projectDeviceMap;
    }

    void addNewVariable(JsonNode node){
        for(JsonNode variable : node.get("variable")) {
            if(project.getVariableByName(variable.asText()).isEmpty()){
                project.addVariable(variable.asText());
            }
        }
    }
}

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
import java.util.*;

public class DiagramClipboardDataDeserializer extends JsonDeserializer<DiagramClipboardData> {

    ObjectMapper mapper = new ObjectMapper();
    Project project;

    public DiagramClipboardDataDeserializer(Project project){
        this.project = project;

        SimpleModule module = new SimpleModule();
        module.addDeserializer(ProjectConfiguration.class, new ProjectConfigurationDeserializer(project));
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

        List<ProjectDevice> deviceList = mapper.readValue(root.get("devices").traverse(),new TypeReference<List<ProjectDevice>>() {});

        ArrayList<ProjectDevice> reNameDevices = addNewDevice(deviceList);
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
                    source = null;
                    destination = null;
                    break;
                }
            }
        }
        return new DiagramClipboardData(sceneList,conditionList,delayList,lineList);
    }


    JsonNode modifiedDeviceNameJson(JsonNode jsonNode,  ArrayList<ProjectDevice> newDevices) {
        for (ProjectDevice device : newDevices) {
            for (JsonNode sceneNode : jsonNode.get("scenes") ) {
                for (JsonNode settingNode : sceneNode.get("setting")) {
                    String name = settingNode.get("device").asText();
                    if(name.equalsIgnoreCase(device.getName())) {
                        ((ObjectNode)settingNode).put("device", device.getName()+"_"+project.getProjectName().replace(" ","_"));
                    }
                    else if (name.equalsIgnoreCase("Memory")) {
                        for (JsonNode valueMap : settingNode.get("valueMap")) {
                            if (valueMap.get("type").asText().equalsIgnoreCase("CustomNumberExpression")) {
                                for (JsonNode terms : valueMap.get("value").get("terms")) {
                                    if(terms.get("value").get("name").asText().equalsIgnoreCase(device.getName())){
                                        ((ObjectNode)terms.get("value")).put("name", device.getName()+"_"+project.getProjectName().replace(" ","_"));
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
                    if (name.equalsIgnoreCase(device.getName())) {
                        ((ObjectNode)settingNode).put("device", device.getName()+"_"+project.getProjectName().replace(" ","_"));
                    }
                }
            }
        }
        return jsonNode;
    }

    ArrayList<ProjectDevice> addNewDevice(List<ProjectDevice> deviceList) {
        ArrayList<ProjectDevice> devices = new  ArrayList<ProjectDevice>();
        ObservableList<ProjectDevice> projectDeviceSet = project.getUnmodifiableProjectDevice();
        if (projectDeviceSet.isEmpty()) {
            for (ProjectDevice projectDevice : deviceList) {
                ProjectDevice newProjectDevice = new ProjectDevice(projectDevice.getName(),projectDevice.getGenericDevice());
                project.addDevice(newProjectDevice);
            }
        } else {
            for (ProjectDevice projectDevice : deviceList) {
                if (!projectDeviceSet.contains(projectDevice)) {
                    System.out.println(projectDevice.getGenericDevice().getName());
                    ProjectDevice newProjectDevice = new ProjectDevice(projectDevice.getName(),projectDevice.getGenericDevice());
                    project.addDevice(newProjectDevice);
                } else {
                    for(ProjectDevice pr : projectDeviceSet) {
                        if(projectDevice.getName().equalsIgnoreCase(pr.getName())) {
                            if(!projectDevice.getGenericDevice().getName().equalsIgnoreCase(pr.getGenericDevice().getName())) {
                                devices.add(projectDevice);
                                String name = projectDevice.getName()+"_"+project.getProjectName().replace(" ","_");
                                ProjectDevice newProjectDevice = new ProjectDevice(name,projectDevice.getGenericDevice());
                                project.addDevice(newProjectDevice);
                            }
                        }
                    }
                }
            }
        }
        return devices;
    }
}

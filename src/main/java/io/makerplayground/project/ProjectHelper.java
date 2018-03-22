package io.makerplayground.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.makerplayground.device.Action;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import javafx.beans.property.ObjectProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class ProjectHelper {
    // Suppress default constructor for noninstantiability
    private ProjectHelper() {
    }

    public static Project loadProject(File f) {
        ObjectMapper mapper = new ObjectMapper();
        Project p = null;
        try {
            p = mapper.readValue(f, Project.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p.setFilePath(f.getAbsolutePath());
        return p;
    }

    public static boolean saveProject(Project p) {
        // TODO: implement code to save a project from file
        throw new UnsupportedOperationException();
    }

    /**
     * Create an instance of a dummy project
     * @return a dummy project used while developing
     * @deprecated
     */
    public static Project loadDummyProject() {
        Project dummyProject = new Project();

//        GenericDevice speaker = null;
//        GenericDevice led = null;
//        for (GenericDevice d : DeviceLibrary.INSTANCE.getActuator()) {
//            if (d.getName().equals("led"))
//                led = d;
//            if (d.getName().equals("speaker"))
//                speaker = d;
//
//            dummyProject.addActuator(d);
//        }

//        Scene s1 = dummyProject.addState();
//        for (UserSetting setting : s1.getSetting()) {
//            if(setting.getDevice().getGenericDevice() == led) {
//                for (Action action : led.getAction()) {
//                   if (action.getName().equals("on"))
//                       setting.setAction(action);
//                }
//            }
//        }
//        Scene s2 = dummyProject.addState();
//        s2.getPosition().setX(300.0);
//        s2.getPosition().setY(500.0);
//        for(UserSetting setting : s2.getSetting()){
//            if(setting.getDevice().getGenericDevice() == led){
//                for (Action action : led.getAction())
//                    if(action.getName().equals("off"))
//                        setting.setAction(action);
//            }
//        }
//        dummyProject.addCondition(s1,s2);
        return dummyProject;
    }
}

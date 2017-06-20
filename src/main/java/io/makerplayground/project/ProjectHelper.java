package io.makerplayground.project;

import io.makerplayground.device.Action;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;

import java.io.File;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class ProjectHelper {
    // Suppress default constructor for noninstantiability
    private ProjectHelper() {
    }

    public static Project loadProject(File f) {
        // TODO: implement code to load a project from file
        throw new UnsupportedOperationException();
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

        GenericDevice speaker = null;
        GenericDevice led = null;
        for (GenericDevice d : DeviceLibrary.INSTANCE.getOutputDevice()) {
            if (d.getName().equals("led"))
                led = d;
            if (d.getName().equals("speaker"))
                speaker = d;

            dummyProject.addOutputDevice(d);
        }

        State s1 = dummyProject.addState();
        for (UserSetting setting : s1.getSetting()) {
            if(setting.getDevice().getGenericDevice() == led) {
                for (Action action : led.getAction()) {
                   if (action.getName().equals("on"))
                       setting.setAction(action);
                }
            }
        }
        State s2 = dummyProject.addState();
        for(UserSetting setting : s2.getSetting()){
            if(setting.getDevice().getGenericDevice() == led){
                for (Action action : led.getAction())
                    if(action.getName().equals("off"))
                        setting.setAction(action);
            }
        }
        return dummyProject;
    }
}

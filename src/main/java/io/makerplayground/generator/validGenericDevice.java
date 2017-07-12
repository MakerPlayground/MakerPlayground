package io.makerplayground.generator;

import io.makerplayground.device.*;
import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;
import io.makerplayground.helper.Unit;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.Scene;
import io.makerplayground.project.UserSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class validGenericDevice {

    public static Map<ProjectDevice, List<Device>> getSupportedDeviceList(Project project) {
        List<Device> actualDevice = DeviceLibrary.INSTANCE.getActualDevice();

        Map<GenericDevice, Map<Action, Map<Parameter, Constraint>>> tempMap = new HashMap<>();
        for (Scene s : project.getScene()) {
            for (UserSetting u : s.getSetting()) {
                GenericDevice genericDevice = u.getDevice().getGenericDevice();
                if (!tempMap.containsKey(genericDevice)) {
                    tempMap.put(genericDevice, new HashMap<>());
                }

                Map<Action, Map<Parameter, Constraint>> compatibility = tempMap.get(genericDevice);
                for (Parameter parameter : u.getValueMap().keySet()) {
                    Action action = u.getAction();
                    Object o = u.getValueMap().get(parameter);

                    if (!compatibility.containsKey(action)) {
                        compatibility.put(action, new HashMap<>());
                    }

                    Constraint newConstraint = null;
                    if (parameter.getDataType() == DataType.INTEGER || parameter.getDataType() == DataType.DOUBLE) {
                        //NumberWithUnit n = (NumberWithUnit) o;
                        //newConstraint = Constraint.createNumericConstraint(n.getValue(), n.getValue(), n.getUnit());
                        // TODO: change to number with unit
                        newConstraint = Constraint.createNumericConstraint((Double) o, (Double) o, Unit.NOT_SPECIFIED);
                    } else if (parameter.getDataType() == DataType.STRING || parameter.getDataType() == DataType.ENUM) {
                        newConstraint = Constraint.createCategoricalConstraint((String) o);
                    } else {
                        continue;
                    }

                    Map<Parameter, Constraint> parameterMap = compatibility.get(action);
                    if (parameterMap.containsKey(parameter)) {
                        Constraint oldConstraint = parameterMap.get(parameter);
                        parameterMap.replace(parameter, oldConstraint.union(newConstraint));
                    } else {
                        parameterMap.put(parameter, newConstraint);
                    }
                }
            }
        }

        for (GenericDevice device : tempMap.keySet()) {
            System.out.println(device.getName());
            for (Action action : tempMap.get(device).keySet()) {
                System.out.println(action.getName());
                for (Parameter parameter : tempMap.get(device).get(action).keySet()) {
                    System.out.println(parameter.getName() + tempMap.get(device).get(action).get(parameter));
                }
            }
        }
        return null;
    }
}

package io.makerplayground.device;

import io.makerplayground.helper.DataType;
import io.makerplayground.helper.NumberWithUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class CompatibleAction {
    private Map<Action, Map<Parameter, Constraint>> compatibility;

    public CompatibleAction() {
        this.compatibility = new HashMap<>();
    }

    public void addAction(Action action, Parameter parameter, Object o) {
        if (!compatibility.containsKey(action)) {
            compatibility.put(action, new HashMap<>());
        }

        Constraint newConstraint = null;
        if (parameter.getDataType() == DataType.INTEGER || parameter.getDataType() == DataType.DOUBLE) {
            NumberWithUnit n = (NumberWithUnit) o;
            newConstraint = Constraint.createNumericConstraint(n.getValue(), n.getValue(), n.getUnit());
        } else if (parameter.getDataType() == DataType.STRING || parameter.getDataType() == DataType.ENUM) {
            newConstraint = Constraint.createCategoricalConstraint((String) o);
        } else {
            return;
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

package io.makerplayground.device;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.Action;
import io.makerplayground.device.shared.Parameter;
import io.makerplayground.device.shared.constraint.Constraint;
import io.makerplayground.device.shared.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActualDeviceTest {

    /*
        ActualDevice{
            id='MP-0001',
            brand='MakerPlayground',
            model='LED',
            url='http://www.makerplayground.io/library/MP-0001',
            deviceType=PERIPHERAL,
            formFactor=MP_DEVICE,
            supportedPlatform=[MP_ARDUINO],
            port=[
                DevicePort{
                    name='Port',
                    type=MP,
                    function=[DevicePortFunction{peripheral=MP_PWM_SINGLE_1, pinType=INOUT}],
                    vmin=5.0,
                    vmax=5.0,
                    x=0.0,
                    y=0.0
                }
            ],
            connectivity=[MP_PWM_SINGLE_1],
            supportedDevice={
                GenericDevice{
                    name='LED',
                    description='LED lamp',
                    action=[
                        Action{
                            name='On',
                            parameter=[
                                Parameter{
                                    name='Brightness',
                                    defaultValue=100.0,
                                    constraint=NumericConstraint{
                                        numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                                    },
                                    dataType=DOUBLE,
                                    controlType=SLIDER
                                }
                            ]
                        },
                        Action{
                            name='Off',
                            parameter=[]
                        }
                    ],
                    condition=[],
                    value=[],
                    property=[]
                }=0
            },
            supportedAction={
                GenericDevice{
                    name='LED',
                    description='LED lamp',
                    action=[
                        Action{
                            name='On',
                            parameter=[
                                Parameter{
                                    name='Brightness',
                                    defaultValue=100.0,
                                    constraint=NumericConstraint{
                                        numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                                    },
                                    dataType=DOUBLE,
                                    controlType=SLIDER
                                }
                            ]
                        },
                        Action{
                            name='Off',
                            parameter=[]
                        }
                    ],
                    condition=[],
                    value=[],
                    property=[]
                }={
                    Action{
                        name='On',
                        parameter=[
                            Parameter{
                                name='Brightness',
                                defaultValue=100.0,
                                constraint=NumericConstraint{
                                    numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                                },
                                dataType=DOUBLE,
                                controlType=SLIDER
                            }
                        ]
                    }={
                        Parameter{
                            name='Brightness',
                            defaultValue=100.0,
                            constraint=NumericConstraint{
                                numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                            },
                            dataType=DOUBLE,
                            controlType=SLIDER
                        }=NumericConstraint{
                            numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                        }
                    },
                    Action{
                        name='Off',
                        parameter=[]
                    }={}
                }
            },
            supportedValue={
                GenericDevice{
                    name='LED',
                    description='LED lamp',
                    action=[
                        Action{
                            name='On',
                            parameter=[
                                Parameter{
                                    name='Brightness',
                                    defaultValue=100.0,
                                    constraint=NumericConstraint{
                                        numericValue={%=Value{min=0.0, max=100.0, unit=%}}
                                    },
                                    dataType=DOUBLE,
                                    controlType=SLIDER
                                }
                            ]
                        },
                        Action{
                            name='Off',
                            parameter=[]
                        }
                    ],
                    condition=[],
                    value=[],
                    property=[]
                }={}
            },
            dependency=null
        }
     */

    private ActualDevice tester;
    private DeviceLibrary library;

    @BeforeEach
    void setUp() {
        library = DeviceLibrary.INSTANCE;
        library.loadDeviceFromJSON();

        tester = library.getActualDevice("MP-0001");
    }

    @Test
    void isSupportShouldBeFalseIfGenericDeviceIsNotInSupportActionList() {
        GenericDevice genericDevice = library.getGenericDevice("RGB LED");
        assertFalse(tester.isSupport(genericDevice, new HashMap<>()));
    }

    @Test
    void isSupportShouldBeFalseIfSomeActionInProjectIsNotSupportedByActualDeviceAction() {
        // no need for this case
    }

    @Test
    void isSupportShouldBeFalseIfSomeParameterOfTheMatchingActionsIsNotExistInActualDeviceAction() {
        // no need for this case
    }

    @Test
    void isSupportShouldBeFalseIfSomeParameterOfTheMatchingActionAreNotCompatibleToParameterOfTheActualDeviceAction() {
        GenericDevice genericDevice = library.getGenericDevice("LED");
        Action action = genericDevice.getAction("On");
        Parameter parameter = action.getParameter("Brightness");

        // We assume that the project needs 101% of brightness as the parameter value, so no device support for this case.
        Constraint constraint = Constraint.createNumericConstraint(0, 101, Unit.PERCENT);

        Map<Parameter, Constraint> innermap = new HashMap<>();
        innermap.put(parameter, constraint);

        Map<Action, Map<Parameter, Constraint>> map = new HashMap<>();
        map.put(action, innermap);

        assertFalse(tester.isSupport(genericDevice, map));
    }

    @Test
    void isSupportShouldBeTrueIfAllParameterInAllMatchingActionAreCompatibleToTheParameterOfTheActualDeviceAction() {
        GenericDevice genericDevice = library.getGenericDevice("LED");
        Action action = genericDevice.getAction("On");
        Parameter parameter = action.getParameter("Brightness");
        Constraint constraint = parameter.getConstraint();

        Map<Parameter, Constraint> innermap = new HashMap<>();
        innermap.put(parameter, constraint);

        Map<Action, Map<Parameter, Constraint>> map = new HashMap<>();
        map.put(action, innermap);

        assertTrue(tester.isSupport(genericDevice, map));
    }
}
package littlebot.robods.control;

import java.util.HashMap;

import littlebot.robods.ControlDatabase;

/**
 * @author Ben Wolsieffer
 */
public class JoystickAxisProperty extends JoystickComponentProperty {

    private int axisIndex = -1;
    private boolean inverted;
    private float value;

    public JoystickAxisProperty(String name, int joystickIndex, int axisIndex, ControlDatabase controlDatabase) {
        super(name, joystickIndex, controlDatabase);
        setAxisIndex(axisIndex);
    }

    public int getAxisIndex() {
        return axisIndex;
    }

    public boolean setAxisIndex(int axisIndex) {
        if (axisIndex >= 0) {
            if (this.axisIndex != axisIndex) {
                unregister();
                this.axisIndex = axisIndex;
                return register();
            }
            return true;
        }
        return false;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
        controlDatabase.setAxis(getJoystickIndex(), axisIndex, value);
    }


    @Override
    protected boolean register() {
        return controlDatabase.registerAxis(getJoystickIndex(), axisIndex);
    }

    @Override
    protected void unregister() {
        controlDatabase.unregisterAxis(getJoystickIndex(), axisIndex);
    }

    @Override
    public void read(HashMap<String, Object> properties) {
        super.read(properties);
        setAxisIndex((Integer) properties.get(getName() + "AXIS_INDEX"));
        inverted = (Boolean) properties.get(getName() + "INVERTED");
    }

    @Override
    public void write(HashMap<String, Object> properties) {
        super.write(properties);
        properties.put(getName() + "AXIS_INDEX", axisIndex);
        properties.put(getName() + "INVERTED", inverted);
    }
}

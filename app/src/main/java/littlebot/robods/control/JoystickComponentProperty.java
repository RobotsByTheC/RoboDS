package littlebot.robods.control;

import java.util.HashMap;

import littlebot.robods.ControlDatabase;

/**
 * @author Ben Wolsieffer
 */
public abstract class JoystickComponentProperty extends Property {

    private int joystickIndex = -1;
    protected final ControlDatabase controlDatabase;


    public JoystickComponentProperty(String name, int joystickIndex, ControlDatabase controlDatabase) {
        super(name);
        this.controlDatabase = controlDatabase;
        setJoystickIndex(joystickIndex);
    }

    public int getJoystickIndex() {
        return joystickIndex;
    }

    public boolean setJoystickIndex(int joystickIndex) {
        if (joystickIndex >= 0) {
            if (this.joystickIndex != joystickIndex) {
                unregister();
                this.joystickIndex = joystickIndex;
                return register();
            }
            return true;
        }
        return false;
    }

    protected abstract boolean register();

    protected abstract void unregister();

    @Override
    public void read(HashMap<String, Object> properties) {
        setJoystickIndex((Integer) properties.get(getName() + "JOYSTICK_INDEX"));
    }

    @Override
    public void write(HashMap<String, Object> properties) {
        properties.put(getName() + "JOYSTICK_INDEX", joystickIndex);
    }
}

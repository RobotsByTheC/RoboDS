package littlebot.robods.control;

import java.util.HashMap;

import littlebot.robods.ControlDatabase;

/**
 * @author Ben Wolsieffer
 */
public class JoystickButtonProperty extends JoystickComponentProperty {
    private int buttonIndex = -1;
    private boolean pressed;

    public JoystickButtonProperty(String name, int joystickIndex, int buttonIndex, ControlDatabase controlDatabase) {
        super(name, joystickIndex, controlDatabase);
        setButtonIndex(buttonIndex);
    }

    public int getButtonIndex() {
        return buttonIndex;
    }

    public boolean setButtonIndex(int buttonIndex) {
        if (buttonIndex >= 0) {
            if (buttonIndex != this.buttonIndex) {
                unregister();
                this.buttonIndex = buttonIndex;
                return register();
            }
            return true;
        }
        return false;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
        controlDatabase.setButton(getJoystickIndex(), buttonIndex, pressed);
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    protected boolean register() {
        return controlDatabase.registerButton(getJoystickIndex(), buttonIndex);
    }

    @Override
    protected void unregister() {
        controlDatabase.unregisterButton(getJoystickIndex(), buttonIndex);
    }

    @Override
    public void read(HashMap<String, Object> properties) {
        super.read(properties);
        setButtonIndex((Integer) properties.get(getName() + "Button Index"));
    }

    @Override
    public void write(HashMap<String, Object> properties) {
        super.write(properties);
        properties.put(getName() + "Button Index", buttonIndex);
    }
}

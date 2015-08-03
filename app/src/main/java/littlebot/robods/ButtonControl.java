package littlebot.robods;

import android.content.Context;

import littlebot.robods.control.JoystickButtonProperty;

/**
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class ButtonControl extends ControlView {

    private final JoystickButtonProperty joystickButtonProperty = new JoystickButtonProperty("Button", 0, 0, getControlDatabase());

    private ButtonListener listener;

    public ButtonControl(Context context, ControlDatabase controlDatabase) {
        super(context, controlDatabase);

        addProperty(joystickButtonProperty);
    }

    public int getJoystickIndex() {
        return joystickButtonProperty.getJoystickIndex();
    }

    public void setJoystickIndex(int joystickIndex) {
        joystickButtonProperty.setJoystickIndex(joystickIndex);
    }

    public int getButtonIndex() {
        return joystickButtonProperty.getButtonIndex();
    }

    public void setButtonIndex(int buttonIndex) {
        joystickButtonProperty.setButtonIndex(buttonIndex);
    }

    public void setButtonPressed(boolean pressed) {
        joystickButtonProperty.setPressed(pressed);
        if (listener != null) {
            if (pressed) {
                listener.buttonPressed();
            } else {
                listener.buttonReleased();
            }
        }
    }

    public boolean isButtonPressed() {
        return joystickButtonProperty.isPressed();
    }

    public void setButtonListener(ButtonListener l) {
        listener = l;
    }

    public interface ButtonListener {
        void buttonPressed();

        void buttonReleased();
    }
}

package littlebot.robods;

import android.content.Context;
import android.util.AttributeSet;

import java.util.HashMap;

/**
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class ButtonView extends ControlView {

    private static final String JOYSTICK_NUMBER_PROPERTY = "joy";
    private static final String BUTTON_NUMBER_PROPERTY = "button";

    private int joystickNumber = 1;
    private int buttonNumber = 1;
    private boolean buttonPressed;

    private ButtonListener listener;

    public ButtonView(Context context) {
        super(context);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Nothing here yet
    }

    public int getJoystickNumber() {
        return joystickNumber;
    }

    public void setJoystickNumber(int joystickNumber) {
        this.joystickNumber = joystickNumber;
    }

    public int getButtonNumber() {
        return buttonNumber;
    }

    public void setButtonNumber(int buttonNumber) {
        this.buttonNumber = buttonNumber;
    }


    public void setButtonPressed(boolean pressed) {
        this.buttonPressed = pressed;

        if (listener != null) {
            if (pressed)
                listener.buttonPressed();
            else
                listener.buttonReleased();
        }
    }

    public boolean isButtonPressed() {
        return buttonPressed;
    }

    @Override
    public void readProperties(HashMap<String, Object> properties) {
        super.readProperties(properties);
        setJoystickNumber((Integer) properties.get(JOYSTICK_NUMBER_PROPERTY));
        setButtonNumber((Integer) properties.get(BUTTON_NUMBER_PROPERTY));
    }

    @Override
    public void writeProperties(HashMap<String, Object> properties) {
        super.writeProperties(properties);
        properties.put(JOYSTICK_NUMBER_PROPERTY, getJoystickNumber());
        properties.put(BUTTON_NUMBER_PROPERTY, getButtonNumber());
    }

    public void setButtonListener(ButtonListener l) {
        listener = l;
    }

    public interface ButtonListener {
        void buttonPressed();

        void buttonReleased();
    }
}

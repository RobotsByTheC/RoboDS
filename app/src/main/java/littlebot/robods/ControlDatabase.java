package littlebot.robods;

import com.google.common.collect.TreeBasedTable;

/**
 * @author Ben Wolsieffer
 */
public class ControlDatabase {

    private final TreeBasedTable<Integer, Integer, Float> axes = TreeBasedTable.create();
    private final TreeBasedTable<Integer, Integer, Boolean> buttons = TreeBasedTable.create();
    private final TreeBasedTable<Integer, Integer, Integer> povHats = TreeBasedTable.create();

    private ControlListener listener;

    public boolean registerAxis(int joystick, int axis) {
        if (!axes.contains(joystick, axis)) {
            axes.put(joystick, axis, 0.0f);
            if (listener != null) {
                listener.axisRegistered(joystick, axis);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean registerButton(int joystick, int button) {
        if (!buttons.contains(joystick, button)) {
            buttons.put(joystick, button, false);
            if (listener != null) {
                listener.buttonRegistered(joystick, button);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean registerPOVHat(int joystick, int povHat) {
        if (!povHats.contains(joystick, povHat)) {
            povHats.put(joystick, povHat, 0);
            if (listener != null) {
                listener.povHatRegistered(joystick, povHat);
            }
            return true;
        } else {
            return false;
        }
    }

    public void setAxis(int joystick, int axis, float value) {
        if (axes.contains(joystick, axis)) {
            axes.put(joystick, axis, value);
            if(listener != null) {
                listener.axisValueChanged(joystick, axis, value);
            }
        } else {
            throw new IllegalArgumentException("Axis (" + joystick + ", " + axis + ") is not registered.");
        }
    }

    public void setButton(int joystick, int button, boolean pressed) {
        if (buttons.contains(joystick, button)) {
            buttons.put(joystick, button, pressed);
            if(listener != null) {
                listener.buttonStateChanged(joystick, button, pressed);
            }
        } else {
            throw new IllegalArgumentException("Button (" + joystick + ", " + button + ") is not registered.");
        }
    }

    public void setPOVHat(int joystick, int povHat, int angle) {
        if (povHats.contains(joystick, povHat)) {
            povHats.put(joystick, povHat, angle);
            if(listener != null) {
                listener.povHatAngleChanged(joystick, povHat, angle);
            }
        } else {
            throw new IllegalArgumentException("POV hat (" + joystick + ", " + povHat + ") is not registered.");
        }
    }

    public void unregisterAxis(int joystick, int axis) {
        if (axes.remove(joystick, axis) != null && listener != null) {
            listener.axisUnregistered(joystick, axis);
        }
    }

    public void unregisterButton(int joystick, int button) {
        if (buttons.remove(joystick, button) != null && listener != null) {
            listener.buttonUnregistered(joystick, button);
        }
    }

    public void unregisterPOVHat(int joystick, int povHat) {
        if (povHats.remove(joystick, povHat) != null && listener != null) {
            listener.povHatUnregistered(joystick, povHat);
        }
    }

    public void setControlListener(ControlListener listener) {
        this.listener = listener;
    }

    public interface ControlListener {
        void axisRegistered(int joystickIndex, int axisIndex);

        void buttonRegistered(int joystickIndex, int buttonIndex);

        void povHatRegistered(int joystickIndex, int povHatIndex);

        void axisValueChanged(int joystickIndex, int axisIndex, float value);

        void buttonStateChanged(int joystickIndex, int buttonIndex, boolean pressed);

        void povHatAngleChanged(int joystickIndex, int povHatIndex, int angle);

        void axisUnregistered(int joystickIndex, int axisIndex);

        void buttonUnregistered(int joystickIndex, int buttonIndex);

        void povHatUnregistered(int joystickIndex, int povHatIndex);
    }
}

package littlebot.robods;

import android.content.Context;
import android.graphics.Color;

import littlebot.robods.control.JoystickAxisProperty;


/**
 * A class that represents any joystick. It does not implement drawing of the
 * joystick, which is left up to subclasses. This class only contains properties
 * and methods that are common to any joystick.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class JoystickControl extends ControlView {

    private final JoystickAxisProperty xAxis = new JoystickAxisProperty("X Axis", 0, 0, getControlDatabase());
    private final JoystickAxisProperty yAxis = new JoystickAxisProperty("Y Axis", 0, 1, getControlDatabase());

    private JoystickChangeListener listener;

    public JoystickControl(Context context, ControlDatabase controlDatabase) {
        super(context, controlDatabase);
        setBackgroundColor(Color.alpha(0));

        addProperty(xAxis);
        addProperty(yAxis);
    }

    public float getXAxisValue() {
        return xAxis.getValue();
    }

    public float getYAxisValue() {
        return yAxis.getValue();
    }

    public int getXJoystickIndex() {
        return xAxis.getJoystickIndex();
    }

    public int getYJoystickIndex() {
        return yAxis.getJoystickIndex();
    }

    public int getXAxisIndex() {
        return xAxis.getAxisIndex();
    }

    public int getYAxisNumber() {
        return yAxis.getAxisIndex();
    }

    public boolean isXInverted() {
        return xAxis.isInverted();
    }

    public boolean isYInverted() {
        return yAxis.isInverted();
    }

    public void setJoystickIndex(int joystickNumber) {
        setXJoystickIndex(joystickNumber);
        setYJoystickIndex(joystickNumber);
    }

    public void setXJoystickIndex(int joystickNumber) {
        xAxis.setJoystickIndex(joystickNumber);
    }

    public void setYJoystickIndex(int joystickNumber) {
        yAxis.setJoystickIndex(joystickNumber);
    }

    public void setXAxisIndex(int xAxisNumber) {
        this.xAxis.setAxisIndex(xAxisNumber);
    }

    public void setYAxisIndex(int yAxisNumber) {
        this.yAxis.setAxisIndex(yAxisNumber);
    }

    public void setXInverted(boolean xInverted) {
        this.xAxis.setInverted(xInverted);
    }

    public void setYInverted(boolean yInverted) {
        this.yAxis.setInverted(yInverted);
    }

    public void setXAxisValue(float value) {
        xAxis.setValue(value);

        if (listener != null) {
            listener.onJoystickChange(getXAxisValue(), getYAxisValue());
        }
    }

    public void setYAxisValue(float value) {
        yAxis.setValue(value);

        if (listener != null) {
            listener.onJoystickChange(getXAxisValue(), getYAxisValue());
        }
    }

    public void setJoystickChangeListener(final JoystickChangeListener l) {
        listener = l;
    }

    public interface JoystickChangeListener {
        void onJoystickChange(float x, float y);
    }
}

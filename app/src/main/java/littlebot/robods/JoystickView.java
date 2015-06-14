package littlebot.robods;

import android.content.Context;
import android.util.AttributeSet;

import java.util.HashMap;


/**
 * A class that represents any joystick. It does not implement drawing of the joystick, which is
 * left up to subclasses. This class only contains properties and methods that are common to any
 * joystick.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class JoystickView extends ControlView {

    private static final String X_JOYSTICK_NUMBER_PROPERTY = "x_joy";
    private static final String Y_JOYSTICK_NUMBER_PROPERTY = "y_joy";
    private static final String X_AXIS_NUMBER_PROPERTY = "x_axis";
    private static final String X_AXIS_INVERTED_PROPERTY = "x_inv";
    private static final String Y_AXIS_NUMBER_PROPERTY = "y_axis";
    private static final String Y_AXIS_INVERTED_PROPERTY = "y_inv";

    private Axis xAxis, yAxis;

    private JoystickChangeListener listener;

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        xAxis = new Axis(1, 1, false);
        yAxis = new Axis(1, 2, false);
    }

    public Axis getXAxis() {
        return xAxis;
    }

    public Axis getYAxis() {
        return yAxis;
    }

    public void setXAxis(Axis axis) {
        xAxis = axis;
    }

    public void setYAxis(Axis axis) {
        yAxis = axis;
    }

    public int getXValue() {
        return xAxis.get();
    }

    public int getYValue() {
        return yAxis.get();
    }

    public int getXJoystickNumber() {
        return xAxis.getJoystickNumber();
    }

    public int getYJoystickNumber() {
        return yAxis.getJoystickNumber();
    }

    public int getXAxisNumber() {
        return xAxis.getAxisNumber();
    }

    public int getYAxisNumber() {
        return yAxis.getAxisNumber();
    }

    public boolean isXInverted() {
        return xAxis.isInverted();
    }

    public boolean isYInverted() {
        return yAxis.isInverted();
    }

    public void setJoystickNumber(int joystickNumber) {
        setXJoystickNumber(joystickNumber);
        setYJoystickNumber(joystickNumber);
    }

    public void setXJoystickNumber(int joystickNumber) {
        xAxis.setJoystickNumber(joystickNumber);
    }

    public void setYJoystickNumber(int joystickNumber) {
        yAxis.setJoystickNumber(joystickNumber);
    }

    public void setXAxisNumber(int xAxisNumber) {
        this.xAxis.setAxisNumber(xAxisNumber);
    }

    public void setYAxisNumber(int yAxisNumber) {
        this.yAxis.setAxisNumber(yAxisNumber);
    }

    public void setXInverted(boolean xInverted) {
        this.xAxis.setInverted(xInverted);
    }

    public void setYInverted(boolean yInverted) {
        this.yAxis.setInverted(yInverted);
    }

    public synchronized void setXValue(int value) {
        xAxis.set(value);

        if (listener != null) {
            listener.onJoystickChange(getXValue(), getYValue());
        }
    }

    public synchronized void setYValue(int value) {
        yAxis.set(value);

        if (listener != null) {
            listener.onJoystickChange(getXValue(), getYValue());
        }
    }

    @Override
    public void readProperties(HashMap<String, Object> properties) {
        super.readProperties(properties);
        setXJoystickNumber((Integer) properties.get(X_JOYSTICK_NUMBER_PROPERTY));
        setYJoystickNumber((Integer) properties.get(Y_JOYSTICK_NUMBER_PROPERTY));
        setXAxisNumber((Integer) properties.get(X_AXIS_NUMBER_PROPERTY));
        setXInverted((Boolean) properties.get(X_AXIS_INVERTED_PROPERTY));
        setYAxisNumber((Integer) properties.get(Y_AXIS_NUMBER_PROPERTY));
        setYInverted((Boolean) properties.get(Y_AXIS_INVERTED_PROPERTY));
    }

    @Override
    public void writeProperties(HashMap<String, Object> properties) {
        super.writeProperties(properties);
        properties.put(X_JOYSTICK_NUMBER_PROPERTY, getXJoystickNumber());
        properties.put(Y_JOYSTICK_NUMBER_PROPERTY, getYJoystickNumber());
        properties.put(X_AXIS_NUMBER_PROPERTY, getXAxisNumber());
        properties.put(X_AXIS_INVERTED_PROPERTY, isXInverted());
        properties.put(Y_AXIS_NUMBER_PROPERTY, getYAxisNumber());
        properties.put(Y_AXIS_INVERTED_PROPERTY, isYInverted());

    }

    public void setJoystickChangeListener(final JoystickChangeListener l) {
        listener = l;
    }

    public interface JoystickChangeListener {
        void onJoystickChange(int x, int y);
    }


}

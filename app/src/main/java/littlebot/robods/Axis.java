package littlebot.robods;

/**
 * Created by raystubbs on 23/03/15.
 */
public class Axis {

    private int joystickNumber;
    private int axisNumber;
    private boolean inverted;

    private int value;
    private AxisListener listener;

    public Axis(int joystickNumber, int axisNumber, boolean inverted){
        this.joystickNumber = joystickNumber;
        this.axisNumber = axisNumber;
        this.inverted = inverted;
    }

    public int get(){
        return value;
    }

    public void set(int value){


        this.value = (inverted)?-value:value;

        if(listener != null)
            listener.valueChanged(value);
    }

    public void setAxisListener(AxisListener listener){
        this.listener = listener;
    }

    public int getJoystickNumber() {
        return joystickNumber;
    }

    public void setJoystickNumber(int joystickNumber) {
        this.joystickNumber = joystickNumber;
    }

    public int getAxisNumber() {
        return axisNumber;
    }

    public void setAxisNumber(int axisNumber) {
        this.axisNumber = axisNumber;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public interface AxisListener{
        void valueChanged(int value);
    }

}

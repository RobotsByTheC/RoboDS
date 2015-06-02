package littlebot.robods;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by raystubbs on 23/03/15.
 */
public abstract class ButtonView extends ControlView {

    private int joystickNumber = -1;
    private int buttonNumber = -1;
    private String text = "default";
    private boolean pressed;

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

    private void init(){
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


    public void setText(String text){
        this.text = text;
    }

    public String getText(){
        return text;
    }

    public void setPressed(boolean pressed){
        this.pressed = pressed;

        if(listener != null){
            if(pressed)
                listener.buttonPressed();
            else
                listener.buttonReleased();
        }
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    public DSLayoutNode toLayoutNode() {
        return new DSLayoutNode(this.getClass(), getProperties());
    }

    @Override
    public Object[] getProperties() {
        return new Object[]{
            joystickNumber,
            buttonNumber,
            this.getText(),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight()
        };
    }

    @Override
    public void setProperties(Object[] properties) {
        setJoystickNumber((Integer)properties[0]);
        setButtonNumber((Integer)properties[1]);
        setText((String)properties[2]);
        setX((Float)properties[3]);
        setY((Float)properties[4]);
        setLayoutParams(new ViewGroup.LayoutParams((Integer)properties[5], (Integer)properties[6]));
    }

    public void setButtonListener(ButtonListener l){
        listener = l;
    }

    public interface ButtonListener{
        void buttonPressed();
        void buttonReleased();
    }
}

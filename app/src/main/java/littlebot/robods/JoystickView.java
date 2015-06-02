package littlebot.robods;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;


/**
 * TODO: document your custom view class.
 */
public abstract class JoystickView extends ControlView{

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

    private void init(){
        xAxis = new Axis(-1, -1, false);
        yAxis = new Axis(-1, -1, false);
    }

    public Axis getXAxis(){return xAxis;}
    public Axis getYAxis(){return yAxis;}

    public int getXValue(){return xAxis.get();}
    public int getYValue(){return yAxis.get();}


    public int getXJoystickNumber(){return xAxis.getJoystickNumber();}
    public int getYJoystickNumber(){return yAxis.getJoystickNumber();}
    public int getXAxisNumber(){return xAxis.getAxisNumber();}
    public  int getYAxisNumber(){return yAxis.getAxisNumber();}
    public boolean isXInverted(){return xAxis.isInverted();}
    public boolean isYInverted(){return yAxis.isInverted();}

    public void setJoystickNumber(int joystickNumber){
        this.xAxis.setJoystickNumber(joystickNumber);
        this.yAxis.setJoystickNumber(joystickNumber);
    }

    public void setXJoystickNumber(int joystickNumber){
        this.xAxis.setJoystickNumber(joystickNumber);
    }

    public void setYJoystickNumber(int joystickNumber){
        this.yAxis.setJoystickNumber(joystickNumber);
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

    public synchronized void setXValue(int value){
        xAxis.set(value);

        if(listener != null)
            listener.onJoystickChange(getXValue(), getYValue());
    }

    public synchronized void setYValue(int value){
        yAxis.set(value);

        if(listener != null)
            listener.onJoystickChange(getXValue(), getYValue());
    }

    public void setXAxis(Axis axis){xAxis = axis;}
    public void setYAxis(Axis axis){yAxis = axis;}

    @Override
    public Object[] getProperties(){
        return new Object[]{
                getXJoystickNumber(),
                getYJoystickNumber(),
                getXAxisNumber(),
                xAxis.isInverted(),
                getYAxisNumber(),
                yAxis.isInverted(),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight()
        };
    }

    @Override
    public void setProperties(Object[] properties){
        setXJoystickNumber((Integer)properties[0]);
        setYJoystickNumber((Integer)properties[1]);
        setXAxisNumber((Integer)properties[2]);
        setXInverted((Boolean)properties[3]);
        setYAxisNumber((Integer)properties[4]);
        setYInverted((Boolean)properties[5]);
        setX((Float)properties[6]);
        setY((Float)properties[7]);
        setLayoutParams(new ViewGroup.LayoutParams((Integer)properties[8], (Integer)properties[9]));
    }

    @Override
    public DSLayoutNode toLayoutNode(){
        return new DSLayoutNode(this.getClass(), this.getProperties());
    }

    public void setJoystickChangeListener(final JoystickChangeListener l){
        listener = l;
    }

    public interface JoystickChangeListener{
        void onJoystickChange(int x, int y);
    }


}

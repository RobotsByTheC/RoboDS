package littlebot.robods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Created by raystubbs on 22/03/15.
 */
public class BasicJoystick extends JoystickView {

    public static final int MAX_VALUE = 128;
    private Paint paint;

    private float pixelToValueRatio;
    private float innerRadius, outerRadius, centerRadius;
    private int innerColor, outerColor;

    public BasicJoystick(Context context) {
        super(context);
        init();
        this.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
    }

    public BasicJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasicJoystick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        setBackgroundColor(Color.alpha(0));
        innerColor = Color.BLACK;
        outerColor = Color.GRAY;

        paint = new Paint();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        centerRadius = Math.min(w, h)/2;

        innerRadius = centerRadius/3;
        outerRadius = centerRadius - innerRadius;

        pixelToValueRatio = MAX_VALUE / outerRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(outerColor);
        canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);
        paint.setColor(innerColor);

        float drawX = centerRadius + ((isXInverted())?-getXValue():getXValue())/pixelToValueRatio;
        float drawY = centerRadius + ((isYInverted())?getYValue():-getYValue())/pixelToValueRatio;

        canvas.drawCircle(drawX, drawY, innerRadius, paint);

        if(isSelected()){
            paint.setColor(Color.argb(125, 0, 255, 0));
            canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);
        }
    }

    @Override
    public ControlView clone() {
        return new BasicJoystick(this.getContext());
    }

    @Override
    public String getControlType() {
        return "Basic Joystick";
    }

    @Override
    public void showEditDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());

        builder.setTitle(getControlType());
        final ViewGroup view = (ViewGroup)((Activity)this.getContext())
                                    .getLayoutInflater().inflate(R.layout.basic_joystick_dialog, null);
        builder.setView(view);

        final TextView xJoyNumTV = (TextView)view.findViewById(R.id.x_joy_num_text);
        final TextView yJoyNumTV = (TextView)view.findViewById(R.id.y_joy_num_text);
        final TextView xAxisNumTV = (TextView)view.findViewById(R.id.x_axis_text);
        final CheckBox xInvertedRB = (CheckBox)view.findViewById(R.id.x_inverted_radio);
        final TextView yAxisNumTV = (TextView)view.findViewById(R.id.y_axis_text);
        final CheckBox yInvertedRB = (CheckBox)view.findViewById(R.id.y_inverted_radio);
        final TextView radiusText = (TextView)view.findViewById(R.id.radius_text);

        xJoyNumTV.setText(Integer.toString(getXJoystickNumber()));
        yJoyNumTV.setText(Integer.toString(getYJoystickNumber()));
        xAxisNumTV.setText(Integer.toString(getXAxisNumber()));
        xInvertedRB.setChecked(isXInverted());
        yAxisNumTV.setText(Integer.toString(getYAxisNumber()));
        yInvertedRB.setChecked(isYInverted());
        radiusText.setText(Integer.toString((int) centerRadius));





        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                try{
                    setXJoystickNumber(Integer.parseInt(xJoyNumTV.getText().toString()));
                    setYJoystickNumber(Integer.parseInt(yJoyNumTV.getText().toString()));
                    setXAxisNumber(Integer.parseInt(xAxisNumTV.getText().toString()));
                    setXInverted(xInvertedRB.isChecked());
                    setYAxisNumber(Integer.parseInt(yAxisNumTV.getText().toString()));
                    setYInverted(yInvertedRB.isChecked());

                    int radius = Integer.parseInt(radiusText.getText().toString());
                    BasicJoystick.this.getLayoutParams().width = radius*2;
                    BasicJoystick.this.getLayoutParams().height = radius*2;
                    BasicJoystick.this.requestLayout();
                    dialog.dismiss();
                }catch(NumberFormatException e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Invalid number format, try again");
                }catch(Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(e.getMessage());
                }




            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();


    }

    public boolean onTouchEvent(MotionEvent event) {

        if (!isEditing()) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float offsetX = event.getX() - centerRadius;
                    float offsetY = event.getY() - centerRadius;

                    //If the touch event occurs outside of the joystick's radius
                    // then keep the cursor inside but adjust the angle
                    if (!(Math.sqrt(offsetX * offsetX + offsetY * offsetY) <= outerRadius)) {
                        float angle = (float) Math.atan2(offsetY, offsetX);
                        offsetX = outerRadius * (float) Math.cos(angle);
                        offsetY = outerRadius * (float) Math.sin(angle);
                    }

                    setXValue((int) (offsetX * pixelToValueRatio));
                    setYValue((int) (-offsetY * pixelToValueRatio));

                    break;
                case MotionEvent.ACTION_UP:
                    setXValue(0);
                    setYValue(0);
                    break;
            }
            invalidate();
        }

        return super.onTouchEvent(event);
    }
}

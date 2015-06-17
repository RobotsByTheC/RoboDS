package littlebot.robods;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;

/**
 * A basic implementation of a round joystick.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public class BasicJoystick extends JoystickView {

    private static final int INNER_COLOR = Color.BLACK,
            OUTER_COLOR = Color.GRAY;

    private static final String RADIUS_PROPERTY = "radius";

    public static final int MAX_VALUE = 128;
    private Paint paint;

    private float pixelToValueRatio;
    private int innerRadius, outerRadius, centerRadius = 200;

    private TextView xJoyNumTV;
    private TextView yJoyNumTV;
    private TextView xAxisNumTV;
    private CheckBox xInvertedRB;
    private TextView yAxisNumTV;
    private CheckBox yInvertedRB;
    private TextView radiusText;

    public BasicJoystick(Context context) {
        super(context);
        init();
    }

    public BasicJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasicJoystick(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundColor(Color.alpha(0));

        paint = new Paint();
        setRadius(centerRadius);

        ViewGroup editDialogLayout = (ViewGroup) (LayoutInflater.from(getContext()).inflate(R.layout.dialog_basic_joystick, null));
        xJoyNumTV = (TextView) editDialogLayout.findViewById(R.id.x_joy_num_text);
        yJoyNumTV = (TextView) editDialogLayout.findViewById(R.id.y_joy_num_text);
        xAxisNumTV = (TextView) editDialogLayout.findViewById(R.id.x_axis_text);
        xInvertedRB = (CheckBox) editDialogLayout.findViewById(R.id.x_inverted_radio);
        yAxisNumTV = (TextView) editDialogLayout.findViewById(R.id.y_axis_text);
        yInvertedRB = (CheckBox) editDialogLayout.findViewById(R.id.y_inverted_radio);
        radiusText = (TextView) editDialogLayout.findViewById(R.id.radius_text);

        setEditDialogLayout(editDialogLayout);
    }

    protected void resetEditDialog() {
        xJoyNumTV.setText(Integer.toString(getXJoystickNumber()));
        yJoyNumTV.setText(Integer.toString(getYJoystickNumber()));
        xAxisNumTV.setText(Integer.toString(getXAxisNumber()));
        xInvertedRB.setChecked(isXInverted());
        yAxisNumTV.setText(Integer.toString(getYAxisNumber()));
        yInvertedRB.setChecked(isYInverted());
        radiusText.setText(Integer.toString(getRadius()));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerRadius = Math.min(w, h) / 2;

        innerRadius = centerRadius / 3;
        outerRadius = centerRadius - innerRadius;

        pixelToValueRatio = (float) MAX_VALUE / outerRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(OUTER_COLOR);
        canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);

        paint.setColor(INNER_COLOR);
        float drawX = centerRadius + ((isXInverted()) ? -getXValue() : getXValue()) / pixelToValueRatio;
        float drawY = centerRadius + ((isYInverted()) ? getYValue() : -getYValue()) / pixelToValueRatio;
        canvas.drawCircle(drawX, drawY, innerRadius, paint);

        if (isSelected()) {
            paint.setColor(SELECTED_COLOR);
            canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);
        }
    }

    public int getRadius() {
        return centerRadius;
    }

    public void setRadius(int radius) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = radius * 2;
        lp.height = radius * 2;
        requestLayout();
    }

    @Override
    public String getControlType() {
        return getContext().getString(R.string.basic_joystick);
    }

    @Override
    public void onEdit(ViewGroup editDialogLayout) {
        try {
            setXJoystickNumber(Integer.parseInt(xJoyNumTV.getText().toString()));
            setYJoystickNumber(Integer.parseInt(yJoyNumTV.getText().toString()));
            setXAxisNumber(Integer.parseInt(xAxisNumTV.getText().toString()));
            setXInverted(xInvertedRB.isChecked());
            setYAxisNumber(Integer.parseInt(yAxisNumTV.getText().toString()));
            setYInverted(yInvertedRB.isChecked());

            setRadius(Integer.parseInt(radiusText.getText().toString()));
        } catch (NumberFormatException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Invalid number format, try again");
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(e.getMessage());
        }
    }

    @Override
    public void readProperties(HashMap<String, Object> properties) {
        super.readProperties(properties);
        setRadius((Integer) properties.get(RADIUS_PROPERTY));
    }

    @Override
    public void writeProperties(HashMap<String, Object> properties) {
        super.writeProperties(properties);
        properties.put(RADIUS_PROPERTY, getRadius());
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
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

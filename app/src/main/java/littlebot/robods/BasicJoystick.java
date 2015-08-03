package littlebot.robods;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;

import littlebot.robods.control.IntegerProperty;

/**
 * A basic implementation of a round joystick.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public class BasicJoystick extends JoystickControl {

    private static final int INNER_COLOR = Color.BLACK,
            OUTER_COLOR = Color.GRAY;

    private final IntegerProperty radius = new IntegerProperty("Radius", 300);

    public static final float MAX_VALUE = 1.0f;
    private Paint paint;

    private float pixelToValueRatio;
    private int innerRadius, outerRadius;

    private TextView xJoyNumTV;
    private TextView yJoyNumTV;
    private TextView xAxisNumTV;
    private CheckBox xInvertedRB;
    private TextView yAxisNumTV;
    private CheckBox yInvertedRB;
    private TextView radiusText;

    public BasicJoystick(Context context, ControlDatabase controlDatabase) {
        super(context, controlDatabase);

        addProperty(radius);

        setBackgroundColor(Color.alpha(0));

        paint = new Paint();
        setRadius(radius.getValue());

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
        xJoyNumTV.setText(Integer.toString(getXJoystickIndex()));
        yJoyNumTV.setText(Integer.toString(getYJoystickIndex()));
        xAxisNumTV.setText(Integer.toString(getXAxisIndex()));
        xInvertedRB.setChecked(isXInverted());
        yAxisNumTV.setText(Integer.toString(getYAxisNumber()));
        yInvertedRB.setChecked(isYInverted());
        radiusText.setText(Integer.toString(getRadius()));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int centerRadius = Math.min(w, h) / 2;

        innerRadius = centerRadius / 3;
        outerRadius = centerRadius - innerRadius;

        pixelToValueRatio = MAX_VALUE / outerRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(OUTER_COLOR);
        int centerRadius = radius.getValue();
        canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);

        paint.setColor(INNER_COLOR);
        float drawX = centerRadius + ((isXInverted()) ? -getXAxisValue() : getXAxisValue()) * outerRadius;
        float drawY = centerRadius + ((isYInverted()) ? getYAxisValue() : -getYAxisValue()) * outerRadius;
        canvas.drawCircle(drawX, drawY, innerRadius, paint);

        if (isSelected()) {
            paint.setColor(SELECTED_COLOR);
            canvas.drawCircle(centerRadius, centerRadius, outerRadius, paint);
        }
    }

    public int getRadius() {
        return radius.getValue();
    }

    private void updateRadius() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = radius.getValue() * 2;
        lp.height = radius.getValue() * 2;
        requestLayout();
    }

    public void setRadius(int radius) {
        this.radius.setValue(radius);
        updateRadius();
    }

    @Override
    public void readProperties(HashMap<String, Object> propMap) {
        super.readProperties(propMap);
        updateRadius();
    }

    @Override
    public String getControlType() {
        return getContext().getString(R.string.basic_joystick);
    }

    @Override
    public void onEdit(ViewGroup editDialogLayout) {
        try {
            setXJoystickIndex(Integer.parseInt(xJoyNumTV.getText().toString()));
            setYJoystickIndex(Integer.parseInt(yJoyNumTV.getText().toString()));
            setXAxisIndex(Integer.parseInt(xAxisNumTV.getText().toString()));
            setXInverted(xInvertedRB.isChecked());
            setYAxisIndex(Integer.parseInt(yAxisNumTV.getText().toString()));
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

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEditing()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    int centerRadius = radius.getValue();
                    float offsetX = event.getX() - centerRadius;
                    float offsetY = event.getY() - centerRadius;

                    //If the touch event occurs outside of the joystick's radius
                    // then keep the cursor inside but adjust the angle
                    if (!(Math.sqrt(offsetX * offsetX + offsetY * offsetY) <= outerRadius)) {
                        float angle = (float) Math.atan2(offsetY, offsetX);
                        offsetX = outerRadius * (float) Math.cos(angle);
                        offsetY = outerRadius * (float) Math.sin(angle);
                    }

                    setXAxisValue(offsetX * pixelToValueRatio);
                    setYAxisValue(-offsetY * pixelToValueRatio);

                    break;
                case MotionEvent.ACTION_UP:
                    setXAxisValue(0);
                    setYAxisValue(0);
                    break;
            }
            invalidate();
        }

        return super.onTouchEvent(event);
    }
}

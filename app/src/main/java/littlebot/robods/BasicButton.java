package littlebot.robods;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import littlebot.robods.control.IntegerProperty;
import littlebot.robods.control.StringProperty;

/**
 * A basic implementation of a square button.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public class BasicButton extends ButtonControl {

    private final StringProperty text = new StringProperty("Text", "Button");
    private  final IntegerProperty width = new IntegerProperty("Width", 100);
    private final IntegerProperty height = new IntegerProperty("Height", 100);

    private static final float CORNER_ROUNDNESS = 10;

    private static final int PRESSED_COLOR = Color.LTGRAY;
    private static final int RELEASED_COLOR = Color.GRAY;

    private Paint paint;
    private RectF drawBounds;

    private TextView joyNumTV;
    private TextView buttonNumTV;
    private TextView textTV;
    private TextView widthTV;
    private TextView heightTV;

    public BasicButton(Context context, ControlDatabase controlDatabase) {
        super(context, controlDatabase);

        addProperty(text);
        addProperty(width);
        addProperty(height);

        setBackgroundColor(Color.alpha(0));
        paint = new Paint();
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);

        ViewGroup editDialogLayout = (ViewGroup) (LayoutInflater.from(getContext()).inflate(R.layout.dialog_basic_button, null));
        joyNumTV = (TextView) editDialogLayout.findViewById(R.id.joy_num_text);
        buttonNumTV = (TextView) editDialogLayout.findViewById(R.id.button_num_text);
        textTV = (TextView) editDialogLayout.findViewById(R.id.button_text);
        widthTV = (TextView) editDialogLayout.findViewById(R.id.width_text);
        heightTV = (TextView) editDialogLayout.findViewById(R.id.height_text);
        setEditDialogLayout(editDialogLayout);
    }

    @Override
    protected void resetEditDialog() {
        joyNumTV.setText(Integer.toString(getJoystickIndex()));
        buttonNumTV.setText(Integer.toString(getButtonIndex()));
        textTV.setText(getText());
        widthTV.setText(Integer.toString(width.getValue()));
        heightTV.setText(Integer.toString(height.getValue()));
    }

    public void setText(String text) {
        this.text.setValue(text);
    }

    public String getText() {
        return text.getValue();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawBounds = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isButtonPressed()) {
            paint.setColor(PRESSED_COLOR);
        } else {
            paint.setColor(RELEASED_COLOR);
        }
        canvas.drawRoundRect(drawBounds, CORNER_ROUNDNESS, CORNER_ROUNDNESS, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(getText(), this.getWidth() / 2, this.getHeight() / 2, paint);

        if (isSelected()) {
            paint.setColor(SELECTED_COLOR);
            canvas.drawRoundRect(drawBounds, CORNER_ROUNDNESS, CORNER_ROUNDNESS, paint);
        }
    }

    @Override
    public void readProperties(HashMap<String, Object> propMap) {
        super.readProperties(propMap);
        updateSize();
    }

    @Override
    public String getControlType() {
        return "Basic Button";
    }

    @Override
    public void onEdit(ViewGroup editDialogLayout) {
        try {
            setJoystickIndex(Integer.parseInt(joyNumTV.getText().toString()));
            setButtonIndex(Integer.parseInt(buttonNumTV.getText().toString()));
            setText(textTV.getText().toString());
            setSize(Integer.parseInt(widthTV.getText().toString()), Integer.parseInt(heightTV.getText().toString()));
        } catch (NumberFormatException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Invalid number format, try again");
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(e.getMessage());
        }
    }

    private void updateSize() {
        RelativeLayout.LayoutParams lp = getLayoutParams();
        lp.width = width.getValue();
        lp.height = height.getValue();
        requestLayout();
    }

    public void setSize(int width, int height) {
        this.width.setValue(width);
        this.height.setValue(height);
        updateSize();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEditing()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setButtonPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    setButtonPressed(false);
                    break;
            }
            invalidate();
        }

        return super.onTouchEvent(event);
    }
}

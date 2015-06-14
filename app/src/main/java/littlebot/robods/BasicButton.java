package littlebot.robods;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * A basic implementation of a square button.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public class BasicButton extends ButtonView {

    private static final String TEXT_PROPERTY = "text";
    private static final String WIDTH_PROPERTY = "width";
    private static final String HEIGHT_PROPERTY = "height";

    private static final float CORNER_ROUNDNESS = 10;

    private static final int PRESSED_COLOR = Color.LTGRAY;
    private static final int RELEASED_COLOR = Color.GRAY;

    private Paint paint;
    private RectF drawBounds;

    private String text = "Button";

    private TextView joyNumTV;
    private TextView buttonNumTV;
    private TextView textTV;
    private TextView widthTV;
    private TextView heightTV;

    public BasicButton(Context context) {
        super(context);
        init();
    }

    public BasicButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasicButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setBackgroundColor(Color.alpha(0));
        paint = new Paint();
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);

        ViewGroup editDialogLayout = (ViewGroup) (LayoutInflater.from(getContext()).inflate(R.layout.basic_button_dialog, null));
        joyNumTV = (TextView) editDialogLayout.findViewById(R.id.joy_num_text);
        buttonNumTV = (TextView) editDialogLayout.findViewById(R.id.button_num_text);
        textTV = (TextView) editDialogLayout.findViewById(R.id.button_text);
        widthTV = (TextView) editDialogLayout.findViewById(R.id.width_text);
        heightTV = (TextView) editDialogLayout.findViewById(R.id.height_text);
        setEditDialogLayout(editDialogLayout);
    }

    @Override
    protected void resetEditDialog() {
        joyNumTV.setText(Integer.toString(getJoystickNumber()));
        buttonNumTV.setText(Integer.toString(getButtonNumber()));
        textTV.setText(getText());
        RelativeLayout.LayoutParams lp = getLayoutParams();
        widthTV.setText(Integer.toString(lp.width));
        heightTV.setText(Integer.toString(lp.height));
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
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
    public String getControlType() {
        return "Basic Button";
    }

    @Override
    public void onEdit(ViewGroup editDialogLayout) {
        try {
            setJoystickNumber(Integer.parseInt(joyNumTV.getText().toString()));
            setButtonNumber(Integer.parseInt(buttonNumTV.getText().toString()));
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

    @Override
    public void readProperties(HashMap<String, Object> properties) {
        super.readProperties(properties);
        setText((String) properties.get(TEXT_PROPERTY));
        setSize((Integer) properties.get(WIDTH_PROPERTY), (Integer) properties.get(HEIGHT_PROPERTY));
    }

    @Override
    public void writeProperties(HashMap<String, Object> properties) {
        super.writeProperties(properties);
        properties.put(TEXT_PROPERTY, getText());
        properties.put(WIDTH_PROPERTY, getWidth());
        properties.put(HEIGHT_PROPERTY, getHeight());
    }

    public void setSize(int width, int height) {
        RelativeLayout.LayoutParams lp = getLayoutParams();
        getLayoutParams().width = width;
        getLayoutParams().height = height;
        requestLayout();
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

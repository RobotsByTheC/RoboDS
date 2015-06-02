package littlebot.robods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by raystubbs on 23/03/15.
 */
public class BasicButton extends ButtonView {

    private final float CORNER_ROUNDNESS = 10;

    private int pressedColor = Color.LTGRAY;
    private int releasedColor = Color.GRAY;

    private Paint paint;
    private RectF drawBounds;

    public BasicButton(Context context) {
        super(context);
        init();
        this.setLayoutParams(new ViewGroup.LayoutParams(100, 50));
    }

    public BasicButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasicButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init(){
        setBackgroundColor(Color.alpha(0));
        paint = new Paint();
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);

        drawBounds = new RectF(0, 0, w, h);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isPressed()){
            paint.setColor(pressedColor);
        }else{
            paint.setColor(releasedColor);
        }

        canvas.drawRoundRect(drawBounds, CORNER_ROUNDNESS, CORNER_ROUNDNESS, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(getText(), this.getWidth()/2, this.getHeight()/2, paint);

        if(isSelected()){
            paint.setColor(Color.argb(125, 0, 255, 0));
            canvas.drawRoundRect(drawBounds, CORNER_ROUNDNESS, CORNER_ROUNDNESS, paint);
        }

    }

    @Override
    public ControlView clone() {
        return new BasicButton(this.getContext());
    }

    @Override
    public String getControlType() {
        return "Basic Button";
    }

    @Override
    public void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        View dialogView = ((Activity)this.getContext()).getLayoutInflater().inflate(R.layout.basic_button_dialog, null);

        final TextView joyNumTV = (TextView)dialogView.findViewById(R.id.joy_num_text);
        final TextView buttonNumTV = (TextView)dialogView.findViewById(R.id.button_num_text);
        final TextView textTV = (TextView)dialogView.findViewById(R.id.button_text);
        final TextView widthTV = (TextView)dialogView.findViewById(R.id.width_text);
        final TextView heightTV = (TextView)dialogView.findViewById(R.id.height_text);

        joyNumTV.setText(Integer.toString(this.getJoystickNumber()));
        buttonNumTV.setText(Integer.toString(this.getButtonNumber()));
        textTV.setText(getText());

        ViewGroup.LayoutParams lp = this.getLayoutParams();
        widthTV.setText(Integer.toString(lp.width));
        heightTV.setText(Integer.toString(lp.height));

        builder.setView(dialogView);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    setJoystickNumber(Integer.parseInt(joyNumTV.getText().toString()));
                    setButtonNumber(Integer.parseInt(buttonNumTV.getText().toString()));
                    setText(textTV.getText().toString());
                    getLayoutParams().width = Integer.parseInt(widthTV.getText().toString());
                    getLayoutParams().height = Integer.parseInt(heightTV.getText().toString());
                    requestLayout();
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

    @Override
    public boolean onTouchEvent(MotionEvent event){


        if(!isEditing()){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    setPressed(false);
                    break;
            }
            invalidate();
        }

        return super.onTouchEvent(event);
    }
}

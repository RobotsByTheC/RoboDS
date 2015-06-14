package littlebot.robods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by raystubbs on 24/03/15.
 */
public class EnableButton extends View {

    private Drawable enabledDrawable, disabledDrawable;
    private boolean enabled = false;

    private ArrayList<EnableListener> listeners = new ArrayList<EnableListener>();

    public EnableButton(Context context) {
        super(context);
        init(context);
    }

    public EnableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EnableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        int size = (int) context.getResources().getDimension(R.dimen.action_icon_size);
        this.setLayoutParams(new ViewGroup.LayoutParams(size, size));

        enabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.enable_button_enabled, null);
        disabledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.enable_button_disabled, null);

        Rect bounds = new Rect(0, 0, size, size);
        enabledDrawable.setBounds(bounds);
        disabledDrawable.setBounds(bounds);

        this.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                enabled = !enabled;

                for (EnableListener l : listeners) {
                    if (enabled)
                        l.onEnabled();
                    else
                        l.onDisabled();
                }

                invalidate();
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (enabled) {
            enabledDrawable.draw(canvas);
        } else {
            disabledDrawable.draw(canvas);
        }
    }

    public void addEnableListener(EnableListener l) {
        listeners.add(l);
    }

    public interface EnableListener {
        void onEnabled();

        void onDisabled();
    }

    public void setEnabled(boolean b) {
        enabled = b;
        invalidate();
    }

    public boolean isEnabled() {
        return enabled;
    }
}

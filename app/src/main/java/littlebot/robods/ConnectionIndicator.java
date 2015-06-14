package littlebot.robods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by raystubbs on 23/03/15.
 */
public class ConnectionIndicator extends View {

    private Drawable connectedDrawable, disconnectedDrawable;
    private boolean connected;

    public ConnectionIndicator(Context context) {
        super(context);
        init(context);
    }

    public ConnectionIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConnectionIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.setLayoutParams(new ViewGroup.LayoutParams(48, 48));

        connectedDrawable = context.getResources().getDrawable(R.drawable.connection_indicator_connected);
        disconnectedDrawable = context.getResources().getDrawable(R.drawable.connection_indicator_disconnected);

        int iconSize = (int) context.getResources().getDimension(R.dimen.action_icon_size);
        connectedDrawable.setBounds(new Rect(0, 0, iconSize, iconSize));
        disconnectedDrawable.setBounds(new Rect(0, 0, iconSize, iconSize));
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (connected)
            connectedDrawable.draw(canvas);
        else
            disconnectedDrawable.draw(canvas);
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        invalidate();
    }
}

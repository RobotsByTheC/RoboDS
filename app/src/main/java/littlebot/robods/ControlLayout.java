package littlebot.robods;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by ben on 6/5/15.
 */
public class ControlLayout extends RelativeLayout {

    public ControlLayout(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void load(DSLayout layout) {
        removeAllViews();

        for (int i = 0; i < layout.getNodeCount(); i++) {
            ControlView newChild = layout.getLayoutNode(i).inflate(getContext());
            addControl(newChild);
        }
    }

    public void save(DSLayout layout) {
        layout.removeAllNodes();
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof ControlView) {
                ControlView control = (ControlView) v;
                layout.addNode(control);
            }
        }
    }

    public void addControl(ControlView control) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(control.getLayoutParams());
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.setMargins((int) control.getXPosition(), (int) control.getYPosition(), -1000000, -1000000);
        control.setLayoutParams(lp);
        if (controlListener != null) {
            controlListener.controlAdded(control);
        }
//        control.requestLayout();
        addView(control);
    }

    public void removeControl(ControlView view) {
        removeView(view);
    }

    private ControlListener controlListener;

    public interface ControlListener {
        void controlAdded(ControlView control);
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }
}

package littlebot.robods;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.common.collect.TreeBasedTable;

/**
 * Represents a layout that contains controls.
 */
public class ControlLayout extends RelativeLayout {

    private static final String TAG = ControlLayout.class.getSimpleName();

    private final TreeBasedTable<Integer, Integer, Float> axes = TreeBasedTable.create();
    private final TreeBasedTable<Integer, Integer, Boolean> buttons = TreeBasedTable.create();
    private final TreeBasedTable<Integer, Integer, Integer> povHats = TreeBasedTable.create();

    private final ControlDatabase controlDatabase = new ControlDatabase();

    public ControlLayout(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void load(DSLayout layout) {
        removeAllViews();

        for (int i = 0; i < layout.getNodeCount(); i++) {
            ControlView newChild = layout.getLayoutNode(i).inflate(getContext(), controlDatabase);
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
        RelativeLayout.LayoutParams lp = control.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lp.setMargins(control.getXPosition(), control.getYPosition(), -10000000, -10000000);
        control.setLayoutParams(lp);
        addView(control);
        if (controlListener != null) {
            controlListener.controlAdded(control);
        }
    }

    public void removeControl(ControlView view) {
        removeView(view);
        if (controlListener != null) {
            controlListener.controlRemoved(view);
        }
    }

    public ControlDatabase getControlDatabase() {
        return controlDatabase;
    }

    private ControlListener controlListener;

    public interface ControlListener {
        void controlAdded(ControlView control);

        void controlRemoved(ControlView control);
    }

    public void setControlListener(ControlListener controlListener) {
        this.controlListener = controlListener;
    }
}

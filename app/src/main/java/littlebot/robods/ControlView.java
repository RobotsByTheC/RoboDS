package littlebot.robods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.HashMap;


/**
 * A {@link View} for a control or indicator in the driver station interface.
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class ControlView extends View implements View.OnLongClickListener {

    public static final int SELECTED_COLOR = Color.argb(125, 0, 255, 0);

    private static final String X_POSITION_PROPERTY = "x_pos";
    private static final String Y_POSITION_PROPERTY = "y_pos";

    private int xPosition, yPosition;

    private boolean editing = true;
    private boolean selected = false;

    private SelectedListener selectedListener;
    private EditListener editListener;
    private ViewGroup editDialogLayout;
    private Dialog editDialog;

    public ControlView(Context context) {
        super(context);
        init(editDialogLayout);
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(editDialogLayout);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(editDialogLayout);
    }

    private void init(ViewGroup editDialogLayout) {
        this.editDialogLayout = editDialogLayout;
        setBackgroundColor(Color.WHITE);
        setOnLongClickListener(this);
        setHapticFeedbackEnabled(editing);
        setLayoutParams(new RelativeLayout.LayoutParams(100, 100));
    }

    @Override
    protected abstract void onDraw(Canvas canvas);

    public abstract String getControlType();

    public void setEditDialogLayout(@Nullable final ViewGroup editDialogLayout) {
        this.editDialogLayout = editDialogLayout;

        if (editDialogLayout != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(getControlType());
            builder.setView(editDialogLayout);
            builder.setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    onEdit(editDialogLayout);
                    if (editListener != null) {
                        editListener.onEdit(editDialogLayout);
                    }
                }
            });
            builder.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            editDialog = builder.create();
        } else {
            editDialog = null;
        }
    }

    protected void resetEditDialog() {
    }

    public Dialog getEditDialog() {
        resetEditDialog();
        return editDialog;
    }

    public void setEditListener(@Nullable EditListener l) {
        this.editListener = l;
    }

    public void setSelectedListener(@Nullable SelectedListener l) {
        selectedListener = l;
    }

    @Override
    public boolean onLongClick(View v) {
        if (editing) {
            startDrag(null, new View.DragShadowBuilder(this), this, 0);
        }

        return true;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
        setHapticFeedbackEnabled(editing);
    }

    public boolean isEditing() {
        return editing;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selectedListener != null) {
            if (selected) {
                selectedListener.selected(this);
            } else {
                selectedListener.deselected(this);
            }
        }

        invalidate();
    }

    public DSLayoutNode toLayoutNode() {
        HashMap<String, Object> properties = new HashMap<>();
        writeProperties(properties);
        return new DSLayoutNode(getClass(), properties);
    }

    /**
     * Read properties for this control from a {@link HashMap}. This is called when a {@link
     * ControlView} is created from a saved layout. Subclasses should override this method to add
     * their own properties, but should make sure to call the super-implementation.
     *
     * @param properties the property map
     */
    public void readProperties(HashMap<String, Object> properties) {
        setPosition((Integer) properties.get(X_POSITION_PROPERTY), (Integer) properties.get(Y_POSITION_PROPERTY));
    }

    /**
     * Write properties from this control to the {@link HashMap}.Subclasses should override this
     * method to add their own properties, but should make sure to call the super-implementation.
     *
     * @param properties the property map
     */
    public void writeProperties(HashMap<String, Object> properties) {
        properties.put(X_POSITION_PROPERTY, getXPosition());
        properties.put(Y_POSITION_PROPERTY, getYPosition());
    }

    @Override
    public RelativeLayout.LayoutParams getLayoutParams() {
        return (RelativeLayout.LayoutParams) super.getLayoutParams();
    }

    public abstract void onEdit(ViewGroup editDialogLayout);

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        if (isEditing()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setSelected(true);
            }
        }

        return super.onTouchEvent(event);
    }

    public interface SelectedListener {
        void selected(ControlView v);

        void deselected(ControlView v);
    }

    /**
     * Gets the x position of this control in the control layout.
     *
     * @return the x position
     */
    public int getXPosition() {
        return xPosition;
    }

    /**
     * Gets the y position of this control in the control layout.
     *
     * @return the y position
     */
    public int getYPosition() {
        return yPosition;
    }

    public void setPosition(int x, int y) {
        xPosition = x - getWidth() / 2;
        yPosition = y - getHeight() / 2;
        RelativeLayout.LayoutParams lp = getLayoutParams();
        lp.setMargins(xPosition, yPosition, -1000000, -1000000);
        requestLayout();
    }

    public interface EditListener {
        void onEdit(ViewGroup editDialogLayout);
    }
}

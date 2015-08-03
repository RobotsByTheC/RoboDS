package littlebot.robods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import littlebot.robods.control.IntegerProperty;
import littlebot.robods.control.Property;


/**
 * A {@link View} for a control or indicator in the driver station interface.
 * This view cannot be inflated as part of an xml layout to make things easier
 * and this capability
 *
 * @author raystubbs
 * @author Ben Wolsieffer
 */
public abstract class ControlView extends View implements View.OnLongClickListener {

    public static final int SELECTED_COLOR = Color.argb(125, 0, 255, 0);

    private final ArrayList<Property> properties = new ArrayList<>(3);

    private final IntegerProperty xPosition = new IntegerProperty("X Position", 0);
    private final IntegerProperty yPosition = new IntegerProperty("Y Position", 0);

    private boolean editing;
    private boolean selected = false;

    private SelectedListener selectedListener;
    private EditListener editListener;
    private Dialog editDialog;
    private ControlDatabase controlDatabase;

    public ControlView(Context context, ControlDatabase controlDatabase) {
        super(context);
        this.controlDatabase = controlDatabase;

        addProperty(xPosition);
        addProperty(yPosition);

        setBackgroundColor(Color.WHITE);
        setOnLongClickListener(this);
        setEditing(false);
        setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
    }

    @Override
    protected abstract void onDraw(Canvas canvas);

    public abstract String getControlType();

    public void setEditDialogLayout(@Nullable final ViewGroup editDialogLayout) {
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

    public ControlDatabase getControlDatabase() {
        return controlDatabase;
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

    protected void addProperty(Property property) {
        properties.add(property);
    }

    /**
     * Read properties for this control from a {@link HashMap}. This is called
     * when a {@link ControlView} is created from a saved layout. Subclasses
     * should override this method to add their own properties, but should make
     * sure to call the super-implementation.
     *
     * @param propMap the property map
     */
    public void readProperties(HashMap<String, Object> propMap) {
        for (Property p : properties) {
            p.read(propMap);
        }
        updateControlPosition();
    }

    /**
     * Write properties from this control to the {@link HashMap}.
     *
     * @param propMap the property map
     */
    public void writeProperties(HashMap<String, Object> propMap) {
        for (Property p : properties) {
            p.write(propMap);
        }
    }

    @Override
    public RelativeLayout.LayoutParams getLayoutParams() {
        return (RelativeLayout.LayoutParams) super.getLayoutParams();
    }

    public abstract void onEdit(ViewGroup editDialogLayout);

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isEditing()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setSelected(true);
                    break;

            }
        }

        return super.onTouchEvent(event);
    }



    @Override
    public boolean onDragEvent(DragEvent event) {
        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_ENDED:
                updatePositionProperties();
                break;
        }

        return super.onDragEvent(event);
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
        return xPosition.getValue();
    }

    /**
     * Gets the y position of this control in the control layout.
     *
     * @return the y position
     */
    public int getYPosition() {
        return yPosition.getValue();
    }

    private void updatePositionProperties() {
        RelativeLayout.LayoutParams lp = getLayoutParams();
        xPosition.setValue(lp.leftMargin);
        yPosition.setValue(lp.topMargin);
    }

    private void updateControlPosition() {
        RelativeLayout.LayoutParams lp = getLayoutParams();
        lp.setMargins(xPosition.getValue(), yPosition.getValue(), -1000000, -1000000);
    }

    public void setPosition(int x, int y) {
        xPosition.setValue(x - getWidth() / 2);
        yPosition.setValue(y - getHeight() / 2);
        updateControlPosition();
        requestLayout();
    }

    public interface EditListener {
        void onEdit(ViewGroup editDialogLayout);
    }
}

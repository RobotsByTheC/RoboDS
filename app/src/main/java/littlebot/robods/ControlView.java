package littlebot.robods;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public abstract class ControlView extends View implements View.OnLongClickListener{

    private boolean editing = true;
    private boolean selected = false;

    private SelectedListener selectedListener;

    public ControlView(Context context) {
        super(context);
        init(null, 0);
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.setBackgroundColor(Color.WHITE);
        this.setOnLongClickListener(this);
    }

    @Override
    protected abstract void onDraw(Canvas canvas);

    public abstract ControlView clone();

    public abstract String getControlType();

    public abstract void showEditDialog();

    public void setSelectedListener(SelectedListener l){
        selectedListener = l;
    }

    @Override
    public boolean onLongClick(View v) {
        if(editing){
            startDrag(null, new View.DragShadowBuilder(this), this, 0);
        }

        return true;
    }

    public void setEditing(boolean editing){
        this.editing = editing;
    }

    public boolean isEditing(){
        return editing;
    }

    public boolean isSelected() {return selected;}

    public void setSelected(boolean selected) {
        this.selected = selected;
        if(selectedListener != null){
            if(selected)
                selectedListener.selected(this);
            else
                selectedListener.deselected(this);
        }


        invalidate();
    }

    public abstract DSLayoutNode toLayoutNode();

    public abstract Object[] getProperties();

    public abstract void setProperties(Object[] properties);

    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(isEditing()){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                if(isSelected())
                    this.setSelected(false);
                else
                    this.setSelected(true);
            }
        }

        return super.onTouchEvent(event);
    }

    public interface SelectedListener{
        public void selected(ControlView v);
        public void deselected(ControlView v);
    }
}

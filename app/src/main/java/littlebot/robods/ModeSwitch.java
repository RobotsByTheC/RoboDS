package littlebot.robods;

import android.app.ActionBar;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

/**
 * Created by raystubbs on 24/03/15.
 */
public class ModeSwitch extends ToggleButton {

    private ModeChangeListener listener;

    public ModeSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ModeSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModeSwitch(Context context) {
        super(context);
        init();
    }

    private void init(){
        this.setText("Teleop");
        this.setTextOff("Teleop");
        this.setTextOn("Auto");
        this.setLayoutParams(new ActionBar.LayoutParams(200, 48));

        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(listener != null){
                    if(isChecked){
                        listener.onAutoEnabled();
                    }else{
                        listener.onTeleopEnabled();
                    }
                }
            }
        });
    }

    public void setModeChangeListener(ModeChangeListener l){
        listener = l;
    }

    public interface ModeChangeListener{
        void onTeleopEnabled();
        void onAutoEnabled();
    }

    public boolean isAuto(){
        return this.isChecked();
    }
}

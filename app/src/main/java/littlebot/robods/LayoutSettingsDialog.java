package littlebot.robods;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by ben on 6/15/15.
 */
public class LayoutSettingsDialog extends Dialog {

    private View.OnClickListener okListener;

    private TextView name;
    private TextView roboRIOIP;
    private RadioButton orientLandRB;
    private RadioButton orientPortRB;
    private boolean newLayout;
    private String oldName = "";

    public LayoutSettingsDialog(Context context, boolean newLayout) {
        super(context);
        this.newLayout = newLayout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getContext().getResources();
        setTitle(res.getString(R.string.dialog_layout_settings_title));
        setContentView(R.layout.dialog_layout_settings);

        name = (TextView) findViewById(R.id.layout_settings_name);
        roboRIOIP = (TextView) findViewById(R.id.layout_settings_roborio_ip);
        orientLandRB = (RadioButton) findViewById(R.id.layout_settings_orientation_landscape);
        orientPortRB = (RadioButton) findViewById(R.id.layout_settings_orientation_portrait);

        findViewById(R.id.dialog_layout_settings_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LayoutManager.getInstance().getLayoutNames(new LayoutManager.OperationCallback<List<String>>() {
                    @Override
                    public void finished(final List<String> layouts) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                String newName = getName();
                                if (Collections.binarySearch(layouts, newName) >= 0 && (newLayout || !newName.equals(oldName))) {
                                    name.setError(getContext().getResources().getString(R.string.dialog_layout_settings_name_error));
                                } else {
                                    if (okListener != null) {
                                        okListener.onClick(v);
                                    }
                                    dismiss();
                                }
                            }
                        });
                    }
                });
            }
        });
        findViewById(R.id.dialog_layout_settings_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public String getName() {
        return name.getText().toString();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getRoboRIOIP() {
        return roboRIOIP.getText().toString();
    }

    public void setRoboRIOIP(String ip) {
        roboRIOIP.setText(ip);
    }

    public DSLayout.Orientation getOrientation() {
        if (orientLandRB.isChecked()) {
            return DSLayout.Orientation.LANDSCAPE;
        } else {
            return DSLayout.Orientation.PORTRAIT;
        }
    }

    public void setOrientation(DSLayout.Orientation orientation) {
        if (orientation == DSLayout.Orientation.LANDSCAPE) {
            orientLandRB.setChecked(true);
        } else {
            orientPortRB.setChecked(true);
        }
    }

    public void fromLayout(DSLayout layout) {
        setOrientation(layout.getOrientation());
        setName(layout.getName());
        oldName = layout.getName();
        setRoboRIOIP(layout.getRioIP());
    }

    public void toLayout(DSLayout layout) {
        layout.setName(getName());
        layout.setRioIP(getRoboRIOIP());
        layout.setOrientation(getOrientation());
    }

    public void setOkListener(View.OnClickListener okListener) {
        this.okListener = okListener;
    }
}

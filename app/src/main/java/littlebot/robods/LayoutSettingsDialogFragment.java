package littlebot.robods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by ben on 6/15/15.
 */
public class LayoutSettingsDialogFragment extends DialogFragment implements RobotChooserDialogFragment.RobotChooseListener {

    private DialogInterface.OnClickListener okListener;

    private TextView name;
    private TextView roboRIOIP;
    private Button robotChooseButton;
    private RadioButton orientLandRB;
    private RadioButton orientPortRB;
    private boolean newLayout;
    private DSLayout layout;
    private String oldName = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        layout = (DSLayout) args.getSerializable("layout");
        newLayout = args.getBoolean("newLayout", false);

        super.onCreate(savedInstanceState);
        final Resources res = getActivity().getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_layout_settings, null);
        builder.setTitle(res.getString(R.string.layout_settings_title))
                .setView(content)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface di, final int which) {
                        LayoutManager.getInstance().getLayoutNames(new LayoutManager.OperationCallback<List<String>>() {
                            @Override
                            public void finished(final List<String> layouts) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String newName = getName();
                                        if (Collections.binarySearch(layouts, newName) >= 0 && (newLayout || !newName.equals(oldName))) {
                                            name.setError(res.getString(R.string.layout_settings_name_error));
                                        } else {
                                            layout.setName(getName());
                                            layout.setRioIP(getRoboRIOIP());
                                            layout.setOrientation(getOrientation());
                                            if (okListener != null) {
                                                okListener.onClick(di, which);
                                            }
                                            dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        AlertDialog dialog = builder.create();

        name = (TextView) content.findViewById(R.id.layout_settings_name);
        roboRIOIP = (TextView) content.findViewById(R.id.layout_settings_roborio_ip);
        robotChooseButton = (Button) content.findViewById(R.id.layout_settings_robot_choose);
        orientLandRB = (RadioButton) content.findViewById(R.id.layout_settings_orientation_landscape);
        orientPortRB = (RadioButton) content.findViewById(R.id.layout_settings_orientation_portrait);

        robotChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotChooserDialogFragment rcdf = new RobotChooserDialogFragment();
                rcdf.setTargetFragment(LayoutSettingsDialogFragment.this, 0);
                rcdf.show(getFragmentManager(), "Robot Chooser");
            }
        });

        setOrientation(layout.getOrientation());
        setName(layout.getName());
        oldName = layout.getName();
        setRoboRIOIP(layout.getRioIP());

        return dialog;
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

    public void setOkListener(DialogInterface.OnClickListener okListener) {
        this.okListener = okListener;
    }

    public static LayoutSettingsDialogFragment newInstance(DSLayout layout, boolean newLayout) {
        LayoutSettingsDialogFragment lsd = new LayoutSettingsDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("layout", layout);
        args.putBoolean("newLayout", newLayout);
        lsd.setArguments(args);

        return lsd;
    }

    @Override
    public void robotChosen(String name) {
        setRoboRIOIP(name);
    }
}

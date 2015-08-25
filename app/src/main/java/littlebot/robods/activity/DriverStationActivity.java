package littlebot.robods.activity;

/**
 * Author:          Ray Stubbs FIRST Team:      2657
 * <p/>
 * Permission:      Use this code for whatever you want to, copy, modify,
 * whatever.
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import littlebot.robods.ConnectionIndicator;
import littlebot.robods.ConnectionManager;
import littlebot.robods.ControlLayout;
import littlebot.robods.DSLayout;
import littlebot.robods.EnableButton;
import littlebot.robods.LayoutManager;
import littlebot.robods.ModeSwitch;
import littlebot.robods.R;
import littlebot.robods.communication.RobotResolver;


public class DriverStationActivity extends AppCompatActivity {

    private static final String TAG = DriverStationActivity.class.getSimpleName();

    public static final int CONNECTION_PERIOD = 3000;

    private TextView voltageDisplay;
    private ConnectionIndicator connectionIndicator;
    private EnableButton enableButton;
    private ModeSwitch modeSwitch;
    private ControlLayout controlLayout;

    private DSLayout layout;

    private ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutManager.initialize(this);
    }

    public void setupLayout() {
        controlLayout = new ControlLayout(this);
        setContentView(controlLayout);

        RobotResolver.getInstance(this).start();
        LayoutManager.getInstance().getCurrentLayout(new LayoutManager.OperationCallback<DSLayout>() {
            @Override
            public void finished(final DSLayout layout) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Loading Layout: " + String.valueOf(layout));
                        if (layout != null) {
                            DriverStationActivity.this.layout = layout;
                            if(connectionManager != null) {
                                connectionManager.disconnect();
                            }
                            connectionManager = new ConnectionManager(DriverStationActivity.this, layout.getRioIP(), CONNECTION_PERIOD,
                                    controlLayout.getControlDatabase(), connectionIndicator,
                                    modeSwitch, enableButton);

                            controlLayout.load(layout);

                            //Set the orientation
                            int requestedOrientation;
                            switch (layout.getOrientation()) {
                                default:
                                case LANDSCAPE:
                                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
                                    break;
                                case PORTRAIT:
                                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
                                    break;
                            }
                            setRequestedOrientation(requestedOrientation);

                            connectionManager.connect();
                        }
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        /**********Setup UI indicators*******************/
        View indicators = getLayoutInflater().inflate(R.layout.action_bar_control_indicators, null);
        bar.setCustomView(indicators);

        connectionIndicator = (ConnectionIndicator) indicators.findViewById(R.id.connection_indicator);
        voltageDisplay = (TextView) indicators.findViewById(R.id.voltage_display);

        /*********Setup UI action bar controls*****************/
        getMenuInflater().inflate(R.menu.menu_activity_controller, menu);

        menu.findItem(R.id.action_switch_layout).setIntent(new Intent(this, LayoutSwitcherActivity.class));
        menu.findItem(R.id.action_edit_layout).setIntent(new Intent(this, LayoutEditorActivity.class));
        View controls = menu.findItem(R.id.action_controls).getActionView();
        enableButton = (EnableButton) controls.findViewById(R.id.enable_button);
        modeSwitch = (ModeSwitch) controls.findViewById(R.id.mode_switch);


        setupLayout();
        return true;
    }

    private void setVoltage(final int whole, final int decimal) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voltageDisplay.setText(whole + "." + Integer.toString(decimal).substring(1) + "v");
            }
        });
    }

    public void onPause() {
        super.onPause();
        if (connectionManager != null) {
            connectionManager.disconnect();
        }
    }

    public void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
        if (connectionManager != null) {
            connectionManager.connect();
        }
    }
}

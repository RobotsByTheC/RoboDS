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
import android.widget.Toast;

import littlebot.robods.ConnectionIndicator;
import littlebot.robods.ControlDatabase;
import littlebot.robods.ControlLayout;
import littlebot.robods.DSLayout;
import littlebot.robods.EnableButton;
import littlebot.robods.LayoutManager;
import littlebot.robods.ModeSwitch;
import littlebot.robods.R;
import littlebot.robods.communication.DriverStationPacket;
import littlebot.robods.communication.PacketManager;
import littlebot.robods.communication.RIOPacketParser;


public class DriverStationActivity extends AppCompatActivity {

    private static final String TAG = DriverStationActivity.class.getName();

    TextView voltageDisplay;
    ConnectionIndicator connectionIndicator;
    EnableButton enableButton;
    ModeSwitch modeSwitch;
    DriverStationPacket driverStationPacket;
    RIOPacketParser packetParser;
    PacketManager packetManager;
    ControlLayout controlLayout;
    private final ControlDatabase.ControlListener controlListener = new ControlDatabase.ControlListener() {

        private DriverStationPacket.Joystick getJoystick(int index) {
            DriverStationPacket.Joystick j = null;
            if ((j = driverStationPacket.getJoystick(index)) == null) {
                j = new DriverStationPacket.Joystick();
                driverStationPacket.addJoystick(index, j);
            }
            return j;
        }

        @Override
        public void axisRegistered(int joystickIndex, int axisIndex) {
            DriverStationPacket.Joystick j = getJoystick(joystickIndex);
            if (j.getAxisCount() <= axisIndex) {
                j.setAxisCount(axisIndex + 1);
            }
        }

        @Override
        public void buttonRegistered(int joystickIndex, int buttonIndex) {
            DriverStationPacket.Joystick j = getJoystick(joystickIndex);
            if (j.getButtonCount() <= buttonIndex) {
                j.setButtonCount(buttonIndex + 1);
            }
        }

        @Override
        public void povHatRegistered(int joystickIndex, int povHatIndex) {
            DriverStationPacket.Joystick j = getJoystick(joystickIndex);
            if (j.getPOVHatCount() <= povHatIndex) {
                j.setPOVHatCount(povHatIndex + 1);
            }
        }

        @Override
        public void axisValueChanged(int joystickIndex, int axisIndex, float value) {
            driverStationPacket.getJoystick(joystickIndex).setAxisValue(axisIndex, value);
        }

        @Override
        public void buttonStateChanged(int joystickIndex, int buttonIndex, boolean pressed) {
            driverStationPacket.getJoystick(joystickIndex).setButtonPressed(buttonIndex, pressed);
        }

        @Override
        public void povHatAngleChanged(int joystickIndex, int povHatIndex, int angle) {
            driverStationPacket.getJoystick(joystickIndex).setPOVHatAngle(povHatIndex, angle);
        }

        @Override
        public void axisUnregistered(int joystickIndex, int axisIndex) {
            DriverStationPacket.Joystick j = driverStationPacket.getJoystick(joystickIndex);
            if (axisIndex == j.getAxisCount() - 1) {
                j.setAxisCount(axisIndex);
            }
        }

        @Override
        public void buttonUnregistered(int joystickIndex, int buttonIndex) {
            DriverStationPacket.Joystick j = driverStationPacket.getJoystick(joystickIndex);
            if (buttonIndex == j.getButtonCount() - 1) {
                j.setButtonCount(buttonIndex);
            }
        }

        @Override
        public void povHatUnregistered(int joystickIndex, int povHatIndex) {
            DriverStationPacket.Joystick j = driverStationPacket.getJoystick(joystickIndex);
            if (povHatIndex == j.getPOVHatCount() - 1) {
                j.setPOVHatCount(povHatIndex);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutManager.initialize(this);
        driverStationPacket = new DriverStationPacket();
        packetParser = new RIOPacketParser();
        packetManager = new PacketManager(driverStationPacket, packetParser);
        setupLayout();
    }

    public void setupLayout() {
        controlLayout = new ControlLayout(this);
        controlLayout.getControlDatabase().setControlListener(controlListener);
        setContentView(controlLayout);

        LayoutManager.getInstance().getCurrentLayout(new LayoutManager.OperationCallback<DSLayout>() {
            @Override
            public void finished(final DSLayout layout) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, String.valueOf(layout));
                        if (layout != null) {
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
                        }
                        // Start sending packets.  I put this here because it needs to be called after
                        // the actionbar is setup, the onStart and onCreate callbacks are called before
                        // the actionbar is setup.
                        packetManager.start(layout.getRioIP());
                    }
                });
            }
        });

    }


    private static final int ITEM_INDEX_ACTION_BAR_CONTROLS = 0,
            ITEM_INDEX_SWITCH_LAYOUT = 1,
            ITEM_INDEX_EDIT_LAYOUT = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*********Inflate the menu*********************************/
        getMenuInflater().inflate(R.menu.menu_activity_controller, menu);

        menu.findItem(R.id.action_switch_layout).setIntent(new Intent(this, LayoutSwitcherActivity.class));
        menu.findItem(R.id.action_edit_layout).setIntent(new Intent(this, LayoutEditorActivity.class));

        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        /**********Setup UI indicators*******************/
        View indicators = getLayoutInflater().inflate(R.layout.action_bar_control_indicators, null);
        bar.setCustomView(indicators);

        connectionIndicator = (ConnectionIndicator) indicators.findViewById(R.id.connection_indicator);
        voltageDisplay = (TextView) indicators.findViewById(R.id.voltage_display);

        packetManager.setPacketListener(new PacketManager.PacketListener() {
            @Override
            public void onPacketReceived() {

            }

            @Override
            public void onPacketSent(DriverStationPacket packet) {
            }
        });


        /*********Setup UI action bar controls*****************/
        View controls = menu.getItem(ITEM_INDEX_ACTION_BAR_CONTROLS).getActionView();
        enableButton = (EnableButton) controls.findViewById(R.id.enable_button);
        modeSwitch = (ModeSwitch) controls.findViewById(R.id.mode_switch);


        enableButton.addEnableListener(new EnableButton.EnableListener() {
            @Override
            public void onEnabled() {
                driverStationPacket.setEnabled(true);
            }

            @Override
            public void onDisabled() {
                driverStationPacket.setEnabled(false);
            }
        });

        modeSwitch.setModeChangeListener(new ModeSwitch.ModeChangeListener() {
            @Override
            public void onTeleopEnabled() {
                enableButton.setEnabled(false);
                driverStationPacket.setMode(DriverStationPacket.Mode.TELEOPERATED);
            }

            @Override
            public void onAutoEnabled() {
                enableButton.setEnabled(false);
                driverStationPacket.setMode(DriverStationPacket.Mode.AUTONOMOUS);
            }
        });

        /*Setup the packet manager callbacks, this is done here because
        the callbacks need the initialized connectionIndicator and enableButton
         */
        setupPacketManagerCallbacks(packetManager, connectionIndicator, enableButton, voltageDisplay);

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

    private void setConnectionStatus(boolean connected) {
        connectionIndicator.setConnected(connected);
    }

    private void setupPacketManagerCallbacks(final PacketManager pm, final ConnectionIndicator ci,
                                             final EnableButton eb, final TextView vd) {

        pm.setConnectionListener(new PacketManager.ConnectionListener() {
            @Override
            public void onConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ci.setConnected(true);
                        Toast.makeText(DriverStationActivity.this, "Connection Established", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onDisconnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ci.setConnected(false);
                        Toast.makeText(DriverStationActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        eb.setEnabled(false);
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ci.setConnected(false);
                        Toast.makeText(DriverStationActivity.this, "Connection Lost", Toast.LENGTH_SHORT).show();
                        eb.setEnabled(false);
                    }
                });
            }

            @Override
            public void onConnectionFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DriverStationActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                        eb.setEnabled(false);
                    }
                });
            }
        });

    }

    public void onStop() {
        super.onStop();
        if (packetManager != null && packetManager.isRunning()) {
            packetManager.stopSending();
        }
    }

    public void onStart() {
        super.onStart();
        supportInvalidateOptionsMenu();
    }
}

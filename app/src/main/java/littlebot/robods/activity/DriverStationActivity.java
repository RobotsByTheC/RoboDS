package littlebot.robods.activity;

/**
 * Author:          Ray Stubbs
 * FIRST Team:      2657
 * <p/>
 * Permission:      Use this code for whatever you want to, copy, modify, whatever.
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;

import littlebot.robods.ButtonView;
import littlebot.robods.ConnectionIndicator;
import littlebot.robods.ControlLayout;
import littlebot.robods.ControlView;
import littlebot.robods.DSLayout;
import littlebot.robods.DSPacketFactory;
import littlebot.robods.EnableButton;
import littlebot.robods.JoystickView;
import littlebot.robods.LayoutManager;
import littlebot.robods.ModeSwitch;
import littlebot.robods.PacketManager;
import littlebot.robods.R;
import littlebot.robods.RIOPacketParser;


public class DriverStationActivity extends AppCompatActivity {

    private static final String TAG = DriverStationActivity.class.getName();

    TextView voltageDisplay;
    ConnectionIndicator connectionIndicator;
    EnableButton enableButton;
    ModeSwitch modeSwitch;
    DSLayout layout;
    DSPacketFactory packetFactory;
    RIOPacketParser packetParser;
    PacketManager packetManager;
    ControlLayout controlLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutManager.initialize(this);
        packetFactory = new DSPacketFactory();
        packetParser = new RIOPacketParser();
        packetManager = new PacketManager(packetFactory, packetParser);
        setupLayout();
    }

    public void setupLayout() {
        controlLayout = new ControlLayout(this);
        setContentView(controlLayout);

        LayoutManager.getInstance().getCurrentLayout(new LayoutManager.OperationCallback<DSLayout>() {
            @Override
            public void finished(DSLayout l) {
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

                        registerUserControls(packetFactory, controlLayout);
                    }
                });
                layout = l;
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

        if (layout == null)
            return true;


        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        /**********Setup UI indicators*******************/
        View indicators = getLayoutInflater().inflate(R.layout.action_bar_control_indicators, null);
        bar.setCustomView(indicators);

        connectionIndicator = (ConnectionIndicator) indicators.findViewById(R.id.connection_indicator);
        voltageDisplay = (TextView) indicators.findViewById(R.id.voltage_display);

        packetManager.setPacketListener(new PacketManager.PacketListener() {
            @Override
            public void onPacketReceived(DatagramPacket packet) {

            }

            @Override
            public void onPacketSent(DatagramPacket packet) {
                byte[] data = packet.getData();
                setVoltage(data[5], data[6]);
            }
        });


        /*********Setup UI action bar controls*****************/
        View controls = menu.getItem(ITEM_INDEX_ACTION_BAR_CONTROLS).getActionView();
        enableButton = (EnableButton) controls.findViewById(R.id.enable_button);
        modeSwitch = (ModeSwitch) controls.findViewById(R.id.mode_switch);

        packetFactory.registerEnableButton(enableButton);
        packetFactory.registerModeSwitch(modeSwitch);


        enableButton.addEnableListener(new EnableButton.EnableListener() {
            @Override
            public void onEnabled() {
                if (!packetManager.isRunning())
                    packetManager.recoverConnection();
            }

            @Override
            public void onDisabled() {
            }
        });

        modeSwitch.setModeChangeListener(new ModeSwitch.ModeChangeListener() {
            @Override
            public void onTeleopEnabled() {
                if (packetManager.isRunning()) {
                    enableButton.setEnabled(false);
                }
            }

            @Override
            public void onAutoEnabled() {
                if (packetManager.isRunning()) {
                    enableButton.setEnabled(false);
                }
            }
        });

        /*Setup the packet manager callbacks, this is done here because
        the callbacks need the initialized connectionIndicator and enableButton
         */
        setupPacketManagerCallbacks(packetManager, connectionIndicator, enableButton, voltageDisplay);

        /*Start sending packets.  I put this here because it needs to be called after
        the actionbar is setup, the onStart and onCreate callbacks are called before
        the actionbar is setup.
         */
        packetManager.startSending(layout.getRioIP());

        return true;
    }

    private void registerUserControls(DSPacketFactory pf, ViewGroup content) {

        for (int i = 0; i < content.getChildCount(); i++) {
            ControlView newChild = (ControlView) content.getChildAt(i);
            ((ControlView) newChild).setEditing(false);

            if (newChild instanceof JoystickView) {
                pf.registerJoystick((JoystickView) newChild);
            } else if (newChild instanceof ButtonView) {
                pf.registerButton((ButtonView) newChild);
            }

        }
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

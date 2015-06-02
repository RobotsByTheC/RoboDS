package littlebot.robods;

/**
 * Author:          Ray Stubbs
 * FIRST Team:      2657
 *
 * Permission:      Use this code for whatever you want to, copy, modify, whatever.
 */

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;


public class MyActivity extends ActionBarActivity {

    TextView voltageDisplay;
    ConnectionIndicator connectionIndicator;
    EnableButton enableButton;
    ModeSwitch modeSwitch;
    DSLayout layout;
    DSPacketFactory packetFactory;
    RIOPacketParser packetParser;
    PacketManager packetManager;
    ViewGroup contentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packetFactory = new DSPacketFactory();
        packetParser = new RIOPacketParser();
        packetManager = new PacketManager(packetFactory, packetParser);
        setupLayout();

    }

   public void setupLayout(){
       File layoutFile = new File(this.getFilesDir() + "/layouts/Default");

       try {
            layout = DSLayout.fromStream(new FileInputStream(layoutFile));
            contentView = layout.inflate(this);
       } catch (FileNotFoundException e) {
            contentView = new AbsoluteLayout(this);
        }

       contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                   ViewGroup.LayoutParams.MATCH_PARENT));

       this.setContentView(contentView);

       if(layout != null){
           //Set the orientation, always landscape for now; switch is for future use
           switch(layout.getOrientation()){
               case ORIENTATION_LANDSCAPE:
                   this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                   break;
               case ORIENTATION_PORTRAIT:
                   this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                   break;
               case ORIENTATION_SENSOR:
                   this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
           }
       }
        registerUserControls(packetFactory, contentView);
   }



    private final int ITEM_INDEX_ACTION_BAR_CONTROLS = 0,
                      ITEM_INDEX_SWITCH_LAYOUT = 1,
                      ITEM_INDEX_EDIT_LAYOUT = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*********Inflate the menu*********************************/
        getMenuInflater().inflate(R.menu.layout_controller, menu);

        menu.getItem(ITEM_INDEX_SWITCH_LAYOUT).setIntent(new Intent(this, LayoutSwitcherActivity.class));
        menu.getItem(ITEM_INDEX_EDIT_LAYOUT).setIntent(new Intent(this, LayoutEditorActivity.class));

        if(layout == null)
            return true;


        ActionBar bar = this.getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        /**********Setup UI indicators*******************/
        View indicators = this.getLayoutInflater().inflate(R.layout.action_bar_control_indicators, null);
        bar.setCustomView(indicators);

        connectionIndicator = (ConnectionIndicator)indicators.findViewById(R.id.connection_indicator);
        voltageDisplay = (TextView)indicators.findViewById(R.id.voltage_display);

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
        View controls =  menu.getItem(ITEM_INDEX_ACTION_BAR_CONTROLS).getActionView();
        enableButton = (EnableButton)controls.findViewById(R.id.enable_button);
        modeSwitch = (ModeSwitch)controls.findViewById(R.id.mode_switch);

        packetFactory.registerEnableButton(enableButton);
        packetFactory.registerModeSwitch(modeSwitch);


        enableButton.addEnableListener(new EnableButton.EnableListener() {
            @Override
            public void onEnabled() {
                if(!packetManager.isRunning())
                    packetManager.recoverConnection();
            }

            @Override
            public void onDisabled() {}
        });

        modeSwitch.setModeChangeListener(new ModeSwitch.ModeChangeListener() {
            @Override
            public void onTeleopEnabled() {
                if(packetManager.isRunning()){
                    enableButton.setEnabled(false);
                }
            }

            @Override
            public void onAutoEnabled() {
                if(packetManager.isRunning()){
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

    private void registerUserControls(DSPacketFactory pf, ViewGroup content){

        for(int i = 0 ; i < content.getChildCount() ; i++){
            ControlView newChild = (ControlView)content.getChildAt(i);
            ((ControlView)newChild).setEditing(false);

            if(newChild instanceof JoystickView){
                pf.registerJoystick((JoystickView)newChild);
            }else if(newChild instanceof ButtonView){
                pf.registerButton((ButtonView)newChild);
            }

        }
    }

    private void setVoltage(final int whole, final int decimal){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voltageDisplay.setText(whole + "." + Integer.toString(decimal).substring(1) + "v");
            }
        });

    }

    private void setConnectionStatus(boolean connected){
        connectionIndicator.setConnected(connected);
    }

    private void setupPacketManagerCallbacks(final PacketManager pm, final ConnectionIndicator ci,
                                             final EnableButton eb, final TextView vd){

        pm.setConnectionListener(new PacketManager.ConnectionListener() {
            @Override
            public void onConnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ci.setConnected(true);
                        Toast.makeText(MyActivity.this, "Connection Established", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onDisconnect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ci.setConnected(false);
                        Toast.makeText(MyActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MyActivity.this, "Connection Lost", Toast.LENGTH_SHORT).show();
                        eb.setEnabled(false);
                    }
                });
            }

            @Override
            public void onConnectionFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
                        eb.setEnabled(false);
                    }
                });
            }
        });

    }

    public void onStop(){
        super.onStop();
        if(packetManager != null && packetManager.isRunning()){
            packetManager.stopSending();
        }
    }

    public void onStart(){
        super.onStart();
        invalidateOptionsMenu();
    }

    public void onResume(){
        super.onResume();
        //setupLayout();
    }
}

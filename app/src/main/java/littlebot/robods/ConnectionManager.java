package littlebot.robods;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import littlebot.robods.communication.DriverStationPacket;
import littlebot.robods.communication.PacketManager;
import littlebot.robods.communication.RobotPacket;
import littlebot.robods.communication.RobotResolver;

/**
 * @author Ben Wolsieffer
 */
public class ConnectionManager {

    private static final String TAG = ConnectionManager.class.getSimpleName();

    private final ControlDatabase.ControlListener controlListener = new ControlDatabase.ControlListener() {

        private DriverStationPacket.Joystick getJoystick(int index) {
            DriverStationPacket.Joystick j;
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

    private final DriverStationPacket driverStationPacket;
    private final RobotPacket robotPacket;
    private final PacketManager packetManager;

    private final Timer connectionTimer = new Timer();

    private final Context context;

    private final String roboRIOName;
    private final int connectionPeriod;

    public ConnectionManager(final Context context, final String roboRIOName, final int connectionPeriod,
                             final ControlDatabase controlDatabase, final ConnectionIndicator connectionIndicator,
                             final ModeSwitch modeSwitch, final EnableButton enableButton) {
        this.context = context;
        this.roboRIOName = roboRIOName;
        this.connectionPeriod = connectionPeriod;

        driverStationPacket = new DriverStationPacket();
        robotPacket = new RobotPacket();
        packetManager = new PacketManager(driverStationPacket, robotPacket);

        controlDatabase.setControlListener(controlListener);
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
        packetManager.setConnectionListener(new PacketManager.ConnectionListener() {
            @Override
            public void onConnect() {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        connectionIndicator.setConnected(true);
                        Toast.makeText(context, "Connection Established", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        connectionIndicator.setConnected(false);
                        Toast.makeText(context, "Connection Lost", Toast.LENGTH_SHORT).show();
                        enableButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onConnectionFailed() {
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Connection Failed", Toast.LENGTH_SHORT).show();
                        enableButton.setEnabled(false);
                    }
                });
            }
        });
    }

    public void connect() {
        if (!packetManager.isRunning()) {
            // Start a timer that attempt a connection on a regular basis
            connectionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        RobotResolver resolver = RobotResolver.getInstance(context);
                        InetAddress robotAddress = resolver.resolve(roboRIOName);
                        packetManager.start(robotAddress);
                        resolver.stop();
                        cancel();
                    } catch (UnknownHostException e) {
                        Log.d(TAG, "Could not resolve robot IP: " + e);
                    } catch (IOException ex) {
                        Log.e(TAG, "Failed to initialize networking: " + ex);
                    }
                }
            }, 0, connectionPeriod);
        }
    }

    public void disconnect() {
        if (packetManager.isRunning()) {
            RobotResolver.getInstance(context).stop();
            packetManager.stop();
        }
    }
}

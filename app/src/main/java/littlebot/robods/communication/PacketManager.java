package littlebot.robods.communication;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by raystubbs on 18/05/15.
 */
public class PacketManager {

    //In these names ds means received by the driverstation and rio means sent to the rio
    private final int ROBOT_PORT = 1110, DS_PORT = 1150;
    private final int DISCONNECT_DELAY = 1000; //One second


    //Status
    private volatile boolean connected, running;

    //Data handlers
    private final DriverStationPacket driverStationPacket;
    private RIOPacketParser packetParser;

    //Event listeners
    private ConnectionListener connectionListener;
    private PacketListener packetListener;

    private final Timer sendTimer = new Timer();

    public PacketManager(DriverStationPacket dsFact, RIOPacketParser rioParc) {
        driverStationPacket = dsFact;
        packetParser = rioParc;
    }

    public void start(final String robotAddressStr) {
        try {
            InetAddress robotAddress = InetAddress.getByName(robotAddressStr);

            final DatagramSocket dsSocket = new DatagramSocket();

            final DatagramPacket dsPacket = new DatagramPacket(new byte[]{}, 0, robotAddress, ROBOT_PORT);
            running = true;
            sendTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (running) {
                        try {
                            driverStationPacket.toDatagramPacket(dsPacket);
                            dsSocket.send(dsPacket);
                            onPacketSent(driverStationPacket);
                        } catch (IOException e) {
                            onConnectionLost();
                            stopSending();
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            }, 0, 20);
        } catch (UnknownHostException e) {
            Log.e("Host Error: ", e.getMessage());
            onConnectionFailed();
        } catch (IOException e) {
            onConnectionLost();
            stopSending();
        }
    }


    public void stopSending() {
        running = false;
    }

    protected void onConnect() {
        if (connectionListener != null) {
            connectionListener.onConnect();
        }
    }

    protected void onDisconnect() {
        if (connectionListener != null) {
            connectionListener.onDisconnect();
        }
    }

    protected void onConnectionFailed() {
        if (connectionListener != null) {
            connectionListener.onConnectionFailed();
        }
    }

    protected void onConnectionLost() {
        if (connectionListener != null) {
            connectionListener.onConnectionLost();
        }
    }

    protected void onPacketReceived(DatagramPacket packet) {
        if (packetListener != null) {
            packetListener.onPacketReceived();
        }
    }

    protected void onPacketSent(DriverStationPacket packet) {
        if (packetListener != null) {
            packetListener.onPacketSent(packet);
        }
    }


    public boolean isConnected() {
        return connected;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConnectionListener(ConnectionListener l) {
        connectionListener = l;
    }

    public void setPacketListener(PacketListener l) {
        packetListener = l;
    }


    /**
     * ************************Listener definitions*********************************
     */
    public interface ConnectionListener {
        void onConnect();

        void onDisconnect();

        void onConnectionLost();

        void onConnectionFailed();
    }

    public interface PacketListener {
        void onPacketReceived();

        void onPacketSent(DriverStationPacket packet);
    }


}

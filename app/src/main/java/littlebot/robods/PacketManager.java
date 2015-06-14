package littlebot.robods;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by raystubbs on 18/05/15.
 */
public class PacketManager {

    //In these names ds means received by the driverstation and rio means sent to the rio
    private final int RIO_PORT = 1110, DS_PORT = 1150;
    private final int DISCONNECT_DELAY = 1000; //Two seconds
    private final int MAX_DS_PACKET_SIZE = 43;
    private InetAddress rioAddr;
    private DatagramSocket rioSocket, dsSocket;

    //Status
    private volatile boolean connected, running;

    //Data handlers
    private DSPacketFactory packetFactory;
    private RIOPacketParser packetParser;

    //Event listeners
    private ConnectionListener connectionListener;
    private PacketListener packetListener;

    public PacketManager(DSPacketFactory dsFact, RIOPacketParser rioParc) {
        packetFactory = dsFact;
        packetParser = rioParc;
    }

    private boolean connect(InetAddress rio) throws SocketException, IOException {
        rioAddr = rio;
        rioSocket = new DatagramSocket();
        dsSocket = new DatagramSocket(DS_PORT);
        dsSocket.setSoTimeout(DISCONNECT_DELAY);
        DatagramPacket dsPacket = new DatagramPacket(new byte[MAX_DS_PACKET_SIZE], MAX_DS_PACKET_SIZE);

        int connectionAttempts = 0;
        while (connectionAttempts < 5) {
            byte[] rioData = packetFactory.getConnectionPacket();
            DatagramPacket rioPacket = new DatagramPacket(rioData, rioData.length, rioAddr, RIO_PORT);
            rioSocket.send(rioPacket);
            try {
                dsSocket.receive(dsPacket);
            } catch (InterruptedIOException e) {
                connectionAttempts++;
                continue;
            }
            connected = true;
            return connected;
        }
        return false;
    }

    public void startSending(final String sRioAddr) {
        running = true;
        new Thread() {
            public void run() {
                try {
                    rioAddr = InetAddress.getByName(sRioAddr);
                } catch (UnknownHostException e) {
                    Log.e("Host Error: ", e.getMessage());
                    onConnectionFailed();
                    running = false;
                    return;
                }
                try {
                    connected = connect(rioAddr);
                } catch (IOException e) {
                    Log.e("Connection Error: ", e.getMessage());
                    onConnectionFailed();
                    running = false;
                    return;
                }

                if (!connected) {
                    onConnectionFailed();
                    running = false;
                    return;
                } else {
                    onConnect();
                }

                while (connected && running) {
                    byte[] rioData = packetFactory.getPacket();
                    DatagramPacket rioPacket = new DatagramPacket(rioData, rioData.length, rioAddr, RIO_PORT);
                    try {
                        rioSocket.send(rioPacket);
                        onPacketSent(rioPacket);
                    } catch (IOException e) {
                        onConnectionLost();
                        stopSending();
                    }

                    try {
                        DatagramPacket dsPacket = new DatagramPacket(new byte[MAX_DS_PACKET_SIZE], MAX_DS_PACKET_SIZE);
                        dsSocket.receive(dsPacket);
                        onPacketReceived(dsPacket);
                    } catch (InterruptedIOException e) {
                        //If the rio takes too long to respond
                        onConnectionLost();
                        stopSending();
                    } catch (IOException e) {
                        onConnectionLost();
                        stopSending();
                    }
                }
            }
        }.start();
    }

    public void recoverConnection() {

        //Remove preceding '/' from string address
        String addr = rioAddr.toString().substring(1);
        startSending(addr);
    }

    protected void stopSending() {
        running = false;
        connected = false;
        onDisconnect();
        rioSocket.close();
        dsSocket.close();
    }

    protected void onConnect() {
        if (connectionListener != null)
            connectionListener.onConnect();
    }

    protected void onDisconnect() {
        if (connectionListener != null)
            connectionListener.onDisconnect();
    }

    protected void onConnectionFailed() {
        if (connectionListener != null)
            connectionListener.onConnectionFailed();
    }

    protected void onConnectionLost() {
        if (connectionListener != null)
            connectionListener.onConnectionLost();
    }

    protected void onPacketReceived(DatagramPacket packet) {
        if (packetListener != null)
            packetListener.onPacketReceived(packet);
    }

    protected void onPacketSent(DatagramPacket packet) {
        if (packetListener != null)
            packetListener.onPacketSent(packet);
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
        void onPacketReceived(DatagramPacket packet);

        void onPacketSent(DatagramPacket packet);
    }


}

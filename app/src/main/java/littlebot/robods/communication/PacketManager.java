package littlebot.robods.communication;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by raystubbs on 18/05/15.
 */
public class PacketManager {

    private static final String TAG = PacketListener.class.getSimpleName();

    //In these names ds means sent by the driverstation and rio means received to the rio
    private final int ROBOT_PORT = 1150, DS_PORT = 1110;
    private final int DISCONNECT_DELAY = 1000; //One second


    //Status
    private volatile boolean connected, running = false, timeSent;

    //Data handlers
    private final DriverStationPacket driverStationPacket;
    private final RobotPacket robotPacket;

    //Event listeners
    private ConnectionListener connectionListener;
    private PacketListener packetListener;

    private DatagramSocket dsSocket;
    private DatagramSocket robotSocket;

    private final Timer sendTimer = new Timer();
    private TimerTask sendTimerTask;
    private Thread receiveThread;

    public PacketManager(DriverStationPacket driverStationPacket, RobotPacket robotPacket) {
        this.driverStationPacket = driverStationPacket;
        this.robotPacket = robotPacket;
    }

    public synchronized void start(final InetAddress robotAddress) throws IOException {
        if (!running) {

            Log.d(TAG, "Trying to connect to: " + robotAddress);

            // Create send/receive UDP sockets
            dsSocket = new DatagramSocket();

            robotSocket = new DatagramSocket(null);
            robotSocket.setReuseAddress(true);
            // Make receiving timeout after a certain time
            robotSocket.setSoTimeout(DISCONNECT_DELAY);
            robotSocket.bind(new InetSocketAddress(ROBOT_PORT));

            final DatagramPacket robotDatagram = new DatagramPacket(new byte[RobotPacket.MAX_LENGTH], RobotPacket.MAX_LENGTH);
            final DatagramPacket driverStationDatagram = new DatagramPacket(new byte[]{}, 0, robotAddress, DS_PORT);

            running = true;

            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        try {
                            robotSocket.receive(robotDatagram);
                            onConnect();
                            onPacketReceived(robotPacket);
                        } catch (SocketTimeoutException e) {
                            // When the timeout occurs, the robot isn't
                            // responding, or we aren't connected yet.
                            onConnectionLost();
                        } catch (IOException e) {
                            // We don't care if an exception occurred if the
                            // socket was closed
                            if (!robotSocket.isClosed()) {
                                onConnectionLost();
                            }
                        }
                    }
                }
            });
            receiveThread.start();
            sendTimer.schedule(sendTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (running) {
                        try {
                            if (connected && !timeSent) {
                                driverStationPacket.addTime();
                                timeSent = true;
                            }
                            driverStationPacket.toDatagramPacket(driverStationDatagram);
                            dsSocket.send(driverStationDatagram);
                            onPacketSent(driverStationPacket);
                        } catch (IOException e) {
                            if (!dsSocket.isClosed()) {
                                onConnectionLost();
                            }
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            }, 0, 20);
        }
    }

    public synchronized void stop() {
        if (running) {
            // Reset flags
            connected = false;
            timeSent = false;
            running = false;

            // Close the transmit socket
            dsSocket.close();

            // Close receiving socket
            sendTimerTask.cancel();
            sendTimer.purge();
            robotSocket.close();
            // Wait for receiving thread to finish (should be fast because
            // socket was closed)
            while (receiveThread.isAlive()) {
                try {
                    receiveThread.join();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    protected void onConnect() {
        if (!connected) {
            connected = true;
            timeSent = false;
            if (connectionListener != null) {
                connectionListener.onConnect();
            }
        }
    }

    protected void onConnectionLost() {
        if (connected) {
            connected = false;
            if (connectionListener != null) {
                connectionListener.onConnectionLost();
            }
        }
    }

    protected void onPacketReceived(RobotPacket packet) {
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

    public synchronized void setConnectionListener(ConnectionListener l) {
        connectionListener = l;
    }

    public synchronized void setPacketListener(PacketListener l) {
        packetListener = l;
    }


    /**
     * ************************Listener definitions*********************************
     */
    public interface ConnectionListener {
        void onConnect();

        void onConnectionLost();

        void onConnectionFailed();
    }

    public interface PacketListener {
        void onPacketReceived();

        void onPacketSent(DriverStationPacket packet);
    }
}

package littlebot.robods.communication;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ben Wolsieffer
 */
public final class RobotResolver {

    public interface RobotListener {
        void robotFound(String name, InetAddress address);

        void robotLost(String name);
    }

    private static final String TAG = RobotResolver.class.getSimpleName();

    public static final String TYPE = "_ssh._tcp.";

    private final NsdManager nsdManager;
    private final NsdManager.DiscoveryListener discoveryListener;

    private final Map<String, InetAddress> robots = Collections.synchronizedMap(new HashMap<String, InetAddress>());
    private final Map<String, InetAddress> unmodifiableRobots = Collections.unmodifiableMap(robots);

    private RobotListener robotListener;

    private boolean running;

    private RobotResolver(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        final NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Failed to resolve IP for " + serviceInfo.getServiceName() + ": " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                InetAddress robotAddress = serviceInfo.getHost();
                Log.i(TAG, "IP for " + serviceInfo.getServiceName() + " is " + robotAddress.getHostAddress());
                String name = serviceInfo.getServiceName();
                InetAddress address = serviceInfo.getHost();
                if (robots.put(name, address) == null && robotListener != null) {
                    robotListener.robotFound(name, address);
                }
            }
        };

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "roboRIO discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                final String name = service.getServiceName();
                Log.i(TAG, name + " found");
                nsdManager.resolveService(service, resolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                final String name = service.getServiceName();
                Log.d(TAG, name + " can no longer be found");
                if (robots.remove(name) != null && robotListener != null) {
                    robotListener.robotLost(service.getServiceName());
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "roboRIO discovery stopped");
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "roboRIO discovery failed");
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Failed to stop roboRIO discovery");
                nsdManager.stopServiceDiscovery(this);
            }
        };
        start();
    }

    public void start() {
        if (!running) {
            running = true;
            nsdManager.discoverServices(TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }
    }

    public void stop() {
        if (running) {
            nsdManager.stopServiceDiscovery(discoveryListener);
            running = false;
        }
    }

    public void setRobotListener(RobotListener robotListener) {
        this.robotListener = robotListener;
    }

    public InetAddress resolve(String name) throws UnknownHostException {
        InetAddress address = robots.get(name);
        if (address == null) {
            if (name.endsWith(".local")) {
                address = robots.get(name.substring(0, name.length() - 6));
            }
            if (address == null) {
                address = InetAddress.getByName(name);
            }
        }

        if (address != null) {
            return address;
        } else {
            throw new UnknownHostException("Could not resolve robot: " + name);
        }
    }

    public Map<String, InetAddress> getRobots() {
        return unmodifiableRobots;
    }

    private static RobotResolver instance;

    public synchronized static RobotResolver getInstance(Context context) {
        if (instance == null) {
            instance = new RobotResolver(context);
        }
        return instance;
    }
}

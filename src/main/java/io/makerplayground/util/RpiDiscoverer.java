package io.makerplayground.util;

import io.makerplayground.generator.upload.UploadTarget;
import javafx.collections.ObservableList;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpiDiscoverer {

    final ObservableList<UploadTarget> hostList;
    private ScheduledThreadPoolExecutor executor;

    public RpiDiscoverer(ObservableList<UploadTarget> hostList) {
        this.hostList = hostList;
    }

    public void startScan() {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(512);
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            List<InterfaceAddress> addressList = new ArrayList<>();
            for (NetworkInterface networkInterface : Collections.list(nets)) {
                try {
                    if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                        addressList.addAll(networkInterface.getInterfaceAddresses());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            addressList.sort(Comparator.comparingInt(InterfaceAddress::getNetworkPrefixLength).reversed());

            List<String> allHosts = new ArrayList<>();
            for (InterfaceAddress addr: addressList) {
                if (addr.getBroadcast() == null) { // IPv6
                    continue;
                }
                SubnetUtils utils = new SubnetUtils(addr.getAddress().getHostAddress()+"/"+addr.getNetworkPrefixLength());
                Collections.addAll(allHosts, utils.getInfo().getAllAddresses());
            }

            double i = 0;
            double timestep = 5000.0 / allHosts.size();
            for (String host: allHosts) {
                executor.scheduleAtFixedRate(new RpiServiceChecker(host, hostList), (long) i, 10000, TimeUnit.MILLISECONDS);
                i += timestep;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return executor != null && !executor.isTerminated();
    }

    public void stopScan() {
        executor.shutdownNow();
    }
}

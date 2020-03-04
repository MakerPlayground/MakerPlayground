package io.makerplayground.util;

import io.makerplayground.generator.upload.UploadTarget;
import javafx.collections.ObservableList;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpiDiscoveryThread extends Thread {

    final ObservableList<UploadTarget> hostList;
    private ScheduledThreadPoolExecutor executor;

    public RpiDiscoveryThread(ObservableList<UploadTarget> hostList) {
        this.hostList = hostList;
    }

    @Override
    public void run() {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(256);
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

            for (InterfaceAddress addr: addressList) {
                if (addr.getBroadcast() == null) { // IPv6
                    continue;
                }
                SubnetUtils utils = new SubnetUtils(addr.getAddress().getHostAddress()+"/"+addr.getNetworkPrefixLength());
                String[] allHosts = utils.getInfo().getAllAddresses();
                for (String host: allHosts) {
                    executor.scheduleAtFixedRate(new RpiServiceChecker(host, hostList), 0, 15000, TimeUnit.MILLISECONDS);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        executor.shutdownNow();
    }
}

package io.makerplayground.util;

import io.makerplayground.generator.upload.UploadConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MakerPlaygroundRpiScanner implements Runnable {

    public static final short MP_PORT = 6212;
    final ObservableList<UploadConnection> hostList = FXCollections.observableList(new ArrayList<>());

    public ObservableList<UploadConnection> getHostList() {
        return hostList;
    }

    @Override
    public void run() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(256);
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
//                System.out.println(addr.getAddress().getHostAddress()+"/"+addr.getNetworkPrefixLength());
                SubnetUtils utils = new SubnetUtils(addr.getAddress().getHostAddress()+"/"+addr.getNetworkPrefixLength());
                String[] allHosts = utils.getInfo().getAllAddresses();
                for (String host: allHosts) {
                    executor.execute(new MakerPlaygroundServiceChecker(host, hostList));
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    private static class MakerPlaygroundServiceChecker implements Runnable {

        private final ObservableList<UploadConnection> availableHostList;
        private final String host;
        private static final String EXPECTED_RESPONSE = "makerplayground";

        public MakerPlaygroundServiceChecker(String host, ObservableList<UploadConnection> availableHostList) {
            this.host = host;
            this.availableHostList = availableHostList;
        }

        @Override
        public void run() {
//            System.out.println("Check : " + this.host);
            boolean isConnected = false;
            try (Socket socket = new Socket()) {
                socket.setReuseAddress(true);
                SocketAddress sa = new InetSocketAddress(this.host, MP_PORT);
                socket.connect(sa, 2500);
                if (socket.isConnected()) {
                    isConnected = true;
                }
            } catch (IOException e) {
                isConnected = false;
                this.availableHostList.removeIf(uploadConnection -> host.equals(uploadConnection.getRpiHostName()));
            }

            if (isConnected) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(String.format("http://%s:%d", this.host, MP_PORT))) // example: "http://192.168.1.43:6212/"
                        .build();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body)
                        .thenAccept(s -> Platform.runLater(() -> {
                            if (EXPECTED_RESPONSE.equals(s)) {
                                if (this.availableHostList.stream().noneMatch(uploadConnection -> host.equals(uploadConnection.getRpiHostName()))) {
                                    this.availableHostList.add(new UploadConnection(this.host));
                                }
                            } else {
                                this.availableHostList.removeIf(uploadConnection -> host.equals(uploadConnection.getRpiHostName()));
                            }
                        }))
                        .join();
            }
        }
    }
}

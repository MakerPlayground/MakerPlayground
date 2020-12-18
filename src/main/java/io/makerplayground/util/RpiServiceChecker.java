package io.makerplayground.util;

import io.makerplayground.upload.UploadTarget;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RpiServiceChecker implements Runnable {

    public static final short PORT = 6212;

    private final ObservableList<UploadTarget> availableHostList;
    private final String host;
    private static final String EXPECTED_RESPONSE = "makerplayground";

    public RpiServiceChecker(String host, ObservableList<UploadTarget> availableHostList) {
        this.host = host;
        this.availableHostList = availableHostList;
    }

    @Override
    public void run() {
//            System.out.println("Check : " + this.host);
        boolean isConnected = false;
        try (Socket socket = new Socket()) {
            socket.setReuseAddress(true);
            SocketAddress sa = new InetSocketAddress(this.host, PORT);
            socket.connect(sa, 2500);
            if (socket.isConnected()) {
                isConnected = true;
            }
        } catch (IOException e) {
            isConnected = false;
            Platform.runLater(() -> this.availableHostList.removeIf(uploadConnection -> host.equals(uploadConnection.getRpiHostName())));
        }

        if (isConnected) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d", this.host, PORT))) // example: "http://192.168.1.43:6212/"
                    .build();
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(s -> Platform.runLater(() -> {
                        if (EXPECTED_RESPONSE.equals(s)) {
                            if (this.availableHostList.stream().noneMatch(uploadConnection -> host.equals(uploadConnection.getRpiHostName()))) {
                                this.availableHostList.add(new UploadTarget(this.host));
                            }
                        } else {
                            this.availableHostList.removeIf(uploadConnection -> host.equals(uploadConnection.getRpiHostName()));
                        }
                    }))
                    .join();
        }
    }
}

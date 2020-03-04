package io.makerplayground.util;

import com.fazecast.jSerialComm.SerialPort;
import io.makerplayground.generator.upload.UploadTarget;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SerialPortDiscoverer {
    final ObservableList<UploadTarget> resultSerialList;
    private ScheduledThreadPoolExecutor executor;

    public SerialPortDiscoverer(ObservableList<UploadTarget> resultSerialList) {
        this.resultSerialList = resultSerialList;
    }

    public void startScan() {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            List<UploadTarget> newList = Arrays.stream(SerialPort.getCommPorts()).map(UploadTarget::new).collect(Collectors.toList());
            resultSerialList.removeIf(uploadConnection -> newList.stream().noneMatch(uploadConnection1 -> uploadConnection1.equals(uploadConnection)));
            resultSerialList.addAll(newList.stream().filter(uploadConnection -> !resultSerialList.contains(uploadConnection)).collect(Collectors.toList()));
        }), 0, 2, TimeUnit.SECONDS);
    }

    public boolean isRunning() {
        return executor != null && !executor.isTerminated();
    }

    public void stopScan() {
        executor.shutdownNow();
    }
}

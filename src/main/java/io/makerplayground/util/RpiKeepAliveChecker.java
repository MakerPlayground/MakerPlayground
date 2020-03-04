package io.makerplayground.util;

import io.makerplayground.generator.upload.UploadTarget;
import javafx.collections.ObservableList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpiKeepAliveChecker {
    final ObservableList<UploadTarget> hostList;
    private ScheduledThreadPoolExecutor executor;

    public RpiKeepAliveChecker(ObservableList<UploadTarget> hostList) {
        this.hostList = hostList;
    }

    public void startScan() {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(3);
        for (UploadTarget connection: hostList) {
            executor.scheduleAtFixedRate(new RpiServiceChecker(connection.getRpiHostName(), hostList), 0, 5, TimeUnit.SECONDS);
        }
    }

    public boolean isRunning() {
        return executor != null && !executor.isTerminated();
    }

    public void stopScan() {
        executor.shutdownNow();
    }
}

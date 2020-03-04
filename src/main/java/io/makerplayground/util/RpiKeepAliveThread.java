package io.makerplayground.util;

import io.makerplayground.generator.upload.UploadTarget;
import javafx.collections.ObservableList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpiKeepAliveThread extends Thread {

    final ObservableList<UploadTarget> hostList;
    private ScheduledThreadPoolExecutor executor;

    public RpiKeepAliveThread(ObservableList<UploadTarget> hostList) {
        this.hostList = hostList;
    }

    @Override
    public void run() {
        executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);
        for (UploadTarget connection: hostList) {
            executor.scheduleAtFixedRate(new RpiServiceChecker(connection.getRpiHostName(), hostList), 0, 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        executor.shutdownNow();
    }
}

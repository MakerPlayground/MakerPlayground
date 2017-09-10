package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.DevicePort;
import io.makerplayground.helper.SingletonDeviceURL;
import io.makerplayground.helper.SingletonUtilTools;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by USER on 20-Jul-17.
 */
public class TableDataList {
    private final String name;
    private final String brand;
    private final String model;
    private final String id;
    private final String pin;
    private final String url;

    public TableDataList(ProjectDevice projectDevice) {
        this.name = projectDevice.getName();
        this.brand = projectDevice.getActualDevice().getBrand();
        this.model = projectDevice.getActualDevice().getModel();
        this.id = projectDevice.getActualDevice().getId();
        this.pin = String.join(",", projectDevice.getDeviceConnection().values().stream().flatMap(Collection::stream)
                .map(DevicePort::getName).collect(Collectors.toList()));
        this.url = projectDevice.getActualDevice().getUrl();
    }

    public String getName() { return name; }

    public String getBrand() { return brand; }

    public String getId() { return id; }

    public String getModel() { return model; }

    public String getPin() { return pin;}

    public Hyperlink getUrl() {
        Hyperlink link = new Hyperlink();
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SingletonDeviceURL.getInstance().setAll(url);
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(URI.create(url));
                } catch (IOException ev) {
                    ev.printStackTrace();
                }
            }
        });
        link.setText(url);
        return link;
    }

}

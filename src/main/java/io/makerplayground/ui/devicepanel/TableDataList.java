package io.makerplayground.ui.devicepanel;

import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

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
        this.pin = projectDevice.getDeviceConnection().values().iterator().next().toString();
        this.url = projectDevice.getActualDevice().getUrl();
    }

    public String getName() { return name; }

    public String getBrand() { return brand; }

    public String getId() { return id; }

    public String getModel() { return model; }

    public String getPin() { return pin;}

    public Hyperlink getUrl() {
        Hyperlink link = new Hyperlink();
        link.setText(url);
        return link;
    }

}

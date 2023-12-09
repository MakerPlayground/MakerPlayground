open module io.makerplayground.frontend {
    requires java.sql;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.apache.commons.io;
    requires org.apache.commons.codec;
    requires org.java_websocket;
    requires org.apache.commons.net;
    requires java.net.http;
    requires org.semver4j;
    requires static lombok;
    requires java.desktop;
    requires jdk.crypto.ec; // need for supporting SSL connection (See: https://stackoverflow.com/q/62238883)
}
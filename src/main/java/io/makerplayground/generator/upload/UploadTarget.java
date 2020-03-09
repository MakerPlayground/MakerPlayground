package io.makerplayground.generator.upload;

import com.fazecast.jSerialComm.SerialPort;

public class UploadTarget {

    private final UploadMode method;
    private final SerialPort serialPort;
    private final String rpiHostName;

    public UploadTarget(SerialPort serialPort) {
        this.method = UploadMode.SERIAL_PORT;
        this.serialPort = serialPort;
        this.rpiHostName = "";
    }

    public UploadTarget(String rpiHostName) {
        this.method = UploadMode.RPI_ON_NETWORK;
        this.rpiHostName = rpiHostName;
        this.serialPort = null;
    }

    public UploadMode getUploadMode() {
        return method;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public String getRpiHostName() {
        return rpiHostName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UploadTarget)) {
            return false;
        }
        UploadTarget obj = (UploadTarget) o;
        if (this.getUploadMode().equals(obj.getUploadMode())) {
            if (this.getUploadMode().equals(UploadMode.SERIAL_PORT)) {
                assert this.serialPort != null;
                return this.serialPort.getDescriptivePortName().equals(((UploadTarget) o).serialPort.getDescriptivePortName());
            }
            else if (this.getUploadMode().equals(UploadMode.RPI_ON_NETWORK)) {
                return this.rpiHostName.equals(((UploadTarget) o).rpiHostName);
            }
            else {
                throw new IllegalStateException("Unsupported Upload Mode");
            }
        }
        return false;
    }
}

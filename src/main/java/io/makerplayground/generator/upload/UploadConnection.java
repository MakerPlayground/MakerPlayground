package io.makerplayground.generator.upload;

import com.fazecast.jSerialComm.SerialPort;

public class UploadConnection {
    public enum Type {
        SERIALPORT, RPI
    }

    private final Type type;
    private final SerialPort serialPort;
    private final String rpiHostName;

    public UploadConnection(SerialPort serialPort) {
        this.type = Type.SERIALPORT;
        this.serialPort = serialPort;
        this.rpiHostName = "";
    }

    public UploadConnection(String rpiHostName) {
        this.type = Type.RPI;
        this.rpiHostName = rpiHostName;
        this.serialPort = null;
    }

    public Type getType() {
        return type;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public String getRpiHostName() {
        return rpiHostName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UploadConnection)) {
            return false;
        }
        UploadConnection obj = (UploadConnection) o;
        if (this.getType().equals(obj.getType())) {
            if (this.getType().equals(Type.SERIALPORT)) {
                assert this.serialPort != null;
                return this.serialPort.getDescriptivePortName().equals(((UploadConnection) o).serialPort.getDescriptivePortName());
            }
            else if (this.getType().equals(Type.RPI)) {
                return this.rpiHostName.equals(((UploadConnection) o).rpiHostName);
            }
            else {
                throw new IllegalStateException("Unsupported UploadConnectionType");
            }
        }
        return false;
    }
}

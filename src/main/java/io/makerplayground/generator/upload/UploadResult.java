package io.makerplayground.generator.upload;

public enum UploadResult {
    OK,
    CANT_FIND_PIO,
    DEVICE_OR_PORT_MISSING,
    CANT_CREATE_PROJECT,
    CANT_GENERATE_CODE,
    CODE_ERROR,
    UNKNOWN_ERROR,
    CANT_FIND_BOARD,
    CANT_WRITE_CODE,
    MISSING_LIBRARY_DIR,
    CANT_FIND_LIBRARY,
    USER_CANCEL
}

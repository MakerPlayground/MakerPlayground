package io.makerplayground.generator.upload;

public enum UploadResult {
    OK,
    CANT_FIND_PIO,
    NOT_ENOUGH_PORT,
    NO_SUPPORT_DEVICE,
    NO_MCU_SELECTED,
    CANT_GENERATE_CODE,
    UNKNOWN_ERROR,
    CANT_FIND_BOARD,
    CANT_WRITE_CODE,
    MISSING_LIBRARY_DIR,
    CANT_FIND_LIBRARY,
    USER_CANCEL,
    NO_PERMISSION;
}

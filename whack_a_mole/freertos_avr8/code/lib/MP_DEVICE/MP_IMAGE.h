#ifndef MP_IMAGE_H
#define MP_IMAGE_H

#include "Arduino.h"

#define MP_IMAGE_FORMAT_UNSPECIFIED 0
#define MP_IMAGE_FORMAT_RGB565 1
#define MP_IMAGE_FORMAT_RGB888 2
#define MP_IMAGE_FORMAT_JPEG 3

struct MP_IMAGE {
    uint16_t id = 0;       // auto increment id used to cache result of image processing operation
    uint8_t* data = NULL;  // pointer to the image data (CAUTION: the memory location may be resused)
    uint16_t width = 0;    // image width
    uint16_t height = 0;   // image height
    uint32_t size = 0;     // size of the image data in byte (depends on pixel format and compression)
    uint8_t format = MP_IMAGE_FORMAT_UNSPECIFIED;   // image format e.g. MP_IMAGE_FORMAT_RGB565
};

#endif
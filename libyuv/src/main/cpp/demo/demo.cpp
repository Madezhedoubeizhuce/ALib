#include <stdio.h>
#include <stdlib.h>
#include "libyuv/convert.h"

int readImage(const char *image_path, uint8_t *image, int size)
{
    FILE* image_file = fopen(image_path, "rb");

    if (image_file == NULL)
    {
        printf("open file failed\n");
        return 1;
    }

    size_t bytes_src = fread(image, sizeof(uint8_t), static_cast<size_t>(size), image_file);
    if (bytes_src < static_cast<size_t>(size))
    {
        printf("read file failed\n");
        return 1;
    }
    fclose(image_file);

    return 0;
}

int writeImage(const char *image_path, uint8_t *image, int size)
{
    printf("write image %s\n", image_path);
    FILE* image_file = fopen(image_path, "wb");

    if (image_file == NULL)
    {
        printf("open file failed\n");
        return 1;
    }

    size_t write_bytes = fwrite(image, sizeof(int8_t), size, image_file);

    if (write_bytes < static_cast<size_t>(size))
    {
        printf("write file failed\n");
        return 1;
    }
    fclose(image_file);

    return 0;
}

int ConvertI444ToNV21(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        return -1;
    }

    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    int ret = libyuv::I444ToNV21(src_img,
        width,
        src_img + width * height,
        width,
        src_img + width * height * 2,
        width,
        *dst_img,
        width,
        *dst_img + width * height,
        width, width, height);

    if (ret != 0)
    {
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }

    return 0;
}

int ConvertI444ToNV12(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        return -1;
    }

    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    int ret = libyuv::I444ToNV12(src_img,
        width,
        src_img + width * height,
        width,
        src_img + width * height * 2,
        width,
        *dst_img,
        width,
        *dst_img + width * height,
        width, width, height);

    if (ret != 0)
    {
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }

    return 0;
}

int ConvertI444ToI420(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        return -1;
    }

    int y_size = width * height;
    int u_size = ((width + 1) / 2) * ((height + 1) / 2);
    int v_size = ((width + 1) / 2) * ((height + 1) / 2);
    *dst_size = y_size + u_size + v_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    // todo: u分量转换有问题，参数传错了吗？
    int ret = libyuv::I444ToI420(src_img,
        width,
        src_img + width * height,
        width,
        src_img + width * height * 2,
        width,
        *dst_img,
        width,
        *dst_img + y_size,
        (width + 1) /2,
        *dst_img + y_size + u_size,
        (width + 1) / 2,
        width, height);

    if (ret != 0)
    {
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }

    return 0;
}

int ConvertI422oNV21(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        printf("src_img is null\n");
        return -1;
    }

    int src_uv_size = ((width + 1) / 2) * height * 2;
    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    printf("call I422ToNV21\n");
    int ret = libyuv::I422ToNV21(src_img,
        width,
        src_img + width * height,
        (width + 1) / 2,
        src_img + width * height + src_uv_size,
        (width + 1) / 2,
        *dst_img,
        width,
        *dst_img + width * height,
        width, width, height);

    if (ret != 0)
    {
        printf("Failed\n");
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }
    printf("Complete\n");

    return 0;
}

void testI444ToI420() {
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 3;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test.yuv", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI444ToI420(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I444ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result.i420", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI444ToNV12()
{
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 3;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test.yuv", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI444ToNV12(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I444ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result.nv12", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI444ToNV21()
{
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 3;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test.yuv", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI444ToNV21(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I444ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result.nv21", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI422ToNV21()
{
    printf("testI422ToNV21\n");
    int img_width = 640, img_height = 480;
    int src_size = img_width * img_height * 2;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/img_640_480.i422", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;
    printf("ConvertI422oNV21 \n");

    int ret = ConvertI422oNV21(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I422ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result_422.nv21", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

int main(int argc, char* argv[])
{
    printf("test libyuv\n");

    // testI444ToNV21();
    // testI444ToI420();
    // testI444ToNV12();
    testI422ToNV21();

    return 0;
}
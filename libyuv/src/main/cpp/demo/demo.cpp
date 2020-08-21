#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include "libyuv/convert.h"
#include "libyuv/scale.h"
#include "libyuv.h"

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
        (width + 1) / 2,
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

int ConvertI444ToI420_1(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        printf("src_img is null\n");
        return -1;
    }

    int src_uv_size = width * height * 2;
    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    const uint8_t* src_y = src_img;
    int src_stride_y = width;
    const uint8_t* src_u = src_img + width * height;
    int src_stride_u = width;
    const uint8_t* src_v =  src_img + width * height * 2;
    int src_stride_v = width;
    uint8_t* dst_y = *dst_img;
    int dst_stride_y = width;
    uint8_t* dst_u = *dst_img + y_size;
    int dst_stride_u = (width + 1) / 2;
    uint8_t* dst_v = *dst_img + y_size + uv_size / 2;
    int dst_stride_v = (width + 1) / 2;
    int src_y_width = width;
    int src_y_height = height;
    int src_uv_width = width;
    int src_uv_height = height;

    const int dst_y_width = width;
    const int dst_y_height = height;
    const int dst_uv_width = (dst_y_width + 1) >> 1;
    const int dst_uv_height = (dst_y_height + 1) >> 1;

    if (dst_y) {
        libyuv::ScalePlane(src_y, src_stride_y, src_y_width, src_y_height, dst_y,
                dst_stride_y, dst_y_width, dst_y_height,  libyuv::kFilterBilinear);
    }
    libyuv::ScalePlane(src_u, src_stride_u, src_uv_width, src_uv_height, dst_u,
                dst_stride_u, dst_uv_width, dst_uv_height,  libyuv::kFilterBilinear);
    libyuv::ScalePlane(src_v, src_stride_v, src_uv_width, src_uv_height, dst_v,
                dst_stride_v, dst_uv_width, dst_uv_height,  libyuv::kFilterBilinear);

    return 0;
}

int ConvertI422ToNV21(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
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

    int ret = libyuv::I422ToNV21(src_img,
        width,
        src_img + width * height,
        (width + 1) / 2,
        src_img + width * height + src_uv_size / 2,
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

    return 0;
}

int ConvertI422ToI420(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
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

    int ret = libyuv::I422ToI420(src_img,
        width,
        src_img + width * height,
        (width + 1) / 2,
        src_img + width * height + src_uv_size / 2,
        (width + 1) / 2,
        *dst_img,
        width,
        *dst_img + width * height,
        (width + 1) / 2,
        *dst_img + width * height  + uv_size / 2,
        (width + 1) / 2, width, height);

    if (ret != 0)
    {
        printf("Failed\n");
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }

    return 0;
}

int ConvertNV21ToI420(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        printf("src_img is null\n");
        return -1;
    }

    int src_uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * ((height + 1) / 2) * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    int half_width = (width + 1) / 2;
    int ret = libyuv::NV21ToI420(src_img,
        width,
        src_img + y_size,
        width,
        *dst_img,
        width,
        *dst_img + y_size,
        (width + 1) / 2,
        *dst_img + y_size  + uv_size / 2,
        (width + 1) / 2, width, height);
    // int ret = libyuv::ConvertToI420(src_img,
    //     y_size + src_uv_size,
    //     *dst_img,
    //     width, 
    //     *dst_img,
    //     half_width,
    //     *dst_img + y_size  + uv_size / 2,
    //     half_width, 
    //     0, 0, width, height, width, height, 
    //     libyuv::kRotate0, libyuv::FOURCC_NV21);

    if (ret != 0)
    {
        printf("Failed\n");
        delete[] *dst_img;
        *dst_img = NULL;
        return -1;
    }

    return 0;
}

int ConvertI444ToI422(uint8_t *src_img, int width, int height, uint8_t **dst_img, int *dst_size)
{
    if (src_img == NULL)
    {
        printf("src_img is null\n");
        return -1;
    }

    int src_uv_size = width * height * 2;
    int y_size = width * height;
    int uv_size = ((width + 1) / 2) * height * 2;
    *dst_size = y_size + uv_size;

    if (*dst_img == NULL)
    {
        *dst_img = new uint8_t[*dst_size];
    }

    const uint8_t* src_y = src_img;
    int src_stride_y = width;
    const uint8_t* src_u = src_img + width * height;
    int src_stride_u = width;
    const uint8_t* src_v =  src_img + width * height * 2;
    int src_stride_v = width;
    uint8_t* dst_y = *dst_img;
    int dst_stride_y = width;
    uint8_t* dst_u = *dst_img + y_size;
    int dst_stride_u = (width + 1) / 2;
    uint8_t* dst_v = *dst_img + y_size + uv_size / 2;
    int dst_stride_v = (width + 1) / 2;
    int src_y_width = width;
    int src_y_height = height;
    int src_uv_width = width;
    int src_uv_height = height;

    const int dst_y_width = width;
    const int dst_y_height = height;
    const int dst_uv_width = (dst_y_width + 1) >> 1;
    const int dst_uv_height = dst_y_height;

    if (dst_y) {
        libyuv::ScalePlane(src_y, src_stride_y, src_y_width, src_y_height, dst_y,
                dst_stride_y, dst_y_width, dst_y_height,  libyuv::kFilterBilinear);
    }
     libyuv::ScalePlane(src_u, src_stride_u, src_uv_width, src_uv_height, dst_u,
                dst_stride_u, dst_uv_width, dst_uv_height,  libyuv::kFilterBilinear);
     libyuv::ScalePlane(src_v, src_stride_v, src_uv_width, src_uv_height, dst_v,
                dst_stride_v, dst_uv_width, dst_uv_height,  libyuv::kFilterBilinear);

    return 0;
}

void testI444ToI422() {
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 3;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test_500_333.i444", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI444ToI422(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I444ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result_500_333_i444.i422", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI444ToI420() {
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 3;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test_500_333.i444", src_img, src_size) != 0)
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

    if (writeImage("./img/result_500_333_i444.i420", dst_img, dst_size) != 0)
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

    if (readImage("./img/test_500_333.i444", src_img, src_size) != 0)
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

    if (writeImage("./img/result_500_333_i444.nv12", dst_img, dst_size) != 0)
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

    if (readImage("./img/test_500_333.i444", src_img, src_size) != 0)
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

    if (writeImage("./img/result_500_333_i444.nv21", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI422ToNV21()
{
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 2;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test_500_333.i422", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI422ToNV21(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("I422ToNV21 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result_500_333_i422.nv21", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testI422ToI420()
{
    int img_width = 500, img_height = 333;
    int src_size = img_width * img_height * 2;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test_500_333.i422", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertI422ToI420(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("testI422ToI420 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result_500_333_i422.i420", dst_img, dst_size) != 0)
    {
        printf("write image failed\n");
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
    }
}

void testNV21ToI420()
{
    int img_width = 500, img_height = 333;
    int y_size = img_width * img_height;
    int src_size = y_size + (y_size + 1) >> 2;

    uint8_t *src_img = new uint8_t[src_size];

    if (readImage("./img/test_500_333.nv21", src_img, src_size) != 0)
    {
        printf("read image failed\n");
        delete[] src_img;
        return;
    }

    uint8_t *dst_img = NULL;
    int dst_size = 0;

    int ret = ConvertNV21ToI420(src_img, img_width, img_height, &dst_img, &dst_size);

    if (ret != 0)
    {
        printf("testNV21ToI420 failed, ret %d\n", ret);
        delete[] src_img;
        if (dst_img != NULL)
            delete[] dst_img;
        return;
    }

    if (writeImage("./img/result_500_333_nv21.i420", dst_img, dst_size) != 0)
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

    try
    {
        // testI444ToI420();
        // testI444ToI422();
        // testI444ToNV21();
        // testI444ToNV12();
        // testI422ToNV21();
        // testI422ToI420();
        testNV21ToI420();
    }
    catch(const std::exception& e)
    {
        std::cerr << e.what() << '\n';
    }

    return 0;
}
#include <stdio.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include "turbojpeg.h"

#define THROW(action, message)                                                 \
    {                                                                          \
        printf("ERROR in line %d while %s:\n%s\n", __LINE__, action, message); \
        retval = -1;                                                           \
        goto bailout;                                                          \
    }

#define THROW_TJ(action) THROW(action, tjGetErrorStr2(tjInstance))

#define THROW_UNIX(action) THROW(action, strerror(errno))

int readImgFile(const char *imgPath, unsigned char **imgBuf, unsigned long *imgSize)
{
    FILE *imgFile = NULL;
    long size;
    int retval = 0;

    /* Read the JPEG file into memory. */
    if ((imgFile = fopen(imgPath, "rb")) == NULL)
        THROW_UNIX("opening input file");
    if (fseek(imgFile, 0, SEEK_END) < 0 || ((size = ftell(imgFile)) < 0) ||
        fseek(imgFile, 0, SEEK_SET) < 0)
        THROW_UNIX("determining input file size");

    if (size == 0)
        THROW("determining input file size", "Input file contains no data");
    *imgSize = (unsigned long)size;

    if ((*imgBuf = (unsigned char *)tjAlloc(*imgSize)) == NULL)
        THROW_UNIX("allocating JPEG buffer");
    if (fread(*imgBuf, *imgSize, 1, imgFile) < 1)
        THROW_UNIX("reading input file");
    fclose(imgFile);
    imgFile = NULL;

bailout:
    if (imgFile)
        fclose(imgFile);
    imgFile = NULL;

    return retval;
}

int writeImgFile(const char *filePath, unsigned char *buf, unsigned long size)
{
    int retval = 0;
    FILE *file = NULL;
    /* Write the JPEG image to disk. */
    if ((file = fopen(filePath, "wb")) == NULL)
        THROW_UNIX("opening output file");
    if (fwrite(buf, size, 1, file) < 1)
        THROW_UNIX("writing output file");

bailout:
    if (file)
        fclose(file);
    file = NULL;

    return retval;
}

void testTransform(const char *img, const char *outImg)
{
    printf("-------------testTransform-------------\n");
    int retval = 0;
    unsigned long jpegSize = 0;
    unsigned char *jpegBuf = NULL;
    tjhandle tjInstance = NULL;

    if (readImgFile(img, &jpegBuf, &jpegSize))
    {
        THROW_UNIX("read jpeg failed");
    }

    printf("jpeg size:%lu\n", jpegSize);

    tjInstance = tjInitTransform();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing transformer");
    }

    unsigned char *dstBuf = NULL; /* Dynamically allocate the JPEG buffer */
    unsigned long dstSize = 0;
    tjtransform xform;
    memset(&xform, 0, sizeof(tjtransform));
    xform.op = TJXOP_HFLIP;
    xform.options |= TJXOPT_TRIM;

    if (tjTransform(tjInstance, jpegBuf, jpegSize, 1, &dstBuf, &dstSize, &xform, 0))
    {
        THROW_TJ("transfrom failed");
    }

    if (writeImgFile(outImg, dstBuf, dstSize))
    {
        THROW_UNIX("save out image failed");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    tjInstance = NULL;
    if (jpegBuf)
    {
        tjFree(jpegBuf);
    }
}

int testDecompress(const char *img, const char *outImg, int *imgWidth, int *imgHeight, int *imgSamp)
{
    printf("-------------testDecompress-------------\n");
    int retval = 0;
    unsigned long jpegSize = 0;
    unsigned char *jpegBuf = NULL;
    tjhandle tjInstance = NULL;

    if (readImgFile(img, &jpegBuf, &jpegSize))
    {
        THROW_UNIX("read jpeg failed");
    }

    tjInstance = tjInitDecompress();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing decompress");
    }

    int width = 0, height = 0;
    int jpegSubsamp = -1, colorSpace = -1;
    if (tjDecompressHeader3(tjInstance, jpegBuf, jpegSize, &width, &height, &jpegSubsamp, &colorSpace))
    {
        THROW_TJ("decompress header");
    }

    *imgWidth = width;
    *imgHeight = height;
    *imgSamp = jpegSubsamp;

    printf("colorSpace %d, jpegSubsamp %d\n", colorSpace, jpegSubsamp);

    int pixelFormat = TJPF_RGB;
    unsigned char *dstBuf = NULL;
    dstBuf = tjAlloc(width * height * tjPixelSize[pixelFormat]);
    if (dstBuf == NULL)
    {
        THROW_TJ("alloc dst buf");
    }
    if (tjDecompress2(tjInstance, jpegBuf, jpegSize, dstBuf,
                      width, 0, height, pixelFormat, 0))
    {
        THROW_TJ("decompress jpeg");
    }

    if (writeImgFile(outImg, dstBuf, width * height * tjPixelSize[pixelFormat]))
    {
        THROW_UNIX("save result img");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    if (jpegBuf)
    {
        tjFree(jpegBuf);
    }
    return retval;
}

void testDecompressToYUV(const char *img, const char *outImg)
{
    printf("-------------testDecompressToYUV-------------\n");
    int retval = 0;
    unsigned long jpegSize = 0;
    unsigned char *jpegBuf = NULL;
    tjhandle tjInstance = NULL;

    if (readImgFile(img, &jpegBuf, &jpegSize))
    {
        THROW_UNIX("read jpeg failed");
    }

    tjInstance = tjInitDecompress();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing decompress");
    }

    int width = 0, height = 0;
    int jpegSubsamp = -1, colorSpace = -1;
    if (tjDecompressHeader3(tjInstance, jpegBuf, jpegSize, &width, &height, &jpegSubsamp, &colorSpace))
    {
        THROW_TJ("decompress header");
    }

    printf("colorSpace %d, jpegSubsamp %d\n", colorSpace, jpegSubsamp);

    int pixelFormat = TJPF_RGB;
    unsigned char *dstBuf = NULL;
    int pad = 1;
    int subsamp = jpegSubsamp;
    int bufSize = tjBufSizeYUV2(width, pad, height, subsamp);
    dstBuf = tjAlloc(bufSize);
    if (dstBuf == NULL)
    {
        THROW_TJ("alloc dst buf");
    }
    if (tjDecompressToYUV2(tjInstance, jpegBuf, jpegSize, dstBuf,
                           width, pad, height, 0))
    {
        THROW_TJ("decompress to yuv");
    }

    if (writeImgFile(outImg, dstBuf, bufSize))
    {
        THROW_UNIX("save result img");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    if (jpegBuf)
    {
        tjFree(jpegBuf);
    }
}

void testCompress(char *img, char *outImg, int width, int height)
{
    printf("-------------testCompress-------------\n");
    int retval = 0;
    tjhandle tjInstance = NULL;
    int pixelFormat = TJPF_RGB;
    unsigned char *jpegBuf = NULL;
    unsigned long jpegSize = 0;
    unsigned char *imgBuf = NULL;
    unsigned long imgSize = 0;

    if (readImgFile(img, &imgBuf, &imgSize))
    {
        THROW_UNIX("read image failed");
    }

    tjInstance = tjInitCompress();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing compress");
    }

    if (tjCompress2(tjInstance, imgBuf, width, 0, height, pixelFormat,
                    &jpegBuf, &jpegSize, TJSAMP_420, 95, 0))
    {
        THROW_TJ("compress");
    }

    if (writeImgFile(outImg, jpegBuf, jpegSize))
    {
        THROW_UNIX("save result img");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    if (jpegBuf)
    {
        tjFree(jpegBuf);
    }
    if (imgBuf)
    {
        tjFree(imgBuf);
    }
}

void testCompressFromYUV(char *img, char *outImg, int width, int height, int samp)
{
    printf("-------------testCompressFromYUV-------------\n");
    int retval = 0;
    tjhandle tjInstance = NULL;
    unsigned char *jpegBuf = NULL;
    unsigned long jpegSize = 0;
    unsigned char *imgBuf = NULL;
    unsigned long imgSize = 0;

    if (readImgFile(img, &imgBuf, &imgSize))
    {
        THROW_UNIX("read image failed");
    }

    tjInstance = tjInitCompress();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing compress");
    }
    int pad = 4;

    if (tjCompressFromYUV(tjInstance, imgBuf, width, pad, height, samp,
                          &jpegBuf, &jpegSize, 95, 0))
    {
        THROW_TJ("compress");
    }

    if (writeImgFile(outImg, jpegBuf, jpegSize))
    {
        THROW_UNIX("save result img");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    if (jpegBuf)
    {
        tjFree(jpegBuf);
    }
    if (imgBuf)
    {
        tjFree(imgBuf);
    }
}

void testDecoedYUV(char *img, char *outImg, int width, int height, int samp)
{
    printf("-------------testDecoedYUV-------------\n");
    int retval = 0;
    tjhandle tjInstance = NULL;
    unsigned char *imgBuf = NULL;
    unsigned long imgSize = 0;
    unsigned char *dstBuf = NULL;

    if (readImgFile(img, &imgBuf, &imgSize))
    {
        THROW_UNIX("read image failed");
    }

    tjInstance = tjInitDecompress();
    if (tjInstance == NULL)
    {
        THROW_TJ("initializing compress");
    }
    int pad = 4;
    int pixelFormat = TJPF_RGB;
    unsigned long dstSize = width * height * tjPixelSize[pixelFormat];
    dstBuf = tjAlloc(dstSize);

    if (tjDecodeYUV(tjInstance, imgBuf, pad, samp, dstBuf, width,
                    0, height, pixelFormat, 0))
    {
        THROW_TJ("decode yuv");
    }

    if (writeImgFile(outImg, dstBuf, dstSize))
    {
        THROW_UNIX("save result img");
    }

bailout:
    if (tjInstance)
    {
        tjDestroy(tjInstance);
    }
    if (dstBuf)
    {
        tjFree(dstBuf);
    }
    if (imgBuf)
    {
        tjFree(imgBuf);
    }
}

int main(int argc, char **argv)
{
    printf("turbo jpeg demo\n");
    int width = 0, height = 0, samp = -1;

    if (testDecompress("./img/test.jpg", "./img/decompress.rgb", &width, &height, &samp) == 0)
    {
        printf("test.jpg width %d, height %d, samp %d\n", width, height, samp);

        testTransform("./img/test.jpg", "./img/out_transform.jpg");
        testDecompressToYUV("./img/test.jpg", "./img/decompress.yuv");
        testCompress("./img/decompress.rgb", "./img/out_compress.jpg", width, height);
        testCompressFromYUV("./img/decompress.yuv", "./img/out_compress_yuv.jpg", width, height, samp);
        testDecoedYUV("img/decompress.yuv", "./img/out_decode.rgb", width, height, samp);
    }

    return 0;
}
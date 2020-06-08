#include <stdio.h>
#include <android/log.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include "turbojpeg.h"
#include "com_alpha_turbojpeg_TurboJpegJni.h"

#ifdef __cplusplus
extern "C" {
#endif
#define LOG_TAG "turbo_jpeg_jni"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

const char *JPEGHEADER_CLASS = "com/alpha/turbojpeg/bean/JpegHeader";
const char *IMAGEBUF_CLASS = "com/alpha/turbojpeg/bean/ImageBuf";

char *ConvertJByteArrayToChars(JNIEnv *env, jbyteArray byte_array);

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitCompress(JNIEnv *env, jobject thiz) {
    LOGD("tjInitCompress");
    tjhandle tjInstance = NULL;
    tjInstance = tjInitCompress();
    if (tjInstance != NULL) {
        return (long) tjInstance;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjCompress2(JNIEnv *env, jobject thiz,
                                                                         jlong handle,
                                                                         jbyteArray srcBuf,
                                                                         jint width, jint pitch,
                                                                         jint height,
                                                                         jint pixelFormat,
                                                                         jobject jpegImage,
                                                                         jint jpegQual,
                                                                         jint flags) {
    char *srcImg = ConvertJByteArrayToChars(env, srcBuf);

    jclass bufClass = env->GetObjectClass(jpegImage);
    jfieldID id_buf = env->GetFieldID(bufClass, "buf", "[B");
    jfieldID id_size = env->GetFieldID(bufClass, "size", "J");

    // jbyteArray jepgBytes=  

    // int ret = tjCompress2((tjhandle) handle, srcBuf, width, pitch, height, pixelFormat,
    //                 &jpegBuf, &jpegSize, TJSAMP_420, jpegQual, flags);
    return 0;
}

char *ConvertJByteArrayToChars(JNIEnv *env, jbyteArray byte_array) {
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(byte_array, 0);
    int chars_len = env->GetArrayLength(byte_array);
    chars = new char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(byte_array, bytes, 0);
    return chars;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjCompressFromYUV(JNIEnv *env, jobject thiz,
                                                        jlong handle, jbyteArray srcBuf, jint width,
                                                        jint pitch,
                                                        jint height, jint pixelFormat,
                                                        jobject jpegImage, jint jpegQual,
                                                        jint flags) {
    return 0;
}

JNIEXPORT jlong JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjBufSize(JNIEnv *env, jobject thiz,
                                                                        jint width, jint height,
                                                                        jint jpegSubsamp) {
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjBufSizeYUV2(JNIEnv *env, jobject thiz,
                                                    jint width, jint height, jint jpegSubsamp) {
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjEncodeYUV3(JNIEnv *env, jobject thiz,
                                                                          jlong handle,
                                                                          jbyteArray srcBuf,
                                                                          jint width, jint pitch,
                                                                          jint height,
                                                                          jint pixelFormat,
                                                                          jobject dstBuf, jint pad,
                                                                          jint subsamp,
                                                                          jint flags) {
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitDecompress(JNIEnv *env, jobject thiz) {
    tjhandle tjInstance = NULL;
    tjInstance = tjInitDecompress();
    if (tjInstance != NULL) {
        return (long) tjInstance;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompressHeader3(JNIEnv *env, jobject thiz, jlong handle,
                                                          jbyteArray jpegBuf, jlong jpegSize,
                                                          jobject jpegHeader) {
    if (jpegHeader == nullptr) {
        return -1;
    }
    tjhandle tjInstance = (tjhandle) handle;

    jbyte *bytes = env->GetByteArrayElements(jpegBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    int width = 0, height = 0;
    int jpegSubsamp = -1, colorSpace = -1;

    if (tjDecompressHeader3(tjInstance, buf, jpegSize, &width, &height, &jpegSubsamp,
                            &colorSpace) != 0) {
        return -1;
    }

    jclass headerClass = env->FindClass(JPEGHEADER_CLASS);
    jfieldID width_field = (env)->GetFieldID(headerClass, "width", "I");
    jfieldID height_field = (env)->GetFieldID(headerClass, "height", "I");
    jfieldID jepg_subsamp_field = (env)->GetFieldID(headerClass, "jepgSubsamp", "I");
    jfieldID jpeg_colorspace_field = (env)->GetFieldID(headerClass, "jpegColorspace", "I");

    (env)->SetIntField(jpegHeader, width_field, width);
    (env)->SetIntField(jpegHeader, height_field, height);
    (env)->SetIntField(jpegHeader, jepg_subsamp_field, jpegSubsamp);
    (env)->SetIntField(jpegHeader, jpeg_colorspace_field, colorSpace);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompress2(JNIEnv *env, jobject thiz,
                                                    jlong handle, jbyteArray jpegBuf,
                                                    jlong jpegSize, jobject dstBuf,
                                                    jint width, jint pitch, jint height,
                                                    jint pixelFormat, jint flag) {
    if (dstBuf == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    unsigned char *dst_buf_native = nullptr;

    jbyte *bytes = env->GetByteArrayElements(jpegBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    int size = width * height * tjPixelSize[pixelFormat];
    dst_buf_native = tjAlloc(size);
    if (dst_buf_native == nullptr) {
        return -1;
    }

    if (tjDecompress2(tjInstance, buf, jpegSize, dst_buf_native,
                      width, pitch, height, pixelFormat, flag) != 0) {
        return -1;
    }
    jbyte *dst_buf_bytes = (jbyte *) dst_buf_native;

    jclass bufClass = env->FindClass(IMAGEBUF_CLASS);
    jfieldID buf_field = (env)->GetFieldID(bufClass, "buf", "[B");

    jbyteArray dstBufArray = env->NewByteArray(size);

    env->SetByteArrayRegion(dstBufArray, 0, size, dst_buf_bytes);
    env->SetObjectField(dstBuf, buf_field, dstBufArray);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompressToYUV2(JNIEnv *env, jobject thiz,
                                                         jlong handle, jbyteArray jpegBuf,
                                                         jlong jpegSize,
                                                         jobject dstBuf, jint width, jint pad,
                                                         jint height, jint flags) {
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjDecodeYUV(JNIEnv *env, jobject thiz,
                                                                         jlong handle,
                                                                         jbyteArray srcBuf,
                                                                         jint pad, jint subsamp,
                                                                         jobject dstBuf,
                                                                         jint width, jint pitch,
                                                                         jint height,
                                                                         jint pixelFormat,
                                                                         jint flags) {
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitTransform(JNIEnv *env, jobject thiz) {
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjTransform(JNIEnv *env, jobject thiz,
                                                                         jlong handle,
                                                                         jbyteArray jpegBuf,
                                                                         jlong jpegSize,
                                                                         jobject dstBuf,
                                                                         jobject transform,
                                                                         jint flags) {
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDeDestroy(JNIEnv *env, jobject thiz, jlong handle) {
    return 0;
}

JNIEXPORT jbyteArray JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjAlloc(JNIEnv *env, jobject thiz, jint bytes) {
    return NULL;
}

JNIEXPORT jstring JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjGetErrorStr2(JNIEnv *env, jobject thiz, jlong handle) {
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjGetErrorCode(JNIEnv *env, jobject thiz, jlong handle) {
    return 0;
}
#ifdef __cplusplus
}
#endif
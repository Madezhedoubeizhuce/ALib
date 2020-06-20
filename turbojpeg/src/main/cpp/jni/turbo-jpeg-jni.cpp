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
const char *TJTRANSFORM_CLASS = "com/alpha/turbojpeg/bean/TjTransform";

jstring charTojstring(JNIEnv *env, char *pat);
void convertToImageBuf(JNIEnv *env, jobject imgBuf, unsigned char *nativeBuf, unsigned long size);

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitCompress(JNIEnv *env, jobject thiz) {
    LOGD("tjInitCompress");
    tjhandle tjInstance = nullptr;
    tjInstance = tjInitCompress();
    if (tjInstance != nullptr) {
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
                                                                         jint jpegSubsamp,
                                                                         jint jpegQual,
                                                                         jint flags) {
    if (jpegImage == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    jbyte *bytes = env->GetByteArrayElements(srcBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    unsigned char *jpegBuf = NULL;
    unsigned long jpegSize = 0;

    if (tjCompress2(tjInstance, buf, width, pitch, height, pixelFormat,
                    &jpegBuf, &jpegSize, jpegSubsamp, jpegQual, flags) != 0) {
        return -1;
    }

    convertToImageBuf(env, jpegImage, jpegBuf, jpegSize);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjCompressFromYUV(JNIEnv *env, jobject thiz,
                                                        jlong handle, jbyteArray srcBuf, jint width,
                                                        jint pad, jint height, jint subsamp,
                                                        jobject jpegImage, jint jpegQual,
                                                        jint flags) {
    if (jpegImage == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    jbyte *bytes = env->GetByteArrayElements(srcBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    unsigned char *jpegBuf = nullptr;
    unsigned long jpegSize = 0;

    if (tjCompressFromYUV(tjInstance, buf, width, pad, height, subsamp,
                          &jpegBuf, &jpegSize, jpegQual, flags) != 0) {
        return -2;
    }

    convertToImageBuf(env, jpegImage, jpegBuf, jpegSize);

    return 0;
}

JNIEXPORT jlong JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjBufSize(JNIEnv *env, jobject thiz,
                                                                        jint width, jint height,
                                                                        jint jpegSubsamp) {
    return tjBufSize(width, height, jpegSubsamp);
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjBufSizeYUV2(JNIEnv *env, jobject thiz,
                                                    jint width, jint pad, jint height,
                                                    jint jpegSubsamp) {
    return tjBufSizeYUV2(width, pad, height, jpegSubsamp);
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjEncodeYUV3(JNIEnv *env, jobject thiz,
                                                                          jlong handle,
                                                                          jbyteArray srcBuf,
                                                                          jint width, jint pitch,
                                                                          jint height,
                                                                          jint pixelFormat,
                                                                          jbyteArray dstBuf,
                                                                          jint pad,
                                                                          jint subsamp,
                                                                          jint flags) {
    if (dstBuf == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    jbyte *bytes = env->GetByteArrayElements(srcBuf, 0);
    unsigned char *src_native_buf = (unsigned char *) bytes;

    bytes = env->GetByteArrayElements(dstBuf, 0);
    unsigned char *dst_native_buf = (unsigned char *) bytes;

    if (tjEncodeYUV3(tjInstance, src_native_buf, width, pitch, height, pixelFormat,
                     dst_native_buf, pad, subsamp, flags) != 0) {
        return -2;
    }

    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitDecompress(JNIEnv *env, jobject thiz) {
    tjhandle tjInstance = nullptr;
    tjInstance = tjInitDecompress();
    if (tjInstance != nullptr) {
        return (long) tjInstance;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompressHeader3(JNIEnv *env, jobject thiz, jlong handle,
                                                          jbyteArray jpegBuf, jobject jpegHeader) {
    if (jpegHeader == nullptr) {
        return -1;
    }
    tjhandle tjInstance = (tjhandle) handle;

    unsigned long jpegSize = env->GetArrayLength(jpegBuf);
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
                                                    jobject dstBuf,
                                                    jint width, jint pitch, jint height,
                                                    jint pixelFormat, jint flag) {
    if (dstBuf == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    unsigned long jpegSize = env->GetArrayLength(jpegBuf);
    jbyte *bytes = env->GetByteArrayElements(jpegBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    unsigned char *dst_buf_native = nullptr;
    int size = width * height * tjPixelSize[pixelFormat];
    dst_buf_native = tjAlloc(size);
    if (dst_buf_native == nullptr) {
        return -1;
    }

    if (tjDecompress2(tjInstance, buf, jpegSize, dst_buf_native,
                      width, pitch, height, pixelFormat, flag) != 0) {
        return -1;
    }

    convertToImageBuf(env, dstBuf, dst_buf_native, size);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompressToYUV2(JNIEnv *env, jobject thiz,
                                                         jlong handle, jbyteArray jpegBuf,
                                                         jbyteArray dstBuf, jint width, jint pad,
                                                         jint height, jint flags) {
    if (dstBuf == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    unsigned long jpegSize = env->GetArrayLength(jpegBuf);
    jbyte *bytes = env->GetByteArrayElements(jpegBuf, 0);
    unsigned char *jpeg_native_buf = (unsigned char *) bytes;

    bytes = env->GetByteArrayElements(dstBuf, 0);
    unsigned char *dst_native_buf = (unsigned char *) bytes;

    if (tjDecompressToYUV2(tjInstance, jpeg_native_buf, jpegSize, dst_native_buf,
                           width, pad, height, flags) != 0) {
        return -1;
    }

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
    if (dstBuf == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    jbyte *bytes = env->GetByteArrayElements(srcBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    unsigned char *dst_buf_native = nullptr;
    int size = width * height * tjPixelSize[pixelFormat];
    dst_buf_native = tjAlloc(size);

    if (tjDecodeYUV(tjInstance, buf, pad, subsamp, dst_buf_native, width,
                    pitch, height, pixelFormat, flags) != 0) {
        return -2;
    }

    convertToImageBuf(env, dstBuf, dst_buf_native, size);

    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitTransform(JNIEnv *env, jobject thiz) {
    tjhandle tjInstance = nullptr;
    tjInstance = tjInitTransform();
    if (tjInstance != nullptr) {
        return (long) tjInstance;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_com_alpha_turbojpeg_TurboJpegJni_tjTransform(JNIEnv *env, jobject thiz,
                                                                         jlong handle,
                                                                         jbyteArray jpegBuf,
                                                                         jint n,
                                                                         jobject dstBuf,
                                                                         jobject transform,
                                                                         jint flags) {
    if (dstBuf == nullptr || transform == nullptr) {
        return -1;
    }

    tjhandle tjInstance = (tjhandle) handle;

    unsigned long jpegSize = env->GetArrayLength(jpegBuf);
    jbyte *bytes = env->GetByteArrayElements(jpegBuf, 0);
    unsigned char *buf = (unsigned char *) bytes;

    unsigned char *dst_buf_native = nullptr;
    unsigned long dstSize = 0;

    tjtransform xform;
    memset(&xform, 0, sizeof(tjtransform));
    jclass transformClass = env->FindClass(TJTRANSFORM_CLASS);
    jfieldID op_field = (env)->GetFieldID(transformClass, "op", "I");
    jfieldID options_field = (env)->GetFieldID(transformClass, "options", "I");
    int op = env->GetIntField(transform, op_field);
    int options = env->GetIntField(transform, options_field);
    xform.op = op;
    xform.options |= options;

    if (tjTransform(tjInstance, buf, jpegSize, n, &dst_buf_native, &dstSize, &xform, flags) != 0) {
        return -1;
    }

    convertToImageBuf(env, dstBuf, dst_buf_native, dstSize);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDestroy(JNIEnv *env, jobject thiz, jlong handle) {
    return tjDestroy((tjhandle) handle);
}

JNIEXPORT jstring JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjGetErrorStr2(JNIEnv *env, jobject thiz, jlong handle) {
    char *errorStr = tjGetErrorStr2((tjhandle) handle);
    return charTojstring(env, errorStr);
}

jstring charTojstring(JNIEnv *env, char *pat) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("java/lang/String");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte *) pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("UTF8");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

void convertToImageBuf(JNIEnv *env, jobject imgBuf, unsigned char *nativeBuf, unsigned long size) {
    jbyte *dst_buf_bytes = (jbyte *) nativeBuf;

    jclass bufClass = env->FindClass(IMAGEBUF_CLASS);
    jfieldID buf_field = (env)->GetFieldID(bufClass, "buf", "[B");

    jbyteArray dstBufArray = env->NewByteArray(size);

    env->SetByteArrayRegion(dstBufArray, 0, size, dst_buf_bytes);
    env->SetObjectField(imgBuf, buf_field, dstBufArray);
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjGetErrorCode(JNIEnv *env, jobject thiz, jlong handle) {
    return tjGetErrorCode((tjhandle) handle);
}
#ifdef __cplusplus
}
#endif
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

char *ConvertJByteArrayToChars(JNIEnv *env, jbyteArray byte_array);

JNIEXPORT jlong JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjInitCompress(JNIEnv *env, jobject thiz) {
    LOGD("tjInitCompress");
    tjhandle tjInstance = NULL;
    tjInstance = tjInitCompress();
    if (tjInstance) {
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
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompressHeader3(JNIEnv *env, jobject thiz, jlong handle,
                                                          jbyteArray jpegBuf, jlong jpegSize,
                                                          jobject jpegHeader) {
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_alpha_turbojpeg_TurboJpegJni_tjDecompress2(JNIEnv *env, jobject thiz,
                                                    jlong handle, jbyteArray jpegBuf,
                                                    jlong jpegSize, jobject dstBuf,
                                                    jint width, jint pitch, jint height,
                                                    jint pixelFormat, jint flag) {
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
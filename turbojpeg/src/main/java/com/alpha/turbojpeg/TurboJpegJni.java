package com.alpha.turbojpeg;

import com.alpha.turbojpeg.bean.ImageBuf;
import com.alpha.turbojpeg.bean.JpegHeader;
import com.alpha.turbojpeg.bean.TjTransform;

public class TurboJpegJni {
    static {
        System.loadLibrary("turbojpeg-jni");
    }

    public native long tjInitCompress();

    public native int tjCompress2(long handle, byte[] srcBuf, int width, int pitch, int height, int pixelFormat,
                                  ImageBuf jpegImage, int jpegSubsamp, int jpegQual, int flags);

    public native int tjCompressFromYUV(long handle, byte[] srcBuf, int width, int pad, int height, int subsamp,
                                        ImageBuf jpegImage, int jpegQual, int flags);

    public native long tjBufSize(int width, int height, int jpegSubsamp);

    public native long tjBufSizeYUV2(int width, int pad, int height, int jpegSubsamp);

    public native int tjEncodeYUV3(long handle, byte[] srcBuf, int width, int pitch, int height, int pixelFormat,
                                   ImageBuf dstBuf, int pad, int subsamp, int flags);

    public native long tjInitDecompress();

    public native int tjDecompressHeader3(long handle, byte[] jpegBuf, JpegHeader jpegHeader);

    public native int tjDecompress2(long handle, byte[] jpegBuf, ImageBuf dstBuf,
                                    int width, int pitch, int height, int pixelFormat, int flag);

    public native int tjDecompressToYUV2(long handle, byte[] jpegBuf, byte[] dstBuf,
                                         int width, int pad, int height, int flags);

    public native int tjDecodeYUV(long handle, byte[] srcBuf, int pad, int subsamp, ImageBuf dstBuf, int width,
                                  int pitch, int height, int pixelFormat, int flags);

    public native long tjInitTransform();

    public native int tjTransform(long handle, byte[] jpegBuf, int n, ImageBuf dstBuf,
                                  TjTransform transform, int flags);

    public native int tjDestroy(long handle);

    public native String tjGetErrorStr2(long handle);

    public native int tjGetErrorCode(long handle);
}

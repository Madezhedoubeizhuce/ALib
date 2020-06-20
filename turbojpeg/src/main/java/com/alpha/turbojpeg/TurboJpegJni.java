package com.alpha.turbojpeg;

import com.alpha.turbojpeg.bean.ImageBuf;
import com.alpha.turbojpeg.bean.JpegHeader;
import com.alpha.turbojpeg.bean.TjTransform;

/**
 * Turbo jpeg java wrap
 */
public class TurboJpegJni {
    static {
        System.loadLibrary("turbojpeg-jni");
    }

    /**
     * Create a TurboJPEG compressor instance.
     *
     * @return a handle to the newly-created instance, or NULL if an error
     * occurred (see #tjGetErrorStr2().)
     */
    public native long tjInitCompress();

    /**
     * Compress an RGB, grayscale, or CMYK image into a JPEG image.
     *
     * @param handle      a handle to a TurboJPEG compressor or transformer instance
     * @param srcBuf      pointer to an image buffer containing RGB, grayscale, or
     *                    CMYK pixels to be compressed
     * @param width       width (in pixels) of the source image
     * @param pitch       bytes per line in the source image.  Normally, this should be
     *                    <tt>width * #tjPixelSize[pixelFormat]</tt> if the image is unpadded, or
     *                    <tt>#TJPAD(width * #tjPixelSize[pixelFormat])</tt> if each line of the image
     *                    is padded to the nearest 32-bit boundary, as is the case for Windows
     *                    bitmaps.  You can also be clever and use this parameter to skip lines, etc.
     *                    Setting this parameter to 0 is the equivalent of setting it to
     *                    <tt>width * #tjPixelSize[pixelFormat]</tt>.
     * @param height      height (in pixels) of the source image
     * @param pixelFormat pixel format of the source image (see @ref TJPF
     *                    "Pixel formats".)
     * @param jpegImage   address of a pointer to an image buffer that will receive the
     *                    JPEG image. TurboJPEG to allocate the buffer for you.
     * @param jpegSubsamp the level of chrominance subsampling to be used when
     *                    generating the JPEG image (see @ref TJSAMP
     *                    "Chrominance subsampling options".)
     * @param jpegQual    the image quality of the generated JPEG image (1 = worst,
     *                    100 = best)
     * @param flags       the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                    "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjCompress2(long handle, byte[] srcBuf, int width, int pitch, int height, int pixelFormat,
                                  ImageBuf jpegImage, int jpegSubsamp, int jpegQual, int flags);

    /**
     * Compress a YUV planar image into a JPEG image.
     *
     * @param handle    a handle to a TurboJPEG compressor or transformer instance
     * @param srcBuf    pointer to an image buffer containing a YUV planar image to be
     *                  compressed.  The size of this buffer should match the value returned by
     *                  #tjBufSizeYUV2() for the given image width, height, padding, and level of
     *                  chrominance subsampling.  The Y, U (Cb), and V (Cr) image planes should be
     *                  stored sequentially in the source buffer (refer to @ref YUVnotes
     *                  "YUV Image Format Notes".)
     * @param width     width (in pixels) of the source image.  If the width is not an
     *                  even multiple of the MCU block width (see #tjMCUWidth), then an intermediate
     *                  buffer copy will be performed within TurboJPEG.
     * @param pad       the line padding used in the source image.  For instance, if each
     *                  line in each plane of the YUV image is padded to the nearest multiple of 4
     *                  bytes, then <tt>pad</tt> should be set to 4.
     * @param height    height (in pixels) of the source image.  If the height is not
     *                  an even multiple of the MCU block height (see #tjMCUHeight), then an
     *                  intermediate buffer copy will be performed within TurboJPEG.
     * @param subsamp   the level of chrominance subsampling used in the source
     *                  image (see @ref TJSAMP "Chrominance subsampling options".)
     * @param jpegImage address of a pointer to an image buffer that will receive the
     *                  JPEG image.  TurboJPEG to allocate the buffer for you.
     * @param jpegQual  the image quality of the generated JPEG image (1 = worst,
     *                  100 = best)
     * @param flags     the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                  "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjCompressFromYUV(long handle, byte[] srcBuf, int width, int pad, int height, int subsamp,
                                        ImageBuf jpegImage, int jpegQual, int flags);

    /**
     * The maximum size of the buffer (in bytes) required to hold a JPEG image with
     * the given parameters.  The number of bytes returned by this function is
     * larger than the size of the uncompressed source image.  The reason for this
     * is that the JPEG format uses 16-bit coefficients, and it is thus possible
     * for a very high-quality JPEG image with very high-frequency content to
     * expand rather than compress when converted to the JPEG format.  Such images
     * represent a very rare corner case, but since there is no way to predict the
     * size of a JPEG image prior to compression, the corner case has to be
     * handled.
     *
     * @param width       width (in pixels) of the image
     * @param height      height (in pixels) of the image
     * @param jpegSubsamp the level of chrominance subsampling to be used when
     *                    generating the JPEG image (see @ref TJSAMP
     *                    "Chrominance subsampling options".)
     * @return the maximum size of the buffer (in bytes) required to hold the
     * image, or -1 if the arguments are out of bounds.
     */
    public native long tjBufSize(int width, int height, int jpegSubsamp);

    /**
     * The size of the buffer (in bytes) required to hold a YUV planar image with
     * the given parameters.
     *
     * @param width   width (in pixels) of the image
     * @param pad     the width of each line in each plane of the image is padded to
     *                the nearest multiple of this number of bytes (must be a power of 2.)
     * @param height  height (in pixels) of the image
     * @param subsamp level of chrominance subsampling in the image (see
     * @return the size of the buffer (in bytes) required to hold the image, or
     * -1 if the arguments are out of bounds.
     * @ref TJSAMP "Chrominance subsampling options".)
     */
    public native long tjBufSizeYUV2(int width, int pad, int height, int subsamp);

    /**
     * Encode an RGB or grayscale image into a YUV planar image.  This function
     * uses the accelerated color conversion routines in the underlying
     * codec but does not execute any of the other steps in the JPEG compression
     * process.
     *
     * @param handle      a handle to a TurboJPEG compressor or transformer instance
     * @param srcBuf      pointer to an image buffer containing RGB or grayscale pixels
     *                    to be encoded
     * @param width       width (in pixels) of the source image
     * @param pitch       bytes per line in the source image.  Normally, this should be
     *                    <tt>width * #tjPixelSize[pixelFormat]</tt> if the image is unpadded, or
     *                    <tt>#TJPAD(width * #tjPixelSize[pixelFormat])</tt> if each line of the image
     *                    is padded to the nearest 32-bit boundary, as is the case for Windows
     *                    bitmaps.  You can also be clever and use this parameter to skip lines, etc.
     *                    Setting this parameter to 0 is the equivalent of setting it to
     *                    <tt>width * #tjPixelSize[pixelFormat]</tt>.
     * @param height      height (in pixels) of the source image
     * @param pixelFormat pixel format of the source image (see @ref TJPF
     *                    "Pixel formats".)
     * @param dstBuf      pointer to an image buffer that will receive the YUV image.
     *                    Use #tjBufSizeYUV2() to determine the appropriate size for this buffer based
     *                    on the image width, height, padding, and level of chrominance subsampling.
     *                    The Y, U (Cb), and V (Cr) image planes will be stored sequentially in the
     *                    buffer (refer to @ref YUVnotes "YUV Image Format Notes".)
     * @param pad         the width of each line in each plane of the YUV image will be
     *                    padded to the nearest multiple of this number of bytes (must be a power of
     *                    2.)  To generate images suitable for X Video, <tt>pad</tt> should be set to
     *                    4.
     * @param subsamp     the level of chrominance subsampling to be used when
     *                    generating the YUV image (see @ref TJSAMP
     *                    "Chrominance subsampling options".)  To generate images suitable for X
     *                    Video, <tt>subsamp</tt> should be set to @ref TJSAMP_420.  This produces an
     *                    image compatible with the I420 (AKA "YUV420P") format.
     * @param flags       the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                    "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjEncodeYUV3(long handle, byte[] srcBuf, int width, int pitch, int height, int pixelFormat,
                                   byte[] dstBuf, int pad, int subsamp, int flags);

    /**
     * Create a TurboJPEG decompressor instance.
     *
     * @return a handle to the newly-created instance, or NULL if an error
     * occurred (see #tjGetErrorStr2().)
     */
    public native long tjInitDecompress();

    /**
     * Retrieve information about a JPEG image without decompressing it.
     *
     * @param handle     a handle to a TurboJPEG decompressor or transformer instance
     * @param jpegBuf    pointer to a buffer containing a JPEG image
     * @param jpegHeader an object that will receive the header of the JPEG image
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjDecompressHeader3(long handle, byte[] jpegBuf, JpegHeader jpegHeader);

    /**
     * Decompress a JPEG image to an RGB, grayscale, or CMYK image.
     *
     * @param handle      a handle to a TurboJPEG decompressor or transformer instance
     * @param jpegBuf     pointer to a buffer containing the JPEG image to decompress
     * @param dstBuf      pointer to an image buffer that will receive the decompressed
     *                    image.  This buffer should normally be <tt>pitch * scaledHeight</tt> bytes
     *                    in size, where <tt>scaledHeight</tt> can be determined by calling
     *                    #TJSCALED() with the JPEG image height and one of the scaling factors
     *                    returned by #tjGetScalingFactors().  The <tt>dstBuf</tt> pointer may also be
     *                    used to decompress into a specific region of a larger buffer.
     * @param width       desired width (in pixels) of the destination image.  If this is
     *                    different than the width of the JPEG image being decompressed, then
     *                    TurboJPEG will use scaling in the JPEG decompressor to generate the largest
     *                    possible image that will fit within the desired width.  If <tt>width</tt> is
     *                    set to 0, then only the height will be considered when determining the
     *                    scaled image size.
     * @param pitch       bytes per line in the destination image.  Normally, this is
     *                    <tt>scaledWidth * #tjPixelSize[pixelFormat]</tt> if the decompressed image
     *                    is unpadded, else <tt>#TJPAD(scaledWidth * #tjPixelSize[pixelFormat])</tt>
     *                    if each line of the decompressed image is padded to the nearest 32-bit
     *                    boundary, as is the case for Windows bitmaps.  (NOTE: <tt>scaledWidth</tt>
     *                    can be determined by calling #TJSCALED() with the JPEG image width and one
     *                    of the scaling factors returned by #tjGetScalingFactors().)  You can also be
     *                    clever and use the pitch parameter to skip lines, etc.  Setting this
     *                    parameter to 0 is the equivalent of setting it to
     *                    <tt>scaledWidth * #tjPixelSize[pixelFormat]</tt>.
     * @param height      desired height (in pixels) of the destination image.  If this
     *                    is different than the height of the JPEG image being decompressed, then
     *                    TurboJPEG will use scaling in the JPEG decompressor to generate the largest
     *                    possible image that will fit within the desired height.  If <tt>height</tt>
     *                    is set to 0, then only the width will be considered when determining the
     *                    scaled image size.
     * @param pixelFormat pixel format of the destination image (see @ref
     *                    TJPF "Pixel formats".)
     * @param flags       the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                    "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjDecompress2(long handle, byte[] jpegBuf, ImageBuf dstBuf,
                                    int width, int pitch, int height, int pixelFormat, int flags);

    /**
     * Decompress a JPEG image to a YUV planar image.  This function performs JPEG
     * decompression but leaves out the color conversion step, so a planar YUV
     * image is generated instead of an RGB image.
     *
     * @param handle  a handle to a TurboJPEG decompressor or transformer instance
     * @param jpegBuf pointer to a buffer containing the JPEG image to decompress
     * @param dstBuf  pointer to an image buffer that will receive the YUV image.
     *                Use #tjBufSizeYUV2() to determine the appropriate size for this buffer based
     *                on the image width, height, padding, and level of subsampling.  The Y,
     *                U (Cb), and V (Cr) image planes will be stored sequentially in the buffer
     *                (refer to @ref YUVnotes "YUV Image Format Notes".)
     * @param width   desired width (in pixels) of the YUV image.  If this is
     *                different than the width of the JPEG image being decompressed, then
     *                TurboJPEG will use scaling in the JPEG decompressor to generate the largest
     *                possible image that will fit within the desired width.  If <tt>width</tt> is
     *                set to 0, then only the height will be considered when determining the
     *                scaled image size.  If the scaled width is not an even multiple of the MCU
     *                block width (see #tjMCUWidth), then an intermediate buffer copy will be
     *                performed within TurboJPEG.
     * @param pad     the width of each line in each plane of the YUV image will be
     *                padded to the nearest multiple of this number of bytes (must be a power of
     *                2.)  To generate images suitable for X Video, <tt>pad</tt> should be set to
     *                4.
     * @param height  desired height (in pixels) of the YUV image.  If this is
     *                different than the height of the JPEG image being decompressed, then
     *                TurboJPEG will use scaling in the JPEG decompressor to generate the largest
     *                possible image that will fit within the desired height.  If <tt>height</tt>
     *                is set to 0, then only the width will be considered when determining the
     *                scaled image size.  If the scaled height is not an even multiple of the MCU
     *                block height (see #tjMCUHeight), then an intermediate buffer copy will be
     *                performed within TurboJPEG.
     * @param flags   the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjDecompressToYUV2(long handle, byte[] jpegBuf, byte[] dstBuf,
                                         int width, int pad, int height, int flags);

    /**
     * Decode a YUV planar image into an RGB or grayscale image.  This function
     * uses the accelerated color conversion routines in the underlying
     * codec but does not execute any of the other steps in the JPEG decompression
     * process.
     *
     * @param handle      a handle to a TurboJPEG decompressor or transformer instance
     * @param srcBuf      pointer to an image buffer containing a YUV planar image to be
     *                    decoded.  The size of this buffer should match the value returned by
     *                    #tjBufSizeYUV2() for the given image width, height, padding, and level of
     *                    chrominance subsampling.  The Y, U (Cb), and V (Cr) image planes should be
     *                    stored sequentially in the source buffer (refer to @ref YUVnotes
     *                    "YUV Image Format Notes".)
     * @param pad         Use this parameter to specify that the width of each line in each
     *                    plane of the YUV source image is padded to the nearest multiple of this
     *                    number of bytes (must be a power of 2.)
     * @param subsamp     the level of chrominance subsampling used in the YUV source
     *                    image (see @ref TJSAMP "Chrominance subsampling options".)
     * @param dstBuf      pointer to an image buffer that will receive the decoded
     *                    image.  This buffer should normally be <tt>pitch * height</tt> bytes in
     *                    size, but the <tt>dstBuf</tt> pointer can also be used to decode into a
     *                    specific region of a larger buffer.
     * @param width       width (in pixels) of the source and destination images
     * @param pitch       bytes per line in the destination image.  Normally, this should
     *                    be <tt>width * #tjPixelSize[pixelFormat]</tt> if the destination image is
     *                    unpadded, or <tt>#TJPAD(width * #tjPixelSize[pixelFormat])</tt> if each line
     *                    of the destination image should be padded to the nearest 32-bit boundary, as
     *                    is the case for Windows bitmaps.  You can also be clever and use the pitch
     *                    parameter to skip lines, etc.  Setting this parameter to 0 is the equivalent
     *                    of setting it to <tt>width * #tjPixelSize[pixelFormat]</tt>.
     * @param height      height (in pixels) of the source and destination images
     * @param pixelFormat pixel format of the destination image (see @ref TJPF
     *                    "Pixel formats".)
     * @param flags       the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                    "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjDecodeYUV(long handle, byte[] srcBuf, int pad, int subsamp, ImageBuf dstBuf, int width,
                                  int pitch, int height, int pixelFormat, int flags);

    /**
     * Create a new TurboJPEG transformer instance.
     *
     * @return a handle to the newly-created instance, or NULL if an error
     * occurred (see #tjGetErrorStr2().)
     */
    public native long tjInitTransform();

    /**
     * Losslessly transform a JPEG image into another JPEG image.  Lossless
     * transforms work by moving the raw DCT coefficients from one JPEG image
     * structure to another without altering the values of the coefficients.  While
     * this is typically faster than decompressing the image, transforming it, and
     * re-compressing it, lossless transforms are not free.  Each lossless
     * transform requires reading and performing Huffman decoding on all of the
     * coefficients in the source image, regardless of the size of the destination
     * image.  Thus, this function provides a means of generating multiple
     * transformed images from the same source or  applying multiple
     * transformations simultaneously, in order to eliminate the need to read the
     * source coefficients multiple times.
     *
     * @param handle     a handle to a TurboJPEG transformer instance
     * @param jpegBuf    pointer to a buffer containing the JPEG source image to
     *                   transform
     * @param n          the number of transformed JPEG images to generate
     * @param dstBuf     pointer to an array of n image buffers.  <tt>dstBufs[i]</tt>
     *                   will receive a JPEG image that has been transformed using the parameters in
     *                   <tt>transforms[i]</tt>.  TurboJPEG to allocate the buffer for you.
     * @param transforms pointer to an array of n #tjtransform structures, each of
     *                   which specifies the transform parameters and/or cropping region for the
     *                   corresponding transformed output image.
     * @param flags      the bitwise OR of one or more of the @ref TJFLAG_ACCURATEDCT
     *                   "flags"
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2()
     * and #tjGetErrorCode().)
     */
    public native int tjTransform(long handle, byte[] jpegBuf, int n, ImageBuf dstBuf,
                                  TjTransform transforms, int flags);

    /**
     * Destroy a TurboJPEG compressor, decompressor, or transformer instance.
     *
     * @param handle a handle to a TurboJPEG compressor, decompressor or
     *               transformer instance
     * @return 0 if successful, or -1 if an error occurred (see #tjGetErrorStr2().)
     */
    public native int tjDestroy(long handle);

    /**
     * Returns a descriptive error message explaining why the last command failed.
     *
     * @param handle a handle to a TurboJPEG compressor, decompressor, or
     *               transformer instance, or NULL if the error was generated by a global
     *               function (but note that retrieving the error message for a global function
     *               is not thread-safe.)
     * @return a descriptive error message explaining why the last command failed.
     */
    public native String tjGetErrorStr2(long handle);

    /**
     * Returns a code indicating the severity of the last error.  See
     *
     * @param handle a handle to a TurboJPEG compressor, decompressor or
     *               transformer instance
     * @return a code indicating the severity of the last error.  See
     * @ref TJERR "Error codes".
     * @ref TJERR "Error codes".
     */
    public native int tjGetErrorCode(long handle);
}

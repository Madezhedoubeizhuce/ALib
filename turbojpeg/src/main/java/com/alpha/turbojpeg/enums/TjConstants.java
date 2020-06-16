package com.alpha.turbojpeg.enums;

public class TjConstants {
    /**
     * This option will cause #tjTransform() to return an error if the transform is
     * not perfect.  Lossless transforms operate on MCU blocks, whose size depends
     * on the level of chrominance subsampling used (see #tjMCUWidth
     * and #tjMCUHeight.)  If the image's width or height is not evenly divisible
     * by the MCU block size, then there will be partial MCU blocks on the right
     * and/or bottom edges.  It is not possible to move these partial MCU blocks to
     * the top or left of the image, so any transform that would require that is
     * "imperfect."  If this option is not specified, then any partial MCU blocks
     * that cannot be transformed will be left in place, which will create
     * odd-looking strips on the right or bottom edge of the image.
     */
    public static final int TJXOPT_PERFECT = 1;
    /**
     * This option will cause #tjTransform() to discard any partial MCU blocks that
     * cannot be transformed.
     */
    public static final int TJXOPT_TRIM = 2;
    /**
     * This option will enable lossless cropping.  See #tjTransform() for more
     * information.
     */
    public static final int TJXOPT_CROP = 4;
    /**
     * This option will discard the color data in the input image and produce
     * a grayscale output image.
     */
    public static final int TJXOPT_GRAY = 8;
    /**
     * This option will prevent #tjTransform() from outputting a JPEG image for
     * this particular transform (this can be used in conjunction with a custom
     * filter to capture the transformed DCT coefficients without transcoding
     * them.)
     */
    public static final int TJXOPT_NOOUTPUT = 16;
    /**
     * This option will enable progressive entropy coding in the output image
     * generated by this particular transform.  Progressive entropy coding will
     * generally improve compression relative to baseline entropy coding (the
     * default), but it will reduce compression and decompression performance
     * considerably.
     */
    public static final int TJXOPT_PROGRESSIVE = 32;
    /**
     * This option will prevent #tjTransform() from copying any extra markers
     * (including EXIF and ICC profile data) from the source image to the output
     * image.
     */
    public static final int TJXOPT_COPYNONE = 64;
}
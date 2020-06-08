package com.alpha.turbojpeg.enums;

public enum TJSAMP {
    /**
     * 4:4:4 chrominance subsampling (no chrominance subsampling).  The JPEG or
     * YUV image will contain one chrominance component for every pixel in the
     * source image.
     */
    TJSAMP_444,
    /**
     * 4:2:2 chrominance subsampling.  The JPEG or YUV image will contain one
     * chrominance component for every 2x1 block of pixels in the source image.
     */
    TJSAMP_422,
    /**
     * 4:2:0 chrominance subsampling.  The JPEG or YUV image will contain one
     * chrominance component for every 2x2 block of pixels in the source image.
     */
    TJSAMP_420,
    /**
     * Grayscale.  The JPEG or YUV image will contain no chrominance components.
     */
    TJSAMP_GRAY,
    /**
     * 4:4:0 chrominance subsampling.  The JPEG or YUV image will contain one
     * chrominance component for every 1x2 block of pixels in the source image.
     *
     * @note 4:4:0 subsampling is not fully accelerated in libjpeg-turbo.
     */
    TJSAMP_440,
    /**
     * 4:1:1 chrominance subsampling.  The JPEG or YUV image will contain one
     * chrominance component for every 4x1 block of pixels in the source image.
     * JPEG images compressed with 4:1:1 subsampling will be almost exactly the
     * same size as those compressed with 4:2:0 subsampling, and in the
     * aggregate, both subsampling methods produce approximately the same
     * perceptual quality.  However, 4:1:1 is better able to reproduce sharp
     * horizontal features.
     *
     * @note 4:1:1 subsampling is not fully accelerated in libjpeg-turbo.
     */
    TJSAMP_411
}

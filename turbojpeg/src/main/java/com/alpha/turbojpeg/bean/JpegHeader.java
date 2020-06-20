package com.alpha.turbojpeg.bean;

public class JpegHeader {
    /**
     * pointer to an integer variable that will receive the width (in
     * pixels) of the JPEG image
     */
    public int width;
    /**
     * pointer to an integer variable that will receive the height
     * (in pixels) of the JPEG image
     */
    public int height;
    /**
     * pointer to an integer variable that will receive the
     * level of chrominance subsampling used when the JPEG image was compressed
     * (see @ref TJSAMP "Chrominance subsampling options".)
     */
    public int jepgSubsamp;
    /**
     * pointer to an integer variable that will receive one
     * of the JPEG colorspace constants, indicating the colorspace of the JPEG
     * image (see @ref TJCS "JPEG colorspaces".)
     */
    public int jpegColorspace;
}

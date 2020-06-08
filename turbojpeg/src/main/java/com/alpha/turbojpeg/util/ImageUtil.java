package com.alpha.turbojpeg.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.alpha.turbojpeg.util.CloseUtil.close;

public class ImageUtil {
    public static byte[] readImageFile(String imgPath, String imgName) throws IOException {
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        File imgFile = new File(imgPath, imgName);
        if (!imgFile.exists()) {
            throw new RuntimeException(imgPath + "/" + imgName + " not exist");
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(imgFile));
            byte[] buf = new byte[1024];
            while (bis.read(buf) != -1) {
                byteOs.write(buf);
            }
        } finally {
            close(bis);
        }
        return byteOs.toByteArray();
    }

    public static void writeImageFile(byte[] jpegBuf, String imgPath, String imgName) throws IOException {
        File imgFile = new File(imgPath, imgName);
        if (imgFile.getParentFile() != null && !imgFile.getParentFile().exists()) {
            imgFile.getParentFile().mkdirs();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(imgFile));
            bos.write(jpegBuf);
        } finally {
            close(bos);
        }
    }
}

package com.alpha.turbojpeg.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.alpha.turbojpeg.util.CloseUtil.close;

public class AssetFileExtraction {
    private static final String TAG = "AssetFileExtraction";

    public static void extract(Context context, String path, String dstDir) throws IOException {
        Log.d(TAG, "extract: dst dir " + dstDir);

        AssetManager assetManager = context.getResources().getAssets();
        String[] fileList = assetManager.list(path);

        for (String file : fileList) {
            Log.d(TAG, file);

            InputStream is = null;
            BufferedOutputStream bos = null;

            try {
                is = assetManager.open(path + "/" + file);
                File extractFile = new File(dstDir, file);
                if (extractFile.getParentFile() != null && !extractFile.getParentFile().exists()) {
                    extractFile.getParentFile().mkdirs();
                }
                bos = new BufferedOutputStream(new FileOutputStream(extractFile));
                byte[] buf = new byte[1024];
                int readSize;
                while ((readSize = is.read(buf)) != -1) {
                    bos.write(buf, 0, readSize);
                }
            } finally {
                close(is);
                close(bos);
            }
        }
    }


}

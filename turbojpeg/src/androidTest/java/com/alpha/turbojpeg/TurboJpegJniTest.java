package com.alpha.turbojpeg;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.alpha.turbojpeg.bean.ImageBuf;
import com.alpha.turbojpeg.bean.JpegHeader;
import com.alpha.turbojpeg.enums.TJCS;
import com.alpha.turbojpeg.enums.TJPF;
import com.alpha.turbojpeg.enums.TJSAMP;
import com.alpha.turbojpeg.util.AssetFileExtraction;
import com.alpha.turbojpeg.util.ImageUtil;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TurboJpegJniTest {
    private static final String TAG = "TurboJpegJniTest";

    private Context appContext;
    private String dstDir;
    private TurboJpegJni jpegJni;

    @Before
    public void init() throws IOException {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dstDir = appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        AssetFileExtraction.extract(appContext, "img", dstDir);
        jpegJni = new TurboJpegJni();
    }

    @Test
    public void testDecompressJpeg() {
        try {
            long handle = jpegJni.tjInitDecompress();
            assertNotEquals(0, handle);

            byte[] jpegBuf = ImageUtil.readImageFile(dstDir, "test.jpg");

            JpegHeader header = new JpegHeader();
            jpegJni.tjDecompressHeader3(handle, jpegBuf, jpegBuf.length, header);

            Log.i(TAG, "testDecompressJpeg: header width " + header.width + ", height " + header.height +
                    ", jepgSubsamp " + header.jepgSubsamp + ", jpegColorspace " + header.jpegColorspace);

            assertEquals(500, header.width);
            assertEquals(333, header.height);
            assertEquals(TJSAMP.TJSAMP_444.ordinal(), header.jepgSubsamp);
            assertEquals(TJCS.TJCS_YCbCr.ordinal(), header.jpegColorspace);

            ImageBuf dstBuf = new ImageBuf();
            jpegJni.tjDecompress2(handle, jpegBuf, jpegBuf.length, dstBuf, header.width,
                    0, header.height, TJPF.TJPF_RGB.ordinal(), 0);

            assertNotNull(dstBuf.buf);
            assertTrue(dstBuf.buf.length > 0);

            // 保存转换后的rgb文件，使用工具查看文件是否正常
            ImageUtil.writeImageFile(dstBuf.buf, dstDir + "/out", "test.rgb");
        } catch (IOException e) {
            Log.e(TAG, "testDecompressJpeg: ", e);
        }
    }
}
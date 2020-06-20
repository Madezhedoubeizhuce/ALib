package com.alpha.turbojpeg;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import com.alpha.turbojpeg.bean.ImageBuf;
import com.alpha.turbojpeg.bean.JpegHeader;
import com.alpha.turbojpeg.bean.TjTransform;
import com.alpha.turbojpeg.enums.TJCS;
import com.alpha.turbojpeg.enums.TJPF;
import com.alpha.turbojpeg.enums.TJSAMP;
import com.alpha.turbojpeg.enums.TJXOP;
import com.alpha.turbojpeg.enums.TjConstants;
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
        long handle = jpegJni.tjInitDecompress();
        assertNotEquals(0, handle);

        try {
            byte[] jpegBuf = ImageUtil.readImageFile(dstDir, "test.jpg");

            JpegHeader header = readJpegHeader();

            ImageBuf dstBuf = new ImageBuf();
            int ret = jpegJni.tjDecompress2(handle, jpegBuf, dstBuf, header.width,
                    0, header.height, TJPF.TJPF_RGB.ordinal(), 0);
            assertEquals(0, ret);

            assertNotNull(dstBuf.buf);
            assertTrue(dstBuf.buf.length > 0);

            // 保存转换后的rgb文件，使用工具查看文件是否正常
            ImageUtil.writeImageFile(dstBuf.buf, dstDir + "/out", "decompress_test.rgb");
        } catch (IOException e) {
            Log.e(TAG, "testDecompressJpeg: ", e);
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    @Test
    public void testTransform() {
        long handle = jpegJni.tjInitTransform();
        assertNotEquals(0, handle);

        try {
            byte[] jpegBuf = ImageUtil.readImageFile(dstDir, "test.jpg");
            ImageBuf dstBuf = new ImageBuf();
            TjTransform tjTransform = new TjTransform();
            tjTransform.op = TJXOP.TJXOP_HFLIP.ordinal();
            tjTransform.options = TjConstants.TJXOPT_TRIM;

            int ret = jpegJni.tjTransform(handle, jpegBuf, 1, dstBuf, tjTransform, 0);
            assertEquals(0, ret);

            assertNotNull(dstBuf.buf);
            assertTrue(dstBuf.buf.length > 0);

            // 保存转换后的rgb文件，使用工具查看文件是否正常
            ImageUtil.writeImageFile(dstBuf.buf, dstDir + "/out", "transfrom_test.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    @Test
    public void testDecompressToYUV() {
        long handle = jpegJni.tjInitDecompress();
        assertNotEquals(0, handle);

        try {
            byte[] jpegBuf = ImageUtil.readImageFile(dstDir, "test.jpg");

            JpegHeader header = readJpegHeader();

            int pad = 1;
            long bufSize = jpegJni.tjBufSizeYUV2(header.width, pad, header.height, header.jepgSubsamp);
            assertTrue(bufSize > 0);
            byte[] dstBuf = new byte[(int) bufSize];
            int ret = jpegJni.tjDecompressToYUV2(handle, jpegBuf, dstBuf, header.width, pad, header.height, 0);
            assertEquals(0, ret);
            ImageUtil.writeImageFile(dstBuf, dstDir + "/out", "decompress_to_yuv_test.yuv");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    private JpegHeader readJpegHeader() throws IOException {
        long handle = jpegJni.tjInitDecompress();
        assertNotEquals(0, handle);

        JpegHeader header;
        try {
            byte[] jpegBuf = ImageUtil.readImageFile(dstDir, "test.jpg");

            header = new JpegHeader();
            int ret = jpegJni.tjDecompressHeader3(handle, jpegBuf, header);
            assertEquals(0, ret);

            Log.i(TAG, "testDecompressJpeg: header width " + header.width + ", height " + header.height +
                    ", jepgSubsamp " + header.jepgSubsamp + ", jpegColorspace " + header.jpegColorspace);

            assertEquals(500, header.width);
            assertEquals(333, header.height);
            assertEquals(TJSAMP.TJSAMP_444.ordinal(), header.jepgSubsamp);
            assertEquals(TJCS.TJCS_YCbCr.ordinal(), header.jpegColorspace);
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }

        return header;
    }

    @Test
    public void testCompress() {
        long handle = jpegJni.tjInitCompress();
        assertNotEquals(0, handle);

        try {
            JpegHeader header = readJpegHeader();

            byte[] srcBuf = ImageUtil.readImageFile(dstDir, "test.rgb");
            ImageBuf jpegBuf = new ImageBuf();
            int ret = jpegJni.tjCompress2(handle, srcBuf, header.width, 0, header.height,
                    TJPF.TJPF_RGB.ordinal(), jpegBuf, header.jepgSubsamp, 95, 0);
            assertEquals(0, ret);
            ImageUtil.writeImageFile(jpegBuf.buf, dstDir + "/out", "compress_test.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    @Test
    public void testCompressFromYUV() {
        long handle = jpegJni.tjInitCompress();
        assertNotEquals(0, handle);

        try {
            JpegHeader header = readJpegHeader();

            byte[] srcBuf = ImageUtil.readImageFile(dstDir, "test.yuv");
            ImageBuf jpegBuf = new ImageBuf();
            int ret = jpegJni.tjCompressFromYUV(handle, srcBuf, header.width, 4, header.height,
                    header.jepgSubsamp, jpegBuf, 95, 0);
            assertEquals(0, ret);
            ImageUtil.writeImageFile(jpegBuf.buf, dstDir + "/out", "compress_from_yuv_test.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    @Test
    public void testDecodeYUV() {
        long handle = jpegJni.tjInitDecompress();
        assertNotEquals(0, handle);

        try {
            JpegHeader header = readJpegHeader();

            byte[] srcBuf = ImageUtil.readImageFile(dstDir, "test.yuv");
            ImageBuf dstBuf = new ImageBuf();
            int ret = jpegJni.tjDecodeYUV(handle, srcBuf, 4, header.jepgSubsamp, dstBuf,
                    header.width, 0, header.height, TJPF.TJPF_RGB.ordinal(), 0);
            if (ret != 0) {
                Log.e(TAG, "testDecodeYUV: error code " + jpegJni.tjGetErrorCode(handle) + ", " + jpegJni.tjGetErrorStr2(handle));
            }
            assertEquals(0, ret);
            ImageUtil.writeImageFile(dstBuf.buf, dstDir + "/out", "decode_yuv_test.rgb");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }

    @Test
    public void testEncodeYUV() {
        long handle = jpegJni.tjInitCompress();
        assertNotEquals(0, handle);

        try {
            JpegHeader header = readJpegHeader();

            byte[] srcBuf = ImageUtil.readImageFile(dstDir, "test.rgb");

            int pad = 1;
            long bufSize = jpegJni.tjBufSizeYUV2(header.width, pad, header.height, header.jepgSubsamp);
            byte[] dstBuf = new byte[(int) bufSize];

            int ret = jpegJni.tjEncodeYUV3(handle, srcBuf, header.width, 0, header.height, TJPF.TJPF_RGB.ordinal()
                    , dstBuf, pad, header.jepgSubsamp, 0);
            assertEquals(0, ret);
            ImageUtil.writeImageFile(dstBuf, dstDir + "/out", "encode_yuv_test.yuv");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            int ret = jpegJni.tjDestroy(handle);
            assertEquals(0, ret);
        }
    }
}
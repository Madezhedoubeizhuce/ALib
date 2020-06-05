package com.alpha.imagelib;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.alpha.imagelib.util.BreakpadUtil;
import com.alpha.turbojpeg.TurboJpegJni;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBreakpad();
        turboJpegTest();
    }

    private void initBreakpad() {
        BreakpadUtil.initExternalReportPath();
        BreakpadUtil.initBreakPad(this);
    }

    private void turboJpegTest() {
        TurboJpegJni jni = new TurboJpegJni();
        jni.tjInitCompress();
    }
}

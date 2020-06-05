package com.alpha.imagelib.util;

import android.content.Context;
import android.os.Environment;

import com.alpha.breakpad.BreakpadInit;

import java.io.File;

public class BreakpadUtil {
    private static File externalReportPath;

    public static void initExternalReportPath() {
        externalReportPath = new File(Environment.getExternalStorageDirectory(), "crashDump");
        if (!externalReportPath.exists()) {
            externalReportPath.mkdirs();
        }
    }

    public static void initBreakPad(Context context) {
        if (externalReportPath == null) {
            externalReportPath = new File(context.getFilesDir(), "crashDump");
            if (!externalReportPath.exists()) {
                externalReportPath.mkdirs();
            }
        }
        BreakpadInit.initBreakpad(externalReportPath.getAbsolutePath());
    }
}

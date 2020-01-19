package com.example.constom.base;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceUtil {
    public static boolean checkIsMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static String getSystemProperty(String propName) {
        BufferedReader input = null;

        Object var4;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            String line = input.readLine();
            input.close();
            return line;
        } catch (IOException var14) {
            var4 = null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException var13) {
                }
            }

        }

        return (String) var4;
    }
}

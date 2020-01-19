package com.example.constom.base;

import com.yixia.base.log.YXLog;
import com.yixia.base.utils.RefUtils;

import java.lang.reflect.Method;

public class HookGCThreadUtils {
    public static void FixTimeOutException() {
        try {
            Class<?> clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
            if (null != clazz) {
                Object obj = RefUtils.readStaticField(clazz, "INSTANCE");
                Method stop = RefUtils.getMethod(clazz, "stop", new Class[0]);
                if (null != stop && null != obj) {
                    stop.invoke(obj);
                }
            }
        } catch (Exception var3) {
            YXLog.e(var3);
        }

    }
}

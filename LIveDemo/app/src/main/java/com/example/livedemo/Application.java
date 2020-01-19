package com.example.livedemo;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.constom.base.HookGCThreadUtils;
import com.example.livedemo.hotfix.HotFix;

import androidx.multidex.MultiDexApplication;

public class Application extends MultiDexApplication {
    private boolean isMainProcess;

    @Override
    protected void attachBaseContext(Context base) {
        isMainProcess = mainProcess(base);
        Log.e("tag", "attachBaseContext: ------isMainProcess=" + isMainProcess);
        super.attachBaseContext(base);
        HotFix.init(base);
        HookGCThreadUtils.FixTimeOutException();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("tag", "onCreate------=");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e("tag", "onTerminate: ------" );
    }

    /**
     * 判断是否在主进程
     *
     * @return true：在  false：不在
     */
    private boolean mainProcess(Context context) {
        String processName = getProcessName();
        return TextUtils.isEmpty(processName) || processName.equals(context.getPackageName());
    }
}

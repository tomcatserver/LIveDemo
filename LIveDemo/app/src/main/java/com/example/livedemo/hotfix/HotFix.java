package com.example.livedemo.hotfix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;

import com.tencent.bugly.beta.tinker.TinkerApplicationLike;
import com.tencent.bugly.beta.tinker.TinkerManager;
import com.yixia.base.log.YXLog;
import com.yixia.base.network.RequestExecutor;
import com.yixia.base.thread.YXThreadPool;
import com.yixia.base.thread.runnable.YXRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tv.xiaoka.base.network.DownloadRequest;
import tv.xiaoka.base.network.OnProgressChangedListener;

/**
 * com.yixia.live.hotfix.HotFix
 *
 * @author Just.T
 * @since 2018/7/18
 */
public class HotFix {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private PatchConfig config;
    @SuppressLint("StaticFieldLeak")
    private static HotFix instance = new HotFix();
    private boolean running = false;
    private Patch patch;
    private OnProgressChangedListener listener = new OnProgressChangedListener() {

        @Override
        public void onTotalSize(long totalSize) {

        }

        @Override
        public void onProgressChanged(long progress) {

        }

        @Override
        public void onFinish(boolean isSuccess) {
            if (isSuccess && config.checkFile(patch.sign)) {
                TinkerManager.getInstance().applyPatch(mContext, config.patchCache);
                config.putCurrentSign(patch.sign);
                YXLog.trace(map(patch));
            }
            running = false;
        }
    };

    private Map<String, String> map(Patch patch) {
        Map<String, String> map = new HashMap<>();
        map.put("patch", patch.toString());
        return map;
    }

    private HotFix() {
    }

    public static void init(Context context) {
        mContext = context;
        TinkerManager.installTinker(TinkerApplicationLike.getTinkerPatchApplicationLike());
    }

    public static HotFix instance() {
        return instance;
    }

    /**
     * 获取Patch信息
     */
    public void start() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("The hot fix not in main Thread");
        }
        if (running) {
            return;
        }
        if (config == null) {
            config = new PatchConfig(mContext);
        }
        running = true;
        YXThreadPool.instance().excute(new YXRunnable() {
            @Override
            public void runInTryCatch() {
                //同步获取Patch信息
                PatchTask task = new PatchTask();
                RequestExecutor.getInstance().startSyncRequest(task);
                patch = task.getPatch();
                //当前本地存储的签名 只有下载成功才会存储
                String currentSign = config.getCurrentSign();
                //如果接口有返回 并且 与本地签名不一致 直接去下载
                if (null != patch && !currentSign.equals(patch.sign)) {
                    downloadPatch();
                    return;
                }
                //如果签名一致 并且文件存在  检查文件与签名是否匹配  使用Patch修复
                if (config.patchExists() && config.checkFile(currentSign)) {
                    TinkerManager.getInstance().applyPatch(mContext, config.patchCache);
                    running = false;
                }
            }
        });
    }

    private void downloadPatch() {
        if (config.checkDownload(patch)) {
            DownloadRequest downloadRequest = new DownloadRequest() {
                @Override
                public String getRequestUrl() {
                    return patch.url;
                }
            };
            downloadRequest.get(null, new File(config.patchCache), listener);
        } else {
            running = false;
        }
    }

    public void fix() {
        if (config == null) {
            config = new PatchConfig(mContext);
        }
        TinkerManager.getInstance().applyPatch(mContext, config.patchCache);
    }
}

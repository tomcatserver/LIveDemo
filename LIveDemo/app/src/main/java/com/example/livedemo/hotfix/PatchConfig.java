package com.example.livedemo.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.File;

import tv.xiaoka.base.base.Constants;

/**
 * com.yixia.live.hotfix.PatchConfig
 *
 * @author Just.T
 * @since 2018/7/18
 */
class PatchConfig {

    private Context context;
    private SharedPreferences sharedPreferences;
    private final String PATCH_SIGN = "patch_sign";
    public final String patchCache;
    private final File patchFile;

    PatchConfig(Context context) {
        this.context = context;
        this.patchCache = context.getFilesDir().getAbsolutePath() + File.separator + "app.hf";
        this.patchFile = new File(patchCache);
    }


    /**
     * Patch校验
     */
    boolean checkDownload(Patch patch) {
        if (patch == null || TextUtils.isEmpty(patch.url)) {
            return false;
        }
        String currentSign = getCurrentSign();
        return TextUtils.isEmpty(currentSign) || !currentSign.equals(patch.sign);
    }

    /**
     * 获取签名
     */
    String getCurrentSign() {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants
                    .NAME_APP_SETTING, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(PATCH_SIGN, "");
    }

    /**
     * 重置签名
     */
    void putCurrentSign(String sign) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants
                    .NAME_APP_SETTING, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(PATCH_SIGN, sign).apply();
    }


    boolean patchExists() {
        return patchFile.exists();
    }


    /**
     * 校验文件和Sign
     */
    boolean checkFile(String sign) {
        String md5 = PatchUtil.MD5(patchFile);
        try {
            boolean verify = PatchUtil.verify(md5, sign);
            if (!verify) {
                patchFile.delete();
            }
            return verify;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

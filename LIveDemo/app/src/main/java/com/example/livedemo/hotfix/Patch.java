package com.example.livedemo.hotfix;

import androidx.annotation.Keep;

/**
 * com.yixia.live.hotfix.Patch
 *
 * @author Just.T
 * @since 2018/7/18
 */
@Keep
public class Patch {
    String url;
    String sign;
    String flag;
    String desc;

    @Override
    public String toString() {
        return "Patch{" +
                "url='" + url + '\'' +
                ", sign='" + sign + '\'' +
                ", flag='" + flag + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

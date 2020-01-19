package com.example.livedemo.hotfix;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yixia.base.bean.ResponseBean;
import com.yixia.base.network.ConfigConstant;
import com.yixia.base.network.TaskProperty;
import com.yizhibo.framework.task.FormTask;

import java.io.Reader;
import java.lang.reflect.Type;

@TaskProperty(
        s = "com.yzb.service.clientapi.api.HotfixApi",
        m = "getPatchInfo")
class PatchTask extends FormTask<Patch> {
    @Override
    protected String getPath() {
        return "/client/patch";
    }

    @Override
    protected String getHost() {
        return ConfigConstant.HOST;
    }

    @Override
    public void onRequestResult(Reader reader) {
        Type type = new TypeToken<ResponseBean<Patch>>() {
        }.getType();
        responseBean = new Gson().fromJson(reader, type);
    }

    Patch getPatch() {
        return responseBean == null ? null : responseBean.getData();
    }

    @Override
    public boolean zip() {
        return false;
    }
}
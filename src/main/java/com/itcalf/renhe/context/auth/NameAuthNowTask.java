package com.itcalf.renhe.context.auth;

import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.bean.NameAuthNowRes;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class NameAuthNowTask extends AsyncTask<String, Integer, NameAuthNowRes> {

    private NameAuthNowTaskListener listener;

    public NameAuthNowTask(NameAuthNowTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected NameAuthNowRes doInBackground(String... params) {

        Map<String, Object> reqParams = new HashMap<String, Object>();

        reqParams.put("sid", params[0]);
        reqParams.put("adSId", params[1]);
        reqParams.put("name", params[2]);
        reqParams.put("code", params[3]);
        try {
            NameAuthNowRes res = (NameAuthNowRes) HttpUtil.doHttpRequest(Constants.Http.REALNAME_AUTH_NOW, reqParams,
                    NameAuthNowRes.class, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(NameAuthNowRes result) {
        if (listener != null) {
            listener.postExecute(result);
        }
    }

    public interface NameAuthNowTaskListener {
        public abstract void postExecute(NameAuthNowRes result);
    }
}

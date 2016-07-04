package com.itcalf.renhe.context.auth;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.NameAuthRes;
import com.itcalf.renhe.utils.HttpUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NameAuthTask extends AsyncTask<String, Integer, NameAuthRes> {

    private NameAuthTaskListener listener;
    private Context context;

    public NameAuthTask(Context context, NameAuthTaskListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected NameAuthRes doInBackground(String... params) {

        try {
            return nameAuth(Constants.Http.REALNAME_AUTH, params[0], params[1], params[2], params[3], params[4],
                    params[5]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(NameAuthRes result) {
        if (listener != null) {
            listener.postExecute(result);
        }
    }

    /**
     * 实名认证
     */
    private NameAuthRes nameAuth(String webServiceURL, String sid, String adsid, String type, String name, String code,
                                 String picfilepath) throws Exception {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("sid", sid);
        reqParams.put("adSId", adsid);
        reqParams.put("type", type);
        reqParams.put("name", name);
        reqParams.put("code", code);
        if (!TextUtils.isEmpty(picfilepath)) {
            File file = new File(picfilepath);
            if (file.exists()) {
                String url = webServiceURL + "?sid=" + RenheApplication.getInstance().getUserInfo().getSid() + "&adSId="
                        + RenheApplication.getInstance().getUserInfo().getAdSId() + "&type=" + type + "&name=" + name + "&code="
                        + code;
                return (NameAuthRes) HttpUtil.uploadFile(url, file, "pic", NameAuthRes.class);
            }
        }
        return (NameAuthRes) HttpUtil.doHttpRequest(webServiceURL, reqParams, NameAuthRes.class, context);
    }

    public interface NameAuthTaskListener {
        void postExecute(NameAuthRes result);
    }
}

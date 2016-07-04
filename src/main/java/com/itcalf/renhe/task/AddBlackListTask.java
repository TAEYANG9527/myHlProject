package com.itcalf.renhe.task;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangning on 2016/2/26.
 */
public class AddBlackListTask extends AsyncTask<String, Void, MessageBoardOperation> {
    private Context mContext;
    private AsyncTaskCallBack asyncTaskCallBack;

    public AddBlackListTask(Context mContext, AsyncTaskCallBack asyncTaskCallBack) {
        this.mContext = mContext;
        this.asyncTaskCallBack = asyncTaskCallBack;
    }

    @Override
    protected MessageBoardOperation doInBackground(String... params) {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", params[0]);
        reqParams.put("adSId", params[1]);
        reqParams.put("blockedMemberSId", params[2]);
        try {
            MessageBoardOperation mbo = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.ADDBLACKLIST, reqParams,
                    MessageBoardOperation.class, mContext);
            return mbo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(MessageBoardOperation result) {
        super.onPostExecute(result);
        if (null != asyncTaskCallBack)
            asyncTaskCallBack.doPost(result);

    }
}

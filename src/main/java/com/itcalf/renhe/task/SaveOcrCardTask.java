package com.itcalf.renhe.task;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.OcrCardOperation;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.orhanobut.logger.Logger;

import java.util.Map;

/**
 * Feature:留言列表异步加载
 */
public class SaveOcrCardTask extends AsyncTask<Object, Void, OcrCardOperation> {
    // 数据回调
    private Context mContext;
    private Map<String, Object> reqParams;
    // 数据回调
    private IBack mBack;

    public SaveOcrCardTask(Context mContext, Map<String, Object> reqParams, IBack mBack) {
        this.mContext = mContext;
        this.reqParams = reqParams;
        this.mBack = mBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBack.onPre();
    }

    // 后台线程调用服务端接口
    @Override
    protected OcrCardOperation doInBackground(Object... params) {
        if (-1 == NetworkUtil.hasNetworkConnection(mContext)) {//网络未连接
            return null;
        }
        try {
            return (OcrCardOperation) HttpUtil.doHttpRequest(Constants.Http.UPLOAD_OCR_CARD, reqParams, OcrCardOperation.class,
                    mContext);
        } catch (Exception e) {
            Logger.e("SaveOcrCardTask===>" + e);
            mBack.doPost(null);
        }
        return null;
    }

    @Override
    protected void onPostExecute(OcrCardOperation result) {
        super.onPostExecute(result);
        mBack.doPost(result);
    }

    public interface IBack {

        void onPre();

        void doPost(OcrCardOperation result);

    }
}

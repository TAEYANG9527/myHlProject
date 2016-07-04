package com.itcalf.renhe.context.relationship;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.SearchRelationship;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chan
 * @description 高级人脉搜索
 * @date 2015-4-22
 */
public class SeniorSearchRelationshipTask extends AsyncTask<Object, Void, SearchRelationship> {

    private IDataBack mDataBack;
    private Context mContext;
    private String[] mFrom = new String[]{"headImage", "nameTv", "titleTv", "infoTv", "rightImage", "sid", "accountType",
            "isRealName", "isConnection", "isInvite", "beInvitedInfo", "imId", "isReceived"};

    public SeniorSearchRelationshipTask(Context context, IDataBack back) {
        super();
        this.mContext = context;
        this.mDataBack = back;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDataBack.onPre();
    }

    @Override
    protected void onPostExecute(SearchRelationship result) {
        super.onPostExecute(result);
        if (null != result) {
            if (1 == result.getState()) {
                if (null != result.getMemberList() && result.getMemberList().length > 0) {
                    List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < result.getMemberList().length; i++) {
                        final Map<String, Object> map = new LinkedHashMap<String, Object>();
                        map.put("avatar_path", result.getMemberList()[i].getUserFace());
                        map.put(mFrom[1], result.getMemberList()[i].getName());
                        map.put(mFrom[2], result.getMemberList()[i].getCurTitle());
                        map.put(mFrom[3], result.getMemberList()[i].getCurCompany());
                        //好友关系
                        map.put(mFrom[4], R.drawable.icon_1st);
                        map.put(mFrom[5], result.getMemberList()[i].getSid());
                        map.put(mFrom[6], result.getMemberList()[i].getAccountType());
                        map.put(mFrom[7], result.getMemberList()[i].isRealname());
                        map.put(mFrom[8], result.getMemberList()[i].isConnection());
                        map.put(mFrom[9], result.getMemberList()[i].isInvite());
                        map.put(mFrom[10], result.getMemberList()[i].getBeInvitedInfo());
                        if (null != result.getMemberList()[i].getUserInfo() && null != result.getMemberList()[i].getUserInfo().getContactInfo()) {
                            map.put(mFrom[11], result.getMemberList()[i].getUserInfo().getContactInfo().getImId());
                        } else {
                            map.put(mFrom[11], 0);
                        }
                        map.put(mFrom[12], result.getMemberList()[i].isReceived());
                        rsList.add(map);
                    }
                    mDataBack.onPost(rsList);
                    return;
                }
            } else {
                mDataBack.onPostExtra(result);
                return;
            }
        }
        mDataBack.onPost(null);
    }

    @Override
    protected SearchRelationship doInBackground(Object... params) {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getSid());
        reqParams.put("text", params[0]);

        if ("-1".equals(String.valueOf(params[1]))) {
            params[1] = "";
        }
        reqParams.put("location", params[1]);

        if ("-1".equals(String.valueOf(params[2]))) {
            params[2] = "";
        }
        reqParams.put("industry", params[2]);
        reqParams.put("company", params[3]);
        reqParams.put("title", params[4]);
        reqParams.put("name", params[5]);
        reqParams.put("page", params[6]);
        reqParams.put("size", params[7]);
        reqParams.put("adSId", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getAdSId());
        try {
            SearchRelationship mb = (SearchRelationship) HttpUtil.doHttpRequest(Constants.Http.SEARCH_RELATIONSHIP_SENIOR,
                    reqParams, SearchRelationship.class, mContext);
            return mb;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    interface IDataBack {

        void onPre();

        void onPost(List<Map<String, Object>> result);

        void onPostExtra(SearchRelationship result);

    }
}

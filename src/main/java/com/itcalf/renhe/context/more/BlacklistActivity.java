package com.itcalf.renhe.context.more;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.more.BlacklistTask.IDataBack;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class BlacklistActivity extends BaseActivity {
    private ListView mBlackList;
    private List<Map<String, Object>> mData;//用于接收返回数据
    private BlacklistAdapter mAdapter;
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private int index = -1;//记录选中的行
    private DataChangeReceiver mReceiver;
    private String sid;
    private LinearLayout noBlacklistLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.blacklist);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("黑名单"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("黑名单"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        mBlackList = (ListView) findViewById(R.id.black_list);
        noBlacklistLl = (LinearLayout) findViewById(R.id.no_data_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "黑名单");
        mData = new ArrayList<Map<String, Object>>();
        mAdapter = new BlacklistAdapter();
        mBlackList.setAdapter(mAdapter);
        if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                .getBoolean("fastscroll", false)) {
            mBlackList.setFastScrollEnabled(true);
        }

        fadeUitl = new FadeUitl(this, getResources().getString(R.string.loading));
        fadeUitl.addFade(rootRl);
        initSearch();
        mReceiver = new DataChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastAction.REMOVE_BLACK_LIST);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void initListener() {
        super.initListener();

        mBlackList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mData.size() && null != mData.get(position)) {
                    index = position;
                    Intent intent = new Intent(BlacklistActivity.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, (String) mData.get(position).get("sid"));
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[10]);
                    intent.putExtra("index", index);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        });

        mBlackList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                sid = (String) mData.get(position).get("sid");
                MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(BlacklistActivity.this);
                materialDialogsUtil.getBuilder(R.string.remove_from_blacklist).callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        index = position;
                        new RemoveBlackListTask(sid).executeOnExecutor(Executors.newCachedThreadPool(),
                                RenheApplication.getInstance().getUserInfo().getSid(),
                                RenheApplication.getInstance().getUserInfo().getAdSId(), sid);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                });
                materialDialogsUtil.show();
                return true;
            }
        });
    }

    private class BlacklistAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(BlacklistActivity.this).inflate(R.layout.blacklist_item, null);
                holder.headImage = (ImageView) convertView.findViewById(R.id.headImage);
                holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
                holder.titleTv = (TextView) convertView.findViewById(R.id.titleTv);
                holder.separatorTv = (TextView) convertView.findViewById(R.id.separatorTv);
                holder.infoTv = (TextView) convertView.findViewById(R.id.infoTv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String picPath = (String) mData.get(position).get("avatar_path");
            if (null != picPath && !"".equals(picPath)) {
                if (null != holder.headImage) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    try {
                        imageLoader.displayImage(picPath, holder.headImage, CacheManager.options,
                                CacheManager.animateFirstDisplayListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                holder.headImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
            }

            holder.nameTv.setText((String) mData.get(position).get("name"));
            String title = (String) mData.get(position).get("title");
            String company = (String) mData.get(position).get("company");

            holder.titleTv.setVisibility(View.VISIBLE);
            holder.separatorTv.setVisibility(View.VISIBLE);
            holder.infoTv.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(title)) {
                holder.titleTv.setVisibility(View.GONE);
                holder.separatorTv.setVisibility(View.GONE);
            } else {
                holder.titleTv.setText(title);
            }
            if (TextUtils.isEmpty(company)) {
                holder.infoTv.setVisibility(View.GONE);
            } else {
                holder.infoTv.setText(company);
            }

            return convertView;
        }

    }

    private class ViewHolder {
        ImageView headImage;
        TextView nameTv, titleTv, separatorTv, infoTv;
    }

    private void initSearch() {
        new BlacklistTask(this, new IDataBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void onPost(List<Map<String, Object>> result) {

                if (fadeUitl != null) {
                    fadeUitl.removeFade(rootRl);
                    fadeUitl = null;
                }
                if (null != result && result.size() > 0) {
                    noBlacklistLl.setVisibility(View.GONE);
                    mBlackList.setVisibility(View.VISIBLE);

                    if (mData != null) {
                        mData.clear();
                    }
                    mData.addAll(result);
                    mAdapter.notifyDataSetChanged();
                } else {
                    noBlacklistLl.setVisibility(View.VISIBLE);
                    mBlackList.setVisibility(View.GONE);
                }
            }

        }).executeOnExecutor(Executors.newCachedThreadPool());
    }

    private class RemoveBlackListTask extends AsyncTask<String, Void, MessageBoardOperation> {
        String sid;

        public RemoveBlackListTask(String sid) {
            this.sid = sid;
        }

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("blockedMemberSId", params[2]);
            try {
                MessageBoardOperation mbo = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.REMOVEBLACKLIST,
                        reqParams, MessageBoardOperation.class, BlacklistActivity.this);
                return mbo;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (null != result && result.getState() == 1) {
                dataChange(index);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HlContacts hlContacts = HlContactsUtils.getHlContactByTypeAndSid(HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE, sid);
                        if (null != hlContacts && hlContacts.getType() == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE) {
                            new ContactsUtil(BlacklistActivity.this).updateContactBlockState(hlContacts.getHlContactRenheMember().getImId(), 2, hlContacts.getHlContactRenheMember().getName(),
                                    hlContacts.getHlContactRenheMember().getUserface());
                        }
                    }
                }).start();

            } else {
                ToastUtil.showToast(BlacklistActivity.this, "发生未知错误");
            }
        }
    }

    private void dataChange(int position) {
        if (position != -1) {
            mData.remove(index);
            mAdapter.notifyDataSetChanged();
            if (mData.size() == 0) {
                noBlacklistLl.setVisibility(View.VISIBLE);
                mBlackList.setVisibility(View.GONE);
            }
        } else {
            initSearch();
        }

    }

    private class DataChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent.getStringExtra("sid")) {
                for (int i = 0; i < mData.size(); i++) {
                    if (mData.get(i).get("sid").toString().equals(intent.getStringExtra("sid"))) {
                        mData.remove(index);
                        mAdapter.notifyDataSetChanged();
                        if (mData.size() == 0) {
                            noBlacklistLl.setVisibility(View.VISIBLE);
                            mBlackList.setVisibility(View.GONE);
                        }
                        break;
                    }
                }
            } else {
                int position = intent.getIntExtra("index", -1);
                dataChange(position);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}

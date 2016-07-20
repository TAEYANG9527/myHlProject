package com.itcalf.renhe.context.wukong.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.CircleRequestAdapter;
import com.itcalf.renhe.bean.CircleJoinINfo;
import com.itcalf.renhe.bean.CircleJoinRequestListInfo;
import com.itcalf.renhe.bean.CircleJoinUserInfo;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.eventbusbean.RefreshChatUnreadEvent;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.MyListView;
import com.itcalf.renhe.view.ScrollBottomScrollView;
import com.itcalf.renhe.view.ScrollBottomScrollView.ScrollBottomListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;

public class ActivityCircleJoinRequest extends BaseActivity {
    private MyListView list;
    private Context context;
    private CircleRequestAdapter adapter;
    private ScrollBottomScrollView scroll;
    private MyBroadcastReciver myBroadcastReciver;
    private View footerView; // 底部加载更多

    private int numPage = 1; // 用户分页加载
    private boolean isMaxPageSize = false; // 是否已加载到最后一条数据
    private CircleJoinINfo circleJoinINfo;
    private String imConversationId;
    private String userConversationName;
    private ArrayList<MemberInfo> memberIdList = new ArrayList<MemberInfo>(); // 用于存储加入的人
    private ArrayList<CircleJoinRequestListInfo> array = new ArrayList<CircleJoinRequestListInfo>();
    private static final StringBuffer cricleJoinRequstStr = new StringBuffer(
            ",-4:您无权加载此数据，只有管理员才能查看,-3:会话Id为空,-2:服器内部异常,1:请求成功,");
    private StringBuffer approverCircleStr = new StringBuffer(",-11:该用户已是这个圈子的成员了,-10:已达圈子的人数上限,-9:圈子已满员,-8:您没有权限操作此申请记录,"
            + "-7:审批记录已被处理,-6:审批记录不能为空,-5:审批结果必须为 1审批通过 或2代表审批不通过,-4：审批结果不能为空,-3:申请加入圈子记录id不能为空,"
            + "-2:很抱歉，发生未知错误！,-1:很抱歉，您的权限不足！,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,1:请求成功,");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.activity_circle_join_request);
        setTextValue(1, "入圈申请");
        context = this;

        init();
    }

    private void init() {
        if (getIntent().getSerializableExtra("circleJoinINfo") != null) {
            circleJoinINfo = (CircleJoinINfo) getIntent().getSerializableExtra("circleJoinINfo");
            array = circleJoinINfo.getCircleJoinRequestList();
            isMaxPageSize = array.size() > 9 ? false : true;
        }
        userConversationName = getIntent().getStringExtra("userConversationName");
        imConversationId = getIntent().getStringExtra("imConversationId");

        myBroadcastReciver = new MyBroadcastReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("im.ActivityCircleJoinRequest");
        registerReceiver(myBroadcastReciver, intentFilter);

        scroll = (ScrollBottomScrollView) findViewById(R.id.scroll);
        scroll.setScrollBottomListener(scrollBottomListener);

        LinearLayout ly_scroll = (LinearLayout) findViewById(R.id.ly_scroll);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footerView = mInflater.inflate(R.layout.pull_to_refresh_foot, null);
        footerView.setClickable(false);
        footerView.setVisibility(View.GONE);
        ly_scroll.addView(footerView);

        list = (MyListView) findViewById(R.id.list);
        adapter = new CircleRequestAdapter(context, array, userConversationName, imConversationId);
        list.setAdapter(adapter);
        EventBus.getDefault().post(new RefreshChatUnreadEvent());//通知聊天界面刷新右上角未读数
    }

    ScrollBottomListener scrollBottomListener = new ScrollBottomListener() {
        @Override
        public void scrollBottom() {
            if (array != null && array.size() > 0 && footerView.getVisibility() == View.GONE && !isMaxPageSize) {
                numPage++;
                circleJoinRequest(numPage);
                footerView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", (Serializable) memberIdList);
        intent.putExtras(bundle);
        setResult(1, intent);
        finish();
        overridePendingTransition(0, R.anim.out_to_right);
        return super.onOptionsItemSelected(menu);
    }

    /**
     * 获取某个圈子所有申请记录
     */
    private void circleJoinRequest(final int num) {
        new AsyncTask<String, Void, CircleJoinINfo>() {
            @Override
            protected CircleJoinINfo doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().circleJoinRequset(params[0],
                            params[1], imConversationId, num, 10, ActivityCircleJoinRequest.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleJoinINfo result) {
                super.onPostExecute(result);
                footerView.setVisibility(View.GONE);
                if (result != null) {
                    if (result.getState() == 1) {
                        if (result.getCircleJoinRequestList().size() > 0) {
                            array.addAll(result.getCircleJoinRequestList());
                            adapter.notifyDataSetChanged();
                        }
                        isMaxPageSize = result.getCircleJoinRequestList().size() > 9 ? false : true;
                    } else {
                        ToastUtil.showToast(ActivityCircleJoinRequest.this, getResult(cricleJoinRequstStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ActivityCircleJoinRequest.this, "获取入圈申请：服器异常");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    public void approveCircleJoinRequest(final int state, final int requestId, final int pos) {
        RenheIMUtil.showProgressDialog(ActivityCircleJoinRequest.this, R.string.loading);
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand()
                            .approveCircleJoinRequest(params[0], params[1], requestId, state, ActivityCircleJoinRequest.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        if (state == 1) {
                            addMembers(pos);
                        } else {
                            RenheIMUtil.dismissProgressDialog();
                            array.get(pos).setApproveState(2);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleJoinRequest.this, getResult(approverCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleJoinRequest.this, "网络异常");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * IM添加成员
     */
    private void addMembers(final int pos) {
        final CircleJoinUserInfo member = array.get(pos).getMemberInfo();

        Message message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(userConversationName + "邀请 " + member.getName() + " 加入了圈子");
        IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> arg0) {
                MemberInfo memberInfo = new MemberInfo();
                memberInfo.setAvatar(member.getUserfaceUrl());
                memberInfo.setNickName(member.getName());
                memberInfo.setOpenId(member.getImId());
                memberInfo.setPinyin(PinyinUtil.cn2Spell(member.getName()));
                memberIdList.add(memberInfo);

                ToastUtil.showToast(ActivityCircleJoinRequest.this, "添加成功");
                RenheIMUtil.dismissProgressDialog();
                array.get(pos).setApproveState(1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {
            }

            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleJoinRequest.this, "加入圈子失败.code:" + arg0 + " reason:" + arg0, Toast.LENGTH_SHORT)
                        .show();
            }
        }, imConversationId + "", message, (long) member.getImId());
    }

    private String getResult(StringBuffer strs, Integer code) {
        int index = strs.indexOf(String.valueOf("," + code + ":"));
        if (index < 0) {
            return "state:" + code;
        }
        return strs.substring(index + 2 + String.valueOf(code).length(),
                strs.indexOf(",", index + 2 + String.valueOf(code).length()));
    }

    private class MyBroadcastReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("im.ActivityCircleJoinRequest")) {
                int pos = intent.getIntExtra("position", 0);
                if (intent.getIntExtra("ApproveState", 0) == 1) {
                    MemberInfo memberInfo2 = (MemberInfo) intent.getSerializableExtra("MemberInfo");
                    if (memberInfo2 != null)
                        memberIdList.add(memberInfo2);
                    array.get(pos).setApproveState(1);
                } else {
                    array.get(pos).setApproveState(2);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    ;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReciver);
    }
}
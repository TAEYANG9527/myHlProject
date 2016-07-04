package com.itcalf.renhe.context.wukong.im;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.CircleDetailMemberAdapter;
import com.itcalf.renhe.bean.CircleInfoByCircleId;
import com.itcalf.renhe.bean.CircleInfoByCircleId.CircleMember;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.CircleAvator;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 由分享链接进入的 本页面为圈子资料页面 可查看圈子成员
 */
public class ActivityCircleDetail extends BaseActivity {

    private TextView circleNumbTxt;
    private TextView circleNameTxt;
    private TextView circleNoticeTxt;
    private LinearLayout circleMembersLy;
    private TextView circleCountTxt;
    private GridView circleGridView;
    private LinearLayout circleBtn;
    private TextView circleBtnTxt;

    private CircleInfoByCircleId cinfo = null;
    private String imConversationId = "";// 圈子imId
    private String creatorMemberId = "";// 圈子创建者Id
    private String circleNumb = "", circleName = "", circleNotice = "";
    private int circleCount = 0, circleTotalCount = 0;
    private int joinType = 1;// 圈子加入类型：1 代表所有人可以加入；2 代表需要审批才可以加入；3 代表所有人都不可以加入
    private boolean isFull = false;// 是否满员
    private boolean isExit = false;// 是否已经是此圈子的成员
    private boolean isApply = false;// 是否已申请

    private CircleDetailMemberAdapter adapter;
    private List<MemberInfo> circleUserList = new ArrayList<MemberInfo>(); // 成员列表
    private List<CircleMember> circleMembers = new ArrayList<CircleMember>(); // 成员列表
    private CircleMember memberInfo = null;
    private Conversation conversation;// 获取当前的圈子会话

    private String circleId;
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private ScrollView scrollView;
    private TextView tv_join_type;
    private boolean isMaster;//是否是圈主
    private String userConversationName;//用户名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.search_circle_detail);
    }

    @Override
    protected void findView() {
        super.findView();
        circleNumbTxt = (TextView) findViewById(R.id.tx_circle_numb);//显示圈号
        circleNameTxt = (TextView) findViewById(R.id.tx_circle_name);//显示圈名
        circleNoticeTxt = (TextView) findViewById(R.id.tx_circle_notice);//显示公告
        circleMembersLy = (LinearLayout) findViewById(R.id.ly_add);//圈子成员
        circleCountTxt = (TextView) findViewById(R.id.tx_circle_count);//显示圈子成员

        circleGridView = (GridView) findViewById(R.id.circle_gridView);//成员图像列表
        circleBtn = (LinearLayout) findViewById(R.id.ly_circle_button);// 发起群聊 | 申请加入按钮
        circleBtnTxt = (TextView) findViewById(R.id.tx_circle_btn);

        rootRl = (RelativeLayout) findViewById(R.id.rootLl);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tv_join_type = (TextView) findViewById(R.id.tv_join_type);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(1, "圈子资料");
        scrollView.setVisibility(View.GONE);
        circleBtn.setVisibility(View.GONE);
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        circleId = getIntent().getExtras().getString("circleId");//圈子id
        new AsyncSearchCircleTask().executeOnExecutor(Executors.newCachedThreadPool(),
                RenheApplication.getInstance().getUserInfo().getSid(), RenheApplication.getInstance().getUserInfo().getAdSId(),
                circleId);
    }

    @Override
    protected void initListener() {
        super.initListener();
        circleMembersLy.setClickable(true);
        circleGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                circleMembersLy.performClick();
            }
        });
        //点击圈子成员按钮
        circleMembersLy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cinfo.isCircleExists()) {
                    if (isExit) {
                        Intent intent = new Intent(ActivityCircleDetail.this, ActivityCircleMemberTwo.class);
                        intent.putExtra("detail", "fromActivityDetail");//判断从圈子资料页面进入字段
                        intent.putExtra("jurisdiction", isMaster);//是否是圈主
                        intent.putExtra("isCircleMember", true);// 用户判断是否是通过圈子搜索进入
                        intent.putExtra("userConversationName", userConversationName);//用户名
                        intent.putExtra("imConversationId", imConversationId);//会话id
                        intent.putExtra("cirlcleName", circleName);//圈名
                        intent.putExtra("circleJoinType", String.valueOf(joinType));//圈子的加入类型
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        Intent intent = new Intent(ActivityCircleDetail.this, ActivityGrpcCircleMemberTwo.class);
                        intent.putExtra("imConversationId", imConversationId);//会话id
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                } else {
                    ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.cicle_get_error_not_exist));
                    finish();
                }
            }
        });
        //发起群聊  申请加入 按钮
        circleBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExit) {
                    if (conversation != null) {
                        Intent intent = new Intent(ActivityCircleDetail.this, ChatMainActivity.class);
                        intent.putExtra("conversation", conversation);
                        startActivity(intent);
                    } else {
                        ToastUtil.showToast(ActivityCircleDetail.this, "圈子存在异常");
                    }
                } else {
                    //判断，如果权限，所有人都可以加入，直接调加入接口。
                    if (joinType == 1) {
                        directJoinType();
                    } else {
                        Intent intent = new Intent(ActivityCircleDetail.this, ActivityApplyAddCircle.class);
                        intent.putExtra("imConversationId", imConversationId);
                        intent.putExtra("circleJoinType", cinfo.getJoinType() + "");
                        startActivityForResult(intent, 99);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            switch (resultCode) {
                case 11:
                    circleBtnStateChat();
                    break;
                case 22:
                    circleBtnStateCheck();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 申请之后回调---群聊
     */
    void circleBtnStateChat() {
        circleCountTxt.setText("" + (circleCount + 1) + "/" + circleTotalCount + "人");
        MemberInfo member = new MemberInfo();
        member.setAvatar(RenheApplication.getInstance().getUserInfo().getUserface());
        member.setNickName(RenheApplication.getInstance().getUserInfo().getName());
        member.setOpenId(RenheApplication.getInstance().getUserInfo().getImId());
        member.setPinyin(PinyinUtil.cn2Spell(RenheApplication.getInstance().getUserInfo().getName()));
        circleUserList.add(member);
        memberInfo = new CircleMember();
        memberInfo.setName(RenheApplication.getInstance().getUserInfo().getName());
        memberInfo.setUserface(RenheApplication.getInstance().getUserInfo().getUserface());
        circleMembers.add(memberInfo);
        adapter.notifyDataSetChanged();
        circleBtn.setEnabled(true);
        circleBtnTxt.setText("发起群聊");
        circleMembersLy.setClickable(true);
        isExit = true;
        // 生成新头像
        GenerateCircleAvator();
    }

    /**
     * 申请之后回调---等待验证
     */
    void circleBtnStateCheck() {
        circleBtn.setEnabled(false);
        circleBtnTxt.setTextColor(getResources().getColor(R.color.gray));
        circleBtnTxt.setText("等待验证");
    }

    /**
     * 获取当前的会话
     */
    private void getConversationInfo(final boolean isSuccess) {
        IMEngine.getIMService(ConversationService.class).getConversation(new Callback<Conversation>() {
            @Override
            public void onException(String arg0, String arg1) {
                Toast.makeText(ActivityCircleDetail.this, "获取会话失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation arg0, int arg1) {

            }

            @Override
            public void onSuccess(Conversation arg0) {
                conversation = arg0;
                if (null != conversation)
                    conversation.sync();
                if (isSuccess) {
                    Intent intent = new Intent(ActivityCircleDetail.this, ChatMainActivity.class);
                    intent.putExtra("conversation", conversation);
                    startActivity(intent);
                    finish();
                } else {
                    fadeUitl.removeFade(rootRl);
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
        }, imConversationId);
    }

    /**
     * 生成圈子头像
     */
    private void GenerateCircleAvator() {
        final int[] memberIds = new int[circleUserList.size()];
        for (int i = 0; i < circleUserList.size(); i++) {
            memberIds[i] = Integer.parseInt(String.valueOf(circleUserList.get(i).getOpenId()));
        }

        new AsyncTask<String, Void, CircleAvator>() {
            @Override
            protected CircleAvator doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().updateCircleavator(params[0],
                            params[1], memberIds, imConversationId, ActivityCircleDetail.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleAvator result) {
                super.onPostExecute(result);
                if (result != null && result.getState() == 1) {
                    updateIcon(result.getAvatar());
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 更新会话图标
     */
    private void updateIcon(String avatar) {
        Message message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(RenheApplication.getInstance().getUserInfo().getName() + "加入了圈子");
        if (conversation != null)
            conversation.updateIcon(avatar, message, new Callback<Void>() {
                @Override
                public void onException(String arg0, String arg1) {
                    // ToastUtil.showToast(ActivityCircleDetail.this,
                    // "头像生成失败！");
                }

                @Override
                public void onProgress(Void arg0, int arg1) {
                }

                @Override
                public void onSuccess(Void arg0) {

                }
            });
    }

    private void initView() {
        isMaster = cinfo.isCreator();//是否是圈主
        userConversationName = cinfo.getMemberList()[0].getName();//用户名
        imConversationId = cinfo.getImConversationId();//圈子会话id
        circleNumb = "" + cinfo.getId();//圈子id
        circleName = cinfo.getName();//圈名
        circleNotice = cinfo.getNote();//圈子公告
        if (circleNotice.isEmpty()) {
            circleNotice = "暂无公告";
        }

        tv_join_type.setText(ActivityCircleSetting.joinTypeStr[cinfo.getJoinType() - 1]);//显示加入方式

        circleCount = cinfo.getMemberCount();//圈子成员数量
        joinType = cinfo.getJoinType();//获取加入方式
        isFull = cinfo.isMemberCountAboveMax();//圈子是否已满员
        isExit = cinfo.isMemberExists();// 是否已经是该圈子成员
        isApply = cinfo.isRequestExists();// boolean 是否已申请加入
        circleTotalCount = cinfo.getMaxMemberCount();// int 圈子成员最大上限数
        circleNumbTxt.setText(circleNumb);
        circleNameTxt.setText(circleName);
        circleNoticeTxt.setText(circleNotice);
        circleCountTxt.setText("" + circleCount + "/" + circleTotalCount + "人");

        if (isExit) {
            circleBtn.setEnabled(true);
            circleBtnTxt.setText("发起群聊");
        } else if (isApply) {
            circleBtn.setEnabled(false);
            circleBtnTxt.setTextColor(getResources().getColor(R.color.gray));
            circleBtnTxt.setText("等待验证");
        } else if (joinType == 3 || isFull) {
            if (isFull && joinType != 3) {
                circleBtnTxt.setText("圈子满员");
            } else {
                circleBtnTxt.setText("不可加入");
            }
            circleBtnTxt.setTextColor(getResources().getColor(R.color.gray));
            circleBtn.setEnabled(false);
        } else {
            circleBtn.setEnabled(true);
            circleBtnTxt.setText("申请加入");
        }

        adapter = new CircleDetailMemberAdapter(this, circleMembers);
        circleGridView.setAdapter(adapter);

        CircleMember[] menbers = cinfo.getMemberList();
        circleUserList.clear();
        for (CircleMember menber : menbers) {
            circleMembers.add(menber);
        }
        adapter.notifyDataSetChanged();
        fadeUitl.removeFade(rootRl);
        scrollView.setVisibility(View.VISIBLE);
        circleBtn.setVisibility(View.VISIBLE);
        getConversationInfo(false);
    }

    /**
     * @功能说明 获取圈子的搜索列表
     */
    class AsyncSearchCircleTask extends AsyncTask<String, Void, CircleInfoByCircleId> {

        @Override
        protected CircleInfoByCircleId doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("circleId", params[2]);
            try {
                CircleInfoByCircleId mb = (CircleInfoByCircleId) HttpUtil
                        .doHttpRequest(Constants.Http.LOAD_CIRCLE_INFO_BY_CIRCLE_ID, reqParams, CircleInfoByCircleId.class, null);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(CircleInfoByCircleId result) {
            super.onPostExecute(result);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        if (result.isCircleExists()) {
                            cinfo = result;
                            initView();
                        } else {
                            ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.cicle_get_error_not_exist));
                            finish();
                        }
                        break;
                    default:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.cicle_get_error));
                        break;
                }
            } else {
                ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.cicle_get_error));
                finish();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 == NetworkUtil.hasNetworkConnection(ActivityCircleDetail.this)) {
                ToastUtil.showNetworkError(ActivityCircleDetail.this);
            }
        }
    }

    /*****************
     * 申请加圈子
     *****************/
    StringBuffer directJoinCircleStr = new StringBuffer(",1:请求成功，而且圈子为可直接加入的圈子,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,"
            + "-1 很抱歉，您的权限不足！:-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:圈子的成员数量已超过最大的限制，无法加入,"
            + "-5:您加入的圈子超过的可以加入圈子总数的限制,-6:您已经是这个圈子的成员了,-7:您要加入的圈子不存在,-8:圈子不能直接加入,");

    /**
     * 无需审批直接加入圈子
     */
    private void directJoinType() {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().directJoinCircle(params[0],
                            params[1], imConversationId, ActivityCircleDetail.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1)
                        applyToCircle();
                    else if (result.getState() == 2)
                        new AsyncApplyJoinCircleTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                RenheApplication.getInstance().getUserInfo().getSid(),
                                RenheApplication.getInstance().getUserInfo().getAdSId(), imConversationId, "");
                    else
                        ToastUtil.showToast(ActivityCircleDetail.this, getResult(directJoinCircleStr, result.getState()));
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 需审批加入圈子请求
     */
    class AsyncApplyJoinCircleTask extends AsyncTask<String, Void, MessageBoardOperation> {
        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("imConversationId", params[2]);
            reqParams.put("purpose", params[3]);
            try {
                MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.JOIN_CIRCLE, reqParams,
                        MessageBoardOperation.class, null);
                return mb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            fadeUitl.removeFade(rootRl);
            if (result != null) {
                switch (result.getState()) {
                    case 1:
                        // 本地添加成功后，调im添加成员
                        applyToCircle();
                        break;
                    case 2:
                        ToastUtil.showToast(ActivityCircleDetail.this, "您已发出申请，等待验证");
                        circleBtnStateCheck();
                        finish();
                        break;
                    case -1:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.sorry_of_privilege));
                        break;
                    case -2:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.sorry_of_unknow_exception));
                        break;
                    case -3:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "im的群聊会话id不能为空");
                        break;
                    case -4:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "圈子的成员数量已超过最大的限制，无法加入!");
                        break;
                    case -5:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "您加入的圈子超过的可以加入圈子总数的限制");
                        break;
                    case -6:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "您已经是这个圈子的成员了");
                        break;
                    case -7:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "您要加入的圈子不存在");
                        break;
                    case -8:
                        ToastUtil.showErrorToast(ActivityCircleDetail.this, "圈子为不可加入的状态，请于圈主联系");
                        break;
                    default:
                        break;
                }
            } else {
                ToastUtil.showErrorToast(ActivityCircleDetail.this, getString(R.string.connect_server_error));
                return;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (-1 == NetworkUtil.hasNetworkConnection(ActivityCircleDetail.this)) {
                ToastUtil.showNetworkError(ActivityCircleDetail.this);
            }
            fadeUitl.addFade(rootRl);
        }
    }

    /**
     * IM:加入圈子
     */
    private void applyToCircle() {
        Message message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(RenheApplication.getInstance().getUserInfo().getName() + "加入圈子");

        int myCId = RenheApplication.getInstance().getUserInfo().getImId();
        Long myConversationId = Long.parseLong(String.valueOf(myCId));

        IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {

            @Override
            public void onException(String arg0, String arg1) {
                ToastUtil.showErrorToast(ActivityCircleDetail.this, "IM申请加群失败：" + arg0 + ";reason:" + arg1);
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {

            }

            @Override
            public void onSuccess(List<Long> arg0) {
                sendJoinCircleInfo(imConversationId);
                ToastUtil.showToast(ActivityCircleDetail.this, "您已成功加入");
                getConversationInfo(true);
            }
        }, imConversationId, message, myConversationId);
    }

    /**
     * 加入悟空圈子成功后，告知服务端
     */
    private void sendJoinCircleInfo(final String imConversationId) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("imConversationId", imConversationId);
        OkHttpClientManager.postAsyn(Constants.Http.SEND_JOIN_CIRCLE_INFO, reqParams, MessageBoardOperation.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
            }

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(Object response) {
                if (null != response && response instanceof MessageBoardOperation) {
                    MessageBoardOperation result = (MessageBoardOperation) response;
                    if (result.getState() == 1) {
                        Logger.e("通知服务端加入圈子成功");
                    }
                }
            }

            @Override
            public void onAfter() {
                super.onAfter();
            }
        });
    }

    private String getResult(StringBuffer strs, Integer code) {
        int index = strs.indexOf(String.valueOf("," + code + ":"));
        if (index < 0) {
            return "state:" + code;
        }
        return strs.substring(index + 2 + String.valueOf(code).length(),
                strs.indexOf(",", index + 2 + String.valueOf(code).length()));
    }
}

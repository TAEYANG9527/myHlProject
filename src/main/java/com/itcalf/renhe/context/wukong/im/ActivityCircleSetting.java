package com.itcalf.renhe.context.wukong.im;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Member;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.CircleGridViewAdapter;
import com.itcalf.renhe.bean.CirCleInfo;
import com.itcalf.renhe.bean.CircleJoinINfo;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.context.more.TwoDimencodeActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.CircleAvator;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import cn.renhe.heliao.idl.collection.MyCollection;

public class ActivityCircleSetting extends BaseActivity implements View.OnClickListener {
    private CircleGridViewAdapter adapter;
    private TextView[] tx;
    private LinearLayout ly_circle_apply;
    RelativeLayout ly_recommend1;
    private TextView add_circle_member;
    private GridView gridView;

    private Conversation mConversation;
    private CircleJoinINfo circleJoinINfo; // 入圈申请
    private boolean isMaster = false; // 是否是管理员
    private MemberInfo memberInfo = new MemberInfo();
    private String userConversationName = "";// 用户名
    private String imConversationId = ""; // im群聊会话ID
    private Long imUserOpenId; // 用户openId;
    private int unReadCount;//未读加入圈子的申请数
    private static final int CIRCLE_JOINREQUSET_ACT = 2; // 申请界面
    private static final int CIRCLE_ADD_ACT = 3;// 添加成员界面
    private String[] circleDetail = new String[4]; // 存储圈子详情
    private List<HlContactRenheMember> mReceiverList = new ArrayList<>(); // 通讯录成员列表
    private List<MemberInfo> circleUserList = new ArrayList<MemberInfo>(); // 成员列表
    public static String[] joinTypeStr = {"所有人可以加入", "需要审批才可以加入", "所有人都不可以加入"};
    private final StringBuffer cricleJoinRequstStr = new StringBuffer(",-4:您无权加载此数据，只有管理员才能查看,-3:会话Id为空,-2:服器内部异常,1:请求成功,");
    private final StringBuffer deleteCircleStr = new StringBuffer(
            ",-7:您没有踢人权限,-6:圈主不能退出群,-5:被踢人不再此圈子中,-4:用户IM id不能为空,-3:会话Id为空,-2:服器内部错误,1:请求成功,");
    private final StringBuffer dissolveCircleStr = new StringBuffer(
            ",-4:您不是管理员，没有解散圈子的权限,-2:服器内部错误,-1:很抱歉，您的权限不足！,-3:会话Id为空,1:请求成功,");
    private final StringBuffer inviteCircleStr = new StringBuffer(
            ",1:请求成功,2:已发出加入邀请申请,-1:很抱歉，您的权限不足！,-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:被邀请的圈子成员的im openid数组不能为空,-5:被邀请的圈子成员的im openid数组数据中包含了还不是和聊会员的数据,-6:圈子的成员数量已超过最大的限制，无法加入,");
    private final StringBuffer invitationJoinCircleStr = new StringBuffer(
            ",1:请求成功，而且圈子为可直接加入的圈子,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,-1:很抱歉，您的权限不足！,-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:被邀请的圈子成员的im openid数组数组不能为空,-5:被邀请的圈子成员的im openid数组数据中包含了还不是和聊会员的数据,-6:您没有权限新增成员,-7:圈子的成员数量已超过最大的限制，无法加入,");
    private final StringBuffer circleQrcodeStr = new StringBuffer(",1:成功,-4:viewSId不能为空,-5:生成二维码出错,-6:圈子不存在,");
    private int circleMaxSize;
    private SharedPreferences sp;
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private ScrollView rootScrollView;
    private Member circleMasterMember;//圈主
    private String httpShortUrl;
    private String isNameExist_net;

    private SwitchCompat collectSwitchCompat;//收藏
    private int ID_TASK_ARCHIVE_MORE_ADDCOLLECT = TaskManager.getTaskId();//收藏
    private int ID_TASK_ARCHIVE_MORE_DELETECOLLECT = TaskManager.getTaskId();//取消收藏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        sp = RenheApplication.getInstance().getUserSharedPreferences();
        getTemplate().doInActivity(this, R.layout.activity_setting_circle);
        setTextValue(1, "圈子修改");

    }

    @Override
    protected void initData() {
        super.initData();

        tx = new TextView[7];
        tx[0] = (TextView) findViewById(R.id.tx_circle_name1);//显示圈名
        tx[1] = (TextView) findViewById(R.id.tx_circle_join1);//显示加入方式
        tx[2] = (TextView) findViewById(R.id.tx_circle_notice1);//显示公告
        tx[3] = (TextView) findViewById(R.id.tx_circle_count1);//显示成员人数
        tx[4] = (TextView) findViewById(R.id.tx_circle_dissolve1);
        tx[5] = (TextView) findViewById(R.id.tx_circle_join_request);
        tx[6] = (TextView) findViewById(R.id.tx_circleId);//圈号

        ly_circle_apply = (LinearLayout) findViewById(R.id.ly_circle_apply1);
        ly_recommend1 = (RelativeLayout) findViewById(R.id.ly_recommend1);
        add_circle_member = (TextView) findViewById(R.id.add_circle_member);
        gridView = (GridView) findViewById(R.id.gridView1);
        adapter = new CircleGridViewAdapter(this, circleUserList);
        gridView.setAdapter(adapter);
        SwitchCompat SwitchCompat = (SwitchCompat) findViewById(R.id.message_avoid_bother_toggle_button);
        collectSwitchCompat = (SwitchCompat) findViewById(R.id.message_collect_toggle_button);

        SwitchCompat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConversation == null) {
                    return;
                }
                if (isChecked) {
                    sp.edit().putBoolean(mConversation.conversationId() + "msg_void_bother", true).commit();
                } else {
                    sp.edit().putBoolean(mConversation.conversationId() + "msg_void_bother", false).commit();
                }
            }
        });
        collectSwitchCompat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addCollect();
                } else {
                    deleteCollect();
                }
            }
        });

        findViewById(R.id.ly_add1).setOnClickListener(this);
        findViewById(R.id.ly_circle_name1).setOnClickListener(this);
        findViewById(R.id.ly_circle_join1).setOnClickListener(this);
        findViewById(R.id.ly_circle_notice1).setOnClickListener(this);
        findViewById(R.id.ly_circle_apply1).setOnClickListener(this);
        findViewById(R.id.ly_circle_scan1).setOnClickListener(this);
        findViewById(R.id.ly_circle_dissolve1).setOnClickListener(this);
        ly_recommend1.setOnClickListener(this);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                findViewById(R.id.ly_add1).performClick();
            }
        });
        findViewById(R.id.ly_circle_dissolve1).setEnabled(false);
        findViewById(R.id.ly_circle_scan1).setVisibility(View.VISIBLE);

        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        rootScrollView = (ScrollView) findViewById(R.id.root_scroll_view);
        rootScrollView.setVisibility(View.GONE);
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        if (getIntent().getSerializableExtra("mConversation") != null) {
            isNameExist_net = getIntent().getStringExtra("isNameExist_net");//获取圈名确定状态
            mConversation = (Conversation) getIntent().getSerializableExtra("mConversation");// 获取悟空 Conversation
            imConversationId = getIntent().getStringExtra("imConversationId");// 获取会话id
            imUserOpenId = getIntent().getLongExtra("userOpenId", 0);//用户oppenid
            unReadCount = getIntent().getIntExtra("unReadCount", 0);//未读加入圈子的申请数
            getMemberInfo();
        } else {
            ToastUtil.showToast(ActivityCircleSetting.this, "IM会话对象为空");
            finish();
        }
        if (mConversation != null)
            SwitchCompat.setChecked(sp.getBoolean(mConversation.conversationId() + "msg_void_bother", false));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem addfriendsItem = menu.findItem(R.id.menu_save);
        addfriendsItem.setTitle("邀请朋友");
        addfriendsItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == R.id.menu_save) {
            addCircleMember();
        }
        return super.onOptionsItemSelected(menu);
    }

    private void addCircleMember() {
        if (isMaster || !tx[1].getText().toString().equals("所有人都不可以加入")) {
            Intent intent = new Intent(ActivityCircleSetting.this, ActivityCircleContacts.class);
            intent.putExtra("circleUserList", (ArrayList<MemberInfo>) circleUserList);
            Bundle bundle = new Bundle();
            ArrayList list = new ArrayList();
            list.add(mReceiverList);
            bundle.putParcelableArrayList("list", list);
            intent.putExtras(bundle);
            startActivityForResult(intent, CIRCLE_ADD_ACT);//添加成员
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        } else {
            ToastUtil.showToast(ActivityCircleSetting.this, "此圈子不允许其他人加入");
        }
    }

    /**
     * 获取某个圈子所有申请记录
     */
    private void circleJoinRequest() {
        new AsyncTask<String, Void, CircleJoinINfo>() {
            @Override
            protected CircleJoinINfo doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().circleJoinRequset(params[0],
                            params[1], imConversationId, 1, 10, ActivityCircleSetting.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleJoinINfo result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        circleJoinINfo = result;
                        if (result.getCircleJoinRequestList().size() > 0) {
                        }
                    } else {
                        ToastUtil.showToast(ActivityCircleSetting.this, getResult(cricleJoinRequstStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ActivityCircleSetting.this, "获取入圈申请：服器异常");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 管理员邀请人入圈子
     */
    private void invitationJoinCircle(final int[] invitationMember, final String name) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().invitationJoinCircle(params[0],
                            params[1], imConversationId, invitationMember, ActivityCircleSetting.this);
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
                        mReceiverList.clear();
                        RenheIMUtil.dismissProgressDialog();
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleSetting.this, getResult(invitationJoinCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleSetting.this, R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 普通成员邀请某人加入圈子
     */
    private void inviteCircle(final int[] invitationMember, final String name) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().inviteCircle(params[0],
                            params[1], imConversationId, invitationMember, ActivityCircleSetting.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                RenheIMUtil.dismissProgressDialog();
                if (result != null) {
                    if (result.getState() == 1) {
                        addMembers(invitationMember, name);
                    } else {
                        ToastUtil.showToast(ActivityCircleSetting.this, getResult(inviteCircleStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ActivityCircleSetting.this, R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * IM添加成员
     */
    private void addMembers(final int[] memberId, final String name) {
        final List<MemberInfo> array1 = new ArrayList<MemberInfo>();
        Long[] memberIds = new Long[memberId.length];
        for (int i = 0; i < memberId.length; i++) {
            for (int j = 0; j < mReceiverList.size(); j++) {
                if (memberId[i] == mReceiverList.get(j).getImId()) {
                    MemberInfo memberInfo = new MemberInfo();
                    memberInfo.setAvatar(mReceiverList.get(j).getUserface());
                    memberInfo.setNickName(mReceiverList.get(j).getName());
                    memberInfo.setOpenId(mReceiverList.get(j).getImId());
                    memberInfo.setPinyin(PinyinUtil.cn2Spell(mReceiverList.get(j).getName()));
                    array1.add(memberInfo);
                }
            }
            memberIds[i] = Long.parseLong(memberId[i] + "");
        }

        Message message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(userConversationName + "邀请 " + name + " 加入了圈子");
        IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> arg0) {
                circleUserList.addAll(array1);
                if (circleMaxSize > 0) {
                    tx[3].setText(circleUserList.size() + "/" + circleMaxSize);
                }
                adapter.notifyDataSetChanged();
                invitationJoinCircle(memberId, name);
                GenerateCircleAvator("update");
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {
            }

            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleSetting.this, "加入圈子失败.code:" + arg0 + " reason:" + arg0, Toast.LENGTH_SHORT).show();
            }
        }, imConversationId + "", message, memberIds);
    }

    /**
     * 获取圈子UserOpendId
     */
    private void getMemberInfo() {
        tx[0].setGravity(Gravity.RIGHT);
        tx[2].setGravity(Gravity.RIGHT);
        //判断是否确定圈名 否的话 默认为 未命名
        //  tx[0].setText(mConversation.title());
        String isNameExist = mConversation.extension("isNameExist");
        if (!TextUtils.isEmpty(isNameExist)) {
            if (mConversation.extension("isNameExist").equals("false")) {
                tx[0].setText(getResources().getString(R.string.circleName));
            } else {
                tx[0].setText(mConversation.title());
            }
        } else {
            if (!TextUtils.isEmpty(isNameExist_net) && isNameExist_net.equals("false")) {
                tx[0].setText(getResources().getString(R.string.circleName));
            } else {
                tx[0].setText(mConversation.title());
            }
        }
        tx[0].post(new Runnable() {
            @Override
            public void run() {
                if (tx[0].getLineCount() > 1) {
                    tx[0].setGravity(Gravity.LEFT);
                } else {
                    tx[0].setGravity(Gravity.RIGHT);
                }
            }
        });
        tx[1].setText((mConversation.extension("joinType") != null && mConversation.extension("joinType").length() == 1)
                ? joinTypeStr[Integer.parseInt(mConversation.extension("joinType")) - 1] : joinTypeStr[1]);
        tx[2].setText(mConversation.extension("note") != null ? mConversation.extension("note") : "");
        tx[2].post(new Runnable() {
            @Override
            public void run() {
                if (tx[2].getLineCount() > 1) {
                    tx[2].setGravity(Gravity.LEFT);
                } else {
                    tx[2].setGravity(Gravity.RIGHT);
                }
            }
        });

        circleDetail[0] = tx[0].getText().toString(); // 圈子标题
        circleDetail[1] = tx[1].getText().toString(); // 圈子加入类型
        circleDetail[2] = tx[2].getText().toString(); // 圈子公告
        circleDetail[3] = mConversation.extension("circleId") != null ? mConversation.extension("circleId") : ""; // 圈子Id
        tx[6].setText(circleDetail[3]);

        IMEngine.getIMService(ConversationService.class).listMembers(new Callback<List<Member>>() {
            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                switch (arg0) {
                    case Constants.IM_ERROR_CODE.ERR_CODE_NOT_GROUP_MEMBER:
                        ToastUtil.showErrorToast(ActivityCircleSetting.this, R.string.im_error_no_permission_circle);
                        finish();
                        break;
                    default:
                        Toast.makeText(ActivityCircleSetting.this, "加载会话成员失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }

            public void onProgress(List<Member> arg0, int arg1) {
            }

            public void onSuccess(List<Member> menbers) {
                circleUserList.add(new MemberInfo());
                LoadCircleInfo(menbers.size());
                for (Member menber : menbers) {
                    if (menber.roleType() == menber.roleType().MASTER) {
                        circleMasterMember = menber;
                        memberInfo = new MemberInfo(menber.user().openId(), menber.user().avatar(), menber.user().nickname(),
                                menber.user().nicknamePinyin());
                        memberInfo.setMaster(true);
                    }
                    if (menber.user().openId() == imUserOpenId) {
                        userConversationName = menber.user().nickname();
                        isMaster = (menber.roleType() == menber.roleType().MASTER);
                        if (isMaster) {
                            tx[4].setText("解散圈子");
                            add_circle_member.setText("添加成员");
                        } else {
                            tx[4].setText("退出圈子");
                            add_circle_member.setText("邀请成员");
                        }
                        ly_circle_apply.setVisibility(isMaster ? View.VISIBLE : View.GONE);
                    }
                    if (menber.user().openId() != memberInfo.getOpenId()) {
                        MemberInfo member = new MemberInfo();
                        member.setAvatar(menber.user().avatar());
                        member.setNickName(menber.user().nickname());
                        member.setOpenId(menber.user().openId());
                        member.setPinyin(menber.user().nicknamePinyin());
                        circleUserList.add(member);
                    }
                }
                circleUserList.set(0, memberInfo);
                if (isMaster)
                    circleJoinRequest();
                adapter.notifyDataSetChanged();
                findViewById(R.id.ly_circle_dissolve1).setEnabled(true);
            }

        }, imConversationId, 0, Integer.MAX_VALUE);
    }

    /**
     * 获取圈子信息
     */
    private void LoadCircleInfo(final int memberSize) {
        new AsyncTask<String, Void, CirCleInfo>() {
            @Override
            protected CirCleInfo doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().loadCircleInfo(params[0],
                            params[1], imConversationId, ActivityCircleSetting.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CirCleInfo result) {
                super.onPostExecute(result);

                if (result != null && result.getState() == 1) {
                    if (!result.isCircleExists()) {
                        ToastUtil.showToast(ActivityCircleSetting.this, "圈子不存在");
                        finish();
                    } else {
                        circleMaxSize = result.getMemberMaxCount();
                        tx[3].setText(memberSize + "/" + result.getMemberMaxCount());// 成员数
                        tx[1].setText(joinTypeStr[result.getJoinType() - 1]);
                        tx[2].setText(result.getNote());
                        fadeUitl.removeFade(rootRl);
                        rootScrollView.setVisibility(View.VISIBLE);
                        httpShortUrl = result.getHttpShortUrl();
                        if (result.isCollected()) {
                            collectSwitchCompat.setChecked(true);
                        } else {
                            collectSwitchCompat.setChecked(false);
                        }
                    }
                }
                RenheIMUtil.dismissProgressDialog();
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 移出会话成员
     */
    private void removeMembers(Long[] openIds, final String removeName) {
        Message message = null;
        message = IMEngine.getIMService(MessageBuilder.class).buildTextMessage(removeName + tx[0].getText().toString() + "圈子");

        IMEngine.getIMService(ConversationService.class).removeMembers(new Callback<List<Long>>() {
            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleSetting.this, "删除失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(List<Long> arg0, int arg1) {
            }

            @Override
            public void onSuccess(List<Long> arg0) {
                quitGroup(removeName);
            }
        }, imConversationId, message, openIds);

    }

    /**
     * 更新会话图标
     */
    private void updateIcon(String avatar) {
        Message message = null;
        // Message message =
        // IMEngine.getIMService(MessageBuilder.class).buildTextMessage(userConversationName
        // + "更新了圈子图标");
        mConversation.updateIcon(avatar, message, new Callback<Void>() {
            @Override
            public void onException(String arg0, String arg1) {
//                Toast.makeText(ActivityCircleSetting.this, "圈子头像更新失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT)
//                        .show();
            }

            @Override
            public void onProgress(Void arg0, int arg1) {
            }

            @Override
            public void onSuccess(Void arg0) {
            }
        });
    }

    /**
     * 退出会话
     */
    private void quitGroup(String removeName) {
        Message message = null;
        //成员退出或被移出圈子，不再发送全员提示消息
//		message = IMEngine.getIMService(MessageBuilder.class).buildTextMessage(removeName + tx[0].getText().toString() + "圈子");

        mConversation.quit(message, new Callback<Void>() {
            @Override
            public void onException(String arg0, String arg1) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCircleSetting.this, "退出圈子失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Void arg0, int arg1) {
            }

            @Override
            public void onSuccess(Void arg0) {
                RenheIMUtil.dismissProgressDialog();
                setResult(1);
                ActivityCircleSetting.this.finish();
            }
        });
    }

    /**
     * 生成圈子头像
     */
    private void GenerateCircleAvator(final String tag) {
        final int[] memberIds = new int[circleUserList.size()];
        for (int i = 0; i < circleUserList.size(); i++) {
            memberIds[i] = Integer.parseInt(String.valueOf(circleUserList.get(i).getOpenId()));
        }

        new AsyncTask<String, Void, CircleAvator>() {
            @Override
            protected CircleAvator doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().updateCircleavator(params[0],
                            params[1], memberIds, imConversationId, ActivityCircleSetting.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleAvator result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1)
                        updateIcon(result.getAvatar());
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 主动退出圈子
     */
    private void deleteCircle(final int[] imMemberId) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().deleteCircle(params[0],
                            params[1], imConversationId, imMemberId, ActivityCircleSetting.this);
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
                        quitGroup(userConversationName + "退出了");
                        GenerateCircleAvator("quit");
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleSetting.this, getResult(deleteCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleSetting.this, R.string.service_exception);
                }

            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 解散圈子
     */
    private void dissolveCircle() {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().dissolveCircle(params[0],
                            params[1], imConversationId, ActivityCircleSetting.this);
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
                        Long[] l = new Long[circleUserList.size()];
                        for (int i = 0; i < circleUserList.size(); i++) {
                            l[i] = circleUserList.get(i).getOpenId();
                        }
                        if (l.length > 1)
                            removeMembers(l, userConversationName + "解散了");
                        else
                            quitGroup(userConversationName + "解散了");
                    } else {
                        RenheIMUtil.dismissProgressDialog();
                        ToastUtil.showToast(ActivityCircleSetting.this, getResult(dissolveCircleStr, result.getState()));
                    }
                } else {
                    RenheIMUtil.dismissProgressDialog();
                    ToastUtil.showToast(ActivityCircleSetting.this, R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    /**
     * 跳转到圈子条件设置页面
     */
    private void cricleCompile(int state, String content) {
        if (isMaster) {
            //重新赋值
            circleDetail[0] = tx[0].getText().toString(); // 圈子标题
            circleDetail[1] = tx[1].getText().toString(); // 圈子加入类型
            circleDetail[2] = tx[2].getText().toString(); // 圈子公告
            circleDetail[3] = mConversation.extension("circleId") != null ? mConversation.extension("circleId") : ""; // 圈子Id
            Intent intent = new Intent(ActivityCircleSetting.this, ActivityCircleCompile.class);
            intent.putExtra("state", state);
            intent.putExtra("content", content);
            intent.putExtra("userConversationName", userConversationName);
            intent.putExtra("mConversation", mConversation);
            intent.putExtra("imConversationId", imConversationId);
            intent.putExtra("circleDetail", circleDetail);
            intent.putExtra("isUpdateCircle", isMaster);
            startActivityForResult(intent, 1);
        }
    }

    private String getResult(StringBuffer strs, Integer code) {
        int index = strs.indexOf(String.valueOf("," + code + ":"));
        if (index < 0) {
            return "state:" + code;
        }
        return strs.substring(index + 2 + String.valueOf(code).length(),
                strs.indexOf(",", index + 2 + String.valueOf(code).length()));
    }

    private int cricleJurisdiction() {
        if (tx[1].getText().toString().equals("所有人可以加入")) {
            return 1;
        } else if (tx[1].getText().toString().equals("需要审批才可以加入")) {
            return 2;
        } else if (tx[1].getText().toString().equals("所有人都不可以加入")) {
            return 3;
        }
        return 1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_circle_name1://进入圈名修改
                cricleCompile(0, tx[0].getText().toString());
                break;
            case R.id.ly_circle_join1:
                cricleCompile(1, tx[1].getText().toString());
                break;
            case R.id.ly_circle_notice1:
                cricleCompile(2, tx[2].getText().toString());
                break;
            case R.id.ly_add1://点击圈子成员按钮 查看圈子成员
                Intent intent = new Intent(ActivityCircleSetting.this, ActivityCircleMemberTwo.class);
                //			intent.putExtra("list", (ArrayList<MemberInfo>) circleUserList);
                intent.putExtra("jurisdiction", isMaster);//删除权限
                intent.putExtra("isCircleMember", true);// 用户判断是否是通过圈子搜索进入
                intent.putExtra("userConversationName", userConversationName);//用户名
                intent.putExtra("imConversationId", imConversationId);//圈子会话id
                intent.putExtra("cirlcleName", tx[0].getText().toString());//圈名
                intent.putExtra("circleJoinType", String.valueOf(cricleJurisdiction()));
                //			intent.putExtra("memberInfo", memberInfo);
                intent.putExtra("memberInfo", circleMasterMember);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.ly_circle_apply1:
                Intent intent2 = new Intent(ActivityCircleSetting.this, ActivityCircleJoinRequest.class);
                intent2.putExtra("imConversationId", imConversationId);
                intent2.putExtra("circleJoinINfo", circleJoinINfo);
                intent2.putExtra("userConversationName", userConversationName);
                startActivityForResult(intent2, CIRCLE_JOINREQUSET_ACT);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                unReadCount = 0;
                break;
            case R.id.ly_circle_scan1:
                if (circleDetail[3] != null && circleDetail[3].length() > 0
                        && NetworkUtil.hasNetworkConnection(ActivityCircleSetting.this) != -1) {
                    Intent intent3 = new Intent(ActivityCircleSetting.this, TwoDimencodeActivity.class);
                    intent3.putExtra("circleName", mConversation.title());
                    intent3.putExtra("circleId", circleDetail[3]);
                    intent3.putExtra("conversationId", mConversation.conversationId());
                    String circleDesp = mConversation.extension("note");
                    if (TextUtils.isEmpty(circleDesp)) {
                        if (null != circleUserList && circleUserList.size() > 2) {
                            circleDesp = circleUserList.get(0).getNickName() + "、" + circleUserList.get(1).getNickName() + "、"
                                    + circleUserList.get(2).getNickName() + "等" + circleUserList.size() + "位成员已在圈子中";
                        }
                    }
                    intent3.putExtra("circleDesp", circleDesp);
                    intent3.putExtra("circleCodeUrl", mConversation.icon());
                    intent3.putExtra("httpShortUrl", httpShortUrl);
                    startActivity(intent3);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else {
                    ToastUtil.showToast(ActivityCircleSetting.this,
                            NetworkUtil.hasNetworkConnection(ActivityCircleSetting.this) != -1 ? "圈子ID获取失败！" : "网络异常");
                }
                break;
            case R.id.ly_circle_dissolve1:
                if (NetworkUtil.hasNetworkConnection(ActivityCircleSetting.this) != -1) {
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(ActivityCircleSetting.this);
                    int textRes = (memberInfo.getOpenId() == imUserOpenId ? R.string.chat_circle_dissolve_tip
                            : R.string.chat_circle_quit_tip);
                    materialDialogsUtil.getBuilder(textRes).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            RenheIMUtil.showProgressDialog(ActivityCircleSetting.this, (memberInfo.getOpenId() == imUserOpenId ? R.string.chat_circle_dissolving_tip
                                    : R.string.chat_circle_quitting_tip));
                            if (memberInfo.getOpenId() == imUserOpenId) {
                                dissolveCircle();
                            } else {
                                int[] Umemberids = {Integer.parseInt(String.valueOf(imUserOpenId))};
                                deleteCircle(Umemberids);
                            }
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                        }
                    }).cancelable(false);
                    materialDialogsUtil.show();
                } else {
                    ToastUtil.showToast(ActivityCircleSetting.this, "网络异常");
                }
                break;
            case R.id.ly_recommend1:
                addCircleMember();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null)
            switch (requestCode) {
                case 1:
                    if (resultCode == 77) {
                        circleUserList.clear();
                        circleUserList.addAll((List<MemberInfo>) data.getSerializableExtra("contacts"));
                        if (circleMaxSize > 0) {
                            tx[3].setText(circleUserList.size() + "/" + circleMaxSize);
                        }
                        adapter.notifyDataSetChanged();
                        GenerateCircleAvator("update");
                    } else if (resultCode == 88) {//删除了圈子的其他成员，自动解散圈子
                        if (memberInfo.getOpenId() == imUserOpenId) {
                            dissolveCircle();
                        } else {
                            int[] Umemberids = {Integer.parseInt(String.valueOf(imUserOpenId))};
                            deleteCircle(Umemberids);
                        }
                    } else {
                        String circleName = data.getStringExtra("content"); //修改 获取修改后的圈名
                        if (!TextUtils.isEmpty(circleName)) {
                            if (!circleName.equals(getResources().getString(R.string.circleName))) {
                                mConversation.updateExtension("isNameExist", "true");
                            }
                            tx[resultCode].setText(circleName);
                        }
                        //tx[resultCode].setText(data.getStringExtra("content"));
                        if (tx[0].getLineCount() > 1) {
                            tx[0].setGravity(Gravity.LEFT);
                        } else {
                            tx[0].setGravity(Gravity.RIGHT);
                        }
                        if (tx[2].getLineCount() > 1) {
                            tx[2].setGravity(Gravity.LEFT);
                        } else {
                            tx[2].setGravity(Gravity.RIGHT);
                        }
                    }
                    break;
                case CIRCLE_JOINREQUSET_ACT:
                    ArrayList<MemberInfo> memberList = (ArrayList<MemberInfo>) data.getSerializableExtra("list");
                    if (memberList != null && memberList.size() > 0) {
                        circleUserList.addAll(memberList);
                        if (circleMaxSize > 0) {
                            tx[3].setText(circleUserList.size() + "/" + circleMaxSize);
                        }
                        adapter.notifyDataSetChanged();
                        GenerateCircleAvator("update");
                    }
                    break;
                case CIRCLE_ADD_ACT://添加成员
                    String name = "";
                    mReceiverList = (List<HlContactRenheMember>) data.getSerializableExtra("contacts");
                    if (mReceiverList.size() > 0) {
                        int[] invitationImMemberIds = new int[mReceiverList.size()];
                        for (int i = 0; i < mReceiverList.size(); i++) {
                            invitationImMemberIds[i] = mReceiverList.get(i).getImId();
                            if (i == mReceiverList.size() - 1) {
                                name = name + mReceiverList.get(i).getName();
                            } else {
                                name = name + mReceiverList.get(i).getName() + ",";
                            }
                        }
                        RenheIMUtil.showProgressDialog(ActivityCircleSetting.this, R.string.loading);
                        if (isMaster)
                            addMembers(invitationImMemberIds, name);
                        else
                            inviteCircle(invitationImMemberIds, name);
                    }
                    break;
                default:
                    break;
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMaster)
            circleJoinRequest();
        if (unReadCount > 0) {
            tx[5].setText(String.valueOf(unReadCount));
            tx[5].setVisibility(View.VISIBLE);
        } else {
            tx[5].setVisibility(View.GONE);
        }
    }

    private void addCollect() {
        if (TaskManager.getInstance().exist(ID_TASK_ARCHIVE_MORE_ADDCOLLECT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_ARCHIVE_MORE_ADDCOLLECT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.addCollect(ID_TASK_ARCHIVE_MORE_ADDCOLLECT, MyCollection.CollectResquest.CollectionType.QUANZI,
                circleDetail[3], circleDetail[3], 0);
    }

    private void deleteCollect() {
        if (TaskManager.getInstance().exist(ID_TASK_ARCHIVE_MORE_DELETECOLLECT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_ARCHIVE_MORE_DELETECOLLECT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.deleteCollect(ID_TASK_ARCHIVE_MORE_DELETECOLLECT, MyCollection.CollectResquest.CollectionType.QUANZI,
                circleDetail[3]);
    }
}

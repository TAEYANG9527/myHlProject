package com.itcalf.renhe.context.wukong.im;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
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
import com.itcalf.renhe.adapter.CreateCircleGridViewAdapter;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.db.CircleDao;
import com.itcalf.renhe.dto.CircleAvator;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.service.CreateCircleServise;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.RollProgressDialog;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class ActivityCreatCircle extends BaseActivity implements View.OnClickListener{
    private CircleDao dao;
    private CreateCircleGridViewAdapter adapter;
    private AlertDialog dialog;
    private TextView[] tx;
    private GridView gridView;

    private Conversation mConversation;
    private CircleAvator circleavator;
    private int[] memberIds = new int[0]; // 创建圈子头像时用于选中用户Id的数组
    private List<HlContactRenheMember> mReceiverList = new ArrayList<>(); // 通讯录成员列表
    private ArrayList<HlContactRenheMember> imgList = new ArrayList<>(); // 存储已添加成员的头像，姓名
    private static final StringBuffer createAvatorStr = new StringBuffer(",-3:请添加成员,-2:很抱歉，发生未知错误！,-1:很抱歉，您的权限不足！,1:请求成功,");
    //private static final StringBuffer createcircleStr = new StringBuffer(",-12:预生成的圈子头像不存在,-11:im的会话id对应的圈子已经存在,-9:圈子成员不能超过49人,-8:圈子成员为空,-7:圈子加入类型不能为空,-6:圈子名称已超过50字,-5:圈子名称不能为空,-4:圈子成员不能为空,-3:im会话Id不能为空,-2:很抱歉，发生未知错误！,-1:很抱歉，您的权限不足！,1:请求成功,");
    private static int titleLimited = TextSize.getInstance().getCircleTitleSize();//圈子名称字数限制
    private static int descriptionLimited = TextSize.getInstance().getCircleDescriptionSize();//圈子公告字数限制
    /*//初始化GrpcController 用于grpc调用
    private GrpcController controller;
    private int ID_TASK_TEST = TaskManager.getTaskId();*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (titleLimited == 0) {
            titleLimited = Constants.CIRCLETITLE;//圈子名称字数限制
        }
        if (descriptionLimited == 0) {
            descriptionLimited = Constants.CIRCLELIMITED;//圈子公告字数限制
        }
        getTemplate().doInActivity(this, R.layout.activity_creat_circle);
        setTextValue(1, "创建圈子");
        dao = new CircleDao(this);
        init();
    }

    private void init() {
        HlContactRenheMember contact = new HlContactRenheMember();//圈子成员信息
        contact.setUserface(((RenheApplication) getApplicationContext()).getUserInfo().getUserface());//圈子 成员图像
        contact.setName(RenheApplication.getInstance().getUserInfo().getName());// 圈子成员姓名
        contact.setTitle(RenheApplication.getInstance().getUserInfo().getTitle());// 圈子成员职务信息
        contact.setCompany(RenheApplication.getInstance().getUserInfo().getCompany());//圈子成员公司
        imgList.add(contact);

        tx = new TextView[4];
        tx[0] = (TextView) findViewById(R.id.tx_circle_name);//显示圈子名称
        tx[1] = (TextView) findViewById(R.id.tx_circle_join);//显示加入方式
        tx[2] = (TextView) findViewById(R.id.tx_circle_notice);//显示公告
        tx[3] = (TextView) findViewById(R.id.tx_circle_count);//显示圈子成员

        gridView = (GridView) findViewById(R.id.gridView);//成员列表
        adapter = new CreateCircleGridViewAdapter(imgList,this);
        gridView.setAdapter(adapter);

        findViewById(R.id.ly_add).setOnClickListener(this);//圈子成员
        findViewById(R.id.ly_circle_name).setOnClickListener(this);//圈子
        findViewById(R.id.ly_circle_join).setOnClickListener(this);//加入方式
        findViewById(R.id.ly_circle_notice).setOnClickListener(this);//公告
        findViewById(R.id.confirm_create).setOnClickListener(this);//确认创建
        findViewById(R.id.ly_recommend).setOnClickListener(this);//添加成员

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_circle_name://点击圈子按钮 修改圈子名称
                cricleCompile(0, tx[0].getText().toString());
                break;
            case R.id.ly_circle_join://点击加入方式按钮 选择加入方式
                cricleCompile(1, tx[1].getText().toString());
                break;
            case R.id.ly_circle_notice://点击公告按钮 填写公告
                cricleCompile(2, tx[2].getText().toString());
                break;
            case R.id.ly_recommend://点击圈子成员按钮 进入选择联系人页面
            case R.id.ly_add://点击添加成员按钮 进入选择联系人页面
                Intent intent = new Intent(ActivityCreatCircle.this, ActivityCircleContacts.class);
                Bundle bundle = new Bundle();
                ArrayList list = new ArrayList();
                list.add(mReceiverList);
                bundle.putParcelableArrayList("list", list);
                intent.putExtras(bundle);
                startHlActivityForResult(intent, 1);
                break;
            case R.id.confirm_create://点击确认创建按钮
                createGroup();
                break;
            default:
                break;
        }
    }

    /**
     * 创建圈子
     */
    private void createGroup() {
        if (NetworkUtil.hasNetworkConnection(ActivityCreatCircle.this) != -1) {
            if (tx[1].getText().toString().length() > 0) {
                if (memberIds.length > 1) {
                    if (tx[0].getText().toString().length() <= titleLimited
                            && tx[2].getText().toString().length() <= descriptionLimited) {
                        GenerateCircleAvator();
                    }else {
                        ToastUtil.showToast(ActivityCreatCircle.this, tx[0].getText().length() > titleLimited
                                ? getString(R.string.circleNameLength) + titleLimited + getString(R.string.character) : getString(R.string.circleNoticeLength) + descriptionLimited + getString(R.string.character));
                    }
                } else {
                    ToastUtil.showToast(ActivityCreatCircle.this, getString(R.string.pleaseAddCircleMember));
                }
            }else {
                ToastUtil.showToast(ActivityCreatCircle.this, getString(R.string.setJoinMethod));
            }
        }else {
            ToastUtil.showToast(ActivityCreatCircle.this, getString(R.string.net_error));
        }
    }

    /**
     * 生成圈子头像
     */
    private void GenerateCircleAvator() {
        showDialog(getString(R.string.createCircleHeadIcon));
        new AsyncTask<String, Void, CircleAvator>() {
            @Override
            protected CircleAvator doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().generateCircleAvator(params[0],
                            params[1], memberIds, ActivityCreatCircle.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleAvator result) {
                super.onPostExecute(result);
                dismissDialog();
                if (result != null) {
                    if (result.getState() == 1) {
                        circleavator = result;
                        createConversation();
                    } else {
                        ToastUtil.showToast(ActivityCreatCircle.this, getResult(createAvatorStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ActivityCreatCircle.this, NetworkUtil.hasNetworkConnection(ActivityCreatCircle.this) != -1
                            ? R.string.service_exception : R.string.net_error);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    private void createConversation() {
        RenheIMUtil.showProgressDialog(this, R.string.circle_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        StringBuffer title = new StringBuffer();
        String circleName = tx[0].getText().toString();
        //判断圈名是否为空
        if (TextUtils.isEmpty(circleName)) {
            StringBuilder circleNameSb = new StringBuilder();
            if (imgList.size() >= 3) {
                circleNameSb.append(imgList.get(0).getName() + "、" + imgList.get(1).getName() + "、" + imgList.get(2).getName());
                if (imgList.size() > 3) {
                    circleNameSb.append("等");
                }
            }else if(imgList.size() == 2){
                circleNameSb.append(imgList.get(0).getName() + "、" + imgList.get(1).getName());
            }
            circleName = circleNameSb.toString();
        }
        //和聊页面列表 item上生成圈子的title
        title.append(circleName);//群聊名称（单聊设置不会生效，只支持群聊）
        Message message = null;//参考创建消息
        message = IMEngine.getIMService(MessageBuilder.class).buildTextMessage(
                ((RenheApplication) getApplicationContext()).getUserInfo().getName() + "创建了" + circleName + "圈子");
        int convType = Conversation.ConversationType.GROUP; // 会话类型：群聊
        HashMap<String, String> map = new HashMap<String, String>();//用于标志客户自定义信息
        map.put("circleId", String.valueOf(circleavator.getCircleId()));//获取圈子的id
        map.put("note", tx[2].getText().toString());//获取公告显示内容
        map.put("joinType", cricleJurisdiction() + "");//获取加入方式类型，默认是所有人可以加入
        map.put(Constants.ISCIRCLEMASTER, String.valueOf(RenheApplication.getInstance().currentOpenId));//存储圈主
        final String circleNameFinal = circleName;
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                Intent intent = new Intent(ActivityCreatCircle.this, ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                //判断圈名是否为空
                if (TextUtils.isEmpty(tx[0].getText().toString())) {
                    conversation.updateExtension("isNameExist", "false");//false 表明创建圈子时未确定圈名为false状态
                    intent.putExtra("isNameExist_net","false");
                } else {
                    conversation.updateExtension("isNameExist", "true");
                    intent.putExtra("isNameExist_net","true");
                }
                RenheIMUtil.dismissProgressDialog();
                insetDataBase(conversation.conversationId(), circleNameFinal);
                startService(new Intent(ActivityCreatCircle.this, CreateCircleServise.class));
                //创建圈子成功后直接跳转到群聊页面
                startActivity(intent);
                ActivityCreatCircle.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();
            }

            @Override
            public void onException(String code, String reason) {
                RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ActivityCreatCircle.this, getString(R.string.circle_create_failed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), circleavator.getAvatar(), message, convType, 0, map, intParseLong());

        /**
         * 1 title 群聊名称（单聊设置不会生效，只支持群聊）
         * 2 icon 群聊头像（单聊设置不会生效，只支持群聊）
         * 3 message 消息（参考创建消息）
         * 4 type 设置单聊群聊类型（参考会话类型）
         * 5................
         * 6 tag extension 可选参数，用于标志客户自定义信息
         * 7 uids 聊天中其他人的openid
         */
        //createCircleRequest(title.toString(), circleavator.getAvatar(), message, convType, 0, map, intParseLong());
    }

    /**
     * 使用grpc 创建圈子
     * @param title
     * @param avatar
     * @param message
     * @param convType
     * @param i
     * @param map
     * @param l
     *//*
    private void createCircleRequest(String title, String avatar, Message message, int convType, int i, Map map, Long[] l) {
        if (TaskManager.getInstance().exist(ID_TASK_TEST)) {
            return;
        }
        if (null == grpcController)
            grpcController = new GrpcController();
        TaskManager.getInstance().addTask(this, ID_TASK_TEST);
        grpcController.createCircleRequest(ID_TASK_TEST, title, avatar, message, convType, i, map, l);
    }*/

    /**
     *利用Grpc访问服务端 获取结果的回调方法
     */

   /* @Override
    public void onSuccess(Object result) {
        RenheIMUtil.dismissProgressDialog();

    }

    @Override
    public void onException(String s, String s1) {
        RenheIMUtil.dismissProgressDialog();
        Toast.makeText(ActivityCreatCircle.this, getString(R.string.circle_create_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgress(Object o, int i) {

    }*/

    private void insetDataBase(String imConversationId, String circleName) {
        boolean tag = dao.isLocal(circleavator.getCircleId() + "");
        dao.insertCircleInfo(((RenheApplication) getApplicationContext()).getUserInfo().getSid(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(), circleavator.getCircleId() + "",
                imConversationId, circleavator.getPreAvatarId() + "", circleName, cricleJurisdiction() + "",
                tx[2].getText().toString(), getimMemberIds(), tag, true);
    }

    private Long[] intParseLong() {
        Long[] l = new Long[memberIds.length];
        for (int i = 0; i < memberIds.length; i++) {
            l[i] = Long.parseLong(memberIds[i] + "");
        }
        return l;
    }

    private int cricleJurisdiction() {
        if (tx[1].getText().toString().equals("所有人都可以加入")) {
            return 1;
        } else if (tx[1].getText().toString().equals("需要审批才可以加入")) {
            return 2;
        } else if (tx[1].getText().toString().equals("所有人都不可以加入")) {
            return 3;
        }
        return 1;
    }

    private String getimMemberIds() {
        String s = "";
        for (int i = 0; i < memberIds.length; i++) {
            s = s + memberIds[i] + ",";
        }
        return s;
    }

    /**
     *进入圈子, 加入方式, 公告 页面
     */
    private void cricleCompile(int state, String content) {
        Intent intent = new Intent(ActivityCreatCircle.this, ActivityCircleCompile.class);
        intent.putExtra("state", state);//表示是 圈子 加入方式 公告 状态
        intent.putExtra("content", content); //各状态的内容
        intent.putExtra("imConversationId", ""); //会话id
        intent.putExtra("userConversationName", "");//用户名
        intent.putExtra("mConversation", mConversation);
        intent.putExtra("circleDetail", new String[0]);
        intent.putExtra("isUpdateCircle", false);//是否更新圈子信息
        startHlActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null)
            if (resultCode != 3) {
                //获取圈子名称 , 加入方式，公告
                tx[resultCode].setText(data.getStringExtra("content"));
                String s = data.getStringExtra("content");
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

            } else {
                if (imgList.size() > 0) {
                    imgList.clear();
                }
                HlContactRenheMember contact = new HlContactRenheMember();
                contact.setUserface(((RenheApplication) getApplicationContext()).getUserInfo().getUserface());//成员 头像
                contact.setName(RenheApplication.getInstance().getUserInfo().getName());//成员 姓名
                contact.setTitle(RenheApplication.getInstance().getUserInfo().getTitle());//成员 职务
                contact.setCompany(RenheApplication.getInstance().getUserInfo().getCompany());//成员 公司
                imgList.add(contact);
                mReceiverList = (List<HlContactRenheMember>) data.getSerializableExtra("contacts");
                memberIds = new int[((RenheApplication) getApplicationContext()).getUserInfo().getImId() > 0
                        ? mReceiverList.size() + 1 : mReceiverList.size()];
                for (int i = 0; i < mReceiverList.size(); i++) {
                    imgList.add(mReceiverList.get(i));
                    memberIds[i] = mReceiverList.get(i).getImId();
                }
                if (((RenheApplication) getApplicationContext()).getUserInfo().getImId() > 0)
                    memberIds[mReceiverList.size()] = ((RenheApplication) getApplicationContext()).getUserInfo().getImId();

                tx[3].setText(memberIds.length + "/50");
                adapter.notifyDataSetChanged();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getResult(StringBuffer strs, Integer code) {
        int index = strs.indexOf(String.valueOf("," + code + ":"));
        if (index < 0) {
            return "state:" + code;
        }
        return strs.substring(index + 2 + String.valueOf(code).length(),
                strs.indexOf(",", index + 2 + String.valueOf(code).length()));
    }

    private void showDialog(String content) {
        dismissDialog();
        dialog = RollProgressDialog.showNetDialog(ActivityCreatCircle.this, true, content);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

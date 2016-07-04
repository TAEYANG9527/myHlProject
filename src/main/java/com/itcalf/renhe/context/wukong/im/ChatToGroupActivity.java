package com.itcalf.renhe.context.wukong.im;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.db.CircleDao;
import com.itcalf.renhe.dto.CircleAvator;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.service.CreateCircleServise;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.RollProgressDialog;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatToGroupActivity extends BaseActivity implements View.OnClickListener {
    private CircleDao dao;
    private Adapter adapter;
    private AlertDialog dialog;
    private GridView gridView;

    private Conversation mConversation;
    private CircleAvator circleavator;
    private int[] memberIds = new int[0]; // 创建圈子头像时用于选中用户Id的数组
    private List<HlContactRenheMember> mReceiverList = new ArrayList<>(); // 通讯录成员列表
    private ArrayList<String> imgList = new ArrayList<String>(); // 存储已添加成员的头像
    private static final StringBuffer createAvatorStr = new StringBuffer(",-3:请添加成员,-2:很抱歉，发生未知错误！,-1:权限不足,1:请求成功,");
    private static final StringBuffer createcircleStr = new StringBuffer(
            ",-12:预生成的圈子头像不存在,-11:im的会话id对应的圈子已经存在,-9:圈子成员不能超过49人,-8:圈子成员为空,-7:圈子加入类型不能为空,-6:圈子名称已超过50字,-5:圈子名称不能为空,-4:圈子成员不能为空,-3:im会话Id不能为空,-2:很抱歉，发生未知错误！,-1:很抱歉，您的权限不足！,1:请求成功,");

    private static final String DEFAULT_GROUP_NAME = "圈子";
    private MenuItem addfriendsItem;

    private ArrayList<MemberInfo> circleUserList;
    private String otherName;
    private String otherFace;
    private String otherOpenId;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.chat_to_group);
        setTextValue(1, "设置");
        dao = new CircleDao(this);
        sp = RenheApplication.getInstance().getUserSharedPreferences();
        init();
    }

    private void init() {
        imgList.add(((RenheApplication) getApplicationContext()).getUserInfo().getUserface());
        if (null != getIntent().getSerializableExtra("conversation")) {
            mConversation = (Conversation) getIntent().getSerializableExtra("conversation");
        }
        otherName = getIntent().getStringExtra("name");
        otherFace = getIntent().getStringExtra("face");
        otherOpenId = getIntent().getStringExtra("openId");
        circleUserList = new ArrayList<MemberInfo>();
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setAvatar(otherFace);
        memberInfo.setNickName(otherName);
        memberInfo.setOpenId(Long.parseLong(otherOpenId));
        memberInfo.setPinyin(PinyinUtil.cn2Spell(otherName));
        circleUserList.add(memberInfo);

        imgList.add(otherFace);
        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new Adapter(this, circleUserList);
        gridView.setAdapter(adapter);

        findViewById(R.id.ly_recommend).setOnClickListener(this);
        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.message_avoid_bother_toggle_button);
        if (mConversation != null)
            switchCompat.setChecked(sp.getBoolean(mConversation.conversationId() + "msg_void_bother", false));
        switchCompat.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConversation == null) {
                    return;
                }
                if (isChecked) {
                    sp.edit().putBoolean(mConversation.conversationId() + "msg_void_bother", true).commit();
                    mConversation.updateNotification(false, new Callback<Void>() {

                        @Override
                        public void onException(String arg0, String arg1) {
                        }

                        @Override
                        public void onProgress(Void arg0, int arg1) {

                        }

                        @Override
                        public void onSuccess(Void arg0) {
                        }

                    });
                } else {
                    mConversation.updateNotification(true, new Callback<Void>() {

                        @Override
                        public void onException(String arg0, String arg1) {
                        }

                        @Override
                        public void onProgress(Void arg0, int arg1) {

                        }

                        @Override
                        public void onSuccess(Void arg0) {
                        }

                    });
                    sp.edit().putBoolean(mConversation.conversationId() + "msg_void_bother", false).commit();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == R.id.menu_save) {
//            if (NetworkUtil.hasNetworkConnection(ChatToGroupActivity.this) != -1)
//                GenerateCircleAvator();
//            else
//                ToastUtil.showToast(ChatToGroupActivity.this, "网络异常");
        }
        return super.onOptionsItemSelected(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        addfriendsItem = menu.findItem(R.id.menu_save);
        addfriendsItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 生成圈子头像
     */
    private void GenerateCircleAvator(final String circleName) {
        showDialog("生成圈子头像中...");
        new AsyncTask<String, Void, CircleAvator>() {
            @Override
            protected CircleAvator doInBackground(String... params) {
                try {
                    return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().generateCircleAvator(params[0],
                            params[1], memberIds, ChatToGroupActivity.this);
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
                        createConversation(circleName);
                    } else {
                        ToastUtil.showToast(ChatToGroupActivity.this, getResult(createAvatorStr, result.getState()));
                    }
                } else {
                    ToastUtil.showToast(ChatToGroupActivity.this, NetworkUtil.hasNetworkConnection(ChatToGroupActivity.this) != -1
                            ? R.string.net_error : R.string.service_exception);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid());
    }

    private void createConversation(String circleName) {
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(this, R.string.circle_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        if (TextUtils.isEmpty(circleName)) {
            circleName = DEFAULT_GROUP_NAME;
        }
        Message message = null;
        message = IMEngine.getIMService(MessageBuilder.class)
                .buildTextMessage(((RenheApplication) getApplicationContext()).getUserInfo().getName() + "创建了圈子");
        int convType = Conversation.ConversationType.GROUP; // 会话类型：群聊
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("circleId", String.valueOf(circleavator.getCircleId()));
        map.put("note", "");
        map.put("joinType", cricleJurisdiction() + "");
        map.put(Constants.ISCIRCLEMASTER, String.valueOf(RenheApplication.getInstance().currentOpenId));

        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                insetDataBase(conversation.conversationId());
                startService(new Intent(ChatToGroupActivity.this, CreateCircleServise.class));
//                sendBroadcast(new Intent(ChatActivity.FINISH_CHAT_ACTION));
                Intent intent = new Intent(ChatToGroupActivity.this, ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
//                startActivity(intent);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ChatToGroupActivity.this, "圈子创建失败,清稍后重试", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, circleName, circleavator.getAvatar(), message, convType, 0, map, intParseLong());
    }

    private void insetDataBase(String imConversationId) {
        boolean tag = dao.isLocal(circleavator.getCircleId() + "");
        dao.insertCircleInfo(((RenheApplication) getApplicationContext()).getUserInfo().getSid(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(), circleavator.getCircleId() + "",
                imConversationId, circleavator.getPreAvatarId() + "", DEFAULT_GROUP_NAME, cricleJurisdiction() + "", "",
                getimMemberIds(), tag, false);
    }

    private Long[] intParseLong() {
        Long[] l = new Long[memberIds.length];
        for (int i = 0; i < memberIds.length; i++) {
            l[i] = Long.parseLong(memberIds[i] + "");
        }
        return l;
    }

    private int cricleJurisdiction() {
        return 1;
    }

    private String getimMemberIds() {
        String s = "";
        for (int i = 0; i < memberIds.length; i++) {
            s = s + memberIds[i] + ",";
        }
        return s;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ly_recommend:
                Intent intent = new Intent(ChatToGroupActivity.this, ActivityCircleContacts.class);
                intent.putExtra("circleUserList", circleUserList);
                Bundle bundle = new Bundle();
                ArrayList list = new ArrayList();
                list.add(mReceiverList);
                bundle.putParcelableArrayList("list", list);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null)
            if (resultCode == 3) {
                imgList = new ArrayList<String>();
                imgList.add(((RenheApplication) getApplicationContext()).getUserInfo().getUserface());
                imgList.add(otherFace);
                mReceiverList = (List<HlContactRenheMember>) data.getSerializableExtra("contacts");
                memberIds = new int[((RenheApplication) getApplicationContext()).getUserInfo().getImId() > 0
                        ? mReceiverList.size() + 2 : mReceiverList.size()];
                for (int i = 0; i < mReceiverList.size(); i++) {
                    imgList.add(mReceiverList.get(i).getUserface());
                    memberIds[i] = mReceiverList.get(i).getImId();
                }
                if (((RenheApplication) getApplicationContext()).getUserInfo().getImId() > 0) {
                    memberIds[mReceiverList.size()] = Integer.parseInt(otherOpenId);
                    memberIds[mReceiverList.size() + 1] = ((RenheApplication) getApplicationContext()).getUserInfo().getImId();
                }
                for (HlContactRenheMember contact : mReceiverList) {
                    if (!isExist(contact.getImId())) {
                        MemberInfo memberInfo = new MemberInfo();
                        memberInfo.setAvatar(contact.getUserface());
                        memberInfo.setNickName(contact.getName());
                        memberInfo.setOpenId(contact.getImId());
                        memberInfo.setPinyin(contact.getFullPinYin());
                        circleUserList.add(memberInfo);
                    }
                }
                adapter.notifyDataSetChanged();
                if (circleUserList.size() >= 2) {
                    String hostName = RenheApplication.getInstance().getUserInfo().getName();
                    StringBuilder circleName = new StringBuilder();
                    if (circleUserList.size() > 2) {
                        circleName.append(hostName + "、" + circleUserList.get(0).getNickName() + "、" + circleUserList.get(1).getNickName() + "等");
                    } else if (circleUserList.size() == 2) {
                        circleName.append(hostName + "、" + circleUserList.get(0).getNickName() + "、" + circleUserList.get(1).getNickName());
                    }
                    GenerateCircleAvator(circleName.toString());
                }
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
        dialog = RollProgressDialog.showNetDialog(ChatToGroupActivity.this, true, content);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    class Adapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<MemberInfo> circleUserList;

        public Adapter(Context context, ArrayList<MemberInfo> circleUserList) {
            this.mContext = context;
            this.circleUserList = circleUserList;
        }

        @Override
        public int getCount() {
            return circleUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return circleUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = LayoutInflater.from(this.mContext).inflate(R.layout.activtity_creat_circle_list_item, null, false);
                vh.iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
                vh.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            MemberInfo memberInfo = circleUserList.get(position);
            if (null != memberInfo) {
                ImageLoader.getInstance().displayImage(memberInfo.getAvatar(), vh.iv_img);
                if (!TextUtils.isEmpty(memberInfo.getNickName())) {
                    vh.nameTv.setVisibility(View.VISIBLE);
                    vh.nameTv.setText(memberInfo.getNickName());
                    if (memberInfo.getAvatar().equals(RenheApplication.getInstance().getUserInfo().getUserface())) {
                        vh.nameTv.setTextColor(getResources().getColor(R.color.CF));
                    } else {
                        vh.nameTv.setTextColor(getResources().getColor(R.color.black));
                    }
                } else {
                    vh.nameTv.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        class ViewHolder {
            private ImageView iv_img;
            private TextView nameTv;
        }
    }

    private boolean isExist(long openId) {
        boolean isExist = false;
        for (MemberInfo memberInfo : circleUserList) {
            if (memberInfo.getOpenId() == openId) {
                isExist = true;
            }
        }
        return isExist;
    }
}

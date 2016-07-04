package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.MessageBuilder;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.ConversationShareType;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.MyCircleListAdapter;
import com.itcalf.renhe.bean.CircleBean;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ActivityMyCircleJoinReuset;
import com.itcalf.renhe.context.wukong.im.ChatMainActivity;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DialogUtil;
import com.itcalf.renhe.utils.DialogUtil.ShareMyDialogClickListener;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chan
 * @createtime 2015-1-19
 * @功能说明 我的圈子列表
 */
public class MyCircleAct extends BaseActivity {

    private LinearLayout searchLl;
    private EditText keywordEdt;
    private ListView mycircleList;
    private MyCircleListAdapter circleAdapter;
    private List<CircleBean> circlelist;
    private List<CircleBean> searchList;// 存值
    private Handler mHandler;

    private DialogUtil dialogUtil;
    private String toForwardContent;
    private String toFrowardPic;
    private String toFrowardTitle;
    /**
     * 消息构造器
     */
    private MessageBuilder mMessageBuilder;
    private boolean isToShare = false;
    private Conversation mConversation;
    private String toForwardObjectId;
    private int shareType;

    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private String contentTitle;
    private String contentOther;
    private ArrayList<String> shareFilePathList;
    private ArrayList<String> shareImagePathList;
    /**
     * 消息中图片或语音的本地路径
     */
    private String mLocalUrl;
    private String mFileType;
    private Uri sendPicUri;
    private com.alibaba.wukong.im.Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRenheApplication().addActivity(this);
        getTemplate().doInActivity(this, R.layout.mycirclelist);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        searchLl = (LinearLayout) findViewById(R.id.search_Ll);
        keywordEdt = (EditText) findViewById(R.id.keyword_edt);
        mycircleList = (ListView) findViewById(R.id.mycircle_list);
    }

    @Override
    protected void initData() {
        super.initData();
        //		setTitle("圈子列表");
        setTextValue(1, "圈子");
        //		RenheIMUtil.showProgressDialog(this, null);
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        circlelist = new ArrayList<CircleBean>();
        searchList = new ArrayList<CircleBean>();
        circleAdapter = new MyCircleListAdapter(this, circlelist);
        mycircleList.setAdapter(circleAdapter);

        isToShare = getIntent().getBooleanExtra("isToShare", false);
        toForwardContent = getIntent().getStringExtra("toForwardContent");
        toFrowardPic = getIntent().getStringExtra("toFrowardPic");
        toForwardObjectId = getIntent().getStringExtra("toForwardObjectId");
        shareType = getIntent().getIntExtra("shareType", ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
        contentTitle = getIntent().getStringExtra("contentTitle");
        toFrowardTitle = getIntent().getStringExtra("title");
        contentOther = getIntent().getStringExtra("contentOther");
        if (null != getIntent().getSerializableExtra("shareFilePathList")) {
            shareFilePathList = (ArrayList<String>) getIntent().getSerializableExtra("shareFilePathList");
            mFileType = getIntent().getStringExtra("fileType");
        }
        if (null != getIntent().getSerializableExtra("shareImagePathList")) {
            shareImagePathList = (ArrayList<String>) getIntent().getSerializableExtra("shareImagePathList");
            sendPicUri = Uri.parse(getIntent().getStringExtra("picUri"));
        }
        if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {//来自文件转发
            if (getIntent().getSerializableExtra("message") != null) {
                message = (com.alibaba.wukong.im.Message) getIntent().getSerializableExtra("message");
                contentTitle = ((MessageContent.FileContent) message.messageContent()).fileName();
                mFileType = ((MessageContent.FileContent) message.messageContent()).fileType();
                contentOther = FileSizeUtil.FormetFileSize(((MessageContent.FileContent) message.messageContent()).size());
            }
        }
        mMessageBuilder = IMEngine.getIMService(MessageBuilder.class);
        dialogUtil = new DialogUtil(this, new ShareMyDialogClickListener() {

            @Override
            public void onclick(int id, String msg, Conversation conversation) {
                if (id == DialogUtil.SURE_BUTTON) {
                    if (null == toForwardContent)
                        toForwardContent = "";
                    if (null == toFrowardTitle)
                        toFrowardTitle = "";
                    if (null == toFrowardPic)
                        toFrowardPic = "";
                    if (TextUtils.isEmpty(contentOther))
                        contentOther = "";
                    else {
                        contentOther = "\n" + contentOther;
                    }
                    String content = "";
                    if (TextUtils.isEmpty(contentTitle))
                        contentTitle = "";
                    else {
                        content += "\n";
                    }
                    if (TextUtils.isEmpty(toForwardContent))
                        toForwardContent = "";
                    else {
                        if (null != toForwardObjectId && !toForwardObjectId.startsWith("msg")
                                && !toForwardObjectId.startsWith("http")) {
                            toForwardContent = "\n" + toForwardContent;
                        }
                    }
                    switch (shareType) {
                        case ConversationShareType.CONVERSATION_SEND_FROM_SHARE:
                        case ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE:
                        case ConversationShareType.CONVERSATION_SEND_FROM_LINK_FORWARD://从IM转发过来的分享消息
                            if (null != toForwardObjectId && toForwardObjectId.startsWith("http")) {
                                content = toForwardContent;
                            } else {
                                content = contentTitle + toForwardContent + contentOther;
                            }
                            createConversation(
                                    mMessageBuilder.buildLinkedMessage(toForwardObjectId, toFrowardTitle, content, toFrowardPic), msg,
                                    1);
                            break;
                        case ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD://从IM、站内信转发过来的消息
                            createConversation(mMessageBuilder.buildTextMessage(toForwardContent), msg, 1);
                            break;
                        case ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_FORWARD://从IM转发过来的图片消息
                            createConversation(mMessageBuilder.buildImageMessage(toFrowardPic), msg, 1);
                            break;
                        case ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD://从IM转发过来的文件消息
                            if (null != shareFilePathList && !shareFilePathList.isEmpty()) {
                                for (int i = 0; i < shareFilePathList.size(); i++) {
                                    com.alibaba.wukong.im.Message message = getSendFileMessage(shareFilePathList.get(i));
                                    if (i == shareFilePathList.size() - 1) {
                                        createConversation(message, msg, 1);
                                    } else {
                                        createConversation(message, msg, 0);
                                    }
                                }
                            } else if (null != message) {
                                String fileUrl = ((MessageContent.FileContent) message.messageContent()).url();//文件下载地址
                                String fileName = ((MessageContent.FileContent) message.messageContent()).fileName();//文件下载地址
                                String fileType = ((MessageContent.FileContent) message.messageContent()).fileType();//文件下载地址
                                long fileSize = ((MessageContent.FileContent) message.messageContent()).size();//文件下载地址
                                message = mMessageBuilder.buildFileMessage(fileUrl, fileSize, fileName, fileType);
                                createConversation(message, msg, 1);
                            }
                            break;
                        case ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE://从IM转发过来的文件消息
                            if (null != shareImagePathList && !shareImagePathList.isEmpty()) {
                                for (int i = 0; i < shareImagePathList.size(); i++) {
                                    if (i == shareImagePathList.size() - 1) {
                                        try { // 发送图片消息
                                            String conpressFilePath = compressBitmap(shareImagePathList.get(i)); // 压缩图片
                                            createConversation(mMessageBuilder.buildImageMessage(conpressFilePath), msg, 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try { // 发送图片消息
                                            String conpressFilePath = compressBitmap(shareImagePathList.get(i)); // 压缩图片
                                            createConversation(mMessageBuilder.buildImageMessage(conpressFilePath), msg, 0);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    createConversation(conversation);
                }
            }

            @Override
            public void onclick(int id, String msg, ConversationItem conversation) {

            }

            @Override
            public void onclick(int id, String msg, HlContactRenheMember hlContactRenheMember) {

            }

            @Override
            public void onclick(int id, String msg) {
            }

        });
        mHandler = new Handler(new android.os.Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String keyword = (String) msg.obj;
                        populateCircles(keyword);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem addfriendsItem = menu.findItem(R.id.menu_save);
        addfriendsItem.setTitle("我申请的");
        addfriendsItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == R.id.menu_save) {
            startActivity(new Intent(MyCircleAct.this, ActivityMyCircleJoinReuset.class));
        }
        return super.onOptionsItemSelected(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getConversationList();
    }

    @Override
    protected void initListener() {
        super.initListener();

        keywordEdt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Message m = new Message();
                m.obj = keywordEdt.getText().toString();
                m.what = 0;
                mHandler.sendMessageDelayed(m, 300);
            }
        });

        keywordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return true;
            }
        });

        mycircleList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isToShare) {
                    mConversation = ((CircleBean) circleAdapter.getItem(position)).getConversation();
                    if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {
                        dialogUtil.createSendFileDialogWithConversation(MyCircleAct.this, contentTitle, contentOther, mFileType, null, shareType, ((CircleBean) circleAdapter.getItem(position)).getConversation());
                    } else if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE) {
                        dialogUtil.createSendFileDialogWithConversation(MyCircleAct.this, contentTitle, contentOther, mFileType, sendPicUri,
                                shareType, ((CircleBean) circleAdapter.getItem(position)).getConversation());
                    } else {
                        dialogUtil.createShareDialogWithConversation(MyCircleAct.this, toFrowardTitle, "取消", "确定", toForwardContent, toFrowardPic,
                                contentTitle, contentOther, ((CircleBean) circleAdapter.getItem(position)).getConversation());
                    }
                } else {
                    Intent intent = new Intent(MyCircleAct.this, ChatMainActivity.class);
                    intent.putExtra("conversation", ((CircleBean) circleAdapter.getItem(position)).getConversation());
                    startHlActivity(intent);
                }
            }
        });
    }

    /**
     * 获取IM圈子列表
     */
    public void getConversationList() {
        // RenheIMUtil.showProgressDialog(getActivity(), "会话加载中...");
        IMEngine.getIMService(ConversationService.class).listConversations(new Callback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                // RenheIMUtil.dismissProgressDialog();
                if (conversations == null || conversations.isEmpty()) {
                    // setEmptyText("您还没有会话，赶紧创建会话发起聊天吧^_^");
                } else {
                    circlelist.clear();
                    searchList.clear();

                    List<CircleBean> myCreatelist = new ArrayList<CircleBean>();
                    List<CircleBean> myJoinlist = new ArrayList<CircleBean>();
                    for (Conversation conversation : conversations) {
                        //if (conversation.status() != Conversation.ConversationStatus.OFFLINE) {
                        if (conversation.status() == Conversation.ConversationStatus.NORMAL
                                || conversation.status() == Conversation.ConversationStatus.HIDE) {
                            CircleBean circleBean = new CircleBean();
                            circleBean.setConversation(conversation);
                            circleBean.setAvater(conversation.icon());// 头像
                            circleBean.setName(conversation.title());// 标题
                            circleBean.setCount(conversation.totalMembers());// 人数
                            // String key =
                            // conversation.privateExtension("circleId");
                            String key = conversation.extension("circleId");
                            if (key != null && key != "") {
                            } else {
                                key = "0";// 默认圈号
                            }
                            circleBean.setNumber(Integer.parseInt(key));// 圈号,由本地服务器端传给IM云返回，key值

                            //进行排序
                            String type = conversation.extension(Constants.ISCIRCLEMASTER);
                            //test
                            if (String.valueOf(RenheApplication.getInstance().currentOpenId).equals(type)) {
                                circleBean.setRoleType(1);
                                myCreatelist.add(circleBean);
                            } else {
                                circleBean.setRoleType(3);
                                myJoinlist.add(circleBean);
                            }
                        }
                    }
                    circlelist.addAll(0, myCreatelist);
                    circlelist.addAll(myJoinlist);
                    if (circlelist.size() > 0) {
                        for (int i = 0, count = circlelist.size(); i < count; i++) {
                            circlelist.get(i).setMyCreateCount(myCreatelist.size());
                            circlelist.get(i).setParticipatedCount(myJoinlist.size());
                        }
                    }
                    searchList.addAll(circlelist);
                    if (circlelist != null && circlelist.size() > 0) {
                        // circlesMap.put("1", circlelist);
                        if (circleAdapter != null) {
                            circleAdapter.notifyDataSetChanged();
                        } else {
                            circleAdapter = new MyCircleListAdapter(MyCircleAct.this, circlelist);
                            mycircleList.setAdapter(circleAdapter);
                        }
                    }
                }
                //				RenheIMUtil.dismissProgressDialog();
                fadeUitl.removeFade(rootRl);
            }

            @Override
            public void onException(String code, String reason) {
                //				RenheIMUtil.dismissProgressDialog();
                fadeUitl.removeFade(rootRl);
                if (Constants.DEBUG_MODE) {
                    Toast.makeText(MyCircleAct.this, "加载会话列表失败.code:" + code + " reason:" + reason, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProgress(List<Conversation> conversations, int i) {
            }
        }, Integer.MAX_VALUE, Conversation.ConversationType.GROUP);
    }

    /**
     * 获取相关条件的圈子
     *
     * @param keyword
     */
    public void populateCircles(String keyword) {
        circlelist.clear();
        if (searchList != null && searchList.size() > 0) {
            if (TextUtils.isEmpty(keyword)) {
                // for (int i = 0; i < searchList.size(); i++) {
                // CircleBean cBean = searchList.get(i);
                // circlelist.add(cBean);
                // }
                circlelist.addAll(searchList);
            } else {
                for (int i = 0; i < searchList.size(); i++) {
                    String circleName = searchList.get(i).getName();
                    String circleNumb = "" + searchList.get(i).getNumber();// 圈号
                    String namePinyinFrist = PinyinUtil.cn2FirstSpell(circleName).toLowerCase();
                    String namePinyin = PinyinUtil.cn2Spell(circleName).toLowerCase();// 拼音小写
                    keyword = keyword.toLowerCase();
                    // 查询条件控制
                    if (circleName.contains(keyword) || namePinyin.contains(keyword) || namePinyinFrist.startsWith(keyword)
                            || circleNumb.startsWith(keyword)) {
                        circlelist.add(searchList.get(i));
                    }
                    if (circlelist.size() > 0) {
                        int create = 0, join = 0;
                        for (int j = 0, count = circlelist.size(); j < count; j++) {
                            if (circlelist.get(j).getRoleType() == 1) {
                                create++;
                            } else if (circlelist.get(j).getRoleType() == 3) {
                                join++;
                            }
                        }
                        for (int j = 0, count = circlelist.size(); j < count; j++) {
                            circlelist.get(j).setMyCreateCount(create);
                            circlelist.get(j).setParticipatedCount(join);
                        }
                    }
                }
            }
            if (circleAdapter != null) {
                circleAdapter.notifyDataSetChanged();
            } else {
                circleAdapter = new MyCircleListAdapter(MyCircleAct.this, circlelist);
                mycircleList.setAdapter(circleAdapter);
            }
        }
    }

    private void createConversation(final com.alibaba.wukong.im.Message message, String msg, int flag) {
        if (null != mConversation) {
            ToastUtil.showToast(MyCircleAct.this, R.string.send_success);
            send(mConversation, message, msg, flag);
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(MyCircleAct.this, R.string.send_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送消息
     *
     * @param message
     */
    private void send(final Conversation mConversation, final com.alibaba.wukong.im.Message message, final String msgExtra,
                      final int flag) {
        if (mConversation != null && !TextUtils.isEmpty(mConversation.draftMessage())) {
            mConversation.updateDraftMessage("");
        }
        if (message.messageContent().type() == MessageContent.MessageContentType.IMAGE
                || message.messageContent().type() == MessageContent.MessageContentType.AUDIO)
            mLocalUrl = ((MessageContent.MediaContent) message.messageContent()).url();
        String mLocalPath = null;
        if ((message.messageContent().type() == MessageContent.MessageContentType.FILE)) {//发送文件，把文件本地路径放入localExtras，方便从message取出文件本地路径
            mLocalPath = ((MessageContent.MediaContent.FileContent) message.messageContent()).url();
        }
        final String localFilePath = mLocalPath;
        message.sendTo(mConversation, new com.alibaba.wukong.Callback<com.alibaba.wukong.im.Message>() {
            @Override
            public void onSuccess(com.alibaba.wukong.im.Message msg) {
                //给message添加扩展字段，存放message发送者的头像和姓名，方便列表页获取
                ConversationListUtil.updateGroupMessageExtension(msg, RenheApplication.getInstance().getUserInfo().getName(),
                        RenheApplication.getInstance().getUserInfo().getUserface());
                if (msg.messageContent().type() == MessageContent.MessageContentType.IMAGE) {
                    deleteBitmap(mLocalUrl);// 删除本地压缩图片
                    Intent i = new Intent(Constants.BroadCastAction.IM_CHAT_FORWARD_ACTION);
                    i.putExtra("message", msg);
                    sendBroadcast(i);
                }
                if ((msg.messageContent().type() == MessageContent.MessageContentType.FILE)) {//发送文件，把文件本地路径放入localExtras，方便从message取出文件本地路径
                    Map<String, String> localPathMap = new HashMap<>();
                    localPathMap.put("localPath", localFilePath);
                    msg.updateLocalExtras(localPathMap);
                }
                if (!TextUtils.isEmpty(msgExtra) && flag == 1) {
                    mMessageBuilder.buildTextMessage(msgExtra).sendTo(mConversation,
                            new com.alibaba.wukong.Callback<com.alibaba.wukong.im.Message>() {
                                @Override
                                public void onSuccess(com.alibaba.wukong.im.Message msg) {
                                    //给message添加扩展字段，存放message发送者的头像和姓名，方便列表页获取
                                    ConversationListUtil.updateGroupMessageExtension(msg, RenheApplication.getInstance().getUserInfo().getName(),
                                            RenheApplication.getInstance().getUserInfo().getUserface());
                                }

                                @Override
                                public void onException(String code, String reason) {
                                    Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
                                }

                                @Override
                                public void onProgress(com.alibaba.wukong.im.Message msg, int i) {
                                }
                            });
                }
            }

            @Override
            public void onException(String code, String reason) {
                Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
                //				ToastUtil.showErrorToast(MyCircleAct.this, "消息发送失败！code=" + code + " reason=" + reason);
            }

            @Override
            public void onProgress(com.alibaba.wukong.im.Message msg, int i) {
            }
        });
    }

    private com.alibaba.wukong.im.Message getSendFileMessage(String filePath) {
        com.alibaba.wukong.im.Message message = null;
        String fileName, fileType = null;
        long fileSize = 0L;
        File file = new File(filePath);
        if (null != file && file.exists()) {
            fileName = file.getName();
            try {
                fileSize = FileSizeUtil.getFileSize(file);
                if (Constants.DEBUG_MODE) {
                    Log.d("文件大小", "*" + fileSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (filePath.lastIndexOf(".") > 0 && filePath.lastIndexOf(".") < filePath.length() - 1) {
                fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
                if (Constants.DEBUG_MODE) {
                    Log.d("文件类型", fileType);
                }
            } else {
                if (Constants.DEBUG_MODE) {
                    Log.d("文件类型", "未知");
                }
            }
            message = mMessageBuilder.buildFileMessage(filePath, fileSize, fileName, fileType);
        }
        return message;
    }

    /**
     * 图片发送前进行压缩
     *
     * @param picturePath
     * @return
     * @throws IOException
     */
    private String compressBitmap(String picturePath) throws IOException {
        if (picturePath.equals(""))
            return null;
        // 缩放图片, width, height 按相同比例缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        // options 设为true时，构造出的bitmap没有图片，只有一些长宽等配置信息，但比较快，设为false时，才有图片
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
        int scale = (int) (options.outWidth / (float) 720);
        if (scale <= 0)
            scale = 1;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(picturePath, options);
        cacheBitmapList.add(bitmap);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/renhe/wukong_"
                    + System.currentTimeMillis() + "_tmp.png";
            saveBitmap(filePath, bitmap);
            return filePath;
        }

        Toast.makeText(this, "请检查SD卡是否可用", Toast.LENGTH_LONG).show();
        return null;
    }

    public void saveBitmap(String filepath, Bitmap bitmap) {
        try {
            File f = new File(filepath);
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteBitmap(String filePath) {
        File f = new File(filePath);
        if (f.exists())
            f.delete();
    }

    /**
     * 创建IM会话
     */
    private void createConversation(Conversation conversation) {
        if (null == conversation)
            return;
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
        Intent intent = new Intent(MyCircleAct.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
        intent.putExtra("conversation", conversation);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
}

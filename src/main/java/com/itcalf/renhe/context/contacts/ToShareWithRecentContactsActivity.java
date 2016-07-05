package com.itcalf.renhe.context.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.doraemon.Doraemon;
import com.alibaba.doraemon.threadpool.Thread;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.BaseRecyclerAdapter;
import com.itcalf.renhe.adapter.ConversationRecyclerItemAdapter;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.HlEngine;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.eventbusbean.SendFileMsgEvent;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DialogUtil;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import de.greenrobot.event.EventBus;

/**
 * description :分享，最近联系人
 * Created by Chans Renhenet
 * 2015/8/3
 */
public class ToShareWithRecentContactsActivity extends BaseActivity implements ConversationListUtil.ConversationCallBack {

    @BindView(R.id.recent_chat_tv)
    TextView recentChatTv;
    @BindView(R.id.recent_chat_fl)
    FrameLayout recentChatFl;
    private ClearableEditText keyword_edt;
    private RelativeLayout chooseOther_Rl;
    private Intent intent;
    private DialogUtil dialogUtil;

    private String toForwardContent;
    private String toFrowardPic;
    private String toFrowardTitle;
    private String contentTitle;
    private String contentOther;

    private ArrayList<String> shareFilePathList;
    private ArrayList<String> shareImagePathList;
    private MessageBuilder mMessageBuilder;
    private Contact ct;
    private String toForwardObjectId;
    private static int REQUEST_CODE_TOCICLE = 10;
    private int shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE;
    private String mLocalUrl;
    private String mFileType;
    private Uri sendPicUri;
    private Message message;

    //数据初始化
    private ArrayList<ConversationItem> datas;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private LinearLayout loadingLl;
    private ConversationRecyclerItemAdapter conversationRecyclerItemAdapter;
    //工具初始化
    private ConversationListUtil conversationListUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.search_recent_contacts);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(getString(R.string.otherapp_share_to_heliao));
        keyword_edt = (ClearableEditText) findViewById(R.id.keyword_edt);
        chooseOther_Rl = (RelativeLayout) findViewById(R.id.chooseOther_Rl);
        mRecyclerView = (RecyclerView) findViewById(R.id.conversation_recycler_view);
        loadingLl = (LinearLayout) findViewById(R.id.loadingLL);
        mRecyclerView.setVisibility(View.GONE);
        loadingLl.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);//这里用线性显示 类似于list view
    }

    @Override
    protected void initData() {
        super.initData();
        mMessageBuilder = IMEngine.getIMService(MessageBuilder.class);
        intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Constants.OTHER_APP_SHARE_TO_HELIAO_FRIEND.equals(action)) {//第三方应用（赞服务）分享内容到和聊好友
            new HlEngine(this, intent).init();
            if (null != getIntent() && null != getIntent().getExtras()) {
                toForwardContent = getIntent().getExtras().getString("toForwardContent");//正文内容
                toFrowardPic = getIntent().getExtras().getString("toForwardPic");//图片url
                shareType = getIntent().getExtras().getInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE);//和聊支持的类型之一：网页链接
                toFrowardTitle = getIntent().getExtras().getString("title");//title
                if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE)
                    toForwardObjectId = getIntent().getExtras().getString("toForwardUrl");//网页url
            }
        } else if (Intent.ACTION_SEND.equals(action) && type != null) {//外部应用分享入口
            new HlEngine(this, intent).init();
            if ("text/plain".equals(type)) {//外部应用分享文本
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "文本");
                }
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                //				String sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (!TextUtils.isEmpty(sharedText)) {
                    toForwardContent = sharedText;
                    shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD;
                } else {
                    shareFilePathList = getShareImagePathList(intent);
                    shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
                }

            } else if (type.startsWith("image/")) {//外部应用分享图片
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "单张图片");
                }
                shareImagePathList = getShareImagePathList(intent);
                shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE;
            } else {//外部应用分享的其他类型文件
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "文本、图片类型外的其他文件");
                }
                shareFilePathList = getShareImagePathList(intent);
                shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            new HlEngine(this, intent).init();
            if (type.startsWith("image/")) {//外部应用分享多图片
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "多张图片");
                }
                shareImagePathList = getShareImagePathList(intent);
                shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE;
            } else {//外部应用分享的其他类型文件
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "多个文本、图片类型外的其他文件");
                }
                shareFilePathList = getShareImagePathList(intent);
                shareType = Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
            }
        } else {//自己应用内的分享
            toForwardContent = getIntent().getExtras().getString("toForwardContent");
            toFrowardPic = getIntent().getExtras().getString("toForwardPic");
            toForwardObjectId = getIntent().getExtras().getString("toForwardObjectId");
            shareType = getIntent().getExtras().getInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
            contentTitle = getIntent().getExtras().getString("contentTitle");
            toFrowardTitle = getIntent().getExtras().getString("title");
            contentOther = getIntent().getExtras().getString("contentOther");
            if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE)
                toForwardObjectId = getIntent().getExtras().getString("toForwardUrl");
            if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {//来自文件转发
                if (getIntent().getSerializableExtra("message") != null) {
                    message = (Message) getIntent().getSerializableExtra("message");
                    contentTitle = ((MessageContent.FileContent) message.messageContent()).fileName();
                    mFileType = ((MessageContent.FileContent) message.messageContent()).fileType();
                    contentOther = FileSizeUtil.FormetFileSize(((MessageContent.FileContent) message.messageContent()).size());
                }
            }
        }

        dialogUtil = new DialogUtil(this, new DialogUtil.ShareMyDialogClickListener() {

            @Override
            public void onclick(int id, String msg) {
            }

            @Override
            public void onclick(int id, String msg, Conversation conversation) {

            }

            @Override
            public void onclick(int id, String msg, ConversationItem conversationItem) {
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
                        contentTitle += "\n";
                        content += "\n";
                    }
                    if (TextUtils.isEmpty(toForwardContent))
                        toForwardContent = "";
                    else {
                        if (null != toForwardObjectId && !toForwardObjectId.startsWith("msg")
                                && !toForwardObjectId.startsWith("http") && !toForwardObjectId.startsWith("user")) {
                            if (!toForwardContent.startsWith("\n"))
                                toForwardContent = "\n" + toForwardContent;
                        }
                    }
                    switch (shareType) {
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE:
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE:
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_LINK_FORWARD://从IM转发过来的分享消息
                            if (null != toForwardObjectId && toForwardObjectId.startsWith("http")) {
                                content = toForwardContent;
                            } else {
                                content = contentTitle + toForwardContent + contentOther;
                            }
                            send(conversationItem.getConversation(),
                                    mMessageBuilder.buildLinkedMessage(toForwardObjectId, toFrowardTitle, content, toFrowardPic), msg,
                                    1);
                            //						createConversation(
                            //								mMessageBuilder.buildLinkedMessage(toForwardObjectId, toFrowardTitle, content, toFrowardPic), msg,
                            //								1);
                            break;
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD://从IM、站内信转发过来的文本消息
                            send(conversationItem.getConversation(), mMessageBuilder.buildTextMessage(toForwardContent), msg, 1);
                            //								createConversation(mMessageBuilder.buildTextMessage(toForwardContent), msg, 1);
                            break;
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_FORWARD://从IM转发过来的图片消息
                            send(conversationItem.getConversation(), mMessageBuilder.buildImageMessage(toFrowardPic), msg, 1);
                            //						createConversation(mMessageBuilder.buildImageMessage(toFrowardPic), msg, 1);
                            break;
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD://从IM转发过来的文件消息
                            if (null != shareFilePathList && !shareFilePathList.isEmpty()) {
                                for (int i = 0; i < shareFilePathList.size(); i++) {
                                    Message message = getSendFileMessage(shareFilePathList.get(i));
                                    if (i == shareFilePathList.size() - 1) {
                                        send(conversationItem.getConversation(), message, msg, 1);
                                        //									createConversation(message, msg, 1);
                                    } else {
                                        send(conversationItem.getConversation(), message, msg, 0);
                                        //									createConversation(message, msg, 0);
                                    }
                                }
                            } else if (null != message) {
                                String fileUrl = ((MessageContent.FileContent) message.messageContent()).url();//文件下载地址
                                String fileName = ((MessageContent.FileContent) message.messageContent()).fileName();//文件下载地址
                                String fileType = ((MessageContent.FileContent) message.messageContent()).fileType();//文件下载地址
                                long fileSize = ((MessageContent.FileContent) message.messageContent()).size();//文件下载地址
                                message = mMessageBuilder.buildFileMessage(fileUrl, fileSize, fileName, fileType);
                                send(conversationItem.getConversation(), message, msg, 1);
                                //							createConversation(message, msg, 1);
                            }
                            break;
                        case Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE://从IM转发过来的文件消息
                            if (null != shareImagePathList && !shareImagePathList.isEmpty()) {
                                for (int i = 0; i < shareImagePathList.size(); i++) {
                                    if (i == shareImagePathList.size() - 1) {
                                        try { // 发送图片消息
                                            String conpressFilePath = compressBitmap(shareImagePathList.get(i)); // 压缩图片
                                            send(conversationItem.getConversation(), mMessageBuilder.buildImageMessage(conpressFilePath), msg, 1);
                                            //										createConversation(mMessageBuilder.buildImageMessage(conpressFilePath), msg, 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        try { // 发送图片消息
                                            String conpressFilePath = compressBitmap(shareImagePathList.get(i)); // 压缩图片
                                            send(conversationItem.getConversation(), mMessageBuilder.buildImageMessage(conpressFilePath), msg, 0);
                                            //										createConversation(mMessageBuilder.buildImageMessage(conpressFilePath), msg, 0);
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
                    createConversation(conversationItem);
                }
            }

            @Override
            public void onclick(int id, String msg, HlContactRenheMember hlContactRenheMember) {

            }
        });
        this.datas = new ArrayList<>();
        this.conversationListUtil = new ConversationListUtil(this, datas);
        conversationListUtil.setConversationCallBack(this);
        conversationRecyclerItemAdapter = new ConversationRecyclerItemAdapter(this, mRecyclerView,
                datas, conversationListUtil, false);
        mRecyclerView.setAdapter(conversationRecyclerItemAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //To login IM
        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        if (userInfo.isImValid()) {
            conversationListUtil.signIn(RenheIMUtil.buildAuthParam(Long.parseLong(userInfo.getImId() + ""),
                    userInfo.getName(), userInfo.getImPwd()));
        } else {
            conversationListUtil.clientRegisterIM();
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        chooseOther_Rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = null;
                if (null != getIntent()) {
                    i = getIntent();
                    i.setClass(ToShareWithRecentContactsActivity.this, ToShareContactsActivity.class);
                } else {
                    i = new Intent(ToShareWithRecentContactsActivity.this, ToShareContactsActivity.class);
                }
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(i, REQUEST_CODE_TOCICLE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        conversationRecyclerItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object data, int position) {
                if (null != data && data instanceof ConversationItem) {
                    ConversationItem conversationItem = (ConversationItem) data;
                    if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                        Conversation conversation = conversationItem.getConversation();
                        if (null != conversation) {
                            if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {
                                dialogUtil.createSendFileDialog(ToShareWithRecentContactsActivity.this, contentTitle,
                                        contentOther, mFileType, null, shareType, conversationItem);
                            } else if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE) {
                                dialogUtil.createSendFileDialog(ToShareWithRecentContactsActivity.this, contentTitle,
                                        contentOther, mFileType, sendPicUri, shareType, conversationItem);
                            } else {
                                dialogUtil.createShareDialog(ToShareWithRecentContactsActivity.this, toFrowardTitle, "取消", "确定",
                                        toForwardContent, toFrowardPic, contentTitle, contentOther, conversationItem);
                            }
                        }
                    }
                }
            }

            @Override
            public boolean onItemLongClick(View view, Object data, int position) {
                return false;
            }
        });
    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void loadConversationListSuccess(ArrayList<ConversationItem> conversationItems) {
        conversationRecyclerItemAdapter.addItems(conversationItems);
        loadingLl.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadConversationListFail() {
        loadingLl.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        recentChatTv.setVisibility(View.GONE);
        recentChatFl.setVisibility(View.GONE);
    }

    @Override
    public void onConversationsAdd(ArrayList<ConversationItem> conversationItems) {

    }

    @Override
    public void onConversationsRemoved(ArrayList<ConversationItem> conversationItems) {

    }

    @Override
    public void onConversationsUpdated(ArrayList<ConversationItem> conversationItems) {

    }

    private ArrayList<String> getShareImagePathList(Intent intent) {
        ArrayList<String> mShareFilePathList = new ArrayList<String>();
        if (Intent.ACTION_SEND.equals(intent.getAction())) { //分享单个
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                if (null != uri) {
                    String path = getRealFilePath(this, uri);
                    File file = new File(path);
                    if (!TextUtils.isEmpty(path)) {
                        long fileSize = 0L;

                        try {
                            fileSize = FileSizeUtil.getFileSize(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!(Constants.DEBUG_MODE && file.getName().toLowerCase().contains("heliao"))) {
                            if (FileSizeUtil.isTooLarge(Constants.IM_FILE_LIMIT_SIZE, fileSize)) {//文件过大，超过10M
                                ToastUtil.showToast(ToShareWithRecentContactsActivity.this,
                                        R.string.file_send_exception_toolarge);
                                finish();
                            }
                        }
                        sendPicUri = uri;
                        contentTitle = file.getName();
                        contentOther = FileSizeUtil.FormetFileSize(fileSize);
                        if (path.lastIndexOf(".") > 0 && path.lastIndexOf(".") < path.length() - 1) {
                            mFileType = path.substring(path.lastIndexOf(".") + 1);
                        }
                        mShareFilePathList.add(path);
                    }
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) { //分享多个
            Bundle extras = intent.getExtras();
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                ArrayList<Parcelable> mList = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
                for (Parcelable pa : mList) {
                    Uri uri = (Uri) pa;
                    if (null != uri) {
                        {
                            String path = getRealFilePath(this, uri);
                            File file = new File(path);
                            if (!TextUtils.isEmpty(path)) {

                                long fileSize = 0L;
                                try {
                                    fileSize = FileSizeUtil.getFileSize(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!(Constants.DEBUG_MODE && file.getName().toLowerCase().contains("heliao"))) {
                                    if (FileSizeUtil.isTooLarge(Constants.IM_FILE_LIMIT_SIZE, fileSize)) {//文件过大，超过10M
                                        ToastUtil.showToast(ToShareWithRecentContactsActivity.this,
                                                R.string.file_send_exception_toolarge);
                                        finish();
                                    }
                                }
                                sendPicUri = uri;
                                contentTitle = file.getName();
                                try {
                                    contentOther = FileSizeUtil.FormetFileSize(FileSizeUtil.getFileSize(file));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (path.lastIndexOf(".") > 0 && path.lastIndexOf(".") < path.length() - 1) {
                                    mFileType = path.substring(path.lastIndexOf(".") + 1);
                                }
                                mShareFilePathList.add(path);
                            }
                        }
                    }
                }
            }
        }
        for (String s : mShareFilePathList) {
            Log.d("图片路径*****", s);
        }
        return mShareFilePathList;
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null,
                    null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 发送消息
     *
     * @param message
     */
    private void send(final Conversation mConversation, final Message message, final String msgExtra,
                      final int flag) {
        ToastUtil.showToast(ToShareWithRecentContactsActivity.this, R.string.send_success);
        Thread thread = (Thread) Doraemon.getArtifact(Thread.THREAD_ARTIFACT);
        thread.start(new Runnable() {
            @Override
            public void run() {
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
                message.sendTo(mConversation, new Callback<Message>() {
                    @Override
                    public void onSuccess(Message msg) {
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
                                    new Callback<Message>() {
                                        @Override
                                        public void onSuccess(Message msg) {
                                            //给message添加扩展字段，存放message发送者的头像和姓名，方便列表页获取
                                            ConversationListUtil.updateGroupMessageExtension(msg, RenheApplication.getInstance().getUserInfo().getName(),
                                                    RenheApplication.getInstance().getUserInfo().getUserface());
                                        }

                                        @Override
                                        public void onException(String code, String reason) {
                                            Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
                                        }

                                        @Override
                                        public void onProgress(Message msg, int i) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onException(String code, String reason) {
                        Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
                        ToastUtil.showErrorToast(ToShareWithRecentContactsActivity.this, "消息发送失败！code=" + code + " reason=" + reason);
                    }

                    @Override
                    public void onProgress(Message msg, int i) {
                        if (null != msg && msg.messageContent().type() == MessageContent.MessageContentType.FILE)
                            EventBus.getDefault().post(new SendFileMsgEvent(msg));
                    }
                });
            }
        });
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TOCICLE) {
            finish();
        }
    }

    private Message getSendFileMessage(String filePath) {
        Message message = null;
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
    private void createConversation(ConversationItem conversationItem) {
        if (null == conversationItem)
            return;
        //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
        ConversationListUtil.updateChatConversationExtension(conversationItem.getConversation(), conversationItem.getNickname(), conversationItem.getIconUrl());
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
        Intent intent = new Intent(ToShareWithRecentContactsActivity.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
        intent.putExtra("conversation", conversationItem.getConversation());
        intent.putExtra("userName", conversationItem.getNickname());
        intent.putExtra("userFace", conversationItem.getIconUrl());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

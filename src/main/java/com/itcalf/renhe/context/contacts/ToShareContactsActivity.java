package com.itcalf.renhe.context.contacts;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.MessageBuilder;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.ConversationShareType;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ContactArrayAdapter;
import com.itcalf.renhe.adapter.HlContactRenheMemberAdapterforContact;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContactRenheMemberItem;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DialogUtil;
import com.itcalf.renhe.utils.DialogUtil.ShareMyDialogClickListener;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.itcalf.renhe.view.SearchContactsSideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author chan
 * @createtime 2014-10-21
 * @功能说明 自己的联系人
 */
public class ToShareContactsActivity extends BaseActivity {
    @BindView(R.id.import_contact_btn)
    Button importContactBtn;
    @BindView(R.id.none_contacts_ll)
    LinearLayout noneContactsLl;
    @BindView(R.id.import_contact_tv)
    com.itcalf.renhe.view.TextView importContactTv;
    //	private boolean isFromMenu;
    // 登出标识（两次按下返回退出时的标识字段）
    // private boolean logoutFlag;
    // 联系人快速定位视图组件
    private SearchContactsSideBar sideBar;
    // 联系人关键字查询条件
    private EditText mKeywordEdt;
    // 联系人数量
    private TextView mContactCountTxt;
    // 联系人列表
    private ListView mContactsListView;
    // 字母显示
    private TextView mLetterTxt;
    // 没有联系人的提示
    private ProgressBar waitPb;

    private Handler mNotifHandler;
    // 带标题分割的Adapter
    private HlContactRenheMemberAdapterforContact mAdapter;
    // 联系人数据
    private Map<String, List<HlContactRenheMember>> mContactsMap;
    private Drawable imgCloseButton;
    private Context context;
    private static final int REQUEST_DELAY_TIME = 0;
    private final static String SECTION_ID = "py";// 区分显示的是否是字母A-Z

    // 注册广播，接受更新消息
    public static final String REFRESH_CONTACT_RECEIVER_ACTION = "com.renhe.refresh_contact";
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;

    private GuideDoubleTapReceiver guideDoubleTapReceiver;
    private DialogUtil dialogUtil;
    private String toForwardContent;
    private String toFrowardPic;
    private String toFrowardTitle;
    private String contentTitle;
    private String contentOther;
    /**
     * 消息构造器
     */
    private MessageBuilder mMessageBuilder;
    private HlContactRenheMember ct;
    private String toForwardObjectId;
    private int shareType = ConversationShareType.CONVERSATION_SEND_FROM_SHARE;
    private static int REQUEST_CODE_TOCICLE = 10;
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
        context = this;
        getTemplate().doInActivity(this, R.layout.search_contacts_new);
    }

    @Override
    protected void findView() {
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);
        mKeywordEdt = (EditText) findViewById(R.id.keyword_edt);
        mContactCountTxt = (TextView) findViewById(R.id.count_txt);
        mContactsListView = (ListView) findViewById(R.id.contacts_list);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        waitPb = (ProgressBar) findViewById(R.id.waitPb);
        sideBar.setVisibility(View.INVISIBLE);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        msp = getSharedPreferences("conversation_list", 0);
        mEditor = msp.edit();
        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_CONTACT_RECEIVER_ACTION);
        context.registerReceiver(broadcast, intentFilter);
        mContactsMap = new TreeMap<>();
        sideBar.setTextView(mLetterTxt);
        // 初始化adapter
        mAdapter = new HlContactRenheMemberAdapterforContact(context, R.layout.contact_list_item, R.id.title_txt);
        mContactsListView.setAdapter(mAdapter);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // // 该字母首次出现的位置
                int section = mAdapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int positon = mAdapter.getPositionForSection(section);
                    mContactsListView.setSelection(positon);
                }
            }
        });
        fadeUitl = new FadeUitl(this, "加载中...");
        fadeUitl.addFade(rootRl);
        mNotifHandler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    // 关键字搜索
                    case 0:
                        String keyword = (String) msg.obj;
                        populateContacts(keyword);
                        break;
                    case 1:
                        fadeUitl.removeFade(rootRl);
                        populateContacts(null);
                        // 获取新的朋友数量
                        break;
                    case 3:
                        break;
                }
                return false;
            }
        });
        mKeywordEdt.addTextChangedListener(tbxEdit_TextChanged);
        mKeywordEdt.setOnTouchListener(txtEdit_OnTouch);
        mKeywordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return true;
            }
        });
        mMessageBuilder = IMEngine.getIMService(MessageBuilder.class);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {//外部应用分享入口
//			new HlEngine(this, intent).init();
            if ("text/plain".equals(type)) {//外部应用分享文本
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "文本");
                }
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                //				String sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (!TextUtils.isEmpty(sharedText)) {
                    toForwardContent = sharedText;
                    shareType = ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD;
                } else {
                    shareFilePathList = getShareImagePathList(intent);
                    shareType = ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
                }

            } else if (type.startsWith("image/")) {//外部应用分享图片
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "单张图片");
                }
                shareImagePathList = getShareImagePathList(intent);
                shareType = ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE;
            } else {//外部应用分享的其他类型文件
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "文本、图片类型外的其他文件");
                }
                shareFilePathList = getShareImagePathList(intent);
                shareType = ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
//			new HlEngine(this, intent).init();
            if (type.startsWith("image/")) {//外部应用分享多图片
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "多张图片");
                }
                shareImagePathList = getShareImagePathList(intent);
                shareType = ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE;
            } else {//外部应用分享的其他类型文件
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "多个文本、图片类型外的其他文件");
                }
                shareFilePathList = getShareImagePathList(intent);
                shareType = ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD;
            }
        } else {//自己应用内的分享
            toForwardContent = getIntent().getExtras().getString("toForwardContent");
            toFrowardPic = getIntent().getExtras().getString("toForwardPic");
            toForwardObjectId = getIntent().getExtras().getString("toForwardObjectId");
            shareType = getIntent().getExtras().getInt("type", ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
            contentTitle = getIntent().getExtras().getString("contentTitle");
            toFrowardTitle = getIntent().getExtras().getString("title");
            contentOther = getIntent().getExtras().getString("contentOther");
            if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE)
                toForwardObjectId = getIntent().getExtras().getString("toForwardUrl");
            if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {//来自文件转发
                if (getIntent().getSerializableExtra("message") != null) {
                    message = (com.alibaba.wukong.im.Message) getIntent().getSerializableExtra("message");
                    contentTitle = ((MessageContent.FileContent) message.messageContent()).fileName();
                    mFileType = ((MessageContent.FileContent) message.messageContent()).fileType();
                    contentOther = FileSizeUtil.FormetFileSize(((MessageContent.FileContent) message.messageContent()).size());
                }
            }
        }

        dialogUtil = new DialogUtil(this, new ShareMyDialogClickListener() {

            @Override
            public void onclick(int id, String msg, Conversation conversation) {

            }

            @Override
            public void onclick(int id, String msg, ConversationItem conversation) {

            }

            @Override
            public void onclick(int id, String msg, HlContactRenheMember hlContactRenheMember) {
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
                        case ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD://从IM、站内信转发过来的文本消息
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
                    createConversation(hlContactRenheMember);
                }
            }

            @Override
            public void onclick(int id, String msg) {
            }

        });
        localSearch();
        //		setTitle("选择");
        setTextValue(1, "选择");
        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
    }

    protected void initListener() {
        mContactsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HlContactRenheMemberItem item = (HlContactRenheMemberItem) mContactsListView.getAdapter().getItem(position);
                if (item != null && item.id != SECTION_ID && item.id != ContactArrayAdapter.IS_NEWFRIEND_ID
                        && item.id != ContactArrayAdapter.IS_CIRCLE_ID && item.id != ContactArrayAdapter.IS_CONTACT_ID) {
                    ct = mAdapter.getItem(position).getHlContactRenheMember();
                    if (ct.isImValid()) {
                        if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {
                            dialogUtil.createSendFileDialogWithContact(ToShareContactsActivity.this, contentTitle, contentOther, mFileType,
                                    null, shareType,ct);
                        } else if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE) {
                            dialogUtil.createSendFileDialogWithContact(ToShareContactsActivity.this, contentTitle, contentOther, mFileType,
                                    sendPicUri, shareType, ct);
                        } else {
                            dialogUtil.createShareDialogWithContacts(ToShareContactsActivity.this, toFrowardTitle, "取消", "确定",
                                    toForwardContent, toFrowardPic, contentTitle, contentOther, ct);
                        }
                    } else {
                        Toast.makeText(ToShareContactsActivity.this, ct.getName() + "还未开通对话功能", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (item.id == ContactArrayAdapter.IS_CIRCLE_ID) {
                        Intent intent = new Intent();
                        intent.setClass(context, MyCircleAct.class);
                        intent.putExtra("isToShare", true);
                        intent.putExtra("toFrowardTitle", toFrowardTitle);
                        intent.putExtra("toForwardContent", toForwardContent);
                        intent.putExtra("toFrowardPic", toFrowardPic);
                        intent.putExtra("toForwardObjectId", toForwardObjectId);
                        intent.putExtra("shareType", shareType);
                        intent.putExtra("contentTitle", contentTitle);
                        intent.putExtra("title", toFrowardTitle);
                        intent.putExtra("contentOther", contentOther);
                        if (null != shareFilePathList && !shareFilePathList.isEmpty()) {
                            intent.putExtra("shareFilePathList", shareFilePathList);
                            intent.putExtra("fileType", mFileType);
                        }
                        if (null != shareImagePathList && !shareImagePathList.isEmpty()) {
                            intent.putExtra("shareImagePathList", shareImagePathList);
                            intent.putExtra("picUri", sendPicUri);
                        }
                        if (shareType == ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD) {
                            intent.putExtra("message", message);
                        }
                        startActivityForResult(intent, REQUEST_CODE_TOCICLE);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        //如果有红点，发广播取消
                    }
                }
            }
        });

        importContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToShareContactsActivity.this, SearchForContactsActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        guideDoubleTapReceiver = new GuideDoubleTapReceiver();
        IntentFilter intentFilter2 = new IntentFilter(TabMainFragmentActivity.CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION);
        registerReceiver(guideDoubleTapReceiver, intentFilter2);
    }

    /**
     * 加载自己的好友，查询本地数据库
     */
    private void localSearch() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    mContactsMap.clear();
                    List<HlContactRenheMember> hlContactRenheMemberList = new ArrayList<>();
                    hlContactRenheMemberList = HlContactsUtils.getAllHlFriendContacts(hlContactRenheMemberList);
                    if (null != hlContactRenheMemberList && !hlContactRenheMemberList.isEmpty()) {
                        for (int i = 0; i < hlContactRenheMemberList.size(); i++) {
                            if (hlContactRenheMemberList.get(i).isImValid()) {
                                String nameInitial = hlContactRenheMemberList.get(i).getInitial();
                                if (!TextUtils.isEmpty(nameInitial)) {
                                    List<HlContactRenheMember> ctList = mContactsMap.get(nameInitial);
                                    if (null == ctList) {
                                        ctList = new ArrayList<>();
                                    }
                                    ctList.add(hlContactRenheMemberList.get(i));
                                    mContactsMap.put(nameInitial, ctList);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                mNotifHandler.sendEmptyMessage(1);
            }
        }).start();
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            // populateContacts(mKeywordEdt.getText().toString());
            Message m = new Message();
            m.obj = mKeywordEdt.getText().toString();
            m.what = 0;
            mNotifHandler.sendMessage(m);
        }
    };

    /**
     * 搜索框输入状态监听
     **/
    private TextWatcher tbxEdit_TextChanged = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())) {
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        getResources().getDrawable(R.drawable.clearbtn_selected), null);
                mKeywordEdt.setCompoundDrawablePadding(1);
            } else {
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handler.postDelayed(run, REQUEST_DELAY_TIME);
        }

    };

    /**
     * 搜索框点击事件监听
     **/
    private OnTouchListener txtEdit_OnTouch = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                /** 手指离开的事件 */
                case MotionEvent.ACTION_UP:
                    /** 手指抬起时候的坐标 **/
                    int curX = (int) event.getX();
                    if (curX > v.getWidth() - v.getPaddingRight() - imgCloseButton.getIntrinsicWidth()
                            && !TextUtils.isEmpty(mKeywordEdt.getText().toString())) {
                        mKeywordEdt.setText("");
                        int cacheInputType = mKeywordEdt.getInputType();
                        // setInputType 可以更改 TextView 的输入方式
                        mKeywordEdt.setInputType(InputType.TYPE_NULL);// EditText始终不弹出软件键盘
                        mKeywordEdt.onTouchEvent(event);
                        mKeywordEdt.setInputType(cacheInputType);
                        return true;
                    }
                    break;
            }
            return false;
        }
    };

    /**
     * 查询联系人，支持拼音简写、字母查询
     *
     * @param keyword
     */
    private void populateContacts(String keyword) {
        mAdapter.clear();
        int count = 0;
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = mContactsMap.size() + 3;//添加了3个新的选项
                mContactsListView.setAdapter(mAdapter);
                mAdapter.prepareSections(sectionsNumber);
                int sectionPosition = 0, listPosition = 0;

                // 添加圈子 add by chan 15.1.19
                HlContactRenheMemberItem circleSection = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, "圈子", ContactArrayAdapter.IS_CIRCLE_ID);
                circleSection.sectionPosition = sectionPosition;
                circleSection.listPosition = listPosition++;
                mAdapter.onSectionAdded(circleSection, sectionPosition);
                mAdapter.add(circleSection);
                HlContactRenheMemberItem circleItem;
                circleItem = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, "圈子", ContactArrayAdapter.IS_CIRCLE_ID);
                circleItem.sectionPosition = sectionPosition;
                circleItem.listPosition = listPosition++;
                mAdapter.add(circleItem);
                sectionPosition++;

                // 联系人列表加载
                Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = it
                            .next();
                    HlContactRenheMemberItem section = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setHlContactRenheMember(null);
                    mAdapter.onSectionAdded(section, sectionPosition);
                    mAdapter.add(section);

                    List<HlContactRenheMember> contactsList = entry.getValue();
                    for (int j = 0; j < contactsList.size(); j++) {
                        HlContactRenheMember ct = contactsList.get(j);
                        ++count;
                        HlContactRenheMemberItem item2 = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, ct.getName(), ct.getId() + "");
                        item2.sectionPosition = sectionPosition;
                        item2.listPosition = listPosition++;
                        item2.setHlContactRenheMember(ct);
                        mAdapter.add(item2);
                    }
                    sectionPosition++;
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Map<String, List<HlContactRenheMember>> mResultsMap = new TreeMap<>();// 添加首字母
                Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = it
                            .next();
                    List<HlContactRenheMember> contactsList = entry.getValue();
                    List<HlContactRenheMember> resultList = new ArrayList<>();
                    if (null != contactsList && !contactsList.isEmpty()) {
                        for (int j = 0; j < contactsList.size(); j++) {
                            HlContactRenheMember ct = contactsList.get(j);
                            if (null != keyword && null != ct.getName()
                                    && (ct.getName().toUpperCase().startsWith(keyword.toUpperCase())
                                    || PinyinUtil.cn2FirstSpell(ct.getName()).startsWith(keyword.toUpperCase()))) {
                                resultList.add(ct);
                                mResultsMap.put(entry.getKey(), resultList);
                            }
                        }
                    }
                }
                mAdapter.prepareSections(mResultsMap.size());
                mContactsListView.setAdapter(mAdapter);
                // 联系人列表加载
                int sectionPosition = 0, listPosition = 0;
                Set<Entry<String, List<HlContactRenheMember>>> resultSet = mResultsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> resultIt = resultSet.iterator();
                while (resultIt.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = resultIt
                            .next();

                    HlContactRenheMemberItem section = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setHlContactRenheMember(null);
                    mAdapter.onSectionAdded(section, sectionPosition);
                    mAdapter.add(section);

                    List<HlContactRenheMember> contactsList = entry.getValue();
                    for (int j = 0; j < contactsList.size(); j++) {
                        HlContactRenheMember ct = contactsList.get(j);
                        ++count;
                        HlContactRenheMemberItem item = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, ct.getName(), ct.getId() + "");
                        item.sectionPosition = sectionPosition;
                        item.listPosition = listPosition++;
                        item.setHlContactRenheMember(ct);
                        mAdapter.add(item);
                    }
                    sectionPosition++;
                }
                // mAdapter.notifyDataSetChanged();
            }
        } else {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = 1;
                mContactsListView.setAdapter(mAdapter);
                mAdapter.prepareSections(sectionsNumber);
            }
        }
        waitPb.setVisibility(View.GONE);
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
            mContactsListView.setVisibility(View.GONE);
            noneContactsLl.setVisibility(View.VISIBLE);
            importContactTv.setText(getString(R.string.contact_to_select_is_empty));
            importContactBtn.setText(getString(R.string.contact_to_add));
        }
        mContactCountTxt.setVisibility(View.GONE);
    }

    /**
     * 接收广播
     */
    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // 同步未完成，注册广播，定时接收消息
            localSearch();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("分享给我的联系人");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("分享给我的联系人"); // 统计页面
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mAdapter) {
            mAdapter.clear();
        }
        if (null != mContactsMap) {
            mContactsMap = null;
        }
        if (null != broadcast) {
            context.unregisterReceiver(broadcast);
            broadcast = null;
        }
        if (null != guideDoubleTapReceiver) {
            context.unregisterReceiver(guideDoubleTapReceiver);
            guideDoubleTapReceiver = null;
        }
        if (null != shareFilePathList) {
            shareFilePathList.clear();
            shareFilePathList = null;
        }
        if (null != shareImagePathList) {
            shareImagePathList.clear();
            shareImagePathList = null;
        }
    }

    class GuideDoubleTapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(TabMainFragmentActivity.CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION)) {
                //				getListView().setSelection(getListView().getTop());
                mContactsListView.setSelection(0);
            }
        }

    }

    /*
    * @flag 只是用来标示是否是最后一条消息，如果是，则发送额外消息，msgExtra，否则继续发送其他消息
    * */
    private void createConversation(final com.alibaba.wukong.im.Message message, final String msgExtra, final int flag) {
        if (null != message) {
            int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
            // 创建会话
            IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                    ConversationListUtil.updateChatConversationExtension(conversation, ct.getName(), ct.getUserface());
                    ToastUtil.showToast(ToShareContactsActivity.this, R.string.send_success);
                    send(conversation, message, msgExtra, flag);
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onException(String code, String reason) {
                    RenheIMUtil.dismissProgressDialog();
                    Toast.makeText(ToShareContactsActivity.this, R.string.send_failed, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(Conversation data, int progress) {
                }
            }, ct.getName(), ct.getUserface(), null, convType, Long.parseLong(ct.getImId() + ""));
        } else {
            Toast.makeText(ToShareContactsActivity.this, R.string.send_failed, Toast.LENGTH_SHORT).show();
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
                //                ToastUtil.showErrorToast(ToShareContactsActivity.this, "消息发送失败！code=" + code + " reason=" + reason);
            }

            @Override
            public void onProgress(com.alibaba.wukong.im.Message msg, int i) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TOCICLE) {
            setResult(RESULT_OK);
            finish();
        }
    }

    //	private ArrayList<String> getShareFilePathList(Intent intent) {
    //		ArrayList<String> mShareFilePathList = new ArrayList<String>();
    //		if (Intent.ACTION_SEND.equals(intent.getAction())) { //分享单个
    //			Bundle extras = intent.getExtras();
    //			if (extras.containsKey(Intent.EXTRA_STREAM)) {
    //				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
    //				if (null != uri) {
    //					String scheme = uri.getScheme();
    //					if (scheme.toString().compareTo("content") == 0) { //content://开头的uri
    //						ContentResolver cr = getContentResolver();
    //						Cursor c = cr.query(uri, null, null, null, null);
    //						c.moveToFirst();
    ////						String filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)).toString();
    //						String filePath = getRealFilePath(this,uri);
    //						if (!filePath.startsWith("/mnt")) {//检查是否有”/mnt“前缀
    //							filePath = "/mnt" + filePath;
    //						}
    //						File file = new File(filePath);
    //						System.out.println("文件是否存在**" + file.exists());
    //						mShareFilePathList.add(filePath);
    //						c.close();
    //					} else if (scheme.toString().compareTo("file") == 0) {//file:///开头的uri
    ////						String filePath = uri.toString().replace("file://", "");//替换file://
    //						String filePath = getRealFilePath(this,uri);
    //						if (!filePath.startsWith("/mnt")) {//加上"/mnt"头
    //							filePath = "/mnt" + filePath;
    //						}
    //						filePath = Uri.encode(filePath);
    //						File file = new File(filePath);
    //						System.out.println("文件是否存在**" + file.exists());
    //						mShareFilePathList.add(filePath);
    //					}
    //				}
    //			}
    //		} else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) { //分享多个
    //			Bundle extras = intent.getExtras();
    //			if (extras.containsKey(Intent.EXTRA_STREAM)) {
    //				ArrayList<Parcelable> mList = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
    //				for (Parcelable pa : mList) {
    //					Uri uri = (Uri) pa;
    //					if (null != uri) {
    //						String scheme = uri.getScheme();
    //						if (scheme.toString().compareTo("content") == 0) { //content://开头的uri
    //							ContentResolver cr = getContentResolver();
    //							Cursor c = cr.query(uri, null, null, null, null);
    //							c.moveToFirst();
    ////							String filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
    //							String filePath = getRealFilePath(this,uri);
    //							if (!filePath.startsWith("/mnt")) {//检查是否有”/mnt“前缀
    //								filePath = "/mnt" + filePath;
    //							}
    //							filePath = Uri.encode(filePath);
    //							File file = new File(filePath);
    //							System.out.println("文件是否存在**" + file.exists());
    //							mShareFilePathList.add(filePath);
    //							c.close();
    //						} else if (scheme.toString().compareTo("file") == 0) {//file:///开头的uri
    ////							String filePath = uri.toString().replace("file://", "");//替换file://
    //							String filePath = getRealFilePath(this,uri);
    //							if (!filePath.startsWith("/mnt")) {//加上"/mnt"头
    //								filePath = "/mnt" + filePath;
    //							}
    //							filePath = Uri.encode(filePath);
    //							File file = new File(filePath);
    //							System.out.println("文件是否存在**" + file.exists());
    //							mShareFilePathList.add(filePath);
    //						}
    //					}
    //				}
    //			}
    //		}
    //		for (String s : mShareFilePathList) {
    //			Log.d("文件路径*****", s);
    //		}
    //		return mShareFilePathList;
    //	}

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
                                ToastUtil.showToast(ToShareContactsActivity.this, R.string.file_send_exception_toolarge);
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
                                        ToastUtil.showToast(ToShareContactsActivity.this, R.string.file_send_exception_toolarge);
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
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
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
    private void createConversation(final HlContactRenheMember hlContactRenheMember) {
        if (null == hlContactRenheMember)
            return;
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(this, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(hlContactRenheMember.getName());
        com.alibaba.wukong.im.Message message = null; // 创建会话发送的系统消息,可以不设置
        int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                ConversationListUtil.updateChatConversationExtension(conversation, title.toString(), hlContactRenheMember.getUserface());
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Intent intent = new Intent(ToShareContactsActivity.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                intent.putExtra("userName", title.toString());
                intent.putExtra("userFace", hlContactRenheMember.getUserface());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Toast.makeText(ToShareContactsActivity.this, "创建会话失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), hlContactRenheMember.getUserface(), message, convType, Long.parseLong(hlContactRenheMember.getImId() + ""));
    }
}

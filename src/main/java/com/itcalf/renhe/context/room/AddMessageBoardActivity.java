package com.itcalf.renhe.context.room;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.HlEngine;
import com.itcalf.renhe.context.imageselector.ImageSelector;
import com.itcalf.renhe.context.imageselector.ImageSelectorActivity;
import com.itcalf.renhe.context.room.addMessage.Bimp;
import com.itcalf.renhe.context.room.addMessage.FileUtils;
import com.itcalf.renhe.context.room.addMessage.PhotoActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleContacts;
import com.itcalf.renhe.context.wukong.im.kit.ThumbnailUtil;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.imageUtil.ImageSelectorUtil;
import com.itcalf.renhe.utils.DraftUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.KeyboardLayout;
import com.itcalf.renhe.view.KeyboardLayout.onKybdsChangeListener;
import com.itcalf.renhe.view.emoji.EmojiFragment;
import com.itcalf.renhe.view.emoji.EmojiUtil;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Feature:添加留言界面 Desc:添加留言界面
 *
 * @author xp
 */
public class AddMessageBoardActivity extends BaseActivity implements EmojiFragment.OnEmotionSelectedListener {
    // 编辑文本内容
    private EditText mContentEdt;
    private GridView noScrollgridview;
    private GridAdapter adapter;
    private KeyboardLayout rootRl;

    private int countString = TextSize.getInstance().getRenMaiQuanContentSize();
    private int strCount = TextSize.getInstance().getRenMaiQuanContentSize();
    /***********
     * 表情部分
     *****************/
    private EmojiFragment emotionFragment;
    private LinearLayout bottomExpressionLy;
    private ImageView imagefaceIv;
    private LinearLayout chat_face_container;
    private final LayoutTransition transitioner = new LayoutTransition();
    private int emotionHeight;
    private boolean isEmShow = false;
    private EmojiUtil emojiUtil;
    /***********
     * 表情部分END
     *****************/

    private static final int AT_CONTACTS = 99;
    private List<HlContactRenheMember> mReceiverList = new ArrayList<>(); // 存储回调时被@的成员
    /**
     * 所有被@的用户
     */
    private List<HlContactRenheMember> allAtList = new ArrayList<>();
    /**
     * 字符串中@的个数
     */
    private int atCount = 0;

    private String draftkey_content;
    private String draftkey_img;
    private LinearLayout shareToWeichatLl;
    private TextView shareToWeichatStateTv;
    public static final int SHARE_TO_WEICHAT_REQUEST_CODE = 1;

    private ArrayList<String> selectedImagePath = new ArrayList<>();

    private static final int REQUEST_CODE_VIEW_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (countString == 0) {
            countString = Constants.MESSAGELENGTH;
        }
        if (strCount == 0) {
            strCount = Constants.MESSAGELENGTH;
        }
        setMyContentView(R.layout.rooms_addcomment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("写留言"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("写留言"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void findView() {
        super.findView();
        mContentEdt = (EditText) findViewById(R.id.contentEdt);
        rootRl = (KeyboardLayout) findViewById(R.id.rootRl);
        bottomExpressionLy = (LinearLayout) findViewById(R.id.bottom_expression_ll);
        imagefaceIv = (ImageView) findViewById(R.id.image_face);
        //表情布局
        chat_face_container = (LinearLayout) findViewById(R.id.chat_face_container);
        shareToWeichatLl = (LinearLayout) findViewById(R.id.share_to_circle_ll);
        shareToWeichatStateTv = (TextView) findViewById(R.id.share_to_weichat_state_tv);
    }

    MenuItem countItem;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem sendItem = menu.findItem(R.id.item_send);
        sendItem.setTitle("发布");
        sendItem.setVisible(true);
        sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        countItem = menu.findItem(R.id.item_count);
        if (countString > 10) {
            countItem.setVisible(false);
        } else {
            countItem.setVisible(true);
        }
        countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        countItem.setTitle("" + countString);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void initData() {
        super.initData();
        //由发布新留言改为发布新动态
        setTextValue(R.id.title_txt, getResources().getString(R.string.renmaiquan_publish_New_Dynamic));
        //初始化表情
        emotionFragment = (EmojiFragment) getSupportFragmentManager().findFragmentByTag("EmotionFragemnt");
        if (emotionFragment == null) {
            emotionFragment = EmojiFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.chat_face_container, emotionFragment, "EmotionFragemnt").commit();
        }
        emojiUtil = new EmojiUtil(this);
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SystemUtils.getScreenHeight(this), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
        transitioner.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", emotionHeight,
                SystemUtils.getScreenHeight(this)).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        rootRl.setLayoutTransition(transitioner);

        if (RenheApplication.getInstance().getUserSharedPreferences()
                .getBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, false)) {
            shareToWeichatStateTv.setText(getString(R.string.renmaiquan_publish_share_to_weichat_state_on));
        } else {
            shareToWeichatStateTv.setText(getString(R.string.renmaiquan_publish_share_to_weichat_state_off));
        }
        Bimp.initMap();
        noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
        noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new GridAdapter(this);

        //初始化草稿
        getDraft();
        adapter.update();
        noScrollgridview.setAdapter(adapter);
        noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                hideEmotionView(false);

                if (arg2 == Bimp.bmp.size()) {
                    MobclickAgent.onEvent(AddMessageBoardActivity.this, "点击添加图片按钮");
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(arg1.getWindowToken(), 0); //强制隐藏键盘
//                    new PopupWindows(AddMessageBoardActivity.this, noScrollgridview);
                    int maxSize;
                    if (Bimp.bmp.size() < Bimp.MAX_SIZE) {
                        maxSize = Bimp.MAX_SIZE - Bimp.bmp.size();
                    } else {
                        maxSize = 0;
                    }
                    ImageSelectorUtil.initActivityImageSelector(AddMessageBoardActivity.this, selectedImagePath, maxSize);
                } else {
                    Intent intent = new Intent(AddMessageBoardActivity.this, PhotoActivity.class);
                    intent.putExtra("ID", arg2);
                    startHlActivityForResult(intent, REQUEST_CODE_VIEW_PHOTO);
                }
            }
        });
        publishMessageHint();
        handleAppShare();
    }

    private void getDraft() {
        draftkey_content = DraftUtil.createKey(RenheApplication.getInstance().getUserInfo().getSid(), "message");
        String draftStr = DraftUtil.getDraft(DraftUtil.DRAFT_MESSAGE, draftkey_content);
        if (!TextUtils.isEmpty(draftStr)) {
            mContentEdt.setText(emojiUtil.getEmotionSpannedString(null, draftStr));
            mContentEdt.setSelection(draftStr.length());
            countString = strCount - draftStr.length();
        }

        draftkey_img = DraftUtil.createKey(RenheApplication.getInstance().getUserInfo().getSid(), "message", "img");
        Bimp.drr.clear();
        for (int i = 0; i < Bimp.MAX_SIZE; i++) {
            String imgpath = DraftUtil.getDraft(DraftUtil.DRAFT_MESSAGE, draftkey_img + i);
            if (!TextUtils.isEmpty(imgpath)) {
                File file = new File(imgpath);
                if (file.exists())
                    Bimp.drr.add(imgpath);
            } else {
                break;
            }
        }

    }

    private void saveDraft_Content(String str) {
        DraftUtil.saveDraft(DraftUtil.DRAFT_MESSAGE, draftkey_content, str);
    }

    private void saveDraft_Image() {
        for (int i = 0; i < Bimp.MAX_SIZE; i++) {
            if (null != Bimp.drr && i < Bimp.drr.size()) {
                String imgpath = Bimp.drr.get(i);
                DraftUtil.putDraft(DraftUtil.DRAFT_MESSAGE, draftkey_img + i, imgpath);
            } else {
                DraftUtil.removeDraft(DraftUtil.DRAFT_MESSAGE, draftkey_img + i);
            }
        }
    }

    private void clearDraft() {
        DraftUtil.clearDraft(DraftUtil.DRAFT_MESSAGE);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
            default:
                return null;
        }
    }

    private void goBack() {
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    private void goSave() {
        //隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        MobclickAgent.onEvent(AddMessageBoardActivity.this, "add_newrenmaiquanmsg");
        StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu1_publish_notice_process_click), 0, "", null);

        final String content = mContentEdt.getText().toString().trim();
        if (!TextUtils.isEmpty(content)) {
            // 添加新留言，调用服务端接口
            String message = mContentEdt.getText().toString();
            List<MessageBoards.AtMemmber> atMemmberList = new ArrayList<>();
            if (allAtList != null && allAtList.size() > 0) {
                for (int i = 0; i < allAtList.size(); i++) {
                    String atName = "@" + allAtList.get(i).getName();
                    String atId = allAtList.get(i).getSid();
                    //如果留言中包含有被@的用户，取出对应的id上传
                    if (message.contains(atName)) {
                        //判断文本中@name 出现的次数与list中相等 或者 是同一个id下面的name
                        if (sameNameCount(allAtList, atName) == findAtCount(atName, message)
                                || sameNameAndDifIdCount(allAtList, atName, atId) < 2) {
//                            atMembers = atMembers + atId + ":" + allAtList.get(i).getName() + ";";
                            MessageBoards.AtMemmber atMemmber = new MessageBoards.AtMemmber();
                            atMemmber.setMemberName(allAtList.get(i).getName());
                            atMemmber.setMemberSid(allAtList.get(i).getSid());
                            atMemmberList.add(atMemmber);
                        }
                    }
                }
            }
            //初始化at的人
            MessageBoards.AtMemmber[] atMemmberArrays = null;
            if (atMemmberList.size() > 0) {
                atMemmberArrays = new MessageBoards.AtMemmber[atMemmberList.size()];
                for (int i = 0; i < atMemmberArrays.length; i++) {
                    atMemmberArrays[i] = atMemmberList.get(i);
                }
            }
            //初始化图片
            MessageBoards.PicList[] picArrays = null;
            List<String> picList = new ArrayList<>();
            for (int i = 0; i < Bimp.drr.size(); i++) {
                String Str = Bimp.drr.get(i).substring(Bimp.drr.get(i).lastIndexOf("/") + 1,
                        Bimp.drr.get(i).lastIndexOf("."));
                String fileType = Bimp.drr.get(i).substring(Bimp.drr.get(i).lastIndexOf("."));
                picList.add(FileUtils.SDPATH + Str + fileType);
            }
            if (picList.size() > 0 && null != Bimp.bmp) {
                picArrays = new MessageBoards.PicList[picList.size()];
                for (int i = 0; i < picArrays.length; i++) {
                    MessageBoards.PicList picList1 = new MessageBoards.PicList();
                    picList1.setThumbnailPicUrl(picList.get(i));
                    picList1.setBmiddlePicUrl(picList.get(i));
                    picList1.setBmiddlePicWidth(Bimp.bmp.get(i).getWidth());
                    picList1.setBmiddlePicHeight(Bimp.bmp.get(i).getHeight());
                    picArrays[i] = picList1;
                }
            }
            Bimp.resetMap();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearDraft();
                }
            }, 1000);
            //初始化新增的人脉圈
            MessageBoards.NewNoticeList addNewNoticeItem = initAddNewMsg(content, atMemmberArrays, picArrays);
            Intent intent = new Intent();
            intent.putExtra("addNewNoticeItem", addNewNoticeItem);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        } else {
            //留言不能为空改为动态不能为空
            ToastUtil.showToast(AddMessageBoardActivity.this, getResources().getString(R.string.renmaiquan_send));
        }
    }

    private MessageBoards.NewNoticeList initAddNewMsg(String content, MessageBoards.AtMemmber[] atMemmbers, MessageBoards.PicList[] picLists) {
        MessageBoards.NewNoticeList addNewNoticeListItem = new MessageBoards.NewNoticeList();
        MessageBoards.SenderInfo senderInfo = new MessageBoards.SenderInfo();
        senderInfo.setName(RenheApplication.getInstance().getUserInfo().getName());
        senderInfo.setUserface(RenheApplication.getInstance().getUserInfo().getUserface());
        senderInfo.setAccountType(RenheApplication.getInstance().getUserInfo().getAccountType());
        senderInfo.setCompany(RenheApplication.getInstance().getUserInfo().getCompany());
        senderInfo.setIndustry(RenheApplication.getInstance().getUserInfo().getIndustry());
        senderInfo.setLocation(RenheApplication.getInstance().getUserInfo().getLocation());
        senderInfo.setRealname(RenheApplication.getInstance().getUserInfo().isRealName());
        senderInfo.setSid(RenheApplication.getInstance().getUserInfo().getSid());
        senderInfo.setTitle(RenheApplication.getInstance().getUserInfo().getTitle());
        addNewNoticeListItem.setSenderInfo(senderInfo);

        MessageBoards.ContentInfo contentInfo = new MessageBoards.ContentInfo();
        contentInfo.setId(0);
        contentInfo.setObjectId(System.currentTimeMillis() + "");
        contentInfo.setLikedList(null);
        contentInfo.setLiked(false);
        contentInfo.setReplyList(null);
        contentInfo.setReplyNum(0);
        contentInfo.setMembers(null);
        contentInfo.setForwardMessageBoardInfo(null);
        contentInfo.setContent(content);
        contentInfo.setPicList(picLists);
        contentInfo.setAtMembers(atMemmbers);
        addNewNoticeListItem.setContentInfo(contentInfo);
        addNewNoticeListItem.setType(MessageBoards.MESSAGE_TYPE_ADD_NEWMSG);
        addNewNoticeListItem.setRank(Integer.MAX_VALUE);
        addNewNoticeListItem.setCreatedDate(System.currentTimeMillis());
        addNewNoticeListItem.setPackUpState(0);
        return addNewNoticeListItem;
    }

    /**
     * 寻找相同name，不同id的个数
     *
     * @param allAtList
     * @param atName
     * @return
     */
    public static int sameNameCount(List<HlContactRenheMember> allAtList, String atName) {
        int againNames = 0;
        if (allAtList != null && allAtList.size() > 0) {
            for (int j = 0; j < allAtList.size(); j++) {
                if (atName.equals(allAtList.get(j).getName())) {
                    againNames++;
                }
            }
        }
        return againNames;
    }

    public static int sameNameAndDifIdCount(List<HlContactRenheMember> allAtList, String atName, String atId) {
        int againNames = 0;
        if (allAtList != null && allAtList.size() > 0) {
            for (int j = 0; j < allAtList.size(); j++) {
                if (atName.equals(allAtList.get(j).getName()) && !atId.equals(allAtList.get(j).getSid())) {
                    againNames++;
                }
            }
        }
        return againNames;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            case R.id.item_send:
                goSave();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initListener() {
        super.initListener();
        //输入内容长度限制，表情过滤
        mContentEdt.setFilters(new InputFilter[]{emojiUtil.emotionFilter, new LengthFilter(strCount)});
        // 监听文本内容变化事件
        mContentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > strCount) {
                    return;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                atCount = findAtCount("@", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                countString = strCount - s.length();
                //invalidateOptionsMenu();
                if (countString <= 10) {
                    countItem.setVisible(true);
                    countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    countItem.setTitle("" + countString);
                } else {
                    countItem.setVisible(false);
                }
                saveDraft_Content(s.toString());
                //获取光标前的字符串
                String str = s.toString().substring(0, mContentEdt.getSelectionStart());
                //当前字符串所有@
                int at = findAtCount("@", s.toString());
                if (at > atCount && str.endsWith("@")) {
                    Intent i = new Intent(AddMessageBoardActivity.this, ActivityCircleContacts.class);
                    i.putExtra(Constants.SELECT_CONTACT_TYPE_KEY, Constants.SELECT_CONTACT_TYPE.SELECT_CONTACT_FOR_AT);
                    startHlActivityForResult(i, AT_CONTACTS);
                }
            }
        });
        //点击事件处理，切换输入法与表情
        mContentEdt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hideEmotionView(true);
            }
        });

        emotionFragment.setOnEmotionListener(this);
        //表情点击按钮 ，点击表情与输入法间进行切换
        imagefaceIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (chat_face_container.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView(SystemUtils.isKeyBoardShow(AddMessageBoardActivity.this));
                }
                mContentEdt.requestFocus();
            }
        });

        /*****检测软键盘是否弹起，消失*****/
        rootRl.setOnkbdStateListener(new onKybdsChangeListener() {
            @Override
            public void onKeyBoardStateChange(int state, int keyboardHeight) {
                switch (state) {
                    case KeyboardLayout.KEYBOARD_STATE_HIDE:
                        if (!isEmShow) {
                            bottomExpressionLy.setVisibility(View.GONE);
                        }
                        break;
                    case KeyboardLayout.KEYBOARD_STATE_SHOW:
                        bottomExpressionLy.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        shareToWeichatLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ShareToWeichatSettingActivity.class, SHARE_TO_WEICHAT_REQUEST_CODE);
            }
        });
    }

    //显示表情键盘
    private void showEmotionView(boolean showAnimation) {
        if (showAnimation) {
            transitioner.setDuration(200);
        } else {
            transitioner.setDuration(0);
        }
        emotionHeight = SystemUtils.getKeyboardHeight(this);
        SystemUtils.hideSoftInput(mContentEdt);
        isEmShow = true;
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal_on));
        chat_face_container.getLayoutParams().height = emotionHeight;
        chat_face_container.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //隐藏表情键盘
    private void hideEmotionView(boolean showKeyBoard) {
        if (showKeyBoard) {
            if (!bottomExpressionLy.isShown()) {
                bottomExpressionLy.setVisibility(View.VISIBLE);
            }
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            SystemUtils.showKeyBoard(mContentEdt);
        } else {
            if (bottomExpressionLy.isShown()) {
                bottomExpressionLy.setVisibility(View.GONE);
            }
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        isEmShow = false;
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
        chat_face_container.setVisibility(View.GONE);
    }

    /**
     * 表情的点击事件
     *
     * @param emotion
     */
    @Override
    public void onEmotionSelected(Emotion emotion) {
        if (null != emotion)
            emojiUtil.onEmotionSelected(emotion, mContentEdt);
    }

    /**
     * @description 查询字符串中指定字符@出现的个数
     * @author Chan
     * @date 2015-3-3
     */
    public static int findAtCount(String select, String str) {
        int c = 0;
        Pattern p = Pattern.compile(select, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            c++;
        }
        return c;

    }

    @SuppressLint("HandlerLeak")
    public class GridAdapter extends BaseAdapter {
        private LayoutInflater inflater; // 视图容器
        private int selectedPosition = -1;// 选中的位置
        private boolean shape;

        public boolean isShape() {
            return shape;
        }

        public void setShape(boolean shape) {
            this.shape = shape;
        }

        public GridAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public void update() {
            loading();
        }

        public int getCount() {
            if (null != Bimp.bmp) {
                if (Bimp.bmp.size() >= Bimp.MAX_SIZE) {
                    return Bimp.MAX_SIZE;
                } else {
                    return (Bimp.bmp.size() + 1);
                }
            } else {
                return 0;
            }

        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public void setSelectedPosition(int position) {
            selectedPosition = position;
        }

        public int getSelectedPosition() {
            return selectedPosition;
        }

        /**
         * ListView Item设置
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            final int coord = position;
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_published_grida, parent, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == Bimp.bmp.size()) {
                //				holder.image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_addpic_unfocused));
                holder.image.setImageResource(R.drawable.icon_addpic_unfocused);
                if (position == Bimp.MAX_SIZE) {
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                holder.image.setImageBitmap(Bimp.bmp.get(position));
            }

            return convertView;
        }

        public class ViewHolder {
            public ImageView image;
        }

        Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        adapter.notifyDataSetChanged();
                        break;
                }
                super.handleMessage(msg);
            }
        };

        public void loading() {
            new Thread(new Runnable() {
                public void run() {
                    saveDraft_Image();
                    while (true) {
                        if (null != Bimp.drr) {
                            if (Bimp.max == Bimp.drr.size()) {
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                break;
                            } else {
                                try {
                                    String path = Bimp.drr.get(Bimp.max);
//                                    Bitmap bm = Bimp.revitionImageSize(path);
                                    Bitmap bm = ThumbnailUtil.compressAndRotateToThumbFileWithdestinationPath(AddMessageBoardActivity.this, path, FileUtils.SDPATH);
                                    Bimp.bmp.add(bm);
//                                    String newStr = fullPath.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
//                                    FileUtils.saveBitmap(bm, "" + newStr);
                                    Bimp.max += 1;
                                    Message message = new Message();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            break;
                        }
                    }
                }
            }).start();
        }
    }

    public String getString(String s) {
        String path = null;
        if (s == null)
            return "";
        for (int i = s.length() - 1; i > 0; i++) {
            s.charAt(i);
        }
        return path;
    }

    protected void onRestart() {
        selectedImagePath.clear();
        super.onRestart();
    }

    private static final int TAKE_PICTURE = 10;
    private String path = "";

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PICTURE:
                if (Bimp.drr.size() < Bimp.MAX_SIZE && resultCode == -1) {
                    Bimp.drr.add(path);
                }
                break;
            case AT_CONTACTS:
                if (data != null) {
                    String atMembs = "";
                    mReceiverList = (List<HlContactRenheMember>) data.getSerializableExtra("contacts");
                    if (mReceiverList.size() > 0) {
                        for (int i = 0; i < mReceiverList.size(); i++) {
                            atMembs = atMembs + "@" + mReceiverList.get(i).getName() + " ";
                        }
                        if (atMembs.length() > 0) {
                            atMembs = atMembs.substring(1, atMembs.length());
                        }
                        //在光标处插入字符
                        int index = mContentEdt.getSelectionStart();
                        Editable editable = mContentEdt.getText();
                        editable.insert(index, atMembs);
                        allAtList.addAll(mReceiverList);//把每次@返回过来的数据都添加到list里面
                    }
                }
                break;
            case SHARE_TO_WEICHAT_REQUEST_CODE:
                if (RenheApplication.getInstance().getUserSharedPreferences()
                        .getBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, false)) {
                    shareToWeichatStateTv.setText(getString(R.string.renmaiquan_publish_share_to_weichat_state_on));
                } else {
                    shareToWeichatStateTv.setText(getString(R.string.renmaiquan_publish_share_to_weichat_state_off));
                }
                break;
            case ImageSelector.IMAGE_REQUEST_CODE:
                if (null != data) {
                    List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
                    for (String path : pathList) {
                        Logger.d("ImagePathList-->" + path);
                    }
                    selectedImagePath.clear();
                    selectedImagePath.addAll(pathList);
                    Bimp.drr.addAll(selectedImagePath);
                    adapter.update();
//                    selectedImagePath.clear();
                }
                break;
            case REQUEST_CODE_VIEW_PHOTO:
                if (resultCode == RESULT_OK)
                    adapter.update();
                break;
        }
    }

    //返回键
    @Override
    public void onBackPressed() {
        if (bottomExpressionLy.isShown()) {
            bottomExpressionLy.setVisibility(View.GONE);
        } else {
            goBack();
        }
    }

    /**
     * 提示人脉圈发布基本原则
     */
    private void publishMessageHint() {
        SharedPreferences prefs = getSharedPreferences("first_guide_setting_info", 0);
        if (!prefs.getBoolean("publishmessage_hint", true))
            return;
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder(R.string.publishmessage_hint_title, R.string.publishmessage_hint, R.string.hint_iknow)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).cancelable(false);
        materialDialogsUtil.show();
        prefs.edit().putBoolean("publishmessage_hint", false).commit();
    }

    private void handleAppShare() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {//外部应用分享入口
            intent.putExtra("appshareTo", Constants.APP_SHARE_TO.APP_SHARE_TO_RENMAIQUAN);
            new HlEngine(this, intent).init();
            if ("text/plain".equals(type)) {//外部应用分享文本
                if (Constants.DEBUG_MODE) {
                    Log.d("接收外部应用分享", "文本");
                }
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                //				String sharedTitle = intent.getStringExtra(Intent.EXTRA_TITLE);
                if (!TextUtils.isEmpty(sharedText)) {
                    mContentEdt.setText(sharedText);
                }
            }
        }
    }
}

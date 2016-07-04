package com.itcalf.renhe.context.room;

import android.animation.LayoutTransition;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleContacts;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.KeyboardLayout;
import com.itcalf.renhe.view.KeyboardLayout.onKybdsChangeListener;
import com.itcalf.renhe.view.emoji.EmojiFragment;
import com.itcalf.renhe.view.emoji.EmojiUtil;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Feature: 转发留言界面 Desc:转发留言界面
 *
 * @author xp
 */
public class ForwardMessageBoardActivity extends BaseActivity implements EmojiFragment.OnEmotionSelectedListener {
    private EditText mContentEdt;
    private String mObjectId;
    private boolean isModify = false;
    private ImageView toForwardIv;
    private TextView toForwardTv;
    private String toForwardContent;
    private String toFrowardPic;
    private int shareType = Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_NORMAL;//分享类型，默认是“正常转发人脉圈”的类型
    private int shareId;
    //@ 代码
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
    private MenuItem countItem;
    private int countString = Constants.MESSAGELENGTH;
    private int strCount = Constants.MESSAGELENGTH;
    /***********
     * 表情部分
     *****************/
    private KeyboardLayout rootRl;
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
    private TextView dialogTitleTv;
    private View seleparateLineView;
    private TextView contentTitleTv;
    private TextView contentOtherTv;
    private String title;
    private String contentTitle;
    private String contentOther;
    private String circleId;//分享圈子
    private String sid;//分享名片
    private ImageView circleSharePicIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.rooms_forwardcomment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("转发留言"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("转发留言"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        mContentEdt = (EditText) findViewById(R.id.contentEdt);
        toForwardIv = (ImageView) findViewById(R.id.froward_pic_iv);
        toForwardTv = (TextView) findViewById(R.id.forward_content_tv);

        rootRl = (KeyboardLayout) findViewById(R.id.rootRl);

        bottomExpressionLy = (LinearLayout) findViewById(R.id.bottom_expression_ll);
        imagefaceIv = (ImageView) findViewById(R.id.image_face);
        //表情布局
        chat_face_container = (LinearLayout) findViewById(R.id.chat_face_container);

        dialogTitleTv = (TextView) findViewById(R.id.dialog_title);
        seleparateLineView = findViewById(R.id.seperate_line);
        contentTitleTv = (TextView) findViewById(R.id.forward_title_tv);
        contentOtherTv = (TextView) findViewById(R.id.forward_other_tv);
        circleSharePicIv = (ImageView) findViewById(R.id.circle_sharePic);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "分享到人脉圈");
        mObjectId = getIntent().getExtras().getString("objectId");
        String sender = getIntent().getExtras().getString("sender");
        String rawContent = getIntent().getExtras().getString("rawContent");

        //表情部分初始化
        emotionFragment = (EmojiFragment) getSupportFragmentManager().findFragmentByTag("EmotionFragemnt");
        if (emotionFragment == null) {
            emotionFragment = EmojiFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.chat_face_container, emotionFragment, "EmotionFragemnt").commit();
        }
        emojiUtil = new EmojiUtil(this);

        if (!TextUtils.isEmpty(sender) && !TextUtils.isEmpty(rawContent)) {
            mContentEdt.append("//@" + sender + ":" + rawContent);
            mContentEdt.setText(emojiUtil.getEmotionSpannedString(null, mContentEdt.getText().toString()));
        } else {
            mContentEdt.setHint("");
        }
        contentTitle = getIntent().getExtras().getString("contentTitle");
        title = getIntent().getExtras().getString("title");
        contentOther = getIntent().getExtras().getString("contentOther");
        if (!TextUtils.isEmpty(title)) {
            dialogTitleTv.setText(title);
        } else {
            dialogTitleTv.setVisibility(View.GONE);
            seleparateLineView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(contentTitle)) {
            contentTitleTv.setText(contentTitle);
        } else {
            contentTitleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(contentOther)) {
            contentOtherTv.setText(contentOther);
        } else {
            contentOtherTv.setVisibility(View.GONE);
        }
        toForwardContent = getIntent().getExtras().getString("toForwardContent");
        toFrowardPic = getIntent().getExtras().getString("toForwardPic");
        shareType = getIntent().getExtras().getInt("shareType", Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_NORMAL);
        shareId = getIntent().getExtras().getInt("shareId", -1);
        circleId = getIntent().getExtras().getString("circleId");
        sid = getIntent().getExtras().getString("sid");
        if (!TextUtils.isEmpty(toForwardContent)) {
            toForwardTv.setText(emojiUtil.getEmotionSpannedString(null, toForwardContent));
        } else {
            toForwardIv.setVisibility(View.GONE);
            circleSharePicIv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(title) && (title.contains(getString(R.string.cicle_share_default_name))
                || title.contains(getString(R.string.vcard_share_default_name)))) {
            if (!TextUtils.isEmpty(toForwardContent) || !TextUtils.isEmpty(title)) {
                toForwardIv.setVisibility(View.GONE);
                circleSharePicIv.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(toFrowardPic)) {
                    circleSharePicIv.setImageResource(R.drawable.avatar);
                } else {
                    ImageLoader imageLoader4 = ImageLoader.getInstance();
                    try {
                        imageLoader4.displayImage(toFrowardPic, circleSharePicIv, CacheManager.options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                toForwardIv.setVisibility(View.GONE);
                circleSharePicIv.setVisibility(View.GONE);
            }

        } else {
            if (!TextUtils.isEmpty(toForwardContent)) {
                toForwardIv.setVisibility(View.VISIBLE);
                circleSharePicIv.setVisibility(View.GONE);
                if (TextUtils.isEmpty(toFrowardPic)) {
                    toForwardIv.setImageResource(R.drawable.chat_link_default);
                } else {
                    try {
                        ImageLoader.getInstance().displayImage(toFrowardPic, toForwardIv, CacheManager.imageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                toForwardIv.setVisibility(View.GONE);
                circleSharePicIv.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendItem = menu.findItem(R.id.item_send);
        sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        sendItem.setVisible(true);
        countItem = menu.findItem(R.id.item_count);
        countItem.setVisible(true);
        countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            case R.id.item_send:
                MobclickAgent.onEvent(ForwardMessageBoardActivity.this, "forward_send");
                goSave();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.sharing).cancelable(false).build();
            default:
                return null;
        }
    }

    private void goBack() {
        if (isModify) {
            MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
            materialDialogsUtil.getBuilder(R.string.renmaiquan_share_message_title_tip, R.string.renmaiquan_share_message_content_tip, R.string.material_dialog_share, R.string.material_dialog_cancel, R.string.material_dialog_giveup).callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    goSave();
                }

                @Override
                public void onNeutral(MaterialDialog dialog) {
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                }
            }).cancelable(false);
            materialDialogsUtil.show();
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    private void goSave() {
        String content = mContentEdt.getText().toString().trim();
        String atMembers = "";
        String message = mContentEdt.getText().toString();
        if (allAtList != null && allAtList.size() > 0) {
            for (int i = 0; i < allAtList.size(); i++) {
                String atName = "@" + allAtList.get(i).getName();
                String atId = allAtList.get(i).getSid();
                //如果留言中包含有被@的用户，取出对应的id上传
                if (message.contains(atName)) {
                    //判断文本中@name 出现的次数与list中相等 或者 是同一个id下面的name
                    if (AddMessageBoardActivity.sameNameCount(allAtList, atName) == AddMessageBoardActivity
                            .findAtCount(atName, message)
                            || AddMessageBoardActivity.sameNameAndDifIdCount(allAtList, atName, atId) < 2) {
                        atMembers = atMembers + atId + ":" + allAtList.get(i).getName() + ";";
                    }
                }
            }
            if (atMembers.endsWith(";")) {
                atMembers = atMembers.substring(0, atMembers.length() - 1);
            }
        }
        switch (shareType) {
            case Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_NORMAL:
                shareNormalToRenmaiquan(content, atMembers);
                break;
            case Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_WEB:
                shareWebViewToRenmaiquan(content, atMembers);
                break;
            case Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_CIRCLE:
                shareCircleToRenmaiquan(content, atMembers);
                break;
            case Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_PROFILE:
                shareProfileToRenmaiquan(content, atMembers);
                break;

            default:
                break;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mContentEdt.setFilters(new InputFilter[]{emojiUtil.emotionFilter, new InputFilter.LengthFilter(Constants.MESSAGELENGTH)});
        mContentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > Constants.MESSAGELENGTH) {
                    return;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                atCount = AddMessageBoardActivity.findAtCount("@", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                countString = strCount - s.length();
                if (countString <= 10) {
                    countItem.setVisible(true);
                    countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    countItem.setTitle("" + countString);
                } else {
                    countItem.setVisible(false);
                }
                if (s.length() > 0) {
                    isModify = true;
                } else {
                    isModify = false;
                }
                //获取光标前的字符串
                String str = s.toString().substring(0, mContentEdt.getSelectionStart());
                //当前字符串所有@
                int at = AddMessageBoardActivity.findAtCount("@", s.toString());
                if (at > atCount && str.endsWith("@")) {
                    Intent i = new Intent(ForwardMessageBoardActivity.this, ActivityCircleContacts.class);
                    i.putExtra(Constants.SELECT_CONTACT_TYPE_KEY, Constants.SELECT_CONTACT_TYPE.SELECT_CONTACT_FOR_AT);
                    startActivityForResult(i, AT_CONTACTS);
                }
            }
        });
        Selection.setSelection(mContentEdt.getEditableText(), 0);

        //点击事件处理，切换输入法与表情
        mContentEdt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hideEmotionView(true);
            }
        });

        //表情点击按钮 ，点击表情与输入法间进行切换
        imagefaceIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (chat_face_container.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView(SystemUtils.isKeyBoardShow(ForwardMessageBoardActivity.this));
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

        emotionFragment.setOnEmotionListener(this);
    }

    @Override
    public void onEmotionSelected(Emotion emotion) {
        if (null != emotion)
            emojiUtil.onEmotionSelected(emotion, mContentEdt);
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

    //返回键
    @Override
    public void onBackPressed() {
        if (bottomExpressionLy.getVisibility() == View.VISIBLE) {
            bottomExpressionLy.setVisibility(View.GONE);
        } else {
            goBack();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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
        }
    }

    private void shareWebViewToRenmaiquan(String content, String atMembers) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return getRenheApplication().getMessageBoardCommand().shareWebToRenmaiquan(params[0], params[1],
                            ForwardMessageBoardActivity.this, params[2], params[3], params[4]);
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog(1);
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                removeDialog(1);
                if (null != result) {
                    if (1 == result.getState()) {
                        ToastUtil.showToast(ForwardMessageBoardActivity.this, "分享成功");
                        removeDialog(1);
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else {
                        ToastUtil.showErrorToast(ForwardMessageBoardActivity.this, "分享失败");
                    }
                } else {
                    ToastUtil.showNetworkError(ForwardMessageBoardActivity.this);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), shareId + "", content, atMembers);

    }

    private void shareNormalToRenmaiquan(String content, String atMembers) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return getRenheApplication().getMessageBoardCommand().forwardMessageBoard(params[0], params[1], params[2],
                            params[3], params[4], ForwardMessageBoardActivity.this);
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog(1);
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                removeDialog(1);
                if (null != result) {
                    if (1 == result.getState()) {
                        ToastUtil.showToast(ForwardMessageBoardActivity.this, "分享成功");
                        removeDialog(1);
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else {
                        ToastUtil.showErrorToast(ForwardMessageBoardActivity.this, "分享失败");
                    }
                } else {
                    ToastUtil.showNetworkError(ForwardMessageBoardActivity.this);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), mObjectId, content, atMembers);

    }

    private void shareCircleToRenmaiquan(String content, String atMembers) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return getRenheApplication().getMessageBoardCommand().shareCircleToRenmaiquan(params[0], params[1],
                            ForwardMessageBoardActivity.this, params[2], params[3], params[4]);
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog(1);
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                removeDialog(1);
                if (null != result) {
                    if (1 == result.getState()) {
                        ToastUtil.showToast(ForwardMessageBoardActivity.this, "分享成功");
                        removeDialog(1);
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else {
                        ToastUtil.showErrorToast(ForwardMessageBoardActivity.this, "分享失败");
                    }
                } else {
                    ToastUtil.showNetworkError(ForwardMessageBoardActivity.this);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), circleId + "", content, atMembers);

    }

    private void shareProfileToRenmaiquan(String content, String atMembers) {
        new AsyncTask<String, Void, MessageBoardOperation>() {
            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    return getRenheApplication().getMessageBoardCommand().shareProfileToRenmaiquan(params[0], params[1],
                            ForwardMessageBoardActivity.this, params[2], params[3], params[4]);
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog(1);
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                removeDialog(1);
                if (null != result) {
                    if (1 == result.getState()) {
                        ToastUtil.showToast(ForwardMessageBoardActivity.this, "分享成功");
                        removeDialog(1);
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else {
                        ToastUtil.showErrorToast(ForwardMessageBoardActivity.this, "分享失败");
                    }
                } else {
                    ToastUtil.showNetworkError(ForwardMessageBoardActivity.this);
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), sid + "", content, atMembers);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mContentEdt.getWindowToken(), 0);
    }
}

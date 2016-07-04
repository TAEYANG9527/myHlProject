package com.itcalf.renhe.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.pay.UpgradeDialog;
import com.itcalf.renhe.dto.ConversationItem;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DialogUtil {
    private Context context;
    private String content = "";
    private MyDialogClickListener myDialogClickListener;
    private ShareMyDialogClickListener myShareDialogClickListener;
    public final static int SURE_BUTTON = 1;
    public final static int CANCLE_BUTTON = 0;

    private Conversation conversation;
    private ConversationItem conversationItem;
    private HlContactRenheMember hlContactRenheMember;

    /**
     * 自定义dialog 按钮监听
     *
     * @author Renhe
     */
    public interface MyDialogClickListener {
        public void onclick(int id);
    }

    public interface ShareMyDialogClickListener {

        public void onclick(int id, String msg, Conversation conversation);

        public void onclick(int id, String msg, ConversationItem conversationItem);

        public void onclick(int id, String msg, HlContactRenheMember hlContactRenheMember);

        public void onclick(int id, String msg);
    }

    public DialogUtil(Context context, MyDialogClickListener myDialogClickListener) {
        this.context = context;
        this.myDialogClickListener = myDialogClickListener;
    }

    public DialogUtil(Context context, ShareMyDialogClickListener myShareDialogClickListener) {
        this.context = context;
        this.myShareDialogClickListener = myShareDialogClickListener;
    }

    /**
     * 创建对话框
     */
    //	private AlertDialog mAlertDialog;
    private Dialog mAlertDialog;
    private TextView dialogTitleTv;
    private View seleparateLineView;
    private Button sureButton;
    private Button cancleButton;
    private TextView contentTv;
    private ImageView picIv;
    private ImageView circleSharePicIv;
    private EditText msgEt;
    private TextView contentTitleTv;
    private TextView otherTv;

    /**
     * @param context
     * @param title
     * @param cancleString
     * @param sureString
     * @param content
     */
    public void createDialog(Context context, String title, String cancleString, String sureString, String content) {
        this.context = context;
        this.content = content;
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(com.itcalf.renhe.R.layout.register_dialog,
                null);

        //		Builder mDialog = new AlertDialog.Builder(context);
        //		mAlertDialog = mDialog.create();
        //		mAlertDialog.setView(view, 0, 0, 0, 0);
        mAlertDialog = new Dialog(context, R.style.TranslucentUnfullwidthWinStyle);
        mAlertDialog.setContentView(view);
        mAlertDialog.setCanceledOnTouchOutside(true);

        dialogTitleTv = (TextView) view.findViewById(R.id.dialog_title);
        seleparateLineView = view.findViewById(R.id.seperate_line);
        sureButton = (Button) view.findViewById(com.itcalf.renhe.R.id.dialog_sure_bt);
        cancleButton = (Button) view.findViewById(R.id.dialog_cancle_bt);
        contentTv = (TextView) view.findViewById(R.id.content_tv);
        if (!TextUtils.isEmpty(title)) {
            dialogTitleTv.setText(title);
        } else {
            dialogTitleTv.setVisibility(View.GONE);
            seleparateLineView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(cancleString)) {
        } else {
            cancleButton.setVisibility(View.GONE);
        }
        sureButton.setText(sureString);
        cancleButton.setText(cancleString);
        contentTv.setText(content);
        mAlertDialog.setCanceledOnTouchOutside(true);
        mAlertDialog.show();
        sureButton.setOnClickListener(new ButtonListener());
        cancleButton.setOnClickListener(new ButtonListener());
    }

    public void createShareDialogWithConversation(Context context, String title, String cancleString, String sureString, String content,
                                                  String picUrl, String contentTitle, String contentOther, Conversation mConversation) {
        this.conversation = mConversation;
        createShareDialog(context, title, cancleString, sureString, content, picUrl, contentTitle, contentOther);
    }

    public void createShareDialog(Context context, String title, String cancleString, String sureString, String content,
                                  String picUrl, String contentTitle, String contentOther, ConversationItem mConversationItem) {
        this.conversationItem = mConversationItem;
        createShareDialog(context, title, cancleString, sureString, content, picUrl, contentTitle, contentOther);
    }

    public void createShareDialogWithContacts(Context context, String title, String cancleString, String sureString, String content,
                                              String picUrl, String contentTitle, String contentOther, HlContactRenheMember hlContactRenheMember) {
        this.hlContactRenheMember = hlContactRenheMember;
        createShareDialog(context, title, cancleString, sureString, content, picUrl, contentTitle, contentOther);
    }

    public void createShareDialog(Context context, String title, String cancleString, String sureString, String content,
                                  String picUrl, String contentTitle, String contentOther) {
        this.context = context;
        this.content = content;
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context)
                .inflate(com.itcalf.renhe.R.layout.share_tofriend_dialog, null);

        mAlertDialog = new Dialog(context, R.style.TranslucentUnfullwidthWinStyle);
        mAlertDialog.setContentView(view);
        mAlertDialog.setCanceledOnTouchOutside(true);

        dialogTitleTv = (TextView) view.findViewById(R.id.dialog_title);
        seleparateLineView = view.findViewById(R.id.seperate_line);
        sureButton = (Button) view.findViewById(com.itcalf.renhe.R.id.dialog_sure_bt);
        cancleButton = (Button) view.findViewById(R.id.dialog_cancle_bt);
        contentTv = (TextView) view.findViewById(R.id.forward_content_tv);
        contentTitleTv = (TextView) view.findViewById(R.id.forward_title_tv);
        otherTv = (TextView) view.findViewById(R.id.forward_other_tv);
        picIv = (ImageView) view.findViewById(R.id.froward_pic_iv);
        circleSharePicIv = (ImageView) view.findViewById(R.id.circle_sharePic);
        msgEt = (EditText) view.findViewById(R.id.contentEdt);

        if (!TextUtils.isEmpty(title)) {
            dialogTitleTv.setText(title);
        } else {
            dialogTitleTv.setVisibility(View.GONE);
            seleparateLineView.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(cancleString)) {
        } else {
            cancleButton.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(content)) {
            contentTv.setText(ContentUtil.getNoAtSpannedString(context, null, content));
        } else {
            contentTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(contentTitle)) {
            contentTitleTv.setText(contentTitle);
        } else {
            contentTitleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(contentOther)) {
            otherTv.setText(contentOther);
        } else {
            otherTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(picUrl)) {
            picIv.setVisibility(View.GONE);
            circleSharePicIv.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(title) && (title.contains(context.getString(R.string.cicle_share_default_name))
                    || title.contains(context.getString(R.string.vcard_share_default_name)))) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                try {
                    imageLoader.displayImage(picUrl, circleSharePicIv, CacheManager.options,
                            CacheManager.imageAnimateFirstDisplayListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                picIv.setVisibility(View.VISIBLE);
                circleSharePicIv.setVisibility(View.GONE);
                ImageLoader imageLoader = ImageLoader.getInstance();
                try {
                    imageLoader.displayImage(picUrl, picIv, CacheManager.imageOptions,
                            CacheManager.imageAnimateFirstDisplayListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        sureButton.setText(sureString);
        cancleButton.setText(cancleString);
        contentTv.setText(content);
        mAlertDialog.setCanceledOnTouchOutside(true);
        mAlertDialog.show();
        msgEt.requestFocus();//获取焦点
        handler.sendEmptyMessageDelayed(0, 0);

        sureButton.setOnClickListener(new ShareButtonListener());
        cancleButton.setOnClickListener(new ShareButtonListener());
    }

    /*发送文件*/
    public void createSendFileDialogWithConversation(Context context, String fileName, String fileSize, String fileType, Uri picUri,
                                                     int shareType, Conversation mConversation) {
        this.conversation = mConversation;
        createSendFileDialog(context, fileName, fileSize, fileType, picUri, shareType);
    }

    /*发送文件*/
    public void createSendFileDialog(Context context, String fileName, String fileSize, String fileType, Uri picUri,
                                     int shareType, ConversationItem mConversationItem) {
        this.conversationItem = mConversationItem;
        createSendFileDialog(context, fileName, fileSize, fileType, picUri, shareType);
    }

    /*发送文件*/
    public void createSendFileDialogWithContact(Context context, String fileName, String fileSize, String fileType, Uri picUri,
                                                int shareType, HlContactRenheMember hlContactRenheMember) {
        this.hlContactRenheMember = hlContactRenheMember;
        createSendFileDialog(context, fileName, fileSize, fileType, picUri, shareType);
    }

    /*发送文件*/
    public void createSendFileDialog(Context context, String fileName, String fileSize, String fileType, Uri picUri,
                                     int shareType) {
        this.context = context;
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.send_file_dialog, null);

        mAlertDialog = new Dialog(context, R.style.TranslucentUnfullwidthWinStyle);
        mAlertDialog.setContentView(view);
        mAlertDialog.setCanceledOnTouchOutside(true);

        sureButton = (Button) view.findViewById(com.itcalf.renhe.R.id.dialog_sure_bt);
        cancleButton = (Button) view.findViewById(R.id.dialog_cancle_bt);
        contentTitleTv = (TextView) view.findViewById(R.id.forward_title_tv);
        otherTv = (TextView) view.findViewById(R.id.forward_other_tv);
        picIv = (ImageView) view.findViewById(R.id.froward_pic_iv);
        msgEt = (EditText) view.findViewById(R.id.contentEdt);

        if (!TextUtils.isEmpty(fileName)) {
            contentTitleTv.setText(fileName);
        } else {
            contentTitleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(fileSize)) {
            otherTv.setText(fileSize);
        } else {
            otherTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(fileType)) {
            if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_TXT)) {
                picIv.setImageResource(R.drawable.icon_filetype_txt);
            } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_DOC)) {
                picIv.setImageResource(R.drawable.icon_filetype_word);
            } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_EXCEL)) {
                picIv.setImageResource(R.drawable.icon_filetype_excel);
            } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PPT)) {
                picIv.setImageResource(R.drawable.icon_filetype_ppt);
            } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PDF)) {
                picIv.setImageResource(R.drawable.icon_filetype_pdf);
            } else {
                picIv.setImageResource(R.drawable.icon_filetype_unknown);
            }
        }
        if (shareType == Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_SHARE && null != picUri) {
            picIv.setImageURI(picUri);
        }
        mAlertDialog.setCanceledOnTouchOutside(true);
        mAlertDialog.show();
        msgEt.requestFocus();//获取焦点
        handler.sendEmptyMessageDelayed(0, 0);

        sureButton.setOnClickListener(new ShareButtonListener());
        cancleButton.setOnClickListener(new ShareButtonListener());
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    InputMethodManager inputManager = (InputMethodManager) msgEt.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(msgEt, 0);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    class ButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dialog_sure_bt:
                    if (!TextUtils.isEmpty(cancleButton.getText().toString().trim())) {
                        myDialogClickListener.onclick(SURE_BUTTON);
                    }
                    mAlertDialog.dismiss();
                    break;
                case R.id.dialog_cancle_bt:
                    myDialogClickListener.onclick(CANCLE_BUTTON);
                    mAlertDialog.dismiss();
                    break;

                default:
                    break;
            }
        }
    }

    class ShareButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            String editString = msgEt.getText().toString().trim();
            switch (v.getId()) {
                case R.id.dialog_sure_bt:
                    mAlertDialog.dismiss();
                    if (!TextUtils.isEmpty(cancleButton.getText().toString().trim())) {
                        if (null != conversation)
                            myShareDialogClickListener.onclick(SURE_BUTTON, editString, conversation);
                        else if (null != conversationItem)
                            myShareDialogClickListener.onclick(SURE_BUTTON, editString, conversationItem);
                        else if (null != hlContactRenheMember)
                            myShareDialogClickListener.onclick(SURE_BUTTON, editString, hlContactRenheMember);
                        else
                            myShareDialogClickListener.onclick(SURE_BUTTON, editString);
                    }
                    break;
                case R.id.dialog_cancle_bt:
                    mAlertDialog.dismiss();
                    myShareDialogClickListener.onclick(CANCLE_BUTTON, "");
                    break;

                default:
                    break;
            }
        }
    }

    public void DismissDialog() {
        if (mAlertDialog != null)
            mAlertDialog.dismiss();
    }

    public boolean isShowing() {
        if (mAlertDialog == null) {
            return false;
        } else {
            return mAlertDialog.isShowing();
        }

    }


    private static UpgradeDialog upgradeDialog;

    public static void showUpgradeDialog(Context mContext) {
        if (null == upgradeDialog)
            upgradeDialog = new UpgradeDialog(mContext, R.style.TranslucentUnfullwidthWinStyle);
        else if (upgradeDialog.isShowing())
            return;
        upgradeDialog.show();
    }
}

package com.itcalf.renhe.view;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.FaceVPAdapter;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.ReplyMessageBoard;
import com.itcalf.renhe.dto.ReplyUnMessageBoard;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.utils.DraftUtil;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.emoji.ExpressionUtil;

import org.aisen.android.common.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Title: SharePopupWindow.java<br>
 * Description: 回复<br>
 * Copyright (c) 人和网版权所有 2014 <br>
 * Create DateTime: 2014-10-8 下午6:50:00 <br>
 *
 * @author wangning
 */
public class ReplyPopupWindow extends PopupWindow {

    private Context ct;
    private RelativeLayout rootRl;
    private EditText replyEt;
    private TextView leftReplyNumTv;
    private ImageButton goReplyIb;
    private LinearLayout bottomReplyLl;
    /***********
     * 表情部分
     *****************/
    private ExpressionUtil expressionUtil;
    private ImageView imagefaceIv;
    private LinearLayout chat_face_container;
    private ViewPager mViewPager;
    private LinearLayout mDotsLayout;
    private List<View> views = new ArrayList<>();
    private int emotionHeight;
    private final LayoutTransition transitioner = new LayoutTransition();
    /***********
     * 表情部分END
     *****************/
    private static int TOTAL_REPLY_NUMBER = TextSize.getInstance().getRenMaiQuanCommentSize();// 评论最多输入140个字

    private String mObjectId;
    private int mId;
    private int type;
    private String senderSid, senderName;
    private boolean isToRensender = false;
    private Integer replyId = null;
    private String replyObjectId = null;

    private ProgressBar progressBar;
    private String draftkey;

    private MessageBoards.NewNoticeList refreshNoticeListItem;
    private int position;

    @SuppressWarnings("deprecation")
    public ReplyPopupWindow(Context mContext, View parent, String mObjectId, int mId, String senderSid, String senderName,
                            Integer replyId, String replyObjectId, boolean isToResender, int type, MessageBoards.NewNoticeList refreshNoticeListItem, int position) {
        ct = mContext;
        this.refreshNoticeListItem = refreshNoticeListItem;
        this.position = position;
        this.mObjectId = mObjectId;
        this.mId = mId;
        this.senderSid = senderSid;
        this.senderName = senderName;
        this.isToRensender = isToResender;
        this.replyId = replyId;
        this.type = type;
        this.replyObjectId = replyObjectId;
        draftkey = DraftUtil.createKey(RenheApplication.getInstance().getUserInfo().getSid(), mObjectId,
                isToResender ? senderSid : null);
        if (TOTAL_REPLY_NUMBER == 0) {
            TOTAL_REPLY_NUMBER = Constants.REPLYLENGTH;
        }
        final View view = View.inflate(mContext, R.layout.reply_popupwindows, null);
        rootRl = (RelativeLayout) view.findViewById(R.id.rootRl);
        replyEt = (EditText) view.findViewById(R.id.reply_edt);
        leftReplyNumTv = (TextView) view.findViewById(R.id.leftreply_num_tv);
        leftReplyNumTv.setText(TOTAL_REPLY_NUMBER + "");
        //		leftReplyNumTv.setVisibility(View.GONE);
        goReplyIb = (ImageButton) view.findViewById(R.id.gotoReply);
        bottomReplyLl = (LinearLayout) view.findViewById(R.id.bottom_reply_ll);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        imagefaceIv = (ImageView) view.findViewById(R.id.image_face);
        // 表情布局
        chat_face_container = (LinearLayout) view.findViewById(R.id.chat_face_container);
        mViewPager = (ViewPager) view.findViewById(R.id.face_viewpager);
        mViewPager.setOnPageChangeListener(new PageChange());
        // 表情下小圆点
        mDotsLayout = (LinearLayout) view.findViewById(R.id.face_dots_container);
        bottomReplyLl.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));

        expressionUtil = new ExpressionUtil(ct);
        InitViewPager();
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SystemUtils.getScreenHeight((Activity) ct), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
        transitioner.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", emotionHeight,
                SystemUtils.getScreenHeight((Activity) ct)).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        rootRl.setLayoutTransition(transitioner);

        String draftStr = DraftUtil.getDraft(DraftUtil.DRAFT_COMMENT, draftkey);
        if (!TextUtils.isEmpty(draftStr)) {
            replyEt.setText(expressionUtil.getEmotionSpannedString(mContext, null, draftStr));
            replyEt.setSelection(draftStr.length() > TOTAL_REPLY_NUMBER ? TOTAL_REPLY_NUMBER : draftStr.length());
        }
        replyEt.setFocusableInTouchMode(true);
        if (isToResender && !TextUtils.isEmpty(senderName)) {
            replyEt.setHint("回复 " + senderName);
        }
        goReplyIb.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setWidth(LayoutParams.FILL_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable cd = new ColorDrawable(-0000);
        setBackgroundDrawable(cd);
        setFocusable(true);
        setOutsideTouchable(true);
        setContentView(view);
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        update();

        view.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = view.findViewById(R.id.bottom_reply_ll).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        goReplyIb.setOnClickListener(new ShareClickListener());
        replyEt.setFilters(new InputFilter[]{expressionUtil.emotionFilter, new LengthFilter(TOTAL_REPLY_NUMBER)});
        replyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= TOTAL_REPLY_NUMBER) {
                    ToastUtil.showToast(ct, "已经达到最大字数" + TOTAL_REPLY_NUMBER);
                    return;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                leftReplyNumTv.setText(TOTAL_REPLY_NUMBER - s.length() + "");
                DraftUtil.saveDraft(DraftUtil.DRAFT_COMMENT, draftkey, s.toString());
            }
        });
        replyEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (chat_face_container.isShown())
                    hideEmotionView(true);
            }
        });
        // 表情点击按钮
        imagefaceIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (chat_face_container.isShown())
                    hideEmotionView(true);
                else
                    showEmotionView(SystemUtils.isKeyBoardShow((Activity) ct));
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
        emotionHeight = SystemUtils.getKeyboardHeight((Activity) ct);
        SystemUtils.hideSoftInput(replyEt);
        imagefaceIv.setImageDrawable(ct.getResources().getDrawable(R.drawable.chat_emo_normal_on));
        chat_face_container.getLayoutParams().height = emotionHeight;
        chat_face_container.setVisibility(View.VISIBLE);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    //隐藏表情键盘
    private void hideEmotionView(boolean showKeyBoard) {
        if (showKeyBoard) {
            SystemUtils.showKeyBoard(replyEt);
        }
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        imagefaceIv.setImageDrawable(ct.getResources().getDrawable(R.drawable.chat_emo_normal));
        chat_face_container.setVisibility(View.GONE);
    }

    class ShareClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.gotoReply:
                    gotoReply();
                    break;
                default:
                    break;
            }
        }
    }

    private void gotoReply() {
        String content = replyEt.getText().toString().trim();
        final String mcontent = content;
        if (!TextUtils.isEmpty(content)) {
            goReplyIb.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    try {
                        if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD)
                            return RenheApplication.getInstance().getMessageBoardCommand().replyMessageBoard((String) params[0],
                                    (String) params[1], (String) params[2], (String) params[3], (String) params[4],
                                    (Boolean) params[5], params[6] == null ? null : (params[6] + ""), (String) params[7], ct);
                        else
                            return RenheApplication.getInstance().getMessageBoardCommand().replyUnMessageBoard((String) params[0],
                                    (String) params[1], (String) params[2], (String) params[3], (String) params[4],
                                    (Boolean) params[5], (String) params[6], (String) params[7], ct);
                    } catch (Exception e) {
                        System.out.println(e);
                        return null;
                    }
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);
                    dismiss();
                    goReplyIb.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager) ct.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(goReplyIb.getWindowToken(), 0);
                    // 隐藏表情
                    if (chat_face_container.getVisibility() == View.VISIBLE) {
                        chat_face_container.setVisibility(View.GONE);
                        imagefaceIv.setImageDrawable(ct.getResources().getDrawable(R.drawable.chat_emo_normal));
                    }

                    if (null != result) {
                        int state;
                        String MessageboardId = null, MessageboardObjectId = null;
                        if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
                            ReplyMessageBoard results = (ReplyMessageBoard) result;
                            state = results.getState();
                            MessageboardId = results.getMessageboardId();
                            MessageboardObjectId = results.getMessageboardObjectId();
                        } else {
                            ReplyUnMessageBoard results = (ReplyUnMessageBoard) result;
                            state = results.getState();
                            MessageboardObjectId = results.getNoticeCommentObjectId();
                        }

                        if (1 == state) {
                            replyEt.setText("");
                            replyEt.clearFocus();
                            DraftUtil.removeDraft(DraftUtil.DRAFT_COMMENT, draftkey);
                            if (isToRensender) {
                                RenmaiQuanUtils.updateRenmaiQuanItemAddReply(refreshNoticeListItem, mcontent, MessageboardObjectId, MessageboardId, senderName, senderSid);
                            } else {
                                RenmaiQuanUtils.updateRenmaiQuanItemAddReply(refreshNoticeListItem, mcontent, MessageboardObjectId, MessageboardId, "", "");
                            }
                            Intent intent = new Intent();
                            intent.putExtra("refreshNoticeListItem", refreshNoticeListItem);
                            intent.putExtra("position", position);
                            intent.putExtra("isToReply", true);
                            intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                            ct.sendBroadcast(intent);

                        } else {
                            ToastUtil.showErrorToast(ct, "评论失败");
                        }
                    } else {
                        ToastUtil.showNetworkError(ct);
                    }
                }
            }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getAdSId(),
                    RenheApplication.getInstance().getUserInfo().getSid(), mId + "", mObjectId, content, false, replyId,
                    replyObjectId);
        } else {
            ToastUtil.showToast(ct, "评论不能为空");
        }

    }

    /**
     * 初始表情到viewPage
     */
    private void InitViewPager() {
        // 获取页数
        int pageCount = expressionUtil.getPagerCount();
        for (int i = 0; i < pageCount; i++) {
            views.add(expressionUtil.viewPagerItem(i, replyEt));
            LayoutParams params = new LayoutParams(16, 16);
            mDotsLayout.addView(expressionUtil.dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(views);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    /**
     * 表情页改变时，dots效果也要跟着改变
     */
    class PageChange implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }
    }
}

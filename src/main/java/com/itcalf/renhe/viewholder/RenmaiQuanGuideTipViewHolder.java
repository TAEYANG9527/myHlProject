package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.register.BindPhoneActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.WriteLogThread;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.WebViewActWithTitle;

/**
 * Created by wangning on 2015/10/12.
 */
public class RenmaiQuanGuideTipViewHolder extends RecyclerHolder {
    public RecyclerView renmaiQuanRecyclerView;
    public MessageBoards.NewNoticeList newNoticeList;
    public MessageBoards.ContentInfo contentInfo;
    //item 头部布局
    private TextView titleTv;
    private Button button;
    public RecyclerRenmaiQuanItemAdapter recyclerRenmaiQuanItemAdapter;

    public RenmaiQuanGuideTipViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof RecyclerRenmaiQuanItemAdapter)
            recyclerRenmaiQuanItemAdapter = (RecyclerRenmaiQuanItemAdapter) adapter;
        this.renmaiQuanRecyclerView = renmaiQuanRecyclerView;
        titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        button = (Button) itemView.findViewById(R.id.button);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, final int position) {
        if (null == holder)
            return;
        if (null == mNewNoticeList)
            return;
        if (mNewNoticeList instanceof MessageBoards.NewNoticeList)
            newNoticeList = (MessageBoards.NewNoticeList) mNewNoticeList;
        contentInfo = newNoticeList.getContentInfo();
        if (null == contentInfo)
            return;
        String content = contentInfo.getContent();
        String buttonContent = contentInfo.getSubject();
        final String webUrl = contentInfo.getUrl();
        int type = newNoticeList.getType();
        switch (type) {
            case MessageBoards.MESSAGE_TYPE_BINDPHONE:
                if (!TextUtils.isEmpty(content)) {
                    titleTv.setText(content);
                } else {
                    titleTv.setText(context.getString(R.string.renmaiquan_guide_bindphone));
                }
                if (!TextUtils.isEmpty(buttonContent)) {
                    button.setText(buttonContent);
                } else {
                    button.setText(context.getString(R.string.renmaiquan_guide_bindphone_bt_text));
                }
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 绑定手机号
                        Intent intent = new Intent(context, BindPhoneActivity.class);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        new WriteLogThread(context, "5.170.1", null).start();
                    }
                });
                break;
            case MessageBoards.MESSAGE_TYPE_IMPORT_CONTACT:
                if (!TextUtils.isEmpty(content)) {
                    titleTv.setText(content);
                } else {
                    titleTv.setText(context.getString(R.string.renmaiquan_guide_import_contact));
                }
                if (!TextUtils.isEmpty(buttonContent)) {
                    button.setText(buttonContent);
                } else {
                    button.setText(context.getString(R.string.renmaiquan_guide_import_contact_bt_text));
                }
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MobileMailList.class);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        new WriteLogThread(context, "5.170.2", null).start();
                    }
                });

                break;
            case MessageBoards.MESSAGE_TYPE_PERFECT_PROFILE:
                if (!TextUtils.isEmpty(content)) {
                    titleTv.setText(content);
                } else {
                    titleTv.setText(context.getString(R.string.renmaiquan_guide_perfect_profile));
                }
                if (!TextUtils.isEmpty(buttonContent)) {
                    button.setText(buttonContent);
                } else {
                    button.setText(context.getString(R.string.renmaiquan_guide_perfect_profile_btn_text));
                }
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                ((RenheApplication) context.getApplicationContext()).getUserInfo().getSid());
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        new WriteLogThread(context, "5.171.5", null).start();
                    }
                });
                break;
            case MessageBoards.MESSAGE_TYPE_UPLOAD_AVATAR:
                if (!TextUtils.isEmpty(content)) {
                    titleTv.setText(content);
                } else {
                    titleTv.setText(context.getString(R.string.renmaiquan_guide_uplpad_avatar));
                }
                if (!TextUtils.isEmpty(buttonContent)) {
                    button.setText(buttonContent);
                } else {
                    button.setText(context.getString(R.string.renmaiquan_guide_uplpad_avatar_btn_text));
                }
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
                                ((RenheApplication) context.getApplicationContext()).getUserInfo().getSid());
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        new WriteLogThread(context, "5.171.6", null).start();
                    }
                });
                break;
            case MessageBoards.MESSAGE_TYPE_WEBSITE:
                if (!TextUtils.isEmpty(content)) {
                    titleTv.setText(content);
                } else {
                    titleTv.setText(context.getString(R.string.renmaiquan_guide_bindphone));
                }
                if (!TextUtils.isEmpty(buttonContent)) {
                    button.setText(buttonContent);
                } else {
                    button.setText(context.getString(R.string.renmaiquan_guide_bindphone_bt_text));
                }
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, WebViewActWithTitle.class);
                        intent.putExtra("url", webUrl);
                        intent.putExtra("shareable", false);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });
                break;
        }

    }

}


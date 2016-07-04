package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.TransferUrl2Drawable;

import java.util.ArrayList;

/**
 * 正在发送的人脉圈留言
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanSendingMsgNormalTextViewHolder extends RenmaiQuanNormalTextViewHolder {

    public RenmaiQuanUtils renmaiQuanUtils;
    public TransferUrl2Drawable transferUrl;

    private TextView contentTv;
    private TextView seeTotalContentTv;
    private LinearLayout uploadErrorLl;
    private int emojiHeight;
    private ArrayList<MessageBoards.NewNoticeList> datas;

    public RenmaiQuanSendingMsgNormalTextViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        contentTv = (TextView) itemView.findViewById(R.id.content_txt);
        seeTotalContentTv = (TextView) itemView.findViewById(R.id.circle_item_more);
        uploadErrorLl = (LinearLayout) itemView.findViewById(R.id.upload_error_ll);
        renmaiQuanUtils = new RenmaiQuanUtils(context);
        transferUrl = new TransferUrl2Drawable(context);
        emojiHeight = contentTv.getLineHeight();
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, final int position) {
        super.initView(holder, mNewNoticeList, position);
        if (newNoticeList.getUploadState() == Constants.RENMAIQUAN_CONSTANTS.RMQ_UPLOAD_STATE_ERROR) {
            uploadErrorLl.setVisibility(View.VISIBLE);
        } else {
            uploadErrorLl.setVisibility(View.GONE);
        }
        initListener(position);
    }

    private void initListener(final int position) {
        uploadErrorLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newNoticeList.setUploadState(Constants.RENMAIQUAN_CONSTANTS.RMQ_UPLOAD_STATE_UPLOADING);
                Intent intent = new Intent();
                intent.putExtra("uploadNoticeListItem", newNoticeList);
                intent.putExtra("position", position);
                intent.setAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_UPLOAD_MSG_NOTICE);
                context.sendBroadcast(intent);
            }
        });
    }
}

package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.RenmaiQuanPicGridAdapter;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanSendingMsgMultiPicViewHolder extends RenmaiQuanMultiPicViewHolder {
    private LinearLayout uploadErrorLl;
    public RenmaiQuanSendingMsgMultiPicViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        uploadErrorLl = (LinearLayout) itemView.findViewById(R.id.upload_error_ll);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
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

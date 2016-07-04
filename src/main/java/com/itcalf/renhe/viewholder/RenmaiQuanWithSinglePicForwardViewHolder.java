package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithSinglePicForwardViewHolder extends RenmaiQuanWithTextForwardViewHolder {
    private ImageView forwardSinglePicIv;
    private RelativeLayout forwardSinglePicRl;

    public RenmaiQuanWithSinglePicForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardSinglePicIv = (ImageView) itemView.findViewById(R.id.forward_single_pic_iv);
        forwardSinglePicRl = (RelativeLayout) itemView.findViewById(R.id.single_pic_rl);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            if (null != forwardMessageBoardInfo) {
                MessageBoards.PicList picList = forwardMessageBoardInfo.getPicLists()[0];
                String imageUrl = picList.getThumbnailPicUrl();
                if (!TextUtils.isEmpty(imageUrl)) {
                    imageLoader.displayImage(imageUrl, forwardSinglePicIv,
                            CacheManager.rmqImageOptions);
                    CharSequence[] middlePics = new CharSequence[1];
                    middlePics[0] = imageUrl;
                    initListener(middlePics);
                }
            }
        }
    }

    private void initListener(final CharSequence[] middlePics) {
        forwardSinglePicRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewPhotoActivity.class);
                intent.putExtra("ID", 0);
                intent.putExtra("middlePics", middlePics);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.zoom_enter, 0);
                MobclickAgent.onEvent(context, "content_pic");
            }
        });
//        forwardSinglePicRl.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                new ContentUtil().createRenMaiQuanDialog(context, 2, "",newNoticeList);
//                return true;
//            }
//        });
    }
}

package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanSinglePicViewHolder extends RenmaiQuanNormalTextViewHolder {
    private ImageView singlePicIv;
    private RelativeLayout singlePicRl;

    public RenmaiQuanSinglePicViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        singlePicIv = (ImageView) itemView.findViewById(R.id.single_pic_iv);
        singlePicRl = (RelativeLayout) itemView.findViewById(R.id.single_pic_rl);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            singlePicIv.setAdjustViewBounds(true);
            MessageBoards.PicList picList = null;
            if (null != contentInfo.getPicList() && contentInfo.getPicList().length > 0) {
                picList = contentInfo.getPicList()[0];
            }
            String imageUrl = "";
            if (null != picList) {
                imageUrl = picList.getThumbnailPicUrl();
            }

            CharSequence[] middlePics = new CharSequence[1];
            if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {//如果是新发布（还未完成）的留言
                File file = new File(imageUrl);
                imageUrl = Uri.fromFile(file).toString();
                middlePics[0] = imageUrl;
            } else {
                if (null != picList)
                    middlePics[0] = picList.getBmiddlePicUrl();
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                singlePicRl.setVisibility(View.VISIBLE);
                try {
                    imageLoader.displayImage(imageUrl, singlePicIv,
                            CacheManager.rmqImageOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                singlePicRl.setVisibility(View.GONE);
            }

            initListener(middlePics);
        }
    }

    private void initListener(final CharSequence[] middlePics) {
        if (null == middlePics)
            return;
        singlePicRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                    context.startActivity(new Intent(context, UpgradeActivity.class));
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                    statisticsMap.put("type", "1");
                    StatisticsUtil.statisticsCustomClickEvent(context.getString(R.string.android_btn_pop_upgrade_click), 0, "", statisticsMap);
                } else {
                    Intent intent = new Intent(context, ViewPhotoActivity.class);
                    intent.putExtra("ID", 0);
                    intent.putExtra("middlePics", middlePics);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.zoom_enter, 0);
                    MobclickAgent.onEvent(context, "content_pic");
                }
            }
        });
//        singlePicRl.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                new ContentUtil().createRenMaiQuanDialog(context, 2, "",newNoticeList);
//                return true;
//            }
//        });
    }
}

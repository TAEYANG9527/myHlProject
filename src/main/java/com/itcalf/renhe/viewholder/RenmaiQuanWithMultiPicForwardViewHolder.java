package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.RoomRemotePicGridAdapter;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithMultiPicForwardViewHolder extends RenmaiQuanWithTextForwardViewHolder {
    private GridView forwardMultiPicGv;

    public RenmaiQuanWithMultiPicForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardMultiPicGv = (GridView) itemView.findViewById(R.id.forward_multi_pic_gv);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            if (null != forwardMessageBoardInfo) {
                MessageBoards.PicList[] picList = forwardMessageBoardInfo.getPicLists();
                RoomRemotePicGridAdapter adapter = new RoomRemotePicGridAdapter(context, picList);
                forwardMultiPicGv.setAdapter(adapter);
                CharSequence[] middlePics = new CharSequence[picList.length];
                for (int i = 0; i < picList.length; i++) {
                    middlePics[i] = picList[i].getBmiddlePicUrl();
                }
                initListener(middlePics);
            }
        }
    }

    private void initListener(final CharSequence[] middlePics) {
        if (null != middlePics && middlePics.length > 0) {
            forwardMultiPicGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Intent intent = new Intent(context, ViewPhotoActivity.class);
                    intent.putExtra("ID", arg2);
                    intent.putExtra("middlePics", middlePics);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.zoom_enter, 0);
                    MobclickAgent.onEvent(context, "content_pic");
                }
            });
//            forwardMultiPicGv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                    new ContentUtil().createRenMaiQuanDialog(context, 2, "",newNoticeList);
//                    return true;
//                }
//            });
        }
    }
}

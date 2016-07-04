package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.room.NewMessageBoardActivity;
import com.orhanobut.logger.Logger;

/**
 * Created by wangning on 2015/10/21.
 */
public class HeaderViewHolder extends RenmaiQuanViewHolder {
    private LinearLayout rootLl;
    private LinearLayout unreadLl;
    private TextView unreadCountTv;
    private ImageView userHeadIv;
    private String userFaceUrl;
    private int unreadCount;

    public HeaderViewHolder(final Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        rootLl = (LinearLayout) itemView.findViewById(R.id.unreadmsg_ll);
        unreadLl = (LinearLayout) itemView.findViewById(R.id.unread_ll);
        userHeadIv = (ImageView) itemView.findViewById(R.id.user_head_iv);
        unreadCountTv = (TextView) itemView.findViewById(R.id.unreadcount_tv);


        unreadLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, NewMessageBoardActivity.class);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                RenheApplication.getInstance().getUserEditor().remove(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_USERFACE);
                RenheApplication.getInstance().getUserEditor().remove(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_COUNT);
                RenheApplication.getInstance().getUserEditor().commit();
                //发送通知
                Intent unReadintent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_UNREAD_NOTICE);
                context.sendBroadcast(unReadintent);
                //发送通知
                Intent tabIntent = new Intent(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
                tabIntent.putExtra(TabMainFragmentActivity.TAB_FLAG, 3);
                tabIntent.putExtra(TabMainFragmentActivity.TAB_ICON_RENMAIQUAN_UNREAD_NUM, 0);
                context.sendBroadcast(tabIntent);
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(Integer.MAX_VALUE - 2);
            }
        });

    }

    @Override
    public void initView(RecyclerHolder holder, Object item, int position) {
        userFaceUrl = RenheApplication.getInstance().getUserSharedPreferences().getString(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_USERFACE, "");
        unreadCount = RenheApplication.getInstance().getUserSharedPreferences().getInt(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_COUNT, 0);
//        userFaceUrl = RenheApplication.getInstance().getUserInfo().getUserface();
//        unreadCount = 2;
//        if (unreadCount <= 0) {
//            rootLl.setVisibility(View.GONE);
//        } else {
//            rootLl.setVisibility(View.VISIBLE);
//        }
        imageLoader.displayImage(userFaceUrl, userHeadIv);
        unreadCountTv.setText(unreadCount + "条新消息");
    }
}

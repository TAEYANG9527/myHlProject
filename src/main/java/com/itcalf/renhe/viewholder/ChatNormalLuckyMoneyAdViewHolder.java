package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.luckyMoneyAd.LuckyMoneyAdDetailActivity;
import com.itcalf.renhe.view.TextView;

/**
 * 红包广告
 *
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalLuckyMoneyAdViewHolder extends ChatViewHolder {

    private TextView luckyNameTv;
    private TextView luckyTipTv;
    public MessageContent.TextContent messageContent;
    private String luckyName;
    private String luckySid;
    private String luckyTip;
    private String luckySenderUserFace;
    private String luckyContent;
    private static final String LUCKY_NAME = "red_name";//标题
    private static final String LUCKY_SID = "red_sid";//红包sid
    private static final String LUCKY_USERFACE = "userface";//发送者头像
    private static final String LUCKY_CONTENT = "content";//广告内容

    public ChatNormalLuckyMoneyAdViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                            RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        luckyNameTv = ((TextView) itemView.findViewById(R.id.lucky_info_title_tv));
        luckyTipTv = ((TextView) itemView.findViewById(R.id.lucky_info_tip_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.TextContent) message.messageContent();
        if (null == messageContent)
            return;
        luckyName = message.extension(LUCKY_NAME);
        luckySid = message.extension(LUCKY_SID);
        luckySenderUserFace = message.extension(LUCKY_USERFACE);
        luckyContent = message.extension(LUCKY_CONTENT);
        luckyTip = isSenderIsSelf() ? context.getString(R.string.lucky_money_see) : context.getString(R.string.lucky_money_get);
        luckyNameTv.setText(luckyName);
        luckyTipTv.setText(luckyTip);
    }

    @Override
    public void onContentRlClickListener() {
        if (!TextUtils.isEmpty(luckySid)) {
            seeLuckyMoneyDetail(luckySid);
        }
    }

    @Override
    public void onContentRlLongClickListener() {
    }

    private void seeLuckyMoneyDetail(String luckySid) {
        Intent intent = new Intent(context, LuckyMoneyAdDetailActivity.class);
        intent.putExtra("luckyAdSid", luckySid);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

}


package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.utils.ChatUtils;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.view.TextView;

import java.util.Date;

/**
 * 自定义的系统消息 eg：别人回复你的话题发送的系统消息、xxx抢了你的红包
 *
 * @author wangning  on 2015/10/12.
 */
public class ChatCustomSystemMsgViewHolder extends RecyclerHolder {
    public TextView createTimeTv;
    private TextView contentTv;
    public Conversation conversation;
    public ChatMessage chatMessage;
    public Message message;
    private static final String HIGHLIGHT = "highlight";//高亮的内容
    private static final String CONTENT = "content";//话题标题
    private static final String URL = "url";//: 话题的地址

    public ChatCustomSystemMsgViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                         RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, adapter);
        this.conversation = conversation;
        createTimeTv = (TextView) itemView.findViewById(R.id.tv_sendtime);
        contentTv = ((TextView) itemView.findViewById(R.id.sysmsg_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        if (null == holder)
            return;
        if (null == conversation)
            return;
        if (null == messageObj)
            return;
        if (messageObj instanceof ChatMessage)
            chatMessage = (ChatMessage) messageObj;
        message = chatMessage.getMessage();
        if (null == message)
            return;
        String msg = message.extension(CONTENT);
        if (TextUtils.isEmpty(msg))
            return;
        //创建时间
        long datetime = message.createdAt();
        if (datetime > 0) {
            String dateString = DateUtil.newFormatByDay(context, new Date(message.createdAt()));
            createTimeTv.setText(dateString);
        } else {
            createTimeTv.setText("");
        }
        String highlight = message.extension(HIGHLIGHT);
        String topicUrl = message.extension(URL);
        if (!TextUtils.isEmpty(highlight) && msg.contains(highlight)) {
            int index = msg.indexOf(highlight);
            SpannableString contentSpannableString = new SpannableString(msg);
            contentSpannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.CL)), index, index + highlight.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentSpannableString.setSpan(new ChatUtils.SystemMsgSpanClick(context, topicUrl,1), index,
                    index + highlight.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            contentTv.setText(contentSpannableString);
            contentTv.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            contentTv.setText(msg);
        }
    }
}


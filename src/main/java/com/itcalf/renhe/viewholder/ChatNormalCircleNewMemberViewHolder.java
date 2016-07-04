package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.view.TextView;

/**
 * 新人加入圈子，给各位问好
 *
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalCircleNewMemberViewHolder extends ChatViewHolder {

    private TextView titleTv;
    private ImageView userPicIv;
    private TextView userInfoTv;
    public MessageContent.TextContent messageContent;
    private String linkedContentLink;
    private String linkedContentTitle;
    private String linkedContentMsg;
    private String linkedContentPicUrl;

    private static final String TITLE = "title";//标题
    private static final String NAME = "name";//姓名
    private static final String IMAGE = "image";// : 头像
    private static final String CURTITLE = "curTitle";//: 职务
    private static final String CURCOMPANY = "curCompany";//: 公司
    private static final String SID = "sid";//: 当前用户的SID

    public ChatNormalCircleNewMemberViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                               RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        titleTv = ((TextView) itemView.findViewById(R.id.title_tv));
        userPicIv = ((ImageView) itemView.findViewById(R.id.user_pic_iv));
        userInfoTv = ((TextView) itemView.findViewById(R.id.user_info_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.TextContent) message.messageContent();
        if (null == messageContent)
            return;
        linkedContentLink = message.extension(SID);
        linkedContentTitle = message.extension(TITLE);
        linkedContentMsg = message.extension(NAME) + "\n" + message.extension(CURTITLE) + "\n" + message.extension(CURCOMPANY);
        linkedContentPicUrl = message.extension(IMAGE);
        titleTv.setText(linkedContentTitle);
        loadImage(userPicIv, linkedContentPicUrl);
        SpannableString selfInfoSpannableString = new SpannableString(linkedContentMsg);
        handlerSelfInfoString(selfInfoSpannableString, linkedContentMsg);
        userInfoTv.setText(selfInfoSpannableString);
    }

    private void handlerSelfInfoString(SpannableString spannableString, String content) {
        String[] texts = content.split("\n");
        if (texts.length > 1) {
            int index = content.indexOf("\n");
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.C1)), 0, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.C2)), index, content.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public void onContentRlClickListener() {
        Intent bundle = new Intent(context, MyHomeArchivesActivity.class);
        bundle.putExtra("profileSid", linkedContentLink);
        context.startActivity(bundle);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onContentRlLongClickListener() {
        if (null != chatUtils) {
            chatUtils.createCopyDialog(context, linkedContentMsg, Constants.ChatShareType.CHAT_SHARE_LINK, linkedContentTitle,
                    "user://"+linkedContentLink, linkedContentPicUrl);
        }
    }

}


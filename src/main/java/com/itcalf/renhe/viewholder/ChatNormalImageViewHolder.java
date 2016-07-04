package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.ViewPhotoActivity;

import java.util.List;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalImageViewHolder extends ChatViewHolder {

    private String imageURl;//图片url
    private ImageView imageIv;
    private MessageContent.MediaContent messageContent;
    private List<String> imgsUrlList;//存储聊天记录里所有的image url

    public ChatNormalImageViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                     RecyclerView.Adapter adapter, Conversation conversation, List<String> imgsUrlList) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        imageIv = ((ImageView) itemView.findViewById(R.id.iv_imgcontent));
        this.imgsUrlList = imgsUrlList;
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.MediaContent) message.messageContent();
        if (null == messageContent)
            return;
        imageURl = messageContent.url();
        if (!TextUtils.isEmpty(imageURl)) {
            if (imageURl.startsWith("http")) {
                loadImage(imageIv, imageURl, CacheManager.imImageOptions);
            } else {
                loadImage(imageIv, "file://" + imageURl, CacheManager.imImageOptions);
            }
        }

    }

    @Override
    public void onContentRlClickListener() {
        if (imageURl.startsWith("http")) {
            int index = -1;
            CharSequence[] middlePics;
            if (null != imgsUrlList && imgsUrlList.size() > 0) {
                middlePics = new CharSequence[imgsUrlList.size()];
                for (int i = 0; i < imgsUrlList.size(); i++) {
                    if (imgsUrlList.get(i).equals(message.messageId() + Constants.CHAT_CONSTANTS.SEPARATOR
                            + ((MessageContent.MediaContent) message.messageContent()).url())) {
                        index = i;
                    }
                    if (imgsUrlList.get(i).contains(Constants.CHAT_CONSTANTS.SEPARATOR))
                        middlePics[i] = imgsUrlList.get(i).substring(imgsUrlList.get(i).indexOf(Constants.CHAT_CONSTANTS.SEPARATOR) + 1);
                    else
                        middlePics[i] = imgsUrlList.get(i);
                }
            } else {
                middlePics = new CharSequence[1];
                middlePics[0] = imageURl;
            }
            if (index < 0) {
                middlePics = new CharSequence[1];
                middlePics[0] = imageURl;
                index = 0;
            }
            Intent intent = new Intent(context, ViewPhotoActivity.class);
            intent.putExtra("ID", index);
            intent.putExtra("middlePics", middlePics);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.zoom_enter, 0);
        }
    }

    @Override
    public void onContentRlLongClickListener() {
        if (null != chatUtils) {
            chatUtils.createCopyDialog(context, ((MessageContent.MediaContent) message.messageContent()).url(),
                    Constants.ChatShareType.CHAT_SHARE_IMAGE, null, null, null);
        }
    }

}


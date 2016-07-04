package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.view.TextView;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalRightAudioViewHolder extends ChatViewHolder {

    private long audioLength = 0;//语音长度
    public ImageView audioIv;
    private TextView audioDurationTv;
    public MessageContent.AudioContent messageContent;

    public ChatNormalRightAudioViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                          RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        audioIv = ((ImageView) itemView.findViewById(R.id.iv_audiocontent));
        audioDurationTv = ((TextView) itemView.findViewById(R.id.audio_length_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.AudioContent) message.messageContent();
        if (null == messageContent)
            return;
        audioLength = messageContent.duration();
        int audipPx = (int) ((audioLength / 2000)) * (int) context.getResources().getDimension(R.dimen.chat_audio_length);
        audipPx = audipPx > DensityUtil.dip2px(context, Constants.CHAT_CONSTANTS.CHAT_AUDIO_BCG_LENGTH)
                ? DensityUtil.dip2px(context, Constants.CHAT_CONSTANTS.CHAT_AUDIO_BCG_LENGTH) : audipPx;
        if (audipPx <= 0)
            audipPx = (int) context.getResources().getDimension(R.dimen.chat_audio_length);
        if (isSenderIsSelf()) {
            audioIv.setPadding(audipPx, 0, 0, 0);
        } else {
            audioIv.setPadding(0, 0, audipPx, 0);
        }
        if (audioLength <= 1000) {
            audioDurationTv.setText("1\"");
        } else {
            audioDurationTv.setText((int) (audioLength / 1000) + "\"");
        }
        //如果item正在播放，则播放动画
        if (recyclerChatItemAdapter.getIsPlayAudioUrl().equals(((MessageContent.MediaContent) message.messageContent()).url())) {
            chatUtils.startAudioAnimation(audioIv, !isSenderIsSelf());
        } else {
            chatUtils.stopAudioAnimation(audioIv, !isSenderIsSelf());
        }
        contentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatUtils.playAudio(message, false, audioIv, position);
            }
        });
    }

    @Override
    public void onContentRlClickListener() {

    }

    @Override
    public void onContentRlLongClickListener() {
    }

}


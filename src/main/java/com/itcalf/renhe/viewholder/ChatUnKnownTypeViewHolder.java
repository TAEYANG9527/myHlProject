package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.wukong.im.SystemMsgActivity;
import com.itcalf.renhe.utils.CheckUpdateUtil;
import com.itcalf.renhe.widget.emojitextview.AisenChatTextView;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatUnKnownTypeViewHolder extends ChatViewHolder {

    private AisenChatTextView contentTv;
    private static final String UPDATE_PART = "马上升级";//点击升级

    public ChatUnKnownTypeViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                     RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        contentTv = ((AisenChatTextView) itemView.findViewById(R.id.chat_content_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
//        contentTv.setContent(context.getString(R.string.chat_linked_unkonwn_msg));
        int index = context.getString(R.string.chat_linked_unkonwn_msg).indexOf(UPDATE_PART);
        SpannableString contentSpannableString = new SpannableString(context.getString(R.string.chat_linked_unkonwn_msg));
        contentSpannableString.setSpan(new UnderlineSpan(), index, index + UPDATE_PART.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        contentSpannableString.setSpan(new UpdateMsgSpanClick(context), index,
                index + UPDATE_PART.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        contentTv.setText(contentSpannableString);
        contentTv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onContentRlClickListener() {

    }

    @Override
    public void onContentRlLongClickListener() {
    }

    /**
     * 点击升级
     */
    class UpdateMsgSpanClick extends ClickableSpan implements View.OnClickListener {
        private Context context;

        public UpdateMsgSpanClick(Context context) {
            this.context = context;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
        }

        @Override
        public void onClick(View v) {
            new CheckUpdateUtil(context).checkUpdate(true);
        }

    }
}


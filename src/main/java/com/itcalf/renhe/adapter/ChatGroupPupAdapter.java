package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CircleJoinCount;
import com.itcalf.renhe.bean.GroupChatPupBean;

import java.util.Arrays;
import java.util.List;

/**
 * 和聊对话列表 tab右上角弹框
 * Created by wangning on 2016/1/12.
 */
public class ChatGroupPupAdapter extends BaseAdapter {
    private Context mContext;
    private List<GroupChatPupBean> list;

    public ChatGroupPupAdapter(Context mContext) {
        list = Arrays.asList(
                new GroupChatPupBean(mContext.getString(R.string.chat_dialogue_1), R.drawable.icon_circletopic),
                new GroupChatPupBean(mContext.getString(R.string.chat_dialogue_2), R.drawable.icon_mytopic),
                new GroupChatPupBean(mContext.getString(R.string.chat_dialogue_3), R.drawable.icon_chat_setting)
        );
        this.mContext = mContext;
    }


    public void refreshAdapterByCircleJoinCount(CircleJoinCount result) {
        if (null == result)
            return;
        if (!TextUtils.isEmpty(result.getCircleTopicUrl())) {
//            list.get(0).setDestinationUrl("http://teamh5dev.renhe.cn/topic/circleTopicList.html?circleId=24");
            list.get(0).setDestinationUrl(result.getCircleTopicUrl());
        }
        if (!TextUtils.isEmpty(result.getMyTopicUrl())) {
//            list.get(1).setDestinationUrl("http://teamh5dev.renhe.cn/topic/myTopicList.html?circleId=24");
            list.get(1).setDestinationUrl(result.getMyTopicUrl());
        }
        if (result.getUnReadTopiCount() > 0) {
            list.get(0).setUnreadNum(result.getUnReadTopiCount());
        }else {
            list.get(0).setUnreadNum(0);
        }
        if (result.getUnReadReplyCount() > 0) {
            list.get(1).setUnreadNum(result.getUnReadReplyCount());
        }else {
            list.get(1).setUnreadNum(0);
        }
        if (result.getUnReadCount() > 0) {
            list.get(2).setUnreadNum(result.getUnReadCount());
        }else {
            list.get(2).setUnreadNum(0);
        }
        notifyDataSetChanged();
    }

    public List<GroupChatPupBean> getList() {
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_groupchat_popup_add_layout, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        TextView unreadNumView = (TextView) convertView.findViewById(R.id.tv_unread_numb);
        GroupChatPupBean bean = list.get(position);
        textView.setText(bean.getTitle());
        textView.setCompoundDrawablesWithIntrinsicBounds(bean.getIcon(), 0, 0, 0);
        if(bean.getUnreadNum() > 0){
            unreadNumView.setVisibility(View.VISIBLE);
            if(bean.getUnreadNum() > 99){
                unreadNumView.setText("...");
            }else {
                unreadNumView.setText(bean.getUnreadNum()+"");
            }
        }else {
            unreadNumView.setVisibility(View.GONE);
        }
        return convertView;
    }
}

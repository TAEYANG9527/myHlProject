package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.PupBean;

import java.util.Arrays;
import java.util.List;

/**
 * 和聊对话列表 tab右上角弹框
 * Created by wangning on 2016/1/12.
 */
public class ConversationPupAdapter extends BaseAdapter {
    private Context mContext;
    private List<PupBean> list;

    public ConversationPupAdapter(Context mContext) {
        list = Arrays.asList(
                new PupBean(mContext.getString(R.string.dialogue_3), R.drawable.icon_join_2x),
                new PupBean(mContext.getString(R.string.dialogue_2), R.drawable.icon_create_2x),
                new PupBean(mContext.getString(R.string.dialogue_1), R.drawable.icon_invite_2x)
        );
        this.mContext = mContext;
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
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_popup_add_layout, parent, false);
        TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
        PupBean bean = list.get(position);
        textView.setText(bean.title);
        textView.setCompoundDrawablesWithIntrinsicBounds(bean.icon, 0, 0, 0);
        return convertView;
    }
}

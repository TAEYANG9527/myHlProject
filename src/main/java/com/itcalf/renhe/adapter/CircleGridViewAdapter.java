package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.view.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class CircleGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<MemberInfo> circleUserList = null;

    public CircleGridViewAdapter(Context context, List<MemberInfo> _circleUserList) {
        this.mContext = context;
        this.circleUserList = _circleUserList;
    }

    @Override
    public int getCount() {
        int num = 0;
        if (circleUserList.size() >= 5)
            num = 5;
        else
            num = circleUserList.size();
        return num;
    }

    public List<MemberInfo> getAll() {
        return circleUserList;
    }

    public void addInfo(List<MemberInfo> array) {
        circleUserList.addAll(array);
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.activtity_creat_circle_list_item, parent, false);
            vh.ivImg = (ImageView) convertView.findViewById(R.id.iv_img);
            vh.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        MemberInfo memberInfo = circleUserList.get(position);
        ImageLoader.getInstance().displayImage(memberInfo.getAvatar(), vh.ivImg);
        if (memberInfo.isMaster()) {
            vh.tvName.setTextColor(mContext.getResources().getColor(R.color.CF));
        } else {
            vh.tvName.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        vh.tvName.setText(circleUserList.get(position).getNickName());
        return convertView;
    }

    class ViewHolder {
        ImageView ivImg;
        TextView tvName;
    }
}

package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**创建圈子时 成员图像及姓名 Gridview的适配器
 * Created by wujian on 2015/12/16.
 */
public class CreateCircleGridViewAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<HlContactRenheMember> imgList;

    public CreateCircleGridViewAdapter(ArrayList<HlContactRenheMember> imgList, Context mContext) {
        this.imgList = imgList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return imgList.size();
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
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.activtity_creat_circle_list_item, null, false);
            vh.iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
            vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        ImageLoader.getInstance().displayImage(imgList.get(position).getUserface(), vh.iv_img);
        if (position == 0) {
            vh.tv_name.setTextColor(mContext.getResources().getColor(R.color.CF));
        } else {
            vh.tv_name.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        vh.tv_name.setText(imgList.get(position).getName());
        return convertView;
    }

    class ViewHolder {
        ImageView iv_img;
        TextView tv_name;
    }
}

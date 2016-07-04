package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CircleInfoByCircleId.CircleMember;
import com.itcalf.renhe.view.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class CircleDetailMemberAdapter extends BaseAdapter {
	private Context mContext;
	private List<CircleMember> circleUserList = null;

	public CircleDetailMemberAdapter(Context context, List<CircleMember> _circleUserList) {
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

	public List<CircleMember> getAll() {
		return circleUserList;
	}

	public void addInfo(List<CircleMember> array) {
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

		CircleMember memberInfo = circleUserList.get(position);
		ImageLoader.getInstance().displayImage(memberInfo.getUserface(), vh.ivImg);
		if (position == 0) {
			vh.tvName.setTextColor(mContext.getResources().getColor(R.color.CF));
		} else {
			vh.tvName.setTextColor(mContext.getResources().getColor(R.color.black));
		}
		vh.tvName.setText(circleUserList.get(position).getName());
		return convertView;
	}

	class ViewHolder {
		ImageView ivImg;
		TextView tvName;
	}
}

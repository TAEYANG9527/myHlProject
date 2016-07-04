package com.itcalf.renhe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashSet;
import java.util.List;

public class MemberUserAdapter extends BaseAdapter {
	private Context context;
	private MemberInfo memberInfo;
	private boolean isJurisdiction;
	private List<MemberInfo> data;
	private LayoutInflater inflater;
	private HashSet<Long> openId = new HashSet<Long>();
	private HashSet<Long> deleteSet = new HashSet<Long>();

	public MemberUserAdapter(Context context, List<MemberInfo> list, MemberInfo memberInfo, HashSet<Long> set,
			HashSet<Long> deleteSet, boolean isJurisdiction) {
		this.data = list;
		this.openId = set;
		this.context = context;
		this.deleteSet = deleteSet;
		this.memberInfo = memberInfo;
		this.isJurisdiction = isJurisdiction;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
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
			convertView = inflater.inflate(R.layout.list_member_list_item, null);
			vh.name = (TextView) convertView.findViewById(R.id.tx_name);
			vh.iv = (ImageView) convertView.findViewById(R.id.iv_avatar);
			vh.cb = (CheckBox) convertView.findViewById(R.id.cb);
			vh.ly_item = (LinearLayout) convertView.findViewById(R.id.ly_item);
			vh.setOnclick();
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.pos = position;
		MemberInfo item = data.get(position);

		if (!deleteSet.contains(item.getOpenId())) {
			if (isJurisdiction) {
				if (memberInfo.getOpenId() == item.getOpenId())
					vh.cb.setVisibility(View.GONE);
				else
					vh.cb.setVisibility(View.VISIBLE);
			} else {
				vh.cb.setVisibility(View.GONE);
			}

			vh.name.setText(item.getNickName());
			vh.cb.setChecked(openId.contains(item.getOpenId()) ? true : false);
			ImageLoader.getInstance().displayImage(item.getAvatar(), vh.iv, CacheManager.options);
		} else {
			vh.ly_item.setVisibility(View.GONE);
		}
		return convertView;
	}

	private class ViewHolder {
		private int pos;
		private TextView name;
		private ImageView iv;
		private CheckBox cb;
		private LinearLayout ly_item;

		void setOnclick() {
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, MyHomeArchivesActivity.class);
					intent.putExtra("name", data.get(pos).getNickName());
					intent.putExtra("openId", data.get(pos).getOpenId());
					context.startActivity(intent);
					((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			});
		}
	}
}

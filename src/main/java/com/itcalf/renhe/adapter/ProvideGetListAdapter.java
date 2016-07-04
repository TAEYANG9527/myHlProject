package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.RecommendIndustry;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author 王宁
 * 
 */
public class ProvideGetListAdapter extends BaseAdapter {
	private Context ct;
	TextView infoTv;
	TextView timeTv;
	// 留言显示数据
	private List<RecommendIndustry> recommendIndustries = new ArrayList<RecommendIndustry>();
	private LayoutInflater flater;

	@SuppressWarnings("unchecked")
	public ProvideGetListAdapter(Context context, List<RecommendIndustry> data) {
		this.flater = LayoutInflater.from(context);
		this.recommendIndustries = (List<RecommendIndustry>) data;
		this.ct = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = flater.inflate(R.layout.provide_get_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.icon);
			viewHolder.flagIv = (ImageView) convertView.findViewById(R.id.flag);
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		RecommendIndustry recommendIndustry = recommendIndustries.get(position);
		if (null != recommendIndustry) {
			viewHolder.nameTv.setText(recommendIndustry.getName());
			if (recommendIndustry.isChecked()) {
				viewHolder.flagIv.setVisibility(View.VISIBLE);
			} else {
				viewHolder.flagIv.setVisibility(View.GONE);
			}
			viewHolder.avatarIv.setImageResource(recommendIndustry.getBcgRe());

		}
		viewHolder.flagIv.setTag(recommendIndustry.getBcgRe() + position);
		viewHolder.avatarIv.setTag(recommendIndustry.getBcgRe() + "icon" + position);
		return convertView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return recommendIndustries.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return recommendIndustries.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public static class ViewHolder {
		public TextView nameTv;
		public ImageView avatarIv;
		public ImageView flagIv;

	}
}
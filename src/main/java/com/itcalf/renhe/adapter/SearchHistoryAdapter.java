package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.relationship.AdvancedSearchActivity;
import com.itcalf.renhe.dto.SearchHistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature:扩展SimpleAdapter的BaseAdapter
 * Description:增加了图片缓存加载，控制特殊视图的显示
 * 
 * @author xp
 * 
 */

public class SearchHistoryAdapter extends BaseAdapter {
	private LayoutInflater flater;
	private Context ct;
	private ListView mListView;
	private SearchHistoryItem searchHistoryItem;
	// 留言显示数据
	private List<SearchHistoryItem> mWeiboList = new ArrayList<SearchHistoryItem>();

	public SearchHistoryAdapter(Context context, List<SearchHistoryItem> data, ListView listView) {
		this.flater = LayoutInflater.from(context);
		this.mWeiboList = (List<SearchHistoryItem>) data;
		this.ct = context;
		this.mListView = listView;
	}

	@Override
	public int getCount() {
		return mWeiboList.size() != 0 ? mWeiboList.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {
		return mWeiboList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = flater.inflate(R.layout.search_history_item, null);
			viewHolder = new ViewHolder();
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		searchHistoryItem = mWeiboList.get(position);
		if (null != searchHistoryItem) {
			StringBuffer mStringBuffer = new StringBuffer();
			String keyword = searchHistoryItem.getKewword();
			String area = searchHistoryItem.getArea();
			String industry = searchHistoryItem.getIndustry();
			String company = searchHistoryItem.getCompany();
			String job = searchHistoryItem.getJob();
			String name = searchHistoryItem.getName();

			if (!TextUtils.isEmpty(keyword)) {
				mStringBuffer.append("\"" + keyword + "\"");
			}
			if ((!TextUtils.isEmpty(area) && !area.equals(AdvancedSearchActivity.ALL_AREA_STRING))) {
				if (TextUtils.isEmpty(mStringBuffer.toString())) {
					mStringBuffer.append(area);
				} else {
					mStringBuffer.append(" + " + area);
				}
			}
			if ((!TextUtils.isEmpty(industry) && !industry.equals(AdvancedSearchActivity.ALL_INDUSTRY_STRING)
					&& !industry.equals(AdvancedSearchActivity.ALL_INDUSTRY_STRING2))) {
				if (TextUtils.isEmpty(mStringBuffer.toString())) {
					mStringBuffer.append(industry);
				} else {
					mStringBuffer.append(" + " + industry);
				}
			}
			if (!TextUtils.isEmpty(company)) {
				if (TextUtils.isEmpty(mStringBuffer.toString())) {
					mStringBuffer.append(company);
				} else {
					mStringBuffer.append(" + " + company);
				}
			}
			if (!TextUtils.isEmpty(job)) {
				if (TextUtils.isEmpty(mStringBuffer.toString())) {
					mStringBuffer.append(job);
				} else {
					mStringBuffer.append(" + " + job);
				}
			}
			if (!TextUtils.isEmpty(name)) {
				if (TextUtils.isEmpty(mStringBuffer.toString())) {
					mStringBuffer.append(name);
				} else {
					mStringBuffer.append(" + " + name);
				}
			}
			viewHolder.nameTv.setText(mStringBuffer.toString());
		}
		return convertView;
	}

	public static class ViewHolder {
		public TextView nameTv;
	}

}

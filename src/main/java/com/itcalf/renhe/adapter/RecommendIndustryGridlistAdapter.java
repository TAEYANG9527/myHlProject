package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.RecommendIndustry;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Title: RecommendGridlistAdapter.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-12 上午10:52:40 <br>
 * @author wangning
 */
public class RecommendIndustryGridlistAdapter extends BaseAdapter {
	private LayoutInflater flater;
	private Context ct;
	private List<RecommendIndustry> recommendIndustries = new ArrayList<RecommendIndustry>();
	private GridView gridView;
	private ModeDto modeDto;

	public RecommendIndustryGridlistAdapter(Context ct, List<RecommendIndustry> recommendIndustries, GridView gridView) {
		super();
		this.flater = LayoutInflater.from(ct);
		this.ct = ct;
		this.recommendIndustries = recommendIndustries;
		this.gridView = gridView;
		modeDto = new ModeDto();
	}

	@Override
	public int getCount() {
		return recommendIndustries.size() != 0 ? recommendIndustries.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {
		return recommendIndustries.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = flater.inflate(R.layout.recommend_industry_grid_item, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.recommendGridItemImage);
			viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
			viewHolder.mCompanyTv = (TextView) convertView.findViewById(R.id.companyTv);
			viewHolder.mTitleTv = (TextView) convertView.findViewById(R.id.titleTv);
			viewHolder.vipPeriodTv = (TextView) convertView.findViewById(R.id.accountTypeYearPeriodTv);
			viewHolder.isCheckedLL = (RelativeLayout) convertView.findViewById(R.id.checkLl);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		RecommendIndustry recommendIndustry = recommendIndustries.get(position);
		if (null != recommendIndustry) {
			viewHolder.nameTv.setText(recommendIndustry.getName());
			viewHolder.mTitleTv.setVisibility(View.GONE);
			viewHolder.mCompanyTv.setVisibility(View.GONE);

			viewHolder.vipIv.setVisibility(View.GONE);
			viewHolder.vipPeriodTv.setVisibility(View.GONE);
			if (recommendIndustry.isChecked()) {
				viewHolder.isCheckedLL.setVisibility(View.VISIBLE);
			} else {
				viewHolder.isCheckedLL.setVisibility(View.GONE);
			}
			viewHolder.avatarIv.setImageResource(recommendIndustry.getBcgRe());

		}
		viewHolder.isCheckedLL.setTag(recommendIndustry.getBcgRe() + position);
		return convertView;
	}

	public static class ViewHolder {
		public TextView nameTv;
		public ImageView avatarIv;
		public TextView mCompanyTv;
		public TextView mTitleTv;
		public ImageView vipIv;
		public TextView vipPeriodTv;
		public RelativeLayout isCheckedLL;
	}

	class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 1000);
					displayedImages.add(imageUri);
				}
			}
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {
			super.onLoadingStarted(imageUri, view);
		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			super.onLoadingFailed(imageUri, view, failReason);
			if (null != view) {
				ImageView imageView = (ImageView) view;
				imageView.setImageResource(R.drawable.upload_portrait);
			}

		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			super.onLoadingCancelled(imageUri, view);
			if (null != view) {
				ImageView imageView = (ImageView) view;
				imageView.setImageResource(R.drawable.upload_portrait);
			}
		}

	}

	public class ModeDto {
		private boolean hasFirstLoaded = false;

		public ModeDto() {
			// TODO Auto-generated constructor stub
		}

		public boolean isHasFirstLoaded() {
			return hasFirstLoaded;
		}

		public void setHasFirstLoaded(boolean hasFirstLoaded) {
			this.hasFirstLoaded = hasFirstLoaded;
		}

	}
}

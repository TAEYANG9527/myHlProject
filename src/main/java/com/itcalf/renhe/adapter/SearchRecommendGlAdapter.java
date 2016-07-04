package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.SearchRecommendedBean;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author chan
 * @createtime 2014-11-13 
 * @功能说明 搜索推荐 GridView Adapter
 */
public class SearchRecommendGlAdapter extends BaseAdapter {
	private LayoutInflater flater;
	private List<SearchRecommendedBean> recommendedUserList = new ArrayList<SearchRecommendedBean>();
	private ModeDto modeDto;

	public SearchRecommendGlAdapter(Context ct, List<SearchRecommendedBean> recommendedUserList) {
		super();
		this.flater = LayoutInflater.from(ct);
		this.recommendedUserList = recommendedUserList;
		modeDto = new ModeDto();
	}

	@Override
	public int getCount() {
		return recommendedUserList.size() != 0 ? recommendedUserList.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {
		return recommendedUserList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = flater.inflate(R.layout.search_recommend_grid_item, null);
			viewHolder = new ViewHolder();
			viewHolder.newTagIv = (ImageView) convertView.findViewById(R.id.new_tag);
			viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.avatar_img);
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.username_txt);
			viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
			viewHolder.realnameIv = (ImageView) convertView.findViewById(R.id.realnameImage);
			viewHolder.friendsIv = (ImageView) convertView.findViewById(R.id.friendsImage);
			viewHolder.friNumbTv = (TextView) convertView.findViewById(R.id.friendsNumb_txt);
			viewHolder.positionTv = (TextView) convertView.findViewById(R.id.position_txt);
			viewHolder.mCompanyTv = (TextView) convertView.findViewById(R.id.company_txt);
			viewHolder.addressTv = (TextView) convertView.findViewById(R.id.location_txt);
			viewHolder.industryTv = (TextView) convertView.findViewById(R.id.industry_txt);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		SearchRecommendedBean recommendedUser = recommendedUserList.get(position);
		if (null != recommendedUser) {
			int state = recommendedUser.getState();
			if (state == 1) {
				viewHolder.newTagIv.setVisibility(View.VISIBLE);
			} else {
				viewHolder.newTagIv.setVisibility(View.GONE);
			}
			viewHolder.nameTv.setText(recommendedUser.getName());
			int numb = recommendedUser.getConnectionNum();
			if (numb > 0) {
				viewHolder.friendsIv.setVisibility(View.VISIBLE);
				if (numb > 299) {
					viewHolder.friNumbTv.setText("299+");
				} else {
					viewHolder.friNumbTv.setText("" + numb);
				}
			} else {
				viewHolder.friendsIv.setVisibility(View.GONE);
				viewHolder.friNumbTv.setText("");
			}

			viewHolder.positionTv.setText(recommendedUser.getTitle());
			viewHolder.mCompanyTv.setText(recommendedUser.getCompany());
			viewHolder.addressTv.setText(recommendedUser.getLocation());
			viewHolder.industryTv.setText(recommendedUser.getIndustry());
			// Vip标志显示
			int accountType = recommendedUser.getAccountType();
			switch (accountType) {
			case 0:
				viewHolder.vipIv.setVisibility(View.GONE);
				break;
			case 1:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				//				viewHolder.vipIv.setImageResource(R.drawable.vip_1);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_1);
				break;
			case 2:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				//				viewHolder.vipIv.setImageResource(R.drawable.vip_2);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_2);
				break;
			case 3:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				//				viewHolder.vipIv.setImageResource(R.drawable.vip_3);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_3);
				break;

			default:
				viewHolder.vipIv.setVisibility(View.GONE);
				break;
			}

			// 实名标志
			boolean isRealName = recommendedUser.isRealname();
			if (isRealName && accountType <= 0) {
				viewHolder.realnameIv.setVisibility(View.VISIBLE);
				//				viewHolder.realnameIv.setImageResource(R.drawable.realname);
				viewHolder.realnameIv.setImageResource(R.drawable.archive_realname);
			} else {
				viewHolder.realnameIv.setVisibility(View.GONE);
			}
			String avatarUrl = recommendedUser.getUserface();
			if (position == 0 && modeDto.isHasFirstLoaded()) {
				return convertView;
			}
			if (position == 0) {
				modeDto.setHasFirstLoaded(true);
			}
			DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.upload_portrait)
					.showImageForEmptyUri(R.drawable.upload_portrait).showImageOnFail(R.drawable.upload_portrait)
					.cacheInMemory(false).cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
					.considerExifParams(true)
					// .displayer(new RoundedBitmapDisplayer(20))
					.build();
			if (!TextUtils.isEmpty(avatarUrl)) {
				viewHolder.avatarIv.setTag(avatarUrl + position);
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(avatarUrl, viewHolder.avatarIv, options, new AnimateFirstDisplayListener());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return convertView;
	}

	public static class ViewHolder {
		public ImageView avatarIv;
		public TextView nameTv;
		public ImageView vipIv;
		public ImageView realnameIv;
		public TextView friNumbTv;
		public TextView positionTv;
		public TextView mCompanyTv;
		public TextView addressTv;
		public TextView industryTv;
		public ImageView friendsIv;
		public ImageView newTagIv;
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

		//		@Override
		//		public void onLoadingStarted(String imageUri, View view) {
		//			super.onLoadingStarted(imageUri, view);
		//		}
		//
		//		@Override
		//		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		//			super.onLoadingFailed(imageUri, view, failReason);
		//			if (null != view) {
		//				ImageView imageView = (ImageView) view;
		//				imageView.setImageResource(R.drawable.upload_portrait);
		//			}
		//
		//		}
		//
		//		@Override
		//		public void onLoadingCancelled(String imageUri, View view) {
		//			super.onLoadingCancelled(imageUri, view);
		//			if (null != view) {
		//				ImageView imageView = (ImageView) view;
		//				imageView.setImageResource(R.drawable.upload_portrait);
		//			}
		//		}

	}

	public class ModeDto {
		private boolean hasFirstLoaded = false;

		public ModeDto() {
		}

		public boolean isHasFirstLoaded() {
			return hasFirstLoaded;
		}

		public void setHasFirstLoaded(boolean hasFirstLoaded) {
			this.hasFirstLoaded = hasFirstLoaded;
		}

	}
}

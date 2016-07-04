package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.dto.RecommendedUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class RecommendListAdapter extends BaseAdapter {

	private Context ct;
	private List<RecommendedUser> recommendedUserList = new ArrayList<RecommendedUser>();
	//	private ModeDto modeDto;
	private ImageLoader imageLoader;

	public RecommendListAdapter(Context ct, List<RecommendedUser> recommendedUserList) {
		super();
		this.ct = ct;
		this.recommendedUserList = recommendedUserList;
		//		modeDto = new ModeDto();
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return recommendedUserList.size() > 6 ? 6 : recommendedUserList.size();//测试，5条
	}

	@Override
	public Object getItem(int position) {
		return recommendedUserList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = (View) LayoutInflater.from(ct).inflate(R.layout.recommend_list_item, null);
			viewHolder = new ViewHolder();

			viewHolder.isCheckedIv = (ImageView) convertView.findViewById(R.id.check_iv);
			viewHolder.findconnections_item = (RelativeLayout) convertView.findViewById(R.id.findconnections_item);
			viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.headImage);// 头像
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);// 联系人名字

			viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
			viewHolder.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
			viewHolder.rightIv = (ImageView) convertView.findViewById(R.id.rightImage);// √标志

			viewHolder.infoTv = (TextView) convertView.findViewById(R.id.infoTv);// 信息简介

			viewHolder.divideline = (View) convertView.findViewById(R.id.divideline);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position == recommendedUserList.size() - 1) {
			viewHolder.divideline.setVisibility(View.GONE);
		} else {
			viewHolder.divideline.setVisibility(View.VISIBLE);
		}

		RecommendedUser recommendedUser = recommendedUserList.get(position);
		if (null != recommendedUser) {
			viewHolder.nameTv.setText(recommendedUser.getName());
			String job = recommendedUser.getTitle();
			String company = recommendedUser.getCompany();
			if ("".equals(job) || "".equals(company))
				viewHolder.infoTv.setText(recommendedUser.getTitle() + "" + recommendedUser.getCompany());
			else
				viewHolder.infoTv.setText(recommendedUser.getTitle() + " / " + recommendedUser.getCompany());

			int accountType = recommendedUser.getAccountType();
			boolean isrealname = recommendedUser.isRealname();
			switch (accountType) {
			case 0:
				viewHolder.vipIv.setVisibility(View.GONE);
				if (isrealname) {
					viewHolder.realNameIv.setVisibility(View.VISIBLE);
					viewHolder.realNameIv.setImageResource(R.drawable.archive_realname);
				} else {
					viewHolder.realNameIv.setVisibility(View.GONE);
				}
				break;
			case 1:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_1);
				break;
			case 2:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_2);
				break;
			case 3:
				viewHolder.vipIv.setVisibility(View.VISIBLE);
				viewHolder.vipIv.setImageResource(R.drawable.archive_vip_3);
				break;
			default:
				break;
			}
			//			if (accountType > 0) {
			//				viewHolder.vipPeriodTv.setText("第" + recommendedUser.getAccountTypeYearPeriod() + "年");
			//			}
			if (recommendedUser.isChecked()) {
				viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_sel);
			} else {
				viewHolder.isCheckedIv.setImageResource(R.drawable.icon_recommend_unsel);
			}
			String avatarUrl = recommendedUser.getUserface();
			//			if (position == 0 && modeDto.isHasFirstLoaded()) {
			//				return convertView;
			//			}
			//			if (position == 0) {
			//				modeDto.setHasFirstLoaded(true);
			//			}
			if (!TextUtils.isEmpty(avatarUrl)) {
				viewHolder.isCheckedIv.setTag(avatarUrl + position);
				try {
					imageLoader.displayImage(avatarUrl, viewHolder.avatarIv, CacheManager.options,
							CacheManager.imageAnimateFirstDisplayListener);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return convertView;
	}

	public static class ViewHolder {
		public ImageView isCheckedIv;
		RelativeLayout findconnections_item;
		ImageView avatarIv;
		TextView nameTv;
		ImageView realNameIv;
		ImageView vipIv;
		ImageView rightIv;
		TextView friendsNumbTv;
		TextView infoTv;
		View divideline;
	}

	//	public class ModeDto {
	//		private boolean hasFirstLoaded = false;
	//
	//		public ModeDto() {
	//		}
	//
	//		public boolean isHasFirstLoaded() {
	//			return hasFirstLoaded;
	//		}
	//
	//		public void setHasFirstLoaded(boolean hasFirstLoaded) {
	//			this.hasFirstLoaded = hasFirstLoaded;
	//		}
	//	}
}

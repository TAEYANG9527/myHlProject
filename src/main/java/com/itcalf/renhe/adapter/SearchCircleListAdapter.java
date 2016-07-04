package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CirCleInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.renhe.heliao.idl.circle.SearchCircle;

/**
 * @author       chan
 * @createtime   2015-1-27
 * @功能说明       搜索圈子列表适配器
 */
public class SearchCircleListAdapter extends BaseAdapter {
	private List<SearchCircle.CircleInfo> circleList;
	private Context context;
	private ImageLoader imageLoader;

	public SearchCircleListAdapter(Context _context, List<SearchCircle.CircleInfo> _circleList) {
		this.circleList = _circleList;
		this.context = _context;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return circleList.size();
	}

	@Override
	public Object getItem(int position) {
		return circleList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewhold = null;
		if (convertView == null) {
			viewhold = new ViewHolder();
			convertView = (View) LayoutInflater.from(context).inflate(R.layout.searchcircle_listitem, null);
			viewhold.circleAvatar = (ImageView) convertView.findViewById(R.id.circleAvatar_img);// 头像
			viewhold.circleName = (TextView) convertView.findViewById(R.id.circleName_txt);// 圈名
			viewhold.circleNumb = (TextView) convertView.findViewById(R.id.circleNumb_txt);// 圈号
			viewhold.circleCount = (TextView) convertView.findViewById(R.id.circleCount_txt);//人数
			convertView.setTag(viewhold);
		} else {
			viewhold = (ViewHolder) convertView.getTag();
		}
		SearchCircle.CircleInfo circleBean = circleList.get(position);
		if (circleBean != null) {
			String avatarUrl = circleBean.getAvatar();
			String circleName = circleBean.getName();
			int circleNumb = circleBean.getId();
			int circleCount = circleBean.getMemberCount();

			try {
				imageLoader.displayImage(avatarUrl, viewhold.circleAvatar, CacheManager.options,
						CacheManager.animateFirstDisplayListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
			viewhold.circleName.setText(circleName);
			viewhold.circleNumb.setText("圈号：" + circleNumb);
			viewhold.circleCount.setText(circleCount + "人");
		}
		return convertView;
	}

	public static class ViewHolder {
		ImageView circleAvatar;
		TextView circleName;
		TextView circleNumb;
		TextView circleCount;
	}
}
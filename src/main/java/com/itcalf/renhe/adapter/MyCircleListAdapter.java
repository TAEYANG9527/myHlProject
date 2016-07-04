package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CircleBean;
import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @author       chan
 * @createtime   2015-1-19
 * @功能说明       我的圈子列表适配器
 */
public class MyCircleListAdapter extends BaseAdapter {
	private List<CircleBean> circleList;
	private Context context;
	private ImageLoader imageLoader;

	public MyCircleListAdapter(Context _context, List<CircleBean> _circleList) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.mycircle_listitem, null);
			viewhold.circleAvatar = (ImageView) convertView.findViewById(R.id.circleAvatar_img);// 头像
			viewhold.circleName = (TextView) convertView.findViewById(R.id.circleName_txt);// 圈名
			viewhold.circleCount = (TextView) convertView.findViewById(R.id.circleCount_txt);//人数

			viewhold.title_txt_container = (LinearLayout) convertView.findViewById(R.id.title_txt_container);
			viewhold.title_txt = (TextView) convertView.findViewById(R.id.title_txt);

			viewhold.circle_list_divide = (View) convertView.findViewById(R.id.circle_list_divide);
			convertView.setTag(viewhold);
		} else {
			viewhold = (ViewHolder) convertView.getTag();
		}

		int currentStr = circleList.get(position).getRoleType();
		int previewStr = (position - 1) >= 0 ? circleList.get(position - 1).getRoleType() : 0;
		if (currentStr != previewStr) {
			viewhold.title_txt_container.setVisibility(View.VISIBLE);
		} else {
			viewhold.title_txt_container.setVisibility(View.GONE);
		}

		int myCreateCount = circleList.get(position).getMyCreateCount();
		int joinCount = circleList.get(position).getParticipatedCount();
		if (currentStr == 1) {
			viewhold.title_txt.setText("我创建的圈子(" + myCreateCount + ")");
		} else if (currentStr == 3) {
			viewhold.title_txt.setText("我加入的圈子(" + joinCount + ")");
		}

		if ((myCreateCount > 0 && position == myCreateCount - 1)
				|| (joinCount > 0 && position == joinCount + myCreateCount - 1)) {
			viewhold.circle_list_divide.setVisibility(View.GONE);
		} else {
			viewhold.circle_list_divide.setVisibility(View.VISIBLE);
		}

		CircleBean circleBean = circleList.get(position);
		if (circleBean != null) {
			String avatarUrl = circleBean.getAvater();
			String circleName = circleBean.getName();
			//			int circleNumb = circleBean.getNumber();
			int circleCount = circleBean.getCount();

			try {
				imageLoader.displayImage(avatarUrl, viewhold.circleAvatar, CacheManager.circleImageOptions);
			} catch (Exception e) {
				e.printStackTrace();
			}
			viewhold.circleName.setText(circleName);
			viewhold.circleCount.setText("（" + circleCount + "）");
		}
		return convertView;
	}

	public static class ViewHolder {
		ImageView circleAvatar;
		TextView circleName;
		TextView circleCount;
		LinearLayout title_txt_container;
		TextView title_txt;
		View circle_list_divide;
	}
}

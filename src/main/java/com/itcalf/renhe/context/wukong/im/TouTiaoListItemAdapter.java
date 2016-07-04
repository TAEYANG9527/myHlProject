package com.itcalf.renhe.context.wukong.im;

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
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiao;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author 王宁
 * 
 */
public class TouTiaoListItemAdapter extends BaseAdapter {
	private Context ct;
	// 留言显示数据
	private TouTiao[] mTouTiaos;
	private LayoutInflater flater;
	private int DEFAULT_IMAGE;
	private DisplayImageOptions options;

	@SuppressWarnings("unchecked")
	public TouTiaoListItemAdapter(Context context, TouTiao[] mTouTiaos) {
		this.flater = LayoutInflater.from(context);
		this.mTouTiaos = mTouTiaos;
		this.ct = context;
		DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
		options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
				.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (null != mTouTiaos) {
			TouTiao tiao = mTouTiaos[position];
			if (convertView == null) {
				if (tiao.getOrders() == 1) {
					convertView = flater.inflate(R.layout.toutiao_item_order_first, null);
				} else {
					convertView = flater.inflate(R.layout.toutiao_item_order_other, null);
				}
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) convertView.findViewById(R.id.toutiao_order_iv);
				viewHolder.titleTv = (TextView) convertView.findViewById(R.id.titleTv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final String url = tiao.getImage();
			if (!TextUtils.isEmpty(url)) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(url, viewHolder.imageView, options, new AnimateFirstDisplayListener());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!TextUtils.isEmpty(tiao.getTitle())) {
				viewHolder.titleTv.setText(tiao.getTitle());
			}
		}

		return convertView;

	}

	@Override
	public int getCount() {
		return mTouTiaos.length;
	}

	@Override
	public Object getItem(int arg0) {
		return mTouTiaos[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public static class ViewHolder {
		public ImageView imageView;
		public TextView titleTv;

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
		//				imageView.setImageResource(DEFAULT_IMAGE);
		//			}
		//
		//		}
		//
		//		@Override
		//		public void onLoadingCancelled(String imageUri, View view) {
		//			super.onLoadingCancelled(imageUri, view);
		//			if (null != view) {
		//				ImageView imageView = (ImageView) view;
		//				imageView.setImageResource(DEFAULT_IMAGE);
		//			}
		//		}

	}

	private boolean isViewExist(ViewGroup parent, int id, int messageId) {
		boolean flag = false;
		String childViewTag = id + "";
		if (null != parent && parent.getTag() != null && parent.getTag().toString().equals(messageId + "")) {
			for (int i = 0; i < parent.getChildCount(); i++) {
				Object viewTag = parent.getChildAt(i).getTag();
				if (null != viewTag.toString() && viewTag.toString().equals(childViewTag)) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}
}
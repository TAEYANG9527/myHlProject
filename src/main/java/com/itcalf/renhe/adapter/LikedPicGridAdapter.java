package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LikedPicGridAdapter extends BaseAdapter {
	private Context context;
	private com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList;
	private LayoutInflater inflater; // 视图容器
	private int selectedPosition = -1;// 选中的位置
	private boolean shape;
	private ModeDto modeDto;
	private int DEFAULT_IMAGE;
	private int replyNum;

	public boolean isShape() {
		return shape;
	}

	public void setShape(boolean shape) {
		this.shape = shape;
	}

	public LikedPicGridAdapter(Context context, com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList, int replyNum) {
		this.context = context;
		this.likedList = likedList;
		inflater = LayoutInflater.from(context);
		modeDto = new ModeDto();
		DEFAULT_IMAGE = R.drawable.avatar;
		this.replyNum = replyNum;
	}

	public int getCount() {
		if (likedList.length <= TwitterShowMessageBoardActivity.GOOD_LIST_MAX_NUM) {
			return likedList.length;
		} else {
			return TwitterShowMessageBoardActivity.GOOD_LIST_MAX_NUM + 1;
		}
	}

	public Object getItem(int arg0) {

		return null;
	}

	public long getItemId(int arg0) {

		return 0;
	}

	public void setSelectedPosition(int position) {
		selectedPosition = position;
	}

	public int getSelectedPosition() {
		return selectedPosition;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		final int coord = position;
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_liked_grid, parent, false);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
			holder.item_grida_rl = (RelativeLayout) convertView.findViewById(R.id.item_grida_rl);
			holder.item_grida_textview_rl = (RelativeLayout) convertView.findViewById(R.id.item_grida_textview_rl);
			holder.item_grid_textview = (TextView) convertView.findViewById(R.id.item_grid_textview);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == 0 && modeDto.isHasFirstLoaded()) {
			return convertView;
		}
		if (position == 0) {
			modeDto.setHasFirstLoaded(true);
		}
		DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE)
				.showImageForEmptyUri(DEFAULT_IMAGE).showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
		if (replyNum <= TwitterShowMessageBoardActivity.GOOD_LIST_MAX_NUM) {
			holder.item_grida_rl.setVisibility(View.VISIBLE);
			holder.item_grida_textview_rl.setVisibility(View.GONE);
			String url = likedList[position].getUserface();
			if (!TextUtils.isEmpty(url)) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(url, holder.image, options, new AnimateFirstDisplayListener());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if (likedList.length <= TwitterShowMessageBoardActivity.GOOD_LIST_MAX_NUM) {
				if (position == likedList.length - 1) {
					holder.item_grida_rl.setVisibility(View.GONE);
					holder.item_grid_textview.setText("等" + replyNum + "人");
					holder.item_grida_textview_rl.setVisibility(View.VISIBLE);
				} else {
					holder.item_grida_rl.setVisibility(View.VISIBLE);
					holder.item_grida_textview_rl.setVisibility(View.GONE);
					if (position < likedList.length - 1) {
						String url = likedList[position].getUserface();
						if (!TextUtils.isEmpty(url)) {
							ImageLoader imageLoader = ImageLoader.getInstance();
							try {
								imageLoader.displayImage(url, holder.image, options, new AnimateFirstDisplayListener());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				if (position == TwitterShowMessageBoardActivity.GOOD_LIST_MAX_NUM) {
					holder.item_grida_rl.setVisibility(View.GONE);
					holder.item_grid_textview.setText("等" + replyNum + "人");
					holder.item_grida_textview_rl.setVisibility(View.VISIBLE);
				} else {
					holder.item_grida_rl.setVisibility(View.VISIBLE);
					holder.item_grida_textview_rl.setVisibility(View.GONE);
					String url = likedList[position].getUserface();
					if (!TextUtils.isEmpty(url)) {
						ImageLoader imageLoader = ImageLoader.getInstance();
						try {
							imageLoader.displayImage(url, holder.image, options, new AnimateFirstDisplayListener());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		return convertView;
	}

	public class ViewHolder {
		public ImageView image;
		public RelativeLayout item_grida_rl;
		public RelativeLayout item_grida_textview_rl;
		public TextView item_grid_textview;
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
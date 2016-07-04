package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RoomRemotePicGridAdapter extends BaseAdapter {
	private Context context;
	private PicList[] picLists;
	private LayoutInflater inflater; // 视图容器
	private int selectedPosition = -1;// 选中的位置
	private boolean shape;
	private ModeDto modeDto;
	private int DEFAULT_IMAGE;

	public boolean isShape() {
		return shape;
	}

	public void setShape(boolean shape) {
		this.shape = shape;
	}

	public RoomRemotePicGridAdapter(Context context, PicList[] picLists) {
		this.context = context;
		this.picLists = picLists;
		inflater = LayoutInflater.from(context);
		modeDto = new ModeDto();
		DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
	}

	public int getCount() {
		return picLists.length;
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
			convertView = inflater.inflate(R.layout.item_published_grida, parent, false);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
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
		if (position < picLists.length) {
			String url = picLists[position].getThumbnailPicUrl();
			if (!TextUtils.isEmpty(url)) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(url, holder.image, CacheManager.imageOptions);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return convertView;
	}

	public class ViewHolder {
		public ImageView image;
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
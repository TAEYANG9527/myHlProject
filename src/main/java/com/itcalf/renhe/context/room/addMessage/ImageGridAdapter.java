package com.itcalf.renhe.context.room.addMessage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.room.addMessage.BitmapCache.ImageCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageGridAdapter extends BaseAdapter {

	private TextCallback textcallback = null;
	final String TAG = getClass().getSimpleName();
	Activity act;
	List<ImageItem> dataList;
	Map<String, String> map = new HashMap<String, String>();
	BitmapCache cache;
	private Handler mHandler;
	private int selectTotal = 0;
	ImageCallback callback = new ImageCallback() {
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag())) {
					((ImageView) imageView).setImageBitmap(bitmap);
				} else {
				}
			} else {
			}
		}
	};

	public static interface TextCallback {
		public void onListen(int count);
	}

	public void setTextCallback(TextCallback listener) {
		textcallback = listener;
	}

	public ImageGridAdapter(Activity act, List<ImageItem> list, Handler mHandler) {
		this.act = act;
		dataList = list;
		cache = new BitmapCache();
		this.mHandler = mHandler;
	}

	@Override
	public int getCount() {
		int count = 0;
		if (dataList != null) {
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class Holder {
		private ImageView iv;
		private ImageView selected;
		private TextView text;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;

		if (convertView == null) {
			holder = new Holder();
			convertView = View.inflate(act, R.layout.item_image_grid, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.image);
			holder.selected = (ImageView) convertView.findViewById(R.id.isselected);
			holder.text = (TextView) convertView.findViewById(R.id.item_image_grid_text);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = dataList.get(position);

		holder.iv.setTag(item.imagePath);
		cache.displayBmp(holder.iv, item.thumbnailPath, item.imagePath, callback);
		if (item.isSelected) {
			holder.selected.setVisibility(View.VISIBLE);
			holder.selected.setImageResource(R.drawable.icon_recommend_sel);
			holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
		} else {
			holder.selected.setVisibility(View.VISIBLE);
			holder.selected.setImageResource(R.drawable.icon_recommend_unsel);
			holder.text.setBackgroundResource(android.R.color.transparent);
		}
		holder.iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String path = dataList.get(position).imagePath;

				if ((Bimp.drr.size() + selectTotal) < Bimp.MAX_SIZE) {
					item.isSelected = !item.isSelected;
					if (item.isSelected) {
						holder.selected.setVisibility(View.VISIBLE);
						holder.selected.setImageResource(R.drawable.icon_recommend_sel);
						holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
						//						Bimp.drr.add(path);
						Bimp.curDrr.add(path);
						selectTotal++;
						if (textcallback != null)
							textcallback.onListen(selectTotal + Bimp.drr.size());
						//						map.put(path, path);

					} else if (!item.isSelected) {
						holder.selected.setVisibility(View.VISIBLE);
						holder.selected.setImageResource(R.drawable.icon_recommend_unsel);
						holder.text.setBackgroundResource(android.R.color.transparent);

						//						Bimp.drr.remove(path);
						Bimp.curDrr.remove(path);
						selectTotal--;
						if (textcallback != null)
							textcallback.onListen(selectTotal + Bimp.drr.size());
						//						map.remove(path);
					}
				} else if ((Bimp.drr.size() + selectTotal) >= Bimp.MAX_SIZE) {
					if (item.isSelected == true) {
						item.isSelected = !item.isSelected;
						holder.selected.setVisibility(View.GONE);
						holder.text.setBackgroundResource(android.R.color.transparent);
						//						Bimp.drr.remove(path);
						Bimp.curDrr.remove(path);
						selectTotal--;
						if (textcallback != null)
							textcallback.onListen(selectTotal + Bimp.drr.size());
						//						map.remove(path);

					} else {
						Message message = Message.obtain(mHandler, 0);
						message.sendToTarget();
					}
				}
			}

		});

		return convertView;
	}
}

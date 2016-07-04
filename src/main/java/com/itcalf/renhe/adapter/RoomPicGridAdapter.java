package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.BitmapUtil;

public class RoomPicGridAdapter extends BaseAdapter {
	private Context context;
	private CharSequence[] bitmaps;
	private LayoutInflater inflater; // 视图容器
	private int selectedPosition = -1;// 选中的位置
	private boolean shape;

	public boolean isShape() {
		return shape;
	}

	public void setShape(boolean shape) {
		this.shape = shape;
	}

	public RoomPicGridAdapter(Context context, CharSequence[] bitmaps) {
		this.context = context;
		this.bitmaps = bitmaps;
		inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return bitmaps.length;
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

		try {
			if (null != bitmaps) {
				Bitmap bitmap = BitmapUtil.getBitmap(bitmaps[position].toString(), 220, 220);
				//					Bitmap bitmap = BitmapFactory.decodeFile(bitmaps[position].toString());
				holder.image.setImageBitmap(bitmap);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public class ViewHolder {
		public ImageView image;
	}
}
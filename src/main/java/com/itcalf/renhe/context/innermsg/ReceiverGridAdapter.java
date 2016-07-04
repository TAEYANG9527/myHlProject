package com.itcalf.renhe.context.innermsg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.po.Contact;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ReceiverGridAdapter extends BaseAdapter {
	public static final String TAG = "ReceiverGridAdapter";
	private Context context;
	private List<HlContactRenheMember> list;
	private ImageLoader imageLoader;

	public ReceiverGridAdapter(Context context, List<HlContactRenheMember> list) {
		this.context = context;
		this.list = list;
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder mHolder;
		if (convertView == null) {
			mHolder = new Holder();
			convertView = LayoutInflater.from(context).inflate(R.layout.select_receiver, null);
			mHolder.avarterIv = (ImageView) convertView.findViewById(R.id.avarterIv);
			convertView.setTag(mHolder);
		} else {
			mHolder = (Holder) convertView.getTag();
		}
		HlContactRenheMember HlContactRenheMember = list.get(position);
		imageLoader.displayImage(HlContactRenheMember.getUserface(), mHolder.avarterIv);
		return convertView;
	}

	class Holder {
		ImageView avarterIv;
	}

}

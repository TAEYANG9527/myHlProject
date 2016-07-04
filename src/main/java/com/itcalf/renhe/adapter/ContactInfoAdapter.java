package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ContactInfoBean;

import java.util.ArrayList;
import java.util.List;

public class ContactInfoAdapter extends BaseAdapter {
	Context mcontext;
	private List<ContactInfoBean> customlist = new ArrayList<ContactInfoBean>();

	public ContactInfoAdapter(Context _ctx, List<ContactInfoBean> _customlist) {
		// TODO Auto-generated constructor stub
		this.mcontext = _ctx;
		this.customlist = _customlist;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return customlist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return customlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mcontext).inflate(R.layout.customeradd_maillist_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.contactName.setText(customlist.get(position).getName());

		return convertView;
	}

	class ViewHolder {
		public TextView contactName;

		ViewHolder(View view) {
			contactName = (TextView) view.findViewById(R.id.contactName);
		}
	}
}

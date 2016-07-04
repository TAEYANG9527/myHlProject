package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.view.MyListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MemberContactAdapter extends BaseAdapter {
	private String keyword = ""; // 首字母搜索索引
	private Context mContext;
	private MemberInfo memberInfo;
	private boolean isJurisdiction;
	private ArrayList<HashMap<String, Object>> arrary;
	private LayoutInflater inflater;
	private HashSet<Long> set = new HashSet<Long>(); // 记录勾选的状态
	private HashSet<Long> deleteSet = new HashSet<Long>(); // 记录删除的对象

	public MemberContactAdapter(Context context, ArrayList<HashMap<String, Object>> map2, MemberInfo memberInfo,
			boolean isJurisdiction) {
		this.mContext = context;
		this.arrary = map2;
		this.memberInfo = memberInfo;
		this.isJurisdiction = isJurisdiction;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return arrary.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<HashMap<String, Object>> getAll() {
		return arrary;
	}

	public void updata(ArrayList<HashMap<String, Object>> array1) {
		this.arrary = array1;
	}

	public void selectKeyword(String key) {
		this.keyword = key;
	}

	/**
	 * 列表中清除所选删除人
	 * */
	public void removeMember(List<Long> openId) {
		ArrayList<HashMap<String, Object>> arrary3 = new ArrayList<HashMap<String, Object>>();
		for (int j = 0; j < arrary.size(); j++) {
			HashMap<String, Object> map = arrary.get(j);
			ArrayList<MemberInfo> array1 = (ArrayList<MemberInfo>) map
					.get(map.keySet().toString().replace("[", "").replace("]", ""));

			HashMap<String, Object> map2 = new HashMap<String, Object>();
			ArrayList<MemberInfo> arrary4 = new ArrayList<MemberInfo>();
			for (int k = 0; k < array1.size(); k++) {
				boolean isTag = true;
				for (int i = 0; i < openId.size(); i++) {
					if (array1.get(k).getOpenId() == openId.get(i)) {
						isTag = false;
					}
					deleteSet.add(openId.get(i));
				}

				if (isTag) {
					arrary4.add(array1.get(k));
				}
			}
			if (arrary4.size() > 0) {
				map2.put(map.keySet().toString().replace("[", "").replace("]", ""), arrary4);
				arrary3.add(map2);
			}
		}
		arrary = arrary3;
		notifyDataSetChanged();
	}

	public void removeSet(Long[] l) {
		for (int i = 0; i < l.length; i++) {
			set.remove(l[i]);
		}
	}

	/**
	 * 返回已勾选的成员
	 * */
	public HashSet<Long> getClickChexBox() {
		return set;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = inflater.inflate(R.layout.member_lsit_item, null);
			vh.list = (MyListView) convertView.findViewById(R.id.list);
			vh.firstCharHint = (TextView) convertView.findViewById(R.id.tx_charHint);
			vh.ly_item = (LinearLayout) convertView.findViewById(R.id.ly_item);
			vh.setOnClick();
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.pos = position;
		HashMap<String, Object> map = arrary.get(position);

		// if (keyword.length() == 0 ||
		// keyword.equals(map.keySet().toString().replace("[", "").replace("]",
		// "")))
		// {
		ArrayList<MemberInfo> array = (ArrayList<MemberInfo>) map.get(map.keySet().toString().replace("[", "").replace("]", ""));
		if (array != null && array.size() > 0) {
			MemberUserAdapter adapter = new MemberUserAdapter(mContext, array, memberInfo, set, deleteSet, isJurisdiction);
			vh.list.setAdapter(adapter);

			if (deleteSet.contains(array.get(0).getOpenId()) && array.size() == 1) {
				vh.firstCharHint.setVisibility(View.GONE);
			} else {
				vh.firstCharHint.setText(map.keySet().toString().replace("[", "").replace("]", ""));
				vh.firstCharHint.setVisibility(View.VISIBLE);
			}
		}
		// vh.ly_item.setVisibility(View.VISIBLE);
		// } else
		// {
		// vh.ly_item.setVisibility(View.GONE);
		// }
		return convertView;
	}

	private class ViewHolder {
		private int pos;
		private TextView firstCharHint;
		private MyListView list;
		private LinearLayout ly_item;

		void setOnClick() {
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					LinearLayout ly = (LinearLayout) list.getChildAt(position);
					HashMap<String, Object> map = arrary.get(pos);
					ArrayList<MemberInfo> array = (ArrayList<MemberInfo>) map
							.get(map.keySet().toString().replace("[", "").replace("]", ""));
					if (set.contains(array.get(position).getOpenId()))
						set.remove(array.get(position).getOpenId());
					else
						set.add(array.get(position).getOpenId());

					CheckBox cb = (CheckBox) ly.findViewById(R.id.cb);
					cb.setChecked(set.contains(array.get(position).getOpenId()) ? true : false);
				}
			});
		}
	}
}

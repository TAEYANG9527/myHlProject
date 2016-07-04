package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ListView适配器抽象类。实现者需要自己实现一下类或接口：<br/>
 * onCreateViewHolder: 加载布局文件并实例化View给入参convertView，同时创建ViewHolder与该convertView关联<br/>
 * onBindViewHolder: 将数据与组件绑定   <br/>
 * ViewHolder: 内部抽象类，封装组件集   <be/>
 * 可以根据需要实现getItemViewType和getViewTypeCount接口<br/>
 * <br/>
 * Created by zhongqian.wzq on 2014/10/29.
 */
public abstract class ListAdapter<T> extends BaseAdapter {
	private LayoutInflater mInflater;
	protected List<T> mList;
	private int[] mItemResource;

	public ListAdapter(Context context, int... itemRes) {
		mList = new ArrayList<T>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemResource = itemRes;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public T getItem(int position) {
		if (position < mList.size()) {
			return mList.size() > 0 ? mList.get(position) : null;
		}
		return null;
	}

	/**实现者根据需求自己实现mList的排序*/
	public void sort() {
	}

	public void setItems(List<T> list) {
		if (list == null || list.size() == 0)
			return;

		for (T t : list) {
			if (!mList.contains(t)) {
				mList.add(t);
			}
		}
		sort();
		notifyDataSetChanged();
	}

	public void setFirstItems(List<T> list) {
		if (list == null || list.size() == 0)
			return;

		for (int i = list.size() - 1; i >= 0; i--) {
			if (!mList.contains(list.get(i))) {
				mList.add(0, list.get(i));
			}
		}
		sort();
		notifyDataSetChanged();
	}

	public void updateItems(List<T> list) {
		if (list == null || list.size() == 0)
			return;

		for (T t : list)
			updateItem(t);
	}

	public void updateItem(T t) {
		if (t == null)
			return;

		boolean result = false;
		int targetPosition = 0;

		if (mList.size() > 0) {
			Iterator<T> it = mList.iterator();

			while (it.hasNext()) {
				T current = it.next();
				if (current.equals(t)) {
					result = true;
					break;
				}
				targetPosition++;
			}

			//新的数据替换老的数据
			if (result) {
				mList.set(targetPosition, t);
			} else {
				mList.add(t);
			}
		}
		sort();
		notifyDataSetChanged();
	}

	public void removeItems(List<T> list) {
		if (list == null || list.size() == 0)
			return;

		for (T t : list) {
			if (mList.contains(t))
				mList.remove(t);
		}

		sort();
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(mItemResource[getItemViewType(position)], null);
		}
		onBindViewHolder(onCreateViewHolder(convertView), getItem(position), position);
		return convertView;
	}

	public abstract ViewHolder onCreateViewHolder(View convertView);

	public abstract void onBindViewHolder(ViewHolder holder, T t, int position);

	public abstract class ViewHolder {
	}
}

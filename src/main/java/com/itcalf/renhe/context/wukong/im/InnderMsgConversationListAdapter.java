package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.itcalf.renhe.dto.IMInnerMsgConversationListOperation.UserInnerMsgConversation;

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
public abstract class InnderMsgConversationListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	protected List<UserInnerMsgConversation> mList;
	private int[] mItemResource;

	public InnderMsgConversationListAdapter(Context context, int... itemRes) {
		mList = new ArrayList<UserInnerMsgConversation>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemResource = itemRes;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public UserInnerMsgConversation getItem(int position) {
		return mList.get(position);
	}

	/**实现者根据需求自己实现mList的排序*/
	public void sort() {
	}

	public void setItems(List<UserInnerMsgConversation> list) {
		if (list == null || list.size() == 0)
			return;

		for (UserInnerMsgConversation UserInnerMsgConversation : list) {
			if (!isItemExist(mList, UserInnerMsgConversation)) {
				mList.add(UserInnerMsgConversation);
			}
		}
		sort();
		notifyDataSetChanged();
	}

	public void updateItems(List<UserInnerMsgConversation> list) {
		if (list == null || list.size() == 0)
			return;

		for (UserInnerMsgConversation userInnerMsgConversation : list)
			updateItem(userInnerMsgConversation);
	}

	public void updateItem(UserInnerMsgConversation userInnerMsgConversation) {
		if (userInnerMsgConversation == null)
			return;

		boolean result = false;
		int targetPosition = 0;

		Iterator<UserInnerMsgConversation> it = mList.iterator();

		while (it.hasNext()) {
			UserInnerMsgConversation current = it.next();
			if (current.getId() == userInnerMsgConversation.getId()) {
				result = true;
				break;
			}
			targetPosition++;
		}

		//新的数据替换老的数据
		if (result) {
			mList.set(targetPosition, userInnerMsgConversation);
		} else {
			mList.add(userInnerMsgConversation);
		}
		sort();
		notifyDataSetChanged();
	}

	public void removeItems(List<UserInnerMsgConversation> list) {
		if (list == null || list.size() == 0)
			return;

		for (UserInnerMsgConversation UserInnerMsgConversation : list) {
			if (isItemExist(mList, UserInnerMsgConversation))
				mList.remove(UserInnerMsgConversation);
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

	public boolean isItemExist(List<UserInnerMsgConversation> mList, UserInnerMsgConversation userInnerMsgConversation) {
		boolean flag = false;
		for (UserInnerMsgConversation item : mList) {
			if (item.getId() == userInnerMsgConversation.getId()) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public abstract ViewHolder onCreateViewHolder(View convertView);

	public abstract void onBindViewHolder(ViewHolder holder, UserInnerMsgConversation userInnerMsgConversation, int position);

	public abstract class ViewHolder {
	}
}

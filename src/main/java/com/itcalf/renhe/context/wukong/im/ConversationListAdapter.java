package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.itcalf.renhe.dto.ConversationItem;

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
public abstract class ConversationListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	protected List<ConversationItem> mList;
	private int[] mItemResource;

	public ConversationListAdapter(Context context, int... itemRes) {
		mList = new ArrayList<ConversationItem>();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemResource = itemRes;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public ConversationItem getItem(int position) {
		return mList.get(position);
	}

	/**实现者根据需求自己实现mList的排序*/
	public void sort() {
	}

	public void setItems(List<ConversationItem> list) {
		if (list == null || list.size() == 0)
			return;

		for (ConversationItem conversationItem : list) {
			if (!isItemExist(mList, conversationItem)) {
				mList.add(conversationItem);
			} else {
				updateItem(conversationItem);
			}
		}
		sort();
		notifyDataSetChanged();
	}

	public void updateItems(List<ConversationItem> list) {
		if (list == null || list.size() == 0)
			return;

		for (ConversationItem conversationItem : list)
			updateItem(conversationItem);
	}

	public void updateItem(ConversationItem conversationItem) {
		if (conversationItem == null)
			return;
		int updatePosition = isIdItemExist(mList, conversationItem);
		if (updatePosition >= 0 || conversationItem.getType() != ConversationItem.IM_CONVERSATION_TYPE) {
			boolean result = false;
			int targetPosition = 0;
			Iterator<ConversationItem> it = mList.iterator();
			while (it.hasNext()) {
				ConversationItem current = it.next();
				if (conversationItem.getType() != ConversationItem.IM_CONVERSATION_TYPE) {
					if (current.getType() == conversationItem.getType()) {
						result = true;
						break;
					}
				} else {
					if (current.getType() == conversationItem.getType() && current.getConversation().conversationId()
							.equals(conversationItem.getConversation().conversationId())) {
						result = true;
						break;
					}
				}
				targetPosition++;
			}

			//新的数据替换老的数据
			if (result) {
				mList.set(targetPosition, conversationItem);
			} else {
				mList.add(conversationItem);
			}
			sort();
			notifyDataSetChanged();
		} else if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
			mList.add(conversationItem);
		}
	}

	public void removeItems(List<ConversationItem> list) {
		if (list == null || list.size() == 0)
			return;

		for (ConversationItem conversationItem : list) {
			int deletePosition = isIdItemExist(mList, conversationItem);
			if (deletePosition >= 0) {
				mList.remove(deletePosition);
			}
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

	public boolean isItemExist(ConversationItem conversationItem) {
		return isItemExist(mList, conversationItem);
	}

	public boolean isItemExist(List<ConversationItem> mList, ConversationItem conversationItem) {
		boolean flag = false;
		for (ConversationItem item : mList) {
			if (conversationItem.getType() != ConversationItem.IM_CONVERSATION_TYPE) {
				if (item.getType() == conversationItem.getType()) {
					flag = true;
					break;
				}
			} else {
				if (item.getType() == conversationItem.getType()
						&& item.getConversation().conversationId().equals(conversationItem.getConversation().conversationId())) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	public int isIdItemExist(List<ConversationItem> mList, ConversationItem conversationItem) {
		int position = -1;
		for (int i = 0; i < mList.size(); i++) {
			ConversationItem conversationItem2 = mList.get(i);
			if (conversationItem.getType() != ConversationItem.IM_CONVERSATION_TYPE) {
				if (conversationItem2.getType() == conversationItem.getType()) {
					position = i;
					break;
				}
			} else {
				if (conversationItem2.getType() == conversationItem.getType() && conversationItem2.getConversation()
						.conversationId().equals(conversationItem.getConversation().conversationId())) {
					position = i;
					break;
				}
			}
		}

		return position;
	}

	public abstract ViewHolder onCreateViewHolder(View convertView);

	public abstract void onBindViewHolder(ViewHolder holder, ConversationItem ConversationItem, int position);

	public abstract class ViewHolder {
	}
}

package com.itcalf.renhe.context.relationship.selectindustry;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.SearchCity;

import java.util.ArrayList;
import java.util.List;

public class TreeViewAdapter extends BaseExpandableListAdapter {
	private LayoutInflater flater;
	public static final int ItemHeight = 48;
	public static final int PaddingLeft = 38;
	private int myPaddingLeft = 0;
	private int selectedId;
	private ExpandableListView expandableListView;

	static public class TreeNode {
		SearchCity parent;
		List<SearchCity> childs = new ArrayList<SearchCity>();
	}

	List<TreeNode> treeNodes = new ArrayList<TreeNode>();
	Context parentContext;

	public TreeViewAdapter(Context context, int myPaddingLeft, ExpandableListView expandableListView, int selectedId) {
		this.flater = LayoutInflater.from(context);
		parentContext = context;
		this.expandableListView = expandableListView;
		this.myPaddingLeft = myPaddingLeft;
		this.selectedId = selectedId;
	}

	public List<TreeNode> getTreeNode() {
		return treeNodes;
	}

	public void updateTreeNode(List<TreeNode> nodes) {
		treeNodes = nodes;
	}

	public void removeAll() {
		treeNodes.clear();
	}

	@Override
	public SearchCity getChild(int groupPosition, int childPosition) {
		return treeNodes.get(groupPosition).childs.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	static public TextView getTextView(Context context) {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ItemHeight);
		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		return textView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		convertView = flater.inflate(R.layout.city_list_item, null);
		//		TextView textView = getTextView(this.parentContext);
		TextView textView = (TextView) convertView.findViewById(R.id.city_name);
		if (null != getChild(groupPosition, childPosition)) {
			textView.setText(getChild(groupPosition, childPosition).getName());
		}
		if (selectedId == getGroup(groupPosition).getId()) {
			((ImageView) convertView.findViewById(R.id.checkedIv)).setVisibility(View.VISIBLE);
		} else {
			if (selectedId == getChild(groupPosition, childPosition).getId()) {
				((ImageView) convertView.findViewById(R.id.checkedIv)).setVisibility(View.VISIBLE);
			} else {
				((ImageView) convertView.findViewById(R.id.checkedIv)).setVisibility(View.GONE);
			}
		}
		textView.setPadding(myPaddingLeft + PaddingLeft, 0, 0, 0);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return treeNodes.get(groupPosition).childs.size();
	}

	@Override
	public SearchCity getGroup(int groupPosition) {
		return treeNodes.get(groupPosition).parent;
	}

	@Override
	public int getGroupCount() {
		return treeNodes.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		convertView = flater.inflate(R.layout.city_list_section_item, null);
		//		TextView textView = getTextView(this.parentContext);
		TextView textView = (TextView) convertView.findViewById(R.id.city_name);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.checkedIv);
		if (isExpanded) {
			imageView.setImageResource(R.drawable.homepage_circle_arrow_up);
		} else {
			imageView.setImageResource(R.drawable.homepage_circle_arrow_down);
		}
		if (null != getGroup(groupPosition)) {
			textView.setText(getGroup(groupPosition).getName());
		}
		textView.setPadding((myPaddingLeft + (PaddingLeft >> 1)) / 2, 0, 0, 0);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}

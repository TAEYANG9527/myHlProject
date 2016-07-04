package com.itcalf.renhe.adapter;

import android.content.Context;
import android.widget.SectionIndexer;

import com.itcalf.renhe.bean.HlContactRenheMemberItem;

/**
 * @author chan
 * @createtime 2014-10-20
 * @功能说明 实现SectionIndexer接口，继承ContactArrayAdapter，实现快速滑动，并且创建一个按字母排序的分类的列表
 */
public class HlContactRenheMemberAdapterforContact extends HlContactRenheMemberContactArrayAdapter implements SectionIndexer {

	// List<HlContactRenheMemberItem> sections;
	HlContactRenheMemberItem[] sections;

	public HlContactRenheMemberAdapterforContact(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
	}

	@Override
	public void prepareSections(int sectionsNumber) {
		sections = new HlContactRenheMemberItem[sectionsNumber];
	}

	@Override
	public void onSectionAdded(HlContactRenheMemberItem section, int sectionPosition) {
		sections[sectionPosition] = section;
	}

	@Override
	public HlContactRenheMemberItem[] getSections() {
		return sections;
	}

	@Override
	public int getPositionForSection(int section) {
		if (section >= sections.length) {
			section = sections.length - 1;
		}
		return sections[section].listPosition;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (position >= getCount()) {
			position = getCount() - 1;
		}
		return getItem(position).sectionPosition;
	}

	/**
	 * 通过标题获取标题的位置
	 * 
	 * @param tag
	 * @return
	 */
	public int getPositionForTag(String tag) {
		if (null != sections && sections.length > 0) {
			if (tag.equals("★")) {
				//				tag = "新的朋友";
				return 0;
			}
			for (int i = 0; i < sections.length; i++) {
				if (null != sections[i] && null != sections[i].text && sections[i].text.equals(tag))
					return i;
			}
		}
		return -1;
	}

}

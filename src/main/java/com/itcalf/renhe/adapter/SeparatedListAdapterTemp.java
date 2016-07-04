package com.itcalf.renhe.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Feature:带标题的ListView Desc:扩展BaseAdapter，实现了带标题的多个ListView显示
 * 
 * @author xp
 * 
 */
public class SeparatedListAdapterTemp extends BaseAdapter implements SectionIndexer {

	private final Map<String, SectionHeader> headerMap = new LinkedHashMap<String, SectionHeader>();
	private final ArrayAdapter<SectionHeader> headersAdapter;
	private final ArrayList<SectionHeader> sections = new ArrayList<SectionHeader>();

	public final static int TYPE_SECTION_HEADER = 0;

	private SectionDataSetObserver sectionDataSetObserver;

	SectionHeader header;

	public SeparatedListAdapterTemp(Context context, int textViewResourceId) {
		headersAdapter = new ArrayAdapter<SectionHeader>(context, textViewResourceId, sections);
	}

	public void addSection(String tag, Adapter adapter) {
		addSection(tag, tag, adapter);
	}

	public void addSection(String tag, String title, Adapter adapter) {
		header = new SectionHeader(title, adapter);
		this.headersAdapter.add(header);
		this.headerMap.put(tag, header);
		sectionDataSetObserver = new SectionDataSetObserver(header);
		adapter.registerDataSetObserver(sectionDataSetObserver);

	}

	public void setSectionTitle(String tag, String title) {
		headerMap.get(tag).title = title;
	}

	/**
	 * 获取索引的对象
	 */
	public Object getItem(int position) {
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) {
				return section;
			}
			if (position < size) {
				return adapter.getItem(position - 1);
			}

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	/**
	 * 获取全部列表数量
	 */
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			total += adapter.getCount() + 1;
		}
		return total;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			total += adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) {
				return TYPE_SECTION_HEADER;
			}
			if (position < size) {
				return type + adapter.getItemViewType(position - 1);
			}

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) {
				return headersAdapter.getView(sectionnum, convertView, parent);
			}
			if (position < size) {
				return adapter.getView(position - 1, convertView, parent);
			}

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	public long getItemId(int position) {
		for (final SectionHeader section : this.sections) {
			final Adapter adapter = section.adapter;
			final int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) {
				return -1;
			}
			if (position < size) {
				return adapter.getItemId(position - 1);
			}

			// otherwise jump into next section
			position -= size;
		}
		return -1;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		for (final SectionHeader header : sections) {
			header.adapter.registerDataSetObserver(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		for (final SectionHeader header : sections) {
			header.adapter.unregisterDataSetObserver(observer);
		}
	}

	private class SectionHeader {

		public final Adapter adapter;
		public String title;
		public int sectionCount;

		public SectionHeader(String title, Adapter adapter) {
			this.title = title;
			this.adapter = adapter;
			this.sectionCount = adapter.getCount();
		}

		@Override
		public String toString() {
			return title;
		}
	}

	/**
	 * 通过标题的位置获取List中Item的位置
	 */
	@Override
	public int getPositionForSection(int section) {
		int position = -1; // offset because we want to return the index of the
		// section header.
		for (int i = 0; i < section; i++) {
			if (sections.size() > i && null != sections.get(i)) {
				position += sections.get(i).sectionCount + 1;
			}
		}
		return position;
	}

	/**
	 * 通过List中Item的位置获取标题的位置
	 */
	@Override
	public int getSectionForPosition(int position) {
		int i = 0;
		for (final SectionHeader section : this.sections) {
			final int size = section.sectionCount;

			// check if position inside this section
			if (position == 0) {
				return 0;
			}
			if (position < size) {
				return i;
			}
			// otherwise jump into next section
			position -= size;
			i++;
		}
		return 0;
	}

	/**
	 * 获取全部的标题
	 */
	@Override
	public String[] getSections() {
		final String[] letters = new String[sections.size()];
		for (int i = 0; i < sections.size(); i++) {
			letters[i] = sections.get(i).title.substring(0, 1);
		}
		return letters;
	}

	/**
	 * 通过标题获取标题的位置
	 * 
	 * @param tag
	 * @return
	 */
	public int getPositionForTag(String tag) {
		String[] sections = getSections();
		if (null != sections && sections.length > 0) {
			for (int i = 0; i < sections.length; i++) {
				if (sections[i].equals(tag))
					return i;
			}
		}
		return -1;
	}

	private class SectionDataSetObserver extends DataSetObserver {

		private final SectionHeader header;

		public SectionDataSetObserver(SectionHeader header) {
			this.header = header;
		}

		@Override
		public void onChanged() {
			header.sectionCount = header.adapter.getCount();
		}

	}

	public void clear() {
		headerMap.clear();
		sections.clear();
		notifyDataSetInvalidated();
	}

}
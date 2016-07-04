package com.itcalf.renhe.view;

import android.widget.ListView;

/**
 * Title: ListviewWithScroolview.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-5-16 下午2:38:13 <br>
 * @author wangning
 */

public class ListviewWithScroolview extends ListView {

	public ListviewWithScroolview(android.content.Context context, android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	/** 
	 * Integer.MAX_VALUE >> 2,如果不设置，系统默认设置是显示两条 
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}

}

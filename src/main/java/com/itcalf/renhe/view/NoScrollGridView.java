package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Title: NoScrollGridView.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-26 下午1:18:29 <br>
 * @author wangning
 */
public class NoScrollGridView extends GridView {

	public NoScrollGridView(Context context) {
		super(context);

	}

	public NoScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}

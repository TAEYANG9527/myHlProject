package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;

import com.itcalf.renhe.Constants;

public class TextView extends android.widget.TextView {

	public TextView(Context context) {
		super(context);
		init(context);
	}

	public TextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context paramContext) {
		//		Constants.TypefaceParams(paramContext);
		if ((isInEditMode()) || (Constants.APP_TYPEFACE == null))
			return;
		setTypeface(Constants.APP_TYPEFACE);
	}
}

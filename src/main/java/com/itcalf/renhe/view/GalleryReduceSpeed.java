package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Gallery;

public class GalleryReduceSpeed extends Gallery {
	public GalleryReduceSpeed(Context context) {
		super(context);
	}

	public GalleryReduceSpeed(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GalleryReduceSpeed(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		velocityX = velocityX > 0 ? 400 : -400;
		return super.onFling(e1, e2, velocityX, velocityY);
	}

}

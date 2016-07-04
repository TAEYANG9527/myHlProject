package com.itcalf.renhe.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;

/**
 * Title: FadeUitl.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-24 上午10:54:32 <br>
 * @author wangning
 */
public class FadeUitl {
	private Context context;
	private String content;
	private RelativeLayout fadeLayout;

	public FadeUitl(Context context, String content) {
		this.context = context;
		this.content = content;
		this.fadeLayout = createFade();
	}

	public void addFade(ViewGroup group) {
		if (group != null) {
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(-1, -1);
			group.addView(fadeLayout, params);
		}
	}

	public void removeFade(ViewGroup group) {
		if (group != null) {
			if (fadeLayout != null && fadeLayout.isShown()) {
				group.removeView(fadeLayout);
			}
		}
	}

	public boolean isFadeShow(ViewGroup group) {
		if (group != null) {
			if (fadeLayout != null && fadeLayout.isShown()) {
				return true;
			}
		}
		return false;
	}

	public RelativeLayout createFade() {
		RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(-2, -2);
		rParams.addRule(13, -1);

		RelativeLayout rLayout = new RelativeLayout(context);
		rLayout.setBackgroundColor(0);

		LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(-2, -2);
		lParams.rightMargin = 10;

		LinearLayout lLayout = new LinearLayout(context);
		lLayout.setOrientation(0);
		lLayout.setGravity(16);

		ProgressBar progress = new ProgressBar(context);
		lLayout.addView(progress, lParams);

		TextView mark_tv = new com.itcalf.renhe.view.TextView(context);
		mark_tv.setText(content);
		mark_tv.setTextColor(context.getResources().getColor(R.color.new_room_item_time_textcolor));
		mark_tv.setTextSize(16.0F);
		lLayout.addView(mark_tv);

		rLayout.addView(lLayout, rParams);

		rLayout.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
		return rLayout;
	}
}

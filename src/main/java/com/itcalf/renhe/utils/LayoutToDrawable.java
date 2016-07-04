package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Button;

import com.itcalf.renhe.R;

/**
 * Title: LayoutToDrawable.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-29 上午11:03:15 <br>
 * @author wangning
 */
public class LayoutToDrawable {
	private Context ct;

	public LayoutToDrawable(Context ct) {
		this.ct = ct;
	}

	public Drawable layoutToDrawable(int num) {

		LayoutInflater inflator = ((Activity) ct).getLayoutInflater();
		View viewHelp = inflator.inflate(R.layout.custon_actionbar_logo, null);

		Button titleBt = (Button) viewHelp.findViewById(R.id.title_button);
		titleBt.setText(num + "");
		//		int size = (int) textView.getText().length();
		Bitmap snapshot = convertViewToBitmap(viewHelp, 0);
		Drawable drawable = (Drawable) new BitmapDrawable(snapshot);

		return drawable;

	}

	public static Bitmap convertViewToBitmap(View view, int size) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		//		int width = size * 40;
		view.layout(0, 0, view.getMeasuredHeight(), view.getMeasuredHeight()); //根据字符串的长度显示view的宽度
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}
}

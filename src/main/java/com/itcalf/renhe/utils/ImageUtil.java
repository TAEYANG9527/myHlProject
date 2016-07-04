package com.itcalf.renhe.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtil {

	public static Bitmap byte2Bitmap(byte[] bytes) {
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}

}

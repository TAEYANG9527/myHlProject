package com.itcalf.renhe.utils;

import android.app.Activity;
import android.graphics.Matrix;

public class MatrixUtil {
	private static Matrix mMatrix;

	public static Matrix getPostMatrix(Activity activity) {
		if (mMatrix == null) {
			mMatrix = new Matrix();
			switch (activity.getWindowManager().getDefaultDisplay().getWidth()) {
			case 320:
				mMatrix.postScale(0.6f, 0.6f);
				break;
			case 240:
				mMatrix.postScale(0.4f, 0.4f);
				break;
			default:
				break;
			}
		}

		return mMatrix;
	}
}

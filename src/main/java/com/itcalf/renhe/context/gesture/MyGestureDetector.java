package com.itcalf.renhe.context.gesture;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/**
 * 手势观察者
 * 
 * @author xp
 * 
 */
public class MyGestureDetector extends SimpleOnGestureListener {

	private GestureDetectorCallBack mGestureDetector;

	private int REL_SWIPE_MIN_DISTANCE;
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;

	public MyGestureDetector(Context mContext, GestureDetectorCallBack mGestureDetector) {
		super();
		this.mGestureDetector = mGestureDetector;
		initData(mContext);
	}

	private void initData(Context mContext) {
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int) (120.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_MAX_OFF_PATH = (int) (250.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY = (int) (200.0f * dm.densityDpi / 160.0f + 0.5);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// int pos = mListView.pointToPosition((int)e.getX(),
		// (int)e.getY());
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
			mGestureDetector.onRTLFling();
			// onRTLFling();
		}
		if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {
			mGestureDetector.onLTRFling();
			// onLTRFling();
		}
		if (e1.getY() - e2.getY() > REL_SWIPE_MIN_DISTANCE && Math.abs(velocityY) > REL_SWIPE_MAX_OFF_PATH) {
			mGestureDetector.onUPFling();
		}
		if (e2.getY() - e1.getY() > REL_SWIPE_MAX_OFF_PATH && Math.abs(velocityY) > REL_SWIPE_THRESHOLD_VELOCITY) {
			mGestureDetector.onDOWNFling();
		}
		return false;
	}

	// private void onLTRFling() {
	// Toast.makeText(mContext, "Left-to-right fling",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// private void onRTLFling() {
	// Toast.makeText(mContext, "Right-to-left fling",
	// Toast.LENGTH_SHORT).show();
	// }

}

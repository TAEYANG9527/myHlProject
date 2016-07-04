package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class CircleMemberGridView extends GridView {

	public CircleMemberGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	//
	//	private OnTouchInvalidPositionListener mTouchInvalidPosListener;
	//
	//	public interface OnTouchInvalidPositionListener
	//	{
	//		boolean onTouchInvalidPosition(int motionEvent);
	//	}
	//
	//	/**
	//	 * 点击空白区域时的响应和处理接口
	//	 */
	//	public void setOnTouchInvalidPositionListener(OnTouchInvalidPositionListener listener)
	//	{
	//		mTouchInvalidPosListener = listener;
	//	}
	//
	//	@Override
	//	public boolean onTouchEvent(MotionEvent event)
	//	{
	//
	//		if (mTouchInvalidPosListener == null)
	//		{
	//			return super.onTouchEvent(event);
	//		}
	//
	//		if (!isEnabled())
	//		{
	//			return isClickable() || isLongClickable();
	//		}
	//
	//		final int motionPosition = pointToPosition((int) event.getX(), (int) event.getY());
	//
	//		if (motionPosition == INVALID_POSITION)
	//		{
	//			super.onTouchEvent(event);
	//			return mTouchInvalidPosListener.onTouchInvalidPosition(event.getActionMasked());
	//		}
	//
	//		return super.onTouchEvent(event);
	//	}
}

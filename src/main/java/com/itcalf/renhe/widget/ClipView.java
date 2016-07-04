package com.itcalf.renhe.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * �ü��߿�
 *
 * @author king
 * @time 2014-6-18 ����3:53:00
 */
public class ClipView extends View {

	/**
	 * �߿�����ұ߽���룬���ڵ���߿򳤶�
	 */

	//	public static final int BORDERDISTANCE_720 = 60;
	public static final int BORDERDISTANCE = 50;
	//	public static final int BORDERDISTANCE_480 = 37;
	//	public static final int BORDERDISTANCE_1080 = 240;

	private int mFocusWidth;//���������screenWith - BORDERDISTANCE*2����ó�
	private int mFocusHeight;//�����߶���mFocusWith��2/3
	private Paint mPaint;
	private Context mContext;

	public static final int WIDTH_GRAVITY = 3;
	public static final int HEIGHT_GRAVITY = 3;
	public static final int IMAGE_WIDTH = 500;//截图的分辨率500x500

	public ClipView(Context context) {
		this(context, null);
		this.mContext = context;
	}

	public ClipView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		this.mContext = context;
	}

	public ClipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
		this.mContext = context;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();
		int borderlength = 0;
		// �߿򳤶ȣ�����Ļ���ұ�Ե50px
		//		int screenWidth = getScreenWidth(mContext);
		//		if(screenWidth <= 480)
		//		{
		//			borderlength = width - BORDERDISTANCE_480 *2;
		//		}
		//		else if(screenWidth>480 && screenWidth<= 720)
		//		{
		//			borderlength = width - BORDERDISTANCE_720 *2;
		//		}
		//		else if(screenWidth>720 && screenWidth<=1080)
		//		{
		//			borderlength = width - BORDERDISTANCE_1080 *2;
		//		}
		//		else
		//		{
		//			borderlength = width - BORDERDISTANCE_DEFAULT *2;
		//		}
		borderlength = width - BORDERDISTANCE * 2;
		mFocusWidth = borderlength;
		Float hf = (float) mFocusWidth * HEIGHT_GRAVITY / WIDTH_GRAVITY;
		mFocusHeight = hf.intValue();
		mPaint.setColor(0xaa000000);

		// ���»���͸����ɫ����
		// top
		canvas.drawRect(0, 0, width, (height - mFocusHeight) / 2, mPaint);
		// bottom
		canvas.drawRect(0, (height + mFocusHeight) / 2, width, height, mPaint);
		// left
		canvas.drawRect(0, (height - mFocusHeight) / 2, BORDERDISTANCE, (height + mFocusHeight) / 2, mPaint);
		// right
		canvas.drawRect(borderlength + BORDERDISTANCE, (height - mFocusHeight) / 2, width, (height + mFocusHeight) / 2, mPaint);

		// ���»��Ʊ߿���
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2.0f);
		// top
		canvas.drawLine(BORDERDISTANCE, (height - mFocusHeight) / 2, width - BORDERDISTANCE, (height - mFocusHeight) / 2, mPaint);
		// bottom
		canvas.drawLine(BORDERDISTANCE, (height + mFocusHeight) / 2, width - BORDERDISTANCE, (height + mFocusHeight) / 2, mPaint);
		// left
		canvas.drawLine(BORDERDISTANCE, (height - mFocusHeight) / 2, BORDERDISTANCE, (height + mFocusHeight) / 2, mPaint);
		// right
		canvas.drawLine(width - BORDERDISTANCE, (height - mFocusHeight) / 2, width - BORDERDISTANCE, (height + mFocusHeight) / 2,
				mPaint);
	}

	public int getFocusWidth() {
		return mFocusWidth;
	}

	public int getFocusHeight() {
		return mFocusHeight;
	}

	public static int getScreenWidth(Context context) {
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}
}

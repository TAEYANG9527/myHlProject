package com.itcalf.renhe.zxing.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.zxing.camera.CameraManager;
import com.itcalf.renhe.zxing.ui.ActivityScan;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 */
public final class ViewfinderView extends View {

	/**
	 * 刷新界面的时间
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	/**
	 * 四个绿色边角对应的长度
	 */
	private int ScreenRate;

	/**
	 * 四个绿色边角对应的宽度
	 */
	private static final int CORNER_WIDTH = 5;
	/**
	 * 扫描框中的中间线的宽度
	 */
	private static final int MIDDLE_LINE_WIDTH = 4;

	/**
	 * 扫描框中的中间线的与扫描框左右的间隙
	 */
	private static final int MIDDLE_LINE_PADDING = 5;

	/**
	 * 中间那条线每次刷新移动的距离
	 */
	private static final int SPEEN_DISTANCE = 4;

	/**
	 * 手机的屏幕密度
	 */
	private static float density;
	/**
	 * 字体大小
	 */
	private static final int TEXT_SIZE = 14;
	/**
	 * 字体距离扫描框下面的距离
	 */
	private static final int TEXT_PADDING_TOP = 64;

	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;

	/**
	 * 中间滑动线的最底端位置
	 */
	private int slideBottom;

	/**
	 * 用户 二维码和名片之间的切换
	 * */
	private boolean isFlag = false;

	/**
	 * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
	 */
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;

	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;

	boolean isFirst;

	//我的二维码点击事件相关
	private Rect mycodeRect = new Rect(0, 0, 0, 0); //文字所在的矩形范围
	private boolean isClick = false;//判断是否触发点击事件

	public ViewfinderView(Context context) {
		super(context);
		density = context.getResources().getDisplayMetrics().density;
		ScreenRate = (int) (15 * density);

		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);

		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		ScreenRate = (int) (15 * density);

		paint = new Paint();
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);

		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@Override
	public void onDraw(Canvas canvas) {

		Rect frame = CameraManager.get().getFramingRect(isFlag);

		if (frame == null) {
			return;
		}

		//获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		//		if (!isFlag) {
		//画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		//扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
		//		} else {
		//			paint.setColor(0x00000000);
		//			canvas.drawRect(0, 0, width, height, paint);
		//		}

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			//画扫描框边上的角，总共8个部分
			paint.setColor(Color.WHITE);
			canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate, frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + ScreenRate, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + ScreenRate, paint);
			canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + ScreenRate, frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left + CORNER_WIDTH, frame.bottom, paint);
			canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, paint);
			canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate, frame.right, frame.bottom, paint);

			if (!isFlag) {

				//初始化中间线滑动的最上边和最下边
				if (!isFirst) {
					isFirst = true;
					slideTop = frame.top;
					slideBottom = frame.bottom - MIDDLE_LINE_WIDTH;
				}

				//绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
				slideTop += SPEEN_DISTANCE;
				if (slideTop >= slideBottom) {
					slideTop = frame.top;
				}

				paint.setColor(getResources().getColor(R.color.viewfinder_line));
				canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop, frame.right - MIDDLE_LINE_PADDING,
						slideTop + MIDDLE_LINE_WIDTH, paint);
				//				canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.qrcode_scan_line), frame.left + MIDDLE_LINE_PADDING,
				//						slideTop - MIDDLE_LINE_WIDTH / 2, paint);

				paint.setAntiAlias(true);
				paint.setColor(Color.WHITE);
				paint.setTextSize(TEXT_SIZE * density);
				paint.setTypeface(Constants.APP_TYPEFACE);
				//			canvas.drawText(getResources().getString(R.string.scan_text), frame.left + 20,
				//					(float) (frame.bottom + (float) TEXT_PADDING_TOP * density), paint);
				float size1 = paint.measureText(getResources().getString(R.string.scan_text));
				canvas.drawText(getResources().getString(R.string.scan_text), width / 2 - size1 / 2,
						(float) (frame.bottom + (float) TEXT_PADDING_TOP * density), paint);

				paint.setColor(Color.WHITE);
				paint.setTextSize((TEXT_SIZE - 1) * density);
				paint.setTypeface(Constants.APP_TYPEFACE);
				String mycodeStr = getResources().getString(R.string.scan_myqrcode);
				float size2 = paint.measureText(mycodeStr);
				int x = (int) ((width - size2) / 2);
				int y = (int) (frame.bottom + (float) (50 * density) + (float) TEXT_PADDING_TOP * density);
				canvas.drawText(mycodeStr, x, y, paint);
				if (mycodeRect.width() == 0) {
					paint.getTextBounds(mycodeStr, 0, mycodeStr.length(), mycodeRect);
					int disclick = (int) (5 * density);//为了扩大点击范围
					mycodeRect.set(x - disclick, y - mycodeRect.height() - disclick, x + mycodeRect.width() + disclick,
							y + disclick);
				}

				Collection<ResultPoint> currentPossible = possibleResultPoints;
				Collection<ResultPoint> currentLast = lastPossibleResultPoints;
				if (currentPossible.isEmpty()) {
					lastPossibleResultPoints = null;
				} else {
					possibleResultPoints = new HashSet<ResultPoint>(5);
					lastPossibleResultPoints = currentPossible;
					paint.setAlpha(OPAQUE);
					paint.setColor(resultPointColor);
					for (ResultPoint point : currentPossible) {
						canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
					}
				}
				if (currentLast != null) {
					paint.setAlpha(OPAQUE / 2);
					paint.setColor(resultPointColor);
					for (ResultPoint point : currentLast) {
						canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
					}
				}
			}

			//只刷新扫描框的内容，其他地方不刷新
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

	public boolean isFlag() {
		return isFlag;
	}

	public void setFlag(boolean isFlag) {
		this.isFlag = isFlag;
		if (!isFlag)
			postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isFlag) {
			return false;
		}
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mycodeRect.contains(x, y)) {
				isClick = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mycodeRect.contains(x, y)) {
				isClick = false;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isClick) {
				isClick = false;
				ActivityScan.showMyQrcode(getContext());
			}
			break;
		}
		return true;
	}

}

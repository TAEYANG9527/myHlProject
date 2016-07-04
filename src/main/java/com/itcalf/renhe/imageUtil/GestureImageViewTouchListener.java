/*
 * Copyright (c) 2012 Jason Polites
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itcalf.renhe.imageUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GestureImageViewTouchListener implements OnTouchListener {

	private GestureImageView image;
	private OnClickListener onClickListener;

	private final PointF current = new PointF();
	private final PointF last = new PointF();
	private final PointF next = new PointF();
	private final PointF midpoint = new PointF();

	private final VectorF scaleVector = new VectorF();
	private final VectorF pinchVector = new VectorF();

	private boolean touched = false;
	private boolean inZoom = false;

	private float initialDistance;
	private float lastScale = 1.0f;
	private float currentScale = 1.0f;

	private float boundaryLeft = 0;
	private float boundaryTop = 0;
	private float boundaryRight = 0;
	private float boundaryBottom = 0;

	private float maxScale = 5.0f;
	private float minScale = 0.25f;

	private float zoomMinSacle = 0.4f;// 缩小时，最低缩小到的大小

	private float fitScaleHorizontal = 1.0f;
	private float fitScaleVertical = 1.0f;

	private int canvasWidth = 0;
	private int canvasHeight = 0;

	private float centerX = 0;
	private float centerY = 0;

	private float startingScale = 0;

	private boolean canDragX = false;
	private boolean canDragY = false;

	private boolean multiTouch = false;

	private int displayWidth;
	private int displayHeight;

	private int imageWidth;
	private int imageHeight;

	private FlingListener flingListener;
	private FlingAnimation flingAnimation;
	private ZoomAnimation zoomAnimation;
	private MoveAnimation moveAnimation;
	private GestureDetector tapDetector;
	private GestureDetector flingDetector;
	private GestureImageViewListener imageListener;

	private Context mContext;
	private long firstClick;
	private long lastClick;
	// 计算点击的次数
	private int count;

	public GestureImageViewTouchListener(Context context, final GestureImageView image, int displayWidth, int displayHeight) {
		super();
		mContext = context;
		this.image = image;

		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;

		this.centerX = (float) displayWidth / 2.0f;
		this.centerY = (float) displayHeight / 2.0f;

		this.imageWidth = image.getImageWidth();
		this.imageHeight = image.getImageHeight();

		startingScale = image.getScale();

		currentScale = startingScale;
		lastScale = startingScale;

		boundaryRight = displayWidth;
		boundaryBottom = displayHeight;
		boundaryLeft = 0;
		boundaryTop = 0;

		next.x = image.getImageX();
		next.y = image.getImageY();

		flingListener = new FlingListener();
		flingAnimation = new FlingAnimation();
		zoomAnimation = new ZoomAnimation();
		moveAnimation = new MoveAnimation();

		flingAnimation.setListener(new FlingAnimationListener() {
			@Override
			public void onMove(float x, float y) {
				handleDrag(current.x + x, current.y + y);
			}

			@Override
			public void onComplete() {
			}
		});

		zoomAnimation.setZoom(2.0f);
		zoomAnimation.setZoomAnimationListener(new ZoomAnimationListener() {
			@Override
			public void onZoom(float scale, float x, float y) {
				if (scale <= maxScale && scale >= minScale) {
					handleScale(scale, x, y);
				}
			}

			@Override
			public void onComplete() {
				inZoom = false;
				handleUp();
			}
		});

		moveAnimation.setMoveAnimationListener(new MoveAnimationListener() {

			@Override
			public void onMove(float x, float y) {
				image.setPosition(x, y);
				image.redraw();
			}
		});

		tapDetector = new GestureDetector(image.getContext(), new SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				startZoom(e);
				tapDetector.setIsLongpressEnabled(false);
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (!inZoom) {
					((Activity) mContext).finish();
					return false;
				}
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				createDialog();// 保存图片
				super.onLongPress(e);
			}
		});

		flingDetector = new GestureDetector(image.getContext(), flingListener);
		imageListener = image.getGestureImageViewListener();

		calculateBoundaries();
	}

	private AlertDialog mAlertDialog;

	public void createDialog() {
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.custom_delete_dialog, null);
		Builder mDialog = new AlertDialog.Builder(mContext);
		LinearLayout reportLl = (LinearLayout) view.findViewById(R.id.reportLl);
		TextView reportTv = (TextView) view.findViewById(R.id.reportTv);
		LinearLayout shieldLl = (LinearLayout) view.findViewById(R.id.shieldLl);
		mAlertDialog = mDialog.create();
		mAlertDialog.setView(view, 0, 0, 0, 0);
		mAlertDialog.setCanceledOnTouchOutside(true);
		mAlertDialog.show();
		reportTv.setText("保存");
		reportLl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}

				if (image == null) {
					ToastUtil.showToast(mContext, "图片不存在");
				}
				File file = null;
				try {
					// 4.0+貌似不能直接创建文件，得先创建文件夹，不然会有异常
					File f = new File(Constants.CACHE_PATH.PICTUREPATH);
					if (!f.exists()) {
						f.mkdirs();
					} else {
						file = new File(Constants.CACHE_PATH.PICTUREPATH + System.currentTimeMillis() + ".png");
						FileOutputStream out = new FileOutputStream(file);
						BitmapDrawable bmd = (BitmapDrawable) image.getDrawable();
						Bitmap bm = bmd.getBitmap();
						if (bm != null) {
							bm.compress(Bitmap.CompressFormat.PNG, 100, out);
							ToastUtil.showToast(mContext, "图片已保存" + Constants.CACHE_PATH.PICTUREPATH);
						}
						out.flush();
						out.close();
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				if (file != null) {
					//					MediaScannerConnection.scanFile(mContext,
					//							new String[] { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
					//									+ "/" + file.getParentFile().getAbsolutePath() }, null, null);
					Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					Uri uri = Uri.fromFile(file);
					intent.setData(uri);
					mContext.sendBroadcast(intent);
				}
			}
		});

		shieldLl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
			}
		});

	}

	private void startFling() {
		flingAnimation.setVelocityX(flingListener.getVelocityX());
		flingAnimation.setVelocityY(flingListener.getVelocityY());
		image.animationStart(flingAnimation);
	}

	private void startZoom(MotionEvent e) {
		inZoom = true;
		zoomAnimation.reset();
		float zoomTo;
		if (image.isLandscape()) {
			if (image.getDeviceOrientation() == Configuration.ORIENTATION_PORTRAIT) {
				int scaledHeight = image.getScaledHeight();

				if (scaledHeight < canvasHeight) {
					zoomTo = fitScaleVertical / currentScale;
					zoomAnimation.setTouchX(e.getX());
					zoomAnimation.setTouchY(image.getCenterY());
				} else {
					zoomTo = fitScaleHorizontal / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(image.getCenterY());
				}
			} else {
				int scaledWidth = image.getScaledWidth();

				if (scaledWidth == canvasWidth) {
					zoomTo = currentScale * 4.0f;
					zoomAnimation.setTouchX(e.getX());
					zoomAnimation.setTouchY(e.getY());
				} else if (scaledWidth < canvasWidth) {
					zoomTo = fitScaleHorizontal / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(e.getY());
				} else {
					zoomTo = fitScaleHorizontal / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(image.getCenterY());
				}
			}
		} else {
			if (image.getDeviceOrientation() == Configuration.ORIENTATION_PORTRAIT) {

				int scaledHeight = image.getScaledHeight();

				if (scaledHeight == canvasHeight) {
					zoomTo = currentScale * 4.0f;
					zoomAnimation.setTouchX(e.getX());
					zoomAnimation.setTouchY(e.getY());
				} else if (scaledHeight < canvasHeight) {
					zoomTo = fitScaleVertical / currentScale;
					zoomAnimation.setTouchX(e.getX());
					zoomAnimation.setTouchY(image.getCenterY());
				} else {
					zoomTo = fitScaleVertical / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(image.getCenterY());
				}
			} else {
				int scaledWidth = image.getScaledWidth();

				if (scaledWidth < canvasWidth) {
					zoomTo = fitScaleHorizontal / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(e.getY());
				} else {
					zoomTo = fitScaleVertical / currentScale;
					zoomAnimation.setTouchX(image.getCenterX());
					zoomAnimation.setTouchY(image.getCenterY());
				}
			}
		}

		zoomAnimation.setZoom(zoomTo);
		image.animationStart(zoomAnimation);
	}

	private void stopAnimations() {
		image.animationStop();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		tapDetector.setIsLongpressEnabled(true);

		if (!inZoom) {
			if (!tapDetector.onTouchEvent(event)) {
				if (event.getPointerCount() == 1 && flingDetector.onTouchEvent(event)) {
					startFling();
				}

				if (event.getAction() == MotionEvent.ACTION_UP) {

					handleUp();
				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					stopAnimations();

					last.x = event.getX();
					last.y = event.getY();

					if (imageListener != null) {
						imageListener.onTouch(last.x, last.y);
					}

					touched = true;

					// 如果第二次点击 距离第一次点击时间过长 那么将第二次点击看为第一次点击
					// if (firstClick != 0 && System.currentTimeMillis() -
					// firstClick > 300) {
					// count = 0;
					// }
					// count++;
					// if (count == 1) {
					// firstClick = System.currentTimeMillis();
					// if(currentScale == fitScaleVertical){
					// ((Activity)mContext).finish();
					// }
					// } else if (count == 2) {
					// lastClick = System.currentTimeMillis();
					// // 两次点击小于300ms 也就是连续点击
					// if (lastClick - firstClick > 300) {// 判断是否是执行了双击事件
					// if(currentScale == fitScaleVertical){
					// ((Activity)mContext).finish();
					// }
					// }
					// }
					// if(event.getAction() != MotionEvent.ACTION_POINTER_DOWN&&
					// event.getAction() != MotionEvent.ACTION_MOVE){
					// if(currentScale == fitScaleVertical){
					// ((Activity)mContext).finish();
					// }
					// }
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (event.getPointerCount() > 1) {
						multiTouch = true;
						if (initialDistance > 0) {

							pinchVector.set(event);
							pinchVector.calculateLength();

							float distance = pinchVector.length;

							if (initialDistance != distance) {

								float newScale = (distance / initialDistance) * lastScale;

								if (newScale <= maxScale) {
									scaleVector.length *= newScale;

									scaleVector.calculateEndPoint();

									scaleVector.length /= newScale;

									float newX = scaleVector.end.x;
									float newY = scaleVector.end.y;

									handleScale(newScale, newX, newY);
								}
							}
						} else {
							initialDistance = MathUtils.distance(event);

							MathUtils.midpoint(event, midpoint);

							scaleVector.setStart(midpoint);
							scaleVector.setEnd(next);

							scaleVector.calculateLength();
							scaleVector.calculateAngle();

							scaleVector.length /= lastScale;
						}
					} else {
						if (!touched) {
							touched = true;
							last.x = event.getX();
							last.y = event.getY();
							next.x = image.getImageX();
							next.y = image.getImageY();
						} else if (!multiTouch) {
							if (handleDrag(event.getX(), event.getY())) {
								image.redraw();
							}
						}
					}
				}
			}
		}

		return true;
	}

	protected void handleUp() {

		multiTouch = false;

		initialDistance = 0;
		lastScale = currentScale;

		if (!canDragX) {
			next.x = centerX;
		}

		if (!canDragY) {
			next.y = centerY;
		}

		boundCoordinates();

		if (!canDragX && !canDragY) {
			// 处理是否支持无限缩小，注释掉即支持缩小
			if (currentScale <= zoomMinSacle) {
				if (image.isLandscape()) {
					currentScale = zoomMinSacle;
					lastScale = zoomMinSacle;
				} else {
					currentScale = zoomMinSacle;
					lastScale = zoomMinSacle;
				}
			}
			// if(image.isLandscape()) {
			// currentScale = fitScaleHorizontal;
			// lastScale = fitScaleHorizontal;
			// }
			// else {
			// currentScale = fitScaleVertical;
			// lastScale = fitScaleVertical;
			// }
		}

		image.setScale(currentScale);
		image.setPosition(next.x, next.y);

		if (imageListener != null) {
			imageListener.onScale(currentScale);
			imageListener.onPosition(next.x, next.y);
		}

		image.redraw();
	}

	protected void handleScale(float scale, float x, float y) {

		currentScale = scale;

		if (currentScale > maxScale) {
			currentScale = maxScale;
		} else if (currentScale < minScale) {
			// currentScale = minScale;
		} else {
			next.x = x;
			next.y = y;
		}

		calculateBoundaries();

		image.setScale(currentScale);
		image.setPosition(next.x, next.y);

		if (imageListener != null) {
			imageListener.onScale(currentScale);
			imageListener.onPosition(next.x, next.y);
		}

		image.redraw();
	}

	protected boolean handleDrag(float x, float y) {
		current.x = x;
		current.y = y;

		float diffX = (current.x - last.x);
		float diffY = (current.y - last.y);

		if (diffX != 0 || diffY != 0) {

			if (canDragX)
				next.x += diffX;
			if (canDragY)
				next.y += diffY;

			boundCoordinates();

			last.x = current.x;
			last.y = current.y;

			if (canDragX || canDragY) {
				image.setPosition(next.x, next.y);

				if (imageListener != null) {
					imageListener.onPosition(next.x, next.y);
				}

				return true;
			}
		}

		return false;
	}

	public void reset() {
		currentScale = startingScale;
		next.x = centerX;
		next.y = centerY;
		calculateBoundaries();
		image.setScale(currentScale);
		image.setPosition(next.x, next.y);
		image.redraw();
	}

	public float getMaxScale() {
		return maxScale;
	}

	public void setMaxScale(float maxScale) {
		this.maxScale = maxScale;
	}

	public float getMinScale() {
		return minScale;
	}

	public void setMinScale(float minScale) {
		this.minScale = minScale;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	protected void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	protected void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}

	protected void setFitScaleHorizontal(float fitScale) {
		this.fitScaleHorizontal = fitScale;
	}

	protected void setFitScaleVertical(float fitScaleVertical) {
		this.fitScaleVertical = fitScaleVertical;
	}

	protected void boundCoordinates() {
		if (next.x < boundaryLeft) {
			next.x = boundaryLeft;
		} else if (next.x > boundaryRight) {
			next.x = boundaryRight;
		}

		if (next.y < boundaryTop) {
			next.y = boundaryTop;
		} else if (next.y > boundaryBottom) {
			next.y = boundaryBottom;
		}
	}

	protected void calculateBoundaries() {

		int effectiveWidth = Math.round((float) imageWidth * currentScale);
		int effectiveHeight = Math.round((float) imageHeight * currentScale);

		canDragX = effectiveWidth > displayWidth;
		canDragY = effectiveHeight > displayHeight;

		if (canDragX) {
			float diff = (float) (effectiveWidth - displayWidth) / 2.0f;
			boundaryLeft = centerX - diff;
			boundaryRight = centerX + diff;
		}

		if (canDragY) {
			float diff = (float) (effectiveHeight - displayHeight) / 2.0f;
			boundaryTop = centerY - diff;
			boundaryBottom = centerY + diff;
		}
	}
}

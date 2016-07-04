package com.itcalf.renhe.utils;

import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class AsyncImageLoader {

    private static AsyncImageLoader mAsyncImage;
    public static Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
    private final int TIMEOUT = 5000;
    private boolean CACHE = true;
    private static Context ct;
    private static String email;
    private boolean isCorner;
    private boolean isAvatar;
    private boolean isHostPic;
    private static ImageLoader imageLoader;

    public static AsyncImageLoader getInstance() {
        if (null == mAsyncImage) {
            mAsyncImage = new AsyncImageLoader();
        }
        imageLoader = ImageLoader.getInstance();
        return mAsyncImage;
    }

    public void clearCache() {
        imageCache.clear();
    }

    public AsyncImageLoader populateData(Context ct, String email, boolean isCorner, boolean isAvatar, boolean isHostPic) {
        this.ct = ct;
        this.email = email;
        this.isCorner = isCorner;
        this.isAvatar = isAvatar;
        this.isHostPic = isHostPic;
        return mAsyncImage;
    }

    /**
     * loading Pic
     *
     * @param imageView
     * @param picPath
     */
    public void loadPic(final ImageView imageView, String sid, String picPath, Integer width, Integer height) {
        if (null != picPath && !"".equals(picPath)) {
            try {
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(picPath, imageView, CacheManager.imageOptions);

                //				new AsyncImageLoader().loadDrawable(sid, picPath, new ImageCallback() {
                //					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
                //						if (null != imageDrawable) {
                //							imageView.setImageDrawable(imageDrawable);
                //							imageView.invalidate();
                //						}
                //					}
                //				}, width, height, ct, email, isCorner, isAvatar, false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "loadPic", e);
            }
        }
    }

    /**
     * loading Pic
     *
     * @param imageView
     * @param picPath
     */
    public void loadPic(final ImageView imageView, String sid, String picPath, final Integer width, final Integer height,
                        final Matrix matrix, boolean loadRoomPic) {
        if (null != picPath && !"".equals(picPath)) {
            try {
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(picPath, imageView, CacheManager.imageOptions);
                //				new AsyncImageLoader().loadDrawable(sid, picPath, new ImageCallback() {
                //					public void imageLoaded(Drawable imageDrawable, String imageUrl) {
                //						if (null != imageDrawable) {
                //							BitmapDrawable bd = (BitmapDrawable) imageDrawable;
                //							Bitmap bitmap = bd.getBitmap();
                //							// matrix.postScale(0.6f, 0.6f);
                //							if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0)
                //								bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                //							imageView.setImageBitmap(bitmap);
                //							imageView.invalidate();
                //						}
                //					}
                //				}, width, height, ct, email, isCorner, isAvatar, loadRoomPic);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "loadPic", e);
            }
        }
    }

    public void loadPic2(final ImageView imageView, final ProgressBar progressBar, String picPath) {
        if (null != picPath && !"".equals(picPath)) {
            try {
                progressBar.setVisibility(View.VISIBLE);
                imageLoader.displayImage(picPath, imageView, CacheManager.largeImageOptions,
                        new ImageAnimateFirstDisplayListener(progressBar));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Constants.TAG, "loadPic", e);
            }
        }
    }

    /**
     * 图片加载第一次显示监听器
     *
     * @author Administrator
     */
    public static class ImageAnimateFirstDisplayListener extends SimpleImageLoadingListener {

        private ProgressBar progressBar;

        public ImageAnimateFirstDisplayListener(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            progressBar.setVisibility(View.GONE);
//			if (loadedImage != null) {
//				ImageView imageView = (ImageView) view;
//				imageView.setVisibility(View.VISIBLE);
//				String fileName = imageLoader.getDiscCache().get(imageUri).getPath();
//				if (!TextUtils.isEmpty(fileName)) {//图片缓存不存在，尝试根据picurl是图片本地路径去查找
//					File file = new File(fileName);
//					if (null != file && file.exists()) {
//						Uri uri = Uri.fromFile(file);
//						imageView.setImageURI(uri);
//					}
//				}
//			}
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            super.onLoadingCancelled(imageUri, view);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
        }
    }

    public String getWebPath(String sid, String webPath, boolean isAvatar) {
        if (isAvatar) {
            String id = webPath.substring(webPath.indexOf("/") + 2);
            id = id.replaceAll("/", "_");
            return id;
        } else {
            return webPath.substring(webPath.lastIndexOf("/") + 1);
        }
    }

    public static Bitmap getRoundedCornerBitmap(Context context, Bitmap bitmap, float upperLeft, float upperRight,
                                                float lowerRight, float lowerLeft, int endWidth, int endHeight) {
        float densityMultiplier = context.getResources().getDisplayMetrics().density;

        // scale incoming bitmap to appropriate px size given arguments and
        // display dpi
        bitmap = Bitmap.createScaledBitmap(bitmap, Math.round(endWidth * densityMultiplier),
                Math.round(endHeight * densityMultiplier), true);

        // create empty bitmap for drawing
        Bitmap output = Bitmap.createBitmap(Math.round(endWidth * densityMultiplier), Math.round(endHeight * densityMultiplier),
                Config.ARGB_8888);

        // get canvas for empty bitmap
        Canvas canvas = new Canvas(output);
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // scale the rounded corners appropriately given dpi
        upperLeft *= densityMultiplier;
        upperRight *= densityMultiplier;
        lowerRight *= densityMultiplier;
        lowerLeft *= densityMultiplier;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        // fill the canvas with transparency
        canvas.drawARGB(0, 0, 0, 0);

        // draw the rounded corners around the image rect. clockwise, starting
        // in upper left.
        canvas.drawCircle(upperLeft, upperLeft, upperLeft, paint);
        canvas.drawCircle(width - upperRight, upperRight, upperRight, paint);
        canvas.drawCircle(width - lowerRight, height - lowerRight, lowerRight, paint);
        canvas.drawCircle(lowerLeft, height - lowerLeft, lowerLeft, paint);

        // fill in all the gaps between circles. clockwise, starting at top.
        RectF rectT = new RectF(upperLeft, 0, width - upperRight, height / 2);
        RectF rectR = new RectF(width / 2, upperRight, width, height - lowerRight);
        RectF rectB = new RectF(lowerLeft, height / 2, width - lowerRight, height);
        RectF rectL = new RectF(0, upperLeft, width / 2, height - lowerLeft);

        canvas.drawRect(rectT, paint);
        canvas.drawRect(rectR, paint);
        canvas.drawRect(rectB, paint);
        canvas.drawRect(rectL, paint);

        // set up the rect for the image
        Rect imageRect = new Rect(0, 0, width, height);

        // set up paint object such that it only paints on Color.WHITE
        paint.setXfermode(new AvoidXfermode(Color.WHITE, 255, AvoidXfermode.Mode.TARGET));

        // draw resized bitmap onto imageRect in canvas, using paint as
        // configured above
        canvas.drawBitmap(bitmap, imageRect, imageRect, paint);

        return output;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 10;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap createReflectedImage(Bitmap originalImage) {
        // The gap we want between the reflection and the original image
        final int reflectionGap = 1;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // Create a Bitmap with the flip matrix applied to it.
        // We only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height / 2, width, height / 2, matrix, false);

        // Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw in the original image
        canvas.drawBitmap(originalImage, 0, 0, null);
        // Draw in the gap
        Paint defaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        // Draw in the reflection
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        // Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
        // Set the paint to use this shader (linear gradient)
        paint.setShader(shader);
        // Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);

        return bitmapWithReflection;
    }

    public static String saveFile(byte[] bytes, String filePath, String fileName) {
        File file = new File(filePath, fileName);
        FileOutputStream fos = null;
        try {
            if (!file.isFile()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
        } catch (Exception e) {
            if (null != fos)
                try {
                    fos.close();
                } catch (IOException e1) {
                }
        }
        return file.getAbsolutePath();
    }

    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, String imageUrl);
    }

}

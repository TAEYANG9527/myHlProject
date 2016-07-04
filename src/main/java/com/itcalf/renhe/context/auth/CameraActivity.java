package com.itcalf.renhe.context.auth;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.zxing.card.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback {

    private SurfaceView surv_camera;
    private ImageView iv_flicker, iv_gallery, iv_takephoto;
    private TextView tv_cancel;

    private Camera mCamera;
    private boolean mCurrentOrientation;
    private boolean isOpenFlicker = true;
    private boolean isPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTemplate().doInActivity(this, R.layout.activity_camera);
    }

    @Override
    protected void findView() {
        super.findView();

        iv_flicker = (ImageView) findViewById(R.id.iv_flicker);
        iv_gallery = (ImageView) findViewById(R.id.iv_gallery);
        iv_takephoto = (ImageView) findViewById(R.id.iv_takephoto);
        tv_cancel = (TextView) findViewById(R.id.tv_cancel);

        surv_camera = (SurfaceView) findViewById(R.id.surv_camera);
        surv_camera.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surv_camera.getHolder().addCallback(this);

    }

    @Override
    protected void initData() {
        super.initData();
        startOrientationChangeListener();
    }

    @Override
    protected void initListener() {
        super.initListener();
        //开关闪光
        iv_flicker.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                changeFlicker();
            }
        });

        //图库
        iv_gallery.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        //拍照
        iv_takephoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCamera != null && isPreview) {
                    iv_takephoto.setEnabled(false);
                    mCamera.takePicture(null, null, null, pictureCallback);
                }
            }
        });

        //取消
        tv_cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void changeFlicker() {
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();
            isOpenFlicker = !isOpenFlicker;
            if (isOpenFlicker) {
                parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);//开启
                mCamera.setParameters(parameters);
                iv_flicker.setImageResource(R.drawable.icon_richscan_flicker_open);
            } else {
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);//关闭
                mCamera.setParameters(parameters);
                iv_flicker.setImageResource(R.drawable.icon_richscan_flicker_close);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            initCamera();
        } catch (Exception e) {

            cameraInitFail();

            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 释放相机
        if (mCamera != null) {
            if (isPreview)
                mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null)
            return;
        Camera.Parameters parameters = mCamera.getParameters();
        //不进行参数设置，不同机型兼容不同
//		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
//		// You need to choose the most appropriate previewSize for your app
//		Camera.Size previewSize = previewSizes.get(0);
//		parameters.setPreviewSize(previewSize.width, previewSize.height);
//      parameters.setPictureSize(previewSize.width, previewSize.height);// 设置捕捉图片尺寸

//        Size largestSize = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width * height);
//        parameters.setPreviewSize(largestSize.width, largestSize.height);// 设置预览图片尺寸
//        Size largestSize2 = getBestSupportedSize(parameters.getSupportedPictureSizes(), width * height);
//        parameters.setPictureSize(largestSize2.width, largestSize2.height);// 设置捕捉图片尺寸
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setJpegQuality(90);

        List<String> modes = parameters.getSupportedFocusModes();
        if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)
                && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        if (isOpenFlicker) {
            parameters.setFlashMode(Parameters.FLASH_MODE_AUTO);//开启
        } else {
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);//关闭
        }

        mCamera.setParameters(parameters);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int degrees = getDisplayRotation(this);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
        //		try {
        //			mCamera.startPreview();
        //			isPreview = true;
        //		} catch (Exception e) {
        //			if (mCamera != null) {
        //				mCamera.release();
        //				mCamera = null;
        //			}
        //		}

    }

    private void initCamera() throws Exception {
        // 开启相机
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);//0代表后置摄像头
        } else
            mCamera = Camera.open();

        if (mCamera != null) {
            mCamera.startPreview();
            isPreview = true;
        }
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            isPreview = false;
        }
    }

    //	/**
    //	 * 取支持的最大的SIZE
    //	 * */
    //	private Size getBestSupportedSize(List<Size> sizes) {
    //        Size largestSize = sizes.get(0);
    //        int largestArea = sizes.get(0).height * sizes.get(0).width;
    //        for (Size s : sizes) {
    //            int area = s.width * s.height;
    //            if (area > largestArea) {
    //                largestArea = area;
    //                largestSize = s;
    //            }
    //        }
    //        return largestSize;
    //    }

    /**
     * 取支持的Size中跟目标分辨率最接近的
     */
    private Size getBestSupportedSize(List<Size> sizes, int targetArea) {
        Size largestSize = sizes.get(0);
        int offsetArea = Math.abs(sizes.get(0).width * sizes.get(0).height - targetArea);
        for (Size s : sizes) {
            int disArea = Math.abs(s.width * s.height - targetArea);
            if (disArea <= offsetArea) {
                offsetArea = disArea;
                largestSize = s;
            }
        }
        return largestSize;
    }

    private final void startOrientationChangeListener() {
        new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {
                if (((rotation >= 0) && (rotation <= 45)) || (rotation >= 315) || ((rotation >= 135) && (rotation <= 225))) {// portrait
                    mCurrentOrientation = true;
                } else if (((rotation > 45) && (rotation < 135)) || ((rotation > 225) && (rotation < 315))) {// landscape
                    mCurrentOrientation = false;
                }
            }
        }.enable();
    }

    /* 图像数据处理完成后的回调函数 */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File file = new File(FileUtil.getImageTempDirectory(CameraActivity.this), "tempVCard.jpeg");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                if (mCurrentOrientation) {
                    // 竖屏时，旋转图片再保存
                    Bitmap oldBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    matrix.setRotate(90);
                    oldBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);
                    oldBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    oldBitmap.recycle();
                } else {
                    out.write(data);
                }

                Intent i = new Intent();
                i.putExtra("filepath", file.getAbsolutePath());
                setResult(RESULT_OK, i);
                finish();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private void cameraInitFail() {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder(R.string.material_dialog_title, R.string.camerafail_hint, R.string.hint_close).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                finish();
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            }
        }).cancelable(false);
        materialDialogsUtil.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null && !TextUtils.isEmpty(uri.toString())) {
                    Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null,
                            null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        if (!TextUtils.isEmpty(photo_path)) {
                            iv_takephoto.setEnabled(false);
                            Intent i = new Intent();
                            i.putExtra("filepath", photo_path);
                            setResult(RESULT_OK, i);
                            finish();
                            return;
                        }
                    }
                }
                ToastUtil.showToast(this, "选取图片失败");
            }
        }
    }

}

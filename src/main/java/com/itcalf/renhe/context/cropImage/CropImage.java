package com.itcalf.renhe.context.cropImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.BitmapUtil;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.widget.ClipImageView;
import com.itcalf.renhe.widget.ClipView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Title: CropImage.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-9 下午3:09:57 <br>
 *
 * @author wangning
 */
public class CropImage extends BaseActivity {
    private View rootView;
    private ClipImageView mCropImage;
    private String imagePath = "";
    private Bitmap bitmap;
    private Button saveBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.fragment_cropimage);
        if (getIntent().getBooleanExtra("istocover", false)) {
            setTextValue(0, "裁剪封面");
        } else {
            setTextValue(0, "裁剪头像");
        }
        mCropImage = (ClipImageView) findViewById(R.id.src_pic);
        saveBt = (Button) findViewById(R.id.saveBt);

        imagePath = getIntent().getStringExtra("path");
        if (!TextUtils.isEmpty(imagePath)) {
//            bitmap = BitmapUtil.getBitmap(imagePath, ClipView.IMAGE_WIDTH * 2, ClipView.IMAGE_WIDTH * 2);
            bitmap = initBitmap();
            if (null == bitmap) {
                Toast.makeText(getApplicationContext(), "未找到相关图片", Toast.LENGTH_SHORT).show();
                return;
            }
            mCropImage.setBitmap(bitmap);
        }

        saveBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = mCropImage.clip();
                cacheBitmapList.add(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapByte = baos.toByteArray();
                FileUtil.writeImage(bitmap, FileUtil.SDCARD_PAHT + "/crop.png", 100);
                Intent mIntent = new Intent();
                mIntent.putExtra("cropImagePath", FileUtil.SDCARD_PAHT + "/crop.png");
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
    }

    private Bitmap initBitmap() {
//        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
//        bitmapOptions.inSampleSize = 0;
        File file = new File(imagePath);
        /**
         * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
         */
        int degree = readPictureDegree(file.getAbsolutePath());
//        Bitmap cameraBitmap = BitmapFactory.decodeFile(imagePath, bitmapOptions);
        bitmap = BitmapUtil.getBitmap(imagePath, ClipView.IMAGE_WIDTH * 2, ClipView.IMAGE_WIDTH * 2);
//        bitmap = cameraBitmap;
        /**
         * 把图片旋转为正的方向
         */
        bitmap = rotaingImageView(degree, bitmap);
        return bitmap;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != bitmap) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}

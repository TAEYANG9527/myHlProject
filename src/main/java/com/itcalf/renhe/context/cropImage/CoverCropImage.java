package com.itcalf.renhe.context.cropImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.BitmapUtil;
import com.itcalf.renhe.widget.ClipView;
import com.itcalf.renhe.widget.CoverClipImageView;

import java.io.ByteArrayOutputStream;

/**
 * Title: CropImage.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-9 下午3:09:57 <br>
 * @author wangning
 */
public class CoverCropImage extends BaseActivity {
	private View rootView;
	private CoverClipImageView mCropImage;
	private Button saveBt;
	private String imagePath = "";
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.fragment_cover_cropimage);
		setTextValue(0, "裁剪封面");

		mCropImage = (CoverClipImageView) findViewById(R.id.src_pic);
		saveBt = (Button) findViewById(R.id.saveBt);

		imagePath = getIntent().getStringExtra("path");
		if (!TextUtils.isEmpty(imagePath)) {
			bitmap = BitmapUtil.getBitmap(imagePath, ClipView.IMAGE_WIDTH * 2, ClipView.IMAGE_WIDTH * 2);
			if (null == bitmap) {
				Toast.makeText(getApplicationContext(), "未找到相关图片", Toast.LENGTH_SHORT).show();
				return;
			}
			mCropImage.setBitmap(bitmap);
			cropImage();
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

	//	@Override
	//	public boolean onPrepareOptionsMenu(Menu menu) {
	//		MenuItem saveItem = menu.findItem(R.id.item_clip);
	//		saveItem.setVisible(true);
	//		Button saveButton = (Button) saveItem.getActionView().findViewById(R.id.save);
	//		if (null != saveButton) {
	//			saveButton.setOnClickListener(new OnClickListener() {
	//				@Override
	//				public void onClick(View arg0) {
	//					Bitmap bitmap = mCropImage.clip();
	//					cacheBitmapList.add(bitmap);
	//					ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
	//					byte[] bitmapByte = baos.toByteArray();
	//					FileUtil.writeImage(bitmap, FileUtil.SDCARD_PAHT + "/crop.png", 100);
	//					Intent mIntent = new Intent();
	//					mIntent.putExtra("cropImagePath", FileUtil.SDCARD_PAHT + "/crop.png");
	//					setResult(RESULT_OK, mIntent);
	//					finish();
	//				}
	//			});
	//		}
	//		return super.onPrepareOptionsMenu(menu);
	//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//		if(!TextUtils.isEmpty(imagePath)){
		//			try {
		//				File file = new File(imagePath);
		//				if(file.exists()){
		//					file.delete();
		//				}
		//			} catch (Exception e) {
		//				e.printStackTrace();
		//			}
		//		}
		if (null != bitmap) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		if (item.getItemId() == R.id.item_clip) {
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}

	private void cropImage() {
		//		saveButton.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				new Thread(new Runnable() {
		//
		//					@Override
		//					public void run() {
		//						FileUtil.writeImage(mCropImage.getCropImage(), FileUtil.SDCARD_PAHT + "/crop.png", 100);
		//						Intent mIntent = new Intent();
		//						mIntent.putExtra("cropImagePath", FileUtil.SDCARD_PAHT + "/crop.png");
		//						setResult(RESULT_OK, mIntent);
		//						finish();
		//					}
		//				}).start();
		//			}
		//		});
	}
}

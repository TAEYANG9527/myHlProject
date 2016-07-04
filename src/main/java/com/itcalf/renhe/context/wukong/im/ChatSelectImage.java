package com.itcalf.renhe.context.wukong.im;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.BitmapUtil;
import com.itcalf.renhe.widget.ClipView;
import com.itcalf.renhe.widget.CoverClipImageView;

/**
 * Title: CropImage.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-9 下午3:09:57 <br>
 * @author wangning
 */
public class ChatSelectImage extends BaseActivity {
	private View rootView;
	private CoverClipImageView mCropImage;
	private Button saveBt;
	private String imagePath = "";
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.chat_select_cropimage);
	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(0, "图片");
		mCropImage = (CoverClipImageView) findViewById(R.id.src_pic);
		saveBt = (Button) findViewById(R.id.saveBt);

		imagePath = getIntent().getStringExtra("path");
		if (!TextUtils.isEmpty(imagePath)) {
			//			Size bitmapSize = BitmapUtil.getBitMapSize(imagePath);
			bitmap = BitmapUtil.getBitmap(imagePath, ClipView.IMAGE_WIDTH * 2, ClipView.IMAGE_WIDTH * 2);
			if (null == bitmap) {
				cacheBitmapList.add(bitmap);
				Toast.makeText(getApplicationContext(), "未找到相关图片", Toast.LENGTH_SHORT).show();
				return;
			}
			mCropImage.setBitmap(bitmap);
		}

		saveBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent();
				mIntent.putExtra("cropImagePath", imagePath);
				setResult(RESULT_OK, mIntent);
				finish();
			}
		});
	}

	//	@Override
	//	public boolean onPrepareOptionsMenu(Menu menu) {
	//		MenuItem saveItem = menu.findItem(R.id.item_clip);
	//		saveItem.setTitle("发送");
	//		saveItem.setVisible(true);
	//		Button saveButton = (Button) saveItem.getActionView().findViewById(R.id.save);
	//		saveButton.setText("发送");
	//		if (null != saveButton) {
	//			saveButton.setOnClickListener(new OnClickListener() {
	//				@Override
	//				public void onClick(View arg0) {
	//					Intent mIntent = new Intent();
	//					mIntent.putExtra("cropImagePath", imagePath);
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
}

package com.itcalf.renhe.imageUtil;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.itcalf.renhe.utils.FileUtil;

/**
 * Created by wangning on 2015/9/17.
 */
public class DeleteSDCardImageUtil {
	public static void sdScan(Context ct, String imagePath) {
		//删除本地的裁剪缓存图片crop.jpg
		deleteImage(ct, imagePath);//相册图库删除相应path的图片，否则只调用deleteFile删除本地图片是没作用的，图库里还是有这张图片
		FileUtil.deleteFile(imagePath);
	}

	private static void deleteImage(Context ct, String imgPath) {
		ContentResolver resolver = ct.getContentResolver();
		Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=?", new String[] { imgPath },
				null);
		if (cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			Uri uri = ContentUris.withAppendedId(contentUri, id);
			ct.getContentResolver().delete(uri, null, null);
		}
	}
}

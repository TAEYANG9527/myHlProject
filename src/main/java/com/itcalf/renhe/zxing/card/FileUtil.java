package com.itcalf.renhe.zxing.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.itcalf.renhe.log.Logger;
import com.itcalf.renhe.log.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {

	private final static Logger log = LoggerFactory.getInstance(FileUtil.class);

	public static String getImageStorageDirectory(Context context) {
		return context.getExternalFilesDir("images").getAbsolutePath();
	}

	public static String getImageThumbnailDirectory(Context context) {
		return context.getExternalFilesDir("images_thumbnail").getAbsolutePath();
	}

	public static String getImageTempDirectory(Context context) {
		return context.getExternalFilesDir("images_tmp").getAbsolutePath();
	}

	public static String getExternalDirectory(Context context) {
		return context.getExternalFilesDir(null).getAbsolutePath();
	}

	public static String getExternalDirectory(Context context, String dirname) {
		return context.getExternalFilesDir(dirname).getAbsolutePath();
	}

	public static String getExternalCacheDirectory(Context context) {
		return context.getExternalCacheDir().getAbsolutePath();
	}

	public static boolean saveDataToFile(byte[] data, File file) throws IOException {
		if (file == null) {
			return false;
		}

		// 准备目录
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		// 准备文件
		if (!file.exists()) {
			file.createNewFile();
		}

		OutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(data);
			outputStream.flush();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		return false;
	}

	public static boolean saveInputStreamToFile(InputStream inputStream, File file) throws IOException {
		if (file == null) {
			return false;
		}
		// 准备目录
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		// 准备文件
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[10240];
			int readLen = 0;
			while ((readLen = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, readLen);
			}
			outputStream.flush();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
		return false;
	}

	public static boolean createImageThumbnail(byte[] data, File destFile, int width, int height) throws IOException {
		if (destFile == null) {
			return false;
		}
		// 准备目录
		if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}
		// 准备文件
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);

		int scaleWidth = options.outWidth / width;
		int scaleHeight = options.outHeight / height;
		int scale = Math.max(scaleWidth, scaleHeight);
		options.inJustDecodeBounds = false;
		options.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		OutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(destFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.flush();
			bitmap.recycle();
			bitmap = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			outputStream.close();
		}
	}

	public static boolean createImageThumbnail(Bitmap bitmap, File destFile, int width, int height) throws IOException {
		if (destFile == null) {
			return false;
		}
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(destFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			outputStream.flush();
			bitmap.recycle();
			bitmap = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			outputStream.close();
		}
	}

	public static boolean createImageThumbnail(File srcFile, File destFile, int width, int height) throws IOException {
		if (destFile == null)
			return false;
		// make dir
		if (!destFile.getParentFile().exists()) {
			destFile.getParentFile().mkdirs();
		}
		// make file
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);

		int scale = Math.max(options.outWidth / width, options.outHeight / height);
		int inSampleSize = scale <= 0 ? 1 : scale;

		options.inJustDecodeBounds = false;
		options.inSampleSize = inSampleSize;

		Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
		OutputStream os = null;
		try {
			os = new FileOutputStream(destFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.flush();
			bitmap.recycle();
			bitmap = null;
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			bitmap = null;
			if (os != null) {
				os.close();
			}
		}
	}

	// get file loacl url
	public static String getImageUrl(Context context, String fileName) {
		return new File(getImageStorageDirectory(context), fileName).getAbsolutePath();
	}

	// get thumbnail file loacl url
	public static String getImageThumbnailUrl(Context context, String fileName) {
		return new File(getImageThumbnailDirectory(context), fileName).getAbsolutePath();
	}

	// delete file (full path)
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.delete();
		}
		return Boolean.TRUE;
	}

	@SuppressLint("SimpleDateFormat")
	public static String processPictureFile(Context context, String filePath) {
		String imageDir = FileUtil.getImageStorageDirectory(context);
		String tempDir = FileUtil.getImageTempDirectory(context);
		String thumbDir = FileUtil.getImageThumbnailDirectory(context);
		try {
			String fileName = new SimpleDateFormat("yyyyMMddkkmmssSSSS").format(new Date(System.currentTimeMillis())).toString()
					+ ".jpeg";
			File tempFile = new File(tempDir, fileName);
			FileUtil.createImageThumbnail(new File(filePath), tempFile, 1024, 768);
			// 把照片文件转移到图片目录
			File imageFile = new File(imageDir, fileName);
			if (tempFile.exists()) {
				tempFile.renameTo(imageFile);
			} else {
				return null;
			}
			// 生产缩略图
			File thumbFile = new File(thumbDir, fileName);
			FileUtil.createImageThumbnail(imageFile, thumbFile, 100, 100);
			return fileName;
		} catch (Exception ex) {
			log.error("error to save picture >>>", ex);
		}
		return null;
	}

}

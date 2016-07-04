package com.itcalf.renhe.cache;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class ExternalStorageUtil {

	public static String getCacheAvatarPath(Context ct, String email) {
		String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
				+ File.separator + email + File.separator + "cache" + File.separator + "avatar" + File.separator;
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return path;
	}

	public static void delCacheAvatarPath(Context ct, String email) {
		String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
				+ File.separator + email + File.separator + "cache" + File.separator + "avatar" + File.separator + "userpic"
				+ File.separator;
		File file = new File(path);
		deleteDir(file);
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static String getCacheDataPath(Context ct, String email) {
		String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
				+ File.separator + email + File.separator + "cache" + File.separator + "data" + File.separator;
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return path;
	}

	public static String getPicCacheDataPath(Context ct, String email) {
		String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
				+ File.separator + email + File.separator + "renhePic" + File.separator;
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return path;
	}

	public static String getCacheRootDir(Context ct, String email) {
		String path = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data"
				+ File.separator + email + File.separator + "cache" + File.separator;
		File file = new File(path);
		if (!file.isDirectory()) {
			file.mkdirs();
		}
		return path;
	}

	// protected static String getMemory(String memory) {
	// StatFs statfs;
	// if (memory.equals("Phone")) {
	// File file = Environment.getDataDirectory();
	// statfs = new StatFs(file.getPath());
	// } else {
	// if (Environment.getExternalStorageState().equals(
	// Environment.MEDIA_MOUNTED)) {
	// File path = Environment.getExternalStorageDirectory();
	// statfs = new StatFs(path.getPath());
	// } else {
	// return "No SDCard!";
	// }
	// }
	// long blockSize = statfs.getBlockSize();
	// long totalBlocks = statfs.getBlockCount();
	// long availBlocks = statfs.getAvailableBlocks();
	// String[] total = filesize(totalBlocks * blockSize);
	// String[] avail = filesize(availBlocks * blockSize);
	// return avail[0] + avail[1] + "/" + total[0] + total[1];
	// }

}

package com.itcalf.renhe.utils;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @description TODO
 * @dataTime 2013年10月21日 下午4:30:36
 * @author johnny
 */
public class ManifestUtil {

	public static String channel;

	public static String getUmengChannel(Context context) {
		//return getMetaDataFromAppication(context, "UMENG_CHANNEL");
		return getChannel(context);
	}

	/**
	 * 获取META-INFO下面的渠道
	 * @param context
	 * @return
	 */
	public static String getChannel(Context context) {
		if (!TextUtils.isEmpty(channel)) {
			return channel;
		}
		ApplicationInfo appinfo = context.getApplicationInfo();
		String sourceDir = appinfo.sourceDir;
		ZipFile zipfile = null;
		final String start_flag = "META-INF/mtchannel_";
		try {
			zipfile = new ZipFile(sourceDir);
			Enumeration<?> entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) entries.nextElement());
				String entryName = entry.getName();
				if (entryName.contains(start_flag)) {
					channel = entryName.replaceAll(start_flag, "");
					return channel;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zipfile != null) {
				try {
					zipfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "renhe_android";

	}

	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getVersionCode(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			return packInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getMetaDataFromActivity(Activity context, String key) {
		try {
			ActivityInfo info = context.getPackageManager().getActivityInfo(context.getComponentName(),
					PackageManager.GET_META_DATA);
			return info.metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMetaDataFromAppication(Context context, String key) {
		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
					PackageManager.GET_META_DATA);
			return appInfo.metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMetaDataFromService(Context context, Class<? extends Service> clazz, String key) {
		try {
			ComponentName cn = new ComponentName(context, clazz);
			ServiceInfo info = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
			return info.metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getMetaDataFromReceiver(Context context, Class<? extends BroadcastReceiver> clazz, String key) {
		try {
			ComponentName cn = new ComponentName(context, clazz);
			ActivityInfo info = context.getPackageManager().getReceiverInfo(cn, PackageManager.GET_META_DATA);
			return info.metaData.getString(key);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

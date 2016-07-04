package com.itcalf.renhe.utils;

import android.util.Log;

import com.itcalf.renhe.log.Logger;
import com.itcalf.renhe.log.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import cn.sharp.android.ncr.ocr.OCRItems;
import cn.sharp.android.ncr.ocr.OCRManager;

/**
 * Title: RecFileUtil.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2013<br>
 * Create DateTime: 2013-12-11 上午11:02:02<br>
 * 
 * @author Conch
 */

public class RecFileUtil {

	private final static Logger log = LoggerFactory.getInstance(RecFileUtil.class);

	/**
	 * do rec cardImage
	 * 
	 * @param cardImage AbsoluteFile
	 * @return
	 */
	public synchronized static String recCardImage(File cardImage) {
		InputStream is = null;
		String result = "";
		try {
			is = new BufferedInputStream(new FileInputStream(cardImage));
			if (is != null) {
				byte[] jpegData = new byte[(int) cardImage.length()];
				is.read(jpegData);
				Log.i("File = ", cardImage + "");
				OCRItems ocrItems = new OCRManager().rec(jpegData);
				if (ocrItems != null) {
					result = ocrItems.returnJSONData();
				}
			}
			return result;
		} catch (FileNotFoundException e) {
			log.error("RecFileUtil.class File Not Found", e);
		} catch (IOException e) {
			log.error("RecFileUtil.class error", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("RecFileUtil.class close InputStream error when do imageFileOCR", e);
				}
			}
		}
		return result;
	}
}

package com.itcalf.renhe.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.itcalf.renhe.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description : 文本高亮
 * Created by Chans
 * 2015/7/13
 */
public class HighlightTextUtil {

	/**
	 * @param toString 输入待高亮的string 每个字符分开高亮
	 * @param context  输入完整的串
	 * @return
	 */
	public SpannableString toHighlight(Context ct, String toString, String context) {
		//被搜索的内容不为null
		if (TextUtils.isEmpty(context))
			return null;

		context = context.toUpperCase();
		SpannableString spannableString = new SpannableString(context);
		if (!TextUtils.isEmpty(toString)) {
			//搜索内容转化为大写
			toString = toString.toUpperCase();

			//判断输入的字符是否含有汉字，含汉字，每个字符分开搜索 仅第一次出现高亮
			boolean includeChinese = isContainChinese(toString);
			if (includeChinese) {
				//包含汉字，则每个都分开去搜索
				char[] c = toString.toCharArray();
				for (int i = 0, s = c.length; i < s; i++) {
					int position = context.indexOf(c[i]);
					if (position > -1)
						spannableString.setSpan(new ForegroundColorSpan(ct.getResources().getColor(R.color.red)), position,
								position + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				}
			} else {
				try {
					//全是拼音
					String[] str;
					//判断是否含“ ”，有，分开搜索
					if (toString.contains(" ")) {
						str = toString.split(" ");
					} else {
						str = new String[] { toString };
					}
					//取前50个字进行比对
					context = context.length() > 50 ? context.substring(0, 50) : context;
					for (int i = 0, s = str.length; i < s; i++) {
						String qpContext = PinyinUtil.cn2Spell(context);
						String pyContext = PinyinUtil.cn2FirstSpellUppercase(context);//转化成拼音首字母大写
						//汉字全拼
						if (qpContext.startsWith(str[i])) {
							char[] py = context.toCharArray();
							int num = 1;//高亮的字数
							int pyLength = 0;
							for (int j = 0, n = py.length; j < n; j++) {
								pyLength += PinyinUtil.cn2Spell(String.valueOf(py[j])).length();
								if (str[i].length() > pyLength) {
									num++;
								} else {
									break;
								}

							}
							if (num > 0)
								spannableString.setSpan(new ForegroundColorSpan(ct.getResources().getColor(R.color.red)), 0, num,
										Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						}
						//汉字首字母
						if (pyContext.startsWith(str[i])) {
							spannableString.setSpan(new ForegroundColorSpan(ct.getResources().getColor(R.color.red)), 0,
									str[i].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						}
						//拼音
						if (context.contains(str[i])) {
							int position = context.indexOf(str[i]);
							if (position > -1)
								spannableString.setSpan(new ForegroundColorSpan(ct.getResources().getColor(R.color.red)),
										position, position + str[i].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return spannableString;
	}

	/**
	 * 判断是否包含中文字符
	 *
	 * @param str
	 * @return
	 */
	public static boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}
}

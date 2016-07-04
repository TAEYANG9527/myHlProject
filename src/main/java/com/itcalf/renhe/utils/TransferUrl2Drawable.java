package com.itcalf.renhe.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.view.TextViewFixTouchConsume;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;
import com.umeng.analytics.MobclickAgent;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Title: TransferUrl2Drawable.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014 <br>
 * Create DateTime: 2014-6-13 上午11:14:09 <br>
 * 
 * @author wangning
 */
public class TransferUrl2Drawable {
	private Context context;
	private List<UrlList> urlList;

	public TransferUrl2Drawable(Context context) {
		this.context = context;
	}

	public String transferUrl(SpannableString text) {
		String s = text.toString();
		urlList = toHref(s);
		//		Drawable drawable = context.getResources().getDrawable(R.drawable.url_bacg);
		//		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		if (urlList != null && urlList.size() > 0) {
			for (int i = 0; i < urlList.size(); i++) {
				String url = urlList.get(i).getUrls();
				Integer[] index = urlList.get(i).getIndex();
				MyURLSpan myURLSpan = new MyURLSpan(url);
				if (index[0] != null && index[1] != null) {
					text.setSpan(myURLSpan, index[0], index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.CL)), index[0], index[1],
							Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					//					text.setSpan(new ImageSpan(drawable), index[0], index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					//					s = s.replaceAll(url, "");
				}
			}
		}
		return s;
	}

	/**
	 * 聊天界面处理链接
	 * @param text
	 * @return
	 */
	public String transferChatUrl(SpannableString text, int type) {
		String s = text.toString();
		urlList = toHref(s);
		if (urlList != null && urlList.size() > 0) {
			for (int i = 0; i < urlList.size(); i++) {
				String url = urlList.get(i).getUrls();
				Integer[] index = urlList.get(i).getIndex();
				MyURLSpan myURLSpan = new MyURLSpan(url);
				if (index[0] != null && index[1] != null) {
					text.setSpan(myURLSpan, index[0], index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					if (type == 0) {
						text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.white)), index[0], index[1],
								Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					} else {
						text.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.room_at_color)), index[0],
								index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					}
					s = s.replaceAll(url, "");
				}
			}
		}
		return s;
	}

	private void stripUnderlines(SpannableString s) {
		URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = s.getSpanStart(span);
			int end = s.getSpanEnd(span);
			s.removeSpan(span);
			span = new URLSpanNoUnderline(span.getURL());
			s.setSpan(span, start, end, 0);
		}
	}

	// 需要一个自定义的URLSpan，不用启动TextPaint的“下划线”属性:

	private class URLSpanNoUnderline extends URLSpan {
		public URLSpanNoUnderline(String url) {
			super(url);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
	}

	public void transferUrl2(SpannableString text) {
		Map<String, Integer[]> map = toHref2(text.toString());
		// SpannableStringBuilder style = new SpannableStringBuilder(text);
		//		Drawable drawable = context.getResources().getDrawable(R.drawable.url_bacg);
		//		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		for (Map.Entry<String, Integer[]> entry : map.entrySet()) {
			String url = entry.getKey();
			Integer[] index = entry.getValue();
			MyURLSpan myURLSpan = new MyURLSpan(url);
			if (index[0] != null && index[1] != null) {
				text.setSpan(myURLSpan, index[0], index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				text.setSpan(new UnderlineSpan(), index[0], index[1], Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				//			text.setSpan(new ImageSpan(drawable), index[0], index[1], Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			}
		}
	}

	class MyURLSpan extends ClickableSpan {

		private String mUrl;

		MyURLSpan(String url) {
			mUrl = url;
		}

		@Override
		public void onClick(View widget) {
			MobclickAgent.onEvent(context, "点击人脉圈的“网页链接”");
			// Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
			// context.startActivity(intent);
			Intent i = new Intent();
			i.setClass(context, WebViewActWithTitle.class);
			i.putExtra("url", mUrl);
			context.startActivity(i);
		}
	}

	//	public static String regex = "((http[s]{0,1}|ftp)://|www)[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?";

	//	public static String regex = "(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";

	//	public static String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";

	//	public static String regex = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";

	public static Map<String, Integer[]> toHref2(String title) {
		String url = "";
		Pattern pat = Pattern.compile(Constants.PATTERN_URL);
		Matcher mat = pat.matcher(title);
		Map<String, Integer[]> map = new HashMap<String, Integer[]>();
		while (mat.find()) {
			url = mat.group();
			// if (url.startsWith("(") && url.endsWith(")")) {
			// url = url.substring(1, url.length() - 1);
			// }
			// if (url.indexOf("http://") < 0)
			// url = "http://" + url;
			map.put(url, new Integer[] { mat.start(), mat.end() });
		}
		return map;
	}

	public List<UrlList> toHref(String title) {
		String url = "";
		Pattern pat = Pattern.compile(Constants.PATTERN_URL);
		Matcher mat = pat.matcher(title);
		List<UrlList> list = new ArrayList<UrlList>();
		while (mat.find()) {
			url = mat.group();
			UrlList ul = new UrlList();
			ul.setIndex(new Integer[] { mat.start(), mat.end() });
			ul.setUrls(url);
			list.add(ul);
		}
		return list;
	}

	// public boolean isValidUrl1(String url) {
	// boolean isValid = false;
	// URL mUrl;
	// try {
	// mUrl = new URL(url);
	// InputStream in = mUrl.openStream();
	// isValid = true;
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// isValid = false;
	// mUrl = null;
	// }
	// return isValid;
	// }

	// 截取4行转发内容
	public static String measureLine(AisenTextView tx, String content, int contentWidth) {
		// content = content.replaceAll("http://+(.*?) ", "");
		int width = (int) getTextViewLength(tx, content);
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(2);
		double d = width * 1.0 / contentWidth;

		if (new BigDecimal(f.format(d)).compareTo(new BigDecimal(4)) > 0) {
			String ellipsizeStr = (String) TextUtils.ellipsize(content, tx.getPaint(), contentWidth * 4 - 2,
					TextUtils.TruncateAt.END);
			return ellipsizeStr;
		}
		return content;
	}

	// 计算出该TextView中文字的长度(像素)
	public static float getTextViewLength(TextView textView, String text) {
		TextPaint paint = textView.getPaint();
		float textLength = paint.measureText(text);
		return textLength;
	}

	/**
	 *  计算出该TextView中文字的长度(像素)(包括换行符)
	 */
	public static float getTextViewLengthWithWrap(TextView textView, String text, int contentWidth) {
		float textFinalLength = 0f;

		TextPaint paint = textView.getPaint();
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(2);
		while (text.contains("\n")) {
			String text1 = text.substring(0, text.indexOf("\n"));
			float textLength = paint.measureText(text1);
			double d = textLength * 1.0 / contentWidth;
			String s = f.format(d);
			int c = 0;
			if (s.contains(".")) {
				c = Integer.parseInt(s.substring(0, s.indexOf(".")));
				if (Integer.parseInt(s.substring(s.indexOf(".") + 1, s.length())) > 0) {
					c = c + 1;
				}
			} else {
				c = c + 1;
			}
			//			int zs = (int) textLength / contentWidth;
			//			int ys = (int) textLength % contentWidth;
			//			if (ys > 0) {
			//				zs = zs + 1;
			//			}
			//			textFinalLength = zs * contentWidth;
			textFinalLength = textFinalLength + c * contentWidth;
			text = text.substring(text.indexOf("\n") + 1, text.length());
		}
		if (text.length() > 0) {
			float textLength = paint.measureText(text);
			textFinalLength = textFinalLength + textLength;
		}
		return textFinalLength;
	}

	// 获取屏幕宽度
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	// Dip转px
	public static int dipToPX(final Context ctx, float dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
	}

	public class UrlList {
		String urls;
		Integer[] index;

		public UrlList() {
		}

		public String getUrls() {
			return urls;
		}

		public void setUrls(String urls) {
			this.urls = urls;
		}

		public Integer[] getIndex() {
			return index;
		}

		public void setIndex(Integer[] index) {
			this.index = index;
		}
	}

	public Map<String, String> getUrlPramNameAndValue(String url) {
		String regEx = "(\\?|&+)(.+?)=([^&]*)";//匹配参数名和参数值的正则表达式
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(url);
		// LinkedHashMap是有序的Map集合，遍历时会按照加入的顺序遍历输出
		Map<String, String> paramMap = new LinkedHashMap<String, String>();
		while (m.find()) {
			String paramName = m.group(2);//获取参数名
			String paramVal = m.group(3);//获取参数值
			paramMap.put(paramName, paramVal);
		}
		return paramMap;
	}
}

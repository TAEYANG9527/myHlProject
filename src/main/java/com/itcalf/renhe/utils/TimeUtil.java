package com.itcalf.renhe.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd hh:mm:ss";
	public static DecimalFormat mDf = new DecimalFormat("00");

	public static String getRoleTime() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return "今天" + mDf.format(cal.get(Calendar.HOUR)) + ":" + mDf.format(cal.get(Calendar.MINUTE));
	}

	public static String getRoleTime(String datetime) {
		SimpleDateFormat lSdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		try {
			Calendar lCompCal = Calendar.getInstance();
			lCompCal.setTime(lSdf.parse(datetime));

			Calendar lCal = Calendar.getInstance();
			lCal.setTime(new Date());
			lCal.add(Calendar.DAY_OF_MONTH, -1);

			/*** 当天  几点几分  如16:30 */
			if (lCompCal.after(lCal)) {
				return mDf.format(lCompCal.get(Calendar.HOUR_OF_DAY)) + ":" + mDf.format(lCompCal.get(Calendar.MINUTE));
			}
			/*** 前一天  昨天 */
			lCal.add(Calendar.DAY_OF_MONTH, -1);
			if (lCompCal.after(lCal)) {
				return "昨天";
			}
			/*** 前二天  2天前 */
			lCal.add(Calendar.DAY_OF_MONTH, -1);
			if (lCompCal.after(lCal)) {
				return "2天前";
			}
			/*** 前三天  3天前 */
			lCal.add(Calendar.DAY_OF_MONTH, -1);
			if (lCompCal.after(lCal)) {
				return "3天前";
			}
			/*** 超过4天以上显示几月几号  3月9日 */
			return lCompCal.get(Calendar.MONTH) + 1 + "月" + lCompCal.get(Calendar.DAY_OF_MONTH) + "日";
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return datetime;
	}

	public static String getMonthTime(String time) {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(time));
			return mDf.format(cal.get(Calendar.MONTH)) + "-" + mDf.format(cal.get(Calendar.MONDAY)) + " "
					+ mDf.format(cal.get(Calendar.HOUR)) + ":" + mDf.format(cal.get(Calendar.MINUTE));
		} catch (ParseException e) {
			return "";
		}
	}

}

package com.itcalf.renhe.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.widget.TextView;

import com.itcalf.renhe.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class DateUtil {

    private static long MINUTE = 60L * 1000L;
    private static long HOUR = 60L * 60L * 1000L;
    private static long DAY = 24L * 60L * 60L * 1000L;

    private static final int LING_CHEN = 0;
    private static final int ZAO_SHANG = 1;
    private static final int ZHONG_WU = 2;
    private static final int XIA_WU = 3;
    private static final int WAN_SHANG = 4;

    @SuppressWarnings("unused")
    public static void string2Date(Context ct, long date, TextView dateTv) {
        long DAY = 24L * 60L * 60L * 1000L;
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        Date date2 = new Date(date);
        if (null != date2) {
            long diff = now.getTime() - date2.getTime();
            dateTv.setText(DateUtil.formatToGroupTagByDay(ct, date2));
        } else {
            dateTv.setText("");
        }

    }

    public static void collectString2Date(Context ct, long date, TextView dateTv) {
        DateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
        Date date2 = new Date(date);
        dateTv.setText(format1.format(date2.getTime() * 1000));
    }

    public static String newFormatByDay(Context context, Date date) {
        if (context == null || date == null) {
            return context.getString(R.string.readable_date_more);
        }
        Date now = new Date();
        long diff = now.getTime() - date.getTime();
        String timePeriod = "";
        switch (amOrPm(date)) {
            case ZAO_SHANG:
                timePeriod = "早上";
                break;
            case XIA_WU:
                timePeriod = "下午";
                break;
            case WAN_SHANG:
                timePeriod = "晚上";
                break;
            case LING_CHEN:
                timePeriod = "凌晨";
                break;
            case ZHONG_WU:
                timePeriod = "中午";
                break;
            default:
                break;
        }
        DateFormat format1 = new SimpleDateFormat("M月d日");
        DateFormat format2 = new SimpleDateFormat("HH:mm");
        DateFormat format3 = new SimpleDateFormat("yyyy年M月d日");
        StringBuffer formatDate = new StringBuffer();

        String yearAndMin = format3.format(date);
        String monthAndDay = format1.format(date);
        String hourAndMin = format2.format(date);
        if (diff > DAY * 3) {// 如果>3天，就用天做时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == year) {
                formatDate.append(monthAndDay + " " + timePeriod + hourAndMin);
            } else {
                formatDate.append(yearAndMin + " " + timePeriod + hourAndMin);
            }
        } else {
            if (diff > DAY * 2 && diff <= DAY * 3) {// 如果>2&<3天，就前天做时间
                if (areSameDay(now.getTime() - 2 * DAY, date.getTime())) {
                    formatDate.append("前天 " + timePeriod + hourAndMin);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    calendar.setTime(date);
                    if (calendar.get(Calendar.YEAR) == year) {
                        formatDate.append(monthAndDay + " " + timePeriod + hourAndMin);
                    } else {
                        formatDate.append(yearAndMin + " " + timePeriod + hourAndMin);
                    }
                }
            } else if (diff > DAY && diff <= DAY * 2) {// 如果>1&<2天，就昨天做时间
                if (areSameDay(now.getTime() - DAY, date.getTime())) {
                    formatDate.append("昨天 " + timePeriod + hourAndMin);
                } else {
                    formatDate.append("前天 " + timePeriod + hourAndMin);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                int canDay = calendar.get(Calendar.DAY_OF_MONTH);
                int canyear = calendar.get(Calendar.YEAR);
                calendar.setTime(date);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);
                if (canDay == day) {
                    formatDate.append(timePeriod + hourAndMin);
                } else {
                    formatDate.append("昨天 " + timePeriod + hourAndMin);
                }
            }
        }
        return formatDate.toString();
    }

    public static String newFormatByDayForListDisply(Context context, Date date) {
        if (context == null || date == null) {
            return context.getString(R.string.readable_date_more);
        }
        Date now = new Date();
        long diff = now.getTime() - date.getTime();
        String timePeriod = "";
        switch (amOrPm(date)) {
            case ZAO_SHANG:
                timePeriod = "早上";
                break;
            case XIA_WU:
                timePeriod = "下午";
                break;
            case WAN_SHANG:
                timePeriod = "晚上";
                break;
            case LING_CHEN:
                timePeriod = "凌晨";
                break;
            case ZHONG_WU:
                timePeriod = "中午";
                break;
            default:
                break;
        }
        DateFormat format1 = new SimpleDateFormat("M月d日");
        DateFormat format2 = new SimpleDateFormat("HH:mm");
        DateFormat format3 = new SimpleDateFormat("yyyy年M月d日");
        StringBuffer formatDate = new StringBuffer();

        String yearAndMin = format3.format(date);
        String monthAndDay = format1.format(date);
        String hourAndMin = format2.format(date);
        if (diff > DAY * 3) {// 如果>3天，就用天做时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == year) {
                formatDate.append(monthAndDay);
            } else {
                formatDate.append(yearAndMin);
            }
        } else {
            if (diff > DAY * 2 && diff <= DAY * 3) {// 如果>2&<3天，就前天做时间
                if (areSameDay(now.getTime() - 2 * DAY, date.getTime())) {
                    formatDate.append("前天 " + timePeriod + hourAndMin);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    calendar.setTime(date);
                    if (calendar.get(Calendar.YEAR) == year) {
                        formatDate.append(monthAndDay);
                    } else {
                        formatDate.append(yearAndMin);
                    }
                }
            } else if (diff > DAY && diff <= DAY * 2) {// 如果>1&<2天，就昨天做时间
                if (areSameDay(now.getTime() - DAY, date.getTime())) {
                    formatDate.append("昨天 " + timePeriod + hourAndMin);
                } else {
                    formatDate.append("前天 " + timePeriod + hourAndMin);
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                int canDay = calendar.get(Calendar.DAY_OF_MONTH);
                int canyear = calendar.get(Calendar.YEAR);
                calendar.setTime(date);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);
                if (canDay == day) {
                    formatDate.append(timePeriod + hourAndMin);
                } else {
                    formatDate.append("昨天 " + timePeriod + hourAndMin);
                }
            }
        }
        return formatDate.toString();
    }

    public static String conversationFormatByDayForListDisply(Context context, Date date) {
        if (context == null || date == null) {
            return context.getString(R.string.readable_date_more);
        }
        Date now = new Date();
        long diff = now.getTime() - date.getTime();
        String timePeriod = "";
        switch (amOrPm(date)) {
            case ZAO_SHANG:
                timePeriod = "早上";
                break;
            case XIA_WU:
                timePeriod = "下午";
                break;
            case WAN_SHANG:
                timePeriod = "晚上";
                break;
            case LING_CHEN:
                timePeriod = "凌晨";
                break;
            case ZHONG_WU:
                timePeriod = "中午";
                break;
            default:
                break;
        }
        DateFormat format1 = new SimpleDateFormat("M月d日");
        DateFormat format2 = new SimpleDateFormat("HH:mm");
        DateFormat format3 = new SimpleDateFormat("yyyy年M月d日");
        StringBuffer formatDate = new StringBuffer();

        String yearAndMin = format3.format(date);
        String monthAndDay = format1.format(date);
        String hourAndMin = format2.format(date);
        if (diff > DAY * 3) {// 如果>3天，就用天做时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == year) {
                formatDate.append(monthAndDay);
            } else {
                formatDate.append(yearAndMin);
            }
        } else {
            if (diff > DAY * 2 && diff <= DAY * 3) {// 如果>2&<3天，就前天做时间
                if (areSameDay(now.getTime() - 2 * DAY, date.getTime())) {
                    formatDate.append("前天 ");
                } else {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    calendar.setTime(date);
                    if (calendar.get(Calendar.YEAR) == year) {
                        formatDate.append(monthAndDay);
                    } else {
                        formatDate.append(yearAndMin);
                    }
                }
            } else if (diff > DAY && diff <= DAY * 2) {// 如果>1&<2天，就昨天做时间
                if (areSameDay(now.getTime() - DAY, date.getTime())) {
                    formatDate.append("昨天 ");
                } else {
                    formatDate.append("前天 ");
                }
            } else {
                Calendar calendar = Calendar.getInstance();
                int canDay = calendar.get(Calendar.DAY_OF_MONTH);
                int canyear = calendar.get(Calendar.YEAR);
                calendar.setTime(date);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);
                if (canDay == day) {
                    formatDate.append(timePeriod + hourAndMin);
                } else {
                    formatDate.append("昨天 ");
                }
            }
        }
        return formatDate.toString();
    }

    /**
     * 判断指定时间是中午还是下午
     *
     * @return 0:早上  1:下午  2:晚上
     */
    public static int amOrPm(Date date) {
        int hour = date.getHours();
        int time = ZAO_SHANG;
        if (hour > 18 && hour < 24) {//晚上
            time = WAN_SHANG;
        } else if (hour >= 0 && hour < 6) {//凌晨
            time = LING_CHEN;
        } else if (hour >= 6 && hour < 12) {//上午
            time = ZAO_SHANG;
        } else if (hour >= 12 && hour < 13) {//上午
            time = ZHONG_WU;
        } else if (hour >= 13 && hour <= 18) {//上午
            time = XIA_WU;
        }
        return time;
    }

    public static String formatToGroupTag(Context context, Date date) {
        if (context == null || date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTime(date);

        if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
                && calendar.get(Calendar.DAY_OF_MONTH) == day) {
            return context.getString(R.string.readable_date_today);
        } else if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.WEEK_OF_YEAR) == week) {
            return context.getString(R.string.readable_date_week);
        } else if (calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month) {
            return context.getString(R.string.readable_date_month);
        } else {
            return context.getString(R.string.readable_date_more);
        }

    }

    /**
     * 判断两个时期是否是同一天
     *
     * @return
     */
    public static boolean areSameDay(Long time1, Long time2) {
        Date dateA = new Date(time1);
        Date dateB = new Date(time2);

        Calendar calDateA = Calendar.getInstance();
        calDateA.setTime(dateA);

        Calendar calDateB = Calendar.getInstance();
        calDateB.setTime(dateB);

        return calDateA.get(Calendar.YEAR) == calDateB.get(Calendar.YEAR)
                && calDateA.get(Calendar.MONTH) == calDateB.get(Calendar.MONTH)
                && calDateA.get(Calendar.DAY_OF_MONTH) == calDateB.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 将时间戳转化成String
     *
     * @param time       输入时间戳
     * @param formatType 需要的格式，比如2015/11/4
     */
    public static String getFormatDate(long time, String formatType) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatType);
        return sdf.format(new Date(time));
    }

    /**
     * 人脉圈时间格式
     *
     * @param context
     * @param date
     * @return
     */
    public static String formatToGroupTagByDay(Context context, Date date) {
        Resources res = context.getResources();
        StringBuffer buffer = new StringBuffer();
        Calendar createCal = Calendar.getInstance();
        createCal.setTimeInMillis(date.getTime());
        Calendar currentcal = Calendar.getInstance();
        currentcal.setTimeInMillis(System.currentTimeMillis());

        long diffTime = (currentcal.getTimeInMillis() - createCal.getTimeInMillis()) / 1000;
        if (currentcal.get(Calendar.YEAR) == createCal.get(Calendar.YEAR)) {
            // 同一月
            if (currentcal.get(Calendar.MONTH) == createCal.get(Calendar.MONTH)) {
                // 同一天
                if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                    if (diffTime < 3600 && diffTime >= 60) {
                        buffer.append((diffTime / 60) + res.getString(R.string.msg_few_minutes_ago));
                    } else if (diffTime < 60) {
                        buffer.append(res.getString(R.string.msg_now));
                    } else if (diffTime >= 3600 && diffTime <= 6 * 3600) {
                        buffer.append((diffTime / 3600) + res.getString(R.string.msg_few_hours_ago));
                    } else {
                        buffer.append(res.getString(R.string.msg_today));
                    }
                } else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 1) {// 前一天
                    buffer.append(res.getString(R.string.msg_yesterday));
                } else if (currentcal.get(Calendar.DAY_OF_MONTH) - createCal.get(Calendar.DAY_OF_MONTH) == 2) {
                    buffer.append(res.getString(R.string.readable_date_before_yesterday));
                } else {
                    return "3天前";
                }
            } else {
                //当前日期减去1天，如果日期对应，代表是昨天
                currentcal.add(Calendar.DATE, -1);
                if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                    buffer.append(res.getString(R.string.msg_yesterday));
                    return buffer.toString();
                }
                //当前日期再减去1天，如果日期对应，代表是前天
                currentcal.add(Calendar.DATE, -1);
                if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                    buffer.append(res.getString(R.string.readable_date_before_yesterday));
                    return buffer.toString();
                }
                return "3天前";
            }
        } else {
            //当前日期减去1天，如果日期对应，代表是昨天
            currentcal.add(Calendar.DATE, -1);
            if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                buffer.append(res.getString(R.string.msg_yesterday));
                return buffer.toString();
            }
            //当前日期再减去1天，如果日期对应，代表是前天
            currentcal.add(Calendar.DATE, -1);
            if (currentcal.get(Calendar.DAY_OF_MONTH) == createCal.get(Calendar.DAY_OF_MONTH)) {
                buffer.append(res.getString(R.string.readable_date_before_yesterday));
                return buffer.toString();
            }
            return "3天前";
        }
        if (buffer.length() == 0) {
            buffer.append(formatDate(createCal.getTimeInMillis(), "MM月dd日 HH:mm"));
        }

        String timeStr = buffer.toString();
        return timeStr;
    }
    // yyyy-MM-dd hh:mm:ss 12小时制
    // yyyy-MM-dd HH:mm:ss 24小时制

    public static final String TYPE_01 = "yyyy-MM-dd HH:mm:ss";

    public static final String TYPE_02 = "yyyy-MM-dd";

    public static final String TYPE_03 = "HH:mm:ss";

    public static final String TYPE_04 = "yyyy年MM月dd日";

    public static String formatDate(long time, String format) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return new SimpleDateFormat(format).format(cal.getTime());
    }

    public static String formatDate(String longStr, String format) {
        try {
            return formatDate(Long.parseLong(longStr), format);
        } catch (Exception e) {
        }
        return "";
    }

    public static long formatStr(String timeStr, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(timeStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}

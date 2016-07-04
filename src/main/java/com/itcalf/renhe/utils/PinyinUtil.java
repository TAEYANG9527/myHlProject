package com.itcalf.renhe.utils;

import android.text.TextUtils;

import com.itcalf.renhe.Constants;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PinyinUtil {

    /**
     * 获取汉字串拼音首字母，英文字符不变
     *
     * @param chinese 汉字串
     * @return 汉语拼音首字母 eg：输入 王宁 ，返回 wn
     */
    public static String cn2FirstSpell(String chinese) {
        if (TextUtils.isEmpty(chinese))
            return "#";
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < arr.length; i++) {
            if ((String.valueOf(arr[i]).matches("[\\u4E00-\\u9FA5]+"))) {
                try {
                    String[] _t = PinyinHelper.toHanyuPinyinStringArray(arr[i], hanYuPinOutputFormat);
                    if (_t != null && _t.length > 0) {
                        pybf.append(_t[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else if (((int) arr[i] >= 65 && (int) arr[i] <= 90) || ((int) arr[i] >= 97 && (int) arr[i] <= 122)) {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 获取汉字串拼音首字母，英文字符不变(不排除非法字符)
     *
     * @param chinese 汉字串
     * @return 汉语拼音首字母
     */
    public static String cn2FirstSpellWithout(String chinese) {
        if (TextUtils.isEmpty(chinese))
            return "#";
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < arr.length; i++) {
            boolean ishanyu = String.valueOf(arr[0]).matches("[\\u4E00-\\u9FA5]+");//判断第一个字符是不是汉字或拼音
            boolean iszimu = ((int) arr[0] >= 65 && (int) arr[0] <= 90) || ((int) arr[0] >= 97 && (int) arr[0] <= 122);
            if (!ishanyu && !iszimu) {
                pybf.append("#");
            } else {
                if ((String.valueOf(arr[i]).matches("[\\u4E00-\\u9FA5]+"))) {
                    try {
                        String[] _t = PinyinHelper.toHanyuPinyinStringArray(arr[i], hanYuPinOutputFormat);
                        if (_t != null) {
                            pybf.append(_t[0].charAt(0));
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else if (((int) arr[i] >= 65 && (int) arr[i] <= 90) || ((int) arr[i] >= 97 && (int) arr[i] <= 122)) {
                    pybf.append(arr[i]);
                }
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 获取汉字串拼音首字母，英文字符不变,标点，空格等保持不变
     *
     * @param chinese 汉字串
     * @return 汉语拼音首字母（转化成大写）
     */
    public static String cn2FirstSpellUppercase(String chinese) {
        if (TextUtils.isEmpty(chinese))
            return "#";
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < arr.length; i++) {
            boolean ishanyu = String.valueOf(arr[i]).matches("[\\u4E00-\\u9FA5]+");
            boolean iszimu = ((int) arr[i] >= 65 && (int) arr[i] <= 90) || ((int) arr[i] >= 97 && (int) arr[i] <= 122);
            //判断第一个字符是不是汉字或拼音
            if (!ishanyu && !iszimu) {
                pybf.append(arr[i]);
            } else {
                if ((String.valueOf(arr[i]).matches("[\\u4E00-\\u9FA5]+"))) {
                    try {
                        String[] _t = PinyinHelper.toHanyuPinyinStringArray(arr[i], hanYuPinOutputFormat);
                        if (_t != null) {
                            pybf.append(_t[0].charAt(0));
                        }
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else if (((int) arr[i] >= 65 && (int) arr[i] <= 90) || ((int) arr[i] >= 97 && (int) arr[i] <= 122)) {
                    //转化成大写
                    if ((int) arr[i] >= 97 && (int) arr[i] <= 122) {
                        arr[i] -= 32;
                    }
                    pybf.append(arr[i]);
                }
            }
        }
        return pybf.toString();
    }

    /**
     * 获取汉字串拼音，英文字符不变
     *
     * @param chinese 汉字串
     * @return 汉语全拼 eg：输入 王宁，返回 wangning
     */
    public static String cn2Spell(String chinese) {
        if (TextUtils.isEmpty(chinese))
            return "#";
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        for (int i = 0; i < arr.length; i++) {
            if ((String.valueOf(arr[i]).matches("[\\u4E00-\\u9FA5]+"))) {
                try {
                    String[] strings = PinyinHelper.toHanyuPinyinStringArray(arr[i], hanYuPinOutputFormat);
                    if (null != strings && strings.length > 0)
                        pybf.append(strings[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else if (((int) arr[i] >= 65 && (int) arr[i] <= 90) || ((int) arr[i] >= 97 && (int) arr[i] <= 122)) {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString();
    }

    /**
     * 获取拼音集合
     *
     * @param src
     * @return Set<String>
     * @author xp
     */
    public static Set<String> getPinyin(String src) {
        if (src != null && !src.trim().equalsIgnoreCase("")) {
            char[] srcChar;
            srcChar = src.toCharArray();
            // 汉语拼音格式输出类
            HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

            // 输出设置，大小写，音标方式等
            hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

            String[][] temp = new String[src.length()][];
            for (int i = 0; i < srcChar.length; i++) {
                char c = srcChar[i];
                // 是中文或者a-z或者A-Z转换拼音(我的需求，是保留中文或者a-z或者A-Z)
                if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
                    try {
                        temp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else if (((int) c >= 65 && (int) c <= 90) || ((int) c >= 97 && (int) c <= 122)) {
                    temp[i] = new String[]{String.valueOf(srcChar[i])};
                } else {
                    temp[i] = new String[]{""};
                }
            }
            String[] pingyinArray = exchange(temp);
            Set<String> pinyinSet = new HashSet<String>();
            for (int i = 0; i < pingyinArray.length; i++) {
                pinyinSet.add(pingyinArray[i]);
            }
            return pinyinSet;
        }
        return null;
    }

    public static String[] exchange(String[][] strJaggedArray) {
        String[][] temp = doExchange(strJaggedArray);
        return temp[0];
    }

    private static String[][] doExchange(String[][] strJaggedArray) {
        int len = strJaggedArray.length;
        if (len >= 2) {
            int len1 = strJaggedArray[0].length;
            int len2 = strJaggedArray[1].length;
            int newlen = len1 * len2;
            String[] temp = new String[newlen];
            int Index = 0;
            for (int i = 0; i < len1; i++) {
                for (int j = 0; j < len2; j++) {
                    temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
                    Index++;
                }
            }
            String[][] newArray = new String[len - 1][];
            for (int i = 2; i < len; i++) {
                newArray[i - 1] = strJaggedArray[i];
            }
            newArray[0] = temp;
            return doExchange(newArray);
        } else {
            return strJaggedArray;
        }
    }

    public static void main(String[] args) {
        String str = "单雨";
    }

    /**
     * 随机获取头像背景色
     *
     * @return
     */
    public static int getAvatarbgIndex() {
        int len = Constants.AVATARBG.length;//获取数组长度给变量len
        //2、根据数组长度，使用Random随机数组的索引值
        Random random = new Random();//创建随机对象
        int index = random.nextInt(len - 1);//随机数组索引，nextInt(len-1)表示随机整数[0,(len-1)]之间的值
        return index;
    }

    public static String getAvatarName(String srcName) {
        if (!TextUtils.isEmpty(srcName)) {
            //判断name是否含中文
            if (srcName.length() == 1) {
                return srcName;
            } else {
                if (isContainChinese(srcName)) {
                    return srcName.substring(srcName.length() - 2, srcName.length());
                } else {
                    return srcName.substring(0, 2);
                }
            }
        }
        return srcName;
    }

    /**
     * 判断是否包含中文
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

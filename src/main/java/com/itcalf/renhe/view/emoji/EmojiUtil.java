package com.itcalf.renhe.view.emoji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.EditText;

import com.itcalf.renhe.R;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.itcalf.renhe.widget.emojitextview.EmotionsDB;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.ui.activity.basic.BaseActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description :表情的工具类
 * Created by Chans Renhenet
 * 2015/11/18
 */
public class EmojiUtil {

    private Context context;

    public EmojiUtil(Context context) {
        this.context = context;
    }

    /**
     * 查看已有文本中的表情
     * 显示大小 默认16dp
     *
     * @param span
     * @param teString
     * @return
     */
    public SpannableString getEmotionSpannedString(SpannableString span, String teString) {
        if (null == span) {
            span = new SpannableString(teString);
        }
        Matcher localMatcher = Pattern.compile("\\[(\\S+?)\\]").matcher(span);
        while (localMatcher.find()) {
            String key = localMatcher.group(0);
            int k = localMatcher.start();
            int m = localMatcher.end();
            byte[] data = EmotionsDB.getEmotion(key);
            if (data == null)
                continue;
            MyBitmap mb = BitmapLoader.getInstance().getImageCache().getBitmapFromMemCache(key, null);
            Bitmap b;
            if (mb != null) {
                b = mb.getBitmap();
            } else {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);
                b = org.aisen.android.common.utils.BitmapUtil.zoomBitmap(b, context.getResources().getDimensionPixelSize(R.dimen.emotion_size));
                // 添加到内存中
                BitmapLoader.getInstance().getImageCache().addBitmapToMemCache(key, null, new MyBitmap(b, key));
            }
            ImageSpan l = new ImageSpan(context, b);//, ImageSpan.ALIGN_BASELINE
            span.setSpan(l, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

    /**
     * 输入文本的过滤，根据输入替换库中的表情
     */
    public InputFilter emotionFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // 是delete直接返回
            if ("".equals(source)) {
                return null;
            }
            byte[] emotionBytes = EmotionsDB.getEmotion(source.toString());
            // 输入的表情字符存在，则替换成表情图片
            if (emotionBytes != null) {
                byte[] data = emotionBytes;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.emotion_size);
                bitmap = org.aisen.android.common.utils.BitmapUtil.zoomBitmap(bitmap, size);
                SpannableString emotionSpanned = new SpannableString(source.toString());
                ImageSpan span = new ImageSpan(context, bitmap);
                emotionSpanned.setSpan(span, 0, source.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return emotionSpanned;
            } else {
                return source;
            }
        }
    };

    /**
     * 表情键盘的点击事件
     *
     * @param emotion
     * @param mContentEdt
     */
    public void onEmotionSelected(Emotion emotion, EditText mContentEdt) {
        Editable editAble = mContentEdt.getEditableText();
        int start = mContentEdt.getSelectionStart();
        if ("[删除]".equals(emotion.getKey())) {
            //删除操作
            int iCursorEnd = mContentEdt.getSelectionEnd();
            if (iCursorEnd > 0) {
                if (iCursorEnd == start) {
                    if (isDelPng(iCursorEnd, mContentEdt)) {
                        String content = mContentEdt.getText().toString().substring(0, iCursorEnd);
                        mContentEdt.getText().delete(content.lastIndexOf("["), iCursorEnd);
                    } else {
                        mContentEdt.getText().delete(iCursorEnd - 1, iCursorEnd);
                    }
                } else {
                    mContentEdt.getText().delete(start, iCursorEnd);
                }
            }
        } else {
            editAble.insert(start, emotion.getKey());
        }
    }

    /**
     * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
     */
    public boolean isDelPng(int cursor, android.widget.EditText mEditTextContent) {
        if (null == mEditTextContent)
            return false;
        String content = mEditTextContent.getText().toString().substring(0, cursor);
        if (content.length() >= 0) {
            // 判断字符串最后一个是否是“]”
            if (content.endsWith("]")) {
                String checkStr = content.substring(content.lastIndexOf("["), content.length());
                if (null != EmotionsDB.getEmotion(checkStr)) {
                    return true;
                }
            }
        }
        return false;
    }
}

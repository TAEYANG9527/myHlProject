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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.FaceGVAdapter;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.itcalf.renhe.widget.emojitextview.Emotions;
import com.itcalf.renhe.widget.emojitextview.EmotionsDB;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.MyBitmap;
import org.aisen.android.ui.activity.basic.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description :表情帮助类
 * Created by Chans Renhenet
 * 2015/9/29
 */
public class ExpressionUtil {

    private Context context;
    private static List<Emotion> emotionsList;
    private static String EMOJI_START = "hl_";
    private static String DELETE_KTY = "[删除]";
    private static int COLUMNS = 6;
    private static int ROWS = 4;

    public ExpressionUtil(Context context) {
        this.context = context;
        initStaticFaces();
    }

    /**
     * 获取表情list
     */
    private void initStaticFaces() {
        // 获取所有表情
        Emotions emotions = EmotionsDB.getEmotions(EMOJI_START);
        if (null == emotionsList && null != emotions)
            emotionsList = emotions.getEmotions();
    }

    /**
     * 根据表情数量以及GridView设置的行数和列数计算Pager数量
     *
     * @return int
     */
    public int getPagerCount() {
        int count = emotionsList.size();
        return count % (COLUMNS * ROWS - 1) == 0 ? count / (COLUMNS * ROWS - 1) : count / (COLUMNS * ROWS - 1) + 1;
    }

    /**
     * viewPage表情底部原点位置
     *
     * @param position 滑块位置
     * @return ImageView
     */
    public ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }

    /**
     * 加载每一页的表情
     * 每个表情点击之后的操作
     *
     * @param position 第几页位置
     * @return View
     */
    public View viewPagerItem(int position, final EditText mEditTextContent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);// 表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        /**
         * 注：因为每一页末尾都有一个删除图标，所以每一页的实际表情columns *　rows　－　1; 空出最后一个位置给删除图标
         * */
        final List<Emotion> subList = new ArrayList<>();
        subList.addAll(emotionsList.subList(position * (COLUMNS * ROWS - 1),
                (COLUMNS * ROWS - 1) * (position + 1) > emotionsList.size() ? emotionsList.size()
                        : (COLUMNS * ROWS - 1) * (position + 1)));
        //末尾添加删除图标
        Emotion delete = new Emotion();
        byte[] d = EmotionsDB.getEmotion(DELETE_KTY);
        delete.setKey(DELETE_KTY);
        delete.setData(d);
        subList.add(delete);

        FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, context, gridview);
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(COLUMNS);

        if (null != mEditTextContent) {
            // 单击表情执行的操作
            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Emotion emotion = subList.get(position);
                    Editable editAble = mEditTextContent.getEditableText();
                    int start = mEditTextContent.getSelectionStart();
                    if ("[删除]".equals(emotion.getKey())) {
                        //删除操作
                        int iCursorEnd = mEditTextContent.getSelectionEnd();
                        if (iCursorEnd > 0) {
                            if (iCursorEnd == start) {
                                if (isDelPng(iCursorEnd, mEditTextContent)) {
                                    String content = mEditTextContent.getText().toString().substring(0, iCursorEnd);
                                    mEditTextContent.getText().delete(content.lastIndexOf("["), iCursorEnd);
                                } else {
                                    mEditTextContent.getText().delete(iCursorEnd - 1, iCursorEnd);
                                }
                            } else {
                                mEditTextContent.getText().delete(start, iCursorEnd);
                            }
                        }
                    } else
                        editAble.insert(start, emotion.getKey());
                }
            });
        }
        return layout;
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
     * 查看已有文本中的表情
     * 默认拿16dp
     *
     * @param context
     * @param span
     * @param teString
     * @return
     */
    public SpannableString getEmotionSpannedString(Context context, SpannableString span, String teString) {
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
     * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
     * *
     */
    public boolean isDelPng(int cursor, EditText mEditTextContent) {
        if (null == mEditTextContent)
            return false;
        String content = mEditTextContent.getText().toString().substring(0, cursor);
        if (content.length() >= 0) {
            // 判断字符串最后一个是否是“]”
            if (content.endsWith("]")) {
                String checkStr = content.substring(content.lastIndexOf("["), content.length());
                // 检查[]内字符串是否为中文表情字符串，待优化
                if (null != EmotionsDB.getEmotion(checkStr)) {
                    return true;
                }
            }
        }
        return false;
    }
}
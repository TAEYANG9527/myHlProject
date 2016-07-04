package com.itcalf.renhe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.itcalf.renhe.adapter.ContactAdapter;
import com.itcalf.renhe.utils.DensityUtil;

/**
 * description :联系人
 * Created by Chans Renhenet
 * 2015/12/23
 */
public class ContactSideBar extends View {
    private char[] l;
    private ListView list;
    private TextView mDialogText;
    private Paint paint = new Paint();

    public ContactSideBar(Context context) {
        super(context);
        init();
    }

    public ContactSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContactSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        l = new char[]{'★', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
                'W', 'X', 'Y', 'Z'};
    }

    public void setListView(ListView _list) {
        list = _list;
    }

    public void setTextView(TextView mDialogText) {
        this.mDialogText = mDialogText;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float i = event.getY();
        int idx = (int) (i / getHeight() * l.length);
        if (idx >= l.length) {
            idx = l.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            mDialogText.setVisibility(View.VISIBLE);
            mDialogText.setText("" + l[idx]);
            int position = ((ContactAdapter) list.getAdapter()).getPositionForSection(l[idx]);
            if (position == -1) {
                return true;
            }
            list.setSelection(position);
        } else {
            mDialogText.setVisibility(View.INVISIBLE);
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        try {
            int height = getHeight();// 获取对应高度
            int width = getWidth(); // 获取对应宽度
            int singleHeight = height / l.length;// 获取每一个字母的高度
            for (int i = 0; i < l.length; i++) {
                paint.setColor(Color.parseColor("#565656"));
//				paint.setTypeface(Constants.APP_TYPEFACE);
                paint.setAntiAlias(true);
                paint.setTextSize(DensityUtil.sp2px(getContext(), 13));
                // x坐标等于中间-字符串宽度的一半.
                float xPos = width / 2 - paint.measureText(String.valueOf(l[i])) / 2;
                float yPos = singleHeight * i + singleHeight;
                canvas.drawText(String.valueOf(l[i]), xPos, yPos, paint);
                paint.reset();// 重置画笔
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDraw(canvas);
    }

}

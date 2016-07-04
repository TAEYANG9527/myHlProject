package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class KeyboardLayout extends RelativeLayout {
    private static final String TAG = KeyboardLayout.class.getSimpleName();
    public static final byte KEYBOARD_STATE_SHOW = -3;
    public static final byte KEYBOARD_STATE_HIDE = -2;
    public static final byte KEYBOARD_STATE_INIT = -1;
    private boolean mHasInit;
    private boolean mHasKeybord;
    private int mHeight;
	private int keyboardHeight = 400;//默认键盘高度
    private onKybdsChangeListener mListener;

    public KeyboardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardLayout(Context context) {
        super(context);
    }

    /**
     * set keyboard state listener
     */
    public void setOnkbdStateListener(onKybdsChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasInit) {
            mHasInit = true;
            mHeight = b;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_INIT, keyboardHeight);
            }
        } else {
            mHeight = mHeight < b ? b : mHeight;// ȡ���
        }
        if (mHasInit && mHasKeybord == false && mHeight > b) {
            mHasKeybord = true;
            if (mListener != null) {
                keyboardHeight = mHeight - b;
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_SHOW, keyboardHeight);
            }
        }
        if (mHasInit && mHasKeybord && mHeight == b) {
            mHasKeybord = false;
            if (mListener != null) {
                mListener.onKeyBoardStateChange(KEYBOARD_STATE_HIDE, keyboardHeight);
            }
        }
    }

    public interface onKybdsChangeListener {
        public void onKeyBoardStateChange(int state, int keyboardHeight);
    }
}

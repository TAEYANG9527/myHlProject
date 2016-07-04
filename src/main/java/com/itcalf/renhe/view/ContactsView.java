package com.itcalf.renhe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.TextView;

import com.itcalf.renhe.R;

/**
 * Feature:自定义的联系人视图
 * Desc:联系人视图，监听触摸事件，判断当前触摸的字母
 * @author xp
 *
 */
public class ContactsView extends EditText implements OnTouchListener {

	//回调接口（通知手指按下事件）
	private TouchCallBack mCallBack;
	//字母列表视图
	private TextView mLetterTxt;

	public void initView(TextView letter, TouchCallBack callBack) {
		mLetterTxt = letter;
		this.mCallBack = callBack;
	}

	public ContactsView(Context context) {
		super(context);
	}

	public ContactsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContactsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ContactsView(Context context, TouchCallBack callBack) {
		super(context);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			mLetterTxt.setVisibility(View.GONE);
			setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.quicksearch1));
			break;
		case MotionEvent.ACTION_DOWN:
			setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.quicksearch2));
			break;
		case MotionEvent.ACTION_MOVE:
			doTouchBack(v, event);
			mLetterTxt.setVisibility(View.VISIBLE);
			break;
		}
		return false;
	}

	/**
	 * 判断当前按下的字母，并且调用回调接口，通知按下事件
	 * @param v
	 * @param event
	 */
	private void doTouchBack(View v, MotionEvent event) {
		int curY;
		int def;
		switch (getWidth()) {
		case 24://320分辨率
			curY = (int) event.getY();
			def = 9;
			while (true) {
				if (curY < (def += 14)) {
					if (curY < v.getHeight()) {
						mCallBack.onTouchCode(((char) (64 + Math.round(def / 14))) + "");
					}
					break;
				}
			}
			;
			break;
		case 18://240分辨率
			curY = (int) event.getY();
			def = 7;
			while (true) {
				if (curY < (def += 9)) {
					if (curY < v.getHeight()) {
						mCallBack.onTouchCode(((char) (64 + Math.round(def / 9))) + "");
					}
					break;
				}
			}
			;
			break;
		default:
			curY = (int) event.getY();
			def = 11;
			while (true) {
				if (curY < (def += 24)) {
					if (curY < v.getHeight()) {
						mCallBack.onTouchCode(((char) (64 + Math.round(def / 24))) + "");
					}
					break;
				}
			}
			;
			break;
		}
	}

	public interface TouchCallBack {
		void onTouchCode(String code);
	}
}

package com.itcalf.renhe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Selection;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: TextViewFixTouchConsume.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-5-9 下午3:50:53 <br>
 * @author wangning
 */
public class MultiLinesTextView extends TextView {
	boolean dontConsumeNonUrlClicks = true;
	boolean linkHit;

	private static final String ELLIPSIS = "...";

	public interface EllipsizeListener {
		void ellipsizeStateChanged(boolean ellipsized);
	}

	private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<EllipsizeListener>();

	private boolean isEllipsized;
	private boolean isStale;
	private boolean programmaticChange;
	private String fullText;
	private int maxLines = -1;
	private float lineSpacingMultiplier = 1.0f;
	private float lineAdditionalVerticalPadding = 0.0f;

	public MultiLinesTextView(Context context) {
		super(context);
	}

	public MultiLinesTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultiLinesTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		linkHit = false;
		boolean res = super.onTouchEvent(event);

		if (dontConsumeNonUrlClicks)
			return linkHit;
		return res;

	}

	public static class LocalLinkMovementMethod extends LinkMovementMethod {
		static LocalLinkMovementMethod sInstance;

		public static LocalLinkMovementMethod getInstance() {
			if (sInstance == null)
				sInstance = new LocalLinkMovementMethod();

			return sInstance;
		}

		@Override
		public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
			int action = event.getAction();

			if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
				int x = (int) event.getX();
				int y = (int) event.getY();

				x -= widget.getTotalPaddingLeft();
				y -= widget.getTotalPaddingTop();

				x += widget.getScrollX();
				y += widget.getScrollY();

				Layout layout = widget.getLayout();
				int line = layout.getLineForVertical(y);
				int off = layout.getOffsetForHorizontal(line, x);

				ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

				if (link.length != 0) {
					if (action == MotionEvent.ACTION_UP) {
						link[0].onClick(widget);
					} else if (action == MotionEvent.ACTION_DOWN) {
						Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
					}

					if (widget instanceof MultiLinesTextView) {
						((MultiLinesTextView) widget).linkHit = true;
					}
					return true;
				} else {
					Selection.removeSelection(buffer);
					Touch.onTouchEvent(widget, buffer, event);
					return false;
				}
			}
			return Touch.onTouchEvent(widget, buffer, event);
		}
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}

	public void addEllipsizeListener(EllipsizeListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		ellipsizeListeners.add(listener);
	}

	public void removeEllipsizeListener(EllipsizeListener listener) {
		ellipsizeListeners.remove(listener);
	}

	public boolean isEllipsized() {
		return isEllipsized;
	}

	@Override
	public void setMaxLines(int maxLines) {
		super.setMaxLines(maxLines);
		this.maxLines = maxLines;
		isStale = true;
	}

	public int getMaxLines() {
		return maxLines;
	}

	@Override
	public void setLineSpacing(float add, float mult) {
		this.lineAdditionalVerticalPadding = add;
		this.lineSpacingMultiplier = mult;
		super.setLineSpacing(add, mult);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		super.onTextChanged(text, start, before, after);
		if (!programmaticChange) {
			fullText = text.toString();
			isStale = true;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isStale) {
			super.setEllipsize(null);
			resetText();
		}
		super.onDraw(canvas);
	}

	private void resetText() {
		String workingText = fullText;
		boolean ellipsized = false;
		int maxLines = this.maxLines > 0 ? this.maxLines : 1;
		if (maxLines <= getLineCount()) {
			Paint paint = getPaint();
			float mw = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) * maxLines;
			if (paint.measureText(workingText) > mw) {
				int ei = (int) ((mw - paint.measureText("...")) / paint.getTextSize()) - 1;
				if (ei < workingText.length()) {
					workingText = workingText.substring(0, ei - 1) + "...";
				}
			}
			ellipsized = true;
		}
		if (!workingText.equals(getText())) {
			programmaticChange = true;
			try {
				setText(workingText);
			} finally {
				programmaticChange = false;
			}
		}
		isStale = false;
		if (ellipsized != isEllipsized) {
			isEllipsized = ellipsized;
			for (EllipsizeListener listener : ellipsizeListeners) {
				listener.ellipsizeStateChanged(ellipsized);
			}
		}
	}

	private Layout createWorkingLayout(String workingText) {
		return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
				Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
	}

	@Override
	public void setEllipsize(TruncateAt where) {
		// Ellipsize settings are not respected     } } 
	}
}

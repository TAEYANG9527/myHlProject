package com.itcalf.renhe.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.itcalf.renhe.R;

import java.util.ArrayList;

/**
 * @author 
 */
public class TitlePopup extends PopupWindow {

	//	private LinearLayout priaseLl;
	private LinearLayout commentLl;
	private LinearLayout shareLl;
	//	private TextView priase;
	private TextView comment;
	private TextView share;
	private View shareSeperateView;
	private Context mContext;

	protected final int LIST_PADDING = 10;

	private Rect mRect = new Rect();

	private final int[] mLocation = new int[2];

	private int mScreenWidth, mScreenHeight;

	private boolean mIsDirty;

	private int popupGravity = Gravity.NO_GRAVITY;

	private OnItemOnClickListener mItemOnClickListener;

	private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();

	public TitlePopup(Context context) {
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	public TitlePopup(Context context, int width, int height) {
		this.mContext = context;

		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);

		setWidth(width);
		setHeight(height);
		setBackgroundDrawable(new BitmapDrawable());

		View view = LayoutInflater.from(mContext).inflate(R.layout.comment_popu, null);
		setContentView(view);
		//		priaseLl = (LinearLayout) view.findViewById(R.id.popu_praise_ll);
		commentLl = (LinearLayout) view.findViewById(R.id.popu_comment_ll);
		shareLl = (LinearLayout) view.findViewById(R.id.popu_share_ll);
		//		priase = (TextView) view.findViewById(R.id.popu_praise);
		comment = (TextView) view.findViewById(R.id.popu_comment);
		share = (TextView) view.findViewById(R.id.popu_share);
		shareSeperateView = (View) view.findViewById(R.id.share_seperate_view);
		//		priaseLl.setOnClickListener(onclick);
		commentLl.setOnClickListener(onclick);
		shareLl.setOnClickListener(onclick);
	}

	/**
	 */
	public void show(final View c) {
		c.getLocationOnScreen(mLocation);
		mRect.set(mLocation[0], mLocation[1], mLocation[0] + c.getWidth(), mLocation[1] + c.getHeight());
		//		priase.setText(mActionItems.get(0).mTitle);
		if (mIsDirty) {
			// populateActions();
		}
		showAtLocation(c, Gravity.NO_GRAVITY, mLocation[0] - this.getWidth() - 10,
				mLocation[1] - ((this.getHeight() - c.getHeight()) / 2));
	}

	OnClickListener onclick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
			switch (v.getId()) {
			case R.id.popu_comment_ll:
				mItemOnClickListener.onItemClick(mActionItems.get(0), 0);
				break;
			//			case R.id.popu_praise_ll:
			//				mItemOnClickListener.onItemClick(mActionItems.get(0), 0);
			//				break;
			case R.id.popu_share_ll:
				mItemOnClickListener.onItemClick(mActionItems.get(1), 1);
				break;
			}
		}

	};

	/**
	 */
	public void addAction(ActionItem action) {
		if (action != null) {
			mActionItems.add(action);
			mIsDirty = true;
		}
	}

	/**
	 */
	public void cleanAction() {
		if (mActionItems.isEmpty()) {
			mActionItems.clear();
			mIsDirty = true;
		}
	}

	/**
	 */
	public ActionItem getAction(int position) {
		if (position < 0 || position > mActionItems.size())
			return null;
		return mActionItems.get(position);
	}

	/**
	 */
	public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
		this.mItemOnClickListener = onItemOnClickListener;
	}

	/**
	 */
	public static interface OnItemOnClickListener {
		public void onItemClick(ActionItem item, int position);
	}

	public void showShareItem() {
		shareLl.setVisibility(View.VISIBLE);
		shareSeperateView.setVisibility(View.VISIBLE);
	}

	public void hideShareItem() {
		shareLl.setVisibility(View.GONE);
		shareSeperateView.setVisibility(View.GONE);
	}
}

package com.itcalf.renhe.context.more;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ToastUtil;

/**
 * @author       chan
 * @createtime   2015-1-13
 * @功能说明       我的财富
 */
public class MyWealthAct extends BaseActivity {

	private TextView gold_icons, renhe_icons;
	private Button exchange_Btn;
	private RelativeLayout use_renhe_icon_Rl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getRenheApplication().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.mywealth);

	}

	@Override
	protected void findView() {
		super.findView();
		gold_icons = (TextView) findViewById(R.id.gold_icons);
		exchange_Btn = (Button) findViewById(R.id.exchange_Btn);
		renhe_icons = (TextView) findViewById(R.id.renhe_icons);
		use_renhe_icon_Rl = (RelativeLayout) findViewById(R.id.use_renhe_icon_Rl);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "我的财富");
	}

	@Override
	protected void initListener() {
		super.initListener();

		exchange_Btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//				showPopWindow(v, 1);
				Intent intent = new Intent();
				intent.putExtra("tag", 1);
				intent.setClass(MyWealthAct.this, MyWealthPopWindow.class);
				startActivity(intent);
			}
		});
		use_renhe_icon_Rl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToastUtil.showToast(MyWealthAct.this, "使用人和币");
				//				showPopWindow(v, 2);
				Intent intent = new Intent();
				intent.putExtra("tag", 2);
				intent.setClass(MyWealthAct.this, MyWealthPopWindow.class);
				startActivity(intent);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/*
		private LinearLayout pop_ll;
		private PopupWindow popWindow;
		private ImageButton close;
		private Button clickBtn;
		private TextView textView;
	
		private void showPopWindow(View parent, int tag) {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View vPopWindow = inflater.inflate(R.layout.mywealth_popwindown, null, false);
			popWindow = new PopupWindow(vPopWindow, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
			pop_ll = (LinearLayout) vPopWindow.findViewById(R.id.mywealth_pop_ll);
			close = (ImageButton) vPopWindow.findViewById(R.id.mywealth_pop_close_btn);
			clickBtn = (Button) vPopWindow.findViewById(R.id.mywealth_pop_btn);
			textView = (TextView) vPopWindow.findViewById(R.id.mywealth_pop_tv);
			popWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
			if (tag == 1) {
				textView.setText("10金币=1人和币");
				clickBtn.setText("马上兑换");
			} else {
				textView.setText("人和币使用说明");
				clickBtn.setText("马上使用");
			}
	
			pop_ll.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popWindow.dismiss();
				}
			});
			close.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popWindow.dismiss();
				}
			});
			clickBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ToastUtil.showToast(MyWealthAct.this, "You are fooled!");
				}
			});
		}
	*/

}

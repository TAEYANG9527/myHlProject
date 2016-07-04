package com.itcalf.renhe.context.more;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ToastUtil;

public class MyWealthPopWindow extends BaseActivity {

	private ImageButton close;
	private Button clickBtn;
	private TextView textView;
	private int flag = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getRenheApplication().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.mywealth_popwindown);
	}

	@Override
	protected void findView() {
		super.findView();
		close = (ImageButton) findViewById(R.id.mywealth_pop_close_btn);
		clickBtn = (Button) findViewById(R.id.mywealth_pop_btn);
		textView = (TextView) findViewById(R.id.mywealth_pop_tv);
	}

	@Override
	protected void initData() {
		super.initData();
		Bundle b = this.getIntent().getExtras();
		if (b != null) {
			flag = b.getInt("tag");
		}
		if (flag == 1) {
			textView.setText("10金币=1人和币");
			clickBtn.setText("马上兑换");
		} else if (flag == 2) {
			textView.setText("人和币使用说明");
			clickBtn.setText("马上使用");
		} else if (flag == 3) {
			String nameString = b.getString("name");
			String info = b.getString("info");
			textView.setText(nameString + "\n" + info);
			clickBtn.setText("确定");
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		clickBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastUtil.showToast(MyWealthPopWindow.this, "You are fooled!");
			}
		});
	}

	//	public boolean dispatchKeyEvent(KeyEvent event) {
	//		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
	//			if (popWindow.isShowing()) {
	//				popWindow.dismiss();
	//			} else {
	//				finish();
	//			}
	//		}
	//		return super.dispatchKeyEvent(event);
	//	}

}

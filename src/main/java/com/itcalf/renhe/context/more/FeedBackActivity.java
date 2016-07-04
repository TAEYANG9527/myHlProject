package com.itcalf.renhe.context.more;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Feature:意见反馈界面 
 * Desc:意见反馈界面
 * @author xp
 * 
 */
public class FeedBackActivity extends BaseActivity {
	private EditText mContentEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.more_feedback);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("设置——反馈"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("设置——反馈"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		mContentEt = (EditText) findViewById(R.id.content_edt);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "意见反馈");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem sendItem = menu.findItem(R.id.item_send);
		sendItem.setVisible(true);
		sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_send) {
			String content = mContentEt.getText().toString().trim();
			if (content.length() == 0) {
				ToastUtil.showToast(FeedBackActivity.this, "反馈意见不能为空");
			} else {
				new FeedBackTask().executeOnExecutor(Executors.newCachedThreadPool(), content);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initListener() {
		super.initListener();

	}

	/**
	 * 提交人和网意见反馈信息
	 * @author xp
	 *
	 */
	class FeedBackTask extends AsyncTask<String, Void, MessageBoardOperation> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(1);
		}

		@Override
		protected MessageBoardOperation doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();

			reqParams.put("sid", getRenheApplication().getUserInfo().getSid());
			reqParams.put("adSId", getRenheApplication().getUserInfo().getAdSId());
			reqParams.put("content", params[0]);
			try {
				MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.MORE_FEEDBACK, reqParams,
						MessageBoardOperation.class, FeedBackActivity.this);
				return mb;
			} catch (Exception e) {
				System.out.println(e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(MessageBoardOperation result) {
			super.onPostExecute(result);
			removeDialog(1);
			if (result != null && result.getState() == 1) {
				ToastUtil.showToast(FeedBackActivity.this, "提交成功");
				finish();
			} else {
				ToastUtil.showErrorToast(FeedBackActivity.this, "提交失败");
			}
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
		default:
			return null;
		}
	}
}

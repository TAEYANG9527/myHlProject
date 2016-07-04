package com.itcalf.renhe.context.wukong.im;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ActivityApplyAddCircle extends BaseActivity {

	private EditText applyEt;
	private LinearLayout applyLy;

	private String imConversationId;
	private String circleJoinType;

	private FadeUitl fadeUitl;

	private final StringBuffer directJoinCircleStr = new StringBuffer(
			",1:请求成功，而且圈子为可直接加入的圈子,2:请求成功，而且圈子为需要验证才能加入，已发出加入申请,-1 很抱歉，您的权限不足！:-2:很抱歉，发生未知错误！,-3:im的群聊会话id不能为空,-4:圈子的成员数量已超过最大的限制，无法加入,-5:您加入的圈子超过的可以加入圈子总数的限制,-6:您已经是这个圈子的成员了,-7:您要加入的圈子不存在,-8:圈子不能直接加入,");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTemplate().doInActivity(this, R.layout.apply_add_circle);
	}

	@Override
	protected void findView() {
		super.findView();
		applyEt = (EditText) findViewById(R.id.apply_reason);
		applyEt.setSelection(applyEt.getText().toString().length());
		applyLy = (LinearLayout) findViewById(R.id.applyLy);
		fadeUitl = new FadeUitl(this, "请求发送中...");
	}

	@Override
	protected void initData() {
		super.initData();
		//		setTitle("加入圈子");
		setTextValue(1, "加入圈子");
		imConversationId = this.getIntent().getExtras().getString("imConversationId");
		circleJoinType = getIntent().getStringExtra("circleJoinType");
	}

	@Override
	protected void initListener() {
		super.initListener();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem submit = menu.findItem(R.id.menu_save);
		submit.setTitle("提交");
		submit.setVisible(true);
		submit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_save) {
			String porpose = applyEt.getText().toString().trim();
			if (circleJoinType != null && circleJoinType.equals("1"))
				directJoinType();
			else
				// 调接口返回
				new AsyncApplyJoinCircleTask().executeOnExecutor(Executors.newCachedThreadPool(),
						RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), imConversationId, porpose);

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 无需审批直接加入圈子
	 * */
	private void directJoinType() {
		new AsyncTask<String, Void, MessageBoardOperation>() {
			@Override
			protected MessageBoardOperation doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().directJoinCircle(params[0],
							params[1], imConversationId, ActivityApplyAddCircle.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(MessageBoardOperation result) {
				super.onPostExecute(result);
				if (result != null) {
					if (result.getState() == 1)
						applyToCircle();
					else if (result.getState() == 2)
						new AsyncApplyJoinCircleTask().executeOnExecutor(Executors.newCachedThreadPool(),
								RenheApplication.getInstance().getUserInfo().getSid(),
								RenheApplication.getInstance().getUserInfo().getAdSId(), imConversationId,
								applyEt.getText().toString().trim());
					else
						ToastUtil.showToast(ActivityApplyAddCircle.this, getResult(directJoinCircleStr, result.getState()));
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	/**
	 * 需审批加入圈子请求
	 */
	class AsyncApplyJoinCircleTask extends AsyncTask<String, Void, MessageBoardOperation> {
		@Override
		protected MessageBoardOperation doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", params[0]);
			reqParams.put("adSId", params[1]);
			reqParams.put("imConversationId", params[2]);
			reqParams.put("purpose", params[3]);
			try {
				MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.JOIN_CIRCLE, reqParams,
						MessageBoardOperation.class, null);
				return mb;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(MessageBoardOperation result) {
			super.onPostExecute(result);
			fadeUitl.removeFade(applyLy);
			if (result != null) {
				switch (result.getState()) {
				case 1:
					// 本地添加成功后，调im添加成员
					applyToCircle();
					break;
				case 2:
					ToastUtil.showToast(ActivityApplyAddCircle.this, "您已发出申请，等待验证");
					setResult(22);
					finish();
					break;
				case -1:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, getString(R.string.sorry_of_privilege));
					break;
				case -2:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, getString(R.string.sorry_of_unknow_exception));
					break;
				case -3:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "im的群聊会话id不能为空");
					break;
				case -4:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "圈子的成员数量已超过最大的限制，无法加入!");
					break;
				case -5:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "您加入的圈子超过的可以加入圈子总数的限制");
					break;
				case -6:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "您已经是这个圈子的成员了");
					break;
				case -7:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "您要加入的圈子不存在");
					break;
				case -8:
					ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "圈子为不可加入的状态，请于圈主联系");
					break;
				default:
					break;
				}
			} else {
				ToastUtil.showErrorToast(ActivityApplyAddCircle.this, getString(R.string.connect_server_error));
				return;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (-1 == NetworkUtil.hasNetworkConnection(ActivityApplyAddCircle.this)) {
				ToastUtil.showNetworkError(ActivityApplyAddCircle.this);
			}
			fadeUitl.addFade(applyLy);
		}
	}

	/**
	 * IM:加入圈子
	 * */
	private void applyToCircle() {
		Message message = IMEngine.getIMService(MessageBuilder.class)
				.buildTextMessage(RenheApplication.getInstance().getUserInfo().getName() + "加入圈子");

		int myCId = RenheApplication.getInstance().getUserInfo().getImId();
		// Long[] myConversationId = new Long[] {
		// Long.parseLong(String.valueOf(myCId)) };
		Long myConversationId = Long.parseLong(String.valueOf(myCId));

		IMEngine.getIMService(ConversationService.class).addMembers(new Callback<List<Long>>() {

			@Override
			public void onException(String arg0, String arg1) {
				ToastUtil.showErrorToast(ActivityApplyAddCircle.this, "IM申请加群失败：" + arg0 + ";reason:" + arg1);
			}

			@Override
			public void onProgress(List<Long> arg0, int arg1) {

			}

			@Override
			public void onSuccess(List<Long> arg0) {
				sendJoinCircleInfo(imConversationId);
				ToastUtil.showToast(ActivityApplyAddCircle.this, "您已成功加入");
				setResult(11);
				finish();
			}
		}, imConversationId, message, myConversationId);
	}
	/**
	 * 加入悟空圈子成功后，告知服务端
	 */
	private void sendJoinCircleInfo(final String imConversationId) {
		Map<String, Object> reqParams = new HashMap<>();
		reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
		reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
		reqParams.put("imConversationId", imConversationId);
		OkHttpClientManager.postAsyn(Constants.Http.SEND_JOIN_CIRCLE_INFO, reqParams, MessageBoardOperation.class, new OkHttpClientManager.ResultCallback() {
			@Override
			public void onBefore(Request request) {
				super.onBefore(request);
			}

			@Override
			public void onError(Request request, Exception e) {

			}

			@Override
			public void onResponse(Object response) {
				if (null != response && response instanceof MessageBoardOperation) {
					MessageBoardOperation result = (MessageBoardOperation) response;
					if (result.getState() == 1) {
						Logger.e("通知服务端加入圈子成功");
					}
				}
			}

			@Override
			public void onAfter() {
				super.onAfter();
			}
		});
	}
	private String getResult(StringBuffer strs, Integer code) {
		int index = strs.indexOf(String.valueOf("," + code + ":"));
		if (index < 0) {
			return "state:" + code;
		}
		return strs.substring(index + 2 + String.valueOf(code).length(),
				strs.indexOf(",", index + 2 + String.valueOf(code).length()));
	}
}

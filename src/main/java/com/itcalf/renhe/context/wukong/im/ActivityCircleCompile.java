package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * 设置圈子的名称, 加入方式, 公告页面
 *
 */
public class ActivityCircleCompile extends BaseActivity {
	private EditText et_content;
	private LinearLayout rg;
	private RelativeLayout rb_one, rb_two, rb_three;
	private ImageView rb_one_iv, rb_two_iv, rb_three_iv;
	private Conversation mConversation;

	private int state;
	private String circleContent = ""; // 上级页面所设置的名称，加入方式，公告
	private String content = ""; // 用于回调到上级Activity的值
	private String userConversationName = ""; // 用户在Im中的名字
	private String[] circleDetail; // 存储圈子信息
	private String imConversationId; // im会话id
	private boolean isUpdateCircle = false;
	private int countString = 0;
	private int limited = 0;
	private MenuItem countItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.activity_circle_compile);
		initGetIntent();//获取从ActivityCreatCircle传递的数据
		init();//初始化各控件
	}

	/**
	 * 获取从ActivityCreatCircle传递的数据
	 */
	private void initGetIntent() {
		state = getIntent().getIntExtra("state", 0);
		circleContent = getIntent().getStringExtra("content");
		userConversationName = getIntent().getStringExtra("userConversationName");
		mConversation = (Conversation) getIntent().getSerializableExtra("mConversation");
		circleDetail = getIntent().getStringArrayExtra("circleDetail");
		imConversationId = getIntent().getStringExtra("imConversationId");
		isUpdateCircle = getIntent().getBooleanExtra("isUpdateCircle", false);
	}

	private void init() {
		et_content = (EditText) findViewById(R.id.et_content);
		rg = (LinearLayout) findViewById(R.id.rg);
		rb_one = (RelativeLayout) findViewById(R.id.rb_one);
		rb_two = (RelativeLayout) findViewById(R.id.rb_two);
		rb_three = (RelativeLayout) findViewById(R.id.rb_three);
		rb_one_iv = (ImageView) findViewById(R.id.rb_one_iv);
		rb_two_iv = (ImageView) findViewById(R.id.rb_two_iv);
		rb_three_iv = (ImageView) findViewById(R.id.rb_three_iv);
		initTitle(state);//初始化title (圈子名称， 加入方式, 圈子公告) 字数限制
		initClickListener();//初始化各控件监听器
	}

	private void initClickListener() {
		rb_one.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rb_one_iv.setVisibility(View.VISIBLE);
				rb_two_iv.setVisibility(View.GONE);
				rb_three_iv.setVisibility(View.GONE);
				content = getString(R.string.rb_one_compile_content);
			}
		});
		rb_two.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rb_one_iv.setVisibility(View.GONE);
				rb_two_iv.setVisibility(View.VISIBLE);
				rb_three_iv.setVisibility(View.GONE);
				content = getString(R.string.rb_two_compile_content);
			}
		});
		rb_three.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rb_one_iv.setVisibility(View.GONE);
				rb_two_iv.setVisibility(View.GONE);
				rb_three_iv.setVisibility(View.VISIBLE);
				content = getString(R.string.rb_three_compile_content);
			}
		});

		if (state == 1) {
			rg.setVisibility(View.VISIBLE);//显示加入方式
			et_content.setVisibility(View.GONE);//隐藏编辑圈子名称 和 公告 控件
			content = mConversation != null ? circleDetail[state] : circleContent;
			if (!TextUtils.isEmpty(content) && content.length() > 0) {
				cricleJurisdiction(content);
			}
		} else {
			if(!getString(R.string.circleName).equals(circleContent)) {
				et_content.setText(mConversation != null ? circleDetail[state] : circleContent);
			}
			et_content.setSelection(et_content.getText().toString().length());

			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					InputMethodManager inputManager = (InputMethodManager) et_content.getContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(et_content, 0);//显示软件盘
					timer.cancel();
				}
			}, 500);
		}

		et_content.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (count > limited) {
					return;
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				countString = limited - s.length();
				countItem.setTitle(countString + "");
			}
		});
	}

	/**
	 *初始化title (圈子名称， 加入方式, 圈子公告) 字数限制
	 */
	private void initTitle(int state) {
		switch (state) {
			case 0:
				setTextValue(1, getString(R.string.title_zero));
				et_content.setSingleLine(true);
				break;
			case 1:
				setTextValue(1, getString(R.string.title_one));
				break;
			case 2:
				setTextValue(1, getString(R.string.title_two));
				break;
			default:
				break;
		}
		//设置 限制字数
		if (state == 0) {
			limited = TextSize.getInstance().getCircleTitleSize();
			if (limited == 0) {
				limited = Constants.CIRCLETITLE;//圈子名称字数限制 20
			}
		} else {
			limited = TextSize.getInstance().getCircleDescriptionSize();
			if (limited == 0) {
				limited = Constants.CIRCLELIMITED;//公告字数限制 50
			}
		}

		if (!TextUtils.isEmpty(circleContent) && circleContent.length() > 0) {
			countString = circleContent.equals(getString(R.string.circleName))?limited:(limited - circleContent.length());
		} else {
			countString = limited;
		}
		//设置文本输入款的最大输入字数
		et_content.setFilters(new InputFilter[] { new InputFilter.LengthFilter(limited) });
	}

	private void cricleJurisdiction(String content) {
		if (content.equals(getString(R.string.rb_one_compile_content))) {
			rb_one.performClick();
		} else if (content.equals(getString(R.string.rb_two_compile_content))) {
			rb_two.performClick();
		} else if (content.equals(getString(R.string.rb_three_compile_content))) {
			rb_three.performClick();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem addfriendsItem = menu.findItem(R.id.menu_save);//actionbar确定按钮
		addfriendsItem.setVisible(true);//显示确定按钮
		countItem = menu.findItem(R.id.item_count);//显示字数限制文本
		if (state == 1) {
			countItem.setVisible(false);
		} else {
			countItem.setVisible(true);
		}
		countItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		countItem.setTitle("" + countString);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {
		if (menu.getItemId() == R.id.menu_save) {
			if (NetworkUtil.hasNetworkConnection(ActivityCircleCompile.this) != -1) {//代表网络良好
				if (isUpdateCircle) {
					String s = state == 1 ? content : et_content.getText().toString();
					if (!s.equals(circleDetail[state])) {
						if (state == 0 && et_content.getText().toString().length() <= 20) {
							circleDetail[state] = state == 1 ? content : et_content.getText().toString();
							updateCircle();
						} else if (et_content.getText().toString().length() <= 50) {
							circleDetail[state] = state == 1 ? content : et_content.getText().toString();
							updateCircle();
						} else {
							ToastUtil.showToast(ActivityCircleCompile.this, state == 0 ? "圈子名称长度不能超过20个字符" : "圈子公告长度不能超过50个字符");
						}
					} else
						ToastUtil.showToast(ActivityCircleCompile.this, getString(R.string.contentNoChange));
				} else {
					content = state != 1 ? et_content.getText().toString() : content;
					Intent intent = new Intent();
					intent.putExtra("content", content);
					setResult(state, intent);
					ActivityCircleCompile.this.finish();
				}
			} else {
				ToastUtil.showToast(ActivityCircleCompile.this, getString(R.string.net_error));
			}
		}
		return super.onOptionsItemSelected(menu);
	}

	/**
	 * 更新圈子信息
	 * */
	private void updateCircle() {

		//通知wukong
		mConversation.updateExtension("joinType", cricleJurisdiction2(circleDetail[1]) + "");
		//通知服务器
		new AsyncTask<String, Void, MessageBoardOperation>() {
			@Override
			protected MessageBoardOperation doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().updateCircle(params[0],
							params[1], imConversationId, cricleJurisdiction2(circleDetail[1]), circleDetail[0], circleDetail[2],
							ActivityCircleCompile.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(MessageBoardOperation result) {
				if (result != null && result.getState() == 1) {
					imUpdateTitle();
				} else {
					ToastUtil.showToast(ActivityCircleCompile.this, R.string.update_avatar_fail);
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	/**
	 * IM 更新标题
	 **/
	private void imUpdateTitle() {
		String content = "";
		if (state == 0) {
			content = userConversationName + "更换了圈子标题 : " + circleDetail[0];
		} else if (state == 1) {
			content = userConversationName + "修改了圈子加入权限 : " + circleDetail[1];
		} else {
			content = userConversationName + "更换了圈子公告 : " + circleDetail[2];
		}

		Message message = null;
		message = IMEngine.getIMService(MessageBuilder.class).buildTextMessage(content);

		mConversation.updateTitle(circleDetail[0], message, new Callback<Void>() {
			@Override
			public void onException(String arg0, String arg1) {
				Toast.makeText(ActivityCircleCompile.this, "圈子标题更换失败.code:" + arg0 + " reason:" + arg1, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onProgress(Void arg0, int arg1) {
				imUpdatePrivateExtensaion();
			}

			@Override
			public void onSuccess(Void arg0) {
				imUpdatePrivateExtensaion();
			}
		});
	}

	/**
	 * IM更新扩展消息
	 * */
	private void imUpdatePrivateExtensaion() {
		mConversation.updateExtension("circleId", circleDetail[3]);
		mConversation.updateExtension("note", circleDetail[2]);
		mConversation.updateExtension("joinType", String.valueOf(cricleJurisdiction2(circleDetail[1])));
		//		RenheIMUtil.dismissProgressDialog();
		Intent intent = new Intent();
		intent.putExtra("content", circleDetail[state]);
		setResult(state, intent);
		ActivityCircleCompile.this.finish();
	}

	private int cricleJurisdiction2(String content) {
		if (content.equals("所有人可以加入")) {
			return 1;
		} else if (content.equals("需要审批才可以加入")) {
			return 2;
		} else if (content.equals("所有人都不可以加入")) {
			return 3;
		}
		return 1;
	}
}

package com.itcalf.renhe.context.auth;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ToastUtil;

public class NameAuthActivity extends BaseActivity {

	private FragmentManager mFragmanager;
	private NameAuthFragment mNameAuthStepOne, mNameAuthStepTwo;
	private NameAuthStepThree mNameAuthStepThree;
	private int currStep = 0;
	public String name;
	public String personalId;
	public String photoFile;
	public final double defaultFee = 3;
	public double authFee;
	public int authStatus = -1; //-1未认证 0认证中1已认证
	public boolean iscanBack = true;//是否可退回上一步
	private NameAuthResReceiver nameAuthResReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.search_contacts_fragment);
		setTextValue(getString(R.string.nameauth));
		mFragmanager = getFragmentManager();
		Intent intent = getIntent();
		authStatus = intent.getIntExtra("realNameStatus", -1);
		changeStep(1);

		nameAuthResReceiver = new NameAuthResReceiver();
		registerReceiver(nameAuthResReceiver, new IntentFilter(Constants.BroadCastAction.ACTION_NAMEAUTHRES));
	}

	public void changeStep(int stepnum) {
		if (stepnum == currStep)
			return;
		switch (stepnum) {
		case 1:
			if (mNameAuthStepOne == null) {
				mNameAuthStepOne = new NameAuthStepOne();
			}
			changeFragment(mNameAuthStepOne, stepnum);
			break;
		case 2:
			if (mNameAuthStepTwo == null) {
				mNameAuthStepTwo = new NameAuthStepTwo();
			}
			changeFragment(mNameAuthStepTwo, stepnum);
			break;
		case 3:
			if (mNameAuthStepThree == null) {
				mNameAuthStepThree = new NameAuthStepThree();
			}
			changeFragment(mNameAuthStepThree, stepnum);
			break;
		}
	}

	private void changeFragment(Fragment fragment, int stepnum) {
		currStep = stepnum;
		FragmentTransaction fragtran = mFragmanager.beginTransaction();
		//		if(fragment.isAdded()){
		//			fragtran.hide()
		//		}
		fragtran.replace(R.id.searchFragment, fragment);
		fragtran.commitAllowingStateLoss();
		//		fragtran.commit();
	}

	@Override
	public void onBackPressed() {

		if (!iscanBack)
			return;

		if (currStep > 1) {
			changeStep(currStep - 1);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {

			if (!iscanBack)
				return true;

			if (currStep > 1) {
				changeStep(currStep - 1);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(nameAuthResReceiver);
	}

	/**
	 * 提交认证成功
	 */
	public void dealAuthResult(int authstatus) {
		if (authStatus == authstatus)
			return;
		authStatus = authstatus;
		changeStep(1);
		Intent intent = new Intent(Constants.BroadCastAction.ACTION_NAMEAUTHSTATUS);
		intent.putExtra("realNameStatus", authStatus);
		sendBroadcast(intent);
	}

	/**
	 * 处理认证接口错误码
	 */
	public void dealAuthError(int state) {
		switch (state) {
		case -3:
			ToastUtil.showToast(this, "该会员已经通过实名认证");
			repeatRealName();
			break;
		case -4:
			ToastUtil.showToast(this, "没有免费认证特权");
			changeStep(1);
			break;
		case -5:
			ToastUtil.showToast(this, "身份证号码格式错误");
			changeStep(1);
			break;
		case -6:
			ToastUtil.showToast(this, "该身份证已经认证");
			changeStep(1);
			break;
		default:
			ToastUtil.showToast(this, "认证失败");
			changeStep(1);
			break;
		}
	}

	/**
	 * 已经实名认证过
	 * */
	public void repeatRealName() {
		Intent intent = new Intent(Constants.BroadCastAction.ACTION_NAMEAUTHSTATUS);
		intent.putExtra("realNameStatus", 1);
		sendBroadcast(intent);
		finish();
	}

	/**
	 *启动认证页面 
	 *@param realNameStatus 实名认证状态
	 **/
	public static void launch(Activity activity, int realNameStatus) {
		Intent intent = new Intent(activity, NameAuthActivity.class);
		intent.putExtra("realNameStatus", realNameStatus);
		activity.startActivity(intent);
		activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	/**
	 * 接收审核结果推送
	 * */
	class NameAuthResReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Constants.BroadCastAction.ACTION_NAMEAUTHRES)) {
				int status = intent.getIntExtra("realNameRes", -2);
				if (status != -2) {
					authStatus = status == 1 ? 1 : -1;
					changeStep(1);
				}
			}
		}
	}

}

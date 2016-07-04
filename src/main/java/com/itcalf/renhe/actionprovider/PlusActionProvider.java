package com.itcalf.renhe.actionprovider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.relationship.AdvancedSearchIndexActivityTwo;
import com.itcalf.renhe.context.wukong.im.ActivityCreatCircle;
import com.itcalf.renhe.context.wukong.im.ActivitySearchCircle;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * @author
 *
 */
public class PlusActionProvider extends ActionProvider {
	private Context context;

	public PlusActionProvider(Context context) {
		super(context);
		this.context = context;
	}

	@Override
	public View onCreateActionView() {
		return null;
	}

	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {

		subMenu.clear();
		subMenu.add(context.getString(R.string.dialogue_1)).setIcon(R.drawable.icon_invite_2x)
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						MobclickAgent.onEvent(context, "add_new_friend");
						context.startActivity(new Intent(context, AdvancedSearchIndexActivityTwo.class));
						((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
						return true;
					}
				});
		subMenu.add(context.getString(R.string.dialogue_2)).setIcon(R.drawable.icon_create_2x)
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						RenheIMUtil.showProgressDialog(context, R.string.conversation_creating);
						if (NetworkUtil.hasNetworkConnection(context) != -1)
							checkCircleCreationPrivilege((Activity) context);
						else
							ToastUtil.showToast(context, R.string.net_error);
						MobclickAgent.onEvent(context, "create_circle");
						return true;
					}
				});
		subMenu.add(context.getString(R.string.dialogue_3)).setIcon(R.drawable.icon_join_2x)
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (RenheApplication.getInstance().getUserInfo() != null
								&& RenheApplication.getInstance().getUserInfo().getImId() > 0)
							context.startActivity(new Intent(context, ActivitySearchCircle.class));
						((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
						MobclickAgent.onEvent(context, "join_circle");
						return true;
					}
				});
		super.onPrepareSubMenu(subMenu);
	}

	/**
	 * 检测是否有权限创建圈子
	 */
	public static void checkCircleCreationPrivilege(final Activity context) {
		new AsyncTask<String, Void, MessageBoardOperation>() {
			@Override
			protected MessageBoardOperation doInBackground(String... params) {
				try {
					return ((RenheApplication) context.getApplicationContext()).getMessageBoardCommand()
							.checkCircleCreationPrivilege(params[0], params[1], context);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(MessageBoardOperation result) {
				super.onPostExecute(result);
//				RenheIMUtil.dismissProgressDialog();
				if (result != null) {
					if (result.getState() == 1) {
						context.startActivity(new Intent(context, ActivityCreatCircle.class));
						((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					} else
						ToastUtil.showToast(context, result.getState() == -1 ? R.string.lack_of_privilege
								: result.getState() == -2 ? R.string.unkown_error : R.string.reach_max_circle);
				} else {
					ToastUtil.showToast(context, R.string.service_exception);
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) context.getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) context.getApplicationContext()).getUserInfo().getSid());
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

}

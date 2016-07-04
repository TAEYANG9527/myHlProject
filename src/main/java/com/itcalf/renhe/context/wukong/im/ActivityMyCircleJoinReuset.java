package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.MyCircleRequestAdapter;
import com.itcalf.renhe.bean.CircleJoinINfo;
import com.itcalf.renhe.bean.CircleJoinRequestListInfo;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.MyListView;
import com.itcalf.renhe.view.ScrollBottomScrollView;
import com.itcalf.renhe.view.ScrollBottomScrollView.ScrollBottomListener;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ActivityMyCircleJoinReuset extends BaseActivity {
	private MyListView list;
	private Context context;
	private MyCircleRequestAdapter adapter;
	private ScrollBottomScrollView scroll;
	private View footerView; // 底部加载更多

	private int numPage = 1; // 用户分页加载
	private boolean isMaxPageSize = false; // 是否已加载到最后一条数据
	private ArrayList<CircleJoinRequestListInfo> array = new ArrayList<CircleJoinRequestListInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.activity_circle_join_request);
		//		setTitle("我的申请");
		setTextValue(1, "我申请的");
		context = this;

		init();
	}

	private void init() {
		scroll = (ScrollBottomScrollView) findViewById(R.id.scroll);
		scroll.setScrollBottomListener(scrollBottomListener);

		LinearLayout ly_scroll = (LinearLayout) findViewById(R.id.ly_scroll);
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		footerView = mInflater.inflate(R.layout.pull_to_refresh_foot, null);
		footerView.setClickable(false);
		footerView.setVisibility(View.GONE);
		ly_scroll.addView(footerView);

		list = (MyListView) findViewById(R.id.list);
		adapter = new MyCircleRequestAdapter(context, array);
		list.setAdapter(adapter);
		myCircleJoinRequest(numPage);
	}

	ScrollBottomListener scrollBottomListener = new ScrollBottomListener() {
		@Override
		public void scrollBottom() {
			if (array != null && array.size() > 0 && footerView.getVisibility() == View.GONE && !isMaxPageSize) {
				numPage++;
				myCircleJoinRequest(numPage);
				footerView.setVisibility(View.VISIBLE);
			}
		}
	};

	/**
	 * 获取我所有申请圈子的 记录
	 * */
	private void myCircleJoinRequest(final int num) {
		if (num == 1)
			RenheIMUtil.showProgressDialog(this,R.string.loading);
		new AsyncTask<String, Void, CircleJoinINfo>() {
			@Override
			protected CircleJoinINfo doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().myCircleJoinRequset(params[0],
							params[1], num, 10, ActivityMyCircleJoinReuset.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(CircleJoinINfo result) {
				super.onPostExecute(result);
				if (num == 1)
					RenheIMUtil.dismissProgressDialog();
				footerView.setVisibility(View.GONE);
				if (result != null) {
					if (result.getState() == 1) {
						if (result.getCircleJoinRequestList().size() > 0) {
							array.addAll(result.getCircleJoinRequestList());
							adapter.notifyDataSetChanged();
						}
						isMaxPageSize = result.getCircleJoinRequestList().size() > 9 ? false : true;
					} else {
						ToastUtil.showToast(ActivityMyCircleJoinReuset.this, "暂无申请记录");
					}
				} else {
					ToastUtil.showToast(ActivityMyCircleJoinReuset.this,
							NetworkUtil.hasNetworkConnection(ActivityMyCircleJoinReuset.this) != -1 ? R.string.net_error
									: R.string.service_exception);
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
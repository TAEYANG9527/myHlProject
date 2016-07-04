package com.itcalf.renhe.context.room;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.mPersonalNWeiboAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.PersonalMessageBoards;
import com.itcalf.renhe.dto.Profile.UserInfo;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.XListView;
import com.itcalf.renhe.view.XListView.IXListViewListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 由档案页面点击动态按钮跳转过来
 * Feature:显示留言列表界面 
 * Description:显示留言列表界面，包括：我的客厅，朋友、同行、同城
 * 
 * @author xp
 * 
 */
public class PersonalMessageBoardActivity extends BaseActivity implements IXListViewListener {
	// 留言列表
	private XListView mWeiboListView;
	//新消息通知视图
	private RelativeLayout newMsgNoticeRl;
	private TextView newMsgNumTv;
	private SharedPreferences msp;
	private SharedPreferences.Editor mEditor;
	// 数据适配器
	private mPersonalNWeiboAdapter mAdapter;
	// 留言显示数据
	private List<Map<String, Object>> mWeiboList = new ArrayList<Map<String, Object>>();
	// 获取更新、更多数据的最大、最小值
	private long maxCreatedDate, minCreatedDate, maxLastUpdaatedDate;
	// 查看类型（eg：我的客厅，朋友、同行、同城）
	private int mType = MessageBoards.MESSAGE_TYPE_RENMAIQUAN;
	private List<MessageBoards> messageBoardsList = new ArrayList<MessageBoards>();
	public static final String ROOM_ITEM_STATE_ACTION_STRING = "room.item.statechanged_favour";
	public static final String ROOM_ITEM_STATE_ACTION_STRING_CHANGE = "room.item.statechanged_favour_change";
	public static final String ROOM_ITEM_STATE_ACTION_STRING_REPLY = "room.item.statechanged_reply";
	public static final String ROOM_ITEM_STATE_ACTION_STRING_REPLY_CHANGE = "room.item.statechanged_reply_change";
	public static final String ROOM_REFRESH_AFTER_SHIELD = "room_refresh_after_shield";//屏蔽之后
	private Handler mHandler;
	private AnimationDrawable animationDrawable;
	private RefreshListForShieldReceiver refreshListForShieldReceiver;
	private String mSenderId;

	private Context context;
	private Handler mLoadCacheHandler;
	private Runnable loadCacheRun;
	private DialogFragment dialogFragment;
	private String tag = "my_dialog";
	private static final int REQUEST_DELAY_TIME = 10;
	private static final int REQUEST_COUNT = 20;//每次向服务器获取的数据量
	private static final int SHOW_LOOK_MORE_COUNT = 5;//每次服务器至少返回多少条新留言底部才显示“查看更多”
	private RenMaiQuanManager renMaiQuanManager;

	public static final String REQUEST_TYPE_NEW = "new";
	public static final String REQUEST_TYPE_RENEW = "renew";
	public static final String REQUEST_TYPE_MORE = "more";

	public static final String RENMAIQUAN_ADD_NEWMSG = "renmaiquan_add_newmsg";//上传新留言图片的广播
	private int androidPhotoType;
	private String viewSid;
	public int PIC_MAX_WIDTH = 680;//客厅显示图片区域，最大宽高是680px
	private long lastVisibleItemTime = 0;//用来判断要显示的这条的时间是否与上条是同一天，如果是，则不显示时间
	private UserInfo userInfo;
	private FadeUitl fadeUitl;
	private RelativeLayout rootRl;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.personal_rooms_msg_list);
		refreshListForShieldReceiver = new RefreshListForShieldReceiver();
		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(ROOM_REFRESH_AFTER_SHIELD);
		registerReceiver(refreshListForShieldReceiver, intentFilter2);
		mHandler = new Handler();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("我的留言"); //统计页面
		MobclickAgent.onResume(this);
		if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0).getBoolean("fastdrag",
				false)) {
			mWeiboListView.setFastScrollEnabled(true);
		} else {
			mWeiboListView.setFastScrollEnabled(false);
		}
	}

	protected void findView() {
		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
		mWeiboListView = (XListView) findViewById(R.id.weibo_list);
		newMsgNoticeRl = (RelativeLayout) findViewById(R.id.newmsg_notify_ll);
		newMsgNumTv = (TextView) findViewById(R.id.newmsg_notify_num);
		msp = getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
		int noticeNum = msp.getInt("unreadmsg_num", 0);
		if (noticeNum > 0) {
			newMsgNumTv.setText(noticeNum + "条新消息");
		}

	}
	@Override
	protected void initData() {
		super.initData();
		viewSid = getIntent().getStringExtra("viewSid");
		if (!TextUtils.isEmpty(getIntent().getStringExtra("friendName"))) {
			setTextValue(1, getIntent().getStringExtra("friendName") + "的" + getResources().getString(R.string.record_new_dynamic));
		} else {
			setTextValue(1, "我的" + getResources().getString(R.string.record_new_dynamic));
		}
		if (getDensity() < 2) {
			PIC_MAX_WIDTH = 330;
			androidPhotoType = 2;
		} else {
			DisplayMetrics metric = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metric);
			double phoneWidth = metric.widthPixels; // 屏幕宽度（像素）
			if (phoneWidth > 640) {
				androidPhotoType = 1;
			} else {
				androidPhotoType = 2;
				PIC_MAX_WIDTH = 330;
			}
		}
		userInfo = (UserInfo) getIntent().getSerializableExtra("userinfo");
		renMaiQuanManager = new RenMaiQuanManager(this);
		fadeUitl = new FadeUitl(this, "加载中...");
		fadeUitl.addFade(rootRl);
		// 留言列表适配器
		mAdapter = new mPersonalNWeiboAdapter(this, mWeiboList, RenheApplication.getInstance().getUserInfo().getEmail(),
				mWeiboListView, mType, MessageBoards.MESSAGE_TYPE_RENMAIQUAN, PIC_MAX_WIDTH, getDensity(), userInfo);
		mWeiboListView.setAdapter(mAdapter);
		if (null != mWeiboList) {
			mWeiboList.clear();
			mAdapter.notifyDataSetChanged();
		}
		//		showDialog(1);
		mLoadCacheHandler = new Handler(new Callback() {

			@Override
			public boolean handleMessage(Message arg0) {
				switch (arg0.what) {
				case 1:
					mAdapter.notifyDataSetChanged();
					//					removeDialog(1);
					fadeUitl.removeFade(rootRl);
					break;
				}
				return false;
			}
		});
		loadCacheRun = new Runnable() {

			@Override
			public void run() {
				loadedCache(mType);
			}
		};
		mLoadCacheHandler.postDelayed(loadCacheRun, REQUEST_DELAY_TIME);//延迟500ms，防止slidemenu滑动卡顿

	}

	/**
	 * 初始化加载服务端数据
	 */
	private void initLoaded(final String type, Object minCreatedDate, Object maxCreatedDate, Object maxLastUpdaatedDate) {
		new PersonalRoomTask(this, lastVisibleItemTime, new PersonalRoomTask.IRoomBack() {

			@Override
			public void doPost(List<Map<String, Object>> newNoticeList, PersonalMessageBoards result, Long lastVisibleItemTime) {
				//				removeDialog(1);
				fadeUitl.removeFade(rootRl);
				PersonalMessageBoardActivity.this.lastVisibleItemTime = lastVisibleItemTime;
				if (null != result) {
					updateMaxMinDate(result.getMinCreatedDate(), result.getMaxCreatedDate(), result.getMaxLastUpdatedDate());
					if (!newNoticeList.isEmpty()) {
						if (!type.equals(REQUEST_TYPE_NEW)) {
							if (newNoticeList.size() >= SHOW_LOOK_MORE_COUNT) {
								mWeiboListView.showFootView();
								mWeiboListView.setPullLoadEnable(true);
							} else {
								mWeiboListView.setPullLoadEnable(false);
								ImageView imageView = new ImageView(PersonalMessageBoardActivity.this);
								imageView.setImageResource(R.drawable.end_line);
								LinearLayout layout = new LinearLayout(PersonalMessageBoardActivity.this);
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
										android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
										android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
								params.setMargins(60, 20, 60, 40);
								layout.addView(imageView, params);
								mWeiboListView.addFooterView(layout);
							}
						}
						if (type == REQUEST_TYPE_RENEW || type == REQUEST_TYPE_NEW) {
							mWeiboList.addAll(0, newNoticeList);
						} else if (type == REQUEST_TYPE_MORE) {
							mWeiboList.addAll(newNoticeList);
						}
						mAdapter.notifyDataSetChanged();

					} else {
						if (type.equals(REQUEST_TYPE_MORE)) {
							mWeiboListView.setPullLoadEnable(false);
							ImageView imageView = new ImageView(PersonalMessageBoardActivity.this);
							imageView.setImageResource(R.drawable.end_line);
							LinearLayout layout = new LinearLayout(PersonalMessageBoardActivity.this);
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
									android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
									android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
							params.setMargins(60, 20, 60, 40);
							layout.addView(imageView, params);
							mWeiboListView.addFooterView(layout);
						}
						mAdapter.notifyDataSetChanged();
					}
				} else {
					ToastUtil.showNetworkError(PersonalMessageBoardActivity.this);
				}
				//				onLoad();
				mWeiboListView.stopRefresh();
				mWeiboListView.stopLoadMore();
			}

			@Override
			public void onPre() {
			}
		}).executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getAdSId(),
				RenheApplication.getInstance().getUserInfo().getSid(), type, REQUEST_COUNT, minCreatedDate, maxCreatedDate,
				maxLastUpdaatedDate, mType, androidPhotoType, viewSid);
	}

	private void updateMaxMinDate(long minCreatedDate, long maxCreatedDate, long maxLastUpdaatedDate) {
		if (minCreatedDate > 0) {
			this.minCreatedDate = minCreatedDate;
		}
		if (maxCreatedDate > 0) {
			this.maxCreatedDate = maxCreatedDate;
		}
		if (maxLastUpdaatedDate > 0) {
			this.maxLastUpdaatedDate = maxLastUpdaatedDate;
		}
	}

	@SuppressWarnings("unused")
	private void refreshListForShield(String senderSid, String type) {
		//屏蔽你
		CacheManager.getInstance().populateData(this).deleteObject(RenheApplication.getInstance().getUserInfo().getEmail(),
				type + "");
		for (int k = 0; k < mWeiboList.size(); k++) {
			if (mWeiboList.get(k).get("sid").equals(senderSid)) {
				mWeiboList.remove(k);
			}
		}

		//		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 加载缓存数据
	 * 
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	private void loadedCache(int type) {
		initLoaded(REQUEST_TYPE_RENEW, minCreatedDate, maxCreatedDate, maxLastUpdaatedDate);

	}
	@Override
	protected void initListener() {
		super.initListener();
		mWeiboListView.setPullLoadEnable(true);
		mWeiboListView.setXListViewListener(this);
		// 监听留言列表单击事件
		mWeiboListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MobclickAgent.onEvent(context, "点击我/好友的留言中某一条留言查看详情");
				position -= 2;//添加HeaderView之后导致OnItemClickListener的position移位 
				if (mWeiboList.size() > (position) && position >= 0) {
					int type = MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD;
					String userSid = "";
					if (null != mWeiboList.get(position).get("type")) {
						type = (Integer) mWeiboList.get(position).get("type");
					}
					//						view.setClickable(true);
					Bundle bundle = new Bundle();

					//					bundle.putSerializable("result", (Serializable) mWeiboList.get(position).get("result"));
					bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
					bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
					bundle.putString("objectId", (String) mWeiboList.get(position).get("objectId"));
					Intent intent = new Intent(PersonalMessageBoardActivity.this, TwitterShowMessageBoardActivity.class);

					intent.putExtras(bundle);
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			}

		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != refreshListForShieldReceiver) {
			unregisterReceiver(refreshListForShieldReceiver);
		}
		if (null != messageBoardsList) {
			messageBoardsList.clear();
		}
		if (null != mWeiboList) {
			mWeiboList.clear();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("我的留言"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	private void onLoad() {
		mWeiboListView.showFootView();
		mWeiboListView.stopRefresh();
		mWeiboListView.stopLoadMore();
		mWeiboListView.setRefreshTime("刚刚");

	}

	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// 执行留言列表异步加载
				initLoaded(REQUEST_TYPE_NEW, minCreatedDate, maxCreatedDate, maxLastUpdaatedDate);
			}
		}, 2000);
	}

	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				initLoaded(REQUEST_TYPE_MORE, minCreatedDate, maxCreatedDate, maxLastUpdaatedDate);
			}
		}, 2000);
	}

	protected int[] count(String text, String sub) {
		int count = 0, start = 0;
		while ((start = text.indexOf(sub, start)) >= 0) {
			start += sub.length();
			count++;
		}
		if (count == 0) {
			return null;
		}
		int a[] = new int[count];
		int count2 = 0;
		while ((start = text.indexOf(sub, start)) >= 0) {
			a[count2] = start;
			start += sub.length();
			count2++;
		}
		return a;
	}

	class MessageMemberSpanClick extends ClickableSpan implements OnClickListener {
		String id;

		public MessageMemberSpanClick(String id) {
			this.id = id;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false); //去掉下划线
		}

		@Override
		public void onClick(View v) {
			if (null != id && !"".equals(id)) {
				Intent intent = new Intent(PersonalMessageBoardActivity.this, MyHomeArchivesActivity.class);
				intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, id);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		}

	}

	class RefreshListForShieldReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String senderSid = arg1.getStringExtra("senderSid");
			String type = arg1.getStringExtra("type");
			mSenderId = senderSid;
			if (!TextUtils.isEmpty(senderSid)) {
				if (!TextUtils.isEmpty(type)) {
					refreshListForShield(senderSid, type + "");
				} else {
					refreshListForShield(senderSid, mType + "");
				}
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

	private float getDensity() {
		DisplayMetrics metric = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metric);
		double width = metric.widthPixels; // 屏幕宽度（像素）
		double height = metric.heightPixels; // 屏幕高度（像素）
		float density = metric.density; // 屏幕密度
		return density;
	}
}

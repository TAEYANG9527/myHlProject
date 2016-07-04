package com.itcalf.renhe.context.wukong.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.IMInnerMsgConversationListOperation;
import com.itcalf.renhe.dto.IMInnerMsgConversationListOperation.UserInnerMsgConversation;
import com.itcalf.renhe.dto.NewInnerMessage;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.view.XListView;
import com.itcalf.renhe.view.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 会话列表Fragment的界面
 * Created by zhongqian.wzq on 2014/10/13.
 */
public class InnerMsgConversationActivity extends BaseActivity implements IXListViewListener {
	private XListView mListView;
	public static ConversationAdapter mAdapter;
	private SharedPreferences msp;
	private SharedPreferences.Editor mEditor;
	private int page = 1;
	public static final String UPDATE_INNERMSG_CONVERSATION_LIST = "update_innermsg_conversation_list_action";
	private UpdateConversationListReceiver updateConversationListReceiver;
	private static final int PAGE_COUNT = 15;
	private FadeUitl fadeUitl;
	private RelativeLayout rootRl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMyContentView(R.layout.im_innermsg_conversation_list);
	}

	@Override
	protected void findView() {
		super.findView();
		mListView = (XListView) findViewById(R.id.innermsg_list);
		rootRl = (RelativeLayout) findViewById(R.id.innermsg_listRl);
		registerForContextMenu(mListView);
		mAdapter = new ConversationAdapter(this);
		mListView.setAdapter(mAdapter);
		msp = getSharedPreferences("conversation_list", 0);
		mEditor = msp.edit();
	}

	@Override
	public void initData() {
		setTextValue(1, "站内信");
		fadeUitl = new FadeUitl(this, "加载中...");
		fadeUitl.addFade(rootRl);
		IntentFilter intentFilter = new IntentFilter(UPDATE_INNERMSG_CONVERSATION_LIST);
		updateConversationListReceiver = new UpdateConversationListReceiver();
		registerReceiver(updateConversationListReceiver, intentFilter);
		loadConversationInfo();
	}

	@Override
	protected void initListener() {
		super.initListener();
		mListView.setXListViewListener(this);
		mListView.setPullRefreshEnable(true);
		mListView.setPullLoadEnable(true);
		mListView.hideFootView();
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(InnerMsgConversationActivity.this, InnerMsgChatActivity.class);
				intent.putExtra("conversationId", mAdapter.getItem(arg2 - 1).getId() + "");
				intent.putExtra("title", mAdapter.getItem(arg2 - 1).getName());
				intent.putExtra("UserInnerMsgConversation", mAdapter.getItem(arg2 - 1));
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				//				mAdapter.getItem(arg2 - 1).setUnReadCount(0);
				final UserInnerMsgConversation userInnerMsgConversation = mAdapter.getItem(arg2 - 1);
				if (userInnerMsgConversation.getUnReadCount() > 0) {
					userInnerMsgConversation.setUnReadCount(0);
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							mAdapter.updateItem(userInnerMsgConversation);
						}
					}, 1500);
				}

			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("站内信回话列表"); //统计页面
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("站内信回话列表");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo adapterMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
		UserInnerMsgConversation conversationItem = mAdapter.getItem(adapterMenuInfo.position - 1);
		menu.setHeaderTitle("是否删除该站内信会话");
		menu.add(0, 1, Menu.NONE, R.string.conversation_delete);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		UserInnerMsgConversation conversationItem = mAdapter.getItem(menuInfo.position - 1);
		List<UserInnerMsgConversation> list = new ArrayList<UserInnerMsgConversation>();
		list.add(conversationItem);
		InnerMsgConversationActivity.mAdapter.removeItems(list);
		list.clear();
		list = null;
		return super.onContextItemSelected(item);
	}

	/**
	 * 会话列表适配器
	 */
	public class ConversationAdapter extends InnderMsgConversationListAdapter {
		public ConversationAdapter(Context context) {
			super(context, R.layout.im_item_conversation_list);
		}

		private void setTitle(TextView view, UserInnerMsgConversation userInnerMsgConversation) {
			if (null != userInnerMsgConversation) {
				String nickname = userInnerMsgConversation.getName();
				view.setText(nickname);
			} else {
				view.setText("");
			}
		}

		@Override
		public void sort() {
			Collections.sort(mList, new Comparator<UserInnerMsgConversation>() {
				@Override
				public int compare(UserInnerMsgConversation lhs, UserInnerMsgConversation rhs) {
					long lhsTime = lhs.getLastUpdatedDate();
					long rhsTime = rhs.getLastUpdatedDate();

					if (lhsTime < rhsTime)
						return 1;
					else if (lhsTime > rhsTime)
						return -1;
					else
						return 0;
				}
			});
		}

		@Override
		public InnderMsgConversationListAdapter.ViewHolder onCreateViewHolder(View convertView) {
			ConversationViewHolder viewHolder = (ConversationViewHolder) convertView.getTag();
			if (viewHolder == null) {
				viewHolder = new ConversationViewHolder();
				viewHolder.iconImageView = (ImageView) convertView.findViewById(R.id.cl_icon);//会话头像
				viewHolder.titleTextView = ((TextView) convertView.findViewById(R.id.cl_title)); //会话标题
				viewHolder.lastMsgTextView = ((TextView) convertView.findViewById(R.id.cl_lastmsg));//最新消息
				viewHolder.mentionTextView = ((TextView) convertView.findViewById(R.id.message_mention));//未读消息提醒
				viewHolder.timeTextView = ((TextView) convertView.findViewById(R.id.cl_time));//消息时间
				convertView.setTag(viewHolder);
			}
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(InnderMsgConversationListAdapter.ViewHolder holder,
				UserInnerMsgConversation conversationItem, int position) {
			ConversationViewHolder conversationViewHolder = (ConversationViewHolder) holder;
			setTitle(conversationViewHolder.titleTextView, conversationItem); //会话标题
			//			conversationViewHolder.iconImageView.setScaleType(ImageView.ScaleType.CENTER);
			if (null != conversationItem) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(conversationItem.getUserfaceUrl(), conversationViewHolder.iconImageView,
							CacheManager.options, CacheManager.animateFirstDisplayListener);
				} catch (Exception e) {
					e.printStackTrace();
				}
				String lastMessage = conversationItem.getLastUpdatedContent(); //最新消息
				long lastUpdateDate = conversationItem.getLastUpdatedDate(); //最新消息
				if (!TextUtils.isEmpty(lastMessage)) {
					conversationViewHolder.lastMsgTextView.setText(lastMessage);
				} else {
					conversationViewHolder.lastMsgTextView.setText("");
				}
				if (lastUpdateDate > 0) {
					conversationViewHolder.timeTextView.setText(DateUtil.newFormatByDayForListDisply(
							InnerMsgConversationActivity.this, new Date(conversationItem.getLastUpdatedDate())));
				} else {
					conversationViewHolder.timeTextView.setText("");
				}
				if (conversationItem.getUnReadCount() > 0) { //未读消息数提示
					conversationViewHolder.mentionTextView.setVisibility(View.VISIBLE);
					conversationViewHolder.mentionTextView.setText("" + conversationItem.getUnReadCount());
				} else {
					conversationViewHolder.mentionTextView.setText("");
					conversationViewHolder.mentionTextView.setVisibility(View.GONE);
				}

			}
		}

		class ConversationViewHolder extends InnderMsgConversationListAdapter.ViewHolder {
			/** 会话头像 */
			public ImageView iconImageView;
			/** 会话标题 */
			public TextView titleTextView;
			/** 最新消息 */
			public TextView lastMsgTextView;
			/** 消息提醒 */
			public TextView mentionTextView;
			public TextView timeTextView;
		}
	};

	private void loadConversationInfo() {
		new GetInnermsgConversationListTask(this) {
			public void doPre() {
			};

			public void doPost(IMInnerMsgConversationListOperation result) {
				fadeUitl.removeFade(rootRl);
				if (null != result && result.getState() == 1) {
					List<UserInnerMsgConversation> userInnerMsgConversations = new ArrayList<IMInnerMsgConversationListOperation.UserInnerMsgConversation>();
					UserInnerMsgConversation[] uInnerMsgConversationsArray = result.getUserConversationList();
					if (null != uInnerMsgConversationsArray && uInnerMsgConversationsArray.length > 0) {
						page++;
						if (uInnerMsgConversationsArray.length < PAGE_COUNT) {
							mListView.setPullLoadEnable(false);
							mListView.hideFootView();
						} else {
							mListView.setPullLoadEnable(true);
							mListView.showFootView();
						}
						for (UserInnerMsgConversation userInnerMsgConversation : uInnerMsgConversationsArray) {
							userInnerMsgConversations.add(userInnerMsgConversation);
						}
						mAdapter.setItems(userInnerMsgConversations);
					}
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId(), page + "", PAGE_COUNT + "");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != updateConversationListReceiver) {
			unregisterReceiver(updateConversationListReceiver);
		}
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		loadConversationInfo();
	}

	class UpdateConversationListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(UPDATE_INNERMSG_CONVERSATION_LIST)) {
				if (null != arg1.getSerializableExtra("UserInnerMsgConversation")) {
					UserInnerMsgConversation userInnerMsgConversation = (UserInnerMsgConversation) arg1
							.getSerializableExtra("UserInnerMsgConversation");
					mAdapter.updateItem(userInnerMsgConversation);
				}
			}
		}

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_save:
			//			checkNewInnerMsg();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 检查新的站内信
	 * @param userInfo
	 */
	public void checkNewInnerMsg() {
		// 检查最新站内信
		final RenheApplication renheApplication = RenheApplication.getInstance();
		Thread mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Map<String, Object> reqParams = new HashMap<String, Object>();
				reqParams.put("sid", renheApplication.getUserInfo().getSid());
				reqParams.put("adSId", renheApplication.getUserInfo().getAdSId());
				NewInnerMessage mb = null;
				try {
					if (-1 != NetworkUtil.hasNetworkConnection(InnerMsgConversationActivity.this)) {
						mb = (NewInnerMessage) HttpUtil.doHttpRequest(Constants.Http.INNERMSG_CHECKUNREADMESSAGE, reqParams,
								NewInnerMessage.class, InnerMsgConversationActivity.this);
						if (mb != null && mb.getState() == 1) {
							SharedPreferences msp = InnerMsgConversationActivity.this.getSharedPreferences(
									RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
							Editor editor = msp.edit();
							editor.putInt("newmsg_unreadmsg_num", mb.getCount());
							Intent intent = new Intent(BroadCastAction.NEWMSG_ICON_ACTION);
							intent.putExtra("newmsg_notice_num", mb.getCount());
							InnerMsgConversationActivity.this.sendBroadcast(intent);
							editor.commit();
							editor = null;
						}
					} else {
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					reqParams = null;
					mb = null;
				}
			}
		});
		mThread.start();
	}
}

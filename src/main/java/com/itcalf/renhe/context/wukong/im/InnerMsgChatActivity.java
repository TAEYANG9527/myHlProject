package com.itcalf.renhe.context.wukong.im;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.ConversationShareType;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.IMInnerMsgChatListOperation;
import com.itcalf.renhe.dto.IMInnerMsgChatListOperation.UserInnerMsgChat;
import com.itcalf.renhe.dto.IMInnerMsgConversationListOperation.UserInnerMsgConversation;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.KeyboardLayout;
import com.itcalf.renhe.view.KeyboardLayout.onKybdsChangeListener;
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

public class InnerMsgChatActivity extends BaseActivity implements OnClickListener, IXListViewListener {
	private XListView mListView;
	private MessageListAdapter mListAdapter;
	private static final String TAG = InnerMsgChatActivity.class.getSimpleName();
	private static long MINUTE = 60L * 1000L;

	private int page = 1;
	private List<UserInnerMsgChat> sysMessagesList = new ArrayList<UserInnerMsgChat>();
	/** 监听事件 */
	private FadeUitl fadeUitl;
	private KeyboardLayout rootRl;
	private boolean hasLoadFull = false;
	private String conversationId;
	private String title;
	private String receiverAdsid;
	/** 文本、图片发送按钮*/
	private Button mSendBtn;
	/**输入框*/
	private EditText mEditTextContent;
	private UserInnerMsgConversation currentUserInnerMsgConversation;
	private static final int PAGE_COUNT = 15;
	private Dialog mAlertDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置界面属性
		setMyContentView(R.layout.im_activity_chatting);
		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		handleIntent(); //处理Conversation
	}

	@Override
	public void findView() {
		rootRl = (KeyboardLayout) findViewById(R.id.rootRl);
		findViewById(R.id.btn_send_img).setVisibility(View.GONE);
		findViewById(R.id.ivPopUp).setVisibility(View.GONE);
		findViewById(R.id.btn_send_img).setVisibility(View.GONE);
		findViewById(R.id.image_face).setVisibility(View.GONE);
		mSendBtn = (Button) findViewById(R.id.btn_send);
		mSendBtn.setVisibility(View.VISIBLE);
		mSendBtn.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
		mListView = (XListView) findViewById(R.id.listview);
		mListAdapter = new MessageListAdapter();
		mListView.setAdapter(mListAdapter);

	}

	private void handleIntent() {
		fadeUitl = new FadeUitl(this, "加载中...");
		fadeUitl.addFade(rootRl);
		conversationId = getIntent().getStringExtra("conversationId");
		title = getIntent().getStringExtra("title");
		if (null != getIntent().getSerializableExtra("UserInnerMsgConversation")) {
			currentUserInnerMsgConversation = (UserInnerMsgConversation) getIntent()
					.getSerializableExtra("UserInnerMsgConversation");
		}
		setConversationTitle(title); //会话标题
		listSystemMessages(page, "");
	}

	private void refreshData() {
		mListView.setPullRefreshEnable(true);
		mListView.showHeaderView((int) getResources().getDimension(R.dimen.xlistview_refresh_need_height));
		listSystemMessages(page, "more");
	}

	@Override
	public void initListener() {

		mListView.setPullRefreshEnable(true);
		mListView.setPullLoadEnable(false);
		mListView.setXListViewListener(this);
		/*****检测软键盘是否弹起，消失*****/
		rootRl.setOnkbdStateListener(new onKybdsChangeListener() {
			@Override
			public void onKeyBoardStateChange(int state, int keyboardHeight) {
				switch (state) {
				case KeyboardLayout.KEYBOARD_STATE_HIDE:
					break;
				case KeyboardLayout.KEYBOARD_STATE_SHOW:
					//滚动到底部
					mListView.clearFocus();
					mListView.post(new Runnable() {
						@Override
						public void run() {
							mListView.setSelection(mListView.getBottom());
						}
					});
					break;
				}
			}
		});

		mListView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:
					// if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)){}
					if (mListView.getFirstVisiblePosition() == 0) {
						if (hasLoadFull)
							return;
						//滑动到顶部自动加载
						refreshData();
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				//if (firstVisibleItem + visibleItemCount == totalItemCount && !flag){
				//flag = true;
				//}else{
				//flag = false;
				//}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send: //发送文本消息
			String contString = mEditTextContent.getText().toString();
			if (contString.trim().length() > 0 && contString.trim().length() <= 5000) {
				UserInnerMsgChat message = new UserInnerMsgChat();
				message.setContent(contString);
				message.setSender(RenheApplication.getInstance().getUserInfo().getSid());
				message.setReceiver(receiverAdsid);
				message.setCreatedDate(System.currentTimeMillis());
				message.setSenderUserfaceUrl(RenheApplication.getInstance().getUserInfo().getUserface());
				mListAdapter.updateItem(message);
				mListView.setSelection(mListView.getBottom());
				mEditTextContent.setText("");
				send(message);
			} else {
				mEditTextContent.setText("");
			}
			break;
		default:
			break;
		}
	}

	private void listSystemMessages(int page, final String requestType) {

		new GetInnermsgChatListTask(this) {
			public void doPre() {
			};

			public void doPost(IMInnerMsgChatListOperation result) {
				if (null != result && result.getState() == 1) {

					UserInnerMsgChat[] userInnerMsgChatArray = result.getUserConversationList();
					if (null != userInnerMsgChatArray && userInnerMsgChatArray.length > 0) {
						if (userInnerMsgChatArray.length < PAGE_COUNT) {
							hasLoadFull = true;
							mListView.setPullRefreshEnable(false);
						}
						incrPageNum();
						if (userInnerMsgChatArray[0].getSender().equals(RenheApplication.getInstance().getUserInfo().getSid())) {
							receiverAdsid = userInnerMsgChatArray[0].getReceiver();
						} else {
							receiverAdsid = userInnerMsgChatArray[0].getSender();
						}
						for (UserInnerMsgChat userInnerMsgChat : userInnerMsgChatArray) {
							userInnerMsgChat.setStatus(UserInnerMsgChat.INNER_MSG_SENDSUCCESS);
							sysMessagesList.add(userInnerMsgChat);
						}
						mListAdapter.setItems(sysMessagesList);
						mListView.setSelection(userInnerMsgChatArray.length - 1);
					} else {
						hasLoadFull = true;
					}
				}
				mListView.stopRefresh();
				mListView.stopLoadMore();
				fadeUitl.removeFade(rootRl);

			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId(), conversationId, page + "", PAGE_COUNT + "");

	}

	private void send(UserInnerMsgChat message) {
		message.setStatus(UserInnerMsgChat.INNER_MSG_SENDING);
		new SendMsgTask(message).executeOnExecutor(Executors.newCachedThreadPool(), message.getReceiver(), message.getContent(),
				message.getContent());
	}

	class SendMsgTask extends AsyncTask<String, Void, MessageBoardOperation> {

		UserInnerMsgChat message;

		public SendMsgTask(UserInnerMsgChat message) {
			this.message = message;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected MessageBoardOperation doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", getRenheApplication().getUserInfo().getSid());
			reqParams.put("receiverSIds", params[0]);
			reqParams.put("subject", "");
			reqParams.put("content", params[2]);
			reqParams.put("systemMessage", "false");
			reqParams.put("adSId", getRenheApplication().getUserInfo().getAdSId());
			try {
				MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.INNERMSG_SEND, reqParams,
						MessageBoardOperation.class, InnerMsgChatActivity.this);
				return mb;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(MessageBoardOperation result) {
			super.onPostExecute(result);
			if (result != null && result.getState() == 1) {
				message.setStatus(UserInnerMsgChat.INNER_MSG_SENDSUCCESS);
				mListAdapter.updateItem(message);
				if (null != currentUserInnerMsgConversation) {
					currentUserInnerMsgConversation.setLastUpdatedContent(message.getContent());
					currentUserInnerMsgConversation.setLastUpdatedDate(message.getCreatedDate());
					Intent intent = new Intent(InnerMsgConversationActivity.UPDATE_INNERMSG_CONVERSATION_LIST);
					intent.putExtra("UserInnerMsgConversation", currentUserInnerMsgConversation);
					sendBroadcast(intent);
				}
			} else {
				message.setStatus(UserInnerMsgChat.INNER_MSG_OFFLINE);
				mListAdapter.updateItem(message);
				ToastUtil.showNetworkError(InnerMsgChatActivity.this);
			}
		}

	}

	private void incrPageNum() {
		page++;
	}

	private void setConversationTitle(String title) {
		setTextValue(1, title);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	/**
	 * 聊天界面消息列表适配器
	 */
	private class MessageListAdapter extends ListAdapter<UserInnerMsgChat> {
		public MessageListAdapter() {
			super(InnerMsgChatActivity.this, R.layout.im_chatlist_itemlayout_left, R.layout.im_chatlist_itemlayout_right);
		}

		/**
		 * item布局文件类型
		 * @param position
		 * @return 返回值对应于构造函数中布局文件的顺序
		 */
		@Override
		public int getItemViewType(int position) {
			UserInnerMsgChat userInnerMsgChat = getItem(position);
			if (userInnerMsgChat.getSender().toString().trim()
					.equals(RenheApplication.getInstance().getUserInfo().getSid().toString().trim())) {
				return 1; //自己发送的消息，显示chatlist_itemlayout_right布局
			} else {
				return 0; //别人发送的消息，显示chatlist_itemlayout_left布局
			}
		}

		/**
		 * 有2种item布局文件，自己发消息在右边，来消息在左边
		 * @return 布局文件种数
		 */
		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public ViewHolder onCreateViewHolder(View convertView) {
			MessageViewHolder viewHolder = (MessageViewHolder) convertView.getTag();
			if (viewHolder == null) {
				viewHolder = new MessageViewHolder();
				viewHolder.ivUserIcon = (ImageView) convertView.findViewById(R.id.iv_userhead);
				viewHolder.tvSendTime = ((TextView) convertView.findViewById(R.id.tv_sendtime));
				viewHolder.tvNickName = ((TextView) convertView.findViewById(R.id.tv_username));
				viewHolder.tvContent = ((TextView) convertView.findViewById(R.id.tv_chatcontent));
				viewHolder.ivSendFail = ((TextView) convertView.findViewById(R.id.tv_sendFail));
				viewHolder.imContent = ((ImageView) convertView.findViewById(R.id.iv_imgcontent));
				viewHolder.audioContent = ((ImageView) convertView.findViewById(R.id.iv_audiocontent));
				viewHolder.pbSendStatus = (ProgressBar) convertView.findViewById(R.id.pb_sending);
				viewHolder.audioLengthTv = (TextView) convertView.findViewById(R.id.audio_length_tv);
				viewHolder.flContent = (FrameLayout) convertView.findViewById(R.id.fl_content);
				viewHolder.audioUnReadIv = (ImageView) convertView.findViewById(R.id.read_circle_view);
				convertView.setTag(viewHolder);
			}
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, final UserInnerMsgChat userInnerMsgChat, final int position) {
			MessageViewHolder messageViewHolder = (MessageViewHolder) viewHolder;
			ImageLoader imageLoader = ImageLoader.getInstance();
			try {
				imageLoader.displayImage(userInnerMsgChat.getSenderUserfaceUrl(), messageViewHolder.ivUserIcon);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (position >= 1) {
				UserInnerMsgChat preMessage = getItem(position - 1);
				if (userInnerMsgChat.getCreatedDate() - preMessage.getCreatedDate() > 5 * MINUTE) {//新的消息据上条相差5分钟，才显示时间
					messageViewHolder.tvSendTime.setVisibility(View.VISIBLE);
					messageViewHolder.tvSendTime.setText(
							DateUtil.newFormatByDay(InnerMsgChatActivity.this, new Date(userInnerMsgChat.getCreatedDate())));
				} else {
					messageViewHolder.tvSendTime.setVisibility(View.GONE);
				}
			} else {
				messageViewHolder.tvSendTime.setVisibility(View.VISIBLE);
				messageViewHolder.tvSendTime
						.setText(DateUtil.newFormatByDay(InnerMsgChatActivity.this, new Date(userInnerMsgChat.getCreatedDate())));
			}

			messageViewHolder.tvContent.setVisibility(View.VISIBLE);
			messageViewHolder.imContent.setVisibility(View.GONE);
			messageViewHolder.audioContent.setVisibility(View.GONE);
			messageViewHolder.audioLengthTv.setVisibility(View.GONE);
			messageViewHolder.tvContent.setText(userInnerMsgChat.getContent());
			//消息发送状态
			if (userInnerMsgChat.getStatus() == UserInnerMsgChat.INNER_MSG_OFFLINE) {
				messageViewHolder.audioLengthTv.setVisibility(View.GONE);
				messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
				messageViewHolder.ivSendFail.setVisibility(View.VISIBLE);
				messageViewHolder.pbSendStatus.setVisibility(View.GONE);
				messageViewHolder.ivSendFail.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						RenheIMUtil.showAlertDialog(InnerMsgChatActivity.this, "是否重新发送", new RenheIMUtil.DialogCallback() {
							@Override
							public void onPositive() {
								send(userInnerMsgChat);
							}

							@Override
							public void onCancle() {

							}

						});
					}
				});
			} else if (userInnerMsgChat.getStatus() == UserInnerMsgChat.INNER_MSG_SENDING) {
				messageViewHolder.audioLengthTv.setVisibility(View.GONE);
				messageViewHolder.ivSendFail.setVisibility(View.GONE);
				messageViewHolder.pbSendStatus.setVisibility(View.VISIBLE);
			} else {
				messageViewHolder.ivSendFail.setVisibility(View.GONE);
				messageViewHolder.pbSendStatus.setVisibility(View.GONE);
				messageViewHolder.audioLengthTv.setVisibility(View.GONE);
				messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
			}

			messageViewHolder.audioLengthTv.setVisibility(View.GONE);
			messageViewHolder.audioUnReadIv.setVisibility(View.GONE);
			messageViewHolder.flContent.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View arg0) {
					createCopyDialog(InnerMsgChatActivity.this, userInnerMsgChat.getContent());
					return true;
				}
			});
			messageViewHolder.ivUserIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(InnerMsgChatActivity.this, MyHomeArchivesActivity.class);
					if (getItemViewType(position) == 0) {
						intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, userInnerMsgChat.getSender());
					} else {
						intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA,
								RenheApplication.getInstance().getUserInfo().getSid());
					}
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			});
		}

		class MessageViewHolder extends ViewHolder {
			/** 发送者头像 */
			ImageView ivUserIcon;
			/** 消息发送时间 */
			TextView tvSendTime;
			/** 发送者昵称 */
			TextView tvNickName;
			/** 文本消息内容 */
			TextView tvContent;
			/** 消息发送失败提醒 */
			TextView ivSendFail;
			/** 图像消息内容 */
			ImageView imContent;
			/** 语音消息内容 */
			ImageView audioContent;
			/** 发送进度条 */
			ProgressBar pbSendStatus;
			TextView audioLengthTv;
			FrameLayout flContent;
			ImageView audioUnReadIv;
		}

		@Override
		public void sort() {
			Collections.sort(mList, new Comparator<UserInnerMsgChat>() {
				@Override
				public int compare(UserInnerMsgChat lhs, UserInnerMsgChat rhs) {
					return lhs.getCreatedDate() > rhs.getCreatedDate() ? 1 : -1;
				}
			});
		}
	}

	@Override
	public void onRefresh() {
		//listSystemMessages(page, "more");
	}

	@Override
	public void onLoadMore() {

	}

	public void createCopyDialog(Context context, final String content) {
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.report_shield_dialog, null);

		mAlertDialog = new Dialog(InnerMsgChatActivity.this, R.style.TranslucentUnfullwidthWinStyle);
		mAlertDialog.setContentView(view);
		mAlertDialog.setCanceledOnTouchOutside(true);
		Window dialogWindow = mAlertDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		lp.width = (int) (dm.widthPixels * 0.8);
		dialogWindow.setAttributes(lp);
		LinearLayout reportLl = (LinearLayout) view.findViewById(R.id.reportLl);
		LinearLayout shieldLl = (LinearLayout) view.findViewById(R.id.shieldLl);
		TextView reportTv = (TextView) view.findViewById(R.id.report_tv);
		TextView shiledTv = (TextView) view.findViewById(R.id.shiledTv);
		reportTv.setText("复制");
		shiledTv.setText("转发");
		mAlertDialog.show();
		reportLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MobclickAgent.onEvent(InnerMsgChatActivity.this, "Innermsg_copy");
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
				ContentUtil.copy(content, InnerMsgChatActivity.this);
				//ContentUtil.showToast(InnerMsgChatActivity.this, "内容已经复制到剪贴板");
				ToastUtil.showToast(InnerMsgChatActivity.this, R.string.already_copy_to_plate);
			}
		});
		shieldLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MobclickAgent.onEvent(InnerMsgChatActivity.this, "Innermsg_forward");
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
				Bundle bundle = new Bundle();
				bundle.putString("toForwardContent", content);
				bundle.putInt("type", ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD);
				Intent intent = new Intent(InnerMsgChatActivity.this, ToShareWithRecentContactsActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
	}
}
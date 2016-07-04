package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ContactArrayAdapter;
import com.itcalf.renhe.adapter.FastScrollAdapterforContact;
import com.itcalf.renhe.bean.ContactItem;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.NewInnerMessage;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.itcalf.renhe.view.SearchContactsSideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author chan
 * @createtime 2014-10-20
 * @功能说明 搜索我的联系人 Desc:初始化加载我的好友联系人，以分割字母标题的方式显示，默认按字母顺序排序。
 */

public class SearchContactsActivityNew extends BaseActivity {
	// 联系人快速定位视图组件
	private SearchContactsSideBar sideBar;
	// 联系人关键字查询条件
	private EditText mKeywordEdt;
	// 联系人数量
	private TextView mContactCountTxt;
	// 联系人列表
	private ListView mContactsListView;
	// 字母显示
	private TextView mLetterTxt;
	// 没有联系人的提示
	private ProgressBar waitPb;

	private Handler mNotifHandler;
	// 带标题分割的Adapter
	private FastScrollAdapterforContact mAdapter;
	// 联系人数据
	private Map<String, List<Contact>> mContactsMap;
	private Drawable imgCloseButton;
	private Context context;
	private static final int REQUEST_DELAY_TIME = 500;
	private final static String SECTION_ID = "py";// 区分显示的是否是字母A-Z

	private int newFri_count = 0;
	// 注册广播，接受更新消息
	private int maxCid = -1;
	private FadeUitl fadeUitl;
	private RelativeLayout rootRl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTextValue(1, "我的人脉");
		setMyContentView(R.layout.search_contacts_new);
		context = (Context) this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("查看自己的联系人"); // 统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("查看自己的联系人"); // 保证 onPageEnd 在onPause 之前调用,因为
												// onPause 中会保存信息
		MobclickAgent.onPause(this);
	}
	@Override
	protected void findView() {
		super.findView();
		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
		sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);
		mKeywordEdt = (EditText) findViewById(R.id.keyword_edt);
		mContactCountTxt = (TextView) findViewById(R.id.count_txt);
		mContactsListView = (ListView) findViewById(R.id.contacts_list);
		mLetterTxt = (TextView) findViewById(R.id.letter_txt);
		imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
		waitPb = (ProgressBar) findViewById(R.id.waitPb);
	}
	@Override
	protected void initData() {
		super.initData();
		// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.BroadCastAction.REFRESH_CONTACT_RECEIVER_ACTION);
		context.registerReceiver(broadcast, intentFilter);

		mContactsMap = new TreeMap<String, List<Contact>>();
		sideBar.setTextView(mLetterTxt);

		// 获取新的朋友数量
		//					getNewFriendsCount();
		newFri_count = RenheApplication.getInstance().getNewFriendsNumb();
		// 初始化adapter
		mAdapter = new FastScrollAdapterforContact(context, R.layout.contact_list_item, R.id.title_txt);
		mContactsListView.setAdapter(mAdapter);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// // 该字母首次出现的位置
				int section = mAdapter.getPositionForTag(s.charAt(0) + "");
				if (-1 != section) {
					int positon = mAdapter.getPositionForSection(section);
					mContactsListView.setSelection(positon);
				}
			}
		});
		fadeUitl = new FadeUitl(this, "加载中...");
		fadeUitl.addFade(rootRl);
		mNotifHandler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				// 关键字搜索
				case 0:
					String keyword = (String) msg.obj;
					populateContacts(keyword);
					break;
				case 1:
					fadeUitl.removeFade(rootRl);
					populateContacts(null);
					break;
				}
				return false;
			}
		});
		mKeywordEdt.addTextChangedListener(tbxEdit_TextChanged);
		mKeywordEdt.setOnTouchListener(txtEdit_OnTouch);
		mKeywordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				return true;
			}
		});
		String userName = getIntent().getStringExtra("friendName");
		localSearch();
		if (!TextUtils.isEmpty(userName)) {
			//			setTitle(userName + "的人脉");
			setTextValue(1, userName + "的人脉");
		}
		if (imgCloseButton != null) {
			imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
		}
	}

	/**
	 * 加载自己的好友，查询本地数据库
	 */
	private void localSearch() {
		Runnable loadCacheRun = new Runnable() {
			@Override
			public void run() {
				try {
					mContactsMap.clear();
					Contact[] cts = RenheApplication.getInstance().getContactCommand()
							.getAllContact(RenheApplication.getInstance().getUserInfo().getSid());
					if (null != cts && cts.length > 0) {
						for (int i = 0; i < cts.length; i++) {
							String namePinyin = PinyinUtil.cn2FirstSpell(cts[i].getName());
							if (null != namePinyin && namePinyin.length() > 0) {
								String n = namePinyin.substring(0, 1).toUpperCase();
								List<Contact> ctList = mContactsMap.get(n);
								if (null == ctList) {
									ctList = new ArrayList<Contact>();
								}
								ctList.add(cts[i]);
								mContactsMap.put(n, ctList);
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				mNotifHandler.sendEmptyMessage(1);
			}
		};
		mNotifHandler.postDelayed(loadCacheRun, REQUEST_DELAY_TIME);
	}
	@Override
	protected void initListener() {
		super.initListener();
		mContactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ContactItem item = (ContactItem) mContactsListView.getAdapter().getItem(position);
				if (item != null && item.id != SECTION_ID && item.id != ContactArrayAdapter.IS_NEWFRIEND_ID) {
					Contact ct = mAdapter.getItem(position).getContact();
					Intent intent = new Intent(context, MyHomeArchivesActivity.class);
					intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, ct.getId());
					intent.putExtra("name", ct.getName());
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				} else {
				}
			}

		});
	}

	Handler handler = new Handler();
	Runnable run = new Runnable() {

		@Override
		public void run() {
			// populateContacts(mKeywordEdt.getText().toString());
			Message m = new Message();
			m.obj = mKeywordEdt.getText().toString();
			m.what = 0;
			mNotifHandler.sendMessage(m);
		}
	};

	/** 搜索框输入状态监听 **/
	private TextWatcher tbxEdit_TextChanged = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (!TextUtils.isEmpty(s.toString())) {
				mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.drawable.clearbtn_selected), null);
				mKeywordEdt.setCompoundDrawablePadding(1);
			} else {
				mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			handler.postDelayed(run, REQUEST_DELAY_TIME);
		}

	};

	/** 搜索框点击事件监听 **/
	private OnTouchListener txtEdit_OnTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			/** 手指离开的事件 */
			case MotionEvent.ACTION_UP:
				/** 手指抬起时候的坐标 **/
				int curX = (int) event.getX();
				if (curX > v.getWidth() - v.getPaddingRight() - imgCloseButton.getIntrinsicWidth()
						&& !TextUtils.isEmpty(mKeywordEdt.getText().toString())) {
					mKeywordEdt.setText("");
					int cacheInputType = mKeywordEdt.getInputType();
					// setInputType 可以更改 TextView 的输入方式
					mKeywordEdt.setInputType(InputType.TYPE_NULL);// EditText始终不弹出软件键盘
					mKeywordEdt.onTouchEvent(event);
					mKeywordEdt.setInputType(cacheInputType);
					return true;
				}
				break;
			}
			return false;
		}
	};

	/**
	 * 查询联系人，支持拼音简写、字母查询
	 * 
	 * @param keyword
	 */
	private void populateContacts(String keyword) {
		mAdapter.clear();
		int count = 0;
		if (null != mContactsMap && !mContactsMap.isEmpty()) {
			if (TextUtils.isEmpty(keyword)) {
				sideBar.setVisibility(View.VISIBLE);
				mContactsListView.setAdapter(mAdapter);
				final int sectionsNumber = mContactsMap.size();// + 1
				mAdapter.prepareSections(sectionsNumber);
				int sectionPosition = 0, listPosition = 0;

				// 联系人列表加载
				Set<Entry<String, List<Contact>>> set = mContactsMap.entrySet();
				Iterator<Entry<String, List<Contact>>> it = set.iterator();
				while (it.hasNext()) {
					Map.Entry<java.lang.String, java.util.List<Contact>> entry = (Map.Entry<java.lang.String, java.util.List<Contact>>) it
							.next();
					ContactItem section = new ContactItem(ContactItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
					section.sectionPosition = sectionPosition;
					section.listPosition = listPosition++;
					section.setContact(null);
					mAdapter.onSectionAdded(section, sectionPosition);
					mAdapter.add(section);

					List<Contact> contactsList = entry.getValue();
					for (int j = 0; j < contactsList.size(); j++) {
						Contact ct = contactsList.get(j);
						++count;
						ContactItem item2 = new ContactItem(ContactItem.ITEM, ct.getName(), ct.getId());
						item2.sectionPosition = sectionPosition;
						item2.listPosition = listPosition++;
						item2.setContact(ct);
						mAdapter.add(item2);
					}
					sectionPosition++;
				}
				mAdapter.notifyDataSetChanged();
			} else {
				sideBar.setVisibility(View.GONE);
				Map<String, List<Contact>> mResultsMap = new TreeMap<String, List<Contact>>();// 添加首字母
				Set<Entry<String, List<Contact>>> set = mContactsMap.entrySet();
				Iterator<Entry<String, List<Contact>>> it = set.iterator();
				while (it.hasNext()) {
					Map.Entry<java.lang.String, java.util.List<Contact>> entry = (Map.Entry<java.lang.String, java.util.List<Contact>>) it
							.next();
					List<Contact> contactsList = entry.getValue();
					List<Contact> resultList = new ArrayList<Contact>();
					if (null != contactsList && !contactsList.isEmpty()) {
						for (int j = 0; j < contactsList.size(); j++) {
							Contact ct = contactsList.get(j);
							if (null != keyword && null != ct.getName()
									&& (ct.getName().toUpperCase().startsWith(keyword.toUpperCase())
											|| PinyinUtil.cn2FirstSpell(ct.getName()).startsWith(keyword.toUpperCase()))) {
								resultList.add(ct);
								mResultsMap.put(entry.getKey(), resultList);
							}
						}
					}
				}
				mAdapter.prepareSections(mResultsMap.size());
				mContactsListView.setAdapter(mAdapter);
				// 联系人列表加载
				int sectionPosition = 0, listPosition = 0;
				Set<Entry<String, List<Contact>>> resultSet = mResultsMap.entrySet();
				Iterator<Entry<String, List<Contact>>> resultIt = resultSet.iterator();
				while (resultIt.hasNext()) {
					Map.Entry<java.lang.String, java.util.List<Contact>> entry = (Map.Entry<java.lang.String, java.util.List<Contact>>) resultIt
							.next();

					ContactItem section = new ContactItem(ContactItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
					section.sectionPosition = sectionPosition;
					section.listPosition = listPosition++;
					section.setContact(null);
					mAdapter.onSectionAdded(section, sectionPosition);
					mAdapter.add(section);

					List<Contact> contactsList = entry.getValue();
					for (int j = 0; j < contactsList.size(); j++) {
						Contact ct = contactsList.get(j);
						++count;
						ContactItem item = new ContactItem(ContactItem.ITEM, ct.getName(), ct.getId());
						item.sectionPosition = sectionPosition;
						item.listPosition = listPosition++;
						item.setContact(ct);
						mAdapter.add(item);
					}
					sectionPosition++;
				}
			}
		} else {
			if (TextUtils.isEmpty(keyword)) {
				sideBar.setVisibility(View.VISIBLE);
			} else {
				sideBar.setVisibility(View.GONE);
			}
		}
		// 获取数据库中maxcid的值
		try {
			IContactCommand contactCommand = RenheApplication.getInstance().getContactCommand();
			UserInfo mUserInfo = RenheApplication.getInstance().getUserInfo();
			// 根据用户email去查maxcid
			maxCid = contactCommand.getContactMaxCidBySid(mUserInfo.getSid());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 没有同步完成
		if (maxCid == -1) {
			waitPb.setVisibility(View.VISIBLE);
			mContactCountTxt.setText("正在同步...");
		} else {
			//			getNewContact();
			waitPb.setVisibility(View.GONE);
			if (count > 0) {
				mContactCountTxt.setText(count + "个联系人");
			} else {
				mContactCountTxt.setText("");
			}
		}

	}

	private void getNewContact() {
		//检查新联系人
		new ContactsUtil(SearchContactsActivityNew.this).SyncContacts();
	}

	/**
	 * 接收广播
	 */
	BroadcastReceiver broadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// 同步未完成，注册广播，定时接收消息
			localSearch();
		}
	};

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != broadcast) {
			context.unregisterReceiver(broadcast);
			broadcast = null;
		}
		if (null != mAdapter) {
			mAdapter.clear();
		}
		if (null != mContactsMap) {
			mContactsMap = null;
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

	/**
	 * 获取新的朋友数量
	 */
	private void getNewFriendsCount() {
		final RenheApplication renheApplication = RenheApplication.getInstance();
		Thread mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				SharedPreferences msp = SearchContactsActivityNew.this.getSharedPreferences("newfriendsCount", 0);
				int newCount = msp.getInt("sinceId", 0);
				Map<String, Object> reqParams = new HashMap<String, Object>();
				reqParams.put("sid", renheApplication.getUserInfo().getSid());
				reqParams.put("adSId", renheApplication.getUserInfo().getAdSId());
				reqParams.put("sinceId", newCount);// 上次请求获取到的最大sinceId，提醒只提醒sinceId之后的朋友数量,默认为0
				NewInnerMessage mb = null;
				try {
					if (-1 != NetworkUtil.hasNetworkConnection(SearchContactsActivityNew.this)) {
						mb = (NewInnerMessage) HttpUtil.doHttpRequest(Constants.Http.GETNEWFRIENDS_COUNT, reqParams,
								NewInnerMessage.class, SearchContactsActivityNew.this);
						if (mb != null && mb.getState() == 1) {
							newFri_count = mb.getCount();
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
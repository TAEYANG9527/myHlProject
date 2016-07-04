package com.itcalf.renhe.context.contacts;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ContactSearchAdapter;
import com.itcalf.renhe.command.IContactCommand;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.ContactList;
import com.itcalf.renhe.dto.ContactList.Member;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.po.Contact;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author chan
 * @createtime 2014-10-24
 * @功能说明 好友联系人界面（分页加载）
 */
public class SearchResultByContactsActivity extends BaseActivity {
	private ListView mContactsListView;
	private View mFooterView;
	// 联系人关键字查询条件
	private EditText mKeywordEdt;
	// 删除图片
	private Drawable imgCloseButton;

	private Handler mNotifHandler;
	private ContactSearchAdapter sAdapter;
	List<Contact> contactslist;
	// 联系人数据
	private Map<String, List<Contact>> mContactsMap = new TreeMap<String, List<Contact>>();

	private String sid = "";
	private int index = 0;

	//	private FadeUitl fadeUitl;
	//	private RelativeLayout rootRl;
	private LinearLayout loadingLL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RenheApplication.getInstance().addActivity(this);
		getTemplate().doInActivity(this, R.layout.search_contact_result);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("查看好友的联系人"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("查看好友的联系人"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		//		rootRl = (RelativeLayout)findViewById(R.id.rootRl);
		loadingLL = (LinearLayout) findViewById(R.id.loadingLL);
		mContactsListView = (ListView) findViewById(R.id.contacts_list);
		mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
		mFooterView.setVisibility(View.GONE);
		mKeywordEdt = (EditText) findViewById(R.id.keyword_edt);
		imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "联系人");

		sid = getIntent().getStringExtra("sid");
		String userName = getIntent().getStringExtra("friendName");
		//		fadeUitl = new FadeUitl(this,"加载中...");
		//		fadeUitl.addFade(rootRl);
		if (TextUtils.isEmpty(sid)) {
			localSearch();
		} else {
			//			showDialog(1);
			loadingLL.setVisibility(View.VISIBLE);
			remoteSearch(sid, index);
		}
		if (!TextUtils.isEmpty(userName)) {
			setTextValue(R.id.title_txt, userName + "的人脉");
		}
		if (imgCloseButton != null) {
			imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
		}

		contactslist = new ArrayList<Contact>();
		// 初始化adapter
		sAdapter = new ContactSearchAdapter(this, contactslist);
		mContactsListView.addFooterView(mFooterView, null, false);
		mContactsListView.setAdapter(sAdapter);

		// 搜索
		mKeywordEdt.addTextChangedListener(tbxEdit_TextChanged);
		mKeywordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				return true;
			}
		});

		mNotifHandler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					String keyword = (String) msg.obj;
					populateContacts(keyword);
					break;
				case 1:
					populateContacts("");
					break;
				case 2:
					toggleFooterView(3);
					break;
				case 3:
					toggleFooterView(2);
					break;
				}
				return false;
			}
		});
	}

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
				mFooterView.setVisibility(View.VISIBLE);
			}
			Message m = new Message();
			m.obj = mKeywordEdt.getText().toString();
			m.what = 0;
			mNotifHandler.sendMessage(m);
		}
	};

	/**
	 * 显示列表底部 1，加载中；2，查看更多；3，已经到底
	 * 
	 * @param show
	 */
	private void toggleFooterView(int show) {
		mFooterView.setVisibility(View.VISIBLE);
		switch (show) {
		case 1:
			((TextView) mFooterView.findViewById(R.id.titleTv)).setText("加载中...");
			mFooterView.findViewById(R.id.waitPb).setVisibility(View.VISIBLE);
			break;
		case 2:
			mFooterView.setEnabled(true);// 不可点
			((TextView) mFooterView.findViewById(R.id.titleTv)).setText("查看更多");
			mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
			break;
		case 3:
			mFooterView.setEnabled(false);// 不可点
			((TextView) mFooterView.findViewById(R.id.titleTv)).setText("已经到底了！");
			mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
			break;

		default:
			break;
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		mFooterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				index += Constants.FRIEND_PAGESIZE;
				toggleFooterView(1);
				remoteSearch(sid, index);
			}
		});

		mContactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < contactslist.size() && null != contactslist.get(position)) {
					Intent intent = new Intent(SearchResultByContactsActivity.this, MyHomeArchivesActivity.class);
					intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, (String) contactslist.get(position).getId());
					startActivity(intent);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			}
		});
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
	 * 查看好友的联系人，需要访问HTTP
	 * 
	 * @param sid
	 */
	private void remoteSearch(final String sid, final int index) {
		final IContactCommand contactCommand = ((RenheApplication) getApplication()).getContactCommand();

		final UserInfo userInfo = ((RenheApplication) getApplication()).getUserInfo();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// ContactList clist = contactCommand.getContactList(sid,
					// userInfo.getSid(), userInfo.getAdSId());
					ContactList clist = contactCommand.getContactListBypage(sid, userInfo.getSid(), userInfo.getAdSId(), index,
							Constants.FRIEND_PAGESIZE);
					if (1 == clist.getState()) {
						Member[] ml = clist.getMemberList();
						if (null != ml && ml.length > 0) {
							for (int i = 0; i < ml.length; i++) {
								Contact ct = new Contact();
								ct.setId(ml[i].getSid());
								ct.setName(ml[i].getName());
								ct.setJob(ml[i].getTitle());
								ct.setCompany(ml[i].getCompany());
								ct.setContactface(ml[i].getUserface());
								ct.setAccountType(ml[i].getAccountType());
								ct.setRealname(ml[i].isRealname());
								String namePinyin = PinyinUtil.cn2FirstSpell(ct.getName());
								// if (null != namePinyin
								// && namePinyin.length() > 0) {
								// String n = namePinyin.substring(0, 1)
								// .toUpperCase();
								String n = "A";
								List<Contact> ctList = mContactsMap.get(n);
								if (null == ctList) {
									ctList = new ArrayList<Contact>();
								}
								ctList.add(ct);
								mContactsMap.put(n, ctList);
							}
							// }
							// 没有到达指定的数目，说明已经没有数据
							if (ml.length < Constants.FRIEND_PAGESIZE) {
								mNotifHandler.sendEmptyMessage(2);
							} else {
								mNotifHandler.sendEmptyMessage(3);
							}
						}
					}
				} catch (Exception e) {
					System.out.println(e);
				}
				mNotifHandler.sendEmptyMessage(1);
			}
		}).start();
	}

	/**
	 * 加载自己的好友，查询本地数据库
	 */
	private void localSearch() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Contact[] cts = getRenheApplication().getContactCommand()
							.getAllContact(getRenheApplication().getUserInfo().getSid());
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
		}).start();
	}

	/**
	 * 查询联系人，支持拼音简写、字母查询
	 * 
	 * @param keyword
	 */
	private void populateContacts(String keyword) {
		// sAdapter = null;
		contactslist.clear();
		if (null != mContactsMap && !mContactsMap.isEmpty()) {

			Map<String, List<Contact>> mResultsMap = null;// 结果集

			if (!TextUtils.isEmpty(keyword)) {
				mFooterView.setVisibility(View.GONE);// 搜索字母匹配加载出来的，不去加载更多
				mResultsMap = new TreeMap<String, List<Contact>>();
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
			} else {
				mResultsMap = mContactsMap;
			}
			// 联系人列表加载
			Set<Entry<String, List<Contact>>> set = mResultsMap.entrySet();
			Iterator<Entry<String, List<Contact>>> it = set.iterator();
			while (it.hasNext()) {
				Map.Entry<java.lang.String, java.util.List<Contact>> entry = (Map.Entry<java.lang.String, java.util.List<Contact>>) it
						.next();
				List<Contact> contactsList = entry.getValue();
				for (int j = 0; j < contactsList.size(); j++) {
					Contact ct = contactsList.get(j);
					contactslist.add(ct);
				}
			}
			sAdapter.notifyDataSetChanged();
			//			fadeUitl.removeFade(rootRl);
			//			removeDialog(1);
			loadingLL.setVisibility(View.GONE);
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2 && resultCode == RESULT_OK) {

		}
	}
}

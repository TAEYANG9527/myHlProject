package com.itcalf.renhe.context.contacts;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.MessageBuilder;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.ContactArrayAdapter;
import com.itcalf.renhe.adapter.HlContactRenheMemberAdapterforContact;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContactRenheMemberItem;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.DialogUtil;
import com.itcalf.renhe.utils.DialogUtil.ShareMyDialogClickListener;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.itcalf.renhe.view.SearchContactsSideBar.OnTouchingLetterChangedListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseContactsActivity extends BaseActivity {
    @BindView(R.id.import_contact_tv)
    com.itcalf.renhe.view.TextView importContactTv;
    @BindView(R.id.import_contact_btn)
    Button importContactBtn;
    @BindView(R.id.none_contacts_ll)
    LinearLayout noneContactsLl;
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
    // private TextView contact_emptytip;
    private ProgressBar waitPb;

    private Handler mNotifHandler;
    // 带标题分割的Adapter
    private HlContactRenheMemberAdapterforContact mAdapter;
    // 联系人数据
    private Map<String, List<HlContactRenheMember>> mContactsMap;
    private Drawable imgCloseButton;
    private Context context;
    private static final int REQUEST_DELAY_TIME = 0;
    private final static String SECTION_ID = "py";// 区分显示的是否是字母A-Z

    // 注册广播，接受更新消息
    public static final String REFRESH_CONTACT_RECEIVER_ACTION = "com.renhe.refresh_contact";
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;

    private GuideDoubleTapReceiver guideDoubleTapReceiver;
    private DialogUtil dialogUtil;
    private String toForwardContent;
    private String toFrowardPic;
    private String toFrowardTitle;
    /**
     * 消息构造器
     */
    private MessageBuilder mMessageBuilder;
    private HlContactRenheMember ct;
    private static int REQUEST_CODE_TOCICLE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        getTemplate().doInActivity(this, R.layout.search_contacts_new);
    }

    @Override
    protected void findView() {
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);
        mKeywordEdt = (EditText) findViewById(R.id.keyword_edt);
        mContactCountTxt = (TextView) findViewById(R.id.count_txt);
        mContactsListView = (ListView) findViewById(R.id.contacts_list);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        waitPb = (ProgressBar) findViewById(R.id.waitPb);
        sideBar.setVisibility(View.INVISIBLE);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        msp = getSharedPreferences("conversation_list", 0);
        mEditor = msp.edit();
        // 注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_CONTACT_RECEIVER_ACTION);
        context.registerReceiver(broadcast, intentFilter);
        mContactsMap = new TreeMap<>();
        sideBar.setTextView(mLetterTxt);
        // 初始化adapter
        mAdapter = new HlContactRenheMemberAdapterforContact(context, R.layout.contact_list_item, R.id.title_txt);
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
                        // 获取新的朋友数量
                        break;
                    case 3:
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
        mMessageBuilder = IMEngine.getIMService(MessageBuilder.class);
        dialogUtil = new DialogUtil(this, new ShareMyDialogClickListener() {

            @Override
            public void onclick(int id, String msg, Conversation conversation) {

            }

            @Override
            public void onclick(int id, String msg, ConversationItem conversation) {

            }

            @Override
            public void onclick(int id, String msg, HlContactRenheMember hlContactRenheMember) {

            }

            @Override
            public void onclick(int id, String msg) {
                if (id == 1) {
                    Intent intent = new Intent();
                    intent.putExtra("contacts", ct);
                    intent.putExtra("extramsg", msg);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        localSearch();
        setTextValue(1, "选择");
        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
    }

    protected void initListener() {
        mContactsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HlContactRenheMemberItem item = (HlContactRenheMemberItem) mContactsListView.getAdapter().getItem(position);
                if (item != null && item.id != SECTION_ID && item.id != ContactArrayAdapter.IS_NEWFRIEND_ID
                        && item.id != ContactArrayAdapter.IS_CIRCLE_ID && item.id != ContactArrayAdapter.IS_CONTACT_ID) {
                    //点选联系人
                    ct = mAdapter.getItem(position).getHlContactRenheMember();
                    dialogUtil.createShareDialog(ChooseContactsActivity.this, getString(R.string.vcard_share_default_name), "取消",
                            "确定", ct.getTitle(), ct.getUserface(), ct.getName(), ct.getCompany());

                } else {
                    //点击其他如圈子
                }
            }
        });
        importContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChooseContactsActivity.this, SearchForContactsActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        guideDoubleTapReceiver = new GuideDoubleTapReceiver();
        IntentFilter intentFilter2 = new IntentFilter(TabMainFragmentActivity.CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION);
        registerReceiver(guideDoubleTapReceiver, intentFilter2);
    }

    /**
     * 加载自己的好友，查询本地数据库
     */
    private void localSearch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mContactsMap.clear();
                    List<HlContactRenheMember> hlContactRenheMemberList = new ArrayList<>();
                    hlContactRenheMemberList = HlContactsUtils.getAllHlFriendContacts(hlContactRenheMemberList);
                    if (null != hlContactRenheMemberList && !hlContactRenheMemberList.isEmpty()) {
                        for (int i = 0; i < hlContactRenheMemberList.size(); i++) {
                            if (hlContactRenheMemberList.get(i).isImValid()) {
                                String nameInitial = hlContactRenheMemberList.get(i).getInitial();
                                if (!TextUtils.isEmpty(nameInitial)) {
                                    List<HlContactRenheMember> ctList = mContactsMap.get(nameInitial);
                                    if (null == ctList) {
                                        ctList = new ArrayList<>();
                                    }
                                    ctList.add(hlContactRenheMemberList.get(i));
                                    mContactsMap.put(nameInitial, ctList);
                                }
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

    /**
     * 搜索框输入状态监听
     **/
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
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        getResources().getDrawable(R.drawable.clearbtn_selected), null);
                mKeywordEdt.setCompoundDrawablePadding(1);
            } else {
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handler.postDelayed(run, REQUEST_DELAY_TIME);
        }

    };

    /**
     * 搜索框点击事件监听
     **/
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
                final int sectionsNumber = mContactsMap.size() + 3;//添加了3个新的选项
                mContactsListView.setAdapter(mAdapter);
                mAdapter.prepareSections(sectionsNumber);
                int sectionPosition = 0, listPosition = 0;

                // 添加圈子 （列表中需要圈子就放开以下代码）
                //				HlContactRenheMemberItem circleSection = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, "圈子", ContactArrayAdapter.IS_CIRCLE_ID);
                //				circleSection.sectionPosition = sectionPosition;
                //				circleSection.listPosition = listPosition++;
                //				mAdapter.onSectionAdded(circleSection, sectionPosition);
                //				mAdapter.add(circleSection);
                //				HlContactRenheMemberItem circleItem;
                //				circleItem = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, "圈子", ContactArrayAdapter.IS_CIRCLE_ID);
                //				circleItem.sectionPosition = sectionPosition;
                //				circleItem.listPosition = listPosition++;
                //				mAdapter.add(circleItem);
                //				sectionPosition++;

                // 联系人列表加载
                Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = (Entry<String, List<HlContactRenheMember>>) it
                            .next();
                    HlContactRenheMemberItem section = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setHlContactRenheMember(null);
                    mAdapter.onSectionAdded(section, sectionPosition);
                    mAdapter.add(section);

                    List<HlContactRenheMember> contactsList = entry.getValue();
                    for (int j = 0; j < contactsList.size(); j++) {
                        HlContactRenheMember ct = contactsList.get(j);
                        ++count;
                        HlContactRenheMemberItem item2 = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, ct.getName(), ct.getId() + "");
                        item2.sectionPosition = sectionPosition;
                        item2.listPosition = listPosition++;
                        item2.setHlContactRenheMember(ct);
                        mAdapter.add(item2);
                    }
                    sectionPosition++;
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Map<String, List<HlContactRenheMember>> mResultsMap = new TreeMap<String, List<HlContactRenheMember>>();// 添加首字母
                Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = (Entry<String, List<HlContactRenheMember>>) it
                            .next();
                    List<HlContactRenheMember> contactsList = entry.getValue();
                    List<HlContactRenheMember> resultList = new ArrayList<HlContactRenheMember>();
                    if (null != contactsList && !contactsList.isEmpty()) {
                        for (int j = 0; j < contactsList.size(); j++) {
                            HlContactRenheMember ct = contactsList.get(j);
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
                Set<Entry<String, List<HlContactRenheMember>>> resultSet = mResultsMap.entrySet();
                Iterator<Entry<String, List<HlContactRenheMember>>> resultIt = resultSet.iterator();
                while (resultIt.hasNext()) {
                    Entry<String, List<HlContactRenheMember>> entry = (Entry<String, List<HlContactRenheMember>>) resultIt
                            .next();

                    HlContactRenheMemberItem section = new HlContactRenheMemberItem(HlContactRenheMemberItem.SECTION, String.valueOf(entry.getKey()), SECTION_ID);
                    section.sectionPosition = sectionPosition;
                    section.listPosition = listPosition++;
                    section.setHlContactRenheMember(null);
                    mAdapter.onSectionAdded(section, sectionPosition);
                    mAdapter.add(section);

                    List<HlContactRenheMember> contactsList = entry.getValue();
                    for (int j = 0; j < contactsList.size(); j++) {
                        HlContactRenheMember ct = contactsList.get(j);
                        ++count;
                        HlContactRenheMemberItem item = new HlContactRenheMemberItem(HlContactRenheMemberItem.ITEM, ct.getName(), ct.getId() + "");
                        item.sectionPosition = sectionPosition;
                        item.listPosition = listPosition++;
                        item.setHlContactRenheMember(ct);
                        mAdapter.add(item);
                    }
                    sectionPosition++;
                }
                // mAdapter.notifyDataSetChanged();
            }
        } else {
            if (TextUtils.isEmpty(keyword)) {
                final int sectionsNumber = 1;
                mContactsListView.setAdapter(mAdapter);
                mAdapter.prepareSections(sectionsNumber);
            }
        }
        waitPb.setVisibility(View.GONE);
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
            mContactsListView.setVisibility(View.GONE);
            noneContactsLl.setVisibility(View.VISIBLE);
            importContactTv.setText(getString(R.string.contact_to_select_is_empty));
            importContactBtn.setText(getString(R.string.contact_to_add));
        }
        mContactCountTxt.setVisibility(View.GONE);
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
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("选择联系人");
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("选择联系人"); // 统计页面
    }

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
        if (null != mAdapter) {
            mAdapter.clear();
        }
        if (null != mContactsMap) {
            mContactsMap = null;
        }
        if (null != broadcast) {
            context.unregisterReceiver(broadcast);
            broadcast = null;
        }
        if (null != guideDoubleTapReceiver) {
            context.unregisterReceiver(guideDoubleTapReceiver);
            guideDoubleTapReceiver = null;
        }
    }

    class GuideDoubleTapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(TabMainFragmentActivity.CONTACTS_ACTIONBAR_DOUBLE_TAP_ACTION)) {
                //				getListView().setSelection(getListView().getTop());
                mContactsListView.setSelection(0);
            }
        }

    }

    //	private void createConversation(final com.alibaba.wukong.im.Message message, final String msgExtra) {
    //		if(null != message){
    //			int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
    //			// 创建会话
    //			IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
    //				@Override
    //				public void onSuccess(Conversation conversation) {
    //					send(conversation, message, msgExtra);
    //				}
    //
    //				@Override
    //				public void onException(String code, String reason) {
    //					com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
    //					Toast.makeText(ToShareContactsActivity.this, R.string.send_failed, Toast.LENGTH_SHORT).show();
    //				}
    //
    //				@Override
    //				public void onProgress(Conversation data, int progress) {
    //				}
    //			}, ct.getName(), ct.getContactface(), null, convType, Long.parseLong(ct.getImId() + ""));
    //		}else{
    //			Toast.makeText(ToShareContactsActivity.this, R.string.send_failed, Toast.LENGTH_SHORT).show();
    //		}
    //	}
    //
    //	/**
    //	 * 发送消息
    //	 *
    //	 * @param message
    //	 */
    //	private void send(final Conversation mConversation, final com.alibaba.wukong.im.Message message, final String msgExtra) {
    //		if (mConversation != null && !TextUtils.isEmpty(mConversation.draftMessage())) {
    //			mConversation.updateDraftMessage("");
    //		}
    //
    //		message.sendTo(mConversation, new com.alibaba.wukong.Callback<com.alibaba.wukong.im.Message>() {
    //			@Override
    //			public void onSuccess(com.alibaba.wukong.im.Message msg) {
    //				ToastUtil.showToast(ToShareContactsActivity.this, R.string.send_success);
    //				if (!TextUtils.isEmpty(msgExtra)) {
    //					mMessageBuilder.buildTextMessage(msgExtra).sendTo(mConversation,
    //							new com.alibaba.wukong.Callback<com.alibaba.wukong.im.Message>() {
    //								@Override
    //								public void onSuccess(com.alibaba.wukong.im.Message msg) {
    //								}
    //
    //								@Override
    //								public void onException(String code, String reason) {
    //									Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
    //								}
    //
    //								@Override
    //								public void onProgress(com.alibaba.wukong.im.Message msg, int i) {
    //								}
    //							});
    //				}
    //				finish();
    //			}
    //
    //			@Override
    //			public void onException(String code, String reason) {
    //				Log.e("sendShareMsg", "消息发送失败！code=" + code + " reason=" + reason);
    //				ToastUtil.showErrorToast(ToShareContactsActivity.this, "消息发送失败！code=" + code + " reason=" + reason);
    //			}
    //
    //			@Override
    //			public void onProgress(com.alibaba.wukong.im.Message msg, int i) {
    //			}
    //		});
    //	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_TOCICLE) {
            finish();
        }
    }

}

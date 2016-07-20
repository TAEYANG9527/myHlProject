package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.HlContactFindResultAdapter;
import com.itcalf.renhe.adapter.HlFastScrollAdapterforContact;
import com.itcalf.renhe.bean.HlContactContactMember;
import com.itcalf.renhe.bean.HlContactItem;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.context.hlinterface.ContactInterface;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.ContactDeleteOrAndEvent;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by wangning on 2016/5/30.
 */
public class MobileContactsActivity extends BaseActivity {
    /**
     * UI
     */
    // 联系人快速定位视图组件
    private SearchContactsSideBar sideBar;
    private LinearLayout editLl;
    // 联系人关键字查询条件
    private ClearableEditText mKeywordEdt;
    private Drawable imgCloseButton;
    // 联系人数量
    private TextView mContactCountTxt;
    // 联系人列表
    private ListView mContactsListView;
    private ListView mFindResultListView;//输入框查找结果列表
    // 字母显示
    private TextView mLetterTxt;
    private ProgressBar waitPb;
    //空--导入界面
    private LinearLayout noneContactsLl;
    private Button importContactBtn;
    /**
     * 数据
     */
    // 带标题分割的Adapter
    private HlFastScrollAdapterforContact mAdapter;
    private List<HlContactItem> hlContactItemList;

    private HlContactFindResultAdapter hlContactFindResultAdapter;
    private List<HlContacts> hlFindResultContactsList;
    private Map<String, List<HlContacts>> hlContactsMap;//adapter所需数据源，展示出来的数据都是存储在这个map里
    //从本地数据库取到的数据源
    private List<HlContacts> hlLocalContactsList;
    private List<HlContactContactMember> hlLocalContactContactMemberList;

    private SharedPreferences msp;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    /**
     * 常量
     */

    /**
     * 工具
     */
    private Handler contactHandler;

    /**
     * 广播
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.mobile_contact_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("手机联系人");
        loadingLL = (LinearLayout) findViewById(R.id.loadingLL);
        sideBar = (SearchContactsSideBar) findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        sideBar.setTextView(mLetterTxt);
        mKeywordEdt = (ClearableEditText) findViewById(R.id.keyword_edt);
        editLl = (LinearLayout) findViewById(R.id.editLl);
        sideBar.setVisibility(View.INVISIBLE);
        editLl.setVisibility(View.INVISIBLE);
        mContactCountTxt = (TextView) findViewById(R.id.count_txt);
        mContactsListView = (ListView) findViewById(R.id.contacts_list);
        mFindResultListView = (ListView) findViewById(R.id.find_result_contacts_list);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        waitPb = (ProgressBar) findViewById(R.id.waitPb);
        noneContactsLl = (LinearLayout) findViewById(R.id.none_contacts_ll);
        importContactBtn = (Button) findViewById(R.id.import_contact_btn);
        noneContactsLl.setVisibility(View.GONE);
        mKeywordEdt.setSearch(true);
    }

    @Override
    protected void initData() {
        super.initData();
        //注册EventBus
        EventBus.getDefault().register(this);
        showLoadingDialog();
        this.contactHandler = new Handler();
        this.hlContactItemList = new ArrayList<>();
        mAdapter = new HlFastScrollAdapterforContact(MobileContactsActivity.this, hlContactItemList);
        mContactsListView.setAdapter(mAdapter);
        mContactsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        this.hlFindResultContactsList = new ArrayList<>();
        hlContactFindResultAdapter = new HlContactFindResultAdapter(MobileContactsActivity.this, hlFindResultContactsList);
        mFindResultListView.setAdapter(hlContactFindResultAdapter);

        this.hlContactsMap = new TreeMap<>(
                new Comparator<String>() {
                    public int compare(String obj1, String obj2) {
                        //重写排序规则，让“#”在最后
                        if (obj1.equals(HlContactsUtils.DEFAULT_INITIAL) && !obj2.equals(HlContactsUtils.DEFAULT_INITIAL))
                            return 1;
                        if (obj2.equals(HlContactsUtils.DEFAULT_INITIAL) && !obj1.equals(HlContactsUtils.DEFAULT_INITIAL))
                            return -1;
                        if (obj2.equals(HlContactsUtils.DEFAULT_INITIAL) && obj1.equals(HlContactsUtils.DEFAULT_INITIAL))
                            return 0;
                        //重写排序规则，让“常用联系人”在最前
                        if (obj1.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL) && !obj2.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL))
                            return -1;
                        if (obj2.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL) && !obj1.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL))
                            return 1;
                        if (obj2.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL) && obj1.equals(HlContactsUtils.OFTEN_USED_DEFAULT_INITIAL))
                            return 0;
                        return obj1.compareTo(obj2);
                    }
                });
        this.hlLocalContactsList = new ArrayList<>();
        this.hlLocalContactContactMemberList = new ArrayList<>();

        sp = MobileContactsActivity.this.getSharedPreferences("last_upload_mobile_time" + RenheApplication.getInstance().getUserInfo().getSid(), 0);
        editor = sp.edit();
        msp = MobileContactsActivity.this.getSharedPreferences("regiser_guide_setting_info", 0);
        //加载本地手机通讯录
        handleLocalMobileContacts();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlContactItemList && !hlContactItemList.isEmpty() && position >= 0) {
                    HlContactItem hlContactItem = hlContactItemList.get(position);
                    if (null != hlContactItem && hlContactItem.type != HlContactItem.SECTION) {
                        HlContacts hlContacts = hlContactItem.getHlContacts();
                        HlContactsUtils.handleOnitemClick(MobileContactsActivity.this, hlContacts);
                    }
                }
            }
        });
        mFindResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlFindResultContactsList && !hlFindResultContactsList.isEmpty()) {
                    HlContacts hlContacts = hlFindResultContactsList.get(position);
                    HlContactsUtils.handleOnitemClick(MobileContactsActivity.this, hlContacts);
                }
            }
        });
        mContactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlContactItemList && !hlContactItemList.isEmpty() && position > 0) {
                    HlContactItem hlContactItem = hlContactItemList.get(position - 1);
                    if (null != hlContactItem && hlContactItem.type != HlContactItem.SECTION) {
                        HlContacts hlContacts = hlContactItem.getHlContacts();
                        HlContactsUtils.handleOnitemLongClick(MobileContactsActivity.this, hlContacts);
                    }
                }
                return true;
            }
        });
        mFindResultListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlFindResultContactsList && !hlFindResultContactsList.isEmpty()) {
                    HlContacts hlContacts = hlFindResultContactsList.get(position);
                    HlContactsUtils.handleOnitemLongClick(MobileContactsActivity.this, hlContacts);
                }
                return true;
            }
        });
        importContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MobileContactsActivity.this, MobileMailList.class));
                MobileContactsActivity.this.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        mKeywordEdt.addTextChangedListener(searchEditTextChangedWatcher);
        mKeywordEdt.setOnTouchListener(searchEditTextOnTouch);
        mKeywordEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                return true;
            }
        });

        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SearchContactsSideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // // 该字母首次出现的位置
                int section = mAdapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int position = mAdapter.getPositionForSection(section);
                    mContactsListView.setSelection(position);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    /**
     * 从本地数据库拉取所有的联系人，处理拉取的结果
     */
    private void handleLocalMobileContacts() {
        HlContactsUtils.handleLocalMobileContactsData(hlContactsMap, hlLocalContactsList,
                hlLocalContactContactMemberList, new ContactInterface() {
                    @Override
                    public void onGenerateHlContactsListSuccess() {

                    }

                    @Override
                    public void onGenerateHlContactMapSuccess() {

                    }

                    @Override
                    public void onHandleLocalDataSuccess() {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                hlContactsMap = HlContactsUtils.generateHlContactMap(hlContactsMap, hlLocalContactsList);
                                refreshUI();
                                //如果本地人脉包含通讯录人脉，标示该用户之前将手机通讯录导入过服务端，默认他赋予了应用导入本地通讯录的权限
                                if (null != hlLocalContactContactMemberList && !hlLocalContactContactMemberList.isEmpty()) {
                                    editor.putBoolean("isAuthImport", true);
                                    editor.commit();
                                }
                            }
                        });
                    }

                    @Override
                    public void onHandleDataToDbSuccess() {

                    }

                    @Override
                    public void onGetOftenUsedContactSuccess() {

                    }
                });
    }

    /**
     * 如果有新数据变化（新增、更新、删除），刷新列表
     */
    private void refreshUI() {
        editLl.setVisibility(View.VISIBLE);
        int sectionsNumber = hlContactsMap.size();
        mAdapter.prepareSections(sectionsNumber);
        addContactsListItem(hlContactsMap);
        hideLoadingDialog();
        mAdapter.notifyDataSetChanged();
        if (null == hlContactsMap || hlContactsMap.isEmpty()) {
            sideBar.setVisibility(View.INVISIBLE);
            noneContactsLl.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.VISIBLE);
            noneContactsLl.setVisibility(View.GONE);
        }
    }

    /**
     * 联系人列表adapter初始化
     *
     * @param mContactsMap 数据源
     */
    private void addContactsListItem(Map<String, List<HlContacts>> mContactsMap) {
        hlContactItemList.clear();
        int sectionPosition = 0, listPosition = 0;
        Set<Map.Entry<String, List<HlContacts>>> set = mContactsMap.entrySet();
        for (Map.Entry<String, List<HlContacts>> entry : set) {
            HlContactItem section = new HlContactItem(HlContactItem.SECTION, String.valueOf(entry.getKey()), String.valueOf(entry.getKey()));
            section.sectionPosition = sectionPosition;
            section.listPosition = listPosition++;
            section.setHlContacts(null);
            mAdapter.onSectionAdded(section, sectionPosition);
            hlContactItemList.add(section);

            List<HlContacts> contactsList = entry.getValue();
            for (int j = 0; j < contactsList.size(); j++) {
                HlContacts ct = contactsList.get(j);
                switch (ct.getType()) {
                    case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                        HlContactItem renheItem = new HlContactItem(HlContactItem.ITEM, ct.getHlContactRenheMember().getInitial(),
                                ct.getHlContactRenheMember().getSid());
                        renheItem.sectionPosition = sectionPosition;
                        renheItem.listPosition = listPosition++;
                        renheItem.setHlContacts(ct);
                        hlContactItemList.add(renheItem);
                        break;
                    case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                        HlContactItem mobileItem = new HlContactItem(HlContactItem.ITEM, ct.getHlContactContactMember().getInitial(),
                                ct.getHlContactContactMember().getSid());
                        mobileItem.sectionPosition = sectionPosition;
                        mobileItem.listPosition = listPosition++;
                        mobileItem.setHlContacts(ct);
                        hlContactItemList.add(mobileItem);
                        break;
                    case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                        HlContactItem cardItem = new HlContactItem(HlContactItem.ITEM, ct.getHlContactCardMember().getInitial(),
                                ct.getHlContactCardMember().getSid());
                        cardItem.sectionPosition = sectionPosition;
                        cardItem.listPosition = listPosition++;
                        cardItem.setHlContacts(ct);
                        hlContactItemList.add(cardItem);
                        break;
                }
            }
            sectionPosition++;
        }
    }

    private void handleFindResultByKewWord(String keyWord) {
        if (TextUtils.isEmpty(keyWord)) {
            mContactsListView.setVisibility(View.VISIBLE);
            if (null == hlContactsMap || hlContactsMap.isEmpty()) {
                sideBar.setVisibility(View.INVISIBLE);
            } else {
                sideBar.setVisibility(View.VISIBLE);
            }
            mFindResultListView.setVisibility(View.GONE);
        } else {
            if (null == hlContactsMap || hlContactsMap.isEmpty())
                return;
            if (null == hlFindResultContactsList) {
                hlFindResultContactsList = new ArrayList<>();
            }
            HlContactsUtils.findResultListByKeyWord(keyWord, hlContactsMap, hlFindResultContactsList);
            if (!hlFindResultContactsList.isEmpty()) {
                mContactsListView.setVisibility(View.GONE);
                sideBar.setVisibility(View.INVISIBLE);
                mFindResultListView.setVisibility(View.VISIBLE);
                hlContactFindResultAdapter.notifyDataSetChanged();
                mFindResultListView.setSelection(0);
            } else {
                mContactsListView.setVisibility(View.GONE);
                sideBar.setVisibility(View.INVISIBLE);
                mFindResultListView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * eventBus 用于通知人脉列表删除或新增item
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactDeleteOrAndEvent event) {
        if (event.getType() == ContactDeleteOrAndEvent.CONTACT_EVENT_TYPE_DELETE) {
            if (HlContactsUtils.deleteContactsMapItem(hlContactsMap, event.getSid()) > 0) {
                addContactsListItem(hlContactsMap);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 搜索框输入状态监听 *
     */
    private TextWatcher searchEditTextChangedWatcher = new TextWatcher() {

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
            } else {
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handleFindResultByKewWord(mKeywordEdt.getText().toString().trim());
        }
    };

    /**
     * 搜索框点击事件监听 *
     */
    private View.OnTouchListener searchEditTextOnTouch = new View.OnTouchListener() {

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
//                        int cacheInputType = mKeywordEdt.getInputType();
                        // setInputType 可以更改 TextView 的输入方式
//                        mKeywordEdt.setInputType(InputType.TYPE_NULL);// EditText始终不弹出软件键盘
                        mKeywordEdt.onTouchEvent(event);
//                        mKeywordEdt.setInputType(cacheInputType);
                        return true;
                    }
                    break;
            }
            return false;
        }
    };
}

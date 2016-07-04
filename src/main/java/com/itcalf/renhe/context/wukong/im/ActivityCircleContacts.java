package com.itcalf.renhe.context.wukong.im;

import android.content.Context;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.SeparatedListAdapter;
import com.itcalf.renhe.adapter.SeparatedListAdapterTemp;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.MemberInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.contacts.SearchForContactsActivity;
import com.itcalf.renhe.context.innermsg.ReceiverGridAdapter;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.SideBar;
import com.itcalf.renhe.view.SideBar.OnTouchingLetterChangedListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Feature:站内信选择我的联系人 Desc:初始化加载我的好友联系人，以分割标题的方式显示，默认按字母顺序排序,增加了多选框选项。
 *
 * @author xp
 */
public class ActivityCircleContacts extends BaseActivity {
    @BindView(R.id.import_contact_btn)
    com.itcalf.renhe.view.Button importContactBtn;
    @BindView(R.id.none_contacts_ll)
    LinearLayout noneContactsLl;
    @BindView(R.id.import_contact_tv)
    com.itcalf.renhe.view.TextView importContactTv;
    // 联系人快速定位视图组件
    private SideBar sideBar;
    // 联系人关键字查询条件
    private EditText mKeywordEdt;
    // 联系人关键字查询条件
    private TextView mContactCountTxt;
    // 联系人列表
    private ListView mContactsListView;
    // 字母显示
    private TextView mLetterTxt;
    private Handler mNotifHandler;
    // 带标题分割的Adapter
    private SeparatedListAdapter mAdapter;
    // 联系人数据
    private Map<String, List<HlContactRenheMember>> mContactsMap = new TreeMap<>();
    // 圈子当前成员
    private HashSet<String> circleMember = new HashSet<>();
    // 当前选择的联系人数据
    private Map<String, HlContactRenheMember> mSelectedMap = new HashMap<>();
    private Drawable imgCloseButton;
    private List<HlContactRenheMember> hlContactRenheMemberList;
    private List<HlContactRenheMember> mReceiverList;
    private ReceiverGridAdapter receiverGridAdapter;
    private ListView mContactsListViewTemp;
    private SeparatedListAdapterTemp mAdapterTemp;
    private int tempCount = 0;
    private final static String COMPLETE = "完成";
    private final static String SURE = "确定";
    private Gallery gl;
    private Button bt_save;
    private int dataSize = 0;
    private String currentTitle = COMPLETE;
    private RelativeLayout rootRl;
    private RelativeLayout contactListRl;
    private int selectContactType;//选择联系人 类型,默认是0，圈子添加好友

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.activity_circle_contacts);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("群聊加人——选择联系人"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("群聊加人——选择联系人"); // 保证 onPageEnd 在onPause 之前调用,因为
        MobclickAgent.onPause(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void findView() {
        super.findView();
        bt_save = (Button) findViewById(R.id.bt_save);
        sideBar = (SideBar) findViewById(R.id.contact_cv);
        sideBar.setVisibility(View.INVISIBLE);
        mKeywordEdt = (EditText) findViewById(R.id.keyword_edt);
        mContactCountTxt = (TextView) findViewById(R.id.count_txt);
        mContactsListView = (ListView) findViewById(R.id.contacts_list);
        mLetterTxt = (TextView) findViewById(R.id.letter_txt);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        gl = (Gallery) this.findViewById(R.id.gallery_view);
        mReceiverList = new ArrayList<>();
        Bundle bundle = this.getIntent().getExtras();
        selectContactType = getIntent().getIntExtra(Constants.SELECT_CONTACT_TYPE_KEY,
                Constants.SELECT_CONTACT_TYPE.SELECT_CONTACT_FOR_ADD_CIRCLE_MEMBER);
        if (selectContactType == Constants.SELECT_CONTACT_TYPE.SELECT_CONTACT_FOR_ADD_CIRCLE_MEMBER) {
            ArrayList list = bundle.getParcelableArrayList("list");
            if (null != list && null != list.get(0)) {
                mReceiverList = (List<HlContactRenheMember>) list.get(0);
            }
            if (getIntent().getSerializableExtra("circleUserList") != null) {
                List<MemberInfo> circleUserList = (List<MemberInfo>) getIntent().getSerializableExtra("circleUserList");
                for (int i = 0; i < circleUserList.size(); i++) {
                    circleMember.add(String.valueOf(circleUserList.get(i).getOpenId()));
                }
            }
            if (getIntent().getCharSequenceArrayExtra("memberOpenIdArray") != null) {
                CharSequence[] memberOpenIdArray = getIntent().getCharSequenceArrayExtra("memberOpenIdArray");
                for (int i = 0; i < memberOpenIdArray.length; i++) {
                    circleMember.add(String.valueOf(memberOpenIdArray[i]));
                }
            }
        }

        if (null == mReceiverList) {
            mReceiverList = new ArrayList<>();
        }
        if (mReceiverList.size() > 0) {
            gl.setVisibility(View.VISIBLE);
            bt_save.setVisibility(View.VISIBLE);
            mContactCountTxt.setVisibility(View.GONE);
        } else {
            gl.setVisibility(View.GONE);
            bt_save.setVisibility(View.GONE);
            mContactCountTxt.setVisibility(View.VISIBLE);
        }
        receiverGridAdapter = new ReceiverGridAdapter(this, mReceiverList);
        mContactsListViewTemp = (ListView) findViewById(R.id.contacts_list_temp);
        gl.setAdapter(receiverGridAdapter);
        gl.setSelection(gl.getCount() / 2);
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        contactListRl = (RelativeLayout) findViewById(R.id.contact_list_rl);
        contactListRl.setVisibility(View.GONE);
        fadeUitl.addFade(rootRl);
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "选择联系人");

        bt_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("contacts", (Serializable) mReceiverList);
                intent.putExtras(bundle);
                setResult(3, intent);
                onResultBack();
            }
        });

        sideBar.setTextView(mLetterTxt);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                // // 该字母首次出现的位置
                int section = mAdapter.getPositionForTag(s.charAt(0) + "");
                if (-1 != section) {
                    int positon = mAdapter.getPositionForSection(section);
                    mContactsListView.setSelection(positon + 1);
                }
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
        mAdapter = new SeparatedListAdapter(this, R.layout.contact_list_header);
        mNotifHandler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        String keyword = (String) msg.obj;
                        populateContact(keyword);
                        break;
                    case 1:
                        fadeUitl.removeFade(rootRl);
                        contactListRl.setVisibility(View.VISIBLE);
                        populateContact(null);
                        mContactsListView.setAdapter(mAdapter);
                        bt_save.setText(mReceiverList.size() > 0 ? "确定(" + mReceiverList.size() + "/" + dataSize + ")" : "确定");
                        break;
                }
                return false;
            }
        });
        localSearch();
        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
        mAdapterTemp = new SeparatedListAdapterTemp(this, R.layout.contact_list_header);

    }

    /**
     * 加载自己的好友，查询本地数据库
     */
    private void localSearch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (currentTitle.equals(COMPLETE)) {
                    onResultBack();
                } else if (currentTitle.equals(SURE)) {
                    mKeywordEdt.setText("");
                }
                return true;
            case R.id.item_save:
                if (currentTitle.equals(COMPLETE)) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("contacts", (Serializable) mReceiverList);
                    intent.putExtras(bundle);
                    setResult(3, intent);
                    onResultBack();
                } else if (currentTitle.equals(SURE)) {
                    mKeywordEdt.setText("");
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initListener() {
        super.initListener();
        gl.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                MobclickAgent.onEvent(ActivityCircleContacts.this, "delete_selected_receiver");
                HlContactRenheMember hlContactRenheMember = (HlContactRenheMember) arg0.getItemAtPosition(arg2);
                manageReceiver(hlContactRenheMember, false);
                if (TextUtils.isEmpty(mKeywordEdt.getText().toString().trim())) {
                    updateView(hlContactRenheMember.getId() + "", 0);
                } else {
                    updateViewTemp(hlContactRenheMember.getId() + "", 0);
                }
            }
        });
        importContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityCircleContacts.this, SearchForContactsActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    private void onResultBack() {
        finish();
        overridePendingTransition(0, R.anim.out_to_right);
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {

        @Override
        public void run() {
            if (!TextUtils.isEmpty(mKeywordEdt.getText().toString())) {
                mContactsListView.setVisibility(View.GONE);
                mContactsListViewTemp.setVisibility(View.VISIBLE);
                populateContactTemp(mKeywordEdt.getText().toString());
                if (tempCount == 0) {
                    mContactsListViewTemp.setAdapter(mAdapterTemp);
                    tempCount += 1;
                }
            } else {
                mContactsListView.setVisibility(View.VISIBLE);
                mContactsListViewTemp.setVisibility(View.GONE);
                populateContact(mKeywordEdt.getText().toString());
            }
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
            } else {
                mKeywordEdt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.edittext_search), null,
                        null, null);
            }
            handler.postDelayed(run, 500);
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

    private void populateContact(String keyword) {
        mAdapter.clear();
        mSelectedMap.clear();
        int count = 0;
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
            Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
            while (it.hasNext()) {
                Entry<String, List<HlContactRenheMember>> entry = it.next();
                List<HlContactRenheMember> contactsList = entry.getValue();
                if (null != contactsList && !contactsList.isEmpty()) {
                    List<HlContactRenheMember> ctList = new ArrayList<>();
                    for (int i = 0; i < contactsList.size(); i++) {
                        HlContactRenheMember ct = contactsList.get(i);
                        if (!TextUtils.isEmpty(keyword) && null != ct.getName()
                                && (ct.getName().toUpperCase().startsWith(keyword.toUpperCase())
                                || ct.getInitialOfFullPinYin().startsWith(keyword.toUpperCase()))) {
                            ++count;
                            ctList.add(ct);
                        } else if (TextUtils.isEmpty(keyword)) {
                            ++count;
                            ctList.add(ct);
                        }
                    }
                    if (!ctList.isEmpty()) {
                        mAdapter.addSection(entry.getKey(), new ContactsAdapter(ctList, ActivityCircleContacts.this));
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
        dataSize = count;
        if (count > 0) {
            mContactCountTxt.setText(count + "个联系人");
        } else {
            mContactCountTxt.setText("");
        }
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            sideBar.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
            mContactsListView.setVisibility(View.GONE);
            noneContactsLl.setVisibility(View.VISIBLE);
            importContactTv.setText(getString(R.string.contact_to_select_is_empty));
            importContactBtn.setText(getString(R.string.contact_to_add));
        }
    }

    public final int LAYOUT_INDEX = 0;
    public final int CHECKBOX_INDEX = 10000;

    class ContactsAdapter extends BaseAdapter {

        private List<HlContactRenheMember> cts;
        private LayoutInflater mLayoutInf;
        private ImageLoader imageLoader;

        public ContactsAdapter(List<HlContactRenheMember> cts, Context ct) {
            super();
            this.cts = cts;
            mLayoutInf = LayoutInflater.from(ct);
            imageLoader = ImageLoader.getInstance();
        }

        @Override
        public int getCount() {
            return cts.size();
        }

        @Override
        public Object getItem(int arg0) {
            return cts.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup group) {
            final HolderView holderView;
            if (null == view) {
                view = mLayoutInf.inflate(R.layout.selected_contact_list_item, null);
                holderView = new HolderView();
                holderView.username = (TextView) view.findViewById(R.id.username_txt);
                holderView.job = ((TextView) view.findViewById(R.id.job_txt));
                holderView.userFace = ((ImageView) view.findViewById(R.id.contactface_img));
                holderView.checkbox = ((CheckBox) view.findViewById(R.id.selected_ck));
                holderView.vipIv = ((ImageView) view.findViewById(R.id.vipImage));
                holderView.realNameIv = ((ImageView) view.findViewById(R.id.realnameImage));
                holderView.contactDivider = view.findViewById(R.id.contact_divider);
                view.setTag(holderView);
            } else {
                holderView = (HolderView) view.getTag();
            }
            if (position == cts.size() - 1) {
                holderView.contactDivider.setVisibility(View.GONE);
            } else {
                holderView.contactDivider.setVisibility(View.VISIBLE);
            }
            holderView.checkbox.setId(CHECKBOX_INDEX + position);
            holderView.username.setText(cts.get(position).getName());


            if (!TextUtils.isEmpty(cts.get(position).getTitle())) {
                holderView.job.setText(cts.get(position).getTitle());
            }
            if (!TextUtils.isEmpty(cts.get(position).getCompany())) {
                if (!TextUtils.isEmpty(holderView.job.getText().toString())) {
                    holderView.job.setText(holderView.job.getText().toString() + " / " + cts.get(position).getCompany().trim());
                } else {
                    holderView.job.setText(cts.get(position).getCompany().trim());
                }
            }
            if (TextUtils.isEmpty(cts.get(position).getTitle()) && TextUtils.isEmpty(cts.get(position).getCompany())) {
                holderView.job.setVisibility(View.GONE);
            } else {
                holderView.job.setVisibility(View.VISIBLE);
            }

            int mid = holderView.checkbox.getId() - CHECKBOX_INDEX;
            view.setId(LAYOUT_INDEX + position);

            boolean isExist = false;
            for (HlContactRenheMember HlContactRenheMember : mReceiverList) {
                if (String.valueOf(HlContactRenheMember.getId()).equals(cts.get(mid).getId() + "")) {
                    isExist = true;
                    break;
                }
            }
            if (isExist) {
                holderView.checkbox.setChecked(true);
            } else {
                holderView.checkbox.setChecked(false);
            }
            int accountType = cts.get(position).getAccountType();
            boolean isRealName = cts.get(position).isRealname();
            switch (accountType) {
                case 0:
                    holderView.vipIv.setVisibility(View.GONE);
                    break;
                case 1:
                    holderView.vipIv.setVisibility(View.VISIBLE);
                    holderView.vipIv.setImageResource(R.drawable.archive_vip_1);
                    break;
                case 2:
                    holderView.vipIv.setVisibility(View.VISIBLE);
                    holderView.vipIv.setImageResource(R.drawable.archive_vip_2);
                    break;
                case 3:
                    holderView.vipIv.setVisibility(View.VISIBLE);
                    holderView.vipIv.setImageResource(R.drawable.archive_vip_3);
                    break;

                default:
                    break;
            }
            if (isRealName && accountType <= 0) {
                holderView.realNameIv.setVisibility(View.VISIBLE);
                holderView.realNameIv.setImageResource(R.drawable.archive_realname);
            } else {
                holderView.realNameIv.setVisibility(View.GONE);
            }

            if (circleMember.contains(String.valueOf(cts.get(position).getImId())))
                holderView.checkbox.setVisibility(View.GONE);
            else
                holderView.checkbox.setVisibility(View.VISIBLE);

            imageLoader.displayImage(cts.get(position).getUserface(), holderView.userFace, CacheManager.circleImageOptions);
            holderView.checkbox.setClickable(false);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!circleMember.contains(String.valueOf(cts.get(position).getImId()))) {
                        CheckBox cb = holderView.checkbox;
                        if (cts.get(position).getImId() > 0) {
                            int id = v.getId() - LAYOUT_INDEX;
                            if (!cb.isChecked()) {
                                cb.setChecked(true);
                                mSelectedMap.put(cts.get(position).getId() + "", cts.get(position));
                                manageReceiver(cts.get(position), true);
                            } else {
                                cb.setChecked(false);
                                mSelectedMap.remove(cts.get(position).getId() + "");
                                manageReceiver(cts.get(position), false);
                            }
                        } else {
                            cb.setChecked(false);
                            ToastUtil.showToast(ActivityCircleContacts.this, "该用户未开通IM");
                        }
                        if (!TextUtils.isEmpty(mKeywordEdt.getText().toString())) {
                            mKeywordEdt.setText("");
                        }
                    }
                }
            });
            return view;
        }
    }

    public static class HolderView {

        public TextView username;
        public TextView job;
        public ImageView userFace;
        public CheckBox checkbox;
        public ImageView vipIv;
        public ImageView realNameIv;
        public View contactDivider;
    }

    public void updateView(String id, int type) {
        int itemIndex = -1;
        for (int i = 0; i < mContactsListView.getAdapter().getCount(); i++) {
            if (mContactsListView.getAdapter().getItem(i) instanceof HlContactRenheMember) {
                HlContactRenheMember HlContactRenheMember = (HlContactRenheMember) mContactsListView.getAdapter().getItem(i);
                if (id.equals(HlContactRenheMember.getId() + "")) {
                    itemIndex = i;
                    break;
                }
            }
        }
        // 得到第一个可显示控件的位置，
        int visiblePosition = mContactsListView.getFirstVisiblePosition();
        // 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
        if (itemIndex >= 0 && itemIndex - visiblePosition >= 0) {
            // 得到要更新的item的view
            View view = mContactsListView.getChildAt(itemIndex - visiblePosition);// 自定义的listview有header会算作listview的子itemview，故加1
            // 从view中取得holder
            if (null != view) {
                HolderView holderView = (HolderView) view.getTag();
                if (null != holderView) {
                    if (type == 1) {// 选中
                        holderView.checkbox.setChecked(true);
                    } else if (type == 0) {
                        holderView.checkbox.setChecked(false);
                    }
                }
            }
        }
    }

    private void manageReceiver(HlContactRenheMember mContact, boolean isChecked) {
        boolean ifExist = false;
        int flag = -1;
        for (int i = 0; i < mReceiverList.size(); i++) {
            HlContactRenheMember contact2 = mReceiverList.get(i);
            if (String.valueOf(contact2.getId()).equals(mContact.getId() + "")) {
                ifExist = true;
                flag = i;
                break;
            }
        }
        if (isChecked) {
            if (!ifExist) {
                mReceiverList.add(mContact);
            }
        } else {
            if (ifExist && flag >= 0) {
                mReceiverList.remove(flag);
            }
        }
        bt_save.setText(mReceiverList.size() > 0 ? "确定(" + mReceiverList.size() + "/" + dataSize + ")" : "确定");
        receiverGridAdapter.notifyDataSetChanged();
        gl.setSelection(gl.getCount() / 2);
        if (mReceiverList.size() > 0) {
            gl.setVisibility(View.VISIBLE);
            bt_save.setVisibility(View.VISIBLE);
            mContactCountTxt.setVisibility(View.GONE);
        } else {
            gl.setVisibility(View.GONE);
            bt_save.setVisibility(View.GONE);
            mContactCountTxt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mAdapter != null && mAdapterTemp != null) {
            mAdapter.clear();
            mAdapterTemp.clear();
        }
    }

    private void populateContactTemp(String keyword) {
        mAdapterTemp.clear();
        int count = 0;
        if (null != mContactsMap && !mContactsMap.isEmpty()) {
            Set<Entry<String, List<HlContactRenheMember>>> set = mContactsMap.entrySet();
            Iterator<Entry<String, List<HlContactRenheMember>>> it = set.iterator();
            while (it.hasNext()) {
                Map.Entry<java.lang.String, java.util.List<HlContactRenheMember>> entry = it.next();
                List<HlContactRenheMember> contactsList = entry.getValue();
                if (null != contactsList && !contactsList.isEmpty()) {
                    List<HlContactRenheMember> ctList = new ArrayList<>();
                    for (int i = 0; i < contactsList.size(); i++) {
                        HlContactRenheMember ct = contactsList.get(i);
                        if (null != keyword && null != ct.getName()
                                && (ct.getName().toUpperCase().startsWith(keyword.toUpperCase())
                                || ct.getInitialOfFullPinYin().startsWith(keyword.toUpperCase()))) {
                            ++count;
                            ctList.add(ct);
                        } else if (TextUtils.isEmpty(keyword)) {
                            ++count;
                            ctList.add(ct);
                        }
                    }
                    if (!ctList.isEmpty()) {
                        mAdapterTemp.addSection(entry.getKey(), new ContactsAdapterTemp(ctList, ActivityCircleContacts.this));
                    }
                    mAdapterTemp.notifyDataSetChanged();
                }
            }
        }
        if (count > 0) {
            mContactCountTxt.setText(count + "个联系人");
        } else {
            mContactCountTxt.setText("");
        }
    }

    class ContactsAdapterTemp extends BaseAdapter {
        private Context mContext;
        private List<HlContactRenheMember> cts;
        private LayoutInflater mLayoutInf;
        private ImageLoader imageLoader;

        public ContactsAdapterTemp(List<HlContactRenheMember> cts, Context ct) {
            super();
            this.cts = cts;
            this.mContext = ct;
            mLayoutInf = LayoutInflater.from(ct);
            imageLoader = ImageLoader.getInstance();
        }

        @Override
        public int getCount() {
            return cts.size();
        }

        @Override
        public Object getItem(int arg0) {
            return cts.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup group) {
            final HolderViewTemp holderView1;
            view = mLayoutInf.inflate(R.layout.selected_contact_list_item, null);
            holderView1 = new HolderViewTemp();
            holderView1.username = (TextView) view.findViewById(R.id.username_txt);
            holderView1.job = ((TextView) view.findViewById(R.id.job_txt));
            holderView1.userFace = ((ImageView) view.findViewById(R.id.contactface_img));
            holderView1.checkbox = ((CheckBox) view.findViewById(R.id.selected_ck));
            holderView1.vipIv = ((ImageView) view.findViewById(R.id.vipImage));
            holderView1.realNameIv = ((ImageView) view.findViewById(R.id.realnameImage));
            view.setTag(holderView1);
            holderView1.checkbox.setId(CHECKBOX_INDEX + position);
            holderView1.username.setText(cts.get(position).getName());

            if (!TextUtils.isEmpty(cts.get(position).getTitle())) {
                holderView1.job.setText(cts.get(position).getTitle());
            }
            if (!TextUtils.isEmpty(cts.get(position).getCompany())) {
                if (!TextUtils.isEmpty(holderView1.job.getText().toString())) {
                    holderView1.job.setText(holderView1.job.getText().toString() + " / " + cts.get(position).getCompany().trim());
                } else {
                    holderView1.job.setText(cts.get(position).getCompany().trim());
                }
            }
            if (TextUtils.isEmpty(cts.get(position).getTitle()) && TextUtils.isEmpty(cts.get(position).getCompany())) {
                holderView1.job.setVisibility(View.GONE);
            } else {
                holderView1.job.setVisibility(View.VISIBLE);
            }


            int mid = holderView1.checkbox.getId() - CHECKBOX_INDEX;
            view.setId(LAYOUT_INDEX + position);

            boolean isExist = false;
            for (HlContactRenheMember HlContactRenheMember : mReceiverList) {
                if (String.valueOf(HlContactRenheMember.getId()).equals(cts.get(position).getId() + "")) {
                    isExist = true;
                    break;
                }
            }
            if (isExist) {
                holderView1.checkbox.setChecked(true);
            } else {
                holderView1.checkbox.setChecked(false);
            }
            int accountType = cts.get(position).getAccountType();
            boolean isRealName = cts.get(position).isRealname();
            switch (accountType) {
                case 0:
                    holderView1.vipIv.setVisibility(View.GONE);
                    break;
                case 1:
                    holderView1.vipIv.setVisibility(View.VISIBLE);
                    holderView1.vipIv.setImageResource(R.drawable.archive_vip_1);
                    break;
                case 2:
                    holderView1.vipIv.setVisibility(View.VISIBLE);
                    holderView1.vipIv.setImageResource(R.drawable.archive_vip_2);
                    break;
                case 3:
                    holderView1.vipIv.setVisibility(View.VISIBLE);
                    holderView1.vipIv.setImageResource(R.drawable.archive_vip_3);
                    break;

                default:
                    break;
            }
            if (isRealName && accountType <= 0) {
                holderView1.realNameIv.setVisibility(View.VISIBLE);
                holderView1.realNameIv.setImageResource(R.drawable.archive_realname);
            } else {
                holderView1.realNameIv.setVisibility(View.GONE);
            }

            if (circleMember.contains(String.valueOf(cts.get(position).getImId())))
                holderView1.checkbox.setVisibility(View.GONE);
            else
                holderView1.checkbox.setVisibility(View.VISIBLE);

            imageLoader.displayImage(cts.get(position).getUserface(), holderView1.userFace, CacheManager.circleImageOptions);
            holderView1.checkbox.setClickable(false);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!circleMember.contains(String.valueOf(cts.get(position).getImId()))) {
                        if (cts.get(position).getImId() > 0) {
                            int id = v.getId() - LAYOUT_INDEX;
                            CheckBox cb = holderView1.checkbox;
                            if (!cb.isChecked()) {
                                cb.setChecked(true);
                                manageReceiver(cts.get(position), true);
                            } else {
                                cb.setChecked(false);
                                manageReceiver(cts.get(position), false);
                            }
                        } else {
                            ToastUtil.showToast(mContext, "该用户未开通IM");
                        }
                        if (!TextUtils.isEmpty(mKeywordEdt.getText().toString())) {
                            mKeywordEdt.setText("");
                            DeviceUitl.hideSoftInput(mKeywordEdt);
                        }
                    }
                }
            });
            return view;
        }
    }

    public static class HolderViewTemp {

        public TextView username;
        public TextView job;
        public ImageView userFace;
        public CheckBox checkbox;
        public ImageView vipIv;
        public ImageView realNameIv;
    }

    public void updateViewTemp(String id, int type) {
        int itemIndex = -1;
        for (int i = 0; i < mContactsListViewTemp.getAdapter().getCount(); i++) {
            if (mContactsListViewTemp.getAdapter().getItem(i) instanceof HlContactRenheMember) {
                HlContactRenheMember HlContactRenheMember = (HlContactRenheMember) mContactsListViewTemp.getAdapter().getItem(i);
                if (id.equals(HlContactRenheMember.getId() + "")) {
                    itemIndex = i;
                    break;
                }
            }
        }
        // 得到第一个可显示控件的位置，
        int visiblePosition = mContactsListViewTemp.getFirstVisiblePosition();
        // 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
        if (itemIndex >= 0 && itemIndex - visiblePosition >= 0) {
            // 得到要更新的item的view
            View view = mContactsListViewTemp.getChildAt(itemIndex - visiblePosition);// 自定义的listview有header会算作listview的子itemview，故加1
            // 从view中取得holder
            if (null != view) {
                HolderViewTemp holderView = (HolderViewTemp) view.getTag();
                if (null != holderView) {
                    if (type == 1) {// 选中
                        holderView.checkbox.setChecked(true);
                    } else if (type == 0) {
                        holderView.checkbox.setChecked(false);
                    }
                }
            }
        }
    }
}

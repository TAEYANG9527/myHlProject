package com.itcalf.renhe.context.fragmentMain;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.actionprovider.PlusActionProvider;
import com.itcalf.renhe.adapter.ContactsPupAdapter;
import com.itcalf.renhe.adapter.HlContactFindResultAdapter;
import com.itcalf.renhe.adapter.HlFastScrollAdapterforContact;
import com.itcalf.renhe.bean.HlContactCardMember;
import com.itcalf.renhe.bean.HlContactContactMember;
import com.itcalf.renhe.bean.HlContactItem;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.context.contacts.MobileContactsActivity;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.context.contacts.MyCircleAct;
import com.itcalf.renhe.context.contacts.NewFriendsAct;
import com.itcalf.renhe.context.contacts.SaveToMailListActivity;
import com.itcalf.renhe.context.hlinterface.ContactInterface;
import com.itcalf.renhe.context.relationship.AdvancedSearchIndexActivityTwo;
import com.itcalf.renhe.context.template.BaseFragment;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.eventbusbean.ContactDeleteOrAndEvent;
import com.itcalf.renhe.eventbusbean.TopContactEvent;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.HlContactsUtils;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.ClearableEditText;
import com.itcalf.renhe.view.SearchContactsSideBar;
import com.itcalf.renhe.view.TipBox;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.renhe.heliao.idl.contact.ContactList;
import de.greenrobot.event.EventBus;

/**
 * 新版人脉列表
 * Created by wangning on 2016/3/8.
 */
public class NewContactFragmentVersion extends BaseFragment {
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
    private LinearLayout contactTopTipLl;
    private ProgressBar contactsTopTipPb;
    private TextView contactsTopTip;
    private View headerView;
    private TextView newFriendNumTv;
    private ImageView newNoticeIv;
    private RelativeLayout newFriendRl;//新的好友
    private RelativeLayout circleRl;//圈子
    private RelativeLayout mobileContactRl;//手机联系人
    //右上角弹框
    private PopupWindow popWindow;
    private View popView;
    private ListView popListView;
    /**
     * 数据
     */

    /**
     * 由于刚打开应用，就会去加载联系人列表，在渲染列表到UI时会卡顿，所以机制改为进入应用，开始请求数据，并插入数据库，
     * 当用户切换到“人脉”tab时才去渲染列表UI,为了提高切换的顺畅，切到人脉 tab后，延时 INIT_UI_DELAY 秒再渲染
     * isUIInited = false; 列表UI是否已经渲染过了
     * hasGetContacts = false; 是否已经从服务端获取到了列表
     * isUserVisable = false; 用户此时是否是在“人脉”tab
     */
    private boolean isUIInited = false;//列表UI是否已经渲染过了
    private boolean hasLoadLocalContacts = false;//是否已经从本地数据库获取到人脉列表
    private boolean isUserVisable = false;//用户此时是否是在“人脉”tab
    private boolean isFinishFirstRequest = false;//用户是否已经调用了第一次请求
    private boolean isFinishMoreRequest = false;//用户是否已经调用了之后的请求
    private boolean hasInitFirstRequestData = false;//是否已经更新了第一页请求到的数据到UI列表
    private boolean hasInitMoreRequestData = false;//是否已经更新了之后请求到的数据到UI列表
    private boolean hasLocalDatas = false;//是否有本地数据
    private boolean isLoadingLocalDatas = false;//是否正在加载本地数据
    // 带标题分割的Adapter
    private HlFastScrollAdapterforContact mAdapter;
    private List<HlContactItem> hlContactItemList;

    private HlContactFindResultAdapter hlContactFindResultAdapter;
    private List<HlContacts> hlFindResultContactsList;
    //maxUpdatTime         上一次检测更新时间
    //maxContactHlmemberId 上次返回的最大memberId
    //maxContactMobileId   上次返回的最大mobileId
    //maxContactCardId     上次返回的最大cardId
    //requestCount         获取个数限制，默认设为200
    private long maxUpdatTime;
    private int maxContactHlmemberId, maxContactMobileId, maxContactCardId;
    private Map<String, List<HlContacts>> hlContactsMap;//adapter所需数据源，展示出来的数据都是存储在这个map里
    //从本地数据库取到的数据源
    private List<HlContacts> hlLocalContactsList;
    private List<HlContactRenheMember> hlLocalContactRenheMemberList;
    private List<HlContactContactMember> hlLocalContactContactMemberList;
    private List<HlContactCardMember> hlLocalContactCardMemberList;
    //第一次拉取获取到的数据源
    private List<HlContacts> hlContactsList;
    private List<HlContactRenheMember> hlContactRenheMemberList;
    private List<HlContactContactMember> hlContactContactMemberList;
    private List<HlContactCardMember> hlContactCardMemberList;
    private List<HlContactRenheMember> needUpdateRenheMemberList;
    private List<HlContactRenheMember> needDeleteRenheMemberList;
    private List<HlContactContactMember> needDeleteMobileMemberList;
    private List<HlContactCardMember> needDeleteCardMemberList;

    //常用联系人
    private List<HlContacts> hlOftenUsedContacts;

    private int newFriendCount;
    //新用户引导提示框
    private TipBox tipBox;
    private boolean isRegister;
    private SharedPreferences msp;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    /**
     * 常量
     */
    /**
     * 由于有的会员人脉非常多，达到七八万，打开应用第一次拉取人脉列表时，如果请求数量太多，比较卡顿（网络等待、UI渲染），
     * 所以第一次先给他拉取少量（100条）显示在UI上，以及插入到数据库，之后再每页获取1000条（服务端目前一页最大限制是1000），
     * 然后等接下来的所有数据全部拉取下来之后再一次性更新到UI列表上以及插入到数据库
     */
    private final static int FIRST_REQUEST_COUNT = 100;
    private final static int MORE_REQUEST_COUNT = 1000;
    private final static long INIT_UI_DELAY = 2000;//切到人脉 tab后，延时 INIT_UI_DELAY 秒再渲染
    private int ID_TASK_GET_CONTACTS_LIST = TaskManager.getTaskId();

    /**
     * 工具
     */
    private Handler contactHandler;

    /**
     * 广播
     */
    private ContactBroadCastReceiver contactBroadCastReceiver;

    @Override
    protected void initLayoutId() {
        layoutId = R.layout.new_contact_fragment_layout_version2;
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        loadingLL = (LinearLayout) view.findViewById(R.id.loadingLL);
        contactTopTipLl = (LinearLayout) view.findViewById(R.id.contact_top_tip_Ll);
        contactsTopTipPb = (ProgressBar) view.findViewById(R.id.contacts_top_tip_Pb);
        contactsTopTip = (TextView) view.findViewById(R.id.contacts_top_tip);//tip
        sideBar = (SearchContactsSideBar) view.findViewById(R.id.contact_cv);
        mLetterTxt = (TextView) view.findViewById(R.id.letter_txt);
        sideBar.setTextView(mLetterTxt);
        mKeywordEdt = (ClearableEditText) view.findViewById(R.id.keyword_edt);
        editLl = (LinearLayout) view.findViewById(R.id.editLl);
        sideBar.setVisibility(View.INVISIBLE);
        editLl.setVisibility(View.INVISIBLE);
        mContactCountTxt = (TextView) view.findViewById(R.id.count_txt);
        mContactsListView = (ListView) view.findViewById(R.id.contacts_list);
        mFindResultListView = (ListView) view.findViewById(R.id.find_result_contacts_list);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        waitPb = (ProgressBar) view.findViewById(R.id.waitPb);
        noneContactsLl = (LinearLayout) view.findViewById(R.id.none_contacts_ll);
        importContactBtn = (Button) view.findViewById(R.id.import_contact_btn);
        noneContactsLl.setVisibility(View.GONE);
        mKeywordEdt.setSearch(true);
        headerView = View.inflate(getActivity(), R.layout.hl_contact_fragment_header_layout, null);
        headerView.setVisibility(View.GONE);
        newFriendNumTv = (TextView) headerView.findViewById(R.id.newFri_numb);
        newNoticeIv = (ImageView) headerView.findViewById(R.id.new_notice_iv);
        newFriendRl = (RelativeLayout) headerView.findViewById(R.id.newfitem_rl);
        circleRl = (RelativeLayout) headerView.findViewById(R.id.mycircle_Rl);
        mobileContactRl = (RelativeLayout) headerView.findViewById(R.id.mobile_contact_Rl);
        //初始化menu菜单window
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popView = inflater.inflate(R.layout.popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        popListView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setOutsideTouchable(true);
        popListView.setAdapter(new ContactsPupAdapter(getActivity()));
        createMenuPopupWindow(getActivity(), popWindow, popListView);
    }

    @Override
    protected void initData() {
        super.initData();
        showLoadingDialog();
        this.contactHandler = new Handler();
        this.hlContactItemList = new ArrayList<>();
        mAdapter = new HlFastScrollAdapterforContact(getActivity(), hlContactItemList);
        mContactsListView.addHeaderView(headerView);
        mContactsListView.setAdapter(mAdapter);
        mContactsListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        this.hlFindResultContactsList = new ArrayList<>();
        hlContactFindResultAdapter = new HlContactFindResultAdapter(getActivity(), hlFindResultContactsList);
        mFindResultListView.setAdapter(hlContactFindResultAdapter);

        maxUpdatTime = SharedPreferencesUtil.getLongShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXUPDATTIME, 0, true);
        maxContactHlmemberId = SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTHLMEMBERID, 0, true);
        maxContactMobileId = SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTMOBILEID, 0, true);
        maxContactCardId = SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTCARDID, 0, true);
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
        this.hlLocalContactRenheMemberList = new ArrayList<>();
        this.hlLocalContactContactMemberList = new ArrayList<>();
        this.hlLocalContactCardMemberList = new ArrayList<>();

        this.hlContactsList = new ArrayList<>();
        this.hlContactRenheMemberList = new ArrayList<>();
        this.hlContactContactMemberList = new ArrayList<>();
        this.hlContactCardMemberList = new ArrayList<>();
        this.needUpdateRenheMemberList = new ArrayList<>();
        this.needDeleteRenheMemberList = new ArrayList<>();
        this.needDeleteMobileMemberList = new ArrayList<>();
        this.needDeleteCardMemberList = new ArrayList<>();

        this.hlOftenUsedContacts = new ArrayList<>();
        sp = getActivity().getSharedPreferences("last_upload_mobile_time" + RenheApplication.getInstance().getUserInfo().getSid(), 0);
        editor = sp.edit();
        if (!HlContactsUtils.renheMemberTableHasCache()) {//人和好友本地缓存不存在，去服务端拉取
            maxUpdatTime = 0;
            maxContactHlmemberId = 0;
            maxContactMobileId = 0;
            maxContactCardId = 0;
            SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXUPDATTIME, 0, true);
            SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTHLMEMBERID, 0, true);
            SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTMOBILEID, 0, true);
            SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTCARDID, 0, true);
            loadRemoteContacts(true);//第一次拉取联系人
        } else {
            hasLocalDatas = true;
        }
        newFriendCount = SharedPreferencesUtil.getIntShareData(Constants.SHAREDPREFERENCES_KEY.NEW_FRIEND_UNREAD_COUNT, 0, true);
        initNewFriendView();
        msp = getActivity().getSharedPreferences("regiser_guide_setting_info", 0);
        isRegister = msp.getBoolean("regiser_contacts" + RenheApplication.getInstance().getUserInfo().getSid(), false);
        if (isRegister) {
            tipBox = new TipBox(getActivity(), 2, new TipBox.OnItemClickListener() {
                @Override
                public void onItemClick() {
                    SharedPreferences.Editor editor2 = msp.edit();
                    editor2.putBoolean("regiser_contacts" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                    editor2.commit();
                }
            });
        }
        Bundle bundle = getArguments();
        if (null != bundle) {
            //这个fragment是人脉tab和档案页，点击个人人脉进入的页面共用，用此标志位区分是否是从我的档案，点击人脉进来
            boolean isFromArchive = bundle.getBoolean("isFromArchive");
            if (isFromArchive) {
                isUserVisable = true;
                initViewOnVisible(isUserVisable);
            }
        }
    }

    @Override
    protected void initMenuView(Menu menu) {
        super.initMenuView(menu);
        MenuItem addfriendsItem = menu.findItem(R.id.item_add);
        addfriendsItem.setTitle("更多");
        addfriendsItem.setVisible(true);
    }

    @Override
    protected void onMenuItemSelected(MenuItem item) {
        super.onMenuItemSelected(item);
        //和聊页面中 点击 加号图标弹出Popupwindow （添加人脉，创建圈子，加入圈子）
        if (null == popWindow)
            return;
        if (popWindow.isShowing()) {
            popWindow.dismiss();
            return;
        }
        Rect frame = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        if (null != getActivity() && getActivity() instanceof AppCompatActivity) {
            popWindow.showAtLocation(popView, Gravity.RIGHT | Gravity.TOP, 20,
                    ((AppCompatActivity) getActivity()).getSupportActionBar().getHeight() + statusBarHeight);
            new ContactsUtil(getActivity()).SyncContacts();//获取新的人脉
        }
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();
        //注册EventBus
        EventBus.getDefault().register(this);
        // 注册广播
        contactBroadCastReceiver = new ContactBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BroadCastAction.NEWFRIENDS_COUNT);//新的好友
        getActivity().registerReceiver(contactBroadCastReceiver, intentFilter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlContactItemList && !hlContactItemList.isEmpty() && position > 0) {
                    HlContactItem hlContactItem = hlContactItemList.get(position - 1);
                    if (null != hlContactItem && hlContactItem.type != HlContactItem.SECTION) {
                        HlContacts hlContacts = hlContactItem.getHlContacts();
                        HlContactsUtils.handleOnitemClick(getActivity(), hlContacts);
                    }
                }
            }
        });
        mFindResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != hlFindResultContactsList && !hlFindResultContactsList.isEmpty()) {
                    HlContacts hlContacts = hlFindResultContactsList.get(position);
                    HlContactsUtils.handleOnitemClick(getActivity(), hlContacts);
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
                        HlContactsUtils.handleOnitemLongClick(getActivity(), hlContacts);
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
                    HlContactsUtils.handleOnitemLongClick(getActivity(), hlContacts);
                }
                return true;
            }
        });
        newFriendRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), NewFriendsAct.class);
                startActivityForResult(intent, Constants.CONTACTS_REQUEST_CODE.CONTACTS_REQUEST_CHECK_NEW_FRIEND);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        circleRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MyCircleAct.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        mobileContactRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MobileContactsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        importContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), MobileMailList.class));
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
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
    protected void unRegisterReceiver() {
        super.unRegisterReceiver();
        EventBus.getDefault().unregister(this);//反注册EventBus
        if (null != contactBroadCastReceiver)
            getActivity().unregisterReceiver(contactBroadCastReceiver);
    }

    /**
     * 获取所有的服务端联系人
     */
    private void loadRemoteContacts(boolean isFirstRequest) {
        if (checkGrpcBeforeInvoke(ID_TASK_GET_CONTACTS_LIST)) {
            grpcController.getContactsList(ID_TASK_GET_CONTACTS_LIST, maxUpdatTime, maxContactHlmemberId,
                    maxContactMobileId, maxContactCardId, isFirstRequest ? FIRST_REQUEST_COUNT : MORE_REQUEST_COUNT);
        }
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (null != result) {
            if (result instanceof ContactList.ContactListResponse) {
                ContactList.ContactListResponse response = (ContactList.ContactListResponse) result;
                if (!isFinishFirstRequest)
                    handleFirstAllRemoteContacts(response);
                else
                    handleMoreAllRemoteContacts(response);
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isUserVisable = isVisibleToUser;
        initViewOnVisible(isUserVisable);
    }

    private void initViewOnVisible(boolean isVisibleToUser) {
        //人脉数据加载
        if (isVisibleToUser && hasLocalDatas && !hasLoadLocalContacts) {
            handleLocalontacts();//从本地数据库拉取所有的联系人，处理拉取的结果
        } else {
            if (isVisibleToUser && !isUIInited && hasLoadLocalContacts) {
                contactHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                    }
                }, INIT_UI_DELAY);
            } else if (isFinishFirstRequest && !hasInitFirstRequestData) {
                contactHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                        hasInitFirstRequestData = true;
                    }
                }, INIT_UI_DELAY);
            } else if (isFinishMoreRequest && !hasInitMoreRequestData) {
                contactHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshUI();
                        hasInitMoreRequestData = true;
                    }
                }, INIT_UI_DELAY);
            }
        }
        if (null != hlContactsMap && !hlContactsMap.isEmpty() && null != sideBar && null != mLetterTxt && isUIInited) {
            if (!isVisibleToUser) {
                sideBar.hideDialog();
                sideBar.setVisibility(View.INVISIBLE);
            } else {
                sideBar.setVisibility(View.VISIBLE);
            }
        }

        //新用户引导
        if (isVisibleToUser) {
            if (tipBox != null) {
                isRegister = msp.getBoolean("regiser_contacts" + RenheApplication.getInstance().getUserInfo().getSid(), false);
                if (isRegister) {
                    if (null != getActivity()) {
                        Rect frame = new Rect();
                        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                        int statusBarHeight = frame.top;
                        int high = 0;
                        if (getActivity() instanceof AppCompatActivity && null != ((AppCompatActivity) getActivity()).getSupportActionBar()) {
                            high = ((AppCompatActivity) getActivity()).getSupportActionBar().getHeight();
                        }
                        tipBox.showAtLocation(tipBox.getContentView(), Gravity.END | Gravity.TOP, 0,
                                high + statusBarHeight - 15);
                    }
                }
            }
        } else {
            if (tipBox != null) {
                tipBox.dismiss();
            }
        }
        //新的好友小红点提示
        if (isVisibleToUser) {
            initNewFriendNoticeView();
        }
    }

    /**
     * 如果有新数据变化（新增、更新、删除），刷新列表
     */
    private void refreshUI() {
        headerView.setVisibility(View.VISIBLE);
        editLl.setVisibility(View.VISIBLE);
        int sectionsNumber = hlContactsMap.size();
        mAdapter.prepareSections(sectionsNumber);
        addContactsListItem(hlContactsMap);
        hideLoadingDialog();
        mAdapter.notifyDataSetChanged();
        isUIInited = true;
        if (null == hlContactsMap || hlContactsMap.isEmpty()) {
            sideBar.setVisibility(View.INVISIBLE);
            noneContactsLl.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.VISIBLE);
            noneContactsLl.setVisibility(View.GONE);
        }
    }

    /**
     * 没有新数据变化（新增、更新、删除），不刷新列表，只初始化界面应该要显示出了的元素，比如搜索框、sidebar
     */
    private void noNeedRefreshUI() {
        hideLoadingDialog();
        headerView.setVisibility(View.VISIBLE);
        if (null != hlContactsMap && hlContactsMap.size() > 0) {
            sideBar.setVisibility(View.VISIBLE);
            editLl.setVisibility(View.VISIBLE);
        } else {
            sideBar.setVisibility(View.INVISIBLE);
            editLl.setVisibility(View.INVISIBLE);
        }
        isUIInited = true;
    }

    /**
     * 从本地数据库拉取所有的联系人，处理拉取的结果
     */
    private void handleLocalontacts() {
        if (isLoadingLocalDatas)
            return;
        //根据取到的人脉，组合成需要的各类集合，包括人和好友列表、通讯录好友列表、名片列表等
        isLoadingLocalDatas = true;
        HlContactsUtils.handleLocalData(hlContactsMap, hlLocalContactsList, hlLocalContactRenheMemberList,
                hlLocalContactContactMemberList, hlLocalContactCardMemberList, new ContactInterface() {
                    @Override
                    public void onGenerateHlContactsListSuccess() {

                    }

                    @Override
                    public void onGenerateHlContactMapSuccess() {

                    }

                    @Override
                    public void onHandleLocalDataSuccess() {
                        if (null != getActivity()) {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    hasLoadLocalContacts = true;
                                    isLoadingLocalDatas = false;
                                    hlContactsMap = HlContactsUtils.generateHlContactMap(hlContactsMap, hlLocalContactsList);
                                    if (isUserVisable && !isUIInited) {
                                        refreshUI();
                                    }
                                    loadRemoteContacts(false);//第一次拉取联系人
                                    //如果本地人脉包含通讯录人脉，标示该用户之前将手机通讯录导入过服务端，默认他赋予了应用导入本地通讯录的权限
                                    if (null != hlLocalContactContactMemberList && !hlLocalContactContactMemberList.isEmpty()) {
                                        editor.putBoolean("isAuthImport", true);
                                        editor.commit();
                                    }
                                }
                            });
                        }
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
     * 第一次从服务端拉取所有的联系人，处理拉取的结果
     */
    private void handleFirstAllRemoteContacts(final ContactList.ContactListResponse response) {
        maxUpdatTime = response.getMaxUpdateTime();
        maxContactHlmemberId = response.getMaxMemberId();
        maxContactMobileId = response.getMaxMobileId();
        maxContactCardId = response.getMaxCardId();
        //根据取到的人脉，组合成需要的各类集合，包括人和好友列表、通讯录好友列表、名片列表等
        HlContactsUtils.generateHlContactsList(response, hlContactsMap, hlContactsList, hlContactRenheMemberList,
                hlContactContactMemberList, hlContactCardMemberList, needUpdateRenheMemberList,
                needDeleteRenheMemberList, needDeleteMobileMemberList, needDeleteCardMemberList, new ContactInterface() {
                    @Override
                    public void onGenerateHlContactsListSuccess() {
                        if (null != getActivity()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hlContactsMap = HlContactsUtils.generateHlContactMap(hlContactsMap, hlContactsList);
                                    /**
                                     * 由于刚打开应用，就会去加载联系人列表，在渲染列表到UI时会卡顿，所以机制改为进入应用，开始请求数据，并插入数据库，
                                     * 当用户切换到“人脉”tab时才去渲染列表UI
                                     * isUIInited = false; 列表UI是否已经渲染过了
                                     * hasGetFirstPageContacts = false; 是否已经从服务端获取到了第一页列表
                                     * isUserVisable = false; 用户此时是否是在“人脉”tab
                                     *
                                     */
                                    isFinishFirstRequest = true;
                                    if (isUserVisable) {
                                        if (!hlContactsList.isEmpty())
                                            refreshUI();
                                        else
                                            noNeedRefreshUI();
                                        hasInitFirstRequestData = true;
                                    }
                                    //将取到的人脉插入到数据库
                                    HlContactsUtils.handleDataToDbAfterRequest(hlContactRenheMemberList,
                                            hlContactContactMemberList, hlContactCardMemberList, needUpdateRenheMemberList,
                                            needDeleteRenheMemberList, needDeleteMobileMemberList, needDeleteCardMemberList, new ContactInterface() {
                                                @Override
                                                public void onGenerateHlContactsListSuccess() {

                                                }

                                                @Override
                                                public void onGenerateHlContactMapSuccess() {

                                                }

                                                @Override
                                                public void onHandleLocalDataSuccess() {

                                                }

                                                @Override
                                                public void onHandleDataToDbSuccess() {
                                                    if (null != getActivity()) {
                                                        getActivity().runOnUiThread(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXUPDATTIME, maxUpdatTime, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTHLMEMBERID, maxContactHlmemberId, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTMOBILEID, maxContactMobileId, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTCARDID, maxContactCardId, true);
                                                                if (response.getNextPage()) {
                                                                    loadRemoteContacts(false);//如果还有没拉取的好友，拉取剩余的联系人
                                                                } else {
                                                                    if (!hasLoadLocalContacts)
                                                                        //获取常用联系人
                                                                        handleOfftenUsedContacts();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onGetOftenUsedContactSuccess() {

                                                }
                                            });
                                    //如果人脉包含通讯录人脉，标示该用户之前将手机通讯录导入过服务端，默认他赋予了应用导入本地通讯录的权限
                                    if (null != hlContactContactMemberList && !hlContactContactMemberList.isEmpty()) {
                                        editor.putBoolean("isAuthImport", true);
                                        editor.commit();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onGenerateHlContactMapSuccess() {

                    }

                    @Override
                    public void onHandleLocalDataSuccess() {

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
     * 第一次从服务端拉取所有的联系人，处理拉取的结果
     */
    private void handleMoreAllRemoteContacts(final ContactList.ContactListResponse response) {
        maxUpdatTime = response.getMaxUpdateTime();
        maxContactHlmemberId = response.getMaxMemberId();
        maxContactMobileId = response.getMaxMobileId();
        maxContactCardId = response.getMaxCardId();
        /**
         * 由于handleMoreAllRemoteContacts在人脉多的情况下，会调用很多次，所以需要把每次调用返回的人脉缓存起来先，保存在
         * hlMoreContactsList里，等全部拉取完成后，统一插入数据库
         *
         */
        final List<HlContacts> mhlMoreContactsList = new ArrayList<>();
        final List<HlContactRenheMember> mhlMoreContactRenheMemberList;
        final List<HlContactContactMember> mhlMoreContactContactMemberList;
        final List<HlContactCardMember> mhlMoreContactCardMemberList;
        final List<HlContactRenheMember> mmoreNeedUpdateRenheMemberList;
        final List<HlContactRenheMember> mmoreNeedDeleteRenheMemberList;
        final List<HlContactContactMember> mmoreNeedDeleteMobileMemberList;
        final List<HlContactCardMember> mmoreNeedDeleteCardMemberList;

        mhlMoreContactRenheMemberList = new ArrayList<>();
        mhlMoreContactContactMemberList = new ArrayList<>();
        mhlMoreContactCardMemberList = new ArrayList<>();
        mmoreNeedUpdateRenheMemberList = new ArrayList<>();
        mmoreNeedDeleteRenheMemberList = new ArrayList<>();
        mmoreNeedDeleteMobileMemberList = new ArrayList<>();
        mmoreNeedDeleteCardMemberList = new ArrayList<>();

        //根据取到的人脉，组合成需要的各类集合，包括人和好友列表、通讯录好友列表、名片列表等
        HlContactsUtils.generateHlContactsList(response, hlContactsMap, mhlMoreContactsList, mhlMoreContactRenheMemberList,
                mhlMoreContactContactMemberList, mhlMoreContactCardMemberList, mmoreNeedUpdateRenheMemberList,
                mmoreNeedDeleteRenheMemberList, mmoreNeedDeleteMobileMemberList, mmoreNeedDeleteCardMemberList, new ContactInterface() {
                    @Override
                    public void onGenerateHlContactsListSuccess() {
                        if (null != getActivity()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hlContactsMap = HlContactsUtils.generateHlContactMap(hlContactsMap, mhlMoreContactsList);
                                    /**
                                     * 由于刚打开应用，就会去加载联系人列表，在渲染列表到UI时会卡顿，所以机制改为进入应用，开始请求数据，并插入数据库，
                                     * 当用户切换到“人脉”tab时才去渲染列表UI
                                     * isUIInited = false; 列表UI是否已经渲染过了
                                     * hasGetContacts = false; 是否已经从服务端获取到了列表
                                     * isUserVisable = false; 用户此时是否是在“人脉”tab
                                     *
                                     */

                                    if (isUserVisable && !response.getNextPage()) {
                                        isFinishMoreRequest = true;
                                        refreshUI();
                                        hasInitMoreRequestData = true;
                                    }
                                    //将获取到的这页的人脉插入到数据库
                                    //将取到的人脉插入到数据库
                                    HlContactsUtils.handleDataToDbAfterRequest(mhlMoreContactRenheMemberList,
                                            mhlMoreContactContactMemberList, mhlMoreContactCardMemberList, mmoreNeedUpdateRenheMemberList,
                                            mmoreNeedDeleteRenheMemberList, mmoreNeedDeleteMobileMemberList, mmoreNeedDeleteCardMemberList, new ContactInterface() {
                                                @Override
                                                public void onGenerateHlContactsListSuccess() {

                                                }

                                                @Override
                                                public void onGenerateHlContactMapSuccess() {

                                                }

                                                @Override
                                                public void onHandleLocalDataSuccess() {

                                                }

                                                @Override
                                                public void onHandleDataToDbSuccess() {
                                                    Logger.d("完成--插入数据库-->" + mhlMoreContactRenheMemberList.size());
                                                    if (null != getActivity()) {
                                                        getActivity().runOnUiThread(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                SharedPreferencesUtil.putLongShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXUPDATTIME, maxUpdatTime, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTHLMEMBERID, maxContactHlmemberId, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTMOBILEID, maxContactMobileId, true);
                                                                SharedPreferencesUtil.putIntShareData(Constants.SHAREDPREFERENCES_KEY.CONTACTS_MAXCONTACTCARDID, maxContactCardId, true);
                                                                if (response.getNextPage()) {
                                                                    Logger.d("还有下一页--开始请求拉取");
                                                                    loadRemoteContacts(false);//如果还有没拉取的好友，拉取剩余的联系人
                                                                } else {
                                                                    isFinishMoreRequest = true;
                                                                    if (!hasLoadLocalContacts)
                                                                        handleOfftenUsedContacts();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onGetOftenUsedContactSuccess() {

                                                }
                                            });
                                    //如果人脉包含通讯录人脉，标示该用户之前将手机通讯录导入过服务端，默认他赋予了应用导入本地通讯录的权限
                                    if (null != mhlMoreContactContactMemberList && !mhlMoreContactContactMemberList.isEmpty()) {
                                        editor.putBoolean("isAuthImport", true);
                                        editor.commit();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onGenerateHlContactMapSuccess() {

                    }

                    @Override
                    public void onHandleLocalDataSuccess() {

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
     * 联系人列表adapter初始化
     *
     * @param mContactsMap 数据源
     */
    private void addContactsListItem(Map<String, List<HlContacts>> mContactsMap) {
        hlContactItemList.clear();
        int sectionPosition = 0, listPosition = 0;
        Set<Map.Entry<String, List<HlContacts>>> set = mContactsMap.entrySet();
        for (Map.Entry<String, List<HlContacts>> entry : set) {
            List<HlContacts> contactsList = entry.getValue();
            /**
             * 由于mContactsMap中不进存放了人和网好友、名片通讯录，还存放了手机通讯录，但是手机通讯录内容是不需要展示在人脉列表的，
             * 当某个section下的数据全是手机通讯录的话，会出现两个紧挨着的section，但是并没有数据，所以添加一个标志位isSectionExist来标示某个section下是否存在一个及以上的人和网好友或者名片通讯录
             */
            boolean isSectionExist = false;
            for (int j = 0; j < contactsList.size(); j++) {
                HlContacts ct = contactsList.get(j);
                if (ct.getType() == HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE ||
                        ct.getType() == HlContacts.HLCONTACTS_CARD_MEMBER_TYPE) {
                    isSectionExist = true;
                    break;
                }
            }
            if (!isSectionExist)
                continue;
            HlContactItem section = new HlContactItem(HlContactItem.SECTION, String.valueOf(entry.getKey()), String.valueOf(entry.getKey()));
            section.sectionPosition = sectionPosition;
            section.listPosition = listPosition++;
            section.setHlContacts(null);
            mAdapter.onSectionAdded(section, sectionPosition);
            hlContactItemList.add(section);
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
//                    case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
//                        HlContactItem mobileItem = new HlContactItem(HlContactItem.ITEM, ct.getHlContactContactMember().getInitial(),
//                                ct.getHlContactContactMember().getSid());
//                        mobileItem.sectionPosition = sectionPosition;
//                        mobileItem.listPosition = listPosition++;
//                        mobileItem.setHlContacts(ct);
//                        hlContactItemList.add(mobileItem);
//                        break;
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

    private void initNewFriendView() {
        if (newFriendCount > 0) {
            newFriendNumTv.setVisibility(View.VISIBLE);
            newNoticeIv.setVisibility(View.GONE);
            newFriendNumTv.setText(newFriendCount + "");
        } else {
            newFriendNumTv.setVisibility(View.GONE);
            newFriendNumTv.setText("");
            initNewFriendNoticeView();
        }
    }

    private void initNewFriendNoticeView() {
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.NEARBY_HAS_NEW, false, true)) {
            if (newFriendNumTv.getVisibility() != View.VISIBLE) {
                newNoticeIv.setVisibility(View.VISIBLE);
            } else {
                newNoticeIv.setVisibility(View.GONE);
            }
        } else {
            newNoticeIv.setVisibility(View.GONE);
        }
    }

    /**
     * 获取常用联系人
     */
    private void handleOfftenUsedContacts() {
        HlContactsUtils.getCommonContacts(hlOftenUsedContacts, new ContactInterface() {
            @Override
            public void onGenerateHlContactsListSuccess() {

            }

            @Override
            public void onGenerateHlContactMapSuccess() {

            }

            @Override
            public void onHandleLocalDataSuccess() {

            }

            @Override
            public void onHandleDataToDbSuccess() {

            }

            @Override
            public void onGetOftenUsedContactSuccess() {
                if (null != getActivity()) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            HlContactsUtils.putOftenUsedToHlContactMap(hlContactsMap, hlOftenUsedContacts);
                            if (isUserVisable) {
                                refreshUI();
                            }
                        }
                    });
                }
            }
        });
    }

    public static PopupWindow createMenuPopupWindow(final Activity mActivity, final PopupWindow pop, ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mActivity.startActivity(new Intent(mActivity, SaveToMailListActivity.class));
                        mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                    case 1:
                        RenheIMUtil.showProgressDialog(mActivity, R.string.loading);
                        if (NetworkUtil.hasNetworkConnection(mActivity) != -1) {
                            PlusActionProvider.checkCircleCreationPrivilege(mActivity);//检查是否有权限创建圈子
                        } else
                            ToastUtil.showToast(mActivity, "网络异常");
                        RenheIMUtil.dismissProgressDialog();//如果网络异常加载提示消失
                        MobclickAgent.onEvent(mActivity, "create_circle");
                        break;
                    case 2:
                        MobclickAgent.onEvent(mActivity, "add_new_friend");
                        mActivity.startActivity(new Intent(mActivity, AdvancedSearchIndexActivityTwo.class));
                        mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }

    /**
     * eventBus 用于通知对话fragment的ListView滚动到顶部
     *
     * @param event
     */
    public void onEventMainThread(TopContactEvent event) {
        if (null != mContactsListView) {
            mContactsListView.setSelection(mContactsListView.getTop());
//            mContactsListView.smoothScrollToPosition(mContactsListView.getTop());
        }
    }

    /**
     * eventBus 用于通知人脉列表删除或新增item
     *
     * @param event
     */
    public void onEventMainThread(ContactDeleteOrAndEvent event) {
        if (event.getType() == ContactDeleteOrAndEvent.CONTACT_EVENT_TYPE_ADD) {
            if (isUIInited)
                isUserVisable = true;
            loadRemoteContacts(false);//拉取新的好友
        } else if (event.getType() == ContactDeleteOrAndEvent.CONTACT_EVENT_TYPE_DELETE) {
            if (HlContactsUtils.deleteContactsMapItem(hlContactsMap, event.getSid()) > 0) {
                addContactsListItem(hlContactsMap);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.CONTACTS_REQUEST_CODE.CONTACTS_REQUEST_CHECK_NEW_FRIEND:
                if (resultCode == getActivity().RESULT_OK) {
                    newNoticeIv.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tipBox != null) {
            tipBox.dismiss();
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

    class ContactBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BroadCastAction.NEWFRIENDS_COUNT)) {
                //新的好友
                newFriendCount = intent.getIntExtra("newFri_numb", 0);
                initNewFriendView();
            }
        }
    }
}

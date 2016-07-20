package com.itcalf.renhe.context.wukong.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.BaseRecyclerAdapter;
import com.itcalf.renhe.adapter.ConversationPupAdapter;
import com.itcalf.renhe.adapter.ConversationRecyclerItemAdapter;
import com.itcalf.renhe.context.template.BaseFragment;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.eventbusbean.TopImEvent;
import com.itcalf.renhe.listener.NewPauseOnScrollListener;
import com.itcalf.renhe.receiver.MyConversationChangeListener;
import com.itcalf.renhe.receiver.MyConversationListener;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 重写的对话列表
 * Created by wangning on 2016/1/8.
 */
public class ConversationListFragment extends BaseFragment implements ConversationListUtil.ConversationCallBack {
    //View初始化
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private LinearLayout loadingLl;
    private ConversationRecyclerItemAdapter conversationRecyclerItemAdapter;
    private PopupWindow popWindow;
    private View popView;
    private ListView popListView;
    //数据初始化
    private ArrayList<ConversationItem> datas;
    //工具初始化
    private ConversationListUtil conversationListUtil;
    private MyConversationListener myConversationListener;
    private MyConversationChangeListener myConversationChangeListener;
    private LoadConversationInfoReceiver loadConversationInfoReceiver;
    private DeleteBlackContactConversationReceiver deleteBlackContactConversationReceiver;

    //常量初始化
    @Override
    protected void initLayoutId() {
        layoutId = R.layout.im_conversation_list_layout;
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.conversation_recycler_view);
        loadingLl = (LinearLayout) view.findViewById(R.id.loadingLL);
        mRecyclerView.setVisibility(View.GONE);
        loadingLl.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);//这里用线性显示 类似于list view

        //初始化menu菜单window
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popView = inflater.inflate(R.layout.popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        popListView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setOutsideTouchable(true);
        popListView.setAdapter(new ConversationPupAdapter(getActivity()));
//        ConversationListUtil.handleGuideNewFisher(getActivity());//如果是刚注册进来的用户，有个popWindows引导框
        ConversationListUtil.createMenuPopupWindow(getActivity(), popWindow, popListView);
    }

    @Override
    protected void initData() {
        super.initData();
        this.datas = new ArrayList<>();
        this.conversationListUtil = new ConversationListUtil(getActivity(), datas);
        conversationListUtil.setConversationCallBack(this);
        conversationRecyclerItemAdapter = new ConversationRecyclerItemAdapter(getActivity(), mRecyclerView,
                datas, conversationListUtil);
        mRecyclerView.setAdapter(conversationRecyclerItemAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.myConversationListener = new MyConversationListener(datas, this, conversationListUtil);
        this.myConversationChangeListener = new MyConversationChangeListener(datas, this,conversationListUtil);
        //To login IM
        if (userInfo.isImValid()) {
            conversationListUtil.signIn(RenheIMUtil.buildAuthParam(Long.parseLong(userInfo.getImId() + ""),
                    userInfo.getName(), userInfo.getImPwd()));
        } else {
            conversationListUtil.clientRegisterIM();
        }
        conversationListUtil.initOtherConversationItems();//初始化其他的对话（IM之外的行业头条、和聊助手）
        conversationListUtil.loadOtherConversationInfo();//从服务器获取最新的行业头条、和聊助手消息
    }

    @Override
    protected void initListener() {
        super.initListener();
        //recyclerView滑动时，暂停图片加载
        mRecyclerView.addOnScrollListener(new NewPauseOnScrollListener(ImageLoader.getInstance(), true, true));
        conversationRecyclerItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object data, int position) {
                if ((null != data && data instanceof ConversationItem)) {
                    ConversationItem conversationItem = (ConversationItem) data;
                    if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
                        Intent intent = new Intent(getActivity(), ChatMainActivity.class);//跳转到群聊页面
                        Conversation conversation = conversationItem.getConversation();
                        intent.putExtra("conversation", conversation);
                        intent.putExtra("userName", conversationItem.getNickname());
                        intent.putExtra("userFace", conversationItem.getIconUrl());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if (conversationItem.getType() == ConversationItem.SYSTEM_MSG_TYPE) { //和聊助手
                        ConversationItem.ConversationListOtherItem conversation = conversationItem.getConversationListOtherItem();
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), SystemMsgActivity.class);
                        intent.putExtra("currentSystemMsgConversation", conversation);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        conversationListUtil.handleHeLiaoHelperAction(conversationItem);
                        StatisticsUtil.statisticsCustomClickEvent(getActivity().getString(R.string.android_btn_menu2_hlzs_click), 0, "", null);
                    } else if (conversationItem.getType() == ConversationItem.TOU_TIAO_TYPE) { //头条信息
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), TouTiaoActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        conversationListUtil.handleTouTiaoAction(conversationItem);
                        StatisticsUtil.statisticsCustomClickEvent(getActivity().getString(R.string.android_btn_menu2_hytt_click), 0, "", null);
                    }
                }
            }

            @Override
            public boolean onItemLongClick(View view, Object data, int position) {
                if ((null != data && data instanceof ConversationItem)) {
                    ConversationItem conversationItem = (ConversationItem) data;
                    if (conversationItem.getType() != ConversationItem.SYSTEM_MSG_TYPE
                            && conversationItem.getType() != ConversationItem.TOU_TIAO_TYPE)
                        conversationListUtil.createDeleteDialog(conversationItem);
                }
                return true;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem addfriendsItem = menu.findItem(R.id.item_add);
        addfriendsItem.setTitle("更多");
        addfriendsItem.setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //和聊页面中 点击 加号图标弹出Popupwindow （添加人脉，创建圈子，加入圈子）
        if (null == popWindow)
            return true;
        if (popWindow.isShowing()) {
            popWindow.dismiss();
            return true;
        }
        Rect frame = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        if (null != getActivity() && getActivity() instanceof AppCompatActivity) {
            popWindow.showAtLocation(popView, Gravity.RIGHT | Gravity.TOP, 20,
                    ((AppCompatActivity) getActivity()).getSupportActionBar().getHeight() + statusBarHeight);
            new ContactsUtil(getActivity()).SyncContacts();//获取新的人脉
        }
        return true;
    }

    @Override
    protected void registerReceiver() {
        super.registerReceiver();
        loadConversationInfoReceiver = new LoadConversationInfoReceiver();
        IntentFilter loadConversationIntentFilter = new IntentFilter(Constants.BroadCastAction.LOAD_CONVERSATION_INFO_ACTION);
        getActivity().registerReceiver(loadConversationInfoReceiver, loadConversationIntentFilter);

        deleteBlackContactConversationReceiver = new DeleteBlackContactConversationReceiver();
        IntentFilter deleteBlackIntentFilter = new IntentFilter(Constants.BroadCastAction.BLOCKED_CONTACTS);
        deleteBlackIntentFilter.addAction(Constants.BroadCastAction.DELETE_CONVERSATION_BY_OPENID_CONTACTS);
        getActivity().registerReceiver(deleteBlackContactConversationReceiver, deleteBlackIntentFilter);

        //注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void unRegisterReceiver() {
        super.unRegisterReceiver();
        if (null != loadConversationInfoReceiver) {
            getActivity().unregisterReceiver(loadConversationInfoReceiver);
        }
        if (null != deleteBlackContactConversationReceiver) {
            getActivity().unregisterReceiver(deleteBlackContactConversationReceiver);
        }
        EventBus.getDefault().unregister(this);//反注册EventBus
    }

    private void addConversationListener() {
        IMEngine.getIMService(ConversationService.class).addConversationListener(myConversationListener);

        IMEngine.getIMService(ConversationService.class).addConversationChangeListener(myConversationChangeListener);
    }

    private void removeConversationListener() {
        IMEngine.getIMService(ConversationService.class).removeConversationListener(myConversationListener);
        IMEngine.getIMService(ConversationService.class).removeConversationChangeListener(myConversationChangeListener);
    }

    @Override
    public void onLoginSuccess() {

    }

    @Override
    public void loadConversationListSuccess(ArrayList<ConversationItem> conversationItems) {
        conversationRecyclerItemAdapter.addConversationItems(conversationItems);
        loadingLl.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        conversationListUtil.calculateUnreadCount();
        addConversationListener();
    }

    @Override
    public void loadConversationListFail() {
        loadingLl.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        conversationListUtil.calculateUnreadCount();
        addConversationListener();
    }

    @Override
    public void onConversationsAdd(ArrayList<ConversationItem> conversationItems) {
        conversationRecyclerItemAdapter.addFirstConversationItems(conversationItems);
        conversationListUtil.calculateUnreadCount();
    }

    @Override
    public void onConversationsRemoved(ArrayList<ConversationItem> conversationItems) {
        conversationRecyclerItemAdapter.deleteConversationItems(conversationItems);
        conversationListUtil.calculateUnreadCount();
    }

    @Override
    public void onConversationsUpdated(ArrayList<ConversationItem> conversationItems) {
        conversationRecyclerItemAdapter.updateConversationItems(conversationItems);
        conversationListUtil.calculateUnreadCount();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeConversationListener();
    }

    /**
     * eventBus 用于通知对话fragment的ListView滚动到顶部
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(TopImEvent event) {
        if (null != mRecyclerView && null != layoutManager) {
            layoutManager.scrollToPosition(mRecyclerView.getTop());
//            mRecyclerView.smoothScrollToPosition(mRecyclerView.getTop());
        }
    }

    class LoadConversationInfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            conversationListUtil.loadOtherConversationInfo();//从服务器获取最新的行业头条、和聊助手消息
        }
    }

    /**
     * 接收黑名单同步广播，删除被拉黑的好友对话
     *
     * @author wangning
     */
    class DeleteBlackContactConversationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(Constants.BroadCastAction.BLOCKED_CONTACTS)) {
                conversationListUtil.deleteBlackContact(null);
            } else if (arg1.getAction().equals(Constants.BroadCastAction.DELETE_CONVERSATION_BY_OPENID_CONTACTS)) {
                String openId = arg1.getStringExtra("deletedOpenId");
                conversationListUtil.deleteBlackContact(openId);
            }
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        ConversationListUtil.handelGuideNewFisherWindow(getActivity(), isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        ConversationListUtil.resetGuideNewFisherWindow();
        super.onDestroy();
    }
}

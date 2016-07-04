package com.itcalf.renhe.context.fragmentMain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.BaseRecyclerAdapter;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.adapter.RmqPupAdapter;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.context.room.db.AddNewMsgManager;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.eventbusbean.TopRmqEvent;
import com.itcalf.renhe.listener.NewPauseOnScrollListener;
import com.itcalf.renhe.task.RenMaiQuanTask;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2015/10/12.
 */
public class RenMaiQuanFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    //View初始化
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private View rootView;
    private RecyclerRenmaiQuanItemAdapter recyclerRenmaiQuanItemAdapter;
    private LinearLayout loadingLl;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar sendingPb;//发送人脉圈留言的进度条
    //右上角弹框
    private PopupWindow popWindow;
    private View popView;
    private ListView popListView;
    //数据初始化
    private ArrayList<MessageBoards.NewNoticeList> datasList;
    private long maxRank, minRank;// 获取更新、更多数据的最大、最小值
    private int androidPhotoType;
    private boolean onLoading = false;
    private ArrayList<MessageBoards.NewNoticeList> addDatasList;
    private boolean isNeedFreshAfterResetCache = false;//在设置里清楚缓存后，是否需要清除缓存后重新载入数据
//    private long lastRefreshTime;
    //工具初始化
    private RenMaiQuanManager renMaiQuanManager;
    private AddNewMsgManager addNewMsgManager;
    private Handler mHandler;
    private RefreshRecyclerViewItemReceiver refreshRecyclerViewItemReceiver;

    //常量初始化
    private static final int HANDLER_NOTIFY_LOAD_CACHE_SUCCESS = 0;//成功加载缓存
    private static final int HANDLER_NOTIFY_DATASETCHANGED = 1;
    private static final int HANDLER_NOTIFY_LOAD_NEW_MSG = 2;//加载新的人脉圈
    private static final int HANDLER_SHOW_LOAD_MORE = 3;//显示正在加载的footer
    private static final int HANDLER_SHOW_READY_LOAD_MORE = 4;//显示准备加载更多的footer
    private static final int HANDLER_SHOW_END = 5;//显示已到底的footer
    private static final int HANDLER_SWIPEREFRESHLAYOUT_HIDE = 6;//隐藏SwipeRefreshLayout
//    private static final int AUTO_NOTIFY_REFRESH_TIME = 20 * 60 * 1000;//如果20分钟内没有刷新过界面，则自动notifydatasetchange一次，主要用来刷新时间的显示

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//声明执行onCreateOptionsMenu方法
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.renmaiquan_recycler_layout, null);
        }
        initView();
        initData();
        initListener();
        registReceiver();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //如果20分钟内没有刷新过界面，则自动notifydatasetchange一次，主要用来刷新时间的显示
//        if (null != recyclerRenmaiQuanItemAdapter) {
//            if (lastRefreshTime == 0)
//                lastRefreshTime = System.currentTimeMillis();
//            if (System.currentTimeMillis() - lastRefreshTime > AUTO_NOTIFY_REFRESH_TIME)
//                recyclerRenmaiQuanItemAdapter.notifyDataSetChanged();
//        }
    }

    private void initView() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.renmaiquan_recycler_view);
        loadingLl = (LinearLayout) rootView.findViewById(R.id.loadingLL);
        mRecyclerView.setVisibility(View.GONE);
        loadingLl.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);//这里用线性显示 类似于list view

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.rmq_swipe_ly);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.BP_1, R.color.BP_1,
                R.color.BP_1, R.color.BP_1);
        // 设置下拉监听，当用户下拉的时候会去执行回调
        mSwipeRefreshLayout.setOnRefreshListener(this);
        sendingPb = (ProgressBar) rootView.findViewById(R.id.sending_progressbar);
        // 调整进度条距离屏幕顶部的距离
//        mSwipeRefreshLayout.setProgressViewOffset(false, 0,
//                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, getResources().getDisplayMetrics()));
        RenmaiQuanUtils.handleGuideNewFisher(getActivity());//如果是刚注册进来的用户，有个popWindows引导框

        //初始化menu菜单window
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popView = inflater.inflate(R.layout.popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        popListView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setOutsideTouchable(true);
        popListView.setAdapter(new RmqPupAdapter(getActivity()));
        RenmaiQuanUtils.createMenuPopupWindow(this, getActivity(), popWindow, popListView);
    }

    private void registReceiver() {
        if (null == getActivity())
            return;
        refreshRecyclerViewItemReceiver = new RefreshRecyclerViewItemReceiver();
        IntentFilter refreshRecyclerViewItemFilter = new IntentFilter(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_BOLOCK_ITEMS);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_UNREAD_NOTICE);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_UNREAD_NOTICE);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_UPLOAD_MSG_NOTICE);
        refreshRecyclerViewItemFilter.addAction(Constants.BrocastAction.REST_CIRCLE_MAX_MIN_RANK_ACTION);
        refreshRecyclerViewItemFilter.addAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_FRIEND_NOTICE);
        getActivity().registerReceiver(refreshRecyclerViewItemReceiver, refreshRecyclerViewItemFilter);
    }

    private void unRegisReceiver() {
        if (null == getActivity())
            return;
        if (null != refreshRecyclerViewItemReceiver)
            getActivity().unregisterReceiver(refreshRecyclerViewItemReceiver);
    }

    private void initListener() {
        //注册EventBus
        EventBus.getDefault().register(this);
        recyclerRenmaiQuanItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object data, int position) {
                if (null != data && data instanceof MessageBoards.NewNoticeList) {
                    MessageBoards.NewNoticeList newNoticeList = (MessageBoards.NewNoticeList) data;
                    //如果不是点击的未读新消息那块膏药
                    if (newNoticeList.getType() >= MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD &&
                            newNoticeList.getType() <= MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_COVER) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("result", newNoticeList);
                        bundle.putInt("position", position);
                        Intent intent = new Intent(getActivity(), TwitterShowMessageBoardActivity.class);
                        intent.putExtras(bundle);
                        getActivity().startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {//会员升级VIP提示的动态,点击跳转到升级页面
                        startActivity(new Intent(getActivity(), UpgradeActivity.class));
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                        statisticsMap.put("type", "1");
                        StatisticsUtil.statisticsCustomClickEvent(getActivity().getString(R.string.android_btn_pop_upgrade_click), 0, "", statisticsMap);
                    }
                }
            }

            @Override
            public boolean onItemLongClick(View view, Object data, int position) {
                if (null != data && data instanceof MessageBoards.NewNoticeList) {
                    MessageBoards.NewNoticeList newNoticeList = (MessageBoards.NewNoticeList) data;
                    //如果不是点击的未读新消息那块膏药
                    int type = newNoticeList.getType();
                    if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD ||
                            type == MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {
                        MessageBoards.SenderInfo senderInfo = newNoticeList.getSenderInfo();
                        if (senderInfo.getSid().equals(RenheApplication.getInstance().getUserInfo().getSid())) {//如果是自己发的人脉圈
                            RenmaiQuanUtils.createSelfDialog(newNoticeList, position, 1, -1, "", addDatasList);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        //recyclerView滑动时，暂停图片加载
        mRecyclerView.addOnScrollListener(new NewPauseOnScrollListener(ImageLoader.getInstance(), true, true));
        //滑动到底部自动加载更多
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastItems = layoutManager.findFirstVisibleItemPosition();

                if (!onLoading) {
                    if (!recyclerRenmaiQuanItemAdapter.isEnd()) {
                        if ((pastItems + visibleItemCount) >= totalItemCount) {
                            if (recyclerRenmaiQuanItemAdapter.isShowTip()) {
                                showLoadingMoreFooter();
                            }
                            onLoading = true;
                            loadRenmaiQuan(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE);
                        }
                    }
                }
            }
        });
    }

    private void initData() {
        datasList = new ArrayList<>();
        recyclerRenmaiQuanItemAdapter = new RecyclerRenmaiQuanItemAdapter(getActivity(), mRecyclerView, datasList);
        mRecyclerView.setAdapter(recyclerRenmaiQuanItemAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        if (DensityUtil.getDensity(getActivity()) < 2) {
            androidPhotoType = 2;
        } else {
            DisplayMetrics metric = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
            double phoneWidth = metric.widthPixels; // 屏幕宽度（像素）
            if (phoneWidth > 640) {
                androidPhotoType = 1;
            } else {
                androidPhotoType = 2;
            }
        }
        renMaiQuanManager = new RenMaiQuanManager(getActivity());
        addNewMsgManager = new AddNewMsgManager(getActivity());
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_NOTIFY_LOAD_CACHE_SUCCESS:
                        recyclerRenmaiQuanItemAdapter.notifyDataSetChanged();
                        loadingLl.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadRenmaiQuan(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW);
                        showLoadingMoreFooter();
                        break;
                    case HANDLER_NOTIFY_LOAD_NEW_MSG:
                        loadingLl.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(true);
                        loadRenmaiQuan(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW);
                        break;
                    case HANDLER_NOTIFY_DATASETCHANGED:
                        recyclerRenmaiQuanItemAdapter.notifyDataSetChanged();
                        break;
                    case HANDLER_SHOW_LOAD_MORE:
                        showLoadingMoreFooter();
                        break;
                    case HANDLER_SHOW_READY_LOAD_MORE:
                        showReadyLoadMoreFooter();
                        break;
                    case HANDLER_SHOW_END:
                        showEndFooter();
                        break;
                    case HANDLER_SWIPEREFRESHLAYOUT_HIDE:
                        mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        loadCache();
    }

    /**
     * 加载人脉圈缓存
     */
    private void loadCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long[] ranks = renMaiQuanManager.getManMinRank();
                maxRank = ranks[0];
                minRank = ranks[1];
                loadReadyUploadCache();// 检查是否有未上传成功的人脉圈
                MessageBoards cacheResult = renMaiQuanManager.getMessageBoardsFromCursor(minRank, maxRank, Constants.RENMAIQUAN_CONSTANTS.REQUEST_COUNT);
                if (null == cacheResult) {
                    maxRank = 0;
                    Message message = new Message();
                    message.what = HANDLER_NOTIFY_LOAD_NEW_MSG;
                    mHandler.sendMessage(message);
                } else {
                    MessageBoards.NewNoticeList[] cacheNoticeList = cacheResult.getNewNoticeList();
                    if (null != cacheNoticeList && cacheNoticeList.length > 0) {
                        for (MessageBoards.NewNoticeList newNoticeList : cacheNoticeList) {
                            datasList.add(newNoticeList);
                        }
                        maxRank = datasList.get(0).getRank();
                        minRank = datasList.get(datasList.size() - 1).getRank();
                        RenmaiQuanUtils.checkUnreadNotice(datasList);//检查是否有新的未读消息
                        Message message = new Message();
                        message.what = HANDLER_NOTIFY_LOAD_CACHE_SUCCESS;
                        mHandler.sendMessage(message);
                    } else {
                        maxRank = 0;
                        Message message = new Message();
                        message.what = HANDLER_NOTIFY_LOAD_NEW_MSG;
                        mHandler.sendMessage(message);
                    }
                }
            }
        }).start();
    }

    /**
     * 检查是否有未上传成功的人脉圈
     */
    private void loadReadyUploadCache() {
        MessageBoards uploadCacheResult = addNewMsgManager.getMessageBoardsFromCursor();
        if (null != uploadCacheResult) {
            if (null == addDatasList) {
                addDatasList = new ArrayList<>();
            }
            MessageBoards.NewNoticeList[] readyUploadcacheResultNewNoticeList = uploadCacheResult.getNewNoticeList();
            if (null != readyUploadcacheResultNewNoticeList && readyUploadcacheResultNewNoticeList.length > 0) {
                for (MessageBoards.NewNoticeList newNoticeList : readyUploadcacheResultNewNoticeList) {
                    newNoticeList.setUploadState(Constants.RENMAIQUAN_CONSTANTS.RMQ_UPLOAD_STATE_ERROR);
                    addDatasList.add(newNoticeList);
                }
            }
        }
    }

    /**
     * 从服务器加载新的人脉圈
     */
    private void loadRenmaiQuan(final String loadType) {
//        lastRefreshTime = System.currentTimeMillis();
        if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW)) {
            //刷新之后，红点消失
            Intent intent = new Intent(TabMainFragmentActivity.TAB_ICON_UNREAD_RECEIVER_ACTION);
            intent.putExtra(TabMainFragmentActivity.TAB_FLAG, 3);
            intent.putExtra(TabMainFragmentActivity.TAB_ICON_RENMAIQUAN_UNREAD_NUM, -1);
            if (null != getActivity())
                getActivity().sendBroadcast(intent);
        } else if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE) && minRank <= 0) {
            onLoading = false;
            return;
        } else if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE)) {
        }
        RenMaiQuanTask renMaiQuanTask = new RenMaiQuanTask(getActivity(), Constants.RENMAIQUAN_CONSTANTS.REQUEST_COUNT, minRank, maxRank, androidPhotoType, datasList, new RenMaiQuanTask.IRoomBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(MessageBoards result) {//接收task执行完毕后的回调，由于此方法在异步线程中，切勿在此直接执行UI相关操作
                onLoading = false;
                if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW) && null != addDatasList
                        && RenmaiQuanUtils.checkNeedAddNewMsg(datasList)) {
                    MessageBoards.NewNoticeList mNewNoticeList = datasList.isEmpty() ? null : datasList.get(0);
                    if (null == mNewNoticeList || mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                        datasList.addAll(0, addDatasList);
                    } else {
                        datasList.addAll(1, addDatasList);
                    }
                }
                RenmaiQuanUtils.checkUnreadNotice(datasList);//检查是否有新的未读消息
                if (null != result) {
                    if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW)) {
                        maxRank = result.getMaxRank();
                        minRank = result.getMinRank();

                    } else if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE)) {
                        minRank = datasList.get(datasList.size() - 1).getRank();
                        if (result.isEnd()) {
                            Message message = new Message();
                            message.what = HANDLER_SHOW_END;
                            mHandler.sendMessage(message);
                        }
                    }
                    if (!result.isEnd() && datasList.size() > 0) {
                        Message message = new Message();
                        message.what = HANDLER_SHOW_LOAD_MORE;
                        mHandler.sendMessage(message);
                    }
                    boolean isNewNoticeListNotEmpty = (null != result.getNewNoticeList() && result.getNewNoticeList().length > 0);
                    boolean isUpdateNoticeListNotEmpty = (null != result.getUpdatedNoticeList() && result.getUpdatedNoticeList().length > 0);
                    boolean isDeleteNoticeListNotEmpty = (null != result.getDeleteNoticeList() && result.getDeleteNoticeList().length > 0);

                    if (isNewNoticeListNotEmpty || isUpdateNoticeListNotEmpty || isDeleteNoticeListNotEmpty) {
                        Message message = new Message();
                        message.what = HANDLER_NOTIFY_DATASETCHANGED;
                        mHandler.sendMessage(message);
                    }
                } else {
                    if (loadType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE)) {
                        Message message = new Message();
                        message.what = HANDLER_SHOW_READY_LOAD_MORE;
                        mHandler.sendMessage(message);
                    }
                }
                Message message = new Message();
                message.what = HANDLER_SWIPEREFRESHLAYOUT_HIDE;
                mHandler.sendMessage(message);
            }
        });
        renMaiQuanTask.executeOnExecutor(Executors.newCachedThreadPool(), loadType);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegisReceiver();
        EventBus.getDefault().unregister(this);//反注册EventBus
        if (null != addDatasList) {
            addDatasList.clear();
            addDatasList = null;
        }
    }

    @Override
    public void onRefresh() {
        loadRenmaiQuan(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW);
    }

    private void showLoadingMoreFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerRenmaiQuanItemAdapter.setIsShowFooter(true);
        recyclerRenmaiQuanItemAdapter.setIsEnd(false);
        recyclerRenmaiQuanItemAdapter.setIsShowTip(false);
        recyclerRenmaiQuanItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void showReadyLoadMoreFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerRenmaiQuanItemAdapter.setIsShowFooter(false);
        recyclerRenmaiQuanItemAdapter.setIsEnd(false);
        recyclerRenmaiQuanItemAdapter.setIsShowTip(true);
        recyclerRenmaiQuanItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void showEndFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerRenmaiQuanItemAdapter.setIsShowFooter(false);
        recyclerRenmaiQuanItemAdapter.setIsEnd(true);
        recyclerRenmaiQuanItemAdapter.setIsShowTip(false);
        recyclerRenmaiQuanItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void hideFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerRenmaiQuanItemAdapter.setIsShowFooter(false);
        recyclerRenmaiQuanItemAdapter.setIsEnd(false);
        recyclerRenmaiQuanItemAdapter.setIsShowTip(false);
        recyclerRenmaiQuanItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    /**
     * 刷新界面的广播
     */
    class RefreshRecyclerViewItemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION)) {
                int position = intent.getIntExtra("position", -1);
                if (null == intent.getSerializableExtra("refreshNoticeListItem"))
                    return;
                MessageBoards.NewNoticeList refreshNoticeListItem = (MessageBoards.NewNoticeList) intent.getSerializableExtra("refreshNoticeListItem");
                if (position < 0) {
                    String objectId = refreshNoticeListItem.getContentInfo().getObjectId();
                    for (int i = 0; i < datasList.size(); i++) {
                        if (null != datasList.get(i).getContentInfo() && datasList.get(i).getContentInfo().getObjectId().equals(objectId)) {
                            position = i;
                            break;
                        }
                    }
                }
                if (position >= 0) {
                    recyclerRenmaiQuanItemAdapter.updateDatasItem(refreshNoticeListItem, position);//更新数据源
                    boolean isToReply = intent.getBooleanExtra("isToReply", false);
                    if (isToReply) {
                        recyclerRenmaiQuanItemAdapter.updateItemReplyView(position);
                    } else {
                        recyclerRenmaiQuanItemAdapter.notifyItemChanged(position);//更新item
                    }
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM)) {//删除个人留言
                int position = intent.getIntExtra("position", -1);
                if (position < 0) {
                    String objectId = intent.getStringExtra("objectId");
                    if (!TextUtils.isEmpty(objectId)) {
                        for (int i = 0; i < datasList.size(); i++) {
                            if (null != datasList && null != datasList.get(i) && null != datasList.get(i).getContentInfo()
                                    && null != datasList.get(i).getContentInfo().getObjectId()) {
                                if (datasList.get(i).getContentInfo().getObjectId().equals(objectId)) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (position >= 0) {
                    datasList.remove(position);
                    recyclerRenmaiQuanItemAdapter.notifyDataSetChanged();
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_BOLOCK_ITEMS)) {//屏蔽某人留言，删除列表中，这个人发的所有留言
                String senderSid = intent.getStringExtra("senderSid");
                for (int i = 0; i < datasList.size(); i++) {
                    if (null != datasList.get(i).getSenderInfo() && datasList.get(i).getSenderInfo().getSid().equals(senderSid)) {
                        datasList.remove(i);
                        recyclerRenmaiQuanItemAdapter.notifyItemRemoved(i);
                        recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(i, recyclerRenmaiQuanItemAdapter.getItemCount());
                    }
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_UNREAD_NOTICE)) {//有新的未读新消息
                MessageBoards.NewNoticeList mNewNoticeList = datasList.isEmpty() ? null : datasList.get(0);
                if (null == mNewNoticeList || mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                    MessageBoards.NewNoticeList newNoticeList = new MessageBoards.NewNoticeList();
                    newNoticeList.setType(MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE);
                    datasList.add(0, newNoticeList);
                    recyclerRenmaiQuanItemAdapter.notifyItemInserted(0);
                    recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(0, recyclerRenmaiQuanItemAdapter.getItemCount());
                    int fistVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();//目前可见的第一项位置
                    if (fistVisibleItemPosition == 0) {
                        mRecyclerView.scrollToPosition(0);
                    }
                } else {
                    recyclerRenmaiQuanItemAdapter.notifyItemChanged(0);
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_UNREAD_NOTICE)) {//删除新的未读新消息
                MessageBoards.NewNoticeList newNoticeList = datasList.get(0);
                if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                    datasList.remove(0);
                    recyclerRenmaiQuanItemAdapter.notifyItemRemoved(0);
                    recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(0, recyclerRenmaiQuanItemAdapter.getItemCount());
                }
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_UPLOAD_MSG_NOTICE)) {//人脉圈删对未上传成功的人脉圈进行重试
                int position = intent.getIntExtra("position", -1);
                if (null == intent.getSerializableExtra("uploadNoticeListItem"))
                    return;
                MessageBoards.NewNoticeList uploadNoticeListItem = (MessageBoards.NewNoticeList) intent.getSerializableExtra("uploadNoticeListItem");
                if (position >= 0) {
                    recyclerRenmaiQuanItemAdapter.updateDatasItem(uploadNoticeListItem, position);//更新数据源
                    recyclerRenmaiQuanItemAdapter.notifyItemChanged(position);//更新item
                }
                RenmaiQuanUtils renmaiQuanUtils = new RenmaiQuanUtils(getActivity(), datasList, addDatasList, sendingPb);
                renmaiQuanUtils.sendNewMsg(uploadNoticeListItem);//发送新留言
            } else if (intent.getAction().equals(Constants.BrocastAction.REST_CIRCLE_MAX_MIN_RANK_ACTION)) {
                isNeedFreshAfterResetCache = true;
                maxRank = 0L;
                minRank = 0L;
                if (null != datasList) {
                    datasList.clear();
                    recyclerRenmaiQuanItemAdapter.notifyDataSetChanged();
                }
                if (null != addDatasList)
                    addDatasList.clear();
            } else if (intent.getAction().equals(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_FRIEND_NOTICE)) {
                int position = intent.getIntExtra("position", -1);
                if (position >= 0) {
                    MessageBoards.NewNoticeList newNoticeList = datasList.get(position);
                    RenmaiQuanUtils.updateRenmaiQuanItemAddFriend(newNoticeList);
                    recyclerRenmaiQuanItemAdapter.updateDatasItem(newNoticeList, position);//更新数据源
                    recyclerRenmaiQuanItemAdapter.updateItemAddFriendView(position);
                }
            }
        }
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
        StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_menu1_addbtn_click), 0, "", null);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.RENMAIQUAN_REQUEST_CODE.REQUEST_CODE_ADDNEWMSG:
                if (resultCode == getActivity().RESULT_OK) {
                    if (null != data.getSerializableExtra("addNewNoticeItem")) {
                        MessageBoards.NewNoticeList addNewNoticeItem = (MessageBoards.NewNoticeList) data.getSerializableExtra("addNewNoticeItem");
                        if (null == addDatasList) {
                            addDatasList = new ArrayList<>();
                        }
                        addDatasList.add(0, addNewNoticeItem);
                        if (datasList.size() > 0) {
                            MessageBoards.NewNoticeList mNewNoticeList = datasList.get(0);
                            if (mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                                datasList.add(0, addNewNoticeItem);
                                recyclerRenmaiQuanItemAdapter.notifyItemInserted(0);
                                recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(0, recyclerRenmaiQuanItemAdapter.getItemCount());

                            } else {
                                datasList.add(1, addNewNoticeItem);
                                recyclerRenmaiQuanItemAdapter.notifyItemInserted(1);
                                recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(1, recyclerRenmaiQuanItemAdapter.getItemCount());

                            }
                        } else {
                            datasList.add(0, addNewNoticeItem);
                            recyclerRenmaiQuanItemAdapter.notifyItemInserted(0);
                            recyclerRenmaiQuanItemAdapter.notifyItemRangeChanged(0, recyclerRenmaiQuanItemAdapter.getItemCount());
                        }
                        mRecyclerView.scrollToPosition(0);
                        RenmaiQuanUtils renmaiQuanUtils = new RenmaiQuanUtils(getActivity(), datasList, addDatasList, sendingPb);
                        renmaiQuanUtils.sendNewMsg(addNewNoticeItem);//发送新动态
                    }
                    break;
                }
            default:
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isNeedFreshAfterResetCache) {
            isNeedFreshAfterResetCache = false;
            mSwipeRefreshLayout.setRefreshing(true);
            loadRenmaiQuan(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW);
        }
        RenmaiQuanUtils.handelGuideNewFisherWindow(getActivity(), isVisibleToUser);
    }

    /**
     * eventBus 用于通知人脉圈fragment的RecyclerView滚动到顶部
     *
     * @param event
     */
    public void onEventMainThread(TopRmqEvent event) {
        if (null != mRecyclerView && null != layoutManager) {
            layoutManager.scrollToPosition(mRecyclerView.getTop());
//            mRecyclerView.smoothScrollToPosition(mRecyclerView.getTop());
        }
    }

    @Override
    public void onDestroy() {
        RenmaiQuanUtils.resetGuideNewFisherWindow();
        super.onDestroy();
    }
}

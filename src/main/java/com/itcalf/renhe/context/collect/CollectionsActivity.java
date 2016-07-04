package com.itcalf.renhe.context.collect;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.BaseRecyclerAdapter;
import com.itcalf.renhe.adapter.RecyclerCollectionItemAdapter;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.listener.NewPauseOnScrollListener;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import cn.renhe.heliao.idl.collection.MyCollection;

/**
 * 查看我的收藏列表
 * Created by wangning on 2016/3/3.
 */
public class CollectionsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    //View初始化
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerCollectionItemAdapter recyclerCollectionItemAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout emptyLl;
    private TextView emptyTipTv;
    private ImageView emptyTipIv;
    //数据初始化
    private ArrayList<MyCollection.CollectionInfo> datasList;
    private int pageIndex = 0;
    private boolean onLoading = false;
    //常量
    private int ID_TASK_SEARCH_COLLECTIONS = TaskManager.getTaskId();//获取收藏列表
    private int ID_TASK_DELETE_COLLECTION = TaskManager.getTaskId();//删除收藏
    private int PAGE_REQUEST_COUNT = 20;//一页获取的数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.collection_recycler_layout);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("收藏");
        mRecyclerView = (RecyclerView) findViewById(R.id.collect_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);//这里用线性显示 类似于list view

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.collect_swipe_ly);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.BP_1, R.color.BP_1,
                R.color.BP_1, R.color.BP_1);
        // 设置下拉监听，当用户下拉的时候会去执行回调
        mSwipeRefreshLayout.setOnRefreshListener(this);
        emptyLl = (LinearLayout) findViewById(R.id.empty_ly);
        emptyTipIv = (ImageView) findViewById(R.id.empty_iv);
        emptyTipTv = (TextView) findViewById(R.id.empty_tv);
        emptyTipIv.setImageResource(R.drawable.empty_collect);
        emptyTipTv.setText(R.string.collect_empty_tip);
    }

    @Override
    protected void initData() {
        super.initData();
        datasList = new ArrayList<>();
        recyclerCollectionItemAdapter = new RecyclerCollectionItemAdapter(this, mRecyclerView, datasList);
        mRecyclerView.setAdapter(recyclerCollectionItemAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        showLoadingDialog();
        pageIndex++;
        onLoading = true;
        searchCollections(pageIndex);
    }

    @Override
    protected void initListener() {
        super.initListener();
        recyclerCollectionItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object data, int position) {
                if (null != data) {
                }
            }

            @Override
            public boolean onItemLongClick(View view, Object data, final int position) {
                MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(CollectionsActivity.this);
                materialDialog.showSelectList(R.array.conversation_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                if (position < datasList.size()) {
                                    deleteCollect(datasList.get(position));
                                    datasList.remove(position);
                                    recyclerCollectionItemAdapter.notifyItemRemoved(position);
                                    recyclerCollectionItemAdapter.notifyItemRangeChanged(position, recyclerCollectionItemAdapter.getItemCount());
                                    if (datasList.isEmpty())
                                        showEmptyView();
                                }
                                break;
                            case 1:
                                break;
                            default:
                                break;
                        }
                    }
                });
                materialDialog.show();
                return true;
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
                    if (!recyclerCollectionItemAdapter.isEnd()) {
                        if ((pastItems + visibleItemCount) >= totalItemCount) {
                            if (recyclerCollectionItemAdapter.isShowTip()) {
                                showLoadingMoreFooter();
                            }
                            onLoading = true;
                            pageIndex++;
                            searchCollections(pageIndex);
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取收藏列表
     */
    private void searchCollections(int pageIndex) {
        if (checkGrpcBeforeInvoke(ID_TASK_SEARCH_COLLECTIONS))
            grpcController.searchCollects(ID_TASK_SEARCH_COLLECTIONS, pageIndex, PAGE_REQUEST_COUNT);
    }

    private void deleteCollect(MyCollection.CollectionInfo collectionInfo) {
        if (null == collectionInfo)
            return;
        if (checkGrpcBeforeInvoke(ID_TASK_DELETE_COLLECTION)) {
            String collectionId = null;
            if (collectionInfo.getCollectionType() == MyCollection.CollectResquest.CollectionType.RENMAI) {
                collectionId = collectionInfo.getCollectionMemberInfo().getSid();
                grpcController.deleteCollect(ID_TASK_DELETE_COLLECTION, collectionInfo.getCollectionType(),
                        collectionId);
            } else if (collectionInfo.getCollectionType() == MyCollection.CollectResquest.CollectionType.QUANZI) {
                collectionId = collectionInfo.getCircleInfo().getId() + "";
                grpcController.deleteCollect(ID_TASK_DELETE_COLLECTION, collectionInfo.getCollectionType(),
                        collectionId);
            } else if (collectionInfo.getCollectionType() == MyCollection.CollectResquest.CollectionType.RENMAIQUAN) {
                collectionId = collectionInfo.getId() + "";
                grpcController.deleteRmqCollect(ID_TASK_DELETE_COLLECTION, collectionInfo.getId(), collectionInfo.getCollectionType(),
                        collectionId);
            }
        }

    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        hideLoadingDialog();
        if (type == ID_TASK_SEARCH_COLLECTIONS) {
            mSwipeRefreshLayout.setRefreshing(false);
            onLoading = false;
            if (null != result) {
                if (result instanceof MyCollection.ListCollectionsResponse) {
                    MyCollection.ListCollectionsResponse response = (MyCollection.ListCollectionsResponse) result;
                    if (pageIndex == 1) {
                        datasList.clear();
                        recyclerCollectionItemAdapter.notifyDataSetChanged();
                    }
                    datasList.addAll(response.getCollectionInfoList());
                    if (response.getCollectionInfoList().size() < PAGE_REQUEST_COUNT) {
                        showEndFooter();
                    }else {
                        showLoadingMoreFooter();
                    }
                    recyclerCollectionItemAdapter.notifyDataSetChanged();
                    if (pageIndex == 1) {
                        if (datasList.isEmpty()) {
                            showEmptyView();
                        } else {
                            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                            emptyLl.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                showEmptyView();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        if (type == ID_TASK_SEARCH_COLLECTIONS) {
            mSwipeRefreshLayout.setRefreshing(false);
            hideLoadingDialog();
            onLoading = false;
            if (pageIndex > 1 && !datasList.isEmpty()) {
                showReadyLoadMoreFooter();
            }
            ToastUtil.showToast(this, msg);
        }
    }

    @Override
    public void onRefresh() {
        searchCollections(1);
    }

    private void showLoadingMoreFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerCollectionItemAdapter.setIsShowFooter(true);
        recyclerCollectionItemAdapter.setIsEnd(false);
        recyclerCollectionItemAdapter.setIsShowTip(false);
        recyclerCollectionItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void showReadyLoadMoreFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerCollectionItemAdapter.setIsShowFooter(false);
        recyclerCollectionItemAdapter.setIsEnd(false);
        recyclerCollectionItemAdapter.setIsShowTip(true);
        recyclerCollectionItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void showEndFooter() {
        int totalItemCount = layoutManager.getItemCount();
        recyclerCollectionItemAdapter.setIsShowFooter(false);
        recyclerCollectionItemAdapter.setIsEnd(true);
        recyclerCollectionItemAdapter.setIsShowTip(false);
        recyclerCollectionItemAdapter.notifyItemChanged(totalItemCount - 1);
    }

    private void showEmptyView() {
        mSwipeRefreshLayout.setVisibility(View.GONE);
        emptyLl.setVisibility(View.VISIBLE);
    }
}

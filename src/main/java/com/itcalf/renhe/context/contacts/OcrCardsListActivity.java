package com.itcalf.renhe.context.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.OcrCardsListAdapter;
import com.itcalf.renhe.bean.NewFriendsListBean;
import com.itcalf.renhe.bean.OcrCardListBean;
import com.itcalf.renhe.bean.OcrColorCard;
import com.itcalf.renhe.bean.OcrLocalCard;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.OcrCardOperation;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.OcrLocalCardUtil;
import com.itcalf.renhe.utils.PinyinUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.ocrsdk.uploadSdk.OcrBackCards;
import cn.ocrsdk.uploadSdk.OcrCard;
import cn.ocrsdk.uploadSdk.OcrErrorCode;
import cn.ocrsdk.uploadSdk.OcrServer;

/**
 * @author wangning
 * @createtime 2015-11-4
 * @功能说明 识别中的名片页面
 */

public class OcrCardsListActivity extends BaseActivity {
    private ListView ocrCardsListView;
    private View mFooterView;
    private RelativeLayout rootRl;
    private LinearLayout noDataLl;

    private OcrCardsListAdapter sAdapter;
    private List<OcrColorCard> cardlist;//脉可寻 名片列表
    private String[] cardsArray;
    private OcrServer maiKeXunServer;
    private boolean isMaiKeXunInit;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler;

    private static final int DETAIL_REQUEST_CODE = 100;//查看详情
    /**
     * 用来标示是否成功加载了列表数据，因为在onbackPress里会调用setresult，
     * 如果没有成功加载列表，直接按返回键，上级activity的onactivityResult收到的array是空，会隐藏正在识别的名片
     */
    private boolean flag = false;

    /**
     * 获取新的好友接口返回值，当该列表页是从拍名片之后直接跳转过来的时候，按返回键，跳转到新的好友界面时，
     * 避免新的好友界面再次调用接口，把该返回结果直接带到新的好友界面
     */
    private NewFriendsListBean newFriendsListBean;

    /**
     * 用来标示该页面是否是从拍名片后直接跳转过来的，如果是true，按返回键，跳转到新的好友界面
     */
    private boolean isFromTakeCard = false;

    private HashMap<String, Integer> avatarColorMap = new HashMap<>();

    private int requestCount = 0;

    /**
     * 被删除的名片列表，用途：由于是用定时器每3秒从循环从脉可寻取名片列表，在删除名片的时候如果正好在3秒这个时间段，
     * 有可能会出现被删除的又被加载出来
     */
    private List<String> deletedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.ocr_unhandle_card_list);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        ocrCardsListView = (ListView) findViewById(R.id.card_list);

        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        mFooterView.setVisibility(View.GONE);
        noDataLl = (LinearLayout) findViewById(R.id.no_data_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "识别中的名片");
        maiKeXunServer = RenheApplication.getInstance().getMaiKeXunServer();//脉可寻 上传名片server
        //判断是否通过验证
        isMaiKeXunInit = (null != maiKeXunServer && maiKeXunServer.isAuth());//账户是否初始化（key，secret）
//        fadeUitl = new FadeUitl(this, "加载中...");
//        fadeUitl.addFade(rootRl);
        cardlist = new ArrayList<>();
        sAdapter = new OcrCardsListAdapter(this, cardlist);
//        ocrCardsListView.addFooterView(mFooterView, null, false);
        ocrCardsListView.setAdapter(sAdapter);
        // 设置下拉监听，当用户下拉的时候会去执行回调
//        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                mSwipeRefreshLayout.setRefreshing(true);
//            }
//        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ocrCardsListView.setVisibility(View.VISIBLE);
                        noDataLl.setVisibility(View.GONE);
                        sAdapter.notifyDataSetChanged();
                        break;
                    case 2:
                        if (sAdapter.getCount() <= 0) {
                            ocrCardsListView.setVisibility(View.GONE);
                            noDataLl.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 3:
//                        fadeUitl.removeFade(rootRl);
                        ocrCardsListView.setVisibility(View.GONE);
                        noDataLl.setVisibility(View.VISIBLE);
                        break;
                    case 4:
//                        fadeUitl.removeFade(rootRl);
                        break;
                }
                return true;
            }
        });
        isFromTakeCard = getIntent().getBooleanExtra("isFromTakeCard", false);
        getLocalCardList();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFooterView(1);
            }
        });

        ocrCardsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MobclickAgent.onEvent(OcrCardsListActivity.this, getString(R.string.ocl_cards_see_detail));
                String uuid;
                if (null != cardlist.get(position).getOcrCard()) {
                    uuid = cardlist.get(position).getOcrCard().carduuid;
                } else {
                    uuid = cardlist.get(position).getOcrLocalCard().getCarduuid();
                }
                Intent intent = new Intent(OcrCardsListActivity.this, OcrCardDetailActivity.class);
                intent.putExtra("uuid", uuid);
                intent.putExtra("ocrLocalCard", cardlist.get(position).getOcrLocalCard());
                startHlActivityForResult(intent, DETAIL_REQUEST_CODE);
            }
        });

        ocrCardsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                createCustomDialog(OcrCardsListActivity.this, cardlist.get(position));
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != timer) {
            timer.cancel();//销毁定时器
        }
        if (null != avatarColorMap)
            avatarColorMap.clear();
    }

    /**
     * 显示列表底部 1，加载中；2，查看更多；3，已经到底
     *
     * @param show
     */
    private void toggleFooterView(int show) {
        mFooterView.setVisibility(View.VISIBLE);
        switch (show) {
            case 0:
                mFooterView.setVisibility(View.GONE);
                break;
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

    /**
     * 获取脉可寻名片数据
     */
    private void getCards(String[] cardsArray) {
        if (null != cardsArray && cardsArray.length > 0) {
            if (isMaiKeXunInit) {
                maiKeXunServer.getDataWithUUID(cardsArray, new OcrBackCards() {
                    @Override
                    public void onBack(int code, String info, OcrCard[] cards) {
                        Message msg = new Message();
                        msg.what = 4;
                        handler.sendMessage(msg);
                        if (code == OcrErrorCode.CODE_SUCCESS) {
                            if (null != cards && cards.length > 0) {
                                Logger.d("获取数据成功！");
//                                cardlist.clear();
                                flag = true;
                                for (OcrCard ocrCard : cards) {
                                    OcrColorCard ocrColorCard = getOcrColorCard(ocrCard.carduuid);
                                    if (null == ocrColorCard) {
                                        ocrColorCard = new OcrColorCard();
                                        if (null == avatarColorMap.get(ocrCard.carduuid)) {
                                            ocrColorCard.setAvatarbgIndex(PinyinUtil.getAvatarbgIndex());
                                            avatarColorMap.put(ocrCard.carduuid, ocrColorCard.getAvatarbgIndex());
                                        } else {
                                            ocrColorCard.setAvatarbgIndex(avatarColorMap.get(ocrCard.carduuid));
                                        }
                                        ocrColorCard.setOcrCard(ocrCard);
                                        OcrLocalCard ocrLocalCard = new OcrLocalCard();
                                        ocrColorCard.setOcrLocalCard(ocrLocalCard);
                                        cardlist.add(ocrColorCard);
                                        updateLocalCard(ocrColorCard.getOcrLocalCard(), ocrCard);
                                    } else {
                                        ocrColorCard.setOcrCard(ocrCard);
                                        updateLocalCard(ocrColorCard.getOcrLocalCard(), ocrCard);
                                    }
                                }
                                if (cardlist.size() > 0) {
                                    for (int i = 0; i < cardlist.size(); i++) {
                                        if (null != cardlist.get(i).getOcrCard() && isInDeleteList(cardlist.get(i).getOcrCard().carduuid)) {
                                            cardlist.remove(i);
                                        }
                                    }
                                    sort();
                                    Message message = new Message();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                }
                            }
//                            else {
//                                Logger.w("获取的数据成功，但为空！");
//                                Message message = new Message();
//                                message.what = 2;
//                                handler.sendMessage(message);
//                            }
                        }
//                        else {
//                            Logger.e("脉可寻获取数据失败！错误信息是=====》 " + info);
//                            Message message = new Message();
//                            message.what = 2;
//                            handler.sendMessage(message);
//                        }
                    }
                });
            } else {
                Logger.e("脉可寻未验证！");
//                Message message = new Message();
//                message.what = 3;
//                handler.sendMessage(message);
            }
        }
//        else {
//            Message message = new Message();
//            message.what = 2;
//            handler.sendMessage(message);
//        }
    }

    private void updateLocalCard(OcrLocalCard ocrLocalCard, OcrCard ocrCard) {
        if (null != ocrLocalCard && null != ocrCard) {
            ocrLocalCard.setSid(RenheApplication.getInstance().getUserInfo().getSid());
            ocrLocalCard.setCarduuid(ocrCard.carduuid);
            ocrLocalCard.setCreatetime(ocrCard.createtime);
            ocrLocalCard.setAddress(ocrCard.address);
            ocrLocalCard.setAudit(ocrCard.audit);
            ocrLocalCard.setCname(ocrCard.cname);
            ocrLocalCard.setDuty(ocrCard.duty);
            ocrLocalCard.setEmail(ocrCard.email);
            ocrLocalCard.setFax(ocrCard.fax);
            ocrLocalCard.setFields(ocrCard.fields);
            ocrLocalCard.setFlag(ocrCard.flag);
            ocrLocalCard.setLogo(ocrCard.logo);
            ocrLocalCard.setMobile1(ocrCard.mobile1);
            ocrLocalCard.setMobile2(ocrCard.mobile2);
            ocrLocalCard.setName(ocrCard.name);
            ocrLocalCard.setTel1(ocrCard.tel1);
            ocrLocalCard.setTel2(ocrCard.tel2);
            ocrLocalCard.setUpdatetime(ocrCard.updatetime);
            ocrLocalCard.setWebsite(ocrCard.website);
            OcrLocalCardUtil.save(ocrLocalCard);
        }
    }

    private OcrColorCard getOcrColorCard(String uuid) {
        for (OcrColorCard ocrColorCard : cardlist) {
            if (null != ocrColorCard.getOcrLocalCard() && ocrColorCard.getOcrLocalCard().getCarduuid().equals(uuid)) {
                return ocrColorCard;
            }
            if (null != ocrColorCard.getOcrCard() && ocrColorCard.getOcrCard().carduuid.equals(uuid)) {
                return ocrColorCard;
            }
        }
        return null;
    }

    private boolean isInDeleteList(String duuid) {
        if (null != deletedList) {
            for (String uuid : deletedList) {
                if (duuid.equals(uuid))
                    return true;
            }
        }
        return false;
    }
    /**
     * 定时器定时获取还未失败完成的脉可寻名片数据
     */
//    private void getCardsByTimer(String[] mCardsArray) {
//        if (isMaiKeXunInit) {
//            maiKeXunServer.getDataWithUUID(mCardsArray, new OcrBackCards() {
//                //            maiKeXunServer.getDataWithTime(0, new OcrBackCards() {
//                @Override
//                public void onBack(int code, String info, OcrCard[] cards) {
//                    if (code == OcrErrorCode.CODE_SUCCESS) {
//                        if (null != cards && cards.length > 0) {
//                            Logger.d("获取数据成功！");
//                            if (null == tempList)
//                                tempList = new ArrayList<>();
//                            else
//                                tempList.clear();
//                            for (OcrCard ocrCard : cards) {
//                                if (ocrCard.audit <= 1) {
//                                    if (!ocrCard.fields.equals("100")) {//已完成字段，"100"为全部完成
//                                        tempList.add(ocrCard);
//                                    } else {
//                                        changeOcrCardState(ocrCard);
//                                    }
//                                } else {
//                                    changeOcrCardState(ocrCard);
//                                }
//                            }
//                            if (null != tempList && tempList.size() > 0) {
//                                cardsArray = new String[tempList.size()];
//                                for (int i = 0; i < cardsArray.length; i++) {
//                                    cardsArray[i] = tempList.get(i).carduuid;
//                                }
//                            } else {
//                                if (null != timer) {
//                                    timer.cancel();//销毁定时器
//                                }
//                            }
//                            if (cardlist.size() > 0) {
//                                Message message = new Message();
//                                message.what = 1;
//                                handler.sendMessage(message);
//                            }
//                        } else {
//                            Logger.w("获取的数据成功，但为空！");
//                        }
//                    } else {
//                        Logger.e("脉可寻获取数据失败！错误信息是=====》 " + info);
//                    }
//                }
//            });
//        } else {
//            Logger.e("脉可寻未验证！");
//        }
//    }

    /**
     * 获取的名片列表，根据创建时间倒序排列
     */
    private void sort() {
        Collections.sort(cardlist, new Comparator<OcrColorCard>() {
            @Override
            public int compare(OcrColorCard lhs, OcrColorCard rhs) {
                int value = 0;
                long lhsTime, rhsTime;
                if (null == lhs.getOcrCard() || null == rhs.getOcrCard()) {
                    return 0;
                }
                lhsTime = lhs.getOcrCard().createtime;
                rhsTime = rhs.getOcrCard().createtime;
                if (lhsTime < rhsTime)
                    value = 1;
                else if (lhsTime > rhsTime)
                    value = -1;
                else
                    value = 0;
                return value;

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case DETAIL_REQUEST_CODE:
                    if (null != data.getStringExtra("deletedUuid")) {
                        String deletedUuid = data.getStringExtra("deletedUuid");
                        if (!TextUtils.isEmpty(deletedUuid))
                            deleteOcrCardByuuid(deletedUuid);
                    }
                    break;
            }

        } else if (resultCode == Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE) {
            setResult(Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE);
            finish();
        }
    }

    private void startTaskTimer() {
        Logger.e("startTaskTimer");
        timerTask = new TimerTask() {
            @Override
            public void run() {
                requestCount++;
//                if (requestCount > Constants.MAI_KE_XUN.CARD_LIST_REQUEST_COUNT) {
//                    timer.cancel();
//                }
                getCardList();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, Constants.MAI_KE_XUN.TIMER_DELAY);//开启定时器,3s后执行，每8秒执行一次
    }

    private void changeOcrCardState(OcrCard ocrCard) {
        if (null != cardlist && cardlist.size() > 0) {
            for (int i = 0; i < cardlist.size(); i++) {
                OcrColorCard ocrColorCard = cardlist.get(i);
                if (ocrCard.carduuid.equals(ocrColorCard.getOcrCard().carduuid)) {
                    ocrColorCard.setOcrCard(ocrCard);
                    cardlist.set(i, ocrColorCard);
                    break;
                }
            }
        }
    }

    private void deleteOcrCardByuuid(String uuid) {
        if (null == deletedList) {
            deletedList = new ArrayList<>();
        }
        deletedList.add(uuid);
        if (null != cardlist && cardlist.size() > 0) {
            for (int i = 0; i < cardlist.size(); i++) {
                OcrColorCard ocrColorCard = cardlist.get(i);
                if (null != ocrColorCard.getOcrCard()) {
                    if (uuid.equals(ocrColorCard.getOcrCard().carduuid)) {
                        cardlist.remove(i);
                        if (cardlist.size() <= 0) {
                            ocrCardsListView.setVisibility(View.GONE);
                            noDataLl.setVisibility(View.VISIBLE);
                        } else {
                            sAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                } else {
                    if (uuid.equals(ocrColorCard.getOcrLocalCard().getCarduuid())) {
                        cardlist.remove(i);
                        if (cardlist.size() <= 0) {
                            ocrCardsListView.setVisibility(View.GONE);
                            noDataLl.setVisibility(View.VISIBLE);
                        } else {
                            sAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isFromTakeCard) {
//            Intent intent = new Intent();
//            intent.setClass(this, NewFriendsAct.class);
//            intent.putExtra("newFriendsListBean", newFriendsListBean);
//            startActivity(intent);
//            overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);
            finish();
        } else {
//                String[] mCardsArray = null;
//                if (null != cardlist && cardlist.size() > 0) {
//                    mCardsArray = new String[cardlist.size()];
//                    for (int i = 0; i < mCardsArray.length; i++) {
//                        mCardsArray[i] = cardlist.get(i).getOcrCard().carduuid;
//                    }
//                }
//                Intent intent = new Intent();
//                intent.putExtra("cardsArray", mCardsArray);
            if (null == cardlist || cardlist.isEmpty())
                setResult(RESULT_OK);
        }

        super.onBackPressed();
    }

    private void getCardList() {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        OkHttpClientManager.postAsyn(Constants.Http.GET_CARDLIST, reqParams, OcrCardListBean.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(Object response) {
                if (response != null) {
                    if (response instanceof OcrCardListBean) {
                        OcrCardListBean ocrCardListBean = (OcrCardListBean) response;
                        switch (ocrCardListBean.getState()) {
                            case 1:
                                cardsArray = ocrCardListBean.getCardList();
                                if (null != cardsArray) {
                                    getCards(cardsArray);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
    }

    private void getLocalCardList() {
        List<OcrLocalCard> ocrLocalCards = OcrLocalCardUtil.findAllCards();
        for (OcrLocalCard ocrLocalCard : ocrLocalCards) {
            OcrColorCard ocrColorCard = new OcrColorCard();
            ocrColorCard.setOcrLocalCard(ocrLocalCard);
            if (null == avatarColorMap.get(ocrLocalCard.getCarduuid())) {
                ocrColorCard.setAvatarbgIndex(PinyinUtil.getAvatarbgIndex());
                avatarColorMap.put(ocrLocalCard.getCarduuid(), ocrColorCard.getAvatarbgIndex());
            } else {
                ocrColorCard.setAvatarbgIndex(avatarColorMap.get(ocrLocalCard.getCarduuid()));
            }
            cardlist.add(ocrColorCard);
        }
        Message message = new Message();
        if (cardlist.size() > 0) {
            message.what = 1;
        } else {
            message.what = 3;
        }
        handler.sendMessage(message);
        startTaskTimer();
    }

    /**
     * 删除名片
     *
     * @param ocrColorCard 需要上传名片的uuid
     */
    private void deleteOcrCard(final OcrColorCard ocrColorCard, final String cardUuid) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("uuid", cardUuid);
        reqParams.put("type", 2);//1.表示上传，2.表示删除
        OkHttpClientManager.postAsyn(Constants.Http.UPLOADORDELETE_OCR_CARD, reqParams, OcrCardOperation.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
            }

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(Object response) {
                if (null != response && response instanceof OcrCardOperation) {
                    OcrCardOperation result = (OcrCardOperation) response;
                    if (result.getState() == 1) {
                        Logger.d("名片删除成功！uuid是==》 " + cardUuid);
                        OcrLocalCardUtil.delete(cardUuid);
                    }
                }
            }

            @Override
            public void onAfter() {
                super.onAfter();
            }
        });
    }

    /**
     * @param context
     */
    public void createCustomDialog(Context context, final OcrColorCard ocrColorCard) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(R.array.conversation_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        MobclickAgent.onEvent(OcrCardsListActivity.this, getString(R.string.ocl_cards_delete));
                        if (null != ocrColorCard.getOcrCard()) {
                            deleteOcrCardByuuid(ocrColorCard.getOcrCard().carduuid);
                            deleteOcrCard(ocrColorCard, ocrColorCard.getOcrCard().carduuid);
                        } else {
                            deleteOcrCardByuuid(ocrColorCard.getOcrLocalCard().getCarduuid());
                            deleteOcrCard(ocrColorCard, ocrColorCard.getOcrLocalCard().getCarduuid());
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
    }
}

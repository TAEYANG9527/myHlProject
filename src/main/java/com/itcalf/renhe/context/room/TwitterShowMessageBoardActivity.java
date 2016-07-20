package com.itcalf.renhe.context.room;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.LikedPicGridAdapter;
import com.itcalf.renhe.adapter.ReplyListAdapter;
import com.itcalf.renhe.adapter.RoomRemotePicGridAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendTask;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.DeleteMsgTask;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.room.ViewFullMessageBoardTask.IDataBack;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.dto.AddFriend;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.ContentInfo;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.LikedList;
import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.dto.MessageBoards.ReplyList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;
import com.itcalf.renhe.dto.ReplyMessageBoard;
import com.itcalf.renhe.dto.ReplyUnMessageBoard;
import com.itcalf.renhe.dto.TextSize;
import com.itcalf.renhe.dto.ViewFullMessageBoard;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.DraftUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.MatrixUtil;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.TransferUrl2Drawable;
import com.itcalf.renhe.view.ActionItem;
import com.itcalf.renhe.view.DetailTitlePopup;
import com.itcalf.renhe.view.SharePopupWindow;
import com.itcalf.renhe.view.TextViewFixTouchConsume;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.view.WebViewForIndustryCircle;
import com.itcalf.renhe.view.emoji.EmojiFragment;
import com.itcalf.renhe.view.emoji.EmojiUtil;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.itcalf.widget.scrollview.ScrollViewX;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 由人脉圈点击RecyclerView的item跳转过来的
 * Feature: 显示留言内容界面 Desc:留言内容界面
 *
 * @author xp
 */

public class TwitterShowMessageBoardActivity extends BaseActivity implements EmojiFragment.OnEmotionSelectedListener {
    private Map<String, Object> map;
    private ViewHolder viewHolder;
    private SharedPreferences userInfo;
    public static final int GOOD_LIST_MAX_NUM = 20;
    public int PIC_MAX_WIDTH = 680;// 客厅显示图片区域，最大宽高是680px

    private boolean isLiked = false;
    private int likeNumber = 0;
    private int replyNum = 0;
    private String mObjectId;
    private int mId;
    private String replyObjectId;
    private int replyId;
    private String reSenderSid;
    private String reSenderMemberName;
    private String mSid;
    private String mUserFaceUrl;
    private String mUserCompany;
    private String mUserJob;
    private String mContent;

    private ScrollViewX scrollView;
    public TextView deletedTv;

    private CheckBox toForwardCb;
    private EditText replyEt;
    private TextView leftReplyNumTv;
    private ImageButton goReplyIb;
    private RelativeLayout rootRl;
    private LinearLayout bottomReplyLl;
    private static int TOTAL_REPLY_NUMBER = TextSize.getInstance().getRenMaiQuanCommentSize();// 评论最多输入140个字
    private ListView replyListView;
    /***********
     * 表情部分
     *****************/
    private ImageView imagefaceIv;
    private LinearLayout chat_face_container;
    private EmojiFragment emotionFragment;
    private int emotionHeight;
    private EmojiUtil emojiUtil;
    /***********
     * 表情部分END
     *****************/
    // 列表底部
    private RelativeLayout mFooterView;
    private RelativeLayout mFooterViewMore;
    private RelativeLayout mFooterViewIng;
    private ReplyListAdapter mSimpleAdapter;
    // 列表数据
    private List<Map<String, Object>> mData;
    private String[] mFrom = new String[]{"titleTv", "infoTv", "timeTv"};
    private int[] mTo = new int[]{R.id.titleTv, R.id.infoTv, R.id.timeTv};
    private int mStart;
    private int type;
    private int mCount = 20;
    public final static int LOAD_TYPE_FROM_CONTENT = 1;
    public final static int LOAD_TYPE_FROM_FORWARD = 2;
    public final static int LOAD_TYPE_FROM_NOTICE = 3;
    private int loadType = LOAD_TYPE_FROM_CONTENT;
    private View seperateLineView;
    // 转发按钮
    private LinearLayout mForwordLl;
    private LinearLayout shareLl;
    private NewNoticeList newNoticeListItem;
    private NewNoticeList forwardNewNoticeListItem;
    private NewNoticeList noticeNewNoticeListItem;

    private RelativeLayout replyAndGoodRlFooterLayoutIngRl;
    private int DEFAULT_IMAGE;
    private int androidPhotoType;
    private GridView likedGridView;
    private ViewFullMessageBoard.LikedList[] likedList;
    private boolean isSelf = false;
    private RequestDialog requestDialog;
    private Dialog mAlertDialog;
    private FadeUitl fadeUitl;
    private static final int COVER_WIDTH = 400;//封面图片的长宽

    private ProgressBar replyProgressBar;
    private TextView sourceTv;
    private String toForwardContent;
    private String toFrowardPic;
    private int shareType = 0;
    private String shareUrl = "";
    private String sharePicUrl = "";
    private double phoneWidth;
    private static final int LOW_DENSITY_MAX_WIDTH = 200;// 针对低分辨率（密度<=1.5）在显示更新了头像时，头像过大，将尺寸特殊处理为200*200
    private static final int HIGH_DENSITY_MAX_WIDTH = 500;//
    //评论，赞按钮收起
    private ImageView commentIv;
    private com.itcalf.renhe.view.DetailTitlePopup titlePopup;

    private boolean isShowMenuMore = false;
    private boolean isShowMenuShare = true;
    private String draftkey;
    private String shareUrlFinal;

    private NewNoticeList refreshNoticeListItem;
    private int position;//从列表页进入详情，列表中相应position
    private LinearLayout noWifiRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.twitter_rooms_show_msg);
        if (TOTAL_REPLY_NUMBER == 0) {
            TOTAL_REPLY_NUMBER = Constants.REPLYLENGTH;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("人脉圈留言详情"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("人脉圈留言详情"); // 保证 onPageEnd 在onPause 之前调用,因为
        // onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        viewHolder = new ViewHolder();
        viewHolder.contentTv = (AisenTextView) findViewById(R.id.content_txt);
        viewHolder.rawContentTv = (AisenTextView) findViewById(R.id.rawcontent_txt);
        viewHolder.nameTv = (TextView) findViewById(R.id.username_txt);
        viewHolder.avatarIv = (ImageView) findViewById(R.id.avatar_img);
        viewHolder.dateTv = (TextView) findViewById(R.id.datetime_txt);
        viewHolder.thumbnailPic = (ImageView) findViewById(R.id.thumbnailPic);
        viewHolder.forwardThumbnailPic = (ImageView) findViewById(R.id.forwardThumbnailPic);
        viewHolder.replyLl = (LinearLayout) findViewById(R.id.room_item_reply_ll);
        viewHolder.goodLl = (LinearLayout) findViewById(R.id.room_item_good_ll);
        viewHolder.goodButton = (Button) findViewById(R.id.room_item_good);
        viewHolder.replyButton = (Button) findViewById(R.id.room_item_reply);
        viewHolder.rawcontentlayout = (LinearLayout) findViewById(R.id.rawcontentlayout);
        viewHolder.mCompanyTv = (TextView) findViewById(R.id.companyTv);
        viewHolder.mIndustryTv = (TextView) findViewById(R.id.industryTv);
        viewHolder.vipIv = (ImageView) findViewById(R.id.vipImage);
        viewHolder.realNameIv = (ImageView) findViewById(R.id.realnameImage);
        viewHolder.goodIv = (ImageView) findViewById(R.id.goodiv);
        viewHolder.arrowIv = (ImageView) findViewById(R.id.arrow_iv);
        viewHolder.threeLl = (LinearLayout) findViewById(R.id.three_ll);
        viewHolder.thumbnailFl = (FrameLayout) findViewById(R.id.thumbnailFl);
        viewHolder.forwardThumbnailFl = (FrameLayout) findViewById(R.id.forwardThumbnailFl);
        viewHolder.thumbnailGridView = (GridView) findViewById(R.id.thumbnailGridview);
        viewHolder.forwardThumbnailGridView = (GridView) findViewById(R.id.forwardThumbnailGridview);
        viewHolder.replyAndGoodLl = (RelativeLayout) findViewById(R.id.reply_and_good_ll);
        viewHolder.replyAndGoodLlGoodRl = (RelativeLayout) findViewById(R.id.reply_and_good_ll_good_rl);
        viewHolder.replyListLl = (LinearLayout) findViewById(R.id.reply_list_ll);
        viewHolder.rootLl = (LinearLayout) findViewById(R.id.rootLl);
        viewHolder.topRl = (RelativeLayout) findViewById(R.id.hall_item_top_rl);
        viewHolder.goodSeperateLineView = (View) findViewById(R.id.good_sepreateView);

        viewHolder.replyAndGoodLlGoodLl = (LinearLayout) findViewById(R.id.reply_and_good_ll_good_ll);
        viewHolder.unReplyAndGoodLlGoodIv = (ImageView) findViewById(R.id.un_reply_and_good_ll_good_iv);
        viewHolder.replyAndGoodLlGoodIv = (ImageView) findViewById(R.id.reply_and_good_ll_good_iv1);
        viewHolder.goodGridIv = (ImageView) findViewById(R.id.reply_and_good_ll_good_iv);
        viewHolder.circleGoodNumberTv = (TextView) findViewById(R.id.reply_and_good_ll_good_tv);

        deletedTv = (TextView) findViewById(R.id.deleted_txt);
        scrollView = (ScrollViewX) findViewById(R.id.room_showmsg_scrollview);
        /******* 回复，评论相关 *****************/
        toForwardCb = (CheckBox) findViewById(R.id.forwardCk);
        replyEt = (EditText) findViewById(R.id.reply_edt);
        leftReplyNumTv = (TextView) findViewById(R.id.leftreply_num_tv);
        leftReplyNumTv.setText(TOTAL_REPLY_NUMBER + "");
        leftReplyNumTv.setVisibility(View.GONE);
        goReplyIb = (ImageButton) findViewById(R.id.gotoReply);
        // 表情图标
        imagefaceIv = (ImageView) findViewById(R.id.image_face);
        // 表情布局
        chat_face_container = (LinearLayout) findViewById(R.id.chat_face_container);
        /******* 回复，评论相关END **************/
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        bottomReplyLl = (LinearLayout) findViewById(R.id.bottom_reply_ll);
        replyListView = (ListView) findViewById(R.id.reply_listView);
        mFooterView = (RelativeLayout) findViewById(R.id.footer_layout);
        mFooterViewMore = (RelativeLayout) findViewById(R.id.footer_layout_more);
        mFooterViewIng = (RelativeLayout) findViewById(R.id.footer_layout_ing);

        seperateLineView = (View) findViewById(R.id.reply_and_good_ll_seperateline);
        mForwordLl = (LinearLayout) findViewById(R.id.forwordBt);
        shareLl = (LinearLayout) findViewById(R.id.room_item_share_ll);
        replyAndGoodRlFooterLayoutIngRl = (RelativeLayout) findViewById(R.id.reply_and_good_rl_footer_layout_ing);
        DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
        likedGridView = (GridView) findViewById(R.id.reply_and_good_ll_goodGridview);
        viewHolder.contentRl = (RelativeLayout) findViewById(R.id.content_Rl);

        replyProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        goReplyIb.setVisibility(View.VISIBLE);
        replyProgressBar.setVisibility(View.GONE);

        sourceTv = (TextView) findViewById(R.id.source_txt);
        viewHolder.shareContentTv = (TextViewFixTouchConsume) findViewById(R.id.share_content_tv);
        viewHolder.sharePicIv = (ImageView) findViewById(R.id.sharePic);
        viewHolder.circleSharePic = (ImageView) findViewById(R.id.circle_sharePic);
        viewHolder.addFriendTv = (com.itcalf.renhe.view.TextView) findViewById(R.id.addfriend_tv);

        commentIv = (ImageView) findViewById(R.id.commentIv);
        titlePopup = new com.itcalf.renhe.view.DetailTitlePopup(this,
                DensityUtil.dip2px(this, Constants.RenMaiQuanPOPWindowParams.DETAIL_WIDTH_MAX),
                DensityUtil.dip2px(this, Constants.RenMaiQuanPOPWindowParams.HEIGHT_MAX));
        viewHolder.forwardShareLl = (LinearLayout) findViewById(R.id.rawcontentlayout_ll);
        viewHolder.forwardShareTitle = (TextView) findViewById(R.id.dialog_title);
        viewHolder.forwardShareSeperateLine = (View) findViewById(R.id.seperate_line);
        viewHolder.forwardShareContentTv = (TextView) findViewById(R.id.forward_content_tv);
        viewHolder.forwardShareContentTv2 = (TextView) findViewById(R.id.forward_content_tv2);
        noWifiRl = (LinearLayout) findViewById(R.id.no_network_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        this.requestDialog = new RequestDialog(this, "正在删除");
        if (getDensity() < 2) {
            androidPhotoType = 2;
            PIC_MAX_WIDTH = 330;
        } else {
            androidPhotoType = 1;
        }
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        phoneWidth = metric.widthPixels; // 屏幕宽度（像素）
        //由留言正文改为动态详情
        setTextValue(R.id.title_txt, getResources().getString(R.string.dynamic_details));
        userInfo = getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        mData = new ArrayList<>();
        mSimpleAdapter = new ReplyListAdapter(TwitterShowMessageBoardActivity.this, mData, R.layout.rooms_reply_item, mFrom, mTo,
                replyListView);
        replyListView.setAdapter(mSimpleAdapter);
        if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0).getBoolean("fastdrag",
                false)) {
            replyListView.setFastScrollEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        loadType = bundle.getInt("loadType", LOAD_TYPE_FROM_CONTENT);
        position = bundle.getInt("position", -1);
        if (loadType == LOAD_TYPE_FROM_CONTENT) {
            if (null != bundle.getSerializable("result")) {
                newNoticeListItem = (NewNoticeList) bundle.getSerializable("result");
                refreshNoticeListItem = newNoticeListItem;
            }
            if (null != newNoticeListItem && newNoticeListItem.getType() == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                isShowMenuShare = false;
                invalidateOptionsMenu();
            }
            initView(newNoticeListItem);
            initViewFullMsg(LOAD_TYPE_FROM_CONTENT, type);
        } else if (loadType == LOAD_TYPE_FROM_FORWARD) {
            type = bundle.getInt("type");
            bottomReplyLl.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
            forwardNewNoticeListItem = (NewNoticeList) bundle.getSerializable("forwardNewNoticeListItem");
            if (null != forwardNewNoticeListItem) {
                mObjectId = forwardNewNoticeListItem.getContentInfo().getObjectId();
                mContent = forwardNewNoticeListItem.getContentInfo().getContent();
//                picLists = forwardNewNoticeListItem.getContentInfo().getPicList();
//                atMemmbers = forwardNewNoticeListItem.getContentInfo().getAtMembers();
                refreshNoticeListItem = forwardNewNoticeListItem;
            }
            fadeUitl = new FadeUitl(this, "加载中...");
            fadeUitl.addFade(rootRl);
            initViewFullMsg(LOAD_TYPE_FROM_FORWARD, type);
        } else if (loadType == LOAD_TYPE_FROM_NOTICE) {
            if (!TextUtils.isEmpty(bundle.getString("objectId"))) {
                mObjectId = bundle.getString("objectId");
            }
            type = bundle.getInt("type");
            fadeUitl = new FadeUitl(this, "加载中...");
            fadeUitl.addFade(rootRl);
            if (type == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                isShowMenuShare = false;
                invalidateOptionsMenu();
            }
            initViewFullMsg(LOAD_TYPE_FROM_NOTICE, type);
        }
        //表情部分初始化
        emotionFragment = (EmojiFragment) getSupportFragmentManager().findFragmentByTag("EmotionFragemnt");
        if (emotionFragment == null) {
            emotionFragment = EmojiFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.chat_face_container, emotionFragment, "EmotionFragemnt").commit();
        }
        emojiUtil = new EmojiUtil(this);
        getDraft(mObjectId, reSenderSid);
    }

    private void initView(NewNoticeList newNoticeListItem) {
        SenderInfo senderInfo = null;
        if (null != newNoticeListItem) {
            map = new HashMap<String, Object>();
            map.put("type", newNoticeListItem.getType());
            senderInfo = newNoticeListItem.getSenderInfo();
            ContentInfo contentInfo = newNoticeListItem.getContentInfo();
            if (null != senderInfo) {
                map.put("sid", senderInfo.getSid());
                map.put("name", senderInfo.getName());
                map.put("userface", senderInfo.getUserface());
                map.put("title", senderInfo.getTitle());
                map.put("company", senderInfo.getCompany());
                map.put("industry", senderInfo.getIndustry());
                map.put("location", senderInfo.getLocation());
                map.put("accountType", senderInfo.getAccountType());
                map.put("isRealName", senderInfo.isRealname());
            }
            map.put("objectId", newNoticeListItem.getContentInfo().getObjectId());
            map.put("content", contentInfo.getContent());
            toForwardContent = contentInfo.getContent();
            if (null != contentInfo) {
                map.put("Id", newNoticeListItem.getContentInfo().getId());
                ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
                if (null != forwardMessageBoardInfo) {
                    map.put("ForwardMessageBoardInfo_isForwardRenhe", forwardMessageBoardInfo.isForwardRenhe());// 是否是人和网的转发，是人和网的转发，会返回forwardMemberName、forwardMemberSId、forwardMessageBoardObjectId、forwardMessageBoardId
                    map.put("ForwardMessageBoardInfo_ObjectId", forwardMessageBoardInfo.getObjectId());// 被转发的客厅objectId
                    map.put("ForwardMessageBoardInfo_Id", forwardMessageBoardInfo.getId());// 被转发的客厅id
                    map.put("ForwardMessageBoardInfo_Name", forwardMessageBoardInfo.getName());// 转发者的姓名
                    map.put("ForwardMessageBoardInfo_Sid", forwardMessageBoardInfo.getSid());// 转发者的sid
                    map.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getContent());// 转发的内容
                    map.put("ForwardMessageBoardInfo_PicList", forwardMessageBoardInfo.getPicLists());// 图片列表信息
                    map.put("ForwardMessageBoardInfo_AtMembers", forwardMessageBoardInfo.getAtMembers());// 留言内容中@信息
                    map.put("ForwardMessageBoardInfo_Type", forwardMessageBoardInfo.getType());// 留言内容中@信息 
                    switch (forwardMessageBoardInfo.getType()) {
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                            map.put("ForwardMessageBoardInfo_id", forwardMessageBoardInfo.getWebsShare().getId() + "");// 留言内容中@信息 
                            map.put("ForwardMessageBoardInfo_Url", forwardMessageBoardInfo.getWebsShare().getUrl());// 留言内容中@信息 
                            map.put("ForwardMessageBoardInfo_PicUrl", forwardMessageBoardInfo.getWebsShare().getPicUrl());
                            map.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getWebsShare().getContent());// 转发的内容
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                            map.put("ForwardMessageBoardInfo_share_sid", forwardMessageBoardInfo.getProfileShare().getSid());// 留言内容中@信息 
                            map.put("ForwardMessageBoardInfo_share_name", forwardMessageBoardInfo.getProfileShare().getName());
                            map.put("ForwardMessageBoardInfo_share_job", forwardMessageBoardInfo.getProfileShare().getJob());// 转发的内容
                            map.put("ForwardMessageBoardInfo_share_company", forwardMessageBoardInfo.getProfileShare().getCompany());// 转发的内容
                            map.put("ForwardMessageBoardInfo_share_picUrl", forwardMessageBoardInfo.getProfileShare().getPicUrl());// 转发的内容
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
                            map.put("ForwardMessageBoardInfo_share_id", forwardMessageBoardInfo.getCircleShare().getId() + "");// 留言内容中@信息 
                            map.put("ForwardMessageBoardInfo_share_name", forwardMessageBoardInfo.getCircleShare().getName());// 留言内容中@信息 
                            map.put("ForwardMessageBoardInfo_share_note", forwardMessageBoardInfo.getCircleShare().getNote());
                            map.put("ForwardMessageBoardInfo_share_picUrl", forwardMessageBoardInfo.getCircleShare().getPicUrl());// 转发的内容
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_COMMUNAL:
                            if (null != forwardMessageBoardInfo.getCommunalShare()) {
                                map.put("ForwardMessageBoardInfo_share_name",
                                        forwardMessageBoardInfo.getCommunalShare().getName());
                                map.put("ForwardMessageBoardInfo_share_job", forwardMessageBoardInfo.getCommunalShare().getDescription());// 转发的内容
                                map.put("ForwardMessageBoardInfo_share_company",
                                        forwardMessageBoardInfo.getCommunalShare().getCompany());// 转发的内容
                                map.put("ForwardMessageBoardInfo_share_picUrl",
                                        forwardMessageBoardInfo.getCommunalShare().getPicUrl());// 转发的内容
                                map.put("ForwardMessageBoardInfo_Url", forwardMessageBoardInfo.getCommunalShare().getUrl());// 留言内容中@信息
                                map.put("ForwardMessageBoardInfo_share_title", forwardMessageBoardInfo.getCommunalShare().getTitle());// 留言内容中@信息
                            }
                            break;
                        default:
                            break;
                    }
                    if (!TextUtils.isEmpty(forwardMessageBoardInfo.getContent())) {
                        toForwardContent = forwardMessageBoardInfo.getContent();
                    } else {
                        if (null != forwardMessageBoardInfo.getPicLists() && forwardMessageBoardInfo.getPicLists().length > 0) {
                            toForwardContent = "";
                        }
                    }
                    if (null != forwardMessageBoardInfo.getPicLists() && forwardMessageBoardInfo.getPicLists().length > 0) {
                        toFrowardPic = forwardMessageBoardInfo.getPicLists()[0].getThumbnailPicUrl();
                    }
                }
                map.put("atMembers", contentInfo.getAtMembers());
                map.put("replyNum", contentInfo.getReplyNum());
                map.put("replyList", contentInfo.getReplyList());
                map.put("likedList", contentInfo.getLikedList());
                map.put("likedNumber", contentInfo.getLikedNum());
                map.put("liked", contentInfo.isLiked());
                map.put("picList", contentInfo.getPicList());// 图片列表信息
                if (null == toFrowardPic && null != contentInfo.getPicList() && contentInfo.getPicList().length > 0) {
                    toFrowardPic = contentInfo.getPicList()[0].getThumbnailPicUrl();
                }
                if (null != forwardMessageBoardInfo) {
                    switch (forwardMessageBoardInfo.getType()) {
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                            if (!TextUtils.isEmpty(forwardMessageBoardInfo.getWebsShare().getPicUrl())) {
                                toFrowardPic = forwardMessageBoardInfo.getWebsShare().getPicUrl();
                            }
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                            if (!TextUtils.isEmpty(forwardMessageBoardInfo.getProfileShare().getPicUrl())) {
                                toFrowardPic = forwardMessageBoardInfo.getProfileShare().getPicUrl();
                            }
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
                            if (!TextUtils.isEmpty(forwardMessageBoardInfo.getCircleShare().getPicUrl())) {
                                toFrowardPic = forwardMessageBoardInfo.getCircleShare().getPicUrl();
                            }
                            break;
                        case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_COMMUNAL:
                            if (!TextUtils.isEmpty(forwardMessageBoardInfo.getCommunalShare().getPicUrl())) {
                                toFrowardPic = forwardMessageBoardInfo.getCommunalShare().getPicUrl();
                            }
                            break;
                        default:
                            break;
                    }

                }
            }
            map.put("createDate", newNoticeListItem.getCreatedDate());// 人脉圈的创建毫秒数
            map.put("source", newNoticeListItem.getSource());//来源
        }
        if (null != map) {
            scrollView.setVisibility(View.VISIBLE);
            bottomReplyLl.setVisibility(View.VISIBLE);
            ImageLoader imageLoader = ImageLoader.getInstance();
            type = MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD;
            String userSid = "";
            String name = "";
            String userface = "";
            long datetime = 0;
            String company = "";
            String title = "";
            String industry = "";
            String location = "";
            int accountType = 0;
            boolean isRealName = false;
            if (null != map.get("type")) {
                type = (Integer) map.get("type");
                if (type != 1) {
                    shareLl.setVisibility(View.GONE);
                    mForwordLl.setVisibility(View.INVISIBLE);
                    titlePopup.hideShareItem();
                    titlePopup.setWidth(DensityUtil.dip2px(this, Constants.RenMaiQuanPOPWindowParams.WIDTH_MIN));
                }
            }
            if (null != map.get("sid")) {
                userSid = (String) map.get("sid");
                mSid = userSid;
                if (mSid.equals(RenheApplication.getInstance().getUserInfo().getSid()) && type == 1) {
                    if (type == 1) {
                        isSelf = true;
                        invalidateOptionsMenu();
                    }
                } else {
                    isSelf = false;
                }
            }
            if (!isSelf && type == 1) {
                isShowMenuMore = true;
                invalidateOptionsMenu();
            }
            if (null != map.get("name")) {
                name = (String) map.get("name");
            }
            if (null != map.get("userface")) {
                userface = (String) map.get("userface");
                mUserFaceUrl = userface;
            }
            if (null != map.get("createDate")) {
                datetime = (Long) map.get("createDate");
            }
            if (datetime > 0) {
                new DateUtil().string2Date(this, datetime, viewHolder.dateTv);
            } else {
                viewHolder.dateTv.setText("");
            }
            if (null != map.get("company")) {
                company = (String) map.get("company");
                mUserCompany = company;
            }
            if (null != map.get("title")) {
                title = (String) map.get("title");
                mUserJob = title;
            }
            if (null != map.get("industry")) {
                industry = (String) map.get("industry");
            }
            if (null != map.get("location")) {
                location = (String) map.get("location");
            }
            Object accountObject = map.get("accountType");
            Object realNameObject = map.get("isRealName");
            if (null != accountObject) {
                accountType = (Integer) map.get("accountType");// 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
            }
            if (null != realNameObject) {
                isRealName = (Boolean) map.get("isRealName");// 是否是实名认证的会员
            }
            String objectId = "";
            if (null != map.get("objectId")) {
                objectId = (String) map.get("objectId");
                mObjectId = objectId;
            }
            if (null != map.get("Id") && (loadType == LOAD_TYPE_FROM_CONTENT || loadType == LOAD_TYPE_FROM_NOTICE)) {
                mId = (Integer) map.get("Id");
            }
            String content = "";
            if (null != map.get("content")) {
                content = ((String) map.get("content")).trim();
                mContent = content;
            }
            viewHolder.nameTv.setText(name);
            if (null != title) {
                viewHolder.mCompanyTv.setText(title.trim());
            }
            if (null != company) {
                if (!TextUtils.isEmpty(viewHolder.mCompanyTv.getText().toString())) {
                    viewHolder.mCompanyTv.setText(viewHolder.mCompanyTv.getText().toString() + " / " + company.trim());
                } else {
                    viewHolder.mCompanyTv.setText(company.trim());
                }
            }
            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(company)) {
                viewHolder.mCompanyTv.setVisibility(View.GONE);
            } else {
                viewHolder.mCompanyTv.setVisibility(View.VISIBLE);
            }
            if (null != location && null != industry) {
                viewHolder.mIndustryTv.setText(location.trim() + " " + industry.trim());
            } else if (null != location && null == industry) {
                viewHolder.mIndustryTv.setText(location.trim() + " ");
            } else if (null == location && null != industry) {
                viewHolder.mIndustryTv.setText(industry.trim());
            } else {
                viewHolder.mIndustryTv.setText("");
            }
            switch (accountType) {
                case 0:
                    viewHolder.vipIv.setVisibility(View.GONE);
                    break;
                case 1:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_1);
                    break;
                case 2:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_2);
                    break;
                case 3:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_3);
                    break;

                default:
                    break;
            }
            if (isRealName && accountType <= 0) {
                viewHolder.realNameIv.setVisibility(View.VISIBLE);
                viewHolder.realNameIv.setImageResource(R.drawable.archive_realname);
            } else {
                viewHolder.realNameIv.setVisibility(View.GONE);
            }
            if (null != senderInfo) {
                //显示加好友的按钮
                if (senderInfo.getFriendState() != 0) {
                    viewHolder.addFriendTv.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.addFriendTv.setVisibility(View.GONE);
                }

                viewHolder.addFriendTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addFriend();
                    }
                });
            }
            int source = 0;
            if (null != map.get("source")) {
                source = (Integer) map.get("source");
            }
            sourceTv.setVisibility(View.INVISIBLE);
            viewHolder.rootLl.setBackgroundResource(R.drawable.room_list_item_bacg);
            PicList[] picList = null;
            if (!TextUtils.isEmpty(content)) {
                viewHolder.contentTv.setContent(content);
                final String mcontentFinal = content;
                if (type == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                    viewHolder.contentRl.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(TwitterShowMessageBoardActivity.this, UpgradeActivity.class));
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    });
                }
                viewHolder.contentRl.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View arg0) {
                        if (type != MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                            new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 1, mcontentFinal, refreshNoticeListItem);
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                viewHolder.contentTv.setText("");
            }

            boolean isForwardRenhe = false;
            String forward_objectId = "";
            String forward_name = "";
            String forward_sid = "";
            String forward_content = "";
            PicList[] forwardPicList = null;
            String theSharePic = null;
            if (null != map.get("ForwardMessageBoardInfo_isForwardRenhe")) {
                isForwardRenhe = (Boolean) map.get("ForwardMessageBoardInfo_isForwardRenhe");
            }
            if (null != map.get("ForwardMessageBoardInfo_ObjectId")) {
                forward_objectId = (String) map.get("ForwardMessageBoardInfo_ObjectId");
            }
            if (null != map.get("ForwardMessageBoardInfo_Name")) {
                forward_name = (String) map.get("ForwardMessageBoardInfo_Name");
            }
            if (null != map.get("ForwardMessageBoardInfo_Sid")) {
                forward_sid = (String) map.get("ForwardMessageBoardInfo_Sid");
            }
            if (null != map.get("ForwardMessageBoardInfo_Content")) {
                forward_content = ((String) map.get("ForwardMessageBoardInfo_Content")).trim();
            }
            if (null != map.get("picList")) {
                picList = (PicList[]) map.get("picList");
            }
            if (null != map.get("ForwardMessageBoardInfo_Type")) {
                shareType = (Integer) map.get("ForwardMessageBoardInfo_Type");
            }
            if (null != map.get("ForwardMessageBoardInfo_Url")) {
                shareUrl = (String) map.get("ForwardMessageBoardInfo_Url");
            }
            if (null != map.get("ForwardMessageBoardInfo_PicUrl")) {
                sharePicUrl = (String) map.get("ForwardMessageBoardInfo_PicUrl");
            }
            if (!TextUtils.isEmpty(forward_content) || shareType > 0) {
                switch (shareType) {
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                        viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
                        String shareWebContent = "";
                        String shareUrl = "";
                        String sharePicUrl = "";

                        if (null != map.get("ForwardMessageBoardInfo_Content")) {
                            shareWebContent = (String) map.get("ForwardMessageBoardInfo_Content");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_Url")) {
                            shareUrl = (String) map.get("ForwardMessageBoardInfo_Url");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_PicUrl")) {
                            sharePicUrl = (String) map.get("ForwardMessageBoardInfo_PicUrl");
                        }
                        shareUrlFinal = shareUrl;
                        theSharePic = sharePicUrl;
                        viewHolder.forwardShareLl.setVisibility(View.VISIBLE);
                        viewHolder.rawContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareTitle.setVisibility(View.GONE);
                        viewHolder.forwardShareSeperateLine.setVisibility(View.GONE);
                        viewHolder.shareContentTv.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareContentTv2.setVisibility(View.GONE);
                        viewHolder.sharePicIv.setVisibility(View.VISIBLE);
                        viewHolder.circleSharePic.setVisibility(View.GONE);
                        viewHolder.shareContentTv.setText(shareWebContent);
                        viewHolder.shareContentTv.setMaxLines(4);
                        if (!TextUtils.isEmpty(sharePicUrl)) {
                            DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE)
                                    .showImageForEmptyUri(DEFAULT_IMAGE).showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false)
                                    .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                    .build();
                            ImageLoader imageLoader2 = ImageLoader.getInstance();
                            try {
                                imageLoader2.displayImage(sharePicUrl, viewHolder.sharePicIv, options,
                                        new AnimateFirstDisplayListener());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                        viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
                        String shareProfileSid = "";
                        String shareProfileName = "";
                        String shareProfileJob = "";
                        String shareProfileCompany = "";
                        String shareProfilePicUrl = "";

                        if (null != map.get("ForwardMessageBoardInfo_share_sid")) {
                            shareProfileSid = (String) map.get("ForwardMessageBoardInfo_share_sid");
                        }
                        shareUrlFinal = shareProfileSid;
                        if (null != map.get("ForwardMessageBoardInfo_share_name")) {
                            shareProfileName = (String) map.get("ForwardMessageBoardInfo_share_name");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_job")) {
                            shareProfileJob = (String) map.get("ForwardMessageBoardInfo_share_job");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_company")) {
                            shareProfileCompany = (String) map.get("ForwardMessageBoardInfo_share_company");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_picUrl")) {
                            shareProfilePicUrl = (String) map.get("ForwardMessageBoardInfo_share_picUrl");
                        }
                        theSharePic = shareProfilePicUrl;
                        viewHolder.forwardShareLl.setVisibility(View.VISIBLE);
                        viewHolder.rawContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareTitle.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareSeperateLine.setVisibility(View.VISIBLE);
                        viewHolder.sharePicIv.setVisibility(View.GONE);
                        viewHolder.circleSharePic.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareTitle.setText(getString(R.string.vcard_share_default_name));
                        if (!TextUtils.isEmpty(shareProfileName)) {
                            viewHolder.shareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.shareContentTv.setText(shareProfileName);
                        } else {
                            viewHolder.shareContentTv.setVisibility(View.GONE);
                        }
                        viewHolder.shareContentTv.setSingleLine(true);
                        if (!TextUtils.isEmpty(shareProfileJob)) {
                            viewHolder.forwardShareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.forwardShareContentTv.setText(shareProfileJob);
                        } else {
                            viewHolder.forwardShareContentTv.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(shareProfileCompany)) {
                            viewHolder.forwardShareContentTv2.setVisibility(View.VISIBLE);
                            viewHolder.forwardShareContentTv2.setText(shareProfileCompany);
                        } else {
                            viewHolder.forwardShareContentTv2.setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(shareProfilePicUrl)) {
                            DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
                                    .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(false)
                                    .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                    .build();
                            ImageLoader imageLoader2 = ImageLoader.getInstance();
                            try {
                                imageLoader2.displayImage(shareProfilePicUrl, viewHolder.circleSharePic, options,
                                        new AnimateFirstDisplayListener());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
                        viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
                        String shareCircleId = "";
                        String shareCircleName = "";
                        String shareCircleNote = "";
                        String shareCirclePicUrl = "";
                        if (null != map.get("ForwardMessageBoardInfo_share_id")) {
                            shareCircleId = (String) map.get("ForwardMessageBoardInfo_share_id");
                        }
                        shareUrlFinal = shareCircleId;
                        if (null != map.get("ForwardMessageBoardInfo_share_name")) {
                            shareCircleName = (String) map.get("ForwardMessageBoardInfo_share_name");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_note")) {
                            shareCircleNote = (String) map.get("ForwardMessageBoardInfo_share_note");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_picUrl")) {
                            shareCirclePicUrl = (String) map.get("ForwardMessageBoardInfo_share_picUrl");
                        }
                        theSharePic = shareCirclePicUrl;
                        viewHolder.forwardShareLl.setVisibility(View.VISIBLE);
                        viewHolder.rawContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareTitle.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareSeperateLine.setVisibility(View.VISIBLE);
                        viewHolder.shareContentTv.setVisibility(View.VISIBLE);
                        viewHolder.sharePicIv.setVisibility(View.GONE);
                        viewHolder.circleSharePic.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareTitle.setText(getString(R.string.cicle_share_default_name));
                        if (!TextUtils.isEmpty(shareCircleName)) {
                            viewHolder.shareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.shareContentTv.setText(shareCircleName);
                        } else {
                            viewHolder.shareContentTv.setVisibility(View.GONE);
                        }
                        viewHolder.shareContentTv.setSingleLine(true);
                        if (!TextUtils.isEmpty(shareCircleNote)) {
                            viewHolder.forwardShareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.forwardShareContentTv.setText(shareCircleNote);
                        } else {
                            viewHolder.forwardShareContentTv.setVisibility(View.GONE);
                        }
                        viewHolder.forwardShareContentTv2.setVisibility(View.GONE);
                        if (!TextUtils.isEmpty(shareCirclePicUrl)) {
                            DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
                                    .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(false)
                                    .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                    .build();
                            ImageLoader imageLoader2 = ImageLoader.getInstance();
                            try {
                                imageLoader2.displayImage(shareCirclePicUrl, viewHolder.circleSharePic, options,
                                        new AnimateFirstDisplayListener());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_COMMUNAL:
                        viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
                        String shareCommunalName = "";
                        String shareCommunalJob = "";
                        String shareCommunalCompany = "";
                        String shareCommunalPicUrl = "";
                        String shareCommunalUrl = "";
                        String shareCommunalTitle = "";
                        if (null != map.get("ForwardMessageBoardInfo_share_name")) {
                            shareCommunalName = (String) map.get("ForwardMessageBoardInfo_share_name");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_job")) {
                            shareCommunalJob = (String) map.get("ForwardMessageBoardInfo_share_job");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_company")) {
                            shareCommunalCompany = (String) map.get("ForwardMessageBoardInfo_share_company");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_picUrl")) {
                            shareCommunalPicUrl = (String) map.get("ForwardMessageBoardInfo_share_picUrl");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_Url")) {
                            shareCommunalUrl = (String) map.get("ForwardMessageBoardInfo_Url");
                        }
                        if (null != map.get("ForwardMessageBoardInfo_share_title")) {
                            shareCommunalTitle = (String) map.get("ForwardMessageBoardInfo_share_title");
                        }
                        shareUrlFinal = shareCommunalUrl;
                        theSharePic = shareCommunalPicUrl;
                        viewHolder.forwardShareLl.setVisibility(View.VISIBLE);
                        viewHolder.rawContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareTitle.setVisibility(View.VISIBLE);
                        viewHolder.forwardShareSeperateLine.setVisibility(View.VISIBLE);
                        viewHolder.sharePicIv.setVisibility(View.GONE);
                        viewHolder.circleSharePic.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(shareCommunalTitle))
                            viewHolder.forwardShareTitle.setText(shareCommunalTitle);
                        else
                            viewHolder.forwardShareTitle.setText("赞服务");
                        if (!TextUtils.isEmpty(shareCommunalName)) {
                            viewHolder.shareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.shareContentTv.setText(shareCommunalName);
                        } else {
                            viewHolder.shareContentTv.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(shareCommunalJob)) {
                            viewHolder.forwardShareContentTv.setVisibility(View.VISIBLE);
                            viewHolder.forwardShareContentTv.setText(shareCommunalJob);
                        } else {
                            viewHolder.forwardShareContentTv.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(shareCommunalCompany)) {
                            viewHolder.forwardShareContentTv2.setVisibility(View.VISIBLE);
                            viewHolder.forwardShareContentTv2.setText(shareCommunalCompany);
                        } else {
                            viewHolder.forwardShareContentTv2.setVisibility(View.GONE);
                        }

                        if (!TextUtils.isEmpty(shareCommunalPicUrl)) {
                            DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.avatar)
                                    .showImageForEmptyUri(R.drawable.avatar).showImageOnFail(R.drawable.avatar).cacheInMemory(false)
                                    .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                    .build();
                            ImageLoader imageLoader2 = ImageLoader.getInstance();
                            try {
                                imageLoader2.displayImage(shareCommunalPicUrl, viewHolder.circleSharePic, options,
                                        new AnimateFirstDisplayListener());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        forward_content = TransferUrl2Drawable.measureLine(viewHolder.rawContentTv, forward_content,
                                TransferUrl2Drawable.getScreenWidth(this) - TransferUrl2Drawable.dipToPX(this, 115));
                        viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
                        if (null != map.get("ForwardMessageBoardInfo_PicList")) {
                            forwardPicList = (PicList[]) map.get("ForwardMessageBoardInfo_PicList");
                        }
                        viewHolder.forwardShareLl.setVisibility(View.GONE);
                        viewHolder.rawContentTv.setVisibility(View.VISIBLE);
                        viewHolder.shareContentTv.setVisibility(View.GONE);
                        viewHolder.sharePicIv.setVisibility(View.GONE);
                        viewHolder.forwardShareContentTv.setVisibility(View.GONE);
                        viewHolder.forwardShareContentTv2.setVisibility(View.GONE);
                        viewHolder.rawContentTv.setContent(forward_content);
                        break;
                }

                final boolean isForwardRenheFinal = isForwardRenhe;
                final String forwardObjectIdFinal = forward_objectId;
                final int shareTypeFinal = shareType;
                final String shareUrlFinal = shareUrl;
                if (null != forwardPicList && userInfo.getBoolean("roomshowpic", true)) {
                    if (forwardPicList.length > 1) {
                        viewHolder.forwardThumbnailFl.setVisibility(View.VISIBLE);
                        viewHolder.forwardThumbnailGridView.setVisibility(View.VISIBLE);
                        viewHolder.forwardThumbnailPic.setVisibility(View.GONE);
                        RoomRemotePicGridAdapter adapter = new RoomRemotePicGridAdapter(this, forwardPicList);
                        viewHolder.forwardThumbnailGridView.setAdapter(adapter);
                        final CharSequence[] middlePics = new CharSequence[forwardPicList.length];
                        for (int i = 0; i < forwardPicList.length; i++) {
                            middlePics[i] = forwardPicList[i].getBmiddlePicUrl();
                        }
                        if (null != middlePics && middlePics.length > 0) {
                            viewHolder.forwardThumbnailGridView.setOnItemClickListener(new OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                    MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情转发内容的图片");
                                    Intent intent = new Intent(TwitterShowMessageBoardActivity.this, ViewPhotoActivity.class);
                                    intent.putExtra("ID", arg2);
                                    intent.putExtra("middlePics", middlePics);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.zoom_enter, 0);
                                }
                            });
//                            viewHolder.forwardThumbnailGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
//                                @Override
//                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                                    new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                    return true;
//                                }
//                            });
                        }
                    } else if (forwardPicList.length == 1) {
                        viewHolder.forwardThumbnailFl.setVisibility(View.VISIBLE);
                        viewHolder.forwardThumbnailGridView.setVisibility(View.GONE);
                        viewHolder.forwardThumbnailPic.setVisibility(View.VISIBLE);

                        String thumbnailPicUrl = forwardPicList[0].getThumbnailPicUrl();
                        if (thumbnailPicUrl.indexOf("/livingroom/images/") > -1
                                || thumbnailPicUrl.indexOf("/events/logos/new/") > -1
                                || thumbnailPicUrl.indexOf("/news_images/") > -1) {
                            try {
                                if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {
                                    AsyncImageLoader.getInstance()
                                            .populateData(this, RenheApplication.getInstance().getUserInfo().getEmail(), false,
                                                    true, false)
                                            .loadPic(viewHolder.forwardThumbnailPic, null, forwardPicList[0].getThumbnailPicUrl(),
                                                    null, null, MatrixUtil.getPostMatrix((Activity) this), true);
                                }
                                if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
                                    viewHolder.forwardThumbnailPic
                                            .setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
//                                    viewHolder.forwardThumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                        @Override
//                                        public boolean onLongClick(View v) {
//                                            new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                            return true;
//                                        }
//                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            int width = forwardPicList[0].getBmiddlePicWidth();
                            int height = forwardPicList[0].getBmiddlePicHeight();
                            if (width > 0 && height > 0) {
                                if (width != 0 && height != 0) {
                                    LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
                                    viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
                                    viewHolder.forwardThumbnailPic.setAdjustViewBounds(true);
                                    viewHolder.forwardThumbnailPic.setMaxWidth(PIC_MAX_WIDTH);
                                    viewHolder.forwardThumbnailPic.setMaxHeight(PIC_MAX_WIDTH);
                                } else {
                                    LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
                                    linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
                                }

                                try {
                                    if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {

                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                                                .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                                                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                                .build();
                                        ImageLoader imageLoader2 = ImageLoader.getInstance();
                                        try {
                                            imageLoader2.displayImage(forwardPicList[0].getThumbnailPicUrl(),
                                                    viewHolder.forwardThumbnailPic, options, new AnimateFirstDisplayListener());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
                                        viewHolder.forwardThumbnailPic
                                                .setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
//                                        viewHolder.forwardThumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                            @Override
//                                            public boolean onLongClick(View v) {
//                                                new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                                return true;
//                                            }
//                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {
                                        AsyncImageLoader.getInstance()
                                                .populateData(this, RenheApplication.getInstance().getUserInfo().getEmail(),
                                                        false, true, false)
                                                .loadPic(viewHolder.forwardThumbnailPic, null,
                                                        forwardPicList[0].getThumbnailPicUrl(), null, null,
                                                        MatrixUtil.getPostMatrix((Activity) this), true);
                                    }
                                    if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
                                        viewHolder.forwardThumbnailPic
                                                .setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
//                                        viewHolder.forwardThumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                            @Override
//                                            public boolean onLongClick(View v) {
//                                                new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                                return true;
//                                            }
//                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        viewHolder.forwardThumbnailFl.setVisibility(View.GONE);
                    }
                } else {
                    viewHolder.forwardThumbnailFl.setVisibility(View.GONE);
                }
            } else {
                viewHolder.rawcontentlayout.setVisibility(View.GONE);
            }
            CharSequence[] paths = null;
            if (null != picList && userInfo.getBoolean("roomshowpic", true)) {
                if (picList.length > 1) {
                    viewHolder.thumbnailFl.setVisibility(View.VISIBLE);
                    viewHolder.thumbnailGridView.setVisibility(View.VISIBLE);
                    viewHolder.thumbnailPic.setVisibility(View.GONE);
                    RoomRemotePicGridAdapter adapter = new RoomRemotePicGridAdapter(this, picList);
                    viewHolder.thumbnailGridView.setAdapter(adapter);
                    final CharSequence[] middlePics = new CharSequence[picList.length];
                    for (int i = 0; i < picList.length; i++) {
                        middlePics[i] = picList[i].getBmiddlePicUrl();
                    }
                    if (null != middlePics && middlePics.length > 0) {
                        viewHolder.thumbnailGridView.setOnItemClickListener(new OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情正文内容的图片");
                                Intent intent = new Intent(TwitterShowMessageBoardActivity.this, ViewPhotoActivity.class);
                                intent.putExtra("ID", arg2);
                                intent.putExtra("middlePics", middlePics);
                                startActivity(intent);
                                overridePendingTransition(R.anim.zoom_enter, 0);
                            }
                        });
//                        viewHolder.thumbnailGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
//                            @Override
//                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                                new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                return true;
//                            }
//                        });
                    }
                } else if (picList.length == 1) {
                    viewHolder.thumbnailFl.setVisibility(View.VISIBLE);
                    viewHolder.thumbnailGridView.setVisibility(View.GONE);
                    viewHolder.thumbnailPic.setVisibility(View.VISIBLE);
                    viewHolder.thumbnailPic.setAdjustViewBounds(true);
                    String thumbnailPicUrl = picList[0].getThumbnailPicUrl();
                    if (thumbnailPicUrl.indexOf("/livingroom/images/") > -1 || thumbnailPicUrl.indexOf("/events/logos/new/") > -1
                            || thumbnailPicUrl.indexOf("/news_images/") > -1) {

                        ImageLoader imageLoader1 = ImageLoader.getInstance();
                        try {
                            imageLoader1.displayImage(picList[0].getThumbnailPicUrl(), viewHolder.thumbnailPic,
                                    CacheManager.imageOptions, new AnimateFirstDisplayListener());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
                            viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
//                            viewHolder.thumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                @Override
//                                public boolean onLongClick(View v) {
//                                    new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                    return true;
//                                }
//                            });
                        }
                    } else {
                        int width = picList[0].getBmiddlePicWidth();
                        int height = picList[0].getBmiddlePicHeight();
                        if (type == MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_COVER) {
                        }
                        if (width > 0 && height > 0) {
                            if (width != 0 && height != 0) {
                                FrameLayout.LayoutParams linearParams = (FrameLayout.LayoutParams) viewHolder.thumbnailPic
                                        .getLayoutParams();
                                viewHolder.thumbnailPic.setLayoutParams(linearParams);
                                if (type == MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_COVER) {
                                    viewHolder.thumbnailPic.setMaxWidth(COVER_WIDTH);
                                    viewHolder.thumbnailPic.setMaxHeight(COVER_WIDTH);
                                } else {
                                    viewHolder.thumbnailPic.setMaxWidth(PIC_MAX_WIDTH);
                                    viewHolder.thumbnailPic.setMaxHeight(PIC_MAX_WIDTH);
                                }
                                viewHolder.thumbnailPic.setAdjustViewBounds(true);
                            } else {
                                LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
                                linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                viewHolder.thumbnailPic.setLayoutParams(linearParams);
                            }

                            try {
                                if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

                                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                                            .showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                                            .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                                            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
                                    ImageLoader imageLoader1 = ImageLoader.getInstance();
                                    try {
                                        imageLoader1.displayImage(picList[0].getThumbnailPicUrl(), viewHolder.thumbnailPic,
                                                options, new AnimateFirstDisplayListener());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
                                    viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
//                                    viewHolder.thumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                        @Override
//                                        public boolean onLongClick(View v) {
//                                            new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                            return true;
//                                        }
//                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (type == MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE) {
                                if (getDensity() < 2) {

                                    viewHolder.thumbnailPic.setAdjustViewBounds(true);
                                    viewHolder.thumbnailPic.setMaxWidth(LOW_DENSITY_MAX_WIDTH);
                                } else {
                                    if (phoneWidth > 640) {
                                        viewHolder.thumbnailPic.setAdjustViewBounds(true);
                                        viewHolder.thumbnailPic.setMaxWidth(HIGH_DENSITY_MAX_WIDTH);

                                    } else {
                                        viewHolder.thumbnailPic.setAdjustViewBounds(true);
                                        viewHolder.thumbnailPic.setMaxWidth(LOW_DENSITY_MAX_WIDTH);
                                    }
                                }
                                try {
                                    if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

                                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                                .showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                                                .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                                                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
                                                .build();
                                        ImageLoader imageLoader1 = ImageLoader.getInstance();
                                        try {
                                            imageLoader1.displayImage(picList[0].getThumbnailPicUrl(), viewHolder.thumbnailPic,
                                                    options, new AnimateFirstDisplayListener());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        viewHolder.thumbnailFl.setVisibility(View.GONE);
                                        viewHolder.thumbnailGridView.setVisibility(View.GONE);
                                        viewHolder.thumbnailPic.setVisibility(View.GONE);
                                    }
                                    if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
                                        viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
//                                        viewHolder.thumbnailPic.setOnLongClickListener(new OnLongClickListener() {
//                                            @Override
//                                            public boolean onLongClick(View v) {
//                                                new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
//                                                return true;
//                                            }
//                                        });
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
                                    linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                    viewHolder.thumbnailPic.setLayoutParams(linearParams);
                                    if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {
                                        AsyncImageLoader.getInstance().populateData(TwitterShowMessageBoardActivity.this,
                                                RenheApplication.getInstance().getUserInfo().getEmail(), false, true, false)
                                                .loadPic(viewHolder.thumbnailPic, null, picList[0].getThumbnailPicUrl(), null,
                                                        null, MatrixUtil.getPostMatrix(TwitterShowMessageBoardActivity.this),
                                                        true);
                                    } else {
                                        viewHolder.thumbnailFl.setVisibility(View.GONE);
                                        viewHolder.thumbnailGridView.setVisibility(View.GONE);
                                        viewHolder.thumbnailPic.setVisibility(View.GONE);

                                    }
                                    if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
                                        if (type == MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                                            viewHolder.thumbnailPic.setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    startActivity(new Intent(TwitterShowMessageBoardActivity.this, UpgradeActivity.class));
                                                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                                }
                                            });
                                        } else {
                                            viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));

                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }

                } else {
                    viewHolder.thumbnailFl.setVisibility(View.GONE);
                }
            } else {
                viewHolder.thumbnailFl.setVisibility(View.GONE);
            }
            int replyNum = 0;
            if (null != map.get("replyNum")) {
                replyNum = (Integer) map.get("replyNum");
            }

            if (null != viewHolder.replyButton) {
                if (replyNum >= 1) {
                    viewHolder.replyButton.setText(replyNum + "");
                } else {
                    viewHolder.replyButton.setText(getResources().getString(R.string.room_reply));
                }
            }

            // 监听赞按钮单击事件
            Object favourNumberObj = map.get("likedNumber");
            Object isFavourObj = map.get("liked");
            int favourNumber = 0;
            if (null != viewHolder.replyAndGoodLlGoodLl) {
                if (null != favourNumberObj) {
                    favourNumber = (Integer) favourNumberObj;
                    if (favourNumber > 0) {
                        if (null != isFavourObj) {
                            isLiked = (Boolean) isFavourObj;
                            if (isLiked) {
                                viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.GONE);
                                viewHolder.replyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                                viewHolder.goodGridIv.setImageResource(R.drawable.good_number_icon);
                                viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                                viewHolder.circleGoodNumberTv.setText(favourNumber + "");
                                viewHolder.circleGoodNumberTv.setTextColor(this.getResources().getColor(R.color.CP));
                                titlePopup.updateGoodItem();
                            } else {
                                viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                                viewHolder.replyAndGoodLlGoodIv.setVisibility(View.GONE);
                                viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                                viewHolder.circleGoodNumberTv.setText(favourNumber + "");
                                viewHolder.circleGoodNumberTv.setTextColor(this.getResources().getColor(R.color.BC_1));
                            }
                        }
                    } else {
                        viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                        viewHolder.replyAndGoodLlGoodIv.setVisibility(View.GONE);
                        viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                        viewHolder.circleGoodNumberTv.setText("0");
                        viewHolder.circleGoodNumberTv.setTextColor(this.getResources().getColor(R.color.BC_1));
                    }
                } else {
                    viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                    viewHolder.replyAndGoodLlGoodIv.setVisibility(View.GONE);
                    viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                    viewHolder.circleGoodNumberTv.setText("0");
                    viewHolder.circleGoodNumberTv.setTextColor(this.getResources().getColor(R.color.BC_1));
                }
                viewHolder.replyAndGoodLlGoodLl
                        .setOnClickListener(new goodClick(viewHolder.replyAndGoodLlGoodLl, favourNumber, type, isLiked));
            }
            // 监听回复按钮单击事件
            if (null != viewHolder.replyButton) {
                viewHolder.replyLl.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmaiquan_detail_reply");
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                writeComment(null, null, null, 0);
                            }
                        }, 100);
                    }
                });
            }
            titlePopup.addAction(new com.itcalf.renhe.view.ActionItem(this, "赞", R.drawable.circle_comment));
            titlePopup.addAction(new com.itcalf.renhe.view.ActionItem(this, "评论", R.drawable.circle_comment));
            titlePopup.addAction(new com.itcalf.renhe.view.ActionItem(this, "分享", R.drawable.btn_icon_share));
            titlePopup.setItemOnClickListener(new PopwindowItemClickListener(viewHolder.goodLl, viewHolder.replyLl, shareLl));
            int state = 1;
            int messageboardPublicationId = -1;
            if (null != map.get("state")) {
                state = (Integer) map.get("state");
                messageboardPublicationId = (Integer) map.get("messageboardPublicationId");
            }
            final int mePublicationId = messageboardPublicationId;
            boolean likeListIsEmpty = true;
            boolean replyListIsEmpty = true;
            LikedList[] likedList = null;
            if (null != map.get("likedList")) {
                likedList = (LikedList[]) map.get("likedList");
                if (likedList.length > 0) {
                    likeListIsEmpty = false;
                } else {
                    likeListIsEmpty = true;
                }
            } else {
                likeListIsEmpty = true;
            }
            ReplyList[] replyList = null;
            if (null != map.get("replyList")) {
                replyList = (ReplyList[]) map.get("replyList");
                if (replyList.length > 0) {
                    replyListIsEmpty = false;
                } else {
                    replyListIsEmpty = true;
                }
            } else {
                replyListIsEmpty = true;
            }
            if (!TextUtils.isEmpty(userface) && null != viewHolder.avatarIv) {
                try {
                    imageLoader.displayImage(userface, viewHolder.avatarIv, CacheManager.options,
                            CacheManager.animateFirstDisplayListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final String muserSid = userSid;
                viewHolder.avatarIv.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情的用户头像");
                        Intent intent = new Intent(TwitterShowMessageBoardActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, muserSid);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });
            }
        }
    }

    private void initViewFullMsg(final int loadType, final int type) {
        new ViewFullMessageBoardTask(this, type, new IDataBack() {

            @Override
            public void onPre() {
                if (loadType > LOAD_TYPE_FROM_CONTENT) {
                    bottomReplyLl.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                }
                mFooterView.setVisibility(View.VISIBLE);
                mFooterViewMore.setVisibility(View.GONE);
                mFooterViewIng.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPost(List<Map<String, Object>> result, ViewFullMessageBoard viewFullMessageBoard) {
                replyAndGoodRlFooterLayoutIngRl.setVisibility(View.GONE);
                if (null != viewFullMessageBoard && -4 == viewFullMessageBoard.getState()) {
                    deletedTv.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    bottomReplyLl.setVisibility(View.GONE);

                    Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
                    intent.putExtra("position", position);
                    intent.putExtra("objectId", mObjectId);
                    sendBroadcast(intent);
                    new RenmaiQuanUtils(TwitterShowMessageBoardActivity.this).deleteRenmaiQuanItem(mObjectId);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            requestDialog.removeFade(rootRl);
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }
                    }, 1000);
                }

                if (null != viewFullMessageBoard && 1 == viewFullMessageBoard.getState()) {
                    if (loadType == LOAD_TYPE_FROM_FORWARD && null != viewFullMessageBoard
                            && null != viewFullMessageBoard.getSenderInfo()) {
                        mId = viewFullMessageBoard.getId();
                        if (viewFullMessageBoard.getSenderInfo().getSid()
                                .equals(RenheApplication.getInstance().getUserInfo().getSid())) {
                            if (type == 1) {
                                isSelf = true;
                                invalidateOptionsMenu();
                                //								invalidateOptionsMenu();
                            }
                        } else {
                            isSelf = false;
                        }
                        forwardNewNoticeListItem.setSenderInfo(viewFullMessageBoard.getSenderInfo());
                        forwardNewNoticeListItem.getContentInfo().setContent(viewFullMessageBoard.getContent());
                        forwardNewNoticeListItem.setCreatedDate(viewFullMessageBoard.getCreatedDateSeconds());
                        forwardNewNoticeListItem.setType(type);
                        refreshNoticeListItem = forwardNewNoticeListItem;
                    } else if (loadType == LOAD_TYPE_FROM_NOTICE) {
                        noticeNewNoticeListItem = new NewNoticeList();
                        noticeNewNoticeListItem.setCreatedDate(viewFullMessageBoard.getCreatedDateSeconds());
                        noticeNewNoticeListItem.setType(viewFullMessageBoard.getType());
                        noticeNewNoticeListItem.setSenderInfo(viewFullMessageBoard.getSenderInfo());
                        ContentInfo contentInfo = new ContentInfo();
                        contentInfo.setContent(viewFullMessageBoard.getContent());
                        contentInfo.setAtMembers(viewFullMessageBoard.getAtMembers());
                        contentInfo.setForwardMessageBoardInfo(viewFullMessageBoard.getForwardMessageBoardInfo());
                        contentInfo.setId(viewFullMessageBoard.getId());
                        contentInfo.setLiked(viewFullMessageBoard.isLiked());
                        contentInfo.setLikedNum(viewFullMessageBoard.getLikedNum());
                        contentInfo.setObjectId(viewFullMessageBoard.getObjectId());
                        contentInfo.setPicList(viewFullMessageBoard.getPicList());
                        contentInfo.setReplyNum(viewFullMessageBoard.getReplyNum());
                        noticeNewNoticeListItem.setContentInfo(contentInfo);
                        refreshNoticeListItem = noticeNewNoticeListItem;
                    }
                    mStart += mCount + 1;
                    boolean likeListIsEmpty = true;
                    boolean replyListIsEmpty = true;
                    likedList = viewFullMessageBoard.getLikedList();
                    if (null != likedList) {
                        if (likedList.length > 0) {
                            likeListIsEmpty = false;
                        } else {
                            likeListIsEmpty = true;
                        }
                    } else {
                        likeListIsEmpty = true;
                    }
                    ViewFullMessageBoard.ReplyList[] replyList = viewFullMessageBoard.getReplyList();
                    if (null != replyList) {
                        if (replyList.length > 0) {
                            replyListIsEmpty = false;
                        } else {
                            replyListIsEmpty = true;
                        }
                    } else {
                        replyListIsEmpty = true;
                    }
                    if (!likeListIsEmpty || !replyListIsEmpty) {
                        viewHolder.replyAndGoodLl.setVisibility(View.VISIBLE);
                        if (!likeListIsEmpty && !replyListIsEmpty) {
                            viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                            viewHolder.replyListLl.setVisibility(View.VISIBLE);
                            seperateLineView.setVisibility(View.VISIBLE);
                        } else if (!likeListIsEmpty && replyListIsEmpty) {
                            viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                            viewHolder.replyListLl.setVisibility(View.GONE);
                            seperateLineView.setVisibility(View.GONE);
                        } else if (likeListIsEmpty && !replyListIsEmpty) {
                            viewHolder.replyAndGoodLlGoodRl.setVisibility(View.GONE);
                            viewHolder.replyListLl.setVisibility(View.VISIBLE);
                            seperateLineView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        viewHolder.replyAndGoodLl.setVisibility(View.GONE);
                    }
                    initLikedView(likedList, viewFullMessageBoard.getLikedNum());
                    if (result.size() <= 0) {
                        viewHolder.replyListLl.setVisibility(View.GONE);
                        replyListView.setVisibility(View.GONE);
                        mFooterView.setVisibility(View.GONE);
                    } else {
                        viewHolder.replyListLl.setVisibility(View.VISIBLE);
                        replyListView.setVisibility(View.VISIBLE);
                        mFooterView.setVisibility(View.VISIBLE);
                        mFooterViewMore.setVisibility(View.VISIBLE);
                        mFooterViewIng.setVisibility(View.GONE);
                    }
                    if (mStart >= viewFullMessageBoard.getReplyNum()) {
                        mFooterView.setVisibility(View.GONE);
                    } else {
                        mFooterView.setVisibility(View.VISIBLE);
                        mFooterViewMore.setVisibility(View.VISIBLE);
                        mFooterViewIng.setVisibility(View.GONE);
                    }
                    mData.addAll(result);
                    mSimpleAdapter.notifyDataSetChanged();
                    if (loadType == LOAD_TYPE_FROM_FORWARD && null != forwardNewNoticeListItem) {
                        fadeUitl.removeFade(rootRl);
                        initView(forwardNewNoticeListItem);
                    } else if (loadType == LOAD_TYPE_FROM_NOTICE && null != noticeNewNoticeListItem) {
                        fadeUitl.removeFade(rootRl);
                        initView(noticeNewNoticeListItem);
                    }
                    replyNum = viewFullMessageBoard.getReplyNum();
                    if (viewFullMessageBoard.getReplyNum() < 1) {
                        viewHolder.replyButton.setText("评论");
                    } else {
                        viewHolder.replyButton.setText(viewFullMessageBoard.getReplyNum() + "");
                    }
                    //点赞通知
                    isLiked = viewFullMessageBoard.isLiked();
                    likeNumber = viewFullMessageBoard.getLikedNum();
                    if (likeNumber <= 0) {
                        viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                        viewHolder.replyAndGoodLlGoodIv.setVisibility(View.GONE);
                        viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                        viewHolder.circleGoodNumberTv.setText("0");
                        viewHolder.circleGoodNumberTv
                                .setTextColor(TwitterShowMessageBoardActivity.this.getResources().getColor(R.color.BC_1));
                    } else {
                        if (isLiked) {
                            viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.GONE);
                            viewHolder.replyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                            viewHolder.goodGridIv.setImageResource(R.drawable.good_number_icon);
                            viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                            viewHolder.circleGoodNumberTv.setText(likeNumber + "");
                            viewHolder.circleGoodNumberTv
                                    .setTextColor(TwitterShowMessageBoardActivity.this.getResources().getColor(R.color.CP));

                        } else {
                            viewHolder.unReplyAndGoodLlGoodIv.setVisibility(View.VISIBLE);
                            viewHolder.replyAndGoodLlGoodIv.setVisibility(View.GONE);
                            viewHolder.circleGoodNumberTv.setVisibility(View.VISIBLE);
                            viewHolder.circleGoodNumberTv.setText(likeNumber + "");
                            viewHolder.circleGoodNumberTv
                                    .setTextColor(TwitterShowMessageBoardActivity.this.getResources().getColor(R.color.BC_1));
                        }
                    }
                    viewHolder.replyAndGoodLlGoodLl
                            .setOnClickListener(new goodClick(viewHolder.replyAndGoodLlGoodLl, likeNumber, type, isLiked));

                } else {
                    seperateLineView.setVisibility(View.GONE);
                    viewHolder.replyListLl.setVisibility(View.GONE);
                    mFooterView.setVisibility(View.GONE);
                    replyListView.setVisibility(View.GONE);
                    mFooterView.setVisibility(View.GONE);
                    if (loadType > LOAD_TYPE_FROM_CONTENT) {
                        fadeUitl.removeFade(rootRl);
                    }
                }
                if (null == viewFullMessageBoard && loadType != LOAD_TYPE_FROM_CONTENT) {
                    noWifiRl.setVisibility(View.VISIBLE);
                    findViewById(R.id.bottom_reply_rll).setVisibility(View.GONE);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), mObjectId, getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), loadType, androidPhotoType, mCount);
    }

    private void initGet() {
        // 初始化异步加载留言回复列表数据
        new ReplyListTask(this, type, new ReplyListTask.IDataBack() {

            @Override
            public void onPre() {
                mFooterView.setVisibility(View.VISIBLE);
                mFooterViewMore.setVisibility(View.GONE);
                mFooterViewIng.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPost(List<Map<String, Object>> result, int replyNum) {
                if (null != result) {
                    mStart += result.size();
                    if (mStart >= replyNum) {
                        mFooterView.setVisibility(View.GONE);
                    } else {
                        mFooterView.setVisibility(View.VISIBLE);
                        mFooterViewMore.setVisibility(View.VISIBLE);
                        mFooterViewIng.setVisibility(View.GONE);
                    }
                    mData.addAll(result);
                    mSimpleAdapter.notifyDataSetChanged();
                } else {
                    mFooterView.setVisibility(View.GONE);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), mObjectId, getRenheApplication().getUserInfo().getAdSId(),
                getRenheApplication().getUserInfo().getSid(), mStart, mCount);
    }

    private void initLikedView(com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList, int replyNum) {
        if (likedList.length > 0) {
            viewHolder.goodSeperateLineView.setVisibility(View.VISIBLE);
            DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE)
                    .showImageForEmptyUri(DEFAULT_IMAGE).showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();

            if (replyNum <= GOOD_LIST_MAX_NUM) {
                viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                LikedPicGridAdapter adapter = new LikedPicGridAdapter(this, likedList, 0);
                likedGridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                final com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList2 = likedList;
                likedGridView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情的赞列表的用户头像");
                        Intent intent = new Intent(TwitterShowMessageBoardActivity.this, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, likedList2[arg2].getSid());
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }

                });
            } else {
                viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                LikedPicGridAdapter adapter = new LikedPicGridAdapter(this, likedList, replyNum);
                likedGridView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList3 = new com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[likedList.length
                        + 1];
                if (likedList.length <= GOOD_LIST_MAX_NUM) {
                    for (int i = 0; i < likedList.length; i++) {
                        likedList3[i] = likedList[i];
                    }
                } else {
                    for (int i = 0; i < GOOD_LIST_MAX_NUM; i++) {
                        likedList3[i] = likedList[i];
                    }
                }

                likedList3[likedList3.length - 1] = new com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList();
                final com.itcalf.renhe.dto.ViewFullMessageBoard.LikedList[] likedList2 = likedList3;
                likedGridView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (arg2 < GOOD_LIST_MAX_NUM) {
                            Intent intent = new Intent(TwitterShowMessageBoardActivity.this, MyHomeArchivesActivity.class);
                            intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, likedList2[arg2].getSid());
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        }
                    }
                });
            }
        } else if (likedList.length <= 0) {
            viewHolder.goodSeperateLineView.setVisibility(View.GONE);
            viewHolder.replyAndGoodLlGoodRl.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem share = menu.findItem(R.id.item_share);
        share.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //已改动，增加分享功能
        if (isShowMenuShare)
            share.setVisible(true);
        else
            share.setVisible(false);
        MenuItem moreItem = menu.findItem(R.id.menu_circle_more);
        moreItem.setTitle("更多");
        if (isShowMenuMore) {
            moreItem.setVisible(true);
        }
        if (isSelf) {
            MenuItem delete = menu.findItem(R.id.item_delete);
            delete.setVisible(true);
        } else {
            MenuItem delete = menu.findItem(R.id.item_delete);
            delete.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmai_detail_share");
                shareLl.performClick();
                return true;
            case R.id.item_delete:
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmai_detail_delete");
                MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(TwitterShowMessageBoardActivity.this);
                materialDialogsUtil.getBuilder(R.string.renmaiquan_delete_message_tip).callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        goDelete("mainMessageBoard", mId + "", mObjectId, replyNum, mObjectId, type);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                    }
                }).cancelable(false);
                materialDialogsUtil.show();
                return true;
            case R.id.menu_circle_more:
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmai_detail_more");
                createDialog(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void initListener() {
        super.initListener();
        replyListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mData && mData.size() > position) {
                    Map<String, Object> map = mData.get(position);
                    String senderId = (String) map.get("senderSid");
                    int replyMessageBoardId = mId;
                    String replyMessageBoardObjectId = (String) map.get("objectId");
                    if (senderId.equals(RenheApplication.getInstance().getUserInfo().getSid())) {
                        createSelfDialog(TwitterShowMessageBoardActivity.this, "replyMessageBoard",
                                RenheApplication.getInstance().getUserInfo().getSid(), replyMessageBoardId + "",
                                replyMessageBoardObjectId, replyNum, mObjectId, type);
                    } else {
                        String senderName = (String) map.get("senderName");
                        writeComment(senderId, senderName, replyMessageBoardObjectId, replyMessageBoardId);
                    }
                }

            }
        });
        replyListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Map<String, Object> map = mData.get(arg2);
                String content = (String) map.get("content");
                new ContentUtil().createCopyDialog(TwitterShowMessageBoardActivity.this, content);
                return true;
            }

        });
        mFooterViewMore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情的评论列表的查看更多");
                initGet();
            }
        });
        // 监听转发按钮单击事件
        mForwordLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmaiquan_detail_forwad");
                if (null != mObjectId) {
                    Bundle bundle = new Bundle();
                    bundle.putString("objectId", mObjectId);
                    if (!TextUtils.isEmpty(viewHolder.rawContentTv.getText().toString())) {
                        bundle.putString("sender", viewHolder.nameTv.getText().toString());
                        bundle.putString("rawContent", viewHolder.contentTv.getText().toString());
                    }
                    bundle.putString("toForwardPic", toFrowardPic);
                    bundle.putString("toForwardContent", toForwardContent);
                    //					startActivity(ForwardMessageBoardActivity.class, bundle);
                    startActivity(ToShareWithRecentContactsActivity.class, bundle);
                }
            }
        });
        shareLl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmaiquan_detail_forwad");
                String forwardContent = "";
                if (!TextUtils.isEmpty(viewHolder.contentTv.getText().toString())
                        && (!TextUtils.isEmpty(viewHolder.rawContentTv.getText().toString())
                        || !TextUtils.isEmpty(viewHolder.shareContentTv.getText().toString()))) {
                    forwardContent = viewHolder.contentTv.getText().toString();
                }
                new SharePopupWindow(TwitterShowMessageBoardActivity.this, rootRl, mId + "",
                        viewHolder.nameTv.getText().toString().trim(), "", mUserFaceUrl, mUserCompany, mUserJob, mContent,
                        mId + "", mId, true, mObjectId, forwardContent, toForwardContent, toFrowardPic, "5.108", 2);
            }
        });
        viewHolder.rawcontentlayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                switch (shareType) {
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                        if (!TextUtils.isEmpty(shareUrlFinal)) {
                            Intent i = new Intent();
                            if (shareUrlFinal.contains(Constants.TOPIC_URL)) {
                                i.setClass(TwitterShowMessageBoardActivity.this, WebViewForIndustryCircle.class);
                            } else {
                                i.setClass(TwitterShowMessageBoardActivity.this, WebViewActWithTitle.class);
                            }
                            i.putExtra("url", shareUrlFinal);
                            startHlActivity(i);
                        }
                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                        if (!TextUtils.isEmpty(shareUrlFinal)) {
                            Intent i = new Intent();
                            i.setClass(TwitterShowMessageBoardActivity.this, MyHomeArchivesActivity.class);
                            i.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, shareUrlFinal);
                            startHlActivity(i);
                        }
                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:

                        if (!TextUtils.isEmpty(shareUrlFinal)) {
                            Intent i = new Intent();
                            i.setClass(TwitterShowMessageBoardActivity.this, ActivityCircleDetail.class);
                            i.putExtra("circleId", shareUrlFinal);
                            startHlActivity(i);
                        }
                        break;
                    case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_COMMUNAL:
                        if (!TextUtils.isEmpty(shareUrlFinal)) {
                            Intent i = new Intent();
                            if (shareUrlFinal.contains(Constants.TOPIC_URL)) {
                                i.setClass(TwitterShowMessageBoardActivity.this, WebViewForIndustryCircle.class);
                            } else {
                                i.setClass(TwitterShowMessageBoardActivity.this, WebViewActWithTitle.class);
                            }
                            i.putExtra("url", shareUrlFinal);
                            startHlActivity(i);
                        }
                        break;
                    default:
                        MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情的转发内容");
                        if (loadType == LOAD_TYPE_FROM_CONTENT) {
                            if (null != newNoticeListItem && null != newNoticeListItem.getContentInfo()
                                    && null != newNoticeListItem.getContentInfo().getForwardMessageBoardInfo()) {
                                ForwardMessageBoardInfo forwardmsgInfo = newNoticeListItem.getContentInfo()
                                        .getForwardMessageBoardInfo();
                                if (forwardmsgInfo.isForwardRenhe() && null != forwardmsgInfo.getObjectId()) {
                                    Bundle bundle = new Bundle();
                                    NewNoticeList forwardNewNoticeListItem = new NewNoticeList();
                                    ContentInfo contentInfo = new ContentInfo();
                                    contentInfo.setId(forwardmsgInfo.getId());
                                    contentInfo.setObjectId(forwardmsgInfo.getObjectId());
                                    contentInfo.setContent(forwardmsgInfo.getContent());
                                    contentInfo.setAtMembers(forwardmsgInfo.getAtMembers());
                                    contentInfo.setPicList(forwardmsgInfo.getPicLists());
                                    forwardNewNoticeListItem.setContentInfo(contentInfo);
                                    bundle.putSerializable("forwardNewNoticeListItem", forwardNewNoticeListItem);
                                    bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
                                    bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_FORWARD);
                                    Intent intent = new Intent(TwitterShowMessageBoardActivity.this,
                                            TwitterShowMessageBoardActivity.class);
                                    intent.putExtras(bundle);
                                    startHlActivity(intent);
                                }
                            }

                        } else if (loadType == LOAD_TYPE_FROM_NOTICE) {
                            if (null != noticeNewNoticeListItem && null != noticeNewNoticeListItem.getContentInfo()
                                    && null != noticeNewNoticeListItem.getContentInfo().getForwardMessageBoardInfo()) {
                                ForwardMessageBoardInfo forwardmsgInfo = noticeNewNoticeListItem.getContentInfo()
                                        .getForwardMessageBoardInfo();
                                if (forwardmsgInfo.isForwardRenhe() && null != forwardmsgInfo.getObjectId()) {
                                    Bundle bundle = new Bundle();
                                    NewNoticeList forwardNewNoticeListItem = new NewNoticeList();
                                    ContentInfo contentInfo = new ContentInfo();
                                    contentInfo.setId(forwardmsgInfo.getId());
                                    contentInfo.setObjectId(forwardmsgInfo.getObjectId());
                                    contentInfo.setContent(forwardmsgInfo.getContent());
                                    contentInfo.setAtMembers(forwardmsgInfo.getAtMembers());
                                    contentInfo.setPicList(forwardmsgInfo.getPicLists());
                                    forwardNewNoticeListItem.setContentInfo(contentInfo);
                                    bundle.putSerializable("forwardNewNoticeListItem", forwardNewNoticeListItem);
                                    bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
                                    bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_FORWARD);
                                    Intent intent = new Intent(TwitterShowMessageBoardActivity.this,
                                            TwitterShowMessageBoardActivity.class);
                                    intent.putExtras(bundle);
                                    startHlActivity(intent);
                                }
                            }
                        }

                        break;
                }

            }
        });
        viewHolder.rawcontentlayout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != refreshNoticeListItem.getContentInfo().getForwardMessageBoardInfo()
                        && !TextUtils.isEmpty(refreshNoticeListItem.getContentInfo().getForwardMessageBoardInfo().getObjectId()))
                    new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 4, "", refreshNoticeListItem);
                else
                    new ContentUtil().createRenMaiQuanDialog(TwitterShowMessageBoardActivity.this, 2, "", refreshNoticeListItem);
                return true;
            }
        });
        replyEt.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    if (mSid.equals(RenheApplication.getInstance().getUserInfo().getSid()) && type != 1)
                        toForwardCb.setVisibility(View.GONE);
                    else
                        toForwardCb.setVisibility(View.GONE);
                } else {
                    toForwardCb.setVisibility(View.GONE);
                }
            }
        });
        replyEt.setFilters(new InputFilter[]{emojiUtil.emotionFilter, new LengthFilter(TOTAL_REPLY_NUMBER)});
        replyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > TOTAL_REPLY_NUMBER) {
                    return;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                leftReplyNumTv.setText(TOTAL_REPLY_NUMBER - s.length() + "");
                DraftUtil.saveDraft(DraftUtil.DRAFT_COMMENT, draftkey, s.toString());
            }
        });
        replyEt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                if (chat_face_container.getVisibility() == View.VISIBLE) {
//                    chat_face_container.setVisibility(View.GONE);
//                    imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
//                }
                if (chat_face_container.isShown()) {
                    hideEmotionView(true);
                }
            }
        });
        // 表情点击按钮
        imagefaceIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
//                // 隐藏软键盘
//                hideSoftInputView();
//                // 显示转发按钮
//                if (toForwardCb.getVisibility() == View.GONE) {
//                    if (mSid.equals(RenheApplication.getInstance().getUserInfo().getSid()) && type != 1)
//                        toForwardCb.setVisibility(View.GONE);
//                    else
//                        toForwardCb.setVisibility(View.GONE);
//                }
//                // 显示表情列表
//                if (chat_face_container.getVisibility() == View.GONE || chat_face_container.getVisibility() == View.INVISIBLE) {
//                    imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal_on));
//                    chat_face_container.setVisibility(View.VISIBLE);
//                    replyEt.requestFocus();
//                } else {
//                    imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
//                    chat_face_container.setVisibility(View.GONE);
//                }
                toForwardCb.setVisibility(View.GONE);
                if (chat_face_container.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView();
                }
            }
        });
        //表情键盘的点击事件
        emotionFragment.setOnEmotionListener(this);
        // 回复发送按钮
        goReplyIb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmaiquan_detail_quick_reply");
                final View mView = v;
                String content = replyEt.getText().toString().trim();
                final String mcontent = content;
                if (!TextUtils.isEmpty(content)) {
                    goReplyIb.setVisibility(View.GONE);
                    replyProgressBar.setVisibility(View.VISIBLE);
                    new AsyncTask<Object, Void, Object>() {
                        @Override
                        protected Object doInBackground(Object... params) {
                            try {
                                if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD)
                                    return getRenheApplication().getMessageBoardCommand().replyMessageBoard(
                                            (String) params[0], (String) params[1], (String) params[2], (String) params[3],
                                            (String) params[4], (Boolean) params[5], (String) params[6], (String) params[7],
                                            TwitterShowMessageBoardActivity.this);
                                else
                                    return getRenheApplication().getMessageBoardCommand().replyUnMessageBoard(
                                            (String) params[0], (String) params[1], (String) params[2], (String) params[3],
                                            (String) params[4], (Boolean) params[5], (String) params[6], (String) params[7],
                                            TwitterShowMessageBoardActivity.this);

                            } catch (Exception e) {
                                System.out.println(e);
                                return null;
                            }
                        }

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            //							showDialog(1);
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            super.onPostExecute(result);
                            //							removeDialog(1);
                            goReplyIb.setVisibility(View.VISIBLE);
                            replyProgressBar.setVisibility(View.GONE);
                            // 隐藏软键盘
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
                            // 隐藏表情
                            if (chat_face_container.getVisibility() == View.VISIBLE) {
                                chat_face_container.setVisibility(View.GONE);
                                imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
                            }
                            if (null != result) {
                                int state;
                                String MessageboardId = null, MessageboardObjectId = null;
                                if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
                                    ReplyMessageBoard results = (ReplyMessageBoard) result;
                                    state = results.getState();
                                    MessageboardId = results.getMessageboardId();
                                    MessageboardObjectId = results.getMessageboardObjectId();
                                } else {
                                    ReplyUnMessageBoard results = (ReplyUnMessageBoard) result;
                                    state = results.getState();
                                    MessageboardObjectId = results.getNoticeCommentObjectId();
                                }

                                if (1 == state) {
                                    replyEt.setText("");
                                    replyEt.clearFocus();
                                    DraftUtil.removeDraft(DraftUtil.DRAFT_COMMENT, draftkey);
                                    viewHolder.replyButton.setText(replyNum + 1 + "");
                                    viewHolder.replyAndGoodLl.setVisibility(View.VISIBLE);

                                    viewHolder.replyListLl.setVisibility(View.VISIBLE);
                                    seperateLineView.setVisibility(View.VISIBLE);
                                    replyListView.setVisibility(View.VISIBLE);
                                    if (likeNumber > 0) {
                                        viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                                        //										seperateLineView.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolder.replyAndGoodLlGoodRl.setVisibility(View.GONE);
                                        //										seperateLineView.setVisibility(View.GONE);
                                    }
                                    if (!TextUtils.isEmpty(mcontent)) {
                                        if (mData.size() >= replyNum) {
                                            Map<String, Object> map = new HashMap<String, Object>();
                                            map.put("id", 0);
                                            map.put("objectId", mObjectId);
                                            map.put("senderSid", RenheApplication.getInstance().getUserInfo().getSid());
                                            map.put("senderName", RenheApplication.getInstance().getUserInfo().getName());
                                            map.put("senderUserface",
                                                    RenheApplication.getInstance().getUserInfo().getUserface());
                                            if (!TextUtils.isEmpty(reSenderSid)) {
                                                map.put("reSenderSid", reSenderSid);
                                            }
                                            if (!TextUtils.isEmpty(reSenderMemberName)) {
                                                map.put("reSenderMemberName", reSenderMemberName);
                                            }
                                            map.put("content", mcontent);
                                            map.put("createdDateSeconds", System.currentTimeMillis());
                                            mData.add(map);
                                            mSimpleAdapter.notifyDataSetChanged();
                                        }
                                        viewHolder.replyButton.setText(replyNum + 1 + "");
                                        replyNum += 1;
                                    }
                                    if (null != refreshNoticeListItem) {
                                        if (!TextUtils.isEmpty(reSenderSid) && !TextUtils.isEmpty(reSenderMemberName)) {
                                            RenmaiQuanUtils.updateRenmaiQuanItemAddReply(refreshNoticeListItem, mcontent, MessageboardObjectId, MessageboardId, reSenderMemberName, reSenderSid);
                                        } else {
                                            RenmaiQuanUtils.updateRenmaiQuanItemAddReply(refreshNoticeListItem, mcontent, MessageboardObjectId, MessageboardId, "", "");
                                        }
                                        Intent intent = new Intent();
                                        intent.putExtra("refreshNoticeListItem", refreshNoticeListItem);
                                        intent.putExtra("position", position);
                                        intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                                        sendBroadcast(intent);
                                    }
                                    Intent intent1 = new Intent();
                                    intent1.putExtra("objectId", mObjectId);
                                    intent1.putExtra("content", mcontent);
                                    intent1.putExtra("reSenderSid", reSenderSid);
                                    intent1.putExtra("reSenderMemberName", reSenderMemberName);
                                    setResult(RESULT_OK, intent1);
                                    ToastUtil.showToast(TwitterShowMessageBoardActivity.this, "发布成功");
                                } else {
                                    ToastUtil.showErrorToast(TwitterShowMessageBoardActivity.this, "发布失败");
                                }
                            } else {
                                ToastUtil.showNetworkError(TwitterShowMessageBoardActivity.this);
                            }
                        }
                    }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getAdSId(),
                            getRenheApplication().getUserInfo().getSid(), mId + "", mObjectId, content,
                            toForwardCb.isChecked(), replyId + "", replyObjectId);
                } else {
                    //回复不能为空改为评论不能为空
                    ToastUtil.showToast(TwitterShowMessageBoardActivity.this, getResources().getString(R.string.renmaiquan_detail));
                }
            }
        });

        viewHolder.topRl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmai_avartar");
                Intent intent = new Intent(TwitterShowMessageBoardActivity.this, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, mSid);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //弹出评论、分享
        commentIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                titlePopup.setAnimationStyle(R.style.cricleBottomAnimation);
                titlePopup.show(arg0);
            }
        });
    }

    @Override
    public void onEmotionSelected(Emotion emotion) {
        if (null != emotion)
            emojiUtil.onEmotionSelected(emotion, replyEt);
    }

    //显示表情键盘
    private void showEmotionView() {
        emotionHeight = SystemUtils.getKeyboardHeight(this);
        SystemUtils.hideSoftInput(replyEt);
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal_on));
        chat_face_container.getLayoutParams().height = emotionHeight;
        chat_face_container.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        replyEt.requestFocus();
    }

    //隐藏表情键盘
    private void hideEmotionView(boolean showKeyBoard) {
        if (showKeyBoard) {
            SystemUtils.showKeyBoard(replyEt);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
        chat_face_container.setVisibility(View.GONE);
    }

    protected int[] count(String text, String sub) {
        int count = 0, start = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            start += sub.length();
            count++;
        }
        if (count == 0) {
            return null;
        }
        int a[] = new int[count];
        int count2 = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            a[count2] = start;
            start += sub.length();
            count2++;
        }
        return a;
    }

    @SuppressWarnings("unused")
    @SuppressLint("SimpleDateFormat")
    private void string2Date(long date, TextView dateTv) {
        long DAY = 24L * 60L * 60L * 1000L;
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        Date date2 = new Date(date);
        if (null != date2) {
            long diff = now.getTime() - date2.getTime();
            dateTv.setText(DateUtil.formatToGroupTagByDay(this, date2));
        } else {
            dateTv.setText("");
        }
    }

    private void markFavour(int id, String objectId, final int type, final boolean toFavour) {
        String idd = "";
        if (id != -1) {
            idd = String.valueOf(id);
        }
        new AsyncTask<String, Void, MessageBoardOperation>() {

            @Override
            protected MessageBoardOperation doInBackground(String... params) {
                try {
                    if (toFavour) {
                        if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD)
                            return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().favourMessageBoard(
                                    params[0], params[1], params[2], params[3], TwitterShowMessageBoardActivity.this);
                        else
                            return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().favourUnMessageBoard(
                                    params[0], params[1], params[2], params[3], TwitterShowMessageBoardActivity.this);
                    } else {
                        return ((RenheApplication) getApplicationContext()).getMessageBoardCommand()
                                .unFavourMessageBoard(params[0], params[1], params[3], TwitterShowMessageBoardActivity.this);
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(MessageBoardOperation result) {
                super.onPostExecute(result);
                if (null != result) {
                    if (1 == result.getState()) {
                    }
                } else {
                    // ToastUtil.showNetworkError(ct);
                }
            }

        }.executeOnExecutor(Executors.newCachedThreadPool(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
                ((RenheApplication) getApplicationContext()).getUserInfo().getSid(), idd, objectId);
    }

    class clickPic implements OnClickListener {
        String picUrl;

        public clickPic(String url) {
            picUrl = url;
        }

        @Override
        public void onClick(View v) {
            MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "点击人脉圈详情的图片");
            if (null != picUrl && !picUrl.equals("")) {
                CharSequence[] middlePics = new CharSequence[1];
                middlePics[0] = picUrl;
                Intent intent = new Intent(TwitterShowMessageBoardActivity.this, ViewPhotoActivity.class);
                intent.putExtra("ID", 0);
                intent.putExtra("middlePics", middlePics);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_enter, 0);
            }
        }
    }

    class goodClick implements OnClickListener {

        View goodButton;
        int type;
        int favourNumber;
        boolean isFavour;
        LinearLayout ll;

        public goodClick(View view, int type) {
            this.goodButton = view;
            this.type = type;
        }

        public goodClick(LinearLayout ll, int favourNumber, int type, boolean isFavour) {
            this.ll = ll;
            this.type = type;
            this.favourNumber = favourNumber;
            this.isFavour = isFavour;
        }

        @Override
        public void onClick(View arg0) {
            MobclickAgent.onEvent(TwitterShowMessageBoardActivity.this, "renmaiquan_good");
            ImageView iv1 = (ImageView) ll.getChildAt(0);
            ImageView iv2 = (ImageView) ll.getChildAt(1);
            TextView tv = (TextView) ll.getChildAt(2);
            if (null != mObjectId) {
                if (isFavour) {
                } else {
                    markFavour(mId, mObjectId, type, true);
                }
            }
            if (!isFavour) {
                // } else
                // {
                isFavour = true;
                favourNumber = favourNumber + 1;
                //				viewHolder.goodIv.setImageResource(R.drawable.good);

                //				Animation animation = AnimationUtils.loadAnimation(TwitterShowMessageBoardActivity.this, R.anim.good_scale);
                //				viewHolder.goodIv.startAnimation(animation);
                //				gButton.setTextColor(getResources().getColor(R.color.room_good_textcolor));
                if (favourNumber > 0) {
                    //					gButton.setText(likeNumber + "");
                    viewHolder.replyAndGoodLl.setVisibility(View.VISIBLE);
                    viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                    viewHolder.goodGridIv.setImageResource(R.drawable.good_number_icon);
                    if (replyNum > 0) {
                        viewHolder.replyListLl.setVisibility(View.VISIBLE);
                        seperateLineView.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.replyListLl.setVisibility(View.GONE);
                        seperateLineView.setVisibility(View.GONE);
                    }
                    if (null != likedList) {
                        ViewFullMessageBoard.LikedList hostLikedList = new ViewFullMessageBoard.LikedList();
                        hostLikedList.setSid(RenheApplication.getInstance().getUserInfo().getSid());
                        hostLikedList.setUserface(RenheApplication.getInstance().getUserInfo().getUserface());
                        ViewFullMessageBoard.LikedList[] newLikedList = new ViewFullMessageBoard.LikedList[likedList.length
                                + 1];
                        newLikedList[0] = hostLikedList;
                        if (likedList.length > 0) {
                            for (int i = 1; i < newLikedList.length; i++) {
                                newLikedList[i] = likedList[i - 1];
                            }
                        }
                        initLikedView(newLikedList, favourNumber);

                    }

                } else {
                    //					gButton.setText("赞");
                }
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.VISIBLE);
                tv.setVisibility(View.VISIBLE);
                tv.setText(favourNumber + "");
                tv.setTextColor(TwitterShowMessageBoardActivity.this.getResources().getColor(R.color.CP));
                //				titlePopup.getAction(0).setItemTv("已赞");
                // 发广播，通知客厅界面更新消息的状态
                MessageBoards.LikedList meLikedList = new MessageBoards.LikedList();
                meLikedList.setName(RenheApplication.getInstance().getUserInfo().getName());
                meLikedList.setSid(RenheApplication.getInstance().getUserInfo().getSid());
                meLikedList.setUserface(RenheApplication.getInstance().getUserInfo().getUserface());

                if (null != refreshNoticeListItem) {
                    RenmaiQuanUtils.updateRenmaiQuanItemAddLiked(refreshNoticeListItem);
                    Intent intent = new Intent();
                    intent.putExtra("refreshNoticeListItem", refreshNoticeListItem);
                    intent.putExtra("position", position);
                    intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                    sendBroadcast(intent);
                }
            }
        }

    }

    public class ViewHolder {
        public AisenTextView contentTv;
        public AisenTextView rawContentTv;
        public TextView nameTv;
        public ImageView avatarIv;
        public TextView dateTv;
        public ImageView thumbnailPic;
        public ImageView forwardThumbnailPic;
        public LinearLayout replyLl;
        public LinearLayout goodLl;
        public Button goodButton;
        public Button replyButton;
        public LinearLayout rawcontentlayout;
        public TextView mCompanyTv;
        public TextView mIndustryTv;
        public ImageView vipIv;
        public ImageView realNameIv;
        public ImageView goodIv;
        public ImageView arrowIv;
        public LinearLayout threeLl;
        public FrameLayout thumbnailFl;
        public FrameLayout forwardThumbnailFl;
        public GridView thumbnailGridView;
        public GridView forwardThumbnailGridView;
        public RelativeLayout replyAndGoodLl;
        public RelativeLayout replyAndGoodLlGoodRl;
        public LinearLayout replyListLl;
        public LinearLayout rootLl;
        public RelativeLayout contentRl;
        public RelativeLayout topRl;
        public View goodSeperateLineView;
        public TextViewFixTouchConsume shareContentTv;
        public ImageView sharePicIv;

        public LinearLayout replyAndGoodLlGoodLl;
        public ImageView unReplyAndGoodLlGoodIv;
        public ImageView replyAndGoodLlGoodIv;
        public ImageView goodGridIv;
        public TextView circleGoodNumberTv;

        public LinearLayout forwardShareLl;
        public View forwardShareSeperateLine;
        public TextView forwardShareTitle;
        public TextView forwardShareContentTv;
        public TextView forwardShareContentTv2;
        public ImageView circleSharePic;
        public com.itcalf.renhe.view.TextView addFriendTv;
    }

    class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 1000);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    viewHolder.replyAndGoodLl.setVisibility(View.VISIBLE);
                    viewHolder.replyListLl.setVisibility(View.VISIBLE);
                    seperateLineView.setVisibility(View.VISIBLE);
                    replyListView.setVisibility(View.VISIBLE);
                    if (likeNumber > 0) {
                        viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.replyAndGoodLlGoodRl.setVisibility(View.GONE);
                    }
                    String content = data.getStringExtra("content");
                    if (TextUtils.isEmpty(content)) {
                        content = "";
                    }
                    viewHolder.replyButton.setText(replyNum + 1 + "");
                    replyNum += 1;
                    String reSenderSid = data.getStringExtra("reSenderSid");
                    String reSenderMemberName = data.getStringExtra("reSenderMemberName");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("id", 0);
                    map.put("objectId", mObjectId);
                    map.put("senderSid", RenheApplication.getInstance().getUserInfo().getSid());
                    map.put("senderName", RenheApplication.getInstance().getUserInfo().getName());
                    map.put("senderUserface", RenheApplication.getInstance().getUserInfo().getUserface());
                    if (!TextUtils.isEmpty(reSenderSid)) {
                        map.put("reSenderSid", reSenderSid);
                    }
                    if (!TextUtils.isEmpty(reSenderMemberName)) {
                        map.put("reSenderMemberName", reSenderMemberName);
                    }
                    map.put("content", content);
                    map.put("createdDateSeconds", System.currentTimeMillis());
                    mData.add(0, map);
                    mSimpleAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private float getDensity() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        double width = metric.widthPixels; // 屏幕宽度（像素）
        double height = metric.heightPixels; // 屏幕高度（像素）
        float density = metric.density; // 屏幕密度
        return density;
    }

    public void createSelfDialog(Context context, final String type, final String userId, final String msgId,
                                 final String msgObjectId, final int replyNum, final String mObjectId, final int ContentType) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.custom_delete_dialog, null);

        mAlertDialog = new Dialog(TwitterShowMessageBoardActivity.this, R.style.TranslucentUnfullwidthWinStyle);
        mAlertDialog.setContentView(view);
        mAlertDialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = mAlertDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = (int) (dm.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
        LinearLayout reportLl = (LinearLayout) view.findViewById(R.id.reportLl);
        LinearLayout shieldLl = (LinearLayout) view.findViewById(R.id.shieldLl);
        mAlertDialog.show();
        reportLl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (null != mAlertDialog) {
                    mAlertDialog.dismiss();
                    int contentRes = R.string.renmaiquan_delete_message_tip;
                    if (type.equals("replyMessageBoard")) {
                        contentRes = R.string.renmaiquan_delete_comment_tip;
                    }
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(TwitterShowMessageBoardActivity.this);
                    materialDialogsUtil.getBuilder(contentRes).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            goDelete(type, msgId, msgObjectId, replyNum, mObjectId, ContentType);
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                        }
                    });
                    materialDialogsUtil.show();

                }
            }
        });
        shieldLl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (null != mAlertDialog) {
                    mAlertDialog.dismiss();
                }
            }
        });
    }

    private void goDelete(final String type, String messageBoardId, final String messageBoardObjectId, final int mreplyNum,
                          final String mObjectId, final int ContentType) {
        new DeleteMsgTask(this, ContentType) {
            public void doPre() {
                requestDialog.addFade(rootRl);
            }

            public void doPost(MessageBoardOperation result) {

                if (null != result && result.getState() == 1) {
                    if (type.equals("mainMessageBoard")) {
                        Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
                        intent.putExtra("position", position);
                        intent.putExtra("objectId", messageBoardObjectId);
                        sendBroadcast(intent);
                        new RenmaiQuanUtils(TwitterShowMessageBoardActivity.this).deleteRenmaiQuanItem(messageBoardObjectId);
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                requestDialog.removeFade(rootRl);
                                finish();
                                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                            }
                        }, 1000);
                    } else if (type.equals("replyMessageBoard")) {
                        replyNum -= 1;
                        RenmaiQuanUtils.updateRenmaiQuanItemDeleteReply(refreshNoticeListItem, messageBoardObjectId);
                        Intent intent = new Intent();
                        intent.putExtra("refreshNoticeListItem", refreshNoticeListItem);
                        intent.putExtra("position", position);
                        intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                        sendBroadcast(intent);
                        for (int i = 0; i < mData.size(); i++) {
                            if (((String) (mData.get(i).get("objectId"))).equals(messageBoardObjectId)) {
                                mData.remove(i);
                                mSimpleAdapter.notifyDataSetChanged();
                            }
                        }
                        if (replyNum >= 1) {
                            viewHolder.replyButton.setText(replyNum + "");
                        } else {
                            viewHolder.replyButton.setText(getResources().getString(R.string.room_reply));
                        }
                        if (replyNum <= 0) {
                            viewHolder.replyListLl.setVisibility(View.GONE);
                            replyListView.setVisibility(View.GONE);
                            if (likeNumber > 0) {
                                viewHolder.replyAndGoodLl.setVisibility(View.VISIBLE);
                                viewHolder.replyAndGoodLlGoodRl.setVisibility(View.VISIBLE);
                                seperateLineView.setVisibility(View.GONE);
                            } else {
                                viewHolder.replyAndGoodLl.setVisibility(View.GONE);
                                viewHolder.replyAndGoodLlGoodRl.setVisibility(View.GONE);
                                seperateLineView.setVisibility(View.GONE);
                            }
                        }
                        requestDialog.removeFade(rootRl);
                    }
                } else {
                    if (type.equals("mainMessageBoard")) {
                        Toast.makeText(TwitterShowMessageBoardActivity.this, "这条留言不存在", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    } else if (type.equals("replyMessageBoard")) {
                        Toast.makeText(TwitterShowMessageBoardActivity.this, "这条评论不存在", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), type, messageBoardId, messageBoardObjectId);
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    class PopwindowItemClickListener implements DetailTitlePopup.OnItemOnClickListener {
        LinearLayout replyLl;
        LinearLayout shareLl;

        public PopwindowItemClickListener(LinearLayout goodLl, LinearLayout replyLl, LinearLayout shareLl) {
            super();
            this.replyLl = replyLl;
            this.shareLl = shareLl;
        }

        @Override
        public void onItemClick(ActionItem item, int position) {
            switch (position) {
                case 0:
                    titlePopup.updateGoodItem();
                    viewHolder.replyAndGoodLlGoodLl.performClick();
                    break;
                case 1:
                    replyLl.performClick();
                    break;
                case 2:
                    shareLl.performClick();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 收藏/举报/屏蔽
     *
     * @param context
     */
    public void createDialog(Context context) {
        new ContentUtil().createRenMaiQuanDialog(context, 3, "", refreshNoticeListItem);
    }

    /**
     * 评论或回复
     */
    private void writeComment(String reSenderSid, String reSenderMemberName, String replyObjectId, int replyId) {

        this.reSenderSid = reSenderSid;
        this.reSenderMemberName = reSenderMemberName;
        this.replyObjectId = replyObjectId;
        this.replyId = replyId;

        replyEt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(replyEt, 0);

        replyEt.performClick();//如果已经打开表情栏就关闭

        if (!TextUtils.isEmpty(reSenderMemberName)) {
            replyEt.setHint("回复 " + reSenderMemberName);
        } else {
            replyEt.setHint("评论");
        }

        getDraft(mObjectId, reSenderSid);
    }

    private void addFriend() {
        if (null != refreshNoticeListItem) {
            new AddFriendTask(this, new AddFriendTask.IAddFriendCallBack() {
                @Override
                public void onPre() {
                    showMaterialLoadingDialog(R.string.add_friend_sending, true);
                }

                @Override
                public void doPost(AddFriend result) {
                    hideMaterialLoadingDialog();
                    if (result == null) {
                        ToastUtil.showErrorToast(TwitterShowMessageBoardActivity.this, getString(R.string.connect_server_error));
                        return;
                    } else if (result.getState() == 1) {
                        viewHolder.addFriendTv.setVisibility(View.GONE);
                        ToastUtil.showToast(TwitterShowMessageBoardActivity.this, R.string.success_friend_request);
                        new RenmaiQuanUtils(TwitterShowMessageBoardActivity.this).updateDBRenmaiQuanItemFriendState(refreshNoticeListItem);
                        Intent intent = new Intent();
                        intent.putExtra("position", position);
                        intent.setAction(Constants.BroadCastAction.RMQ_ACTION_RMQ_ADD_FRIEND_NOTICE);
                        sendBroadcast(intent);
                    }
                }
            }).executeOnExecutor(Executors.newCachedThreadPool(), refreshNoticeListItem.getSenderInfo().getSid(), "", "", 10 + "");
        }
    }

    /**
     * 获取草稿
     */
    private void getDraft(String mobjectid, String resendersid) {
        draftkey = DraftUtil.createKey(RenheApplication.getInstance().getUserInfo().getSid(), mObjectId, resendersid);
        String draftStr = DraftUtil.getDraft(DraftUtil.DRAFT_COMMENT, draftkey);
        if (!TextUtils.isEmpty(draftStr)) {
            replyEt.setText(emojiUtil.getEmotionSpannedString(null, draftStr));
//            replyEt.setSelection(draftStr.length());
        } else {
            replyEt.setText("");
        }
    }
}

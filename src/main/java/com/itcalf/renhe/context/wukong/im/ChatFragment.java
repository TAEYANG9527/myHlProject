package com.itcalf.renhe.context.wukong.im;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.doraemon.Doraemon;
import com.alibaba.doraemon.audio.AudioMagician;
import com.alibaba.doraemon.image.ImageMagician;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationChangeListener;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Member;
import com.alibaba.wukong.im.MessageBuilder;
import com.alibaba.wukong.im.MessageContent;
import com.alibaba.wukong.im.MessageListener;
import com.alibaba.wukong.im.MessageService;
import com.alibaba.wukong.im.User;
import com.alibaba.wukong.im.UserService;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.BaseRecyclerAdapter;
import com.itcalf.renhe.adapter.ChatGroupPupAdapter;
import com.itcalf.renhe.adapter.RecyclerChatItemAdapter;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.bean.CircleJoinCount;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.context.contacts.ChooseContactsActivity;
import com.itcalf.renhe.context.imageselector.ImageSelector;
import com.itcalf.renhe.context.imageselector.ImageSelectorActivity;
import com.itcalf.renhe.context.luckyMoneyAd.LuckyMoneyAdFillInfoActivity;
import com.itcalf.renhe.context.luckymoney.LuckyMoneySendActivity;
import com.itcalf.renhe.context.wukong.im.kit.MessageSender;
import com.itcalf.renhe.context.wukong.im.kit.MessageSenderImpl;
import com.itcalf.renhe.eventbusbean.ChangeChatTitleEvent;
import com.itcalf.renhe.eventbusbean.RefreshChatUnreadEvent;
import com.itcalf.renhe.eventbusbean.RefreshChatUserInfoEvent;
import com.itcalf.renhe.eventbusbean.SendFileMsgEvent;
import com.itcalf.renhe.eventbusbean.SendMsgSuccessEvent;
import com.itcalf.renhe.imageUtil.ImageSelectorUtil;
import com.itcalf.renhe.listener.NewPauseOnScrollListener;
import com.itcalf.renhe.utils.ChatUtils;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.emoji.EmojiFragment;
import com.itcalf.renhe.view.emoji.ExpressionUtil;
import com.itcalf.renhe.viewholder.ConversationViewHolder;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.itcalf.renhe.widget.emojitextview.EmotionsDB;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import org.aisen.android.common.utils.BitmapUtil;
import org.aisen.android.common.utils.SystemUtils;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangning on 2015/10/12.
 */
public class ChatFragment extends Fragment implements MessageListener, SensorEventListener,
        EmojiFragment.OnEmotionSelectedListener, EmojiFragment.OnMoreSelectedListener, View.OnClickListener, ChatUtils.ChatCallBack {
    //View初始化
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private View rootView;
    private RecyclerChatItemAdapter recyclerChatItemAdapter;
    private LinearLayout loadingLl;
    private LinearLayout rootLl;
    private LinearLayout chatLl;
    private RelativeLayout layEmotion;
    private EditText mEditTextContent;
    private ImageView imagefaceIv;
    private EmojiFragment emotionFragment;
    private LinearLayout bottomExpressionLl, bottomSilentLl;
    private RelativeLayout bottomRl;
    /**
     * 文本、图片发送按钮
     */
    private Button mSendBtn, mAddImgBtn;
    private RelativeLayout mAddImgRl;
    private ImageView newfeatherIconIv;//有新功能的加号右上角小红点
    /**
     * 按住说话控件
     */
    private TextView mRcdBtn;
    /**
     * 文本和语音输入模式切换按键
     */
    private ImageView mChatModeImgBtn;
    private View rcChat_popup;
    private ImageView volume;
    private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
    private ImageView sc_img1;
    private LinearLayout del_re;//录音时的取消按钮
    private TextView descTv;
    private ImageView microPhoneIv;// 麦克风view
    private MenuItem groupMenuItem;
    private MenuItem chatMenuItem;
    private TextView unReadNoticeNumTv;//圈子中未读的消息提醒数目，显示在右上角的更多图标的角标
    private PopupWindow popWindow;
    private View popView;
    private ListView popListView;
    private ChatGroupPupAdapter chatGroupPupAdapter;
    //数据初始化
    private Conversation mConversation;//当前会话
    private long otherOpenId;//单聊时，对方的openId
    private String chatToUserName;//对方（单聊或者群聊）的名字
    private String chatToUserFace;//对方（单聊或者群聊）的头像
    private ArrayList<ChatMessage> datasList;
    private HashMap<Long, Member> groupMemberInfo = new HashMap<>(); // 用户存储圈子成员信息
    private List<String> imgsUrlList = new ArrayList<>();//存储聊天记录里所有的image url
    private HashMap<Long, String> remindInfo = new HashMap<>(); // 被@选中成员信息
    private boolean onLoading = false;
    private final LayoutTransition transitioner = new LayoutTransition();
    private int emotionHeight;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;
    private boolean isEditTextFocusd = false;
    private int scrolledX, scrolledY;
    private ArrayList<String> selectedImagePath = new ArrayList<>();//用于存放选取的图片的本地路径
    private String isNameExist_net;//用来标示创建的圈子是否有名字
    private int unReadJoinCircleRequestCount;// int 未读加入圈子的申请数
    private int unReadMyTopicNoticeCount;// int 未读的我的话题个数
    private int unReadCircleTopicNoticceCount;// int 未读的圈子话题个数
    private int totalUnReadNum;//总共未读的圈子提醒个数
    private CircleJoinCount circleJoinCountResult;
    private int mySilentState;//我是否被禁言 0=未禁言 1=禁言
    /**
     * 字符串中@的个数
     */
    private int atCount = 0;
    //工具初始化
    private ChatUtils chatUtils;
    private MessageBuilder mMessageBuilder;//消息构造器
    private AudioMagician mAudioMagician;//录音工具
    private ImageMagician mImageMagician;//图片显示工具类
    private ChatConversationChangeListener chatConversationChangeListener;
    private MessageSender mMessageSender;//消息发送机，用于应用的消息发送，简化发送逻辑。
    private ExpressionUtil expressionUtil;
    //听筒模式
    private AudioManager audioManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Handler mHandler;
    private boolean hasRequest = false;//是否有入圈申请

    //常量初始化
    public static final long LOAD_MORE_DELAY = 500;//上拉加载更多时，延时0.5s加载
    public final static String CONVERSATION_TYPE_TAG = "conversationType";
    public final static String IM_USER_SILENT_STATE_TAG = "silentState";//成员列表user 扩展字段存储的用户是否被禁言的状态key值

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//声明执行onCreateOptionsMenu方法
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mConversation) {
            RenheApplication.getInstance().setInChatOpenId(mConversation.conversationId());//设置全局唯一的当前会话id
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);//注册语音处理listener
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        RenheApplication.getInstance().setInChatOpenId(Constants.CURRENT_IS_NOT_IN_CHAT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == getActivity())
            return null;
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_im_chat, null);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        initView();
        initData();
        initListener();
        registReceiver();
        return rootView;
    }

    private void initView() {
        rootLl = (LinearLayout) rootView.findViewById(R.id.rootLl);
        chatLl = (LinearLayout) rootView.findViewById(R.id.chat_context_Ll);
        mEditTextContent = (EditText) rootView.findViewById(R.id.et_sendmessage);
        imagefaceIv = (ImageView) rootView.findViewById(R.id.image_face);
        layEmotion = (RelativeLayout) rootView.findViewById(R.id.containerRl);
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SystemUtils.getScreenHeight(getActivity()), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
        transitioner.setAnimator(LayoutTransition.APPEARING, animIn);

        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY", emotionHeight,
                SystemUtils.getScreenHeight(getActivity())).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        rootLl.setLayoutTransition(transitioner);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.chat_recycler_view);
        loadingLl = (LinearLayout) rootView.findViewById(R.id.loadingLL);
        mRecyclerView.setVisibility(View.GONE);
        loadingLl.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);//这里用线性显示 类似于list view

        mSendBtn = (Button) rootView.findViewById(R.id.btn_send);
        mSendBtn.setOnClickListener(this);
        mAddImgBtn = (Button) rootView.findViewById(R.id.btn_send_img);
        mAddImgBtn.setOnClickListener(this);
        mAddImgRl = (RelativeLayout) rootView.findViewById(R.id.btn_send_img_rl);
        newfeatherIconIv = (ImageView) rootView.findViewById(R.id.newfeather_icon_iv);
        mChatModeImgBtn = (ImageView) rootView.findViewById(R.id.ivPopUp);
        mRcdBtn = (TextView) rootView.findViewById(R.id.btn_rcd);
        del_re = (LinearLayout) rootView.findViewById(R.id.del_re);
        rcChat_popup = rootView.findViewById(R.id.rcChat_popup);
        volume = (ImageView) rootView.findViewById(R.id.volume);
        voice_rcd_hint_rcding = (LinearLayout) rootView.findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_loading = (LinearLayout) rootView.findViewById(R.id.voice_rcd_hint_loading);
        voice_rcd_hint_tooshort = (LinearLayout) rootView.findViewById(R.id.voice_rcd_hint_tooshort);
        sc_img1 = (ImageView) rootView.findViewById(R.id.sc_img1);
        descTv = (TextView) rootView.findViewById(R.id.desc);
        microPhoneIv = (ImageView) rootView.findViewById(R.id.imageView1);
        bottomExpressionLl = (LinearLayout) rootView.findViewById(R.id.bottom_expression_ll);
        bottomSilentLl = (LinearLayout) rootView.findViewById(R.id.bottom_silent_ll);
        bottomRl = (RelativeLayout) rootView.findViewById(R.id.bottom_rl);
        //初始化menu菜单window
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popView = inflater.inflate(R.layout.popupwindow_add_layout, null);
        popView.getBackground().setAlpha(230);
        popListView = (ListView) popView.findViewById(R.id.lv_popupwindow_add);
        popWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setOutsideTouchable(true);
        chatGroupPupAdapter = new ChatGroupPupAdapter(getActivity());
        popListView.setAdapter(chatGroupPupAdapter);
    }

    private void registReceiver() {
        if (null == getActivity())
            return;
    }

    private void unRegisReceiver() {
        if (null == getActivity())
            return;

    }

    private void initListener() {
        //注册EventBus
        EventBus.getDefault().register(this);
        IMEngine.getIMService(MessageService.class).addMessageListener(this);//注册IM接收listener
        chatConversationChangeListener = new ChatConversationChangeListener();
        IMEngine.getIMService(ConversationService.class).addConversationChangeListener(chatConversationChangeListener);
        //recyclerView滑动时，暂停图片加载
        mRecyclerView.addOnScrollListener(new NewPauseOnScrollListener(ImageLoader.getInstance(), true, true));
        //滑动到顶部自动加载更多
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (datasList == null || datasList.size() <= 0) {
                            break;
                        }
                        int pastItems = layoutManager.findFirstVisibleItemPosition();
                        Logger.d("onScrolled>>" + pastItems);
                        if (!onLoading) {
                            if (!recyclerChatItemAdapter.isEnd()) {
                                if ((pastItems == 0)) {
                                    onLoading = true;
                                    recyclerChatItemAdapter.setIsShowFooter(true);
                                    scrolledX = mRecyclerView.getScrollX();
                                    scrolledY = mRecyclerView.getScrollY();
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadConversationMessages(null != datasList.get(0) ? datasList.get(0).getMessage() :
                                                    null);
                                        }
                                    }, LOAD_MORE_DELAY);
                                }
                            }
                        }
                        break;
                }
            }
        });
        emotionFragment.setOnEmotionListener(this);
        emotionFragment.setOnMoreSelectedListener(this);
        // 内容编辑
        mEditTextContent.addTextChangedListener(editContentWatcher);
        // 更换表情
        mEditTextContent.setFilters(new InputFilter[]{emotionFilter});
        mEditTextContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (layEmotion.isShown()) {
                    hideEmotionView(true);
                }
                chatUtils.scrollToBottom();
            }
        });
        mEditTextContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!isEditTextFocusd && layEmotion.isShown()) {
                        hideEmotionView(true);
                    }
                    isEditTextFocusd = true;
                    chatUtils.scrollToBottom();
                }
            }
        });
        /**
         * 切换表情跟键盘
         */
        imagefaceIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layEmotion.isShown()) {
                    if (emotionFragment.isShowingEmoji()) {
                        emotionFragment.changeShowingView(true);
                        hideEmotionView(true);
                    } else {
                        emotionFragment.changeShowingView(true);
                        changeEmojiImageState(true);
                    }
                } else {
                    emotionFragment.changeShowingView(true);
                    showEmotionView(SystemUtils.isKeyBoardShow(getActivity()));
                }
                isEditTextFocusd = true;
                mEditTextContent.requestFocus();
                chatUtils.scrollToBottom();
            }
        });

        mChatModeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRcdBtn.getVisibility() == View.VISIBLE) {
                    mChatModeImgBtn.setImageResource(R.drawable.chat_bottom_audio_bg);
                    mRcdBtn.setVisibility(View.GONE);
                    mEditTextContent.setVisibility(View.VISIBLE);
                    imagefaceIv.setVisibility(View.VISIBLE);
                    mEditTextContent.requestFocus();
                    changeSendBtState();
                    SystemUtils.showKeyBoard(mEditTextContent);
                    chatUtils.scrollToBottom();
                } else {
                    mChatModeImgBtn.setImageResource(R.drawable.chat_bottom_keyboard_bg);
                    mRcdBtn.setVisibility(View.VISIBLE);
                    mEditTextContent.setVisibility(View.GONE);
                    imagefaceIv.setVisibility(View.GONE);
                    mSendBtn.setVisibility(View.GONE);
                    mAddImgRl.setVisibility(View.VISIBLE);
                    mAddImgBtn.setVisibility(View.VISIBLE);
                    SystemUtils.hideSoftInput(mEditTextContent);
                    hideEmotionView(false);
                }
            }
        });
        mAddImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRcdBtn.getVisibility() == View.VISIBLE) {
                    mChatModeImgBtn.setImageResource(R.drawable.chat_bottom_audio_bg);
                    mRcdBtn.setVisibility(View.GONE);
                    mEditTextContent.setVisibility(View.VISIBLE);
                    imagefaceIv.setVisibility(View.VISIBLE);
                    mEditTextContent.requestFocus();
                }
                changeEmojiImageState(false);
                if (layEmotion.isShown()) {
                    if (!emotionFragment.isShowingEmoji())
                        hideEmotionView(true);
                    emotionFragment.changeShowingView(false);
                } else {
                    emotionFragment.changeShowingView(false);
                    showEmotionView(SystemUtils.isKeyBoardShow(getActivity()));
                }
                chatUtils.scrollToBottom();
//                chatUtils.showLatestPhoto(ChatFragment.this, mAddImgBtn, emotionHeight);//展示最新拍/截屏的照片
            }
        });
        mRcdBtn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // 按下语音录制按钮时返回false执行父类OnTouch
                return false;
            }
        });
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SystemUtils.hideSoftInput(mEditTextContent);//隐藏键盘
                if (layEmotion.isShown()) {//如果正在显示表情，隐藏表情
                    hideEmotionView(false);
                }
                return false;
            }
        });
        recyclerChatItemAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Object data, int position) {
                SystemUtils.hideSoftInput(mEditTextContent);//隐藏键盘
                if (layEmotion.isShown()) {//如果正在显示表情，隐藏表情
                    hideEmotionView(false);
                }
            }

            @Override
            public boolean onItemLongClick(View view, Object data, int position) {
                return false;
            }
        });
        /**
         * Fragment中没有onTouchEvent解决方法
         * Fragment中，注册
         * 接收MainActivity的Touch回调的对象
         * 重写其中的onTouchEvent函数，并进行该Fragment的逻辑处理
         */
        ChatMainActivity.MyTouchListener myTouchListener = new ChatMainActivity.MyTouchListener() {
            @Override
            public void onTouchEvent(MotionEvent event) {
                // 处理手势事件
                if (mRcdBtn.getVisibility() != View.VISIBLE)
                    return;
                chatUtils.handleRecordBtTouch(event, mRcdBtn, del_re, rcChat_popup, voice_rcd_hint_loading, voice_rcd_hint_rcding, voice_rcd_hint_tooshort,
                        microPhoneIv, volume, descTv, sc_img1);
            }
        };

        // 将myTouchListener注册到分发列表
        ((ChatMainActivity) getActivity()).registerMyTouchListener(myTouchListener);
        loadCache();//加载缓存
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            if (null != bundle.getSerializable(ChatMainActivity.CONVERSATION_ARG))
                mConversation = (Conversation) bundle.getSerializable(ChatMainActivity.CONVERSATION_ARG);
            if (null != bundle.getString(ChatMainActivity.CHATTOUSERNAME_ARG))
                chatToUserName = bundle.getString(ChatMainActivity.CHATTOUSERNAME_ARG);
            if (null != bundle.getString(ChatMainActivity.CHATTOUSERFACE_ARG))
                chatToUserFace = bundle.getString(ChatMainActivity.CHATTOUSERFACE_ARG);
            isNameExist_net = bundle.getString("isNameExist_net");
        }
        if (mConversation == null) {
            if (null != getActivity()) {
                getActivity().finish();
                ToastUtil.showToast(getActivity(), "会话消息获取异常，请重试");
            } else {
                return;
            }
        }
        //初始化emojiFragment
        emotionFragment = EmojiFragment.newInstance();
        Bundle emojiBundle = new Bundle();
        emojiBundle.putSerializable(CONVERSATION_TYPE_TAG, mConversation.type());
        emotionFragment.setArguments(emojiBundle);
        getFragmentManager().beginTransaction().add(R.id.containerRl, emotionFragment, "EmotionFragemnt").commit();

        if (null != datasList)
            datasList.clear();
        datasList = new ArrayList<>();
        // 初始化工具类
        Doraemon.init(getActivity());
        mMessageBuilder = IMEngine.getIMService(MessageBuilder.class);
        mMessageSender = MessageSenderImpl.getInstance();
        mAudioMagician = (AudioMagician) Doraemon.getArtifact(AudioMagician.AUDIO_ARTIFACT);
        mImageMagician = (ImageMagician) Doraemon.getArtifact(ImageMagician.IMAGE_ARTIFACT);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        recyclerChatItemAdapter = new RecyclerChatItemAdapter(getActivity(), mRecyclerView, datasList, mConversation, imgsUrlList);
        mRecyclerView.setAdapter(recyclerChatItemAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    default:
                        break;
                }
                return true;
            }
        });
        expressionUtil = new ExpressionUtil(getActivity());
        ChatUtils.createMenuPopupWindow(this, mConversation, popWindow, popListView, chatGroupPupAdapter);
    }

    /**
     * 加载聊天缓存
     */
    private void loadCache() {
        if (mConversation == null)
            return;
        mConversation.sync();
        if (mConversation.unreadMessageCount() != 0) {
            mConversation.resetUnreadCount(); // 清除未读消息数
        }
        if (mConversation.type() == Conversation.ConversationType.GROUP) {
            mConversation.updateAtMeStatus(false);//将@设为已读
            //清空conversation extension 中的xx评论了你的话题相关字段
            mConversation.updateExtension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY, "");
            mConversation.updateExtension(ConversationViewHolder.HIGHLIGHT, "");
            mConversation.updateExtension(ConversationViewHolder.CONTENT, "");
            mConversation.updateExtension(ConversationViewHolder.OPENID, "");
        }
        otherOpenId = mConversation.getPeerId();
        chatUtils = new ChatUtils(getActivity(), mConversation, mRecyclerView, recyclerChatItemAdapter,
                datasList, groupMemberInfo, imgsUrlList, mEditTextContent, mAudioMagician, remindInfo, mMessageBuilder);
        recyclerChatItemAdapter.setChatUtils(chatUtils);
        chatUtils.setChatCallBack(this);
        initTitleAndIcon();
        ((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        msp = getActivity().getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        mEditor = msp.edit();
        if (mConversation.type() == Conversation.ConversationType.CHAT) {
            mEditor.putInt(mConversation.getPeerId() + "num", 0);
        } else {
            mEditor.putInt(mConversation.conversationId() + "num", 0);
        }
        mEditor.apply();
        initNewFeatherIcon();
    }

    /**
     * 初始化底部输入框右侧“+”号上小红点显示与否
     */
    private void initNewFeatherIcon() {
        if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_WALLET_NEW, true, true))
            newfeatherIconIv.setVisibility(View.VISIBLE);
        if (mConversation.type() == Conversation.ConversationType.GROUP) {
            if (SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.USER_USER_CHAT_LUCKYMONEY_AD_NEW, true, true))
                newfeatherIconIv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 对草稿信息进行存储
     */

    private void storeConversationDraftText() {
        String draft = mEditTextContent.getText().toString();
        if (draft.trim().length() <= 0) {
            draft = "";
        }
        if (mConversation != null && !draft.equals(mConversation.draftMessage())) {
            mConversation.updateDraftMessage(draft);
        }
    }

    /**
     * 加载会话的草稿信息
     */
    private void recoverConversationDraftText() {
        if (mConversation != null) {
            // 如果存在草稿信息，则将草稿信息加载到输入区域
            if (!TextUtils.isEmpty(mConversation.draftMessage())) {
                mEditTextContent.setText(expressionUtil.getEmotionSpannedString(getActivity(), null, mConversation.draftMessage()));
//                mEditTextContent.setSelection(mConversation.draftMessage().length());
                mEditTextContent.setSelection(mEditTextContent.getText().toString().length());
            }
        }
    }

    /**
     * 初始化聊天对方的名字和头像
     */
    private void initTitleAndIcon() {
        if (mConversation.type() == Conversation.ConversationType.CHAT) {
            if (!TextUtils.isEmpty(chatToUserName) && !TextUtils.isEmpty(chatToUserFace)) {
                EventBus.getDefault().post(new ChangeChatTitleEvent(chatToUserName));
                // 获取会话消息
                loadConversationMessages(null);
            } else {
                IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {

                    @Override
                    public void onException(String arg0, String arg1) {

                    }

                    @Override
                    public void onProgress(User arg0, int arg1) {

                    }

                    @Override
                    public void onSuccess(User user) {
                        chatToUserName = user.nickname();
                        chatToUserFace = user.avatar();
//                        getActivity().setTitle(chatToUserName);
                        EventBus.getDefault().post(new ChangeChatTitleEvent(chatToUserName));
                        loadConversationMessages(null);
                    }
                }, mConversation.getPeerId());

            }
        } else if (mConversation.type() == Conversation.ConversationType.GROUP) {
            chatToUserName = mConversation.title();
            chatToUserFace = mConversation.icon();
            //5.4.1 版本 由 conversation.title显示前三个成员名 改为 圈子（成员数）
            initGroupTitle();
            if (-1 != NetworkUtil.hasNetworkConnection(getActivity())) {
                IMEngine.getIMService(ConversationService.class).listMembers(new Callback<List<Member>>() {
                    @Override
                    public void onException(String arg0, String arg1) {

                    }

                    public void onProgress(List<Member> arg0, int arg1) {
                    }

                    public void onSuccess(List<Member> members) {
                        for (Member member : members) {
                            if (null != groupMemberInfo && null != member) {
                                groupMemberInfo.put(member.user().openId(), member);
                            }
                            if (null != member && null != member.user() && member.user().openId() == RenheApplication.getInstance().currentOpenId
                                    && null != member.user().extension(IM_USER_SILENT_STATE_TAG)) {
                                try {
                                    mySilentState = Integer.parseInt(member.user().extension(IM_USER_SILENT_STATE_TAG));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        loadConversationMessages(null);
                    }

                }, mConversation.conversationId(), 0, Integer.MAX_VALUE);
            } else {
                bottomRl.setVisibility(View.GONE);
                loadConversationMessages(null);
            }
        }
    }

    /**
     * 加载会话消息记录
     */
    private void loadConversationMessages(final com.alibaba.wukong.im.Message indexMessage) {
        if (null == mConversation)
            return;
        // 获取会话消息
        mConversation.listPreviousMessages(indexMessage, Constants.CHAT_CONSTANTS.REQUEST_COUNT, new Callback<List<com.alibaba.wukong.im.Message>>() {
            @Override
            public void onException(String arg0, String arg1) {
                loadingLl.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                changeBottomViewBySilentState();

                if (null == indexMessage)
                    recoverConversationDraftText(); // 输入框草稿
                Logger.e("获取会话消息失败！错误码：" + arg0 + "，原因：" + arg1);
            }

            @Override
            public void onProgress(List<com.alibaba.wukong.im.Message> arg0, int arg1) {
            }

            @Override
            public void onSuccess(List<com.alibaba.wukong.im.Message> messages) {
                loadingLl.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                changeBottomViewBySilentState();
                chatUtils.handleCacheMessages(messages, chatToUserName, chatToUserFace, indexMessage == null);
                //通过indexMessage是不是null来区分，此次加载更多是不是用户通过上拉刷新来加载的,是null代表加载是通过打开对话，自动获取的第一页缓存
                if (null != indexMessage)
//                    mRecyclerView.scrollToPosition(datasList.size() > messages.size() ? messages.size() + 1 : messages.size());
//                    mRecyclerView.scrollTo(scrolledX, scrolledY);
                    //如果是用户手动上拉加载更多，加载完成后，让列表滚动到加载前的第一个可见item的位置，并且让最新一页的最后一个item露出一半左右，具体体验参考微信聊天记录加载
                    layoutManager.scrollToPositionWithOffset(messages.size(), scrolledY - DensityUtil.dip2px(getActivity(), 50));
                onLoading = false;
                if (null == indexMessage) {//获取所有的图片，只需要一次就行，通过indexMessage是不是null来区分,是null代表加载是通过打开对话，自动获取的第一页缓存
                    chatUtils.loadConversationImageMessages();//获取聊天记录里所有的图片缓存
                    recoverConversationDraftText(); // 输入框草稿
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send: // 发送文本消息
                String contString = mEditTextContent.getText().toString();
                if (contString.trim().length() > 0 && contString.trim().length() <= 5000) {
                    com.alibaba.wukong.im.Message message = null;
                    if (mEditTextContent.getText().toString().contains("@") && mConversation.type() == Conversation.ConversationType.GROUP) {
                        message = chatUtils.buildRemindTextMessage(contString);
                        message = message != null ? message : mMessageBuilder.buildTextMessage(contString);
                    } else {
                        message = mMessageBuilder.buildTextMessage(contString);
                    }
                    chatUtils.send(message, null);
                } else {
                    mEditTextContent.setText("");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        storeConversationDraftText();
        unRegisReceiver();
        IMEngine.getIMService(MessageService.class).removeMessageListener(this);//注销IM接收listener
        IMEngine.getIMService(ConversationService.class).removeConversationChangeListener(chatConversationChangeListener);
        EventBus.getDefault().unregister(this);//反注册EventBus
        if (null != groupMemberInfo)
            groupMemberInfo.clear();
        if (null != imgsUrlList)
            imgsUrlList.clear();
        if (null != remindInfo)
            remindInfo.clear();
        if (null != chatUtils)
            chatUtils.stopPlay();
        selectedImagePath.clear();
    }

    /**
     * eventBus 接收发送消息成功之后的回调
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendMsgSuccessEvent event) {
        com.alibaba.wukong.im.Message message = event.getMessage();
        String msg = "onEventMainThread收到了消息：" + message.messageContent();
        Logger.e(msg);
        //如果是发送的图片，将它加入到图片浏览的列表末尾
        if (message.messageContent().type() == MessageContent.MessageContentType.IMAGE) {
            if (((MessageContent.MediaContent) message.messageContent()).url().startsWith("http")) {
                imgsUrlList.add(message.messageId() + Constants.CHAT_CONSTANTS.SEPARATOR +
                        ((MessageContent.MediaContent) message.messageContent()).url());
            }
        }
        remindInfo.clear();
    }

    /**
     * eventBus 接收发送文件消息进度的回调
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendFileMsgEvent event) {
        com.alibaba.wukong.im.Message message = event.getMessage();
        Logger.d("onEventMainThread收到了文件消息进度--->>" + message.sendProgress());
        if (message.messageContent().type() == MessageContent.MessageContentType.FILE) {
            recyclerChatItemAdapter.updateItemView(message);
        }
    }

    /**
     * eventBus 用于刷新对话未读消息刷新
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshChatUnreadEvent event) {
        Logger.d("onEventMainThread用于刷新对话未读消息刷新--->>");
        if (null != mConversation && mConversation.type() == Conversation.ConversationType.GROUP)//判断是否有入圈申请,放在这调用是因为要在右上角menu加载完了后才能正常显示请求到的数字
            chatUtils.circleJoinRequest(mConversation.conversationId());
    }

    /**
     * 监听到网络重新连接
     * eventBus 用于刷新对话未读消息刷新
     *
     * @param event
     */
    //在Android的主线程中运行
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshChatUserInfoEvent event) {
        Logger.d("onEventMainThread用于刷新，刷新获取user--->>");
        bottomRl.setVisibility(View.VISIBLE);
        if (null != mConversation && mConversation.type() == Conversation.ConversationType.GROUP) {
            IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {
                @Override
                public void onException(String arg0, String arg1) {

                }

                @Override
                public void onProgress(User arg0, int arg1) {

                }

                @Override
                public void onSuccess(User user) {
                    if (null != user && null != user.extension(IM_USER_SILENT_STATE_TAG)) {
                        try {
                            mySilentState = Integer.parseInt(user.extension(IM_USER_SILENT_STATE_TAG));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        changeBottomViewBySilentState();
                    }
                }
            }, RenheApplication.getInstance().currentOpenId);
        }
    }

    @Override
    public void onAdded(List<com.alibaba.wukong.im.Message> messageList, DataType dataType) {
        Logger.w("IM onAdded");
        // 会话事件
        if (messageList == null || messageList.isEmpty())
            return;
        // 消息的新增、更改、删除事件
        Iterator<com.alibaba.wukong.im.Message> iter = messageList.iterator();
        while (iter.hasNext()) {
            com.alibaba.wukong.im.Message message = iter.next();
            Conversation conversation = message.conversation();
            if (null != conversation) {
                if (!mConversation.conversationId().equals(conversation.conversationId())) {
                    Logger.e("message 不是这个conversation的，删除>>" + conversation.conversationId());
                    iter.remove();
                }
            } else {
                iter.remove();
            }
        }
        List<ChatMessage> chatMessageList = new ArrayList<>();
        Map<String, String> localPathMap = null;
        for (com.alibaba.wukong.im.Message message : messageList) {
            if (message.senderId() != RenheApplication.getInstance().currentOpenId) {
                ChatMessage chatMessage = new ChatMessage();
                if (mConversation.type() == Conversation.ConversationType.CHAT) {
                    chatMessage.setSenderName(chatToUserName);
                    chatMessage.setSenderUserFace(chatToUserFace);
                    chatMessage.setMessage(message);
                    chatMessageList.add(chatMessage);
                } else if (mConversation.type() == Conversation.ConversationType.GROUP) {
                    if (null != groupMemberInfo) {
                        Member member = groupMemberInfo.get(message.senderId());
                        if (null != member) {
                            chatMessage.setSenderName(member.user().nickname());
                            chatMessage.setSenderUserFace(member.user().avatar());
                            chatMessage.setMessage(message);
                            chatMessageList.add(chatMessage);
                        }
                    }
                }
                if ((message.messageContent().type() == MessageContent.MessageContentType.FILE)) {
                    localPathMap = new HashMap<>();
                    localPathMap.put("localPath", Constants.CACHE_PATH.IM_DOWNLOAD_PATH + message.messageId()
                            + ((MessageContent.FileContent) message.messageContent()).fileName());
                    message.updateLocalExtras(localPathMap);
                } else if (message.messageContent().type() == MessageContent.MessageContentType.IMAGE
                        && ((MessageContent.MediaContent) message.messageContent()).url().startsWith("http")) {
                    imgsUrlList.add(
                            message.messageId() + Constants.CHAT_CONSTANTS.SEPARATOR + ((MessageContent.MediaContent) message.messageContent()).url());
                }
            } else {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setSenderName(RenheApplication.getInstance().getUserInfo().getName());
                chatMessage.setSenderUserFace(RenheApplication.getInstance().getUserInfo().getUserface());
                chatMessage.setMessage(message);
                chatMessageList.add(chatMessage);
            }
        }
        for (ChatMessage chatMessage : chatMessageList) {
            Logger.d("on add cache>>" + chatMessage.getMessage().messageId() + "");
        }
        if (chatMessageList.size() > 1) {
            recyclerChatItemAdapter.addChatMessageItems(chatMessageList);
            if (mRecyclerView != null && chatUtils.isNeedScrollToBottom(layoutManager, null)) {
                if (recyclerChatItemAdapter.getItemCount() - 1 > 0)
                    mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
            }
        } else if (chatMessageList.size() == 1) {
            recyclerChatItemAdapter.addChatMessageItem(chatMessageList.get(0));
            if (mRecyclerView != null && chatUtils.isNeedScrollToBottom(layoutManager, chatMessageList.get(0).getMessage())) {
                if (recyclerChatItemAdapter.getItemCount() - 1 > 0)
                    mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public void onRemoved(List<com.alibaba.wukong.im.Message> list) {
        Logger.w("IM onRemoved");
        recyclerChatItemAdapter.deleteMessagesItems(list);
    }

    @Override
    public void onChanged(List<com.alibaba.wukong.im.Message> list) {
        Logger.w("IM onChanged");
        recyclerChatItemAdapter.updateMessagesItems(list);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //听筒模式不好使，先注释掉了
//        float range = event.values[0];
//        if (range == mSensor.getMaximumRange()) {
//            audioManager.setMode(AudioManager.MODE_NORMAL);
//        } else {
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK)
            switch (requestCode) {
                case Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_CHOOSE_CONTACTS://选取要发送的名片
                    HlContactRenheMember contacts = (HlContactRenheMember) data.getSerializableExtra("contacts");
                    String extramsg = data.getStringExtra("extramsg");
                    sendSelectedVcard(contacts, extramsg);
                    break;
                case Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_REMIND://选取要@的成员
                    if (data != null) {
                        String nickName = data.getStringExtra("name");
                        long openId = data.getLongExtra("openId", 0);
                        chatUtils.handleAtAction(openId, nickName, true);
                    }
                    break;
                case ImageSelector.IMAGE_REQUEST_CODE:
                    if (null != data) {
                        List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
                        for (String path : pathList) {
                            Logger.d("ImagePathList-->" + path);
                        }
                        selectedImagePath.clear();
                        selectedImagePath.addAll(pathList);
                        for (String path : selectedImagePath) {
//                            com.alibaba.wukong.im.Message messageImg = mMessageBuilder.buildImageMessage(path);
//                            chatUtils.send(messageImg, null);
                            if (mMessageSender != null) {
                                mMessageSender.sendAlbumImage(path, mConversation, true);
                            }
                        }
                        selectedImagePath.clear();
                    }
                    break;
                case Constants.CHAT_CONSTANTS.IM_CHAT_TO_CIRCLE:
                    if (null != data) {
                        Conversation conversation = (Conversation) data.getSerializableExtra("conversation");
                        Intent intent = new Intent(getActivity(), ChatMainActivity.class);
                        intent.putExtra("conversation", conversation);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                    getActivity().finish();
                    break;
                case Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_CHECK_TOPIC://查看话题
                    if (null != mConversation && mConversation.type() == Conversation.ConversationType.GROUP)//判断是否有入圈申请,放在这调用是因为要在右上角menu加载完了后才能正常显示请求到的数字
                        chatUtils.circleJoinRequest(mConversation.conversationId());
                    break;
            }
        else if (resultCode == 1) {
            if (requestCode == Constants.CHAT_CONSTANTS.IM_USER_DELETE_CIRCLE) {//解散圈子的回调
                getActivity().finish();
            }
        }
    }

    /**
     * 发送被选中的名片
     *
     * @param contacts 联系人
     * @param extramsg 附带说的话
     */
    private void sendSelectedVcard(HlContactRenheMember contacts, String extramsg) {
        if (contacts != null) {
            String url = "user://" + contacts.getSid();
            String title = getString(R.string.vcard_share_default_name);
            String content = contacts.getName() + "\n" + contacts.getTitle() + "\n" + contacts.getCompany();
            String contactsface = contacts.getUserface();
            com.alibaba.wukong.im.Message message = mMessageBuilder.buildLinkedMessage(url, title, content, contactsface);
            chatUtils.send(message, extramsg);
        }
    }

    /**
     * 发送表情
     */
    @Override
    public void onEmotionSelected(Emotion emotion) {
        Editable editAble = mEditTextContent.getEditableText();
        int start = mEditTextContent.getSelectionStart();
        if ("[删除]".equals(emotion.getKey())) {
            //删除操作
            int iCursorEnd = mEditTextContent.getSelectionEnd();
            if (iCursorEnd > 0) {
                if (iCursorEnd == start) {
                    if (isDelPng(iCursorEnd, mEditTextContent)) {
                        String content = mEditTextContent.getText().toString().substring(0, iCursorEnd);
                        mEditTextContent.getText().delete(content.lastIndexOf("["), iCursorEnd);
                    } else {
                        mEditTextContent.getText().delete(iCursorEnd - 1, iCursorEnd);
                    }
                } else {
                    mEditTextContent.getText().delete(start, iCursorEnd);
                }
            }
        } else
            editAble.insert(start, emotion.getKey());
    }

    /**
     * 发送照片
     */
    @Override
    public void onImageSelected() {
        ImageSelectorUtil.initFragmentImageSelector(this, selectedImagePath);
        hideEmotionView(false);
    }

    /**
     * 发送名片
     */
    @Override
    public void onVCardSelected() {
        startActivityForResult(new Intent(getActivity(), ChooseContactsActivity.class),
                Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_CHOOSE_CONTACTS);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * 发红包
     */
    @Override
    public void onluckyMoneySelected() {
        Intent intent = new Intent(getActivity(), LuckyMoneySendActivity.class);
        intent.putExtra("conversationId", (null != mConversation) ? mConversation.conversationId() : "");
        intent.putExtra("conversationType", (null != mConversation) ? mConversation.type() : Conversation.ConversationType.UNKNOWN);
        if (mConversation.type() == Conversation.ConversationType.GROUP) {
            intent.putExtra("conversationGroupNum", mConversation.totalMembers());
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        if (newfeatherIconIv.getVisibility() == View.VISIBLE) {
            newfeatherIconIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onluckyMoneyAdSelected() {
        Intent intent = new Intent(getActivity(), LuckyMoneyAdFillInfoActivity.class);
        intent.putExtra("conversationId", (null != mConversation) ? mConversation.conversationId() : "");
//        intent.putExtra("conversationType", (null != mConversation) ? mConversation.type() : Conversation.ConversationType.UNKNOWN);
        if (mConversation.type() == Conversation.ConversationType.GROUP) {
            intent.putExtra("conversationGroupNum", mConversation.totalMembers());
        }
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        if (newfeatherIconIv.getVisibility() == View.VISIBLE) {
            newfeatherIconIv.setVisibility(View.GONE);
        }
    }

    /**
     * 判断即将删除的字符串是否是图片占位字符串tempText 如果是：则讲删除整个tempText
     * *
     */
    public boolean isDelPng(int cursor, android.widget.EditText mEditTextContent) {
        if (null == mEditTextContent)
            return false;
        String content = mEditTextContent.getText().toString().substring(0, cursor);
        if (content.length() >= 0) {
            // 判断字符串最后一个是否是“]”
            if (content.endsWith("]")) {
                String checkStr = content.substring(content.lastIndexOf("["), content.length());
                if (null != EmotionsDB.getEmotion(checkStr)) {
                    return true;
                }
            }
        }
        return false;
    }


    private TextWatcher editContentWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            atCount = findAtCount("@", s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            changeSendBtState();
            if (mConversation.type() == Conversation.ConversationType.GROUP) {
                //获取光标前的字符串
                String str = s.toString().substring(0, mEditTextContent.getSelectionStart());
                //当前字符串所有@
                int at = findAtCount("@", s.toString());
                if (at > atCount && str.endsWith("@")) {
                    if (groupMemberInfo.size() > 0) {
                        Intent intent = new Intent(getActivity(), ChatRemindActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list", groupMemberInfo);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_REMIND);
                        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    } else {
                        Toast.makeText(getActivity(), "正在为您获取成员列表", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    };

    /**
     * 输入文本的过滤，根据输入替换库中的表情
     */
    private InputFilter emotionFilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // 是delete直接返回
            if ("".equals(source)) {
                return null;
            }
            byte[] emotionBytes = EmotionsDB.getEmotion(source.toString());
            // 输入的表情字符存在，则替换成表情图片
            if (emotionBytes != null) {
                byte[] data = emotionBytes;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                int size = BaseActivity.getRunningActivity().getResources().getDimensionPixelSize(R.dimen.textSize16);
                bitmap = BitmapUtil.zoomBitmap(bitmap, size);
                SpannableString emotionSpanned = new SpannableString(source.toString());
                ImageSpan span = new ImageSpan(getActivity(), bitmap);
                emotionSpanned.setSpan(span, 0, source.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return emotionSpanned;
            } else {
                return source;
            }
        }

    };

    private void hideEmotionView(boolean showKeyBoard) {
        transitioner.setDuration(0);
        if (showKeyBoard) {
            LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) chatLl.getLayoutParams();
            localLayoutParams.height = layEmotion.getTop();
            localLayoutParams.weight = 0.0F;
            layEmotion.setVisibility(View.GONE);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            SystemUtils.showKeyBoard(mEditTextContent);
            mEditTextContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    unlockContainerHeightDelayed();
                }
            }, 200L);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            unlockContainerHeightDelayed();
            layEmotion.setVisibility(View.GONE);
        }
        changeEmojiImageState(false);
    }

    private void showEmotionView(boolean showAnimation) {
        if (showAnimation) {
            transitioner.setDuration(200);
        } else {
            transitioner.setDuration(0);
        }
        emotionHeight = SystemUtils.getKeyboardHeight(getActivity());
        SystemUtils.hideSoftInput(mEditTextContent);
        layEmotion.getLayoutParams().height = emotionHeight;
        layEmotion.setVisibility(View.VISIBLE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // 2015 05-09 在5.0有navigationbar的手机，高度高了一个statusBar
        int lockHeight = SystemUtils.getAppContentHeight(getActivity());
//        if (Build.VERSION.SDK_INT < 19)
//            lockHeight = lockHeight - statusBarHeight;
        lockContainerHeight(lockHeight);
        changeEmojiImageState(true);
    }

    private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) chatLl.getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) chatLl.getLayoutParams();
        localLayoutParams.height = 0;
        localLayoutParams.weight = 1.0F;
    }

    /**
     * 切换发送键状态
     */
    private void changeSendBtState() {
        if ("".equals(mEditTextContent.getText().toString())) {
            // 文本框为空则显示发送图片按钮
            mSendBtn.setVisibility(View.GONE);
            mAddImgRl.setVisibility(View.VISIBLE);
            mAddImgBtn.setVisibility(View.VISIBLE);
            remindInfo.clear();
        } else {// 否则显示发送文本消息按钮
            mSendBtn.setVisibility(View.VISIBLE);
            mAddImgRl.setVisibility(View.GONE);
            mAddImgBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 切换表情键状态
     *
     * @param isFoused
     */
    private void changeEmojiImageState(boolean isFoused) {
        if (isFoused && emotionFragment.isShowingEmoji())
            imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal_on));
        else
            imagefaceIv.setImageDrawable(getResources().getDrawable(R.drawable.chat_emo_normal));
    }

    @Override
    public void onGetGroupNoticeSuccess(CircleJoinCount result) {
//        result.setUnReadCount(2);
//        result.setUnReadReplyCount(3);
//        result.setUnReadTopiCount(4);
        circleJoinCountResult = result;
        unReadJoinCircleRequestCount = result.getUnReadCount();
        unReadMyTopicNoticeCount = result.getUnReadReplyCount();
        unReadCircleTopicNoticceCount = result.getUnReadTopiCount();
        if (null != unReadNoticeNumTv) {
            totalUnReadNum = unReadJoinCircleRequestCount + unReadMyTopicNoticeCount + unReadCircleTopicNoticceCount;
            initGroupChatUnreadNoticeView(result);
        }
    }

    @Override
    public void onCircleSettingClick() {
        Intent intent = new Intent(getActivity(), ActivityCircleSetting.class);
        intent.putExtra("mConversation", mConversation);
        intent.putExtra("userOpenId", RenheApplication.getInstance().currentOpenId);
        intent.putExtra("imConversationId", mConversation.conversationId());
        intent.putExtra("isNameExist_net", isNameExist_net);
        intent.putExtra("unReadCount", unReadJoinCircleRequestCount);
        startActivityForResult(intent, Constants.CHAT_CONSTANTS.IM_USER_DELETE_CIRCLE);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    class ChatConversationChangeListener extends ConversationChangeListener {
        /**
         * 会话标题变更
         */
        @Override
        public void onTitleChanged(List<Conversation> list) {
            Logger.w("IM消息 onTitleChanged");
            if (doUpdateConversation(list)) {
                if (mConversation.type() == Conversation.ConversationType.GROUP) {
                    EventBus.getDefault().post(new ChangeChatTitleEvent(mConversation.title() + "(" + mConversation.totalMembers() + ")"));
                }
            }
        }

        /**
         * 会话状态变更
         */
        @Override
        public void onStatusChanged(List<Conversation> list) {
            Logger.w("IM消息 onStatusChanged");
            if (doUpdateConversation(list)) {

            }
        }

        /**
         * 会话tag变更
         */
        @Override
        public void onTagChanged(List<Conversation> list) {
            Logger.w("IM消息 onTagChanged");
            if (doUpdateConversation(list)) {

            }
        }

        /**
         * 会话extension变更
         */
        @Override
        public void onExtensionChanged(List<Conversation> list) {
            //TODO 可以讲圈子入圈申请的数量更新放在这
            Logger.w("IM消息 onExtensionChanged");
            if (doUpdateConversation(list)) {

            }
        }

        /**
         * 会话@状态变更
         */
        @Override
        public void onAtMeStatusChanged(List<Conversation> list) {
            Logger.w("IM消息 onAtMeStatusChanged");
            if (doUpdateConversation(list)) {

            }
        }

        /**
         * 会话成员数变更
         */
        @Override
        public void onMemberCountChanged(List<Conversation> list) {
            Logger.w("IM消息 onMemberCountChanged");
            if (doUpdateConversation(list)) {
                initGroupTitle();
            }
        }

        @Override
        public void onLocalExtrasChanged(List<Conversation> list) {
            Logger.w("IM消息 onLocalExtrasChanged");
            if (doUpdateConversation(list)) {

            }
        }

        /**
         * 执行conversation更新的具体操作
         *
         * @param conversationList
         */
        private boolean doUpdateConversation(List<Conversation> conversationList) {
            if (conversationList == null || conversationList.isEmpty())
                return false;
            for (Conversation conversation : conversationList) {
                if (conversation.conversationId().equals(mConversation.conversationId())) {
                    mConversation = conversation;
                    Logger.w("IM消息 doUpdateConversation");
                    return true;
                }
            }
            return false;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    InputMethodManager inputManager = (InputMethodManager) mEditTextContent.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(mEditTextContent, 0);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        groupMenuItem = menu.findItem(R.id.menu_circle_setting);//群聊 进入详情图标
        groupMenuItem.setVisible(false);
        groupMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);//单聊 进入详情图标
        chatMenuItem = menu.findItem(R.id.menu_circle_chat_setting);
        chatMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);//单聊 进入详情图标
        chatMenuItem = menu.findItem(R.id.menu_circle_chat_setting);
        chatMenuItem.setVisible(false);
        chatMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem moreItem = menu.findItem(R.id.archive_more);
        moreItem.setTitle("更多");
        if (null != mConversation && mConversation.type() == Conversation.ConversationType.CHAT) {
            chatMenuItem.setVisible(true);
            moreItem.setVisible(false);
        } else {
            chatMenuItem.setVisible(false);
            moreItem.setVisible(true);
        }
        LinearLayout moreActionView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.badge_actionbar_item_view, null);
        ImageView moreIcon = (ImageView) moreActionView.findViewById(R.id.ic_action);
        moreIcon.setImageResource(R.drawable.ic_action_navigation_more_selector);
        moreItem.setActionView(moreActionView);
        unReadNoticeNumTv = (TextView) moreActionView.findViewById(R.id.tv_unread_numb);
//        initGroupChatUnreadNoticeView(circleJoinCountResult);
        if (null != mConversation && mConversation.type() == Conversation.ConversationType.GROUP)//判断是否有入圈申请,放在这调用是因为要在右上角menu加载完了后才能正常显示请求到的数字
            chatUtils.circleJoinRequest(mConversation.conversationId());
        moreActionView.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onClick(View v) {
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
                }
            }
        });
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_circle_chat_setting:
                intent = new Intent(getActivity(), ChatToGroupActivity.class);
                intent.putExtra("name", chatToUserName);
                intent.putExtra("face", chatToUserFace);
                intent.putExtra("openId", otherOpenId + "");
                intent.putExtra("conversation", mConversation);
                startActivityForResult(intent, Constants.CHAT_CONSTANTS.IM_CHAT_TO_CIRCLE);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initGroupTitle() {
        String isNameExist = mConversation.extension("isNameExist");//获取创建圈子时 圈名确定状态
        if (!TextUtils.isEmpty(isNameExist)) {
            if (isNameExist.equals("false")) {
                EventBus.getDefault().post(new ChangeChatTitleEvent("圈子(" + mConversation.totalMembers() + ")"));
            } else {
                EventBus.getDefault().post(new ChangeChatTitleEvent(chatToUserName + "(" + mConversation.totalMembers() + ")"));
            }
        } else {
            if (!TextUtils.isEmpty(isNameExist_net) && isNameExist_net.equals("false")) {
                EventBus.getDefault().post(new ChangeChatTitleEvent("圈子(" + mConversation.totalMembers() + ")"));
            } else {
                EventBus.getDefault().post(new ChangeChatTitleEvent(chatToUserName + "(" + mConversation.totalMembers() + ")"));
            }
        }
    }

    /**
     * @description 查询字符串中指定字符@出现的个数
     * @date 2015-3-3
     */
    public static int findAtCount(String select, String str) {
        int c = 0;
        Pattern p = Pattern.compile(select, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        while (m.find()) {
            c++;
        }
        return c;

    }

    /**
     * 群聊时，初始化右上角未读数目的view
     */
    private void initGroupChatUnreadNoticeView(CircleJoinCount result) {
        if (totalUnReadNum > 0) {
            unReadNoticeNumTv.setVisibility(View.VISIBLE);
            if (totalUnReadNum > 99) {
                unReadNoticeNumTv.setText("...");
            } else {
                unReadNoticeNumTv.setText(totalUnReadNum + "");
            }
        } else {
            unReadNoticeNumTv.setVisibility(View.INVISIBLE);
        }
        if (null != result && null != chatGroupPupAdapter) {
            chatGroupPupAdapter.refreshAdapterByCircleJoinCount(result);
        }
    }

    /**
     * 根据获取到的用户禁言状态来初始化底部UI
     */
    private void changeBottomViewBySilentState() {
//        if (Constants.TEST_MODE)
//            mySilentState = 0;
        if (mySilentState == 1) {
            bottomExpressionLl.setVisibility(View.GONE);
            bottomSilentLl.setVisibility(View.VISIBLE);
        } else {
            bottomExpressionLl.setVisibility(View.VISIBLE);
            bottomSilentLl.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

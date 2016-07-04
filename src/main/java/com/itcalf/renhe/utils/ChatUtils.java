package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.doraemon.Doraemon;
import com.alibaba.doraemon.audio.AudioMagician;
import com.alibaba.doraemon.audio.OnPlayListener;
import com.alibaba.doraemon.audio.OnRecordListener;
import com.alibaba.wukong.Callback;
import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMConstants;
import com.alibaba.wukong.im.Member;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageBuilder;
import com.alibaba.wukong.im.MessageContent;
import com.alibaba.wukong.im.User;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ChatGroupPupAdapter;
import com.itcalf.renhe.adapter.RecyclerChatItemAdapter;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.bean.CircleJoinCount;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.luckyMoneyAd.LuckyMoneyAdDetailActivity;
import com.itcalf.renhe.context.luckymoney.LuckyMoneyDetailActivity;
import com.itcalf.renhe.eventbusbean.SendMsgSuccessEvent;
import com.itcalf.renhe.view.TipBox;
import com.itcalf.renhe.view.WebViewForIndustryCircle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.common.utils.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import cn.renhe.heliao.idl.money.red.HeliaoRobRed;
import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2015/10/13.
 */
public class ChatUtils {
    private static Context ct;
    private ImageLoader imageLoader;
    private RecyclerView mRecyclerView;
    private RecyclerChatItemAdapter recyclerChatItemAdapter;
    private Conversation mConversation;
    private EditText mEditTextContent;
    private MessageBuilder mMessageBuilder;//消息构造器
    private AudioMagician mAudioMagician;
    private ArrayList<ChatMessage> datasList;
    private HashMap<Long, Member> groupMemberInfo; // 用户存储圈子成员信息
    private HashMap<Long, String> remindInfo;
    private List<String> imgsUrlList;//存储聊天记录里所有的image url
    private Handler mHandler;
    private Vibrator vibrator;
    private int flag = 1;//录制语言时,触摸事件的标志位
    private boolean isCancled = false;//录制语言时,触摸事件的标志位
    private boolean isShosrt = false;//录制语言时,触摸事件的标志位
    private long startVoiceT, endVoiceT;//录制语言时,触摸事件的时间计时
    private ImageView volume;//录制语言时,声音音量显示的控件

    private AnimationDrawable audioAnimationDrawable;
    private final static int RETRYTIME = 10000;//最新拍/截取的照片

    private static ChatCallBack chatCallBack;
    private LuckyMoneyCallBack luckyMoneyCallBack;

    public ChatUtils(Context ct, RecyclerChatItemAdapter adapter, ArrayList<ChatMessage> datasList) {
        this.ct = ct;
        this.recyclerChatItemAdapter = adapter;
        this.datasList = datasList;
        imageLoader = ImageLoader.getInstance();
        mHandler = new Handler();
        this.mAudioMagician = (AudioMagician) Doraemon.getArtifact(AudioMagician.AUDIO_ARTIFACT);
    }

    public ChatUtils(Context ct, Conversation mConversation, RecyclerView mRecyclerView, RecyclerChatItemAdapter recyclerChatItemAdapter,
                     ArrayList<ChatMessage> datasList, HashMap<Long, Member> groupMemberInfo, List<String> imgsUrlList,
                     EditText mEditTextContent, AudioMagician mAudioMagician, HashMap<Long, String> remindInfo,
                     MessageBuilder mMessageBuilder) {
        this(ct, recyclerChatItemAdapter, datasList);
        this.mConversation = mConversation;
        this.mRecyclerView = mRecyclerView;
        this.groupMemberInfo = groupMemberInfo;
        this.imgsUrlList = imgsUrlList;
        this.mEditTextContent = mEditTextContent;
        this.remindInfo = remindInfo;
        this.mMessageBuilder = mMessageBuilder;
        this.mAudioMagician = mAudioMagician;
    }

    public ChatCallBack getChatCallBack() {
        return chatCallBack;
    }

    public void setChatCallBack(ChatCallBack chatCallBack) {
        this.chatCallBack = chatCallBack;
    }

    public void set(ChatCallBack chatCallBack) {
        this.chatCallBack = chatCallBack;
    }

    public LuckyMoneyCallBack getLuckyMoneyCallBack() {
        return luckyMoneyCallBack;
    }

    public void setLuckyMoneyCallBack(LuckyMoneyCallBack luckyMoneyCallBack) {
        this.luckyMoneyCallBack = luckyMoneyCallBack;
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void send(final Message message, final String extramsg) {
        // 文本消息发送后，将草稿信息和输入框内容清除
        if (null != mEditTextContent) {
            mEditTextContent.setText("");
        }
        if (mConversation != null && !TextUtils.isEmpty(mConversation.draftMessage())) {
            mConversation.updateDraftMessage("");
        }
        String mLocalUrl = null;
        if (message.messageContent().type() == MessageContent.MessageContentType.IMAGE
                || message.messageContent().type() == MessageContent.MessageContentType.AUDIO)
            mLocalUrl = ((MessageContent.MediaContent) message.messageContent()).url();
        String mLocalPath = null;
        //发送文件，把文件本地路径放入localExtras，方便从message取出文件本地路径
        if ((message.messageContent().type() == MessageContent.MessageContentType.FILE)) {
            mLocalPath = ((MessageContent.MediaContent.FileContent) message.messageContent()).url();
        }
        final String localFilePath = mLocalPath;
        final String localUrl = mLocalUrl;

        message.sendTo(mConversation, new Callback<Message>() {
            @Override
            public void onSuccess(Message msg) {
                MobclickAgent.onEvent(ct, "IM_sendmsg");
                //给message添加扩展字段，存放message发送者的头像和姓名，方便列表页获取
                ConversationListUtil.updateGroupMessageExtension(msg, RenheApplication.getInstance().getUserInfo().getName(),
                        RenheApplication.getInstance().getUserInfo().getUserface());
                if (null != remindInfo) {
                    remindInfo.clear();
                }
                //如果有额外留言(比如分享消息给好友时，会提示他要不要附带说句话)，发送它
                if (!TextUtils.isEmpty(extramsg)) {
                    send(mMessageBuilder.buildTextMessage(extramsg), null);
                }
                if (msg.messageContent().type() == MessageContent.MessageContentType.IMAGE) {
//                    deleteBitmap(localUrl);// 删除本地压缩图片
                    if (((MessageContent.MediaContent) msg.messageContent()).url().startsWith("http")) {
                        imgsUrlList.add(msg.messageId() + Constants.CHAT_CONSTANTS.SEPARATOR +
                                ((MessageContent.MediaContent) msg.messageContent()).url());
                    }
                } else if (msg.messageContent().type() == MessageContent.MessageContentType.AUDIO) {
                    // 将远程url与本地url关联，这样每次在播放语音消息需访问远程url的时候，可以播放相关联的本地语音文件
                    if (null != mAudioMagician)
                        mAudioMagician.update2RemoteUrl(localUrl,
                                ((MessageContent.MediaContent) msg.messageContent()).url());
                    //发送文件，把文件本地路径放入localExtras，方便从message取出文件本地路径
                } else if ((msg.messageContent().type() == MessageContent.MessageContentType.FILE)) {
                    Map<String, String> localPathMap = new HashMap<>();
                    localPathMap.put("localPath", localFilePath);
                    msg.updateLocalExtras(localPathMap);
                }
                EventBus.getDefault().post(new SendMsgSuccessEvent(msg));
            }

            @Override
            public void onException(String code, String reason) {
                com.orhanobut.logger.Logger.w("消息发送失败！code=" + code + " reason=" + reason);
                switch (code) {
                    case Constants.IM_ERROR_CODE.ERR_CODE_NOT_IN_GROUP:
                        ToastUtil.showErrorToast(ct, R.string.im_error_not_in_group);
                        break;
                    case IMConstants.ErrorCode.ERR_CODE_UPLOADFAILED://图片发送失败
                        ToastUtil.showErrorToast(ct, R.string.im_error_upload);
                        break;
                    case IMConstants.ErrorCode.ERR_CODE_REQUEST_TIMEOUT://超时
                        ToastUtil.showErrorToast(ct, R.string.im_error_timeout);
                        break;
                    default:
                        ToastUtil.showErrorToast(ct, "消息发送失败");
                        break;
                }
            }

            @Override
            public void onProgress(Message msg, int i) {
                if (Constants.DEBUG_MODE) {
                    Log.d("消息发送进度", "*" + i);
                }
            }
        });
    }

    public void handleCacheMessages(List<Message> messages, String chatToUserName, String chatToUserFace, boolean scroolToBottom) {
        if (messages != null && messages.size() > 0) {
            Map<String, String> localPathMap;
            List<ChatMessage> cacheMessages = new ArrayList<>();
            if (mConversation.type() == Conversation.ConversationType.CHAT) {
                for (com.alibaba.wukong.im.Message message : messages) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage(message);
                    if (message.senderId() == RenheApplication.getInstance().currentOpenId) {
                        chatMessage.setSenderName(RenheApplication.getInstance().getUserInfo().getName());
                        chatMessage.setSenderUserFace(RenheApplication.getInstance().getUserInfo().getUserface());
                    } else {
                        chatMessage.setSenderName(chatToUserName);
                        chatMessage.setSenderUserFace(chatToUserFace);
                        if (message.messageContent().type() == MessageContent.MessageContentType.FILE) {
                            localPathMap = new HashMap<>();
                            localPathMap.put("localPath", Constants.CACHE_PATH.IM_DOWNLOAD_PATH + message.messageId()
                                    + ((MessageContent.FileContent) message.messageContent()).fileName());
                            message.updateLocalExtras(localPathMap);
                        }
                    }
                    cacheMessages.add(chatMessage);
                }
            } else if (mConversation.type() == Conversation.ConversationType.GROUP) {
                for (com.alibaba.wukong.im.Message message : messages) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessage(message);
                    String name = null;
                    String userFace = null;
                    if (null != groupMemberInfo && null != groupMemberInfo.get(message.senderId())) {
                        User user = groupMemberInfo.get(message.senderId()).user();
                        if (null != user) {
                            name = user.nickname();
                            userFace = user.avatar();
                        }
                    }
                    if (!TextUtils.isEmpty(name))
                        chatMessage.setSenderName(name);
                    if (!TextUtils.isEmpty(userFace))
                        chatMessage.setSenderUserFace(userFace);
                    cacheMessages.add(chatMessage);

                    if (message.senderId() != RenheApplication.getInstance().currentOpenId
                            && (message.messageContent().type() == MessageContent.MessageContentType.FILE)) {
                        localPathMap = new HashMap<>();
                        localPathMap.put("localPath", Constants.CACHE_PATH.IM_DOWNLOAD_PATH + message.messageId()
                                + ((MessageContent.FileContent) message.messageContent()).fileName());
                        message.updateLocalExtras(localPathMap);
                    }
                }
            }
            recyclerChatItemAdapter.addChatMessageItems(cacheMessages);
            if (cacheMessages.size() < Constants.CHAT_CONSTANTS.REQUEST_COUNT - 1) {
                recyclerChatItemAdapter.setIsEnd(true);
                recyclerChatItemAdapter.setIsShowFooter(false);
            } else {
                recyclerChatItemAdapter.setIsShowFooter(true);
            }
            if (scroolToBottom) {
                // 滚动到底部
//                scrollToBottom();
                if (mRecyclerView != null) {
                    mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
                }
            }
        } else {
            recyclerChatItemAdapter.setIsEnd(true);
            recyclerChatItemAdapter.setIsShowFooter(false);
        }
    }

    /**
     * 获取消息记录中所有的图片类型消息
     */
    public void loadConversationImageMessages() {
        if (null == mConversation)
            return;
        mConversation.listPreviousLocalMessages(null, Integer.MAX_VALUE, MessageContent.MessageContentType.IMAGE, new Callback<List<Message>>() {
            @Override
            public void onException(String arg0, String arg1) {
            }

            @Override
            public void onProgress(List<com.alibaba.wukong.im.Message> arg0, int arg1) {
            }

            @Override
            public void onSuccess(List<com.alibaba.wukong.im.Message> arg0) {
                insetImgUrl2List(arg0, true);
            }
        });
    }

    /**
     * 录音
     */
    private OnRecordListener recordListener = new OnRecordListener() {
        @Override
        public void onRecordStart(String arg0) {
        }

        @Override
        public void onRecordCompleted(String audioPath, List<Integer> volumns, long duration) {
            if (duration < 1000) {
                isCancled = false;
                return;
            }
            if (!isCancled) {
                // 构建语音消息并发送
                Message message = mMessageBuilder.buildAudioMessage(audioPath, duration, volumns);
                send(message, null);
            }
            isCancled = false;
        }

        /**
         * * 每1s钟通知一次采样结果
         *
         *            录音采样结果（最近50个点） 内部采样方法 private void doUpdateSample(){
         *
         *            if (this.mRecorder != null) { int amplitude =
         *            mRecorder.getMaxAmplitude();
         *
         *            int waveformSample = (int) (20 * Math.log(amplitude/120));
         *            // 更新采样数组 mAllSamples.add(waveformSample); }
         *
         *            }
         */
        @Override
        public void notifySampleResult(long duration, List<Integer> volumns) {
            for (Integer vInteger : volumns) {
                Logger.d("volumn>>" + vInteger);
//                updateDisplay((Math.exp(vInteger / 20) * 120) / 2700);
                updateDisplay(Math.round(vInteger / 100));
            }
        }

        @Override
        public void onRecordErrorListener(int i, String s) {
            Toast.makeText(ct, "录音出错", Toast.LENGTH_SHORT).show();
        }

    };

    private void updateDisplay(double signalEMA) {
        if (null == volume)
            return;
        switch ((int) signalEMA) {
            case 0:
            case 1:
                volume.setImageResource(R.drawable.amp1);
                break;
            case 2:
            case 3:
                volume.setImageResource(R.drawable.amp2);

                break;
            case 4:
            case 5:
                volume.setImageResource(R.drawable.amp3);
                break;
            case 6:
            case 7:
                volume.setImageResource(R.drawable.amp4);
                break;
            case 8:
            case 9:
                volume.setImageResource(R.drawable.amp5);
                break;
            case 10:
            case 11:
                volume.setImageResource(R.drawable.amp6);
                break;
            default:
                volume.setImageResource(R.drawable.amp7);
                break;
        }
    }

    /**
     * 处理录音ontouch事件
     *
     * @param event
     * @param mRcdBtn
     * @param del_re
     * @param rcChat_popup
     * @param voice_rcd_hint_loading
     * @param voice_rcd_hint_rcding
     * @param voice_rcd_hint_tooshort
     * @param microPhoneIv
     * @param volume
     * @param descTv
     * @param sc_img1
     */
    public void handleRecordBtTouch(MotionEvent event, TextView mRcdBtn, LinearLayout del_re, final View rcChat_popup,
                                    final LinearLayout voice_rcd_hint_loading, final LinearLayout voice_rcd_hint_rcding,
                                    final LinearLayout voice_rcd_hint_tooshort, final ImageView microPhoneIv, final ImageView volume,
                                    final TextView descTv, final ImageView sc_img1) {
        this.volume = volume;
        // 获取按键在当前窗口内的绝对坐标
        int[] location = new int[2];
        mRcdBtn.getLocationInWindow(location);

        int[] del_location = new int[2];
        del_re.getLocationInWindow(del_location);
        int del_Y = del_location[1];
        int del_x = del_location[0];

        if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) { // 按下录音键开始语音
            // 判断手势按下的位置是否是语音录制按钮的范围内
            if (event.getY() > location[1] && (event.getX() > location[0]) && event.getX() < location[0] + mRcdBtn.getWidth()) {
                vibratePhone();
                mHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        stopVibrate();
                    }
                }, 1000);
                flag = 2;
                mAudioMagician.record(recordListener);
                mRcdBtn.setBackgroundResource(R.drawable.im_chat_speak_bt_aft_p_shape);
                rcChat_popup.setVisibility(View.VISIBLE);
                voice_rcd_hint_loading.setVisibility(View.VISIBLE);
                voice_rcd_hint_rcding.setVisibility(View.GONE);
                voice_rcd_hint_tooshort.setVisibility(View.GONE);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (!isShosrt) {
                            voice_rcd_hint_loading.setVisibility(View.GONE);
                            voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
                        }
                    }
                }, 300);
                mRcdBtn.setText("松开 结束");
                microPhoneIv.setImageResource(R.drawable.voice_rcd_hint);
                volume.setVisibility(View.VISIBLE);
                descTv.setText("手指上滑，取消发送");
                descTv.setBackgroundResource(R.color.transparent);
                startVoiceT = System.currentTimeMillis();
            }
        }
        if (event.getY() < location[1] && flag == 2) {
            Animation mLitteAnimation = AnimationUtils.loadAnimation(ct, R.anim.cancel_rc);
            Animation mBigAnimation = AnimationUtils.loadAnimation(ct, R.anim.cancel_rc2);
            mRcdBtn.setText("松开手指，取消发送");
            microPhoneIv.setImageResource(R.drawable.voice_rcd_cancle);
            volume.setVisibility(View.GONE);
            descTv.setBackgroundResource(R.drawable.im_chat_cancle_audio_shape);
            descTv.setText("松开手指，取消发送");
            del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
            if (event.getY() >= del_Y && event.getY() <= del_Y + del_re.getHeight() && event.getX() >= del_x
                    && event.getX() <= del_x + del_re.getWidth()) {
                del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
                sc_img1.startAnimation(mLitteAnimation);
                sc_img1.startAnimation(mBigAnimation);
            }
        } else if (event.getX() > location[0] && flag == 2) {
            mRcdBtn.setText("松开 结束");
            del_re.setBackgroundResource(0);
            microPhoneIv.setImageResource(R.drawable.voice_rcd_hint);
            volume.setVisibility(View.VISIBLE);
            descTv.setText("手指上滑，取消发送");
            descTv.setBackgroundResource(R.color.transparent);
        }
        if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) { // 松开手势时执行录制完成
            mRcdBtn.setText("按住 说话");
            if (!(event.getY() > location[1] && event.getX() > location[0])) {// 手势划出按钮，表示取消发送
                isCancled = true;
                rcChat_popup.setVisibility(View.GONE);
                microPhoneIv.setImageResource(R.drawable.voice_rcd_hint);
                volume.setVisibility(View.VISIBLE);
                descTv.setText("手指上滑，取消发送");
                descTv.setBackgroundResource(R.color.transparent);
            } else {
                voice_rcd_hint_rcding.setVisibility(View.GONE);
                endVoiceT = System.currentTimeMillis();
                int time = (int) ((endVoiceT - startVoiceT) / 1000);
                if (time < 1) {
                    isCancled = true;
                    isShosrt = true;
                    voice_rcd_hint_loading.setVisibility(View.GONE);
                    voice_rcd_hint_rcding.setVisibility(View.GONE);
                    voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            voice_rcd_hint_tooshort.setVisibility(View.GONE);
                            rcChat_popup.setVisibility(View.GONE);
                            isShosrt = false;
                        }
                    }, 1000);
                }
            }
            mAudioMagician.stopRecord();
            flag = 1;
            mRcdBtn.setBackgroundResource(R.drawable.im_chat_speak_bt_pre_p_shape);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mRcdBtn.setText("按住 说话");
        }
    }

    /**
     * 按住说话时的振动效果
     */
    private void vibratePhone() {
        /*
         * 想设置震动大小可以通过改变pattern来设定，如果开启时间太短，震动效果可能感觉不到
		 */
        vibrator = (Vibrator) ct.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {50, 100}; // 停止 开启 停止 开启
        vibrator.vibrate(pattern, 1); // 重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    public void stopVibrate() {
        vibrator.cancel();
    }

    /**
     * 往图片集合插入数据
     *
     * @param msgList      获取到的一页消息记录
     * @param isInsertHead 这些图片是否需要插入到imgsUrlList头部
     */
    public void insetImgUrl2List(List<com.alibaba.wukong.im.Message> msgList, boolean isInsertHead) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).messageContent().type() == MessageContent.MessageContentType.IMAGE
                    && ((MessageContent.MediaContent) msgList.get(i).messageContent()).url().startsWith("http")) {
                list.add(msgList.get(i).messageId() + Constants.CHAT_CONSTANTS.SEPARATOR
                        + ((MessageContent.MediaContent) msgList.get(i).messageContent()).url());
            }
        }

        if (list.size() > 0) {
            if (isInsertHead) {
                imgsUrlList.addAll(0, list);
            } else {
                imgsUrlList.addAll(list);
            }
        }
    }


    /**
     * ListView滚动到底部
     */
    public void scrollToBottom() {
        Logger.w("scrollToBottom");
        if (mRecyclerView != null) {
//            mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
                }
            }, 100);
        }
    }

    /**
     * @param context
     * @param content
     * @param type    1:文字内容  2:图片  3:分享的内容
     */
    public void createCopyDialog(final Context context, final String content, final int type, final String forwardTitle,
                                 final String forwardLink, final String forwardPicUrl) {
        int arrayId = R.array.im_choice_items;
        switch (type) {
            case Constants.ChatShareType.CHAT_SHARE_TEXT:
                arrayId = R.array.im_choice_items;
                break;
            case Constants.ChatShareType.CHAT_SHARE_IMAGE:
                arrayId = R.array.im_choice_item2;
                break;
            case Constants.ChatShareType.CHAT_SHARE_LINK:
                arrayId = R.array.im_choice_item2;
                break;
            default:
                break;
        }
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(arrayId).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (type) {
                    case Constants.ChatShareType.CHAT_SHARE_TEXT:
                        switch (which) {
                            case 0:
                                copy(context, content);
                                break;
                            case 1:
                                forward(context, type, content, forwardTitle, forwardLink, forwardPicUrl);
                                break;
                            default:
                                break;
                        }
                        break;
                    case Constants.ChatShareType.CHAT_SHARE_IMAGE:
                        switch (which) {
                            case 0:
                                forward(context, type, content, forwardTitle, forwardLink, forwardPicUrl);
                                break;
                            default:
                                break;
                        }
                        break;
                    case Constants.ChatShareType.CHAT_SHARE_LINK:
                        switch (which) {
                            case 0:
                                forward(context, type, content, forwardTitle, forwardLink, forwardPicUrl);
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }

            }
        });
        materialDialog.show();
    }

    private void copy(Context context, String content) {
        MobclickAgent.onEvent(context, "IM_copy");
        ContentUtil.copy(content, context);
        //ContentUtil.showToast(context, "内容已经复制到剪贴板");
        ToastUtil.showToast(context, R.string.already_copy_to_plate);
    }

    private void forward(Context context, int type, String content, String forwardTitle,
                         String forwardLink, String forwardPicUrl) {
        MobclickAgent.onEvent(context, "IM_forward");
        Bundle bundle = new Bundle();

        switch (type) {
            case Constants.ChatShareType.CHAT_SHARE_TEXT:
                bundle.putString("toForwardContent", content);
                bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_TEXT_FORWARD);
                break;
            case Constants.ChatShareType.CHAT_SHARE_IMAGE:
                bundle.putString("toForwardPic", content);
                bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_IMAGE_FORWARD);
                break;
            case Constants.ChatShareType.CHAT_SHARE_LINK:
                bundle.putString("toForwardContent", content);
                bundle.putString("toForwardObjectId", forwardLink);
                bundle.putString("toForwardPic", forwardPicUrl);
                bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_LINK_FORWARD);
                if (forwardLink.startsWith("msg")) {
                    bundle.putString("title", forwardTitle);
                } else if (forwardLink.startsWith("user")) {
                    bundle.putString("title", context.getString(R.string.vcard_share_default_name));
                } else if (forwardLink.startsWith("group")) {
                    bundle.putString("title", context.getString(R.string.cicle_share_default_name));
                }
                break;

            default:
                break;
        }

        Intent intent = new Intent(context, ToShareWithRecentContactsActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    /**
     * @param message 转发的message
     */
    public void createForwardDialog(final Context context, final Message message) {
        int arrayId = R.array.im_choice_item2;
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(arrayId).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                if (which == 0) {
                    MobclickAgent.onEvent(context, "IM_forward");
                    Bundle bundle = new Bundle();
                    switch (message.messageContent().type()) {
                        case MessageContent.MessageContentType.FILE:
                            bundle.putSerializable("message", message);
                            bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD);
                            break;
                        default:
                            break;
                    }

                    Intent intent = new Intent(context, ToShareWithRecentContactsActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        });
        materialDialog.show();
    }

    private void deleteBitmap(String filePath) {
        File f = new File(filePath);
        if (f.exists())
            f.delete();
    }

    /**
     * 判断这条message是否是自己发的
     *
     * @return
     */
    public boolean isSenderIsSelf(Message message) {
        if (null == message)
            return false;
        if (message.senderId() == RenheApplication.getInstance().currentOpenId) {
            return true; // 自己发送的消息
        } else {
            return false; // 别人发送的消息
        }
    }

    public boolean isNeedScrollToBottom(LinearLayoutManager layoutManager, Message message) {
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int pastItems = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        if (lastVisibleItem >= datasList.size() - 2)
            return true;
        if ((pastItems + visibleItemCount) >= totalItemCount - 1) {
            mRecyclerView.scrollToPosition(recyclerChatItemAdapter.getItemCount() - 1);
            return true;
        }
        if (mRecyclerView != null && isSenderIsSelf(message))
            return true;
        return false;
    }

    public void playAudio(Message message, boolean isLeft, ImageView imageView, int position) {
        //如果点击的item正在播放，则停止播放，否则播放这个item
        if (recyclerChatItemAdapter.getIsPlayAudioUrl().equals(((MessageContent.MediaContent) message.messageContent()).url())) {
            stopPlay();
        } else {
            mAudioMagician.play(((MessageContent.MediaContent) message.messageContent()).url(),
                    new AudioOnplayListener(imageView, isLeft, message, position));
            if (!message.iHaveRead()) {
                message.conversation().addUnreadCount(-1);
                message.read();
            }
        }
    }

    public void stopPlay() {
        if (!TextUtils.isEmpty(recyclerChatItemAdapter.getIsPlayAudioUrl())) {
            mAudioMagician.stop(recyclerChatItemAdapter.getIsPlayAudioUrl());
        }
    }

    public void startAudioAnimation(ImageView imageView, boolean isLeft) {
        imageView.clearAnimation();
        if (isLeft) {
            imageView.setImageResource(R.anim.chat_audio_play_left_frame);
        } else {
            imageView.setImageResource(R.anim.chat_audio_play_right_frame);
        }
        recyclerChatItemAdapter.setAudioImageView(imageView);
        if (imageView.getDrawable() instanceof AnimationDrawable) {
            recyclerChatItemAdapter.setAudioAnimationDrawable((AnimationDrawable) imageView.getDrawable());
            if (null != recyclerChatItemAdapter.getAudioAnimationDrawable())
                recyclerChatItemAdapter.getAudioAnimationDrawable().start();// 开始
        }
    }

    public void stopAudioAnimation(ImageView imageView, boolean isLeft) {
        imageView.clearAnimation();
        if (isLeft) {
            imageView.setImageResource(R.drawable.chatfrom_voice_playing_f3);
        } else {
            imageView.setImageResource(R.drawable.chatto_voice_playing_default);
        }
    }

    /**
     * 播放语音图标动画
     */
    class AudioOnplayListener implements OnPlayListener {
        ImageView imageView;
        boolean isLeft;
        Message message;
        int position;

        public AudioOnplayListener(ImageView imageView, boolean isLeft, Message message, int position) {
            this.imageView = imageView;
            this.isLeft = isLeft;
            this.message = message;
            this.position = position;
        }

        /**
         * @param arg0
         * @param arg1 ： 1——开始播放 6——自然结束播放 2——強制結束
         */
        @Override
        public void onPlayStateListener(String arg0, int arg1) {
            if (arg1 == 1) {
                recyclerChatItemAdapter.setIsPlayAudioUrl(((MessageContent.MediaContent) message.messageContent()).url());
                recyclerChatItemAdapter.setAudioImageView(imageView);
                if (isLeft) {
                    imageView.setImageResource(R.anim.chat_audio_play_left_frame);
                } else {
                    imageView.setImageResource(R.anim.chat_audio_play_right_frame);
                }
                if (imageView.getDrawable() instanceof AnimationDrawable) {
                    recyclerChatItemAdapter.setAudioAnimationDrawable((AnimationDrawable) imageView.getDrawable());
                    if (null != recyclerChatItemAdapter.getAudioAnimationDrawable())
                        recyclerChatItemAdapter.getAudioAnimationDrawable().start();// 开始
                }
            } else {
                recyclerChatItemAdapter.setIsPlayAudioUrl("");
                if (null != recyclerChatItemAdapter.getAudioAnimationDrawable()) {
                    recyclerChatItemAdapter.getAudioAnimationDrawable().stop();// 开始
                    recyclerChatItemAdapter.setAudioAnimationDrawable(null);
                }
                imageView.clearAnimation();
                if (null != recyclerChatItemAdapter.getAudioImageView()) {
                    recyclerChatItemAdapter.getAudioImageView().clearAnimation();
                }
                if (isLeft) {
                    imageView.setImageResource(R.drawable.chatfrom_voice_playing_f3);
                    if (null != recyclerChatItemAdapter.getAudioImageView())
                        recyclerChatItemAdapter.getAudioImageView().setImageResource(R.drawable.chatfrom_voice_playing_f3);
                    if (arg1 == 6) {//自然播放结束，继续播放下一个未读语音
                        position += 1;
                        while (position < datasList.size()) {
                            if (!datasList.get(position).getMessage().iHaveRead() && datasList.get(position).getMessage().messageContent()
                                    .type() == MessageContent.MessageContentType.AUDIO) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        datasList.get(position).getChatViewHolder().getContentRl().performClick();
                                    }
                                }, 500);
                                break;
                            }
                            position += 1;
                        }
                    }
                } else {
                    imageView.setImageResource(R.drawable.chatto_voice_playing_default);
                    if (null != recyclerChatItemAdapter.getAudioImageView())
                        recyclerChatItemAdapter.getAudioImageView().setImageResource(R.drawable.chatto_voice_playing_default);
                }
                recyclerChatItemAdapter.setAudioImageView(null);
            }
        }

        @Override
        public void onRequestStart(String s) {

        }

        @Override
        public void onRequestFinsh(String s, int i) {

        }

        @Override
        public void onProgressListener(String arg0, int arg1, int arg2) {

        }

        @Override
        public void onPlayErrorListener(String s, int i, String s1) {

        }
    }

    public void handleAtAction(long openId, String nickName, boolean hasAt) {
        String s1 = mEditTextContent.getText().toString().substring(0, mEditTextContent.getSelectionStart());
        if (!remindInfo.containsKey(openId)) {
            String s2 = "";
            if (s1.length() < mEditTextContent.getText().toString().length())
                s2 = mEditTextContent.getText().toString().substring(mEditTextContent.getSelectionStart(),
                        mEditTextContent.getText().toString().length());

            mEditTextContent.setText(s1 + (hasAt ? nickName : "@" + nickName) + " " + s2);
            mEditTextContent.setSelection(mEditTextContent.getText().toString().length());
            remindInfo.put(openId, nickName);
        } else {
            if (s1.endsWith("@")) {
                mEditTextContent.setText(s1.substring(0, s1.lastIndexOf("@")));
                mEditTextContent.setSelection(mEditTextContent.getText().toString().length());
            }
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollToBottom();
                SystemUtils.showKeyBoard(mEditTextContent);
            }
        }, 200);
    }

    /**
     * 构造文本消息，文本中带有@功能，格式是 @{openId}
     */
    public com.alibaba.wukong.im.Message buildRemindTextMessage(final String msg) {
        HashMap<Long, String> map = new HashMap<>();
        map.putAll(remindInfo);
        return map.size() > 0 ? mMessageBuilder.buildTextMessage(msg, map) : null;
    }

    /**
     * 在对话界面弹框提示30秒内刚拍摄/截屏获取到的图片
     *
     * @param fragment
     * @param parentView
     */
    public void showLatestPhoto(final Fragment fragment, final View parentView, int emojiLayoutHeight) {
        //// TODO: 2015/12/29 wn要将读取图片的操作放在异步线程里，不然点击加号，会有UI卡顿
        TipBox tipBox;
        TimeCount time;
        String imagePath = null;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = fragment.getActivity().getContentResolver();
        //获取jpeg和png格式的文件，并且按照时间进行倒序
        Cursor cursor = contentResolver.query(uri, null,
                MediaStore.Images.Media.MIME_TYPE + "=\"image/jpeg\" or " + MediaStore.Images.Media.MIME_TYPE
                        + "=\"image/png\" or " + MediaStore.Images.Media.MIME_TYPE + "=\"image/jpg\"",
                null, MediaStore.Images.Media.DATE_TAKEN + " desc");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int photoDate = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
                String date = cursor.getString(photoDate);
                long dateLong = Long.parseLong(date);
//                if (System.currentTimeMillis() - dateLong <= 30 * 1000) {//30秒内添加的照片
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                }
            }
            cursor.close();
        }
        if (!TextUtils.isEmpty(imagePath)) {
            final String imagePathFinal = imagePath;
            tipBox = new TipBox(fragment.getActivity(), Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM,
                    new TipBox.OnItemClickListener() {
                        @Override
                        public void onItemClick() {
//                                    startCustomPhotoZoom(imagePathFinal, true);
                        }
                    }, imagePathFinal);
            if (!TextUtils.isEmpty(imagePathFinal)) {
                DisplayMetrics metric = new DisplayMetrics();
                fragment.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);
                int width = metric.widthPixels; // 屏幕高度（像素）
                int height = metric.heightPixels; // 屏幕高度（像素）
                tipBox.showAtLocation(parentView, Gravity.TOP, width, height - emojiLayoutHeight - DensityUtil.dip2px(fragment.getActivity(), 50));
                Logger.d("emojiLayoutHeight>>" + emojiLayoutHeight + ">>" + height);
                time = new TimeCount(RETRYTIME, 1000, tipBox);//构造CountDownTimer对象
                time.start();//开始计时
            }
        }
    }

    public static PopupWindow createMenuPopupWindow(final Fragment fragment, final Conversation conversation, final PopupWindow pop, final ListView listView,
                                                    final ChatGroupPupAdapter chatGroupPupAdapter) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://圈子话题
                        if (null != chatGroupPupAdapter && null != chatGroupPupAdapter.getList()
                                && chatGroupPupAdapter.getList().size() >= 3) {
                            if (!TextUtils.isEmpty(chatGroupPupAdapter.getList().get(0).getDestinationUrl())) {
                                Intent intent = new Intent(fragment.getActivity(), WebViewForIndustryCircle.class);
                                intent.putExtra("url", chatGroupPupAdapter.getList().get(0).getDestinationUrl());
                                if (null != conversation && null != conversation.extension() && null != conversation.extension().get("circleId"))
                                    intent.putExtra("circleId", Integer.parseInt(conversation.extension().get("circleId")));
                                fragment.startActivityForResult(intent, Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_CHECK_TOPIC);
                                fragment.getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        }
                        break;
                    case 1://我的话题
                        if (null != chatGroupPupAdapter && null != chatGroupPupAdapter.getList()
                                && chatGroupPupAdapter.getList().size() >= 3) {
                            if (!TextUtils.isEmpty(chatGroupPupAdapter.getList().get(1).getDestinationUrl())) {
                                Intent intent = new Intent(fragment.getActivity(), WebViewForIndustryCircle.class);
                                intent.putExtra("url", chatGroupPupAdapter.getList().get(1).getDestinationUrl());
                                if (null != conversation && null != conversation.extension() && null != conversation.extension().get("circleId"))
                                    intent.putExtra("circleId", Integer.parseInt(conversation.extension().get("circleId")));
                                fragment.startActivityForResult(intent, Constants.CHAT_CONSTANTS.IM_REQUEST_CODE_CHECK_TOPIC);
                                fragment.getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            }
                        }
                        break;
                    case 2://圈子设置
                        if (null != chatCallBack)
                            chatCallBack.onCircleSettingClick();
                        break;
                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }

    /* 定义一个倒计时的内部类 */
    public class TimeCount extends CountDownTimer {
        TipBox tipBox;

        public TimeCount(long millisInFuture, long countDownInterval, TipBox tipBox) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
            this.tipBox = tipBox;
        }

        @Override
        public void onFinish() {//计时完毕时触发
            if (tipBox != null) {
                tipBox.dismiss();
                tipBox = null;
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    /**
     * 获取某个圈子是否有申请
     */
    public void circleJoinRequest(final String imConversationId) {
        new AsyncTask<String, Void, CircleJoinCount>() {
            @Override
            protected CircleJoinCount doInBackground(String... params) {
                Map<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("sid", params[0]);
                reqParams.put("adSId", params[1]);
                reqParams.put("imConversationId", imConversationId);
                try {
                    CircleJoinCount cjc = (CircleJoinCount) HttpUtil.doHttpRequest(Constants.Http.CIRCLE_JOIN_COUNT, reqParams,
                            CircleJoinCount.class, ct);
                    return cjc;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(CircleJoinCount result) {
                super.onPostExecute(result);
                if (result != null) {
                    if (result.getState() == 1) {
                        if (null != chatCallBack)
                            chatCallBack.onGetGroupNoticeSuccess(result);
                    }
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId());
    }

    public interface ChatCallBack {
        void onGetGroupNoticeSuccess(CircleJoinCount result);//群聊，未读数目（申请加入圈子、行业圈未读消息等）

        void onCircleSettingClick();//右上角下拉菜单，点击圈子设置
    }

    public interface LuckyMoneyCallBack {
        void checkLuckyMoneyRemainSuccess(HeliaoRobRed.CheckRemainResponse remainResponse);//检查红包是否还有剩余成功回调

        void openLuckyMoneySuccess(HeliaoRobRed.OpenRedResponse openRedResponse);//打开红包
    }

    /**
     * 会话系统消息文字点击处理，eg：xx回复了你的话题、xx抢了你的红包
     */
    public static class SystemMsgSpanClick extends ClickableSpan implements View.OnClickListener {
        private Context context;
        private String topicUrl;
        private int systemMsgType;//1：话题 2：红包 3：红包广告
        private boolean isYours;//是否是你发的红包广告 bool值

        public SystemMsgSpanClick(Context context, String topicUrl, int systemMsgType) {//话题url
            this.context = context;
            this.topicUrl = topicUrl;
            this.systemMsgType = systemMsgType;
        }

        public SystemMsgSpanClick(Context context, String topicUrl, int systemMsgType, boolean isYours) {//红包广告构造方法
            this.context = context;
            this.topicUrl = topicUrl;
            this.systemMsgType = systemMsgType;
            this.isYours = isYours;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(View v) {
            if (systemMsgType == 1) {
                if (!TextUtils.isEmpty(topicUrl)) {
                    Intent intent = new Intent();
                    intent.setClass(context, WebViewForIndustryCircle.class);
                    intent.putExtra("url", topicUrl);
                    if (topicUrl.contains("renhe")) {
                        String fix = "?";
                        if (topicUrl.contains("?")) {
                            fix = "&";
                        }
                        intent.putExtra("login",
                                fix + "adSid=" + RenheApplication.getInstance().getUserInfo().getAdSId() + "&sid="
                                        + RenheApplication.getInstance().getUserInfo().getSid());
                    }
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            } else if (systemMsgType == 2) {
                if (!TextUtils.isEmpty(topicUrl)) {
                    Intent intent = new Intent(context, LuckyMoneyDetailActivity.class);
                    intent.putExtra("luckySid", topicUrl);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            } else if (systemMsgType == 3) {
                if (!TextUtils.isEmpty(topicUrl)) {
                    Intent intent = new Intent();
                    if (isYours) {
                        intent.setClass(context, LuckyMoneyDetailActivity.class);
                        intent.putExtra("luckySid", topicUrl);
                    } else {
                        intent.setClass(context, LuckyMoneyAdDetailActivity.class);
                        intent.putExtra("luckyAdSid", topicUrl);
                    }
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        }

    }
}

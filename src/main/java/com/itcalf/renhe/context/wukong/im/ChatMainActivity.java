package com.itcalf.renhe.context.wukong.im;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.eventbusbean.ChangeChatTitleEvent;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * description :测试
 * Created by Chans Renhenet
 * 2015/11/12
 */
public class ChatMainActivity extends BaseActivity {
    private Conversation conversation;
    private String chatToUserName;//对方（单聊或者群聊）的名字
    private String chatToUserFace;//对方（单聊或者群聊）的头像
    public final static String CONVERSATION_ARG = "conversation";
    public final static String CHATTOUSERNAME_ARG = "userName";
    public final static String CHATTOUSERFACE_ARG = "userFace";
    private ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_im_chat);
        //注册EventBus
        EventBus.getDefault().register(this);
        if (null != getIntent().getSerializableExtra(CONVERSATION_ARG)) {
            conversation = (Conversation) getIntent().getSerializableExtra(CONVERSATION_ARG);
        }
        if (null != getIntent().getSerializableExtra(CHATTOUSERNAME_ARG)) {
            chatToUserName = getIntent().getStringExtra(CHATTOUSERNAME_ARG);
        }
        if (null != getIntent().getSerializableExtra(CHATTOUSERFACE_ARG)) {
            chatToUserFace = getIntent().getStringExtra(CHATTOUSERFACE_ARG);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("chatFragment");
        if (null != fragment) {
            getSupportFragmentManager().beginTransaction().remove(fragment);
//            Logger.w("chat fragement已经存在>>" + fragment);
//            chatFragment = (ChatFragment) fragment;
//            if (null != chatFragment.getArguments()) {
//                Bundle bundle = chatFragment.getArguments();
//                bundle.putSerializable(CONVERSATION_ARG, conversation);
//                bundle.putString(CHATTOUSERNAME_ARG, chatToUserName);
//                bundle.putString(CHATTOUSERFACE_ARG, chatToUserFace);
//                bundle.putString("isNameExist_net", getIntent().getStringExtra("isNameExist_net"));
//                getSupportFragmentManager().beginTransaction().show(chatFragment);
//            }
        }
// else {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CONVERSATION_ARG, conversation);
        bundle.putString(CHATTOUSERNAME_ARG, chatToUserName);
        bundle.putString(CHATTOUSERFACE_ARG, chatToUserFace);
        bundle.putString("isNameExist_net", getIntent().getStringExtra("isNameExist_net"));
        chatFragment = ChatFragment.newInstance();
        chatFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.chatFragment, chatFragment, "chatFragment").commit();
//        }
    }

    /**
     * eventBus 接收发送消息成功之后的回调
     *
     * @param event
     */
    public void onEventMainThread(ChangeChatTitleEvent event) {
        setTextValue(event.getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//反注册EventBus
        if (null != chatFragment)
            getSupportFragmentManager().beginTransaction().remove(chatFragment);
    }

    /**
     * Fragment中没有onTouchEvent解决方法
     * Fragment中，注册
     * 接收MainActivity的Touch回调的对象
     * 重写其中的onTouchEvent函数，并进行该Fragment的逻辑处理
     */
    public interface MyTouchListener {
        void onTouchEvent(MotionEvent event);
    }

    // 保存MyTouchListener接口的列表
    private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<>();

    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     *
     * @param listener
     */
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     *
     * @param listener
     */
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove(listener);
    }

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}

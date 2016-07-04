package com.itcalf.renhe.context.fragmentMain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.WriteLogThread;
import com.itcalf.renhe.view.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 长按人脉列表弹出的Dialog
 *
 * @author YZQ
 */
public class ContactDialog extends Dialog {

    private Context context;

    public ContactDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    private TextView tx_nam;
    //	private TextView tx_im, tx_call;
    private TextView avatar_txt;

    private String id = "";
    private String name = "";
    private String userFace = "";
    private int imId;
    private String mobile;
    private String tel;
    private Intent intent;
    private String shortName;
    private int colorIndex;

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_pop_window);
        initListener();
        initData();
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    private void initData() {
        ImageView imageView = (ImageView) findViewById(R.id.avatar_img);
        avatar_txt = (TextView) findViewById(R.id.avatar_txt);
        tx_nam = (TextView) findViewById(R.id.tx_name);
        id = intent.getStringExtra(id);
        name = intent.getStringExtra("name");
        userFace = intent.getStringExtra("userFace");
        imId = intent.getIntExtra("imId", 0);
        mobile = intent.getStringExtra("mobile");
        tel = intent.getStringExtra("tel");
        String icon = intent.getStringExtra("icon");
        type = intent.getIntExtra("type", 0);
        shortName = intent.getStringExtra("shortName");
        colorIndex = intent.getIntExtra("colorIndex", 0);
        if (type == 1 || type == 2) {
            findViewById(R.id.tx_im).setVisibility(View.GONE);
//            imageView.setImageResource(type == 1 ? R.drawable.icon_contact_phone : R.drawable.icon_contact_card);
            imageView.setVisibility(View.GONE);
            avatar_txt.setVisibility(View.VISIBLE);
            avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
            avatar_txt.setText(shortName);
        } else {
            findViewById(R.id.tx_im).setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            avatar_txt.setVisibility(View.GONE);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(icon, imageView, CacheManager.options, CacheManager.animateFirstDisplayListener);
        }

        tx_nam.setText(name);
        if (TextUtils.isEmpty(mobile) && TextUtils.isEmpty(tel)) {
            findViewById(R.id.tx_call).setVisibility(View.GONE);
        }
    }

    private void initListener() {
        findViewById(R.id.tx_im).setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (imId > 0)
                    createIM();
                else
                    Toast.makeText(context, "该用户未开通聊天功能", Toast.LENGTH_SHORT).show();
                //和聊统计
                new WriteLogThread(context, "5.126.1", new String[]{"" + id, "1"}).start();
            }
        });

        findViewById(R.id.tx_call).setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + (TextUtils.isEmpty(mobile) ? tel : mobile)));
                context.startActivity(intent);
                if (isShowing()) {
                    dismiss();
                }
                //和聊统计
                new WriteLogThread(context, "5.126.1", new String[]{"" + id, "2"}).start();
            }
        });
    }

    private void createIM() {
        com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(context, R.string.conversation_creating);
        // 会话标题,注意：单聊的话title,icon默认均为对方openid,传入的值无效
        final StringBuffer title = new StringBuffer();
        title.append(name);
        Message message = null; // 创建会话发送的系统消息,可以不设置

        int convType = Conversation.ConversationType.CHAT; // 会话类型：单聊or群聊
        // 创建会话
        IMEngine.getIMService(ConversationService.class).createConversation(new com.alibaba.wukong.Callback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                //单聊，给conversation添加扩展字段，存放聊天双发的头像姓名，方便聊天列表页获取
                ConversationListUtil.updateChatConversationExtension(conversation, title.toString(), userFace);
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Intent intent = new Intent(context, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
                intent.putExtra("conversation", conversation);
                context.startActivity(intent);
                if (isShowing()) {
                    dismiss();
                }
            }

            @Override
            public void onException(String code, String reason) {
                com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
                Toast.makeText(context, "创建会话失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(Conversation data, int progress) {
            }
        }, title.toString(), userFace, message, convType, Long.parseLong(imId + ""));
    }
}
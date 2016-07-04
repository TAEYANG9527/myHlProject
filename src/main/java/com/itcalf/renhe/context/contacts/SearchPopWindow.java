package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationService;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.ConversationListUtil;

public class SearchPopWindow extends BaseActivity {

	private TextView tx_nam;
	private TextView tx_im, tx_call;

	private String name = "";
	private String userFace = "";
	private int imId;
	private String mobile;
	private String tel;

	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getRenheApplication().addActivity(this);
		new ActivityTemplate().doInActivity(this, R.layout.search_pop_window);
	}

	@Override
	protected void findView() {
		super.findView();

		tx_im = (TextView) findViewById(R.id.tx_im);
		tx_nam = (TextView) findViewById(R.id.tx_name);
	}

	@Override
	protected void initData() {
		super.initData();
		name = getIntent().getStringExtra("name");
		userFace = getIntent().getStringExtra("userFace");
		imId = getIntent().getIntExtra("imId", 0);
		mobile = getIntent().getStringExtra("mobile");
		tel = getIntent().getStringExtra("tel");
		tx_nam.setText(name);
		if (TextUtils.isEmpty(mobile) && TextUtils.isEmpty(tel)) {
			findViewById(R.id.tx_call).setVisibility(View.GONE);
		}
		type = getIntent().getIntExtra("type", 0);
		if (type == 2 || type == 3) {
			tx_im.setVisibility(View.GONE);
		} else {
			tx_im.setVisibility(View.VISIBLE);
		}

	}

	@Override
	protected void initListener() {
		super.initListener();

		findViewById(R.id.tx_im).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (imId > 0)
					createIM();
				else
					Toast.makeText(SearchPopWindow.this, "该用户未开通聊天功能", Toast.LENGTH_SHORT).show();
			}
		});

		findViewById(R.id.tx_call).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + (TextUtils.isEmpty(mobile) ? tel : mobile)));
				startActivity(intent);
				finish();
			}
		});
	}

	private void createIM() {
		com.itcalf.renhe.context.wukong.im.RenheIMUtil.showProgressDialog(SearchPopWindow.this, R.string.conversation_creating);
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
				Intent intent = new Intent(SearchPopWindow.this, com.itcalf.renhe.context.wukong.im.ChatMainActivity.class);
				intent.putExtra("conversation", conversation);
				startActivity(intent);
				SearchPopWindow.this.finish();
			}

			@Override
			public void onException(String code, String reason) {
				com.itcalf.renhe.context.wukong.im.RenheIMUtil.dismissProgressDialog();
				Toast.makeText(SearchPopWindow.this, "创建会话失败", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProgress(Conversation data, int progress) {
			}
		}, title.toString(), userFace, message, convType, Long.parseLong(imId + ""));
	}
}

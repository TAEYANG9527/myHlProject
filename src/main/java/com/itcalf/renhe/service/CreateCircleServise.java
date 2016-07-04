package com.itcalf.renhe.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.wukong.im.db.CircleDao;
import com.itcalf.renhe.dto.MessageBoardOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

public class CreateCircleServise extends Service {
	private CircleDao dao;
	// 圈子id
	private int circleId;
	// im的会话id
	private String imConversationId;
	// 预生成的圈子头像id
	private int preAvatarId;
	// 圈子名称
	private String name;
	// 加入圈子类型
	private int joinType;
	// 圈子公告
	private String note;
	// 圈子成员的im openid数组
	private int[] imMemberIds;

	private int num = 0; // 初始值

	private int errorNum = 0; // 错误次数

	// 数据库读取的记录
	private ArrayList<HashMap<String, Object>> array = new ArrayList<HashMap<String, Object>>();
	private boolean searchAble = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		dao = new CircleDao(CreateCircleServise.this);
		initData();
		return super.onStartCommand(intent, flags, startId);
	}

	private void initData() {
		array = dao.queryCircleInfo(((RenheApplication) getApplicationContext()).getUserInfo().getSid(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId());
		if (array.size() > 0) {
			handler.sendEmptyMessage(0);
		} else {
			handler.sendEmptyMessage(2);
		}
	}

	/**
	 * 创建圈子
	 * */
	private void CreateCircle() {
		new AsyncTask<String, Void, MessageBoardOperation>() {
			@Override
			protected MessageBoardOperation doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().createCircle(params[0],
							params[1], circleId, imConversationId, preAvatarId, name, joinType, note, imMemberIds, searchAble,
							CreateCircleServise.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(MessageBoardOperation result) {
				super.onPostExecute(result);
				if (result != null && result.getState() == 1) {
					handler.sendEmptyMessage(3);
					num++;
					if (num < array.size())
						handler.sendEmptyMessage(0);
					else
						handler.sendEmptyMessage(2);
				} else {
					handler.sendEmptyMessage(1);
				}
			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				circleId = Integer.parseInt(String.valueOf(array.get(array.size() - 1 - num).get("circleId")));
				imConversationId = String.valueOf(array.get(array.size() - 1 - num).get("imConversationId"));
				preAvatarId = Integer.parseInt(String.valueOf(array.get(array.size() - 1 - num).get("preAvatarId")));
				name = String.valueOf(array.get(array.size() - 1 - num).get("name"));
				joinType = Integer.parseInt(String.valueOf(array.get(array.size() - 1 - num).get("joinType")));
				note = String.valueOf(array.get(array.size() - 1 - num).get("note"));
				imMemberIds = intParseLong(String.valueOf(array.get(array.size() - 1 - num).get("imMemberIds")));
				searchAble = (Boolean) (array.get(array.size() - 1 - num).get("searchAble"));
				CreateCircle();
				break;
			case 1:
				errorNum++; // 请求失败次数超过三次则不再请求 终止服务
				if (errorNum < 3)
					handler.sendEmptyMessageAtTime(0, 3000);
				else
					handler.sendEmptyMessage(2);
				break;
			case 2:
				CreateCircleServise.this.stopSelf();
				break;
			case 3:
				dao.deleterCircleInfo(((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(), circleId + "");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	private int[] intParseLong(String result) {
		String[] s = result.split(",");
		int[] l = new int[s.length];
		for (int i = 0; i < s.length; i++) {
			l[i] = Integer.parseInt(s[i]);
		}
		return l;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

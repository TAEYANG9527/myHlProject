package com.itcalf.renhe.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.ShareUtil;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: SharePopupWindow.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-10-8 下午6:50:00 <br>
 * @author wangning
 */
public class SharePopupWindow extends PopupWindow {
	PackageManager mPackageManager;
	private String mOtherSid;
	private String userName;
	private String userDesp;
	private String userFaceUrl;
	private String userCompany;
	private String userJob;
	private String userContent;
	private String userSid;
	private int messageId;
	private boolean isFromArchieve = true;

	private String mQQAppid = "";
	private QQAuth mQQAuth;
	private int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
	private static final String SHARE_URL = "http://www.renhe.cn/messageboard/";
	private Context ct;
	private LinearLayout qqLayout;
	private LinearLayout weixinLayout;
	private LinearLayout friendLayout;
	private LinearLayout weiboLayout;
	private Button cancelBt;
	private ImageView qqIv;
	private ImageView weixinIv;
	private ImageView friendIv;
	private ImageView weiboIv;
	private LinearLayout renmaiquanLayout;
	private ImageView renmaiquanIv;
	private LinearLayout renheFriendLayout;
	private ImageView renheFriendIv;
	protected List<Bitmap> cacheBitmapList;
	private String mObjectId;
	private String mrawContent;
	private String mtoForwardContent;
	private String mtoForwardPic;
	private String logtype;
	private int origin;
	private int desc;//desc：1:QQ,2:微信,3:朋友圈,4:微博,5:人脉圈,6:朋友
	private ShareUtil shareUtil;
	@SuppressWarnings("deprecation")
	public SharePopupWindow(Context mContext, View parent, String mOtherSid, String userName, String userDesp, String userFaceUrl,
			String userCompany, String userJob, String userContent, String userSid, int messageId, boolean isFromArchieve,
			String objectId, String rawContent, String toForwardContent, String toForwardPic, String logtype, int origin) {//origin:来源，1:列表页;2:详情页

		ct = mContext;
		cacheBitmapList = new ArrayList<Bitmap>();
		final View view = View.inflate(mContext, R.layout.share_popupwindows, null);
		//		view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
		LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
		qqLayout = (LinearLayout) view.findViewById(R.id.qqLl);
		weixinLayout = (LinearLayout) view.findViewById(R.id.weixinLl);
		friendLayout = (LinearLayout) view.findViewById(R.id.friendLl);
		weiboLayout = (LinearLayout) view.findViewById(R.id.weiboLl);
		qqIv = (ImageView) view.findViewById(R.id.qqiv);
		weixinIv = (ImageView) view.findViewById(R.id.weixiniv);
		friendIv = (ImageView) view.findViewById(R.id.friendiv);
		weiboIv = (ImageView) view.findViewById(R.id.weiboiv);
		weiboLayout = (LinearLayout) view.findViewById(R.id.weiboLl);
		qqIv = (ImageView) view.findViewById(R.id.qqiv);
		renmaiquanLayout = (LinearLayout) view.findViewById(R.id.renmaiQuanLl);
		renmaiquanIv = (ImageView) view.findViewById(R.id.renmaiQuaniv);
		renheFriendLayout = (LinearLayout) view.findViewById(R.id.renmaiFriendLl);
		renheFriendIv = (ImageView) view.findViewById(R.id.renmaiFriendiv);
		mObjectId = objectId;
		mrawContent = rawContent;
		mtoForwardContent = toForwardContent;
		mtoForwardPic = toForwardPic;
		cancelBt = (Button) view.findViewById(R.id.item_popupwindows_cancel);
		view.startAnimation(AnimationUtils.loadAnimation(ct, R.anim.fade_in));
		ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.share_push_bottom_in));

		setWidth(LayoutParams.FILL_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		ColorDrawable cd = new ColorDrawable(-0000);
		setBackgroundDrawable(cd);
		//			setBackgroundDrawable(new BitmapDrawable());
		setFocusable(true);
		setOutsideTouchable(true);
		setContentView(view);
		showAtLocation(parent, Gravity.BOTTOM, 0, 0);
		update();
		view.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int height = view.findViewById(R.id.ll_popup).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});

		if (null != mOtherSid && !"".equals(mOtherSid)) {
			this.mOtherSid = mOtherSid;
		}
		if (null != userName && !"".equals(userName)) {
			this.userName = userName;
		}
		if (!TextUtils.isEmpty(userDesp)) {
			this.userDesp = userDesp;
		}
		if (!TextUtils.isEmpty(userFaceUrl)) {
			this.userFaceUrl = userFaceUrl;
		}
		if (!TextUtils.isEmpty(userCompany)) {
			this.userCompany = userCompany;
		}
		if (!TextUtils.isEmpty(userJob)) {
			this.userJob = userJob;
		}
		if (!TextUtils.isEmpty(userContent)) {
			this.userContent = userContent;
		}
		if (!TextUtils.isEmpty(userSid)) {
			this.userSid = userSid;
		}
		this.messageId = messageId;
		this.isFromArchieve = isFromArchieve;
		mPackageManager = ct.getPackageManager();
		qqLayout.setOnClickListener(new ShareClickListener());
		weixinLayout.setOnClickListener(new ShareClickListener());
		friendLayout.setOnClickListener(new ShareClickListener());
		weiboLayout.setOnClickListener(new ShareClickListener());
		renmaiquanLayout.setOnClickListener(new ShareClickListener());
		renheFriendLayout.setOnClickListener(new ShareClickListener());
		cancelBt.setOnClickListener(new ShareClickListener());
		setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				if (cacheBitmapList != null && cacheBitmapList.size() > 0) {
					for (int i = 0; i < cacheBitmapList.size(); i++) {
						if (cacheBitmapList.get(i) != null && !cacheBitmapList.get(i).isRecycled()) {
							cacheBitmapList.get(i).recycle();
						}
					}
					cacheBitmapList.clear();
					cacheBitmapList = null;
				}
				System.gc();
			}
		});
		this.logtype = logtype;
		this.origin = origin;
		shareUtil = new ShareUtil(ct,userName,messageId,userContent,toForwardPic);
	}

	class ShareClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.qqLl:
				MobclickAgent.onEvent(ct, "将人脉圈内容分享到QQ");
				if (checkApkExist(ct, "com.tencent.mobileqq")) {
					shareUtil.share2QQ();
				} else {
					Toast.makeText(ct, "您还未安装QQ", Toast.LENGTH_SHORT).show();
				}
				dismiss();
				desc = 1;
				break;
			case R.id.weixinLl:
				MobclickAgent.onEvent(ct, "将人脉圈内容分享到微信");
				if (checkApkExist(ct, "com.tencent.mm")) {
					shareUtil.share2Tencent(false);
				} else {
					Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
				}
				dismiss();
				desc = 2;
				break;
			case R.id.friendLl:
				MobclickAgent.onEvent(ct, "将人脉圈内容分享到朋友圈");
				if (checkApkExist(ct, "com.tencent.mm")) {
					shareUtil.share2Tencent(true);
				} else {
					Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
				}
				dismiss();
				desc = 3;
				break;
			case R.id.weiboLl:
				MobclickAgent.onEvent(ct, "将人脉圈内容分享到微博");
				shareUtil.share2Weibo();
				dismiss();
				desc = 4;
				break;
			case R.id.renmaiFriendLl:
				MobclickAgent.onEvent(ct, ct.getString(R.string.share_to_hlfriends));
				if (null != mObjectId) {
					ShareUtil.shareRenMaiQuanToHlFriend(ct, mObjectId,userName, mtoForwardPic, mtoForwardContent,Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
				}
				dismiss();
				desc = 6;
				break;
			case R.id.renmaiQuanLl:
				MobclickAgent.onEvent(ct, "将人脉圈内容分享到人脉圈");
				ShareUtil.shareRenMaiQuanToRenMaiQuan(ct, mObjectId, mtoForwardPic, mtoForwardContent, userName, mrawContent);
				dismiss();
				desc = 5;
				break;
			case R.id.item_popupwindows_cancel:
				dismiss();
			default:
				break;
			}
			//和聊统计
			String content = "";
			if (logtype.equals("5.108")) {
				content = logtype + LoggerFileUtil.getConstantInfo(ct) + "|" + origin + "|" + desc;
			} else {
				content = logtype + LoggerFileUtil.getConstantInfo(ct) + "|" + desc;
			}
			LoggerFileUtil.writeFile(content, true);
		}

	}

	public boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

}

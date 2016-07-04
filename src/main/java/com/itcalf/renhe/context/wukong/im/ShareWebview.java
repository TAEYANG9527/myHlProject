package com.itcalf.renhe.context.wukong.im;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.ConversationShareType;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.WebViewContent;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ShareUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Title: SharePopupWindow.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-10-8 下午6:50:00 <br>
 * @author wangning
 */
@SuppressLint("ViewConstructor")
public class ShareWebview extends PopupWindow {
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
	PackageManager mPackageManager;
	private String shareTitle = "和聊 - 行业头条";
	private String shareContent = "";
	private String sharePic;
	private String shareUrl;

	private String mQQAppid = "";
	private QQAuth mQQAuth;
	private int shareType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT;
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
	protected List<Bitmap> cacheBitmapList;
	private Bitmap qqBitmap;
	private Bitmap weixinBitmap;
	private Bitmap friendBitmap;
	private Bitmap weiboBitmap;
	private LinearLayout renmaiquanLayout;
	private LinearLayout renheFriendLayout;
	private boolean mNoNeedGetWeb = false;
	private int shareId;
	private ShareUtil shareUtil;
	@SuppressWarnings("deprecation")
	public ShareWebview(Context mContext, View parent, String sharePic, String shareTitle, String shareContent, String shareUrl,
			boolean noNeedGetWeb) {
		ct = mContext;
		this.shareTitle = shareTitle;
		this.sharePic = sharePic;
		this.shareContent = shareContent;
		this.shareUrl = shareUrl;
		this.mNoNeedGetWeb = noNeedGetWeb;
		cacheBitmapList = new ArrayList<Bitmap>();

		cacheBitmapList = new ArrayList<Bitmap>();
		final View view = View.inflate(mContext, R.layout.share_popupwindows, null);
		LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
		qqLayout = (LinearLayout) view.findViewById(R.id.qqLl);
		weixinLayout = (LinearLayout) view.findViewById(R.id.weixinLl);
		friendLayout = (LinearLayout) view.findViewById(R.id.friendLl);
		weiboLayout = (LinearLayout) view.findViewById(R.id.weiboLl);
		qqIv = (ImageView) view.findViewById(R.id.qqiv);
		weixinIv = (ImageView) view.findViewById(R.id.weixiniv);
		friendIv = (ImageView) view.findViewById(R.id.friendiv);
		weiboIv = (ImageView) view.findViewById(R.id.weiboiv);
		renmaiquanLayout = (LinearLayout) view.findViewById(R.id.renmaiQuanLl);
		renheFriendLayout = (LinearLayout) view.findViewById(R.id.renmaiFriendLl);
		cancelBt = (Button) view.findViewById(R.id.item_popupwindows_cancel);
		view.startAnimation(AnimationUtils.loadAnimation(ct, R.anim.fade_in));
		ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.share_push_bottom_in));
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		ColorDrawable cd = new ColorDrawable(-0000);
		setBackgroundDrawable(cd);
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
		mPackageManager = ct.getPackageManager();
		qqLayout.setOnClickListener(new ShareClickListener());
		weixinLayout.setOnClickListener(new ShareClickListener());
		friendLayout.setOnClickListener(new ShareClickListener());
		weiboLayout.setOnClickListener(new ShareClickListener());
		cancelBt.setOnClickListener(new ShareClickListener());
		renmaiquanLayout.setOnClickListener(new ShareClickListener());
		renheFriendLayout.setOnClickListener(new ShareClickListener());
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
	}

	class ShareClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.item_popupwindows_cancel) {
				dismiss();
			} else {
				getWebViewContent(arg0.getId());
			}
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

	private void getWebViewContent(final int viewId) {
		new GetWebViewContentTask(ct) {
			public void doPre() {
				((Activity) ct).showDialog(1);
			};

			public void doPost(WebViewContent result) {
				((Activity) ct).removeDialog(1);
				if (null != result && result.getState() == 1) {
					shareId = result.getId();
					shareTitle = result.getTitle();
					shareContent = result.getDescribe();
					if (TextUtils.isEmpty(shareContent))
						shareContent = result.getTitle();
					sharePic = result.getPicUrl();
					shareId = result.getId();
					clickItem(viewId);
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), shareUrl);
	}

	private void clickItem(int viewId) {
		switch (viewId) {
		case R.id.qqLl:
			MobclickAgent.onEvent(ct, "将行业头条分享到QQ");
			if (checkApkExist(ct, "com.tencent.mobileqq")) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				imageLoader.loadImage(sharePic, new AnimateFirstDisplayListener(1));
			} else {
				Toast.makeText(ct, "您还未安装QQ", Toast.LENGTH_SHORT).show();
			}
			dismiss();
			break;
		case R.id.weixinLl:
			MobclickAgent.onEvent(ct, "将行业头条分享到微信");
			if (checkApkExist(ct, "com.tencent.mm")) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				imageLoader.loadImage(sharePic, new AnimateFirstDisplayListener(2));
			} else {
				Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
			}
			dismiss();
			break;
		case R.id.friendLl:
			MobclickAgent.onEvent(ct, "将行业头条分享到朋友圈");
			if (checkApkExist(ct, "com.tencent.mm")) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				imageLoader.loadImage(sharePic, new AnimateFirstDisplayListener(3));
			} else {
				Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
			}
			dismiss();
			break;
		case R.id.weiboLl:
			MobclickAgent.onEvent(ct, "将行业头条分享到微博");
			if (checkApkExist(ct, "com.sina.weibo")) {
				shareUtil = new ShareUtil(ct,shareTitle,shareUrl,shareContent,sharePic);
				shareUtil.share2Weibo();
			} else {
				Toast.makeText(ct, "您还未安装新浪微博", Toast.LENGTH_SHORT).show();
			}
			dismiss();
			break;
		case R.id.renmaiFriendLl:
			MobclickAgent.onEvent(ct, ct.getString(R.string.share_to_hlfriends));
			ShareUtil.shareWebToHlFriend(ct,shareUrl,sharePic,shareContent,shareTitle,ConversationShareType.CONVERSATION_SEND_FROM_WEBVIEW_SHARE);
			dismiss();
			break;
		case R.id.renmaiQuanLl:
			MobclickAgent.onEvent(ct, "将人脉圈内容分享到人脉圈");
			ShareUtil.shareWebToRenMaiQuan(ct,shareId,sharePic,shareContent);
			dismiss();
			break;
		case R.id.item_popupwindows_cancel:
			dismiss();
		default:
			break;
		}
	}

	class GetWebViewContentTask extends BaseAsyncTask<WebViewContent> {
		private Context mContext;

		public GetWebViewContentTask(Context mContext) {
			super(mContext);
			this.mContext = mContext;
		}

		@Override
		public void doPre() {

		}

		@Override
		public void doPost(WebViewContent result) {

		}

		@Override
		protected WebViewContent doInBackground(String... params) {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());// 
			reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());// 
			reqParams.put("url", params[0]);// 
			try {
				WebViewContent mb = (WebViewContent) HttpUtil.doHttpRequest(Constants.Http.GET_WEBVIEW_CONTENT, reqParams,
						WebViewContent.class, null);
				return mb;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
		int type;

		public AnimateFirstDisplayListener(int type) {
			this.type = type;
		}

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			shareUtil = new ShareUtil(ct,shareTitle,shareUrl,shareContent,sharePic);
			switch (type) {
			case 1:
				shareUtil.share2QQ();
				break;
			case 2:
				shareUtil.share2Tencent(false);
				break;
			case 3:
				shareUtil.share2Tencent(true);
				break;

			default:
				break;
			}
		}

		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			super.onLoadingFailed(imageUri, view, failReason);
			switch (type) {
			case 1:
				shareUtil.share2QQ();
				break;
			case 2:
				shareUtil.share2Tencent(false);
				break;
			case 3:
				shareUtil.share2Tencent(true);
				break;

			default:
				break;
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			super.onLoadingCancelled(imageUri, view);
			switch (type) {
			case 1:
				shareUtil.share2QQ();
				break;
			case 2:
				shareUtil.share2Tencent(false);
				break;
			case 3:
				shareUtil.share2Tencent(true);
				break;
			default:
				break;
			}
		}
	}
}
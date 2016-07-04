package com.itcalf.renhe.context.more;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.CircleQrcodeInfo;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.SelfTwoDimenCodeMessageBoardOperation;
import com.itcalf.renhe.utils.ShareUtil;
import com.itcalf.renhe.utils.WriteLogThread;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * Title: TwoDimencodeActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-3-17 下午6:20:11 <br>
 * @author wangning
 */
public class TwoDimencodeActivity extends BaseActivity {
	private RelativeLayout rootRl;
	private ImageView twoDcodeImg;
	private TextView circleNameTv;
	private LinearLayout qqLayout;
	private LinearLayout weixinLayout;
	private LinearLayout friendLayout;
	private LinearLayout weiboLayout;
	private LinearLayout renmaiquanLayout;
	private LinearLayout renheFriendLayout;
	private LinearLayout smsLl;
	private TextView shareTipTv;

	private String circleId;
	private int DEFAULT_IMAGE;
	private DisplayImageOptions options;
	private String circleName;
	private int type;
	public static final int SHARE_TYPE_CIRCLE = 0;//分享圈子
	public static final int SHARE_TYPE_ARCHIVE = 1;//分享个人档案
	private Profile profile;
	private String httpShortUrl;
	private String circleCodeUrl;
	private String conversationId;
	private String circleDesp;
	private ShareUtil shareUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.two_dimencode);
	}

	@Override
	protected void findView() {
		super.findView();
		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
		circleNameTv = (TextView) findViewById(R.id.cirleNameTv);
		twoDcodeImg = (ImageView) findViewById(R.id.showImg);
		twoDcodeImg.setEnabled(true);
		DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
		qqLayout = (LinearLayout) findViewById(R.id.qqLl);
		weixinLayout = (LinearLayout) findViewById(R.id.weixinLl);
		friendLayout = (LinearLayout) findViewById(R.id.friendLl);
		weiboLayout = (LinearLayout) findViewById(R.id.weiboLl);
		weiboLayout = (LinearLayout) findViewById(R.id.weiboLl);
		renmaiquanLayout = (LinearLayout) findViewById(R.id.renmaiQuanLl);
		renheFriendLayout = (LinearLayout) findViewById(R.id.renmaiFriendLl);
		smsLl = (LinearLayout) findViewById(R.id.smsLl);
		shareTipTv = (TextView) findViewById(R.id.share_code_tip_tv);
	}

	@Override
	protected void initData() {
		super.initData();
		type = getIntent().getIntExtra("type", SHARE_TYPE_CIRCLE);
		switch (type) {
		case SHARE_TYPE_CIRCLE:
			setTextValue("分享圈子");
			circleName = getIntent().getStringExtra("circleName");
			circleDesp = getIntent().getStringExtra("circleDesp");
			circleCodeUrl = getIntent().getStringExtra("circleCodeUrl");
			if (!TextUtils.isEmpty(circleName))
				circleNameTv.setText(circleName);
			if (TextUtils.isEmpty(circleName))
				circleName = "圈子";
			circleId = getIntent().getStringExtra("circleId");
			conversationId = getIntent().getStringExtra("conversationId");
			httpShortUrl = getIntent().getStringExtra("httpShortUrl");
			options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
					.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
					.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
			shareUtil = new ShareUtil(this, circleId, circleName, circleDesp, circleCodeUrl, httpShortUrl);
			getCircleQrcode();
			break;
		case SHARE_TYPE_ARCHIVE:
			setTextValue("分享");
			circleNameTv.setVisibility(View.GONE);
			shareTipTv.setText(getString(R.string.share_code_tip));
			//			friendLayout.setVisibility(View.GONE);
			//			weiboLayout.setVisibility(View.GONE);
			//			smsLl.setVisibility(View.GONE);
			if (null != getIntent().getSerializableExtra("profile")) {
				circleCodeUrl = profile.getUserInfo().getUserface();
			} else {
				circleCodeUrl = RenheApplication.getInstance().getUserInfo().getUserface();
			}
			getArchiveQrcode();
			break;
		default:
			break;
		}

	}

	@Override
	protected void initListener() {
		super.initListener();
		qqLayout.setOnClickListener(new ShareClickListener());
		weixinLayout.setOnClickListener(new ShareClickListener());
		friendLayout.setOnClickListener(new ShareClickListener());
		weiboLayout.setOnClickListener(new ShareClickListener());
		renmaiquanLayout.setOnClickListener(new ShareClickListener());
		renheFriendLayout.setOnClickListener(new ShareClickListener());
		smsLl.setOnClickListener(new ShareClickListener());
	}

	class ShareClickListener implements View.OnClickListener {

		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.qqLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareQQ");
				if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile, Constants.SHARE_SOURCE_TYPE.SHARE_QQ);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_QQ);
					}
				}
				shareUtil.share2QQ();
				break;
			case R.id.weixinLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareWX");
				if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile, Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN);
					}
				}
				shareUtil.share2Tencent(false);
				break;
			case R.id.friendLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareWXFriend");
				if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile,
								Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN_TIMELINE);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN_TIMELINE);
					}
				}
				shareUtil.share2Tencent(true);
				break;
			case R.id.weiboLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareWB");
				if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile,
								Constants.SHARE_SOURCE_TYPE.SHARE_SINNAWEIBO);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_SINNAWEIBO);
					}
				}
				shareUtil.share2Weibo();
				break;
			case R.id.renmaiFriendLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareRMFriend");
				if (type == SHARE_TYPE_CIRCLE) {
					shareUtil.shareCircle2HlFriend();
				} else if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile,
								Constants.SHARE_SOURCE_TYPE.SHARE_HL_FRIEND);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_HL_FRIEND);
					}
					shareUtil.shareArchive2HlFriend(profile);
				}
				break;
			case R.id.renmaiQuanLl:
				MobclickAgent.onEvent(TwoDimencodeActivity.this, "circle_QRcode_shareRM");
				if (type == SHARE_TYPE_CIRCLE) {
					shareUtil.shareCircle2Renmaiquan();
				} else if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile,
								Constants.SHARE_SOURCE_TYPE.SHARE_HL_CIRCLE);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_HL_CIRCLE);
					}
					shareUtil.shareArchive2Renmaiquan(profile);
				}
				break;
			case R.id.smsLl:
				if (type == SHARE_TYPE_ARCHIVE) {
					if (null != getIntent().getSerializableExtra("profile")) {
						profile = (Profile) getIntent().getSerializableExtra("profile");
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, profile,
								Constants.SHARE_SOURCE_TYPE.SHARE_SMS);
					} else {
						shareUtil = new ShareUtil(TwoDimencodeActivity.this, Constants.SHARE_SOURCE_TYPE.SHARE_SMS);
					}
				}
				shareUtil.share2SMS();
				break;
			default:
				break;
			}
		}

	}

	/**
	 * 获取getCircleQrcode()圈子二维码
	 * */
	private void getCircleQrcode() {
		new AsyncTask<String, Void, CircleQrcodeInfo>() {
			@Override
			protected CircleQrcodeInfo doInBackground(String... params) {
				try {
					return ((RenheApplication) getApplicationContext()).getMessageBoardCommand().getCircleQrcode(params[0],
							params[1], circleId, TwoDimencodeActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(CircleQrcodeInfo result) {
				super.onPostExecute(result);
				RenheIMUtil.dismissProgressDialog();
				if (result != null && result.getState() == 1) {
					ImageLoader imageLoader = ImageLoader.getInstance();
					try {
						imageLoader.displayImage(result.getQrcode(), twoDcodeImg, options);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}.executeOnExecutor(Executors.newCachedThreadPool(),
				((RenheApplication) getApplicationContext()).getUserInfo().getAdSId(),
				((RenheApplication) getApplicationContext()).getUserInfo().getSid());
	}

	/**
	 * 获取个人档案二维码
	 * */
	private void getArchiveQrcode() {
		new GetSelfTwoDimenCodeTask(this) {
			public void doPre() {
			};

			public void doPost(SelfTwoDimenCodeMessageBoardOperation result) {
				if (null != result && result.getState() == 1) {
					ImageLoader imageLoader = ImageLoader.getInstance();
					try {
						imageLoader.displayImage(result.getQrcode(), twoDcodeImg, options);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
		}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
				RenheApplication.getInstance().getUserInfo().getAdSId(), RenheApplication.getInstance().getUserInfo().getSid());
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
	}

}

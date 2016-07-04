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
import com.itcalf.renhe.dto.Profile;
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
 *
 * @author wangning
 */
public class ShareProfilePopupWindow extends PopupWindow {
    PackageManager mPackageManager;
    private Profile mProfile;
    private String mOtherSid;
    private String userName;
    private String userDesp;
    private String userFaceUrl;
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
    private ImageView qqIv;
    private ImageView weixinIv;
    private Button cancelBt;
    protected List<Bitmap> cacheBitmapList;
    private Bitmap qqBitmap;
    private Bitmap weixinBitmap;
    private LinearLayout renheFriendLayout;
    private LinearLayout renmaiQuanLayout;
    private int desc;//desc：1:QQ,2:微信,3:朋友圈,4:微博,5:人脉圈,6:朋友
    private ShareUtil shareUtil;

    private View view;
    private View parent;
    private LinearLayout ll_popup;

    @SuppressWarnings("deprecation")
    public ShareProfilePopupWindow(Context mContext, View parent, String userName, String userDesp, String userFaceUrl,
                                   String userSid, Profile profile, boolean isFromArchieve) {
        ct = mContext;
        cacheBitmapList = new ArrayList<Bitmap>();
        view = View.inflate(mContext, R.layout.share_archive_popupwindows, null);
        this.parent = parent;
        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        qqLayout = (LinearLayout) view.findViewById(R.id.qqLl);
        weixinLayout = (LinearLayout) view.findViewById(R.id.weixinLl);
        qqIv = (ImageView) view.findViewById(R.id.qqiv);
        weixinIv = (ImageView) view.findViewById(R.id.weixiniv);
        renheFriendLayout = (LinearLayout) view.findViewById(R.id.renmaiFriendLl);
        renmaiQuanLayout = (LinearLayout) view.findViewById(R.id.renmaiQuanLl);
        cancelBt = (Button) view.findViewById(R.id.item_popupwindows_cancel);

        setWidth(LayoutParams.FILL_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable cd = new ColorDrawable(-0000);
        setBackgroundDrawable(cd);
        //			setBackgroundDrawable(new BitmapDrawable());
        setFocusable(true);
        setOutsideTouchable(true);
        setContentView(view);
//        show();
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

        if (null != profile) {
            mProfile = profile;
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
        if (!TextUtils.isEmpty(userSid)) {
            this.userSid = userSid;
        }
        this.isFromArchieve = isFromArchieve;
        mPackageManager = ct.getPackageManager();
        qqLayout.setOnClickListener(new ShareClickListener());
        weixinLayout.setOnClickListener(new ShareClickListener());
        cancelBt.setOnClickListener(new ShareClickListener());
        renheFriendLayout.setOnClickListener(new ShareClickListener());
        renmaiQuanLayout.setOnClickListener(new ShareClickListener());
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

    public void show() {
        view.startAnimation(AnimationUtils.loadAnimation(ct, R.anim.fade_in));
        ll_popup.startAnimation(AnimationUtils.loadAnimation(ct, R.anim.share_push_bottom_in));
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        update();
    }

    class ShareClickListener implements OnClickListener {

        @Override
        public void onClick(View arg0) {
            switch (arg0.getId()) {
                case R.id.qqLl:
                    if (checkApkExist(ct, "com.tencent.mobileqq")) {
                        shareUtil = new ShareUtil(ct, mProfile, Constants.SHARE_SOURCE_TYPE.SHARE_QQ);
                        shareUtil.share2QQ();
                    } else {
                        Toast.makeText(ct, "您还未安装QQ", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    desc = 1;
                    break;
                case R.id.weixinLl:
                    if (checkApkExist(ct, "com.tencent.mm")) {
                        shareUtil = new ShareUtil(ct, mProfile, Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN);
                        shareUtil.share2Tencent(false);
                    } else {
                        Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    desc = 2;
                    break;
                case R.id.renmaiQuanLl:
                    if (null != userSid) {
                        shareUtil = new ShareUtil(ct, mProfile, Constants.SHARE_SOURCE_TYPE.SHARE_HL_CIRCLE);
                        shareUtil.shareArchive2Renmaiquan(mProfile);
                    }
                    dismiss();
                    desc = 5;
                    break;
                case R.id.renmaiFriendLl:
                    MobclickAgent.onEvent(ct, ct.getString(R.string.share_to_hlfriends));
                    if (null != userSid) {
                        shareUtil = new ShareUtil(ct, mProfile, Constants.SHARE_SOURCE_TYPE.SHARE_HL_FRIEND);
                        shareUtil.shareArchive2HlFriend(mProfile);
                    }
                    dismiss();
                    desc = 6;
                    break;
                case R.id.item_popupwindows_cancel:
                    dismiss();
                    break;
                default:
                    break;
            }
            //和聊统计
            String content = "5.132" + LoggerFileUtil.getConstantInfo(ct) + "|" + userSid + "|" + desc;
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

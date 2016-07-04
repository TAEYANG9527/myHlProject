package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.room.ForwardMessageBoardActivity;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.UserInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.auth.QQAuth;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * Created by wangning on 2015/8/22.
 */
public class ShareUtil {
    private Context ct;
    private String mQQAppid = "";
    private QQAuth mQQAuth;
    //分享圈子
    private String circleId;
    private String circleName;
    private String circleDesp;
    private String picUrl;

    private String titleString = "";
    private String contentString = "";
    private String webUrl = "";
    private String httpShortUrl;
    //分享人脉圈
    private String userName;
    private String userJob;
    private String userCompany;
    private int messageId;
    private String msgContent;

    private String bodyString;//分享到短信的文案
    //新浪微博分享
    /**
     * 微博微博分享接口实例
     */
    private IWeiboShareAPI mWeiboShareAPI = null;
    private static final int WEIBO_CONTENT_MAX_LENGTH = 140;//微博正文字数限制
    //分享圈子

    public ShareUtil(Context ct, String circleId, String circleName, String circleDesp, String circleCodeUrl,
                     String httpShortUrl) {
        this.ct = ct;
        this.circleId = circleId;
        this.circleName = circleName;
        this.circleDesp = circleDesp;
        this.picUrl = circleCodeUrl;

        this.webUrl = Constants.SHARE_CIRCLE_URL + circleId;
        this.titleString = this.circleName;//好友姓名
        this.contentString = this.circleDesp;
        this.httpShortUrl = httpShortUrl;

        bodyString = MessageFormat.format(ct.getString(R.string.share_code_to_sms_tip), circleName) + httpShortUrl;
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ct, Constants.SINA_WEIBO_APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
    }

    //分享人脉圈到微信、QQ、新浪微博等
    public ShareUtil(Context ct, String userName, int messageId, String msgContent, String picUrl) {
        this.ct = ct;
        this.userName = userName;
        this.messageId = messageId;
        this.msgContent = msgContent;

        webUrl = Constants.SHARE_RENMAIQUAN_URL + messageId;
        titleString = MessageFormat.format(ct.getString(R.string.share_renmaiquan_title_tip), userName);//好友姓名
        contentString = this.msgContent;
        this.picUrl = picUrl;

        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ct, Constants.SINA_WEIBO_APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
    }

    /**
     * 分享别人的档案
     *
     * @param ct
     * @param profile   用户档案
     * @param shareType 来自什么分享，比如QQ、微信、微博等
     */
    public ShareUtil(Context ct, Profile profile, int shareType) {
        this.ct = ct;
        this.picUrl = profile.getUserInfo().getUserface();
        this.webUrl = Constants.SHARE_ARCHIVE_URL + profile.getUserInfo().getId() + "";
        this.titleString = MessageFormat.format(ct.getString(R.string.share_archive_title_tip), profile.getUserInfo().getName());//好友姓名
        Profile.UserInfo userInfo = profile.getUserInfo();
        this.contentString = titleString + " " + userInfo.getCompany() + " " + userInfo.getTitle();
        switch (shareType) {
            case Constants.SHARE_SOURCE_TYPE.SHARE_QQ:
                this.contentString = userInfo.getCompany() + " " + userInfo.getTitle();
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN:
                this.contentString = userInfo.getCompany() + "\n" + userInfo.getTitle() + "\n点击查看更多信息";
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN_TIMELINE:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_SINNAWEIBO:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_HL_CIRCLE:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_HL_FRIEND:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_SMS:
                break;
        }

    }

    /**
     * 分享个人档案
     *
     * @param ct
     * @param shareType 来自什么分享，比如QQ、微信、微博等
     */
    public ShareUtil(Context ct, int shareType) {
        this.ct = ct;
        this.picUrl = RenheApplication.getInstance().getUserInfo().getUserface();
        this.webUrl = Constants.SHARE_ARCHIVE_URL + RenheApplication.getInstance().getUserInfo().getId() + "";
        this.titleString = MessageFormat.format(ct.getString(R.string.share_archive_title_tip),
                RenheApplication.getInstance().getUserInfo().getName());//好友姓名
        UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
        String company = "";
        if (!TextUtils.isEmpty(userInfo.getCompany())) {
            company = userInfo.getCompany();
        }
        String job = "";
        if (!TextUtils.isEmpty(userInfo.getTitle())) {
            job = userInfo.getTitle();
        }
        this.contentString = titleString + " " + company + " " + job;
        switch (shareType) {
            case Constants.SHARE_SOURCE_TYPE.SHARE_QQ:
                this.contentString = company + " " + job;
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN:
                this.contentString = company + "\n" + job + "\n点击查看更多信息";
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_WEIXIN_TIMELINE:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_SINNAWEIBO:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_HL_CIRCLE:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_HL_FRIEND:
                break;
            case Constants.SHARE_SOURCE_TYPE.SHARE_SMS:
                break;
        }
        bodyString = "我分享了" + RenheApplication.getInstance().getUserInfo().getName() + "的和聊档案给你" + webUrl + " "
                + ct.getString(R.string.share_desc_hl_tip);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ct, Constants.SINA_WEIBO_APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
    }

    //分享网页
    public ShareUtil(Context ct, String shareTitle, String shareUrl, String shareContent, String sharePic) {
        this.ct = ct;
        webUrl = shareUrl;
        titleString = shareTitle;
        contentString = shareContent;
        this.picUrl = sharePic;

        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ct, Constants.SINA_WEIBO_APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void share(String content, Uri uri, String packageName) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (uri != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            String webUrl = Constants.SHARE_CIRCLE_URL + circleId;
            String titleString = circleName;//好友姓名
            content = titleString + webUrl;
            shareIntent.putExtra("sms_body", content);
        } else {
            shareIntent.setType("text/plain");
        }
        shareIntent.setPackage(packageName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        ct.startActivity(shareIntent);
    }

    public void share2Tencent(boolean is2TimeLine) {
        if (checkApkExist(ct, "com.tencent.mm")) {
            IWXAPI api;
            /**
             * 注册到微信
             */
            api = WXAPIFactory.createWXAPI(ct, Constants.WEI_XIN_SHARE.WEI_XIN_APP_ID, true);
            api.registerApp(Constants.WEI_XIN_SHARE.WEI_XIN_APP_ID);

            if (is2TimeLine) {
                int wxSdkVersion = api.getWXAppSupportAPI();
                if (wxSdkVersion < Constants.TIMELINE_SUPPORTED_VERSION) {
                    Toast.makeText(ct, "抱歉，您的微信版本暂不支持分享到朋友圈", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            /**
             * 发送url
             */
            WXWebpageObject webpage = new WXWebpageObject();
            WXMediaMessage msg = new WXMediaMessage(webpage);
            msg.title = is2TimeLine ? contentString : titleString;
            msg.description = contentString;
            webpage.webpageUrl = webUrl;
            Bitmap thumb;//好友头像
            thumb = getUserPic();
            if (thumb != null) {
                msg.thumbData = WeixinUtil.bmpToByteArray(thumb, true);
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = is2TimeLine ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
                api.sendReq(req);
            } else {
                Toast.makeText(ct, "分享失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ct, "您还未安装微信", Toast.LENGTH_SHORT).show();
        }
    }

    public void share2QQ() {
        if (checkApkExist(ct, "com.tencent.mobileqq")) {
            mQQAppid = Constants.QQ_SHARE.QQ_APP_ID;
            mQQAuth = QQAuth.createInstance(mQQAppid, ct);
            int mExtarFlag = 0x00;
            final Bundle params = new Bundle();
            params.putString(QQShare.SHARE_TO_QQ_TITLE, titleString);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, webUrl);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, contentString);
            String path = getUserPicPath();
            if (!TextUtils.isEmpty(path))
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, path);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, ct.getString(R.string.app_name));
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, mExtarFlag);
            doShareToQQ(params);
        } else {
            Toast.makeText(ct, "您还未安装QQ", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 用异步方式启动分享
     *
     * @param params
     */
    public void doShareToQQ(final Bundle params) {

        final QQShare mQQShare = new QQShare(ct, mQQAuth.getQQToken());
        final Activity activity = (Activity) ct;
        new Thread(new Runnable() {

            @Override
            public void run() {
                mQQShare.shareToQQ(activity, params, new IUiListener() {

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onComplete(Object response) {
                    }

                    @Override
                    public void onError(UiError e) {
//                        Looper.prepare();
                        Toast.makeText(ct, "分享失败", Toast.LENGTH_SHORT).show();
//                        Looper.loop();
                    }

                });
            }
        }).start();
    }

    public void share2Weibo() {
        sendMultiMessage();
    }

    public void shareCircle2HlFriend() {
        if (null != circleId) {
            Bundle bundle = new Bundle();
            bundle.putString("toForwardObjectId", "group://" + circleId);
            bundle.putString("toForwardPic", picUrl);
            bundle.putString("toForwardContent", circleDesp);
            bundle.putString("contentTitle", circleName);
            bundle.putString("title", ct.getString(R.string.cicle_share_default_name));
            bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
            Intent intent = new Intent(ct, ToShareWithRecentContactsActivity.class);
            intent.putExtras(bundle);
            ct.startActivity(intent);
            ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    public void shareArchive2HlFriend(Profile profile) {
        Bundle bundle = new Bundle();
        if (null != profile) {
            bundle.putString("contentTitle", profile.getUserInfo().getName());
            bundle.putString("title", ct.getString(R.string.vcard_share_default_name));
            bundle.putString("toForwardObjectId", "user://" + profile.getUserInfo().getSid());
            bundle.putString("toForwardPic", profile.getUserInfo().getUserface());
            bundle.putString("toForwardContent", profile.getUserInfo().getTitle());
            bundle.putString("contentOther", profile.getUserInfo().getCompany());
        } else {
            bundle.putString("contentTitle", RenheApplication.getInstance().getUserInfo().getName());
            bundle.putString("title", ct.getString(R.string.vcard_share_default_name));
            bundle.putString("toForwardObjectId", "user://" + RenheApplication.getInstance().getUserInfo().getSid());
            bundle.putString("toForwardPic", RenheApplication.getInstance().getUserInfo().getUserface());
            bundle.putString("toForwardContent", RenheApplication.getInstance().getUserInfo().getTitle());
            bundle.putString("contentOther", RenheApplication.getInstance().getUserInfo().getCompany());
        }
        bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_SHARE);
        Intent intent = new Intent(ct, ToShareWithRecentContactsActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public void shareCircle2Renmaiquan() {
        Bundle bundle = new Bundle();
        bundle.putString("circleId", circleId);
        bundle.putString("rawContent", circleDesp);
        bundle.putString("toForwardPic", picUrl);
        bundle.putString("toForwardContent", circleDesp);
        bundle.putString("contentTitle", circleName);
        bundle.putString("title", ct.getString(R.string.cicle_share_default_name));
        bundle.putInt("shareType", Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_CIRCLE);
        Intent intent = new Intent(ct, ForwardMessageBoardActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public void shareArchive2Renmaiquan(Profile profile) {
        Bundle bundle = new Bundle();
        if (null != profile) {
            bundle.putString("contentTitle", profile.getUserInfo().getName());
            bundle.putString("title", ct.getString(R.string.vcard_share_default_name));
            bundle.putString("toForwardObjectId", "user://" + profile.getUserInfo().getSid());
            bundle.putString("toForwardPic", profile.getUserInfo().getUserface());
            bundle.putString("toForwardContent", profile.getUserInfo().getTitle());
            bundle.putString("contentOther", profile.getUserInfo().getCompany());
            bundle.putString("sid", profile.getUserInfo().getSid());
        } else {
            bundle.putString("contentTitle", RenheApplication.getInstance().getUserInfo().getName());
            bundle.putString("title", ct.getString(R.string.vcard_share_default_name));
            bundle.putString("toForwardObjectId", "user://" + RenheApplication.getInstance().getUserInfo().getSid());
            bundle.putString("toForwardPic", RenheApplication.getInstance().getUserInfo().getUserface());
            bundle.putString("toForwardContent", RenheApplication.getInstance().getUserInfo().getTitle());
            bundle.putString("contentOther", RenheApplication.getInstance().getUserInfo().getCompany());
            bundle.putString("sid", RenheApplication.getInstance().getUserInfo().getSid());
        }
        bundle.putInt("shareType", Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_PROFILE);
        Intent intent = new Intent(ct, ForwardMessageBoardActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    //通过短信分享圈子
    public void share2SMS() {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", bodyString);
        ct.startActivity(intent);
    }

    //分享人脉圈到和聊好友
    public static void shareRenMaiQuanToHlFriend(Context ct, String mObjectId, String userName, String mtoForwardPic,
                                                 String mtoForwardContent, int type) {
        Bundle bundle = new Bundle();
        bundle.putString("toForwardObjectId", "msg://" + mObjectId);
        bundle.putString("toForwardPic", mtoForwardPic);
        bundle.putString("toForwardContent", mtoForwardContent);
        bundle.putInt("type", type);
        bundle.putString("title", MessageFormat.format(ct.getString(R.string.share_renmaiquan_title_tip), userName));
        Intent intent = new Intent(ct, ToShareWithRecentContactsActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    //分享人脉圈到人脉圈
    public static void shareRenMaiQuanToRenMaiQuan(Context ct, String mObjectId, String mtoForwardPic, String mtoForwardContent,
                                                   String sender, String mrawContent) {
        Bundle bundle = new Bundle();
        bundle.putString("objectId", mObjectId);
        bundle.putString("sender", sender);
        bundle.putString("rawContent", mrawContent);
        bundle.putString("toForwardPic", mtoForwardPic);
        bundle.putString("toForwardContent", mtoForwardContent);
        Intent intent = new Intent(ct, ForwardMessageBoardActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    //分享网页到人脉圈
    public static void shareWebToRenMaiQuan(Context ct, int shareId, String sharePic, String shareContent) {
        Bundle bundle2 = new Bundle();
        bundle2.putString("toForwardPic", sharePic);
        bundle2.putString("toForwardContent", shareContent);
        bundle2.putInt("shareId", shareId);
        bundle2.putInt("shareType", Constants.ShareToRenmaiquanType.SHARETO_RENMAIQUAN_TYPE_WEB);
        Intent intent2 = new Intent(ct, ForwardMessageBoardActivity.class);
        intent2.putExtras(bundle2);
        ct.startActivity(intent2);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    //分享网页到和聊好友
    public static void shareWebToHlFriend(Context ct, String shareUrl, String sharePic, String shareContent, String shareTitle,
                                          int type) {
        Bundle bundle = new Bundle();
        bundle.putString("toForwardPic", sharePic);
        bundle.putString("title", shareTitle);
        bundle.putString("toForwardContent", shareContent);
        bundle.putString("toForwardUrl", shareUrl);
        bundle.putInt("type", type);
        Intent intent = new Intent(ct, ToShareWithRecentContactsActivity.class);
        intent.putExtras(bundle);
        ct.startActivity(intent);
        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    public Bitmap getUserPic() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        String fileName = null; //使用Universal ImageLoader后的缓存目录
        if (!TextUtils.isEmpty(picUrl)) {
            if (Constants.DEBUG_MODE) {
                Logger.w("picurl***" + picUrl);
            }
            fileName = imageLoader.getDiscCache().get(picUrl).getPath();
            if (TextUtils.isEmpty(fileName)) {//图片缓存不存在，尝试根据picurl是图片本地路径去查找
                File file = new File(picUrl);
                if (null != file && file.exists()) {
                    fileName = picUrl;
                }
            }
        }
        if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            if (null != file && file.exists()) {
                Bitmap mbitmap = BitmapFactory.decodeFile(fileName);
                if (Constants.DEBUG_MODE) {
                    Logger.w("mbitmap***" + mbitmap);
                }
                return mbitmap;
            } else {
                //放人和网logo
                Bitmap mlogo = BitmapFactory.decodeResource(ct.getResources(), R.drawable.icon_134);
                return mlogo;
            }
        } else {
            //放人和网logo
            Bitmap mlogo1 = BitmapFactory.decodeResource(ct.getResources(), R.drawable.icon_134);
            return mlogo1;
        }
    }

    public String getUserPicPath() {
        String path = "";
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!TextUtils.isEmpty(picUrl)) {
            path = imageLoader.getDiscCache().get(picUrl).getPath();
        } else {
            //放人和网logo
            path = Constants.CACHE_PATH.HL_ICON_PATH + "icon_134.png";
            File file = new File(path);
            if (null == file || !file.exists()) {
                try {
                    copyIcon(ct, "icon_134.png", path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    public String getWebPath(String webPath) {
        if (null != webPath && !"".equals(webPath)) {
            String id = webPath.substring(webPath.indexOf("/") + 2);
            id = id.replaceAll("/", "_");
            return id;
        }
        return null;
    }

    public String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351 时，支持同时分享多条消息，
     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     */
    private void sendMultiMessage() {

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        //		if (hasText) {
        weiboMessage.textObject = getTextObj();
        //		}
        //
        //		if (hasImage) {
        weiboMessage.imageObject = getImageObj();
        //		}

        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
        //		if (hasWebpage) {
        //		weiboMessage.mediaObject = getWebpageObj();
        //		}

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        AuthInfo authInfo = new AuthInfo(ct, Constants.SINA_WEIBO_APP_KEY, Constants.SINA_WEIBO_REDIRECT_URL,
                Constants.SINA_WEIBO_SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(ct);
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest((Activity) ct, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException(WeiboException arg0) {
            }

            @Override
            public void onComplete(Bundle bundle) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(ct, newToken);
                //				Toast.makeText(ct, "onAuthorizeComplete token = " + newToken.getToken(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * 创建文本消息对象。
     *
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        String weiboContent = webUrl + " " + contentString;
        if (weiboContent.length() > WEIBO_CONTENT_MAX_LENGTH) {
            weiboContent = weiboContent.substring(0, WEIBO_CONTENT_MAX_LENGTH);
        }
        textObject.text = weiboContent;
        return textObject;
    }

    /**
     * 创建图片消息对象。
     *
     * @return 图片消息对象。
     */
    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        //        设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        Bitmap bitmap = getUserPic();
        imageObject.setImageObject(bitmap);
        return imageObject;
    }

    /**
     * 创建多媒体（网页）消息对象。
     *
     * @return 多媒体（网页）消息对象。
     */
    private WebpageObject getWebpageObj() {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = contentString;
        mediaObject.description = contentString;

        Bitmap bitmap = getUserPic();
        // 设置 Bitmap 类型的图片到视频对象里         设置缩略图。 注意：最终压缩过的缩略图大小不得超过 32kb。
        mediaObject.setThumbImage(bitmap);
        mediaObject.actionUrl = webUrl;
        mediaObject.defaultText = ct.getString(R.string.app_download);
        return mediaObject;
    }

    /**
     * 把assets下面的db icon保存到本地sd卡
     *
     * @param context
     * @throws IOException
     */
    public void copyIcon(Context context, String fileName, String newPath) throws IOException {
        if (new File(newPath).exists()) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(newPath);
        InputStream is = context.getResources().getAssets().open(fileName);
        byte[] buffer = new byte[1024 * 5];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        fos.close();
        is.close();
    }
}
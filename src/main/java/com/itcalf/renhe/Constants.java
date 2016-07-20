package com.itcalf.renhe;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;

import com.itcalf.renhe.utils.FileUtil;

import java.io.File;

public class Constants {
    // TODO 开发用，发布时置为false
    public static final boolean DEBUG_MODE = true;// 应用内各种日志打印的开关，debug版本为true，release版本为false
    // TODO 开发用，发布时置为false
    public static final boolean HTTP_DEBUG_MODE = true;// 网络请求主机地址开关，debug版本为true，release版本为false

    public static final boolean TEST_MODE = DEBUG_MODE;// release版本删除该字段，检查程序中是否存在测试代码，这个字段是在应用内功能摸你测试时使用，防止添加了测试代码，之后忘记去掉

    public static final int PLATFORM_TYPE = 0;
    /**
     * APP字体
     */
    public static Typeface APP_TYPEFACE;

    /**
     * 加好友源自  0 或者不传递代表其他渠道，1代表来源于网站；2代表来源于人脉搜索；3代表来源于档案二维码扫描；
     * 4代表来源于名片扫描；5代表来源于附近的人脉；6推荐；7圈子成员；8代表来源于邮箱通讯录；9代表来源于手机通讯录;
     * 10代表来源于黑名单
     */
    public static int[] ADDFRIENDTYPE = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    /**
     * 新留言字数 **
     */
    public static final int MESSAGELENGTH = 800;
    /**
     * 评论字数 **
     */
    public static final int REPLYLENGTH = 140;
    /**
     * 圈子名称字数限制 **
     */
    public static final int CIRCLETITLE = 20;
    /**
     * 圈子公告字数限制 **
     */
    public static final int CIRCLELIMITED = 50;
    /**
     * 个人档案--技能专长字数限制**
     */
    public static final int ARCHIVESINFOLIMITED = 10;
    /**
     * 个人档案--提供得到字数限制**
     */
    public static final int PROVIDE_WANT = 10;
    /**
     * 个人档案--工作经历--职位**
     */
    public static final int WORK_TITLE = 32;
    /**
     * 个人档案--工作经历--公司名称**
     */
    public static final int WORK_COMPANY = 32;
    /**
     * 个人档案--工作经历--描述**
     */
    public static final int WORK_DESCRIPTION = 320;
    /**
     * 个人档案--职业技能**
     */
    public static final int SPECIALTY = 15;
    /**
     * 记录日志标志
     */
    public static boolean LOG = true;
    /**
     * 标志TAG
     */
    public static String TAG = "Renhe";

    public static final int PAGESIZE = 100000;// 分页返回数据，每页暂定100条
    public static final int FRIEND_PAGESIZE = 20;// 分页返回数据，每页暂定100条

    //设置字体参数
    public static void TypefaceParams(Context context) {
        APP_TYPEFACE = Typeface.createFromAsset(context.getAssets(), "fzltxh_gbk.ttf");
    }

    /**
     * 短信验证码倒计时长
     */
    public static final int COUNTDOWN_TIME = 60 * 1000;//60s
    /**
     * 日志上传间隔时间 **
     */
    public static final int LOGGER_UPLOAD_INTERVAL = 1 * 60 * 60;//1小时

    /**
     * mobile通讯录的更新时间间隔
     */
    public static final int MOBILE_CONTACTS_SYNC_TIME = 12 * 60 * 60 * 1000;

    /**
     * IM 是否是圈主 额外字段 *
     */
    public static final String ISCIRCLEMASTER = "ownerId";

    /**
     * 人和币
     */
    public static final int BIZ_TYPE_RENHE_MONEY = 1;
    /**
     * 付费会员
     */
    public static final int BIZ_TYPE_MEMBER_PAY = 2;
    /**
     * 人脉快递
     */
    public static final int BIZ_TYPE_RENMAI_EXPRESS = 3;
    /**
     * 精准推广
     */
    public static final int BIZ_TYPE_GENERALIZATION = 4;
    /**
     * 实名认证
     */
    public static final int BIZ_TYPE_REALNAMEAUTH = 5;

    public static class Http {

        public final static String getUrl() {
            if (HTTP_DEBUG_MODE) {
                return "http://4gtest.renhe.cn/";
            }
            return "http://4g.renhe.cn/";
        }

        public static final String REGISTER = getUrl() + "register.shtml";
        public static final String SHOW_PROFILE = getUrl() + "v2/viewprofile.shtml";
        public static final String PERSONAL_MESSAGEBOARDS = getUrl() + "messageboard/personalMesssageBoards.shtml";
        public static final String SHOW_MESSAGEBOARD = getUrl() + "messageboard/showMessageBoard.shtml";
        public static final String MSG_COMMENTS = getUrl() + "v9/messageboard/comments.shtml";
        public static final String UNMESSAGEBOARD_MSG_COMMENTS = getUrl() + "notice/v9/viewNoticeComments.shtml";
        public static final String FORWARD_MESSAGEBOARD = getUrl() + "v7/messageboard/forwardMessageBoard.shtml";
        public static final String FAVOUR_MESSAGEBOARD = getUrl() + "messageboard/likeMessageBoard.shtml";
        public static final String UNFAVOUR_MESSAGEBOARD = getUrl() + "messageboard/unlikeMessageBoard.shtml";
        public static final String CHECK_CIRCLECREATIONPRICILEGE = getUrl() + "circle/checkCircleCreationPrivilege.shtml";
        public static final String GENERATE_CIRCLE_AVATOR = getUrl() + "circle/generateCircleAvator.shtml";
        public static final String UPDATE_CIRCLE_AVATOR = getUrl() + "circle/updateCircleAvator.shtml";
        public static final String CHECK_UPDATE_CIRCLE = getUrl() + "circle/checkUpdateCircle.shtml";
        public static final String UPDATE_CIRCLE = getUrl() + "circle/updateCircle.shtml";
        public static final String CIRCLE_JOIN_REQUEST = getUrl() + "circle/circleJoinRequest.shtml";
        public static final String MY_CRICLE_JOIN_REQUSET = getUrl() + "circle/myCircleJoinRequst.shtml";
        public static final String DISSOLVE_CIRCLE = getUrl() + "circle/dissolveCircle.shtml";
        public static final String INVITE_CIRCLE = getUrl() + "circle/inviteCircle.shtml";
        public static final String INVITATION_JOIN_CIRCLE = getUrl() + "circle/invitationJoinCircle.shtml";
        public static final String DELETE_CIRCLE = getUrl() + "circle/deleteCircleMember.shtml";
        public static final String APPROVE_CIRCLE_JONIN_REQUEST = getUrl() + "circle/approveCircleJoinRequest.shtml";
        public static final String LOAD_CIRCLE_INFO = getUrl() + "circle/loadCircleInfo.shtml";
        public static final String CREATE_CIRCLE = getUrl() + "circle/createCircle.shtml";
        public static final String SEARCH_CIRCLE = getUrl() + "circle/searchCircle.shtml";
        public static final String JOIN_CIRCLE = getUrl() + "circle/joinCircle.shtml";
        public static final String DIRECT_JOIN_CIRCLE = getUrl() + "circle/directJoinCircle.shtml";
        public static final String CIRCLE_JOIN_COUNT = getUrl() + "circle/circleRequestUnReadCount.shtml";
        public static final String FAVOUR_UNMESSAGEBOARD = getUrl() + "notice/likeRenmaiquan.shtml";
        public static final String REPLY_MESSAGEBOARD = getUrl() + "v7/messageboard/replyMessageBoard.shtml";
        public static final String REPLY_UNMESSAGEBOARD = getUrl() + "notice/v6/commentRenmaiquan.shtml"; // 评论/回复
        // 非MessageBoard类型
        public static final String SEARCH_RELATIONSHIP = getUrl() + "member/search.shtml";

        public static final String SEARCH_GLOBAL_RELATIONSHIP = getUrl() + "member/v2/renmai.shtml";//全局搜索人脉
        public static final String SEARCH_RELATIONSHIP_SENIOR = getUrl() + "member/v2/renmaiAccu.shtml";//人脉高级搜索
        public static final String SEARCH_RELATIONSHIPANDCIRCLE_V2 = getUrl() + "member/v2/renmaiAndCircle.shtml";//人脉和人脉圈搜索
        public static final String SEARCH_ADVANCED_PRIVILEGE = getUrl() + "member/v2/advancedSearchPrivilege.shtml";

        public static final String SEARCH_FOLLOWERS = getUrl() + "messageboard/followers.shtml";
        public static final String SEARCH_FOLLOWERINGS = getUrl() + "messageboard/followings.shtml";
        public static final String NEARBY_PEOPLE = getUrl() + "member/near.shtml";
        public static final String CLEAN_LOCATION = getUrl() + "member/cleanLocation.shtml";
        public static final String UPDATE_LOCATION = getUrl() + "member/updateLocation.shtml";

        public static final String GET_CONFIG = getUrl() + "config.shtml";
        //黑名单
        public static final String BLACKLIST = getUrl() + "member/listBlockedMember.shtml";//黑名单列表
        public static final String ADDBLACKLIST = getUrl() + "member/addBlockedMember.shtml";//加入黑名单
        public static final String REMOVEBLACKLIST = getUrl() + "member/removeBlockedMember.shtml";//移除黑名单
        // 接口升级，支持分页
        public static final String CONTACTLIST = getUrl() + "member/contactList.shtml";
        public static final String CONTACTLISTBYPAGE = getUrl() + "member/contactListBySkipAndLimit.shtml";//查看别人的
        public static final String MYCONTACTLISTBYPAGE = getUrl() + "member/v3/contactListBySkipAndLimit.shtml";//查看自己的
        public static final String CONTACTLISTBYNEW = getUrl() + "member/v2/contactListByMinConnectionId.shtml";
        public static final String MYCONTACTLISTBYNEW = getUrl() + "member/v3/contactListByMinConnectionId.shtml";

        public static final String INNERMSG_SEND = getUrl() + "message/sendMessage.shtml";
        public static final String INNERMSG_CHECKUNREADMESSAGE = getUrl() + "message/checkUnReadMessageCount.shtml";
        public static final String MORE_FEEDBACK = getUrl() + "feedback/insertFeedback.shtml";
        public static final String MEMBER_UPLOADIMG = getUrl() + "member/uploadUserFaceImage.shtml";
        public static final String CHECK_VERION_UPDATE = getUrl() + "newVersion.shtml";
        public static final String HELIAO_FEEDBACK = getUrl() + "feedback/insertHeliaoFeedback.shtml";

        public static final String ADDFRIEND_V3 = getUrl() + "contact/v3/addFriend.shtml";// 添加好友接口2015.6.30

        public static final String GETNEWFRIENDS_COUNT = getUrl() + "contact/newFriendCount.shtml";// 获取新的朋友数量接口
        //        public static final String GETNEWFRIENDS_LIST = getUrl() + "contact/newFriendList.shtml";// 获取新的朋友列表接口
        public static final String GETNEWFRIENDS_LIST = getUrl() + "v4/contact/getNewFriendList.shtml";// 获取新的朋友列表接口
        public static final String GETINTEREST_LIST = getUrl() + "contact/interest.shtml";// 获取猜你感兴趣列表接口

        public static final String GETHOTSEARCH_RECOMMENDED_LIST = getUrl() + "member/hotSearchRecommendMemberList.shtml";// 获取最火的搜索关键字和推荐的人脉列表接口

        public static final String RECEIVEADDFRIEND = getUrl() + "contact/receiveAddFriend.shtml";
        public static final String NOTICE_UNREADMSG = getUrl() + "notice/noticeNotifyList.shtml";
        public static final String UNREADMSGNUM = getUrl() + "messageboard/messageBoardNotifyCount.shtml";
        //注册新流程 modify by 2015.4.9
        public static final String SENDREGISTERVERIFICATION = getUrl() + "v6/register/sendRegisterMobileVerificationCode.shtml";
        public static final String REGISTERMOBILE_V6 = getUrl() + "v6/register/register.shtml";
        public static final String REGISTER_MOBILE_V7 = getUrl() + "v7/register/doRegisterMobile.shtml";
        public static final String REGISTER_MOBILE_COMPLETE_V7 = getUrl() + "v7/register/doRegisterMobileEven.shtml";
        // 新注册流程
        public static final String REGISTERMOBILE = getUrl() + "registerMobile.shtml";
        public static final String VERIFICATIONREGISTER_V7 = getUrl()
                + "v7/register/doVerificationRegisterMobileVerificationCodeAndRegisterPassWord.shtml";//注册验证 V5.3.1版本8.27
        // 新登录
        public static final String NEWLOGIN = getUrl() + "userLogin.shtml";
        // 老用户绑定手机号
        public static final String SENDMOBILEVALIDATIONCODE = getUrl() + "member/sendMobileValidationCode.shtml";
        public static final String BINDMOBILE = getUrl() + "member/bindMobile.shtml";
        public static final String MODIFYMOBILE = getUrl() + "member/modifyMobile.shtml";//修改手机号

        // 档案编辑
        public static final String EDITPROFESSION = getUrl() + "editprofile/editProfession.shtml";
        public static final String EDITSPECIALTIES = getUrl() + "editprofile/editSpecialties.shtml";

        public static final String EDITPROVIDE = getUrl() + "editprofile/editMemberPreferredTags.shtml";
        public static final String EDITGET = getUrl() + "editprofile/editMemberAimTags.shtml";
        // 合并上面两个接口，保存会员的我想获得，我能提供数据
        public static final String EDIT_PROVIDE_GET = getUrl() + "editprofile/editMemberPreferredAimTags.shtml";

        public static final String ADD_WORK_INFO = getUrl() + "editprofile/addBaseMemberExperience.shtml";
        public static final String EDIT_WORK_INFO = getUrl() + "editprofile/editBaseMemberExperience.shtml";
        public static final String DELETE_WORK_INFO = getUrl() + "editprofile/deleteMemberExperience.shtml";
        public static final String EDIT_SIMPLE_WORK_INFO = getUrl() + "editprofile/editCompanyAndTitle.shtml";
        public static final String ADD_EDU_INFO = getUrl() + "editprofile/addMemberEducation.shtml";
        public static final String EDIT_EDU_INFO = getUrl() + "editprofile/editMemberEducation.shtml";
        public static final String DELETE_EDU_INFO = getUrl() + "editprofile/deleteMemberEducation.shtml";
        public static final String EDIT_SELF_INFO = getUrl() + "editprofile/editBasicInfo.shtml";
        public static final String EDIT_ORGANSITION_INFO = getUrl() + "editprofile/editAssociations.shtml";
        public static final String EDIT_INTEREST_INFO = getUrl() + "editprofile/editInterests.shtml";
        public static final String EDIT_AWARD_INFO = getUrl() + "editprofile/editAwards.shtml";
        public static final String EDIT_ALL_OTHER_INFO = getUrl() + "editprofile/editOtherInfo.shtml";
        public static final String EDIT_CONTACT_INFO = getUrl() + "editprofile/editContactInfo.shtml";
        public static final String EDIT_WEBSITE_INFO = getUrl() + "editprofile/editWebsiteInfo.shtml";
        public static final String BLOCK_MESSAGEBOARD_MEMBER = getUrl() + "messageboard/addBlockMessageboardMember.shtml";

        public static final String GET_REGISTER_RECOMMEND = getUrl() + "recommendation/registerMemberRecommend.shtml";
        public static final String EDIT_MEMBER_PREFERRED_AIM_TAGS = getUrl() + "editprofile/editMemberPreferredAimTags.shtml";

        public static final String ADD_RECOMMEND_USERS = getUrl() + "contact/batchAddFriend.shtml";

        public static final String GET_FAXIAN = getUrl() + "notice/renmaiquanFound.shtml";
        public static final String PREPUBLISH_MESSAGEBOARD = getUrl() + "v6/messageboard/prePublishMessageBoard.shtml";
        public static final String UPLOAD_PHOTO = getUrl() + "v2/messageboard/uploadMessageboardPhoto.shtml";
        public static final String VIEW_FULLBOARD = getUrl() + "v10/messageboard/viewFullMessageBoard.shtml";
        public static final String VIEW_FULLUNBOARD = getUrl() + "notice/v9/viewNotice.shtml";
        public static final String PERSONAL_RENMAIQUAN = getUrl() + "v10/messageboard/list.shtml";
        public static final String CHECK_UNREAD_MSG = getUrl() + "notice/renmaiquanHasNewAfterTime.shtml";
        public static final String ADD_RECOMMEND_INDUSTRYS = getUrl() + "member/selectMemberCategory.shtml";
        public static final String DELETE_RENMAIQUAN_MSG = getUrl() + "messageboard/deleteMessageBoard.shtml";
        public static final String DELETE_UNMESSAGEBOARD_RENMAIQUAN_MSG = getUrl() + "notice/deleteNoticeComments.shtml";
        public static final String PERSONAL_AUTH = getUrl() + "editprofile/getPrivacySettingInfo.shtml";
        public static final String EDIT_PERSONAL_AUTH = getUrl() + "editprofile/editPrivacySettingInfo.shtml";
        public static final String GET_BOOK_FRIEND_LIST = getUrl() + "member/getMemberRssList.shtml";
        public static final String BOOK_FRIEND = getUrl() + "member/addMemberRss.shtml";
        public static final String UPDATE_BOOK_FRIEND = getUrl() + "member/updateMemberRss.shtml";
        public static final String DELETE_BOOK_FRIEND = getUrl() + "member/deleteMemberRss.shtml";
        public static final String VIEW_CONTACT_PROFILE = getUrl() + "viewContactProfile.shtml";
        public static final String UPLOAD_COVER = getUrl() + "editprofile/uploadProfileCoverImage.shtml";
        public static final String HOT_SEARCH_LIST = getUrl() + "member/hotSearchList.shtml";
        public static final String MEMBER_COVER_LIST = getUrl() + "editprofile/profileCoverDefaultList.shtml";
        public static final String UPLOAD_MEMBER_COVER_LIST = getUrl() + "editprofile/uploadProfileCoverImageWithDefault.shtml";
        public static final String GET_RENMAIQUAN_V3 = getUrl() + "notice/v12/renmaiquan.shtml";

        public static final String GET_TOUTIAO_LIST = getUrl() + "toutiao/showToutiao.shtml";
        public static final String LOAD_CONVERSATION_INFO = getUrl() + "conversation/loadConversationInfo.shtml";
        public static final String LIST_SYSTEM_MESSAGE = getUrl() + "conversation/v3/listSystemMessage.shtml";
        public static final String GET_IM_INNERMSG_LIST = getUrl() + "conversation/listMessageConversation.shtml";
        public static final String GET_IM_INNERMSG_CHAT_LIST = getUrl() + "conversation/listMessage.shtml";

        // 小红点提醒(包含人脉圈有新动态、找人脉中有新匹配的人脉、有新的未读提醒)
        public static final String GET_TAB_REDDOT = getUrl() + "hasNew.shtml";
        public static final String REGISTER_IM_MEMBER = getUrl() + "im/registerImMember.shtml";
        public static final String VIEW_PROFILE_WITH_OPENID = getUrl() + "v2/viewProfileWithOpenid.shtml";
        public static final String VIEW_CONTACT_PROFILE_WITH_OPENID = getUrl() + "viewContactProfileWithOpenid.shtml";
        public static final String ADD_NEWDEVICE_TOKEN = getUrl() + "member/addNewDeviceToken.shtml";
        public static final String ADD_MEMBER_DEVICE_TOKEN = getUrl() + "member/addMemberDeviceToken.shtml";
        public static final String LOG_OUT = getUrl() + "loginout.shtml";

        public static final String RENMAIQUAN_HASNEW = getUrl() + "notice/v6/renmaiquanHasNew.shtml";

        /**
         * 导入手机，邮箱通讯录联系人**
         */
        public static final String IMPORT_MOBILE_CONTACTS = getUrl() + "import/v2/importContactMobile.shtml";
        public static final String GET_IMPORT_CONTACTS = getUrl() + "import/getImportContact.shtml";
        public static final String GET_EMAIL_CONTACTS = getUrl() + "contact/v1/getEmailContact.shtml";
        //获取网页图片和title
        public static final String GET_WEBVIEW_CONTENT = getUrl() + "shareto/getHtmlInfo.shtml";
        public static final String SHARE_WEBVIEW_CONTENT_TO_RENMAIQUAN = getUrl() + "shareto/v2/renMaiQuan.shtml";
        public static final String GET_CIRCLE_QRCODE = getUrl() + "qrcode/circle.shtml";
        public static final String GET_PROFILE_QRCODE = getUrl() + "qrcode/profile.shtml";
        public static final String DELETE_FRIEND = getUrl() + "contact/deleteFriend.shtml";
        public static final String DELETE_MOBILE_FRIEND = getUrl() + "member/deleteMobileContact.shtml";

        //获取所有名片
        public static final String GET_OWN_VCARDS = getUrl() + "card/initCard.shtml";
        //上传名片
        public static final String UPLOAD_VCARDS = getUrl() + "card/uploadCard.shtml";
        //上传名片
        public static final String DELETE_VCARDS = getUrl() + "card/deleteCard.shtml";
        //查询名片（可批量查询）
        public static final String INQUIRY_VCARDS = getUrl() + "card/listCard.shtml";
        /**
         * 记录搜索内容点击记录*
         */
        public static final String RECORD_MEMBERCLICK = getUrl() + "member/writeMemberClickRecord.shtml";
        //日志文件上传接口
        public static final String UPLOAD_APPLOG = getUrl() + "applog/upload.shtml";
        public static final String SHARE_CIRCLE_TO_RENMAIQUAN = getUrl() + "share/circle.shtml";
        public static final String SHARE_PROFILE_TO_RENMAIQUAN = getUrl() + "share/profile.shtml";
        //获取圈子详情接口
        public static final String LOAD_CIRCLE_INFO_BY_CIRCLE_ID = getUrl() + "circle/loadCircleInfoByCircleId.shtml";

        //支付接口
        public static final String WEIXIN_REPAY = getUrl() + "v2/pay/weixin/unifiedOrder.shtml";
        public static final String WEIXIN_ORDER_SEARCH = getUrl() + "pay/weixin/queryOrder.shtml";
        public static final String ZHIFUBAO_REPAY = getUrl() + "pay/alipay/unifiedOrder.shtml";
        //用户升级信息
        public static final String ACCOUNT_VIP_INFO = getUrl() + "vip/accountVipInfo.shtml";
        /**
         * 获取账户限额信息*
         */
        public static final String ACCOUNTLIMIT_INFO = getUrl() + "v2/accountlimit/getAccountLimitAmount.shtml";
        public static final String ACCOUNTLIMIT_INCREASEADDFRIEND = getUrl() + "v2/accountlimit/increaseAddFriendLimit.shtml";
        public static final String ACCOUNTLIMIT_INCREASEFRIENDAMOUNT = getUrl()
                + "v2/accountlimit/increaseFriendAmountLimit.shtml";
        public static final String ACCOUNTLIMIT_INCREASERENMAISEARCHLIST = getUrl()
                + "v2/accountlimit/increaseRenMaiSearchListLimit.shtml";
        public static final String ACCOUNTLIMIT_INCREASEADVANCEDSEARCH = getUrl()
                + "v2/accountlimit/increaseAdvancedSearchLimit.shtml";
        public static final String ACCOUNTLIMIT_INCREASEMEMBERNEARBYFILTER = getUrl()
                + "v2/accountlimit/increaseMemberNearbyFilterLimit.shtml";

        public static final String ACCOUNT_UPGRADE = getUrl() + "v2/vip/accountUpgrade.shtml";
        //实名认证接口
        public static final String REALNAME_AUTH = getUrl() + "member/realNameAuth.shtml";
        //免费实名认证接口
        public static final String REALNAME_AUTH_NOW = getUrl() + "member/realNameAuthNow.shtml";
        //获取实名认证状态接口
        public static final String REALNAME_AUTH_STATUS = getUrl() + "member/getRealNameAuthStatus.shtml";
        //获取实名认证状态接口
        public static final String REALNAME_AUTH_FEE = getUrl() + "member/getRealNameAuthFee.shtml";
        //邀请码
        public static final String INVITATIONCODE_MYSELF = getUrl() + "member/getMyMemberCoupon.shtml";
        public static final String INVITATIONCODE_CHECK = getUrl() + "member/checkCoupon.shtml";
        public static final String INVITATIONCODE_USE = getUrl() + "member/useCoupon.shtml";

        //获取常用联系人
        public static final String GET_COMMON_CONTACT_LIST = getUrl() + "member/commonContactList.shtml";
        //获取手机联系人IM聊天信息
        public static final String SEND_IM_MOBILE_CHAT = getUrl() + "chat/chatFriend.shtml";
        public static final String GET_IM_MOBILE_CHAT_LIST = getUrl() + "chat/chatList.shtml";
        //手机联系人邀请好友
        public static final String INVITE_MOBILE_CONTACTS = getUrl() + "invite/inviteFriend.shtml";
        //手机联系人一键邀请
        public static final String A_KEY_INVITE_MOBILE_CONTACTS = getUrl() + "invite/inviteMobileMoreFriend.shtml";
        //email联系人 一键邀请
        public static final String A_KEY_INVITE_EMAIL_CONTACTS = getUrl() + "invite/inviteEmailMoreFriend.shtml";

        //找回密码
        public static final String FIND_PASSWORD = "http://www.renhe.cn/forgot.html?from=heliaoAll";
        //是否显示和财富接口
        public static final String IS_SHOW_HCF_URL = getUrl() + "auxiliary/checkIsShowUrl.shtml";
        //获取优惠券列表
        public static final String GET_COUPONS_LIST = getUrl() + "coupon/list.shtml";
        //查看名片和手机号码
        public static final String GET_CARD_MOBILE_INFO = getUrl() + "v1/contact/viewCardAndMobile.shtml";
        //新的好友--名片和手机通讯录添加好友
        public static final String INVITE_CARD_MOBILE_FRIEND = getUrl() + "v1/invite/inviteNewFriend.shtml";
        //删除新的好友--名片和手机通讯录好友
        public static final String DELETE_CARD_MOBILE_FRIEND = getUrl() + "v4/contact/deleteFriend.shtml";
        //上传名片uuid 和删除名片
        public static final String UPLOADORDELETE_OCR_CARD = getUrl() + "v1/card/uploadOrdelete.shtml";
        //脉可寻名片上传
        public static final String UPLOAD_OCR_CARD = getUrl() + "v1/card/uploadCard.shtml";
        //人脉圈，人脉，完善资料小红点显示
        public static final String HAS_NEW_NOTIFY = getUrl() + "notice/v10/hasNewNotify.shtml";
        //查看过完善资料
        public static final String LOOK_PERFECINFO = getUrl() + "v1/lookPerfectInfo.shtml";
        //获取名片
        public static final String GET_CARDLIST = getUrl() + "v1/card/getCardList.shtml";
        public static final String SEND_JOIN_CIRCLE_INFO = getUrl() + "circle/sendJoinCircleInfo.shtml";
        //红包广告上传图片
        public static final String AD_UPLOAD_ADCOVER = getUrl() + "ad/uploadAdCover.shtml";
        //余额支付
        public static final String PAY_BY_BALANCE = getUrl() + "pay/balance.shtml";
        //第三方应用分享网页到和聊人脉圈
        public static final String OTHER_APP_SHARETO_RMQ = getUrl() + "share/source.shtml";
    }

    public static class Prefs {
        public static final String EXECUTE_UPLOAD_CARD_SERVICE = "EXECUTE_UPLOAD_CARD_SERVICE";
    }

    public static String DATA_LOGOUT = "data_logout";

    public static class VCardStatus {
        public static final int RECOGNIZING = 0;
        public static final int RECOGNIZED = 1;
        public static final int UPLOADING = -1;
        public static final int UPLOADFAIL = -2;
    }

    //以下名片相关可删除
    public static class IntentKey {
        public static final String CARD_FILE_PATH = "card_file_path";
    }

    public static class VCard {

        public static final String EXTENDEDNAME_ORG = "X-ORG";
        public static final String EXTENDEDNAME_QQ = "X-QQ";
        public static final String EXTENDEDNAME_WEIXIN = "X-WEIXIN";
        public static final String EXTENDEDNAME_MSN = "X-MSN";
        public static final String EXTENDEDNAME_TWITTER = "X-TWITTER";
        public static final String EXTENDEDNAME_FACEBOOK = "X-FACEBOOK";
        public static final String EXTENDEDNAME_SINA_WEIBO = "X-SINA-WEIBO";
        public static final String EXTENDEDNAME_TENCENT_WEIBO = "X-QQ-WEIBO";
        public static final String EXTENDEDNAME_BLOG_WEBSITE = "X-BLOG-WEBSITE";
        public static final String EXTENDEDNAME_PERSONAL_WEBSITE = "X-PERSONAL-WEBSITE";
        public static final String EXTENDEDNAME_COMPANY_WEBSITE = "X-COMPANY-WEBSITE";

    }

    public static class RecognitionStage {
        public static final int STAGE_RECOGNIZED = 1;
    }

    public static class CardStatus {

        public static final int STATUS_NEW = 0;
        public static final int STATUS_UPLOADED = 2;
        public static final int STATUS_RECOGNIZED = 3;
    }

    public static class RecognitionResult {

        public static final int RESULT_RECOGNIZED_OK = 2;
    }

    /**
     * IM 参数
     */
    public static class IMParams {
        public static final String APP_KEY = "81084189F8F99A5A93F08066A72A8345";
        public static final String APP_SECRET = "59B0800E17A2592829D4A8443333F027";
        public static final String ORG = "renhe";
        public static final String DOMAIN = "renheApp";
    }

    public static final String CURRENT_IS_NOT_IN_CHAT = "current_is_not_in_chat";

    /**
     * 设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
     */
    public static final String JPUSH_APP_BUNDLE = "renhe_android";

    public static class RenMaiQuanPOPWindowParams {
        public static final int WIDTH_MIN = 110;//DP 165
        public static final int WIDTH_MAX = 165;//DP 247
        public static final int DETAIL_WIDTH_MAX = 247;//DP 247
        public static final int HEIGHT_MAX = 40;//DP
    }

    /**
     * 广播action
     *
     * @author Renhe
     */
    public static class BrocastAction {
        public static final String REST_CIRCLE_MAX_MIN_RANK_ACTION = "rest_circle_max_min_rank_action";
    }

    public static int FACE_START_INDEX = 48;

    /**
     * 和聊对话界面
     */
    public static class ConversationStaticItem {
        public static final String CONVERSATION_ITEM_HELPER = "和聊助手";
        public static final String CONVERSATION_ITEM_TOUTIAO = "行业头条";
    }

    /**
     * Url格式正则表达式
     */
    public static final String PATTERN_URL = "((http[s]{0,1}|ftp)://|www)[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?";
    public static final String TELPHONE_REGULAR_EXPRESSION = "^(13[0-9]|15[0-9]|18[0-9]|170|176|177|178|147|)\\d{8}$";

    /**
     * 和聊对话界面复制、转发type
     */
    public static class ChatShareType {
        public static final int CHAT_SHARE_TEXT = 1;
        public static final int CHAT_SHARE_IMAGE = 2;
        public static final int CHAT_SHARE_LINK = 3;
        public static final int CHAT_SHARE_FILE = 4;
    }

    /**
     * 将内容分享到对话的type
     */
    public static class ConversationShareType {
        public static final int CONVERSATION_SEND_FROM_SHARE = 1;
        public static final int CONVERSATION_SEND_FROM_WEBVIEW_SHARE = 2;
        public static final int CONVERSATION_SEND_FROM_TEXT_FORWARD = 3;
        public static final int CONVERSATION_SEND_FROM_IMAGE_FORWARD = 4;
        public static final int CONVERSATION_SEND_FROM_LINK_FORWARD = 5;
        public static final int CONVERSATION_SEND_FROM_FILE_FORWARD = 6;
        public static final int CONVERSATION_SEND_FROM_IMAGE_SHARE = 7;
    }

    /**
     * 将内容分享到人脉圈
     */
    public static class ShareToRenmaiquanType {
        public static final int SHARETO_RENMAIQUAN_TYPE_NORMAL = 1;
        public static final int SHARETO_RENMAIQUAN_TYPE_WEB = 2;
        public static final int SHARETO_RENMAIQUAN_TYPE_CIRCLE = 3;
        public static final int SHARETO_RENMAIQUAN_TYPE_PROFILE = 4;
    }

    /**
     * 人脉圈分享内容类型
     */
    public static class RenmaiquanShareType {
        public static final int RENMAIQUAN_TYPE_WEB = 100;
        public static final int RENMAIQUAN_TYPE_PROFILE = 101;
        public static final int RENMAIQUAN_TYPE_CIRCLE = 102;
        public static final int RENMAIQUAN_TYPE_COMMUNAL = 103;//赞服务分享
    }

    /**
     * 外部浏览器点击url跳转到和聊app的url
     */
    public static class BrowserToHeliaoUrl {
        public static final String RENHE_CIRCLE = "m.renhe.cn/heliao/circle";
        public static final String RENHE_PROFILE = "m.renhe.cn/heliao/profile";
        public static final String RENHE_RENMAIQUAN = "m.renhe.cn/heliao/notice";
    }

    /**
     * 外部浏览器点击url跳转到和聊app的url
     */
    public static class HlUseHelpUrl {
        public static final String HL_USE_HELP_URL = "http://m.renhe.cn/service/catalog.htm";
    }

    /**
     * 广播action
     */
    public static class BroadCastAction {
        public static final String REFRESH_ARCHIEVE_RECEIVER_ACTION = "com.renhe.refresh_archieve";
        public static final String UPLOAD_AVARTAR_ARCHIEVE_ACTION = "com.renhe.upload_image_archieve";
        public static final String UPLOAD_COVER_ARCHIEVE_ACTION = "com.renhe.upload_cover_archieve";
        public final static String UPDATE_MAXCREATETIME_ACTION = "update_maxcreatetime_action";
        public final static String NEWFRIENDS_COUNT = "newfriends_num";
        public static final String REFRESH_CONTACT_RECEIVER_ACTION = "com.renhe.refresh_contact";
        public final static String ICON_ACTION = "notice_icon_num";
        public final static String NEWMSG_ICON_ACTION = "newmsg_notice_icon_num";
        public final static String ADD_BLACK_LIST = "add_black_list";
        public final static String REMOVE_BLACK_LIST = "remove_black_list";
        public final static String UPDATE_ACCOUNTLIMIT_ACTION = "update_accountlimit_action";
        public static final String ACTION_NAMEAUTHSTATUS = "com.renhe.nameauthstatus";
        public static final String ACTION_NAMEAUTHRES = "com.renhe.nameauthres";
        public static final String BLOCKED_CONTACTS = "com.renhe.blocked_contacts";
        public static final String DELETE_CONVERSATION_BY_OPENID_CONTACTS = "delete_conversation_by_openid_contacts";
        public static final String IM_CHAT_REFRESH_ACTION = "im_chat_refresh_action";
        public static final String UPLOAD_MOBILE_CONTACTS_ACTION = "upload_mobile_contacts_action";
        public static final String UPLOAD_MOBILE_CONTACTS_FAILED_ACTION = "upload_mobile_contacts_failed_action";
        public static final String XING_NOTIFY_BROADCAST_ACTION_FOR_NEWFRIEND = "xing_notify_broadcast_action_for_newfriend";//通知(新的好友)点击广播action
        public static final String IM_CHAT_FORWARD_ACTION = "im_chat_forward_action";//IM转发成功之后的广播
        //人脉圈重绘
        public static final String REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION = "refresh_recyclerview_item_receiver_action";
        public static final String RMQ_ACTION_RMQ_DELETE_ITEM = "rmq_action_rmq_delete_item";//人脉圈删除某条留言的广播
        public static final String RMQ_ACTION_RMQ_BOLOCK_ITEMS = "rmq_action_rmq_bolock_items";//人脉圈屏蔽某人的所有留言的广播
        public static final String RMQ_ACTION_RMQ_ADD_UNREAD_NOTICE = "rmq_action_rmq_add_unread_notice";//人脉圈新增未读新消息的广播
        public static final String RMQ_ACTION_RMQ_DELETE_UNREAD_NOTICE = "rmq_action_rmq_delete_unread_notice";//人脉圈删除未读新消息的广播
        public static final String RMQ_ACTION_RMQ_UPLOAD_MSG_NOTICE = "rmq_action_rmq_upload_msg_notice";//人脉圈删对未上传成功的人脉圈进行重试
        public static final String RMQ_ACTION_RMQ_ADD_FRIEND_NOTICE = "rmq_action_rmq_add_friend_notice";//人脉圈删在详情页添加好友成功后，通知列表页更新UI
        //收到新的好友推送，发送广播，通知主页面调用获取新的好友数量的接口
        public static final String NEW_FRIEND_ACTION = "new_friend_action";
        //对话列表
        public static final String LOAD_CONVERSATION_INFO_ACTION = "load_conversation_info_action";

        //我的页面
        public final static String UPDATE_AVATAR_ACTION = "update_avatar_image";
    }

    /**
     * 账户限额--升级：
     * 0:每日发送好友邀请
     * 1:人脉上限
     * 2:人脉搜索列表上限
     * 3:高级搜索
     * 4:附近人脉行业过滤
     */
    public static int[] ACCOUNTLIMITUPGRADE = {0, 1, 2, 3, 4};

    /**
     * 账户限额-人脉上限>此值，显示为无限*
     */
    public static int FRIENDAMOUNTLIMIT = 10000;

    /**
     * 存在shareprefrence里的黑名单列表，用于屏蔽IM好友的消息
     */
    public static final String BLOCKED_CONTACTS_SHAREDPREFERENCES = "blockedContactsSp";

    public static class WEI_XIN_SHARE {
        public static final String WEI_XIN_APP_ID = "wx490886869df61d81";
        public static final String WEI_XIN_APP_SECRET = "6acc65d1f11294c5ca084dc776ab3588";
    }

    public static class QQ_SHARE {
        public static final String QQ_APP_ID = "100830477";
    }

    public static class MI_PUSH {
        public static final String MI_PUSH_APP_ID = "2882303761517243172";
        public static final String MI_PUSH_APP_KEY = "5861724343172";
        public static final String MI_PUSH_APP_SECRET = "urRRJQbj7pgSR/+AbcXhow==";
    }

    public static class HUAWEI_PUSH {
        public static final String HUAWEI_PUSH_APP_ID = "1061207";
        public static final String HUAWEI_PUSH_APP_SECRET = "5vbi5syvveyp132o7mpv5wtrq1fd4bg9";
    }

    public static class SHARE_SOURCE_TYPE {
        public static final int SHARE_QQ = 1;
        public static final int SHARE_WEIXIN = 2;
        public static final int SHARE_WEIXIN_TIMELINE = 3;
        public static final int SHARE_SINNAWEIBO = 4;
        public static final int SHARE_HL_CIRCLE = 5;
        public static final int SHARE_HL_FRIEND = 6;
        public static final int SHARE_SMS = 7;//短信
    }

    /**
     * 分享圈子的默认url
     */
    public static final String SHARE_CIRCLE_URL = "http://m.renhe.cn/heliao/circle.shtml?id=";
    /**
     * 分享人脉圈的默认url
     */
    public static final String SHARE_RENMAIQUAN_URL = "http://www.renhe.cn/messageboard/";
    /**
     * 分享人脉档案的默认url
     */
    public static final String SHARE_ARCHIVE_URL = "http://r.renhe.cn/";
    /**
     * qq分享的最低版本限制
     */
    public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    /*文件下载的状态*/
    public static class FILE_DOWNLOAD_STATE {
        public static final int DOWNLOAD_STATE_WAITTIGN = -1;//等待下载
        public static final int DOWNLOAD_STATE_DOWNLOADING = 1;//正在下载
        public static final int DOWNLOAD_STATE_SUCCESS = 2;//下载完成
    }

    /*IM发送/接收文件的类型，比如doc、txt*/
    public static class FILE_TYPE {
        public static final String FILE_TYPE_TXT = "txt";//TXT文件
        public static final String FILE_TYPE_DOC = "doc";//doc文件
        public static final String FILE_TYPE_EXCEL = "xls";//excel文件
        public static final String FILE_TYPE_PPT = "ppt";//PPT文件
        public static final String FILE_TYPE_PDF = "pdf";//PDF文件
    }

    /*外部APP可调用分享信息入口，比如IM，人脉圈等*/
    public static class APP_SHARE_TO {
        public static final int APP_SHARE_TO_IM = 1;//分享到IM
        public static final int APP_SHARE_TO_RENMAIQUAN = 2;//分享到人脉圈
    }

    /*IM发送文件的最大尺寸：10MB*/
    public static final int IM_FILE_LIMIT_SIZE = 10;//MB

    /*Tipbox类型*/
    public static class TIP_BOX_FROM_TYPE {
        public static final int TIP_BOX_FROM_IM = 5;//对话界面，点击加号，弹出相册最新添加的图片
    }

    public static final String SELECT_CONTACT_TYPE_KEY = "selectType";//选择联系人 类型
    /*选择联系人 类型*/

    public static class SELECT_CONTACT_TYPE {
        public static final int SELECT_CONTACT_FOR_ADD_CIRCLE_MEMBER = 0;//默认，圈子添加好友
        public static final int SELECT_CONTACT_FOR_AT = 1;//选择@要提醒的人
    }

    /**
     * 信鸽notification通知点击派发逻辑
     */
    public static final String XING_NOTIFY_ACTION = "xing_notify_action";//通知来源
    public static final String XING_NOTIFY_TYPE = "xing_notify_type";//通知来源
    public static final String XING_NOTIFY_BROADCAST_ACTION = "xing_notify_broadcast_action";//通知点击广播action

    /*信鸽notification类型*/
    public static class XINGE_NOTIFY_TYPE {
        public static final int INNERMSG_PUSH = 1; // 站内信推送
        public static final int MESSAGENUM_PUSH = 2; // 人脉圈动态提醒，比如xx赞了你的留言、xx评论了你等
        public static final int VIPCHECKPASS_PUSH = 3; // 会员审核通过推送
        public static final int NOTICE_MESSAGENUM_PUSH = 4; // 人脉圈动态提醒，比如xx赞了你的留言、xx评论了你等

        public static final int NOTICE_NEW_FRIEND_PUSH = 5; // 新的朋友
        public static final int NOTICE_NEW_INNERMSG_PUSH = 6; // 站内信
        public static final int NOTICE_SYSTEMMSG_PUSH = 7; // 和聊助手
        public static final int NOTICE_TOUTIAO_PUSH = 8; // 行业头条
        public static final int NOTICE_VCARDREC_PUSH = 10; //名片识别成功推送
        public static final int NOTICE_REALNAMEAUTH_PUSH = 11; //实名认证审核结果推送
    }

    public static class ConfigureTimeouts {
        /* 从连接池中取连接的超时时间 */
        public static final int GET_REQUEST_TIME_OUT = 10;
        /* 连接超时 */
        public static final int CONNECT_TIME_OUT = 15;
        /* 请求超时 */
        public static final int REQUEST_TIME_OUT = 30;
    }

    public static String HTTP_CHAR_SET = "GBK";
    public static String HTTP_CHAR_SET_UTF = "UTF-8";

    /*应用公用SharedPreferences*/
    public static final String HL_SHAREDPREFERENCES = "hl_SharedPreferences";

    /*用户私有SharedPreferences*/
    public static final String USER_SHAREDPREFERENCES = "user_SharedPreferences";

    /*SharedPreferences字段类型*/
    public static class SHAREDPREFERENCES_KEY {
        public static final String RENMAIQUAN_SYNC_TO_WEICHAT = "renmaiquan_sync_to_weichat";//发布人脉圈同时同步到朋友圈
        public static final String RENMAIQUAN_UNREAD_USERFACE = "renmaiquan_unread_userface";//人脉圈未读消息头像
        public static final String RENMAIQUAN_UNREAD_COUNT = "renmaiquan_unread_count";//人脉圈未读消息数量
        public static final String NEW_FRIEND_UNREAD_COUNT = "new_friend_unread_count";//人脉未读消息数量

        public static final String CONTACTS_MAX_ID = "contacts_maxId";//人脉获取返回的maxid
        public static final String CONTACTS_MAX_MOBILE_ID = "contacts_maxMobileId";//人脉获取返回的手机联系人maxid
        public static final String CONTACTS_MAX_CARD_ID = "contacts_maxCardId";//人脉获取返回的名片maxid
        public static final String CONTACTS_LAST_UPDATE_DATE = "contacts_lastUpdatedDate";//人脉获取返回的最后更新时间maxLastUpdatedDate

        public static final String IM_CONVERSATION_HELIAO_HELPER_LAST_MSG = "im_conversation_heliao_helper_last_msg";//对话列表中，和聊助手的最后一条消息
        public static final String IM_CONVERSATION_HELIAO_HELPER_UNREAD_COUNT = "im_conversation_heliao_helper_unread_count";//对话列表中，和聊助手的未读消息个数
        public static final String IM_CONVERSATION_HELIAO_HELPER_CREATE_TIME = "im_conversation_heliao_helper_create_time";//对话列表中，和聊助手的最后更新时间
        public static final String IM_CONVERSATION_TOUTIAO_LAST_MSG = "im_conversation_toutiao_last_msg";//对话列表中，行业头条的最后一条消息
        public static final String IM_CONVERSATION_TOUTIAO_UNREAD_COUNT = "im_conversation_toutiao_unread_count";//对话列表中，行业头条的未读消息个数
        public static final String IM_CONVERSATION_TOUTIAO_CREATE_TIME = "im_conversation_toutiao_create_time";//对话列表中，行业头条的最后更新时间

        public static final String IM_CONVERSATION_LASTLOAD_TIME = "im_conversation_lastload_time";//最后加载时间的毫秒值。此字段做强制的校验，此字段不能为空，为空时请传0给服务器端
        /**
         * 判断是否添加了和聊桌面快捷方式
         */
        public static final String ADD_HELIAO_SHORT_CUT = "add_heliao_short_cut";
        /**
         * 判断是否已经看到“为好友打分”这个new的项目
         */
        public static final String USER_GRADE_NEW = "show_user_grade_new";
        /**
         * 判断是否已经看到“我的界面 收藏”这个new的项目
         */
        public static final String USER_USER_COLLECT_NEW = "show_user_collect_new";
        /**
         * 判断是否已经看到“我的界面 钱包”这个new的项目
         */
        public static final String USER_USER_WALLET_NEW = "show_user_wallet_new";
        /**
         * 判断是否已经看到“对话界面 红包”这个new的项目
         */
        public static final String USER_USER_CHAT_WALLET_NEW = "show_user_chat_wallet_new";
        /**
         * 判断是否已经看到“对话界面 红包广告”这个new的项目
         */
        public static final String USER_USER_CHAT_LUCKYMONEY_AD_NEW = "show_user_chat_luckymoney_ad_new";
        /**
         * 判断是否已经看到“档案 右上角人脉秘书红点提示”这个new的项目
         */
        public static final String USER_USER_ARCHIVE_SECRETARY_UREAD_NEW = "show_user_archive_secretary_uread_new";
        public static final String USER_USER_ARCHIVE_SECRETARY_NEW = "show_user_archive_secretary_new";
        /**
         * 判断是否已经看到“我的界面 企业查询”这个new的项目
         */
        public static final String USER_USER_COMPANY_AUTH_NEW = "show_user_company_auth_new";
        /**
         * 判断我的界面 是否有和财富、赞服务等
         */
        public static final String MY_FRAGMENT_HECAIFU_EXIST = "my_fragment_hecaifu_exist";
        public static final String MY_FRAGMENT_ZANFUWU_EXIST = "my_fragment_zanfuwu_exist";
        public static final String MY_FRAGMENT_HECAIFU_TITLE = "my_fragment_hecaifu_title";
        public static final String MY_FRAGMENT_HECAIFU_TIP = "my_fragment_hecaifu_tip";
        public static final String MY_FRAGMENT_HECAIFU_URL = "my_fragment_hecaifu_url";
        public static final String MY_FRAGMENT_ZANFUWU_TITLE = "my_fragment_zanfuwu_title";
        public static final String MY_FRAGMENT_ZANFUWU_TIP = "my_fragment_zanfuwu_tip";
        public static final String MY_FRAGMENT_ZANFUWU_URL = "my_fragment_zanfuwu_url";

        //maxUpdatTime         上一次检测更新时间
        //maxContactHlmemberId 上次返回的最大memberId
        //maxContactMobileId   上次返回的最大mobileId
        //maxContactCardId     上次返回的最大cardId
        //requestCount         获取个数限制，默认设为200
        public static final String CONTACTS_MAXUPDATTIME = "contacts_maxupdattime";
        public static final String CONTACTS_MAXCONTACTHLMEMBERID = "contacts_maxcontacthlmemberid";
        public static final String CONTACTS_MAXCONTACTMOBILEID = "contacts_maxcontactmobileid";
        public static final String CONTACTS_MAXCONTACTCARDID = "contacts_maxcontactcardid";
        public static final String CONTACTS_REQUESTCOUNT = "contacts_requestcount";

        //常用联系人
        public static final String CONTACTS_OFEN_USERD = "contacts_ofen_userd";

        //新的好友界面的“附近的人脉”是否需要显示小红点
        public static final String NEARBY_HAS_NEW = "nearby_has_new";

    }

    public static final int RENMAIQUAN_SYNC_TO_WEICHAT_DELAY_TIME = 2000;//2秒钟之后同步人脉圈

	/*新浪微博分享*/
    /**
     * 当前 DEMO 应用的 APP_KEY，第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY
     */
    public static final String SINA_WEIBO_APP_KEY = "4247054666";

    /**
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     * <p>
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    public static final String SINA_WEIBO_REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     * <p>
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     * <p>
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     * <p>
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    public static final String SINA_WEIBO_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read," + "follow_app_official_microblog,"
            + "invitation_write";

    /**
     * 信鸽注册成功返回的device Token
     */
    public static String PUSH_DFVICE_TOKEN = "";
    /**
     * 判别用户身份证号的正则表达式（要么是15位，要么是18位，最后一位可以为字母）
     */
    public static final String IDENTITY_CARD_PATTERN = "(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])";
    /**
     * 和聊Log TAG
     */
    public static final String HL_TAG = "和聊Log";

    //人脉圈重绘
    public static class RENMAIQUAN_CONSTANTS {
        public static final String REQUEST_TYPE_NEW = "new";//加载人脉圈类型——new
        public static final String REQUEST_TYPE_MORE = "more";//加载人脉圈类型——more
        public static final int REQUEST_COUNT = 20;// 每次向服务器获取的数据量
        public static final int LOAD_TYPE_LOADING = 1;// 正在加载更多
        public static final int LOAD_TYPE_READY = 2;// 准备加载更多
        public static final int LOAD_TYPE_END = 3;// 加载完毕，没有更多数据了
        public static final int LOAD_TYPE_LOADING_WITHOUT_TEXT = 4;// 正在加载更多,只有一个progressbar
        public static final int RMQ_CONTENT_MAX_LINE = 7;// 人脉圈正文最大行数
        public static final String RMQ_QUEST_TAG = "renmaiquan";// 人脉圈OKhttp请求的tag
        public static final int RMQ_UPLOAD_STATE_UPLOADING = 0;// 正在上传
        public static final int RMQ_UPLOAD_STATE_ERROR = -1;// 上传失败
    }

    public static class RENMAIQUAN_REQUEST_CODE {
        public static final int REQUEST_CODE_ADDNEWMSG = 1;// 新增人脉圈
    }

    public static class ARCHIVE_REQUEST_CODE {
        public static final int REQUEST_CODE_ARCHIVE_MORE = 3001;// 查看会员资料的更多页面
        public static final int REQUEST_CODE_COMPANY_AUTH = 3002;//企业认证/查询
    }

    public static class CONTACTS_REQUEST_CODE {
        public static final int CONTACTS_REQUEST_CHECK_NEW_FRIEND = 1;// 人脉列表界面跳转到新的好友界面
    }

    /**
     * 给好友打分、评论
     */
    public static class USER_GRADE_REQUEST_CODE {
        public static final int USER_GRADE_REQUEST = 1;
        public static final int USER_SECRETARY_REQUEST = 2;//人脉秘书
    }

    //IM错误提示code
    public static class IM_ERROR_CODE {
        public static final String ERR_CODE_NOT_IN_GROUP = "130003";//用户不在群中
        public static final String ERR_CODE_NOT_GROUP_MEMBER = "130012";//群成员不存在
    }

    //sd卡视频的md5值，用于判断SD卡视频文件是否完整
    public static final String HL_GUIDE_VIDEO_MD5 = "f8d22d222c8f7dfe107bdcdae5f99b79";

    //对话重绘
    public static class CHAT_CONSTANTS {
        public static final int REQUEST_COUNT = 15;// 每次加载IM消息的数据量
        public static final String RMQ_QUEST_TAG = "chat_okhttp";// 对话OKhttp请求的tag
        public static final String SEPARATOR = "|";//imgsUrlList存储的用于分割messageId和image url的标志
        public static int CHAT_AUDIO_BCG_LENGTH = 200;// 語音消息泡最長是200dp
        public static final int IM_REQUEST_CODE_CHOOSE_CONTACTS = 2007;//对话界面，发送名片时的请求码
        public static final int IM_REQUEST_CODE_REMIND = 2006;//对话界面，@的回调
        public static final int IM_REQUEST_CODE_CHECK_TOPIC = 2016;//对话界面，查看话题
        public static final int IM_USER_DELETE_CIRCLE = 2005;
        public static final int IM_CHAT_TO_CIRCLE = 2008;
        public static final long IM_SHOW_TIME_DIFF = 5 * 60L * 1000L;// 每次加载IM消息的数据量
        public static final int IM_FOWRARD_RECENT_CONTACTS_MAX_NUM = 20;// 转发给和聊好友时，供选择的最近聊天的联系人数目

    }

    public static String COMMONCONTACT = "0常用联系人";//加0方便排序

    //对话列表重绘
    public static class CONVERSATION_CONSTANTS {
        public static final String CHAT_CONVER_INFO_NAME = "name";// 单聊时，存在conversation extension map里的name key值
        public static final String CHAT_CONVER_INFO_USERFACE = "userface";// 单聊时，存在conversation extension map里的userface key值
    }

    //脉可寻 名片扫描
    public static class MAI_KE_XUN {
        public static final String PUID = "151103";
        public static final String KEY = "339CCBD56F00523BF03365741FA89A1E";
        public static final String SECRET = "ebdf1f0994ceaf37535d2f45377c987ac7aa1238192dfa737267f4fa9c2ce8280ad2cc650eb0c78ff2fae5be02aa7b927b6839f915317117b072860c39af60cb";//imgsUrlList存储的用于分割messageId和image url的标志
        public static final int TIMER_DELAY = 3000;//定时器获取名片信息的间隔时间，8秒(脉可寻是心跳机制，每隔10秒，连续取。结果也只返回一次。)
        public static final int DETAIL_TIMER_DELAY = 8000;//名片详情页，定时器获取名片信息的间隔时间，8秒(脉可寻是心跳机制，每隔10秒，连续取。结果也只返回一次。)
        public static final int CARD_DETAIL_RESULT_CODE = 10;
        public static final int CARD_LIST_REQUEST_COUNT = 3;//名片列表页向服务端请求的次数，超过次数，加载进度条消失
    }

    //查看脉可寻 名片详情状态值
    public static class MAI_KE_XUN_HANDLE_DETAIL_TYPE {
        public static final int SAVE = 1;//保存
        public static final int DELETE = 2;//删除
    }

    //人脉头像背景
    public static int[] AVATARBG = {R.drawable.avatar_bg1_solid_circle_style,
            R.drawable.avatar_bg2_solid_circle_style, R.drawable.avatar_bg3_solid_circle_style,
            R.drawable.avatar_bg4_solid_circle_style, R.drawable.avatar_bg5_solid_circle_style,
            R.drawable.avatar_bg6_solid_circle_style, R.drawable.avatar_bg7_solid_circle_style,
            R.drawable.avatar_bg8_solid_circle_style, R.drawable.avatar_bg9_solid_circle_style,
            R.drawable.avatar_bg10_solid_circle_style, R.drawable.avatar_bg11_solid_circle_style,
            R.drawable.avatar_bg12_solid_circle_style};

    //应用内所有缓存文件的存储目录
    public static class CACHE_PATH {
        /*下载目录，譬如IM发送文件的下载目录*/
        public static final String IM_DOWNLOAD_PATH = FileUtil.CURRENT_PATH
                + File.separator + "download" + File.separator;
        //存放开机视频路径
        public static final String HL_GUIDE_VIDEO_PATH = FileUtil.CURRENT_PATH
                + File.separator + "video.mp4";

        /**
         * 脉可寻拍摄的名片保存路径
         */
        public static final String MAI_KE_XUN_CARD_PATH = "renhe" + File.separator + "ocrCard";

        //表情数据库保存路径
        public static final String EMOTION_DB_PATH = FileUtil.CURRENT_PATH + File.separator + "database"
                + File.separator + "emotions.db";

        //保存应用崩溃日志的路径
        public static final String HL_CRASH_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "renhe" + File.separator + "crashLog" + File.separator;
        // 保存头像的指定路径
        public static final String AVATERPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "renhe" + File.separator + "avater" + File.separator;

        //长按图片的保存路径
        public static final String PICTUREPATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "renhe" + File.separator + "pic" + File.separator;

        //assets城市/行业,学校 DB本地数据库保存路径
        public static final String ASSETS_DB_PATH = FileUtil.CURRENT_PATH + File.separator + "database"
                + File.separator;

        //和聊应用icon保存路径，(用于分享到微信、qq等时用，没有图片时，默认使用icon昨晚图片)
        public static final String HL_ICON_PATH = FileUtil.CURRENT_PATH + File.separator;
        // 发人脉圈、IM发照片 拍照后存放的图片路径
        public static final String HL_IMAGESELECTOR_CAMERA_PATH = "/renhe/ImageSelector/Pictures";
    }

    //支付宝支付状态码
    public static class AlipayCode {
        public static final String ALIPAY_SUCCESS = "9000";//订单支付成功
        public static final String ALIPAY_HANDLING = "8000";//正在处理中
        public static final String ALIPAY_FAIL = "4000";//订单支付失败
        public static final String ALIPAY_USER_CANCLE = "6001";//用户中途取消
        public static final String ALIPAY_NETWORK_ERROR = "6002";//网络连接出错
    }

    public static final String TOPIC_URL = "xiaozuh5.renhe.cn";//圈子话题/我的话题 域名，用来区分点击url是往普通的webview跳，还是往专门给话题定制的webview跳

    //聊天自定义的type，eg：红包、新人刚加入圈子打个招呼、发布了一个新话题
    public static class CHAT_CUSTOM_TYPE {
        public static final String CUSTOM_TYPE_KEY = "type";//文本扩展消息以`extension`中存在type字段来标识
        public static final String MESSAGE_LUCKY_MONEY = "red";//红包类型
        public static final String MESSAGE_LUCKY_MONEY_NOTICE = "red_notice";//红包通知类型(客户端将红包关键字高亮)
        public static final String MESSAGE_LUCKY_MONEY_AD_NOTICE = "ad_red_notice";//红包广告通知类型(客户端将红包关键字高亮)
        public static final String MESSAGE_TOPIC_REPLY = "topic_reply";//别人回复你的话题，发送的系统消息
        public static final String MESSAGE_TOPIC_SEND = "topic_send";//成员发布新话题，在群里发一条特殊消息
        public static final String MESSAGE_CIRCLE_NEW_MEMBER_PROFILE = "profile";//刚加入圈子，发条打招呼内容
        public static final String CONVERSATION_TOPIC_REPLY = "topic_reply";//别人回复你的帖子提醒：相当于是@消息
        public static final String MESSAGE_LUCKY_MONEY_AD = "ad_red";//红包广告类型
    }

    //根据特定url过滤，跳转
    public static class PUBLIC_FILETER_URL {
        public static final String RENHE_PROFILE = "http://m.renhe.cn/heliao/profile.shtml";
        public static final String HELIAO_PROFILE = "heliaoapp://m.renhe.cn/heliao/profile.shtml";
        public static final String RENHE_CIRCLE = "http://m.renhe.cn/heliao/circle.shtml";
    }

    //对话重绘
    public static class PAY_PASSWORD_SET {
        public static final int SET_TYPE_RESET_INPUT_NEW_PASSWORD = 1;// 忘记支付密码,输入新的支付密码
        public static final int SET_TYPE_RESET_SURE_NEW_PASSWORD = 2;// 忘记支付密码,确认新的支付密码
        public static final int SET_TYPE_RESET_INPUT_OLD_PASSWORD = 3;// 修改支付密码，输入老的支付密码
    }

    public static final String LUCKY_HELP_URL = "http://m.renhe.cn/service/hongbao.htm";
    public static final String CASH_HELP_URL = "http://m.renhe.cn/service/hongbaotixian.htm";
    /**
     * 1; // 人脉快递使用帮助文档
     * 2; // 人脉秘书使用帮助文档
     * 3; // 企业查询使用帮助文档
     * 4; // 企业验证查询帮助文档
     * 5; // 邀请好友发红包使用帮助当地
     * 6; // 广告红包使用帮助文档
     */
    public static final String TOPSPEED_INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/1";
    public static final String COURIER_INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/2";
    public static final String COMPANY_CHECK_INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/3";
    public static final String COMPANY_VALID_CHECK__INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/4";
    public static final String ADDFRIEND_ADDRED_INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/5";
    public static final String LUCKYAD_INVITE_HELP_URL = "http://heliaom.renhe.cn/helpDocument/6";
    public static final String TAB_ME_COMPANY_SEARCH_URL = "http://heliaom.renhe.cn/enterprise/search";
    public static final String ARCHIVE_COMPANY_SEARCH_URL = "http://heliaom.renhe.cn/enterprise/searchList?keyword=";// +查询内容&viewMemberId=被查询中的memberid
    public static final String ARCHIVE_COMPANY_AUTH_URL = "http://heliaom.renhe.cn/enterprise/searchList?keyword=";// +查询内容&viewMemberId=被查询中的memberid

    /**
     * 接受第三方应用（赞服务）分享内容到和聊
     */
    public static final String OTHER_APP_SHARE_TO_HELIAO_FRIEND = "heliao.android.intent.action.friend";// 接受第三方应用（赞服务）分享内容到和聊好友
    public static final String OTHER_APP_SHARE_TO_HELIAO_RENMAIQUAN = "heliao.android.intent.action.renmaiquan";// 接受第三方应用（赞服务）分享内容到和聊人脉圈

    /**
     * GRPC
     * release: heliaorpc.renhe.cn port:8090 对应4g
     * test: heliaorpctest.renhe.cn port:8091 对应4gtest
     */
    public static class GRPC {
        /**
         * grpc host地址
         *
         * @return
         */
        public final static String getGrcpHost() {
            if (HTTP_DEBUG_MODE) {
                return "heliaorpctest.renhe.cn";
//                return "xiaozudh5.renhe.cn";
            }
            return "heliaorpc.renhe.cn";
        }

        /**
         * grpc 端口
         *
         * @return
         */
        public final static int getGrcpPort() {
            if (HTTP_DEBUG_MODE) {
                return 8091;
//                return 80;
            }
            return 8090;
        }

        public static final String ERROR_INFO = "网络访问错误，请检查网络设置！";//对应strings文件里的

    }
}

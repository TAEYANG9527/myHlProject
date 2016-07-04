package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * 1,查看自己客厅留言接口(自己发布的客厅留言及自己关注的客厅留言) 2,查看朋友客厅的留言接口 3,查看同城的留言接口 4,查看同行的留言接口
 * 5,最受关注的留言接口
 */
public class MessageBoards implements Serializable {

    private static final long serialVersionUID = -6501243574466513990L;
    private int state; // 请求是否成功结果 说明：1 请求成功；-3：type必须为renew、new、more；-4：type类型为new，而maxCreatedDate和maxLastUpdaatedDate未正确设置值；-5：type类型为more，而minCreatedDate和minLastUpdaatedDate未指定
    private NewNoticeList[] newNoticeList; // 新增的人脉圈列表
    private UpdatedNoticeList[] updatedNoticeList; //更新的人脉圈列表
    private long maxRank; //long 说明：最大的创建时间，type为renew和new时才会返回
    private long minRank; //long 说明：最小的创建时间，type为renew和more时才会返回
    private DeleteNoticeList[] deleteNoticeList; // 删除的人脉圈列表
    private boolean isEnd;//判断人脉圈下拉加载更多，是不是到底了
    /**
     * type String 人脉圈类型:
     * 1、"message_board" 客厅类型
     * 2、"member_update_contact" 更新了联系方式
     * 3、"member_update_user_face" 更新了头像
     * 4、"profile_add_position" 新增了工作经历
     * 5、"profile_update_position" 更新了工作经历
     * 6、"profile_add_education" 新增了教育经历
     * 7、"profile_update_education" 更新了教育经历
     */

    public static final int MESSAGE_TYPE_MESSAGE_BOARD = 1;
    public static final int MESSAGE_TYPE_MEMBER_UPDATE_CONTACT = 2;
    public static final int MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE = 3;
    public static final int MESSAGE_TYPE_PROFILE_ADD_POSITION = 4;
    public static final int MESSAGE_TYPE_PROFILE_UPDATE_POSITION = 5;
    public static final int MESSAGE_TYPE_PROFILE_ADD_EDUCATION = 6;
    public static final int MESSAGE_TYPE_PROFILE_UPDATE_EDUCATION = 7;
    public static final int MESSAGE_TYPE_PROFILE_UPDATE_COVER = 8;

    public static final int MESSAGE_TYPE_BINDPHONE = 9;//绑定手机引导
    public static final int MESSAGE_TYPE_IMPORT_CONTACT = 10;//导入通讯录引导
    public static final int MESSAGE_TYPE_RECOMMEND_FRIEND = 11;//好友推荐
    public static final int MESSAGE_TYPE_FRIEND_JOIN_NOTICE = 12;//好友加入提醒
    public static final int MESSAGE_TYPE_PERFECT_PROFILE = 13;//完善资料
    public static final int MESSAGE_TYPE_UPLOAD_AVATAR = 14;//上传头像
    public static final int MESSAGE_TYPE_WEBSITE = 15;//网页引导
    public static final int MESSAGE_TYPE_VIP_UPGRADE_TIP = 16;//好友升级会员后，人脉圈动态提示
    public static final int MESSAGE_TYPE_PUBLISH_JOG = 17;//发布职位动态
    public static final int MESSAGE_TYPE_PUBLISH_ACTIVITY = 18;//发布活动动态
    public static final int MESSAGE_TYPE_PUBLISH_XIAOZU = 19;//发布小组动态
    public static final int MESSAGE_TYPE_REALNAME = 20;//实名认证动态

    public static final int MESSAGE_TYPE_ADD_NEWMSG = 100;//发布新的人脉圈
    public static final int MESSAGE_TYPE_UNREAD_NOTICE = 101;//未读新消息，为了方便在recyclerview中添加一个header，将其定义为一个人脉圈类型添加到list头部

    /**
     * 人脉圈类型——人脉圈、发现
     *
     * @author Renhe
     */
    public static final int MESSAGE_TYPE_RENMAIQUAN = 1;
    public static final int MESSAGE_TYPE_FAXIAN = 2;

    public static class NewNoticeList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int type;//人脉圈类型
        private SenderInfo senderInfo;
        private ContentInfo contentInfo;
        private long createdDate;//：long 人脉圈的创建毫秒数
        private int source;// 动态来源，0:好友与自己 1:好友的好友 2:同城 3:同行
        private long rank;// 动态评分
        private int packUpState;//在人脉圈列表正文显示的状态：0：缩略显示  1：全文显示
        private int uploadState;//只有当type = MESSAGE_TYPE_ADD_NEWMSG 时才有该值,留言上传状态，0代表上传失败，1代表正在上传
        private double lineNum;//在人脉圈列表正文显示的行数

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public SenderInfo getSenderInfo() {
            return senderInfo;
        }

        public void setSenderInfo(SenderInfo senderInfo) {
            this.senderInfo = senderInfo;
        }

        public ContentInfo getContentInfo() {
            return contentInfo;
        }

        public void setContentInfo(ContentInfo contentInfo) {
            this.contentInfo = contentInfo;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public int getSource() {
            return source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        public long getRank() {
            return rank;
        }

        public void setRank(long rank) {
            this.rank = rank;
        }

        public int getPackUpState() {
            return packUpState;
        }

        public int getUploadState() {
            return uploadState;
        }

        public void setUploadState(int uploadState) {
            this.uploadState = uploadState;
        }

        public void setPackUpState(int packUpState) {

            this.packUpState = packUpState;
        }

        public double getLineNum() {
            return lineNum;
        }

        public void setLineNum(double lineNum) {
            this.lineNum = lineNum;
        }
    }

    public class UpdatedNoticeList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int type;//人脉圈类型  目前只支持message_board类型的数据更新 ,仅支持type = 1；
        private UpdatedNoticeListContentInfo contentInfo;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public UpdatedNoticeListContentInfo getContentInfo() {
            return contentInfo;
        }

        public void setContentInfo(UpdatedNoticeListContentInfo contentInfo) {
            this.contentInfo = contentInfo;
        }

    }

    public static class SenderInfo implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private String sid; // String 说明：发送者sid
        private String name;// String 说明：发送者姓名
        private String userface; //String 说明：发送者头像
        private String title;// String 说明：发送者当前职务
        private String company; //String 说明：发送者当前公司
        private String industry; //String 说明：发送者行业
        private String location; //String 说明：发送者地区
        private int accountType; //int 说明：发送者vip类型
        private boolean isRealname; //boolean 说明：发送者是否已通过实名认证}
        private int friendStatus;//好友状态，0好友 1不是好友 2已发送好友请求

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserface() {
            return userface;
        }

        public void setUserface(String userface) {
            this.userface = userface;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getAccountType() {
            return accountType;
        }

        public void setAccountType(int accountType) {
            this.accountType = accountType;
        }

        public boolean isRealname() {
            return isRealname;
        }

        public void setRealname(boolean isRealname) {
            this.isRealname = isRealname;
        }

        public int getFriendState() {
            return friendStatus;
        }

        public void setFriendState(int friendState) {
            this.friendStatus = friendState;
        }
    }

    public static class ContentInfo implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// int 说明：客厅的id
        private String objectId;// String 说明：客厅的objectId
        private String content;// String 说明：客厅的内容
        private ForwardMessageBoardInfo forwardMessageBoardInfo;
        private AtMemmber[] atMembers;//留言内容中@信息
        private int replyNum;//说明：回复数量
        private ReplyList[] replyList;//回复列表
        private boolean liked;//是否已经点赞了此条留言
        private int likedNum;//说明：点赞的数量
        private LikedList[] likedList;
        private PicList[] picList;
        private RecommendMember[] members;//type为11、12时的用户列表
        private String subject;
        private String url;
        private String banner;//vip升级banner图
        private int messageboardPublicationId;//只有当type = MESSAGE_TYPE_ADD_NEWMSG 时才有该值,预发布的客厅内容id

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public ForwardMessageBoardInfo getForwardMessageBoardInfo() {
            return forwardMessageBoardInfo;
        }

        public void setForwardMessageBoardInfo(ForwardMessageBoardInfo forwardMessageBoardInfo) {
            this.forwardMessageBoardInfo = forwardMessageBoardInfo;
        }

        public AtMemmber[] getAtMembers() {
            return atMembers;
        }

        public void setAtMembers(AtMemmber[] atMembers) {
            this.atMembers = atMembers;
        }

        public int getReplyNum() {
            return replyNum;
        }

        public void setReplyNum(int replyNum) {
            this.replyNum = replyNum;
        }

        public ReplyList[] getReplyList() {
            return replyList;
        }

        public void setReplyList(ReplyList[] replyList) {
            this.replyList = replyList;
        }

        public boolean isLiked() {
            return liked;
        }

        public void setLiked(boolean liked) {
            this.liked = liked;
        }

        public int getLikedNum() {
            return likedNum;
        }

        public void setLikedNum(int likedNum) {
            this.likedNum = likedNum;
        }

        public LikedList[] getLikedList() {
            return likedList;
        }

        public void setLikedList(LikedList[] likedList) {
            this.likedList = likedList;
        }

        public PicList[] getPicList() {
            return picList;
        }

        public void setPicList(PicList[] picList) {
            this.picList = picList;
        }

        public RecommendMember[] getMembers() {
            return members;
        }

        public void setMembers(RecommendMember[] members) {
            this.members = members;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getMessageboardPublicationId() {
            return messageboardPublicationId;
        }

        public void setMessageboardPublicationId(int messageboardPublicationId) {
            this.messageboardPublicationId = messageboardPublicationId;
        }

        public String getBanner() {
            return banner;
        }

        public void setBanner(String banner) {
            this.banner = banner;
        }
    }

    public static class ForwardMessageBoardInfo implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private boolean isForwardRenhe;//: boolean 是否是人和网的转发，是人和网的转发时会返回name、sid、objectId、id
        private String objectId;//：String 被转发的客厅objectId
        private int id;//：int 被转发的客厅id
        private String name;//: String 转发者的姓名
        private String sid;//: String 转发者的sid
        private String content;//: String 转发的内容
        private AtMemmber[] atMembers;//留言内容中@信息
        private PicList[] picList;//留言内容中@信息

        private int type;
        private webShare webShare;
        private circleShare circleShare;
        private profileShare profileShare;

        public boolean isForwardRenhe() {
            return isForwardRenhe;
        }

        public void setForwardRenhe(boolean isForwardRenhe) {
            this.isForwardRenhe = isForwardRenhe;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public AtMemmber[] getAtMembers() {
            return atMembers;
        }

        public void setAtMembers(AtMemmber[] atMembers) {
            this.atMembers = atMembers;
        }

        public PicList[] getPicLists() {
            return picList;
        }

        public void setPicLists(PicList[] picLists) {
            this.picList = picLists;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public circleShare getCircleShare() {
            return circleShare;
        }

        public void setCircleShare(circleShare circleShare) {
            this.circleShare = circleShare;
        }

        public profileShare getProfileShare() {
            return profileShare;
        }

        public void setProfileShare(profileShare profileShare) {
            this.profileShare = profileShare;
        }

        public webShare getWebsShare() {
            return webShare;
        }

        public void setWebsShare(webShare websShare) {
            this.webShare = websShare;
        }

    }

    public static class webShare implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// int 网页分享ID
        private String url;// String 网页链接
        private String content;// String 网页标题
        private String picUrl;// String 网页图标url

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

    }

    public static class circleShare implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// int 网页分享ID
        private String name;// String 网页链接
        private String note;// String 网页标题
        private String picUrl;// String 网页图标url

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

    }

    public static class profileShare implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String sid;// String 网页链接
        private String name;// String 网页标题
        private String job;// String 网页图标url
        private String company;// String 网页图标url
        private String picUrl;// String 网页图标url

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getJob() {
            return job;
        }

        public void setJob(String job) {
            this.job = job;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

    }

    public static class AtMemmber implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String memberSid;//：String @者加密后的id
        private String memberName;//：String @者姓名

        public String getMemberSid() {
            return memberSid;
        }

        public void setMemberSid(String memberSid) {
            this.memberSid = memberSid;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        @Override
        public String toString() {
            return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("memberSid", memberSid)
                    .append("memberName", memberName).toString();
        }

    }

    public static class ReplyList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// 留言在mysql中的id
        private String objectId;// 留言的objectId
        private String senderSid; // 留言者的sid
        private String senderName; // 留言者姓名
        private String content; // 留言内容
        private String reSenderSid;// 被回复者的sid
        private String reSenderMemberName;// 被回复者姓名

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSenderSid() {
            return senderSid;
        }

        public void setSenderSid(String senderSid) {
            this.senderSid = senderSid;
        }

        public String getSenderName() {
            return senderName;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getReSenderSid() {
            return reSenderSid;
        }

        public void setReSenderSid(String reSenderSid) {
            this.reSenderSid = reSenderSid;
        }

        public String getReSenderMemberName() {
            return reSenderMemberName;
        }

        public void setReSenderMemberName(String reSenderMemberName) {
            this.reSenderMemberName = reSenderMemberName;
        }

    }

    public static class LikedList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String sid;// 点赞者的sid
        private String name;// 点赞者的name
        private String userface;

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserface() {
            return userface;
        }

        public void setUserface(String userface) {
            this.userface = userface;
        }

    }

    public static class PicList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String thumbnailPicUrl;// 说明：图片的缩略图信息，用于缩略图显示时候的图片url
        private String bmiddlePicUrl;// 图片的大图信息，用于加载大图时候的图片url

        private int bmiddlePicWidth;//图片的大图信息的宽度，当图片张数为1时返回
        private int bmiddlePicHeight;//说明：图片的大图信息的高度，当图片张数为1时返回

        private String resourceId;//只有当type = MESSAGE_TYPE_ADD_NEWMSG 时才有该值,预发布的客厅图片的资源id，每个id为uuid，数据类型为String类型
        private int uploadState;//只有当type = MESSAGE_TYPE_ADD_NEWMSG 时才有该值,图片上传状态，1代表已上传，0代表为上传

        public String getThumbnailPicUrl() {
            return thumbnailPicUrl;
        }

        public void setThumbnailPicUrl(String thumbnailPicUrl) {
            this.thumbnailPicUrl = thumbnailPicUrl;
        }

        public String getBmiddlePicUrl() {
            return bmiddlePicUrl;
        }

        public void setBmiddlePicUrl(String bmiddlePicUrl) {
            this.bmiddlePicUrl = bmiddlePicUrl;
        }

        public int getBmiddlePicWidth() {
            return bmiddlePicWidth;
        }

        public void setBmiddlePicWidth(int bmiddlePicWidth) {
            this.bmiddlePicWidth = bmiddlePicWidth;
        }

        public int getBmiddlePicHeight() {
            return bmiddlePicHeight;
        }

        public void setBmiddlePicHeight(int bmiddlePicHeight) {
            this.bmiddlePicHeight = bmiddlePicHeight;
        }

        public String getResourceId() {
            return resourceId;
        }

        public int getUploadState() {
            return uploadState;
        }

        public void setUploadState(int uploadState) {
            this.uploadState = uploadState;
        }

        public void setResourceId(String resourceId) {

            this.resourceId = resourceId;
        }
    }

    public static class UpdatedNoticeListContentInfo implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// int 说明：客厅的id
        private String objectId;// String 说明：客厅的objectId
        private int replyNum;//说明：回复数量
        private ReplyList[] replyList;//回复列表
        private boolean liked;//是否已经点赞了此条留言
        private int likedNum;//说明：点赞的数量
        private LikedList[] likedList;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public int getReplyNum() {
            return replyNum;
        }

        public void setReplyNum(int replyNum) {
            this.replyNum = replyNum;
        }

        public ReplyList[] getReplyList() {
            return replyList;
        }

        public void setReplyList(ReplyList[] replyList) {
            this.replyList = replyList;
        }

        public boolean isLiked() {
            return liked;
        }

        public void setLiked(boolean liked) {
            this.liked = liked;
        }

        public int getLikedNum() {
            return likedNum;
        }

        public void setLikedNum(int likedNum) {
            this.likedNum = likedNum;
        }

        public LikedList[] getLikedList() {
            return likedList;
        }

        public void setLikedList(LikedList[] likedList) {
            this.likedList = likedList;
        }
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public NewNoticeList[] getNewNoticeList() {
        return newNoticeList;
    }

    public void setNewNoticeList(NewNoticeList[] newNoticeList) {
        this.newNoticeList = newNoticeList;
    }

    public UpdatedNoticeList[] getUpdatedNoticeList() {
        return updatedNoticeList;
    }

    public void setUpdatedNoticeList(UpdatedNoticeList[] updatedNoticeList) {
        this.updatedNoticeList = updatedNoticeList;
    }

    public long getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(long maxRank) {
        this.maxRank = maxRank;
    }

    public long getMinRank() {
        return minRank;
    }

    public void setMinRank(long minRank) {
        this.minRank = minRank;
    }

    public class DeleteNoticeList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String objectId;// String 说明：被删除的objectId

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }
    }

    public DeleteNoticeList[] getDeleteNoticeList() {
        return deleteNoticeList;
    }

    public void setDeleteNoticeList(DeleteNoticeList[] deleteNoticeList) {
        this.deleteNoticeList = deleteNoticeList;
    }

    public static class RecommendMember implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private String sid;// String 说明：发送者sid
        private String name;// String 说明：发送者姓名
        private String userface;// String 说明：发送者头像
        private String title;// String 说明：发送者当前职务
        private String company;// String 说明：发送者当前公司
        private String industry;// String 说明：发送者行业
        private String location;//String 说明：发送者地区
        private int accountType;//int 说明：发送者vip类型
        private boolean isRealname;// boolean 说明：发送者是否已通过实名认证

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUserface() {
            return userface;
        }

        public void setUserface(String userface) {
            this.userface = userface;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public int getAccountType() {
            return accountType;
        }

        public void setAccountType(int accountType) {
            this.accountType = accountType;
        }

        public boolean isRealname() {
            return isRealname;
        }

        public void setRealname(boolean isRealname) {
            this.isRealname = isRealname;
        }

    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

}

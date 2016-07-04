package com.itcalf.renhe.dto;

import java.io.Serializable;
import java.util.Arrays;

public class ContactList implements Serializable {

    private static final long serialVersionUID = -1187223497234726128L;
    private int state;
    private int maxCid;
    private long maxLastUpdatedDate;// 最大更新时间，请求更新及删除列表时需要带上
    private Member[] memberList;
    private Member[] updateMemberList;//更新列表
    private MemberId[] deleteMemberIdList;//删除表
    private Member[] mobileList;//手机联系人
    private Member[] cardList;//名片联系人
    private Member[] renheMemberList;//手机通讯录和名片中非好友的Renhe会员
    private int maxMobileId;
    private int maxCardId;

    public static class Member implements Serializable {

        private static final long serialVersionUID = 8067421929445489899L;
        private String sid;//公用字段，手机，card
        private String name;//公用字段，手机，card
        private String title;
        private String company;
        private String userface;
        private int accountType;// 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
        private boolean isRealname;// 是否是实名认证的会员
        private boolean blockedContact;//是否已拉黑该会员
        private boolean beBlocked;// 是否已被该会员拉黑
        private boolean imValid;// boolean 是否有有效的IM账号
        private int imId;// int 有效的IM账号id
        private String mobile;//公用字段，手机
        private String tel;

        private String shortName;//手机，card简短的名字，用于头像显示
        private int colorIndex;//手机，card:头像背景使用的颜色索引，0~11
        private String cover;//背景图片

        private int cardId;
        private String vcardContent;//vCard格式名片数据。。。8.8过时
        private MobileDetail[] detail;

        private int profileSourceType;//区分存放的类型

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public void setColorIndex(int colorIndex) {
            this.colorIndex = colorIndex;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

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

        public String getUserface() {
            return userface;
        }

        public void setUserface(String userface) {
            this.userface = userface;
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

        public boolean isBlockedContact() {
            return blockedContact;
        }

        public void setBlockedContact(boolean blockedContact) {
            this.blockedContact = blockedContact;
        }

        public boolean isBeBlocked() {
            return beBlocked;
        }

        public void setBeBlocked(boolean beBlocked) {
            this.beBlocked = beBlocked;
        }

        public boolean isImValid() {
            return imValid;
        }

        public void setImValid(boolean imValid) {
            this.imValid = imValid;
        }

        public int getImId() {
            return imId;
        }

        public void setImId(int imId) {
            this.imId = imId;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public int getCardId() {
            return cardId;
        }

        public void setCardId(int cardId) {
            this.cardId = cardId;
        }

        public String getVcardContent() {
            return vcardContent;
        }

        public void setVcardContent(String vcardContent) {
            this.vcardContent = vcardContent;
        }

        public int getProfileSourceType() {
            return profileSourceType;
        }

        public void setProfileSourceType(int profileSourceType) {
            this.profileSourceType = profileSourceType;
        }

        public MobileDetail[] getDetail() {
            return detail;
        }

        public void setDetail(MobileDetail[] detail) {
            this.detail = detail;
        }

        /**
         * 手机通讯录更多
         *
         * @return
         */
        public class MobileDetail {

            private String contentType;//1 email 2 手机
            private String subject;
            private String content;

            public MobileDetail() {
            }

            public String getContentType() {
                return contentType;
            }

            public void setContentType(String contentType) {
                this.contentType = contentType;
            }

            public String getSubject() {
                return subject;
            }

            public void setSubject(String subject) {
                this.subject = subject;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        @Override
        public String toString() {
            return "Member [sid=" + sid + ", name=" + name + ", title=" + title + ", company=" + company + ", userface="
                    + userface + "]";
        }

    }

    public static class MemberId implements Serializable {
        private static final long serialVersionUID = 1L;
        private String memberSId;

        public String getMemberSId() {
            return memberSId;
        }

        public void setMemberSId(String memberSId) {
            this.memberSId = memberSId;
        }

    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Member[] getUpdateMemberList() {
        return updateMemberList;
    }

    public void setUpdateMemberList(Member[] updateMemberList) {
        this.updateMemberList = updateMemberList;
    }

    public MemberId[] getDeleteMemberIdList() {
        return deleteMemberIdList;
    }

    public void setDeleteMemberIdList(MemberId[] deleteMemberIdList) {
        this.deleteMemberIdList = deleteMemberIdList;
    }

    public Member[] getMemberList() {
        return memberList;
    }

    public void setMemberList(Member[] memberList) {
        this.memberList = memberList;
    }

    public int getMaxCid() {
        return maxCid;
    }

    public void setMaxCid(int maxCid) {
        this.maxCid = maxCid;
    }

    public long getMaxLastUpdatedDate() {
        return maxLastUpdatedDate;
    }

    public void setMaxLastUpdatedDate(long maxLastUpdatedDate) {
        this.maxLastUpdatedDate = maxLastUpdatedDate;
    }

    public Member[] getMobileList() {
        return mobileList;
    }

    public void setMobileList(Member[] mobileList) {
        this.mobileList = mobileList;
    }

    public Member[] getCardList() {
        return cardList;
    }

    public void setCardList(Member[] cardList) {
        this.cardList = cardList;
    }

    public Member[] getRenheMemberList() {
        return renheMemberList;
    }

    public void setRenheMemberList(Member[] renheMemberList) {
        this.renheMemberList = renheMemberList;
    }

    public int getMaxMobileId() {
        return maxMobileId;
    }

    public void setMaxMobileId(int maxMobileId) {
        this.maxMobileId = maxMobileId;
    }

    public int getMaxCardId() {
        return maxCardId;
    }

    public void setMaxCardId(int maxCardId) {
        this.maxCardId = maxCardId;
    }

    @Override
    public String toString() {
        return "Contact [state=" + state + ", memberList=" + Arrays.toString(memberList) + "]";
    }

}

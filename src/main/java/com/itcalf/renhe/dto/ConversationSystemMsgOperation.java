package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: ConversationSystemMsgOperation.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-10 下午2:36:35 <br>
 *
 * @author wangning
 */
public class ConversationSystemMsgOperation implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int state;// int 请求是否成功结果 说明：1 请求成功
    private SystemMessage[] messageList;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public SystemMessage[] getMessageList() {
        return messageList;
    }

    public void setMessageList(SystemMessage[] messageList) {
        this.messageList = messageList;
    }

    public static class SystemMessage implements Serializable {
        public static final int SYSTEM_MSG_SENDING = 0;
        public static final int SYSTEM_MSG_OFFLINE = -1;
        public static final int SYSTEM_MSG_SENDSUCCESS = 1;
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int id;// int 说明：系统消息id
        private String content;// String 说明：系统消息内容
        private long createdDate;// long 说明：系统消息发送时间
        private int bizType;// 若bizType为0，则不显示消息体链接；  若bizType为1，则表示是 刚刚注册和聊时，提醒用户完善资料的消息。跳转到个人资料页面
        private Extdata extdata;// String 说明：系统消息附带消息，大部分为空，需要时才传递
        private String linkTitle;// String 说明：系统消息显示链接文案的标题
        private int status;
        private boolean showImage;// boolean 说明：是否需要显示图片
        private String imageUrl;// String 说明：图片的URL

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getSubject() {
            return content;
        }

        public void setSubject(String subject) {
            this.content = subject;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public int getBizType() {
            return bizType;
        }

        public void setBizType(int bizType) {
            this.bizType = bizType;
        }

        public Extdata getExtdata() {
            return extdata;
        }

        public void setExtdata(Extdata extdata) {
            this.extdata = extdata;
        }

        public String getLinkTitle() {
            return linkTitle;
        }

        public void setLinkTitle(String linkTitle) {
            this.linkTitle = linkTitle;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public boolean isShowImage() {
            return showImage;
        }

        public void setShowImage(boolean showImage) {
            this.showImage = showImage;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class Extdata implements Serializable {
        private static final long serialVersionUID = 1L;
        private String imConversationId;//biztype = 2
        private String amount_name;//: "退还金额",
        private String amount;//: 5.92,
        private String create_date;//
        private Detail[] detail;
        private String url;//biztype = 13
        public String getImConversationId() {
            return imConversationId;
        }

        public void setImConversationId(String imConversationId) {
            this.imConversationId = imConversationId;
        }

        public String getAmount_name() {
            return amount_name;
        }

        public void setAmount_name(String amount_name) {
            this.amount_name = amount_name;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCreate_date() {
            return create_date;
        }

        public void setCreate_date(String create_date) {
            this.create_date = create_date;
        }

        public Detail[] getDetail() {
            return detail;
        }

        public void setDetail(Detail[] detail) {
            this.detail = detail;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Detail implements Serializable {
        String label;//: "退款方式",
        String content;//

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

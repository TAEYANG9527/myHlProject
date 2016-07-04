package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author chan
 * @createtime 2015-2-3
 * @功能说明 获取导入的手机通讯录联系人
 */
public class ContactsReturn implements Serializable {
    private static final long serialVersionUID = 1L;
    private String alpha;//名字首字母
    private int state;
    private int maxId;//最大id
    private int count;
    private List<ContactResult> contactResult;

    public String getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getMaxId() {
        return maxId;
    }

    public void setMaxId(int maxId) {
        this.maxId = maxId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ContactResult> getContactResult() {
        return contactResult;
    }

    public void setContactResult(List<ContactResult> contactResult) {
        this.contactResult = contactResult;
    }

    /**
     * 通讯录
     **/
    public static class ContactResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String shortName;
        private int colorIndex;
        private String contactId;
        private boolean isRenheMember;
        private RenheMemberInfo renheMemberInfo;
        private String[] emailItems;
        private String[] mobileItems;
        private String[] telephoneItems;
        private String[] telephoneOtherItems;
        private String[] addressItems;
        private String[] otherItems;

        private int selectState = 0;//0:表示未进行选择，1：已选中；2：未选中

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactId() {
            return contactId;
        }

        public void setContactId(String contactId) {
            this.contactId = contactId;
        }

        public boolean isRenheMember() {
            return isRenheMember;
        }

        public void setRenheMember(boolean isRenheMember) {
            this.isRenheMember = isRenheMember;
        }

        public RenheMemberInfo getRenheMemberInfo() {
            return renheMemberInfo;
        }

        public void setRenheMemberInfo(RenheMemberInfo renheMemberInfo) {
            this.renheMemberInfo = renheMemberInfo;
        }

        public String[] getEmailItems() {
            return emailItems;
        }

        public void setEmailItems(String[] emailItems) {
            this.emailItems = emailItems;
        }

        public String[] getMobileItems() {
            return mobileItems;
        }

        public void setMobileItems(String[] mobileItems) {
            this.mobileItems = mobileItems;
        }

        public String[] getTelephoneItems() {
            return telephoneItems;
        }

        public void setTelephoneItems(String[] telephoneItems) {
            this.telephoneItems = telephoneItems;
        }

        public String[] getTelephoneOtherItems() {
            return telephoneOtherItems;
        }

        public void setTelephoneOtherItems(String[] telephoneOtherItems) {
            this.telephoneOtherItems = telephoneOtherItems;
        }

        public String[] getAddressItems() {
            return addressItems;
        }

        public void setAddressItems(String[] addressItems) {
            this.addressItems = addressItems;
        }

        public String[] getOtherItems() {
            return otherItems;
        }

        public void setOtherItems(String[] otherItems) {
            this.otherItems = otherItems;
        }

        public int getSelectState() {
            return selectState;
        }

        public void setSelectState(int selectState) {
            this.selectState = selectState;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public void setColorIndex(int colorIndex) {
            this.colorIndex = colorIndex;
        }
    }
}

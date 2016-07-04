package com.itcalf.renhe.eventbusbean;

/**
 * 用于通知人脉列表删除或新增item
 * Created by wangning on 2015/12/30.
 */
public class ContactDeleteOrAndEvent {
    public static final int CONTACT_EVENT_TYPE_ADD = 1;
    public static final int CONTACT_EVENT_TYPE_DELETE = 2;
    private int type;//1:新增 2：删除
    private String sid;//只有type == 2 时才有用，待删除的sid
    public ContactDeleteOrAndEvent() {
    }

    public ContactDeleteOrAndEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}

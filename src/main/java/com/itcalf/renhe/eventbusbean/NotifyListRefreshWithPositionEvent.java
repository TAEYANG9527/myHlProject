package com.itcalf.renhe.eventbusbean;

/**
 * 用于通知人脉圈fragment的RecyclerView滚动到顶部
 * Created by wangning on 2015/12/30.
 */
public class NotifyListRefreshWithPositionEvent {
    public static final int RENMAI_SEARCH = 1;
    public static final int RENMAI_SEARCH_MORE = 2;
    public static final int ADVANCE_SEARCH = 3;

    public static final int MODE_ADD = 1;
    public static final int MODE_ACCEPT = 2;
    private int type;//1:人脉搜索页面 2：人脉搜索的更多页面 3:高级搜索结果页面
    private int position;
    private int mode;//1:添加好友 2：接收好友

    public NotifyListRefreshWithPositionEvent() {
    }

    public NotifyListRefreshWithPositionEvent(int type, int position, int mode) {
        this.type = type;
        this.position = position;
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}

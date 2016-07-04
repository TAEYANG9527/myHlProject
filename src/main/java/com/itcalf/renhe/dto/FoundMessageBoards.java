package com.itcalf.renhe.dto;

import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.UpdatedNoticeList;

import java.io.Serializable;

/**
 * 
 * 1,查看自己客厅留言接口(自己发布的客厅留言及自己关注的客厅留言) 2,查看朋友客厅的留言接口 3,查看同城的留言接口 4,查看同行的留言接口
 * 5,最受关注的留言接口
 */
public class FoundMessageBoards implements Serializable {

	private static final long serialVersionUID = -6501243574466513990L;
	private int state; // 请求是否成功结果 说明：1 请求成功；-3：type必须为renew、new、more；-4：type类型为new，而maxCreatedDate和maxLastUpdaatedDate未正确设置值；-5：type类型为more，而minCreatedDate和minLastUpdaatedDate未指定
	private NewNoticeList[] newNoticeList; // 新增的人脉圈列表
	private UpdatedNoticeList[] updatedNoticeList; //更新的人脉圈列表
	private long maxLastUpdatedDate; //long 说明：最大的修改时间，type为renew和new时才会返回
	private int cacheTime;//说明：上次请求服务器端时间，用于判断是否需要加载数据

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

	public long getMaxLastUpdatedDate() {
		return maxLastUpdatedDate;
	}

	public void setMaxLastUpdatedDate(long maxLastUpdatedDate) {
		this.maxLastUpdatedDate = maxLastUpdatedDate;
	}

	public int getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
	}

}

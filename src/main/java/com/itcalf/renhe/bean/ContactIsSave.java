package com.itcalf.renhe.bean;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/7/28
 */
public class ContactIsSave {

	private String sid;
	private int maxCid;
	private int maxMobileId;
	private int maxCardId;
	private long maxLastUpdatedDate;

	public ContactIsSave() {

	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public int getMaxCid() {
		return maxCid;
	}

	public void setMaxCid(int maxCid) {
		this.maxCid = maxCid;
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

	public long getMaxLastUpdatedDate() {
		return maxLastUpdatedDate;
	}

	public void setMaxLastUpdatedDate(long maxLastUpdatedDate) {
		this.maxLastUpdatedDate = maxLastUpdatedDate;
	}
}

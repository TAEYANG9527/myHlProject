package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

public class SearchRecommendedList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state;
	private String[] hotSearchList;
	private List<SearchRecommendedBean> memberList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String[] getHotSearchList() {
		return hotSearchList;
	}

	public void setHotSearchList(String[] hotSearchList) {
		this.hotSearchList = hotSearchList;
	}

	public List<SearchRecommendedBean> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<SearchRecommendedBean> memberList) {
		this.memberList = memberList;
	}

}

package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author       chan
 * @createtime   2015-1-27
 * @功能说明	圈子搜索列表的结果模型
 */
public class SearchCircleBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state;
	private List<SearchCircleInfo> circleList;

	public SearchCircleBean() {

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public List<SearchCircleInfo> getCircleList() {
		return circleList;
	}

	public void setCircleList(List<SearchCircleInfo> circleList) {
		this.circleList = circleList;
	}

}

package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class CircleJoinINfo implements Serializable {
	private static final long serialVersionUID = 23452344231L;

	private int state;

	private ArrayList<CircleJoinRequestListInfo> circleJoinRequestList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public ArrayList<CircleJoinRequestListInfo> getCircleJoinRequestList() {
		return circleJoinRequestList;
	}

	public void setCircleJoinRequestList(ArrayList<CircleJoinRequestListInfo> circleJoinRequestList) {
		this.circleJoinRequestList = circleJoinRequestList;
	}

}

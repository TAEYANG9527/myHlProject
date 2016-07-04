package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

public class OcrCardListBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private int state;
	private String[] cardList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String[] getCardList() {
		return cardList;
	}

	public void setCardList(String[] cardList) {
		this.cardList = cardList;
	}
}

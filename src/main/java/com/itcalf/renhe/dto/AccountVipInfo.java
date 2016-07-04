package com.itcalf.renhe.dto;

import java.util.List;

/**
 * @author YZQ
 */
public class AccountVipInfo {

	private int accountType;
	private String endDate;
	private Price vip;
	private Price golden;
	private Price platinum;

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Price getVip() {
		return vip;
	}

	public void setVip(Price vip) {
		this.vip = vip;
	}

	public Price getGolden() {
		return golden;
	}

	public void setGolden(Price golden) {
		this.golden = golden;
	}

	public Price getPlatinum() {
		return platinum;
	}

	public void setPlatinum(Price platinum) {
		this.platinum = platinum;
	}

	public class Price {
		public double price;
		public double fee;
		public List<String> privilege;
	}

}

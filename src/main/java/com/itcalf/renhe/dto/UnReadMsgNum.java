package com.itcalf.renhe.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Title: UnReadMsgNum.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-5-21 下午2:12:28 <br>
 * @author wangning
 */
public class UnReadMsgNum {
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private int count;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("state", state).append("count ", count).toString();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getNum() {
		return count;
	}

	public void setNum(int num) {
		this.count = num;
	}

}

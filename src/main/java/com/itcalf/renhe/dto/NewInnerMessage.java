package com.itcalf.renhe.dto;

import java.io.Serializable;

public class NewInnerMessage implements Serializable {
	private static final long serialVersionUID = 149883934230611700L;
	private int state;
	private int count;

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state).append("count", count)
				.toString();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}

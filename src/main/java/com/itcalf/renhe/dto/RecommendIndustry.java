package com.itcalf.renhe.dto;

/**
 * Title: RecommendIndustry.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-10-24 下午2:46:01 <br>
 * @author wangning
 */
public class RecommendIndustry {
	private int id;
	private String name;
	private int type;
	private int bcgRe;
	private boolean checked = false;

	public RecommendIndustry(int id, String name, int type, int bcgRe) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.bcgRe = bcgRe;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public int getBcgRe() {
		return bcgRe;
	}

	public void setBcgRe(int bcgRe) {
		this.bcgRe = bcgRe;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

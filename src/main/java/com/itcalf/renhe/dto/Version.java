package com.itcalf.renhe.dto;

import java.io.Serializable;

public class Version implements Serializable {

	private static final long serialVersionUID = -1898340399407988775L;
	private int state;
	private String version;
	private String newVersionDownloadUrl;

	private String updatelog;
	private int forceUpdate;

	public String getUpdatelog() {
		return updatelog;
	}

	public void setUpdatelog(String updatelog) {
		this.updatelog = updatelog;
	}

	public int getForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(int forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getNewVersionDownloadUrl() {
		return newVersionDownloadUrl;
	}

	public void setNewVersionDownloadUrl(String newVersionDownloadUrl) {
		this.newVersionDownloadUrl = newVersionDownloadUrl;
	}

	@Override
	public String toString() {
		return "Version [state=" + state + ", version=" + version + ", newVersionDownloadUrl=" + newVersionDownloadUrl + "]";
	}

}

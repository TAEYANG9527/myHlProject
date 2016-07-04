package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: TouTiao.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-5 上午11:50:58 <br>
 * @author wangning
 */
public class TouTiaoOperation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int state;
	private int count;
	private long maxUpdatedDate;// int 最新的访问时间,用于下次获取新的行业头条 type为new时才有
	private boolean clearCache;// boolean 是否需要清空客户端缓存 true清空 type为new时才有
	private long minUpdatedDate;// 当type为new时，clearCache为true时；当type为more才记录该值

	private TouTiaoList[] messageList;//

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

	public long getMaxUpdatedDate() {
		return maxUpdatedDate;
	}

	public void setMaxUpdatedDate(int maxUpdatedDate) {
		this.maxUpdatedDate = maxUpdatedDate;
	}

	public boolean isClearCache() {
		return clearCache;
	}

	public void setClearCache(boolean clearCache) {
		this.clearCache = clearCache;
	}

	public long getMinUpdatedDate() {
		return minUpdatedDate;
	}

	public void setMinUpdatedDate(int minUpdatedDate) {
		this.minUpdatedDate = minUpdatedDate;
	}

	public TouTiaoList[] getMessageList() {
		return messageList;
	}

	public void setMessageList(TouTiaoList[] messageList) {
		this.messageList = messageList;
	}

	public static class TouTiaoList {
		private int id;// int ：消息体id
		private long createdDate;// int ：消息体发布时间
		private TouTiao[] toutiaoList;//

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public long getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(long createdDate) {
			this.createdDate = createdDate;
		}

		public TouTiao[] getToutiaoList() {
			return toutiaoList;
		}

		public void setToutiaoList(TouTiao[] toutiaoList) {
			this.toutiaoList = toutiaoList;
		}

	}

	public static class TouTiao implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;// int ：消息id
		private int messageId;// int ：外层消息体的Id
		private String title;// String：消息内容
		private String source;// String：消息来源
		private String image;// String：图片URL
		private String url;// String： 头条详情URL
		private long createdDate;// int ： 消息推荐时间
		private int orders;// int ：消息排序 倒序排列(一般是0或1，首条消息为1)}]}]

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getMessageId() {
			return messageId;
		}

		public void setMessageId(int messageId) {
			this.messageId = messageId;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public long getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(long createdDate) {
			this.createdDate = createdDate;
		}

		public int getOrders() {
			return orders;
		}

		public void setOrders(int orders) {
			this.orders = orders;
		}

	}
}

package com.itcalf.renhe.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.room.BlockMessageboardMemberTask;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.context.room.ViewPhotoActivity;
import com.itcalf.renhe.context.room.WebViewActivityForReport;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.dto.MessageBoards.ContentInfo;
import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.TransferUrl2Drawable;
import com.itcalf.renhe.view.TextViewFixTouchConsume;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Feature:扩展SimpleAdapter的BaseAdapter
 * Description:增加了图片缓存加载，控制特殊视图的显示
 * 
 * @author xp
 * 
 */

public class PersonalNWeiboAdapter extends BaseAdapter {
	private LayoutInflater flater;
	private String email;
	private Context ct;
	private ListView mListView;
	Map<String, Object> map;
	// 留言显示数据
	private List<Map<String, Object>> mWeiboList = new ArrayList<Map<String, Object>>();
	SharedPreferences userInfo;
	Editor editor;
	private TransferUrl2Drawable transferUrl;
	private AlertDialog mAlertDialog;
	private int type = 1;

	private DateUtil dateUtil;
	private int PIC_MAX_WIDTH;
	private int DEFAULT_IMAGE;
	private float density;
	private static final int LOW_DENSITY_MAX_WIDTH = 200;//针对低分辨率（密度<=1.5）在显示更新了头像时，头像过大，将尺寸特殊处理为200*200
	private static final int HIGH_DENSITY_MAX_WIDTH = 500;//

	public PersonalNWeiboAdapter(Context context, List<? extends Map<String, ?>> data, String email, ListView listView, int type,
			int renmaiquanType, int picMaxWidth, float density) {
		this.flater = LayoutInflater.from(context);
		this.mWeiboList = (List<Map<String, Object>>) data;
		this.email = email;
		this.ct = context;
		this.mListView = listView;
		userInfo = ct.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
		editor = userInfo.edit();
		transferUrl = new TransferUrl2Drawable(ct);
		this.type = type;
		this.dateUtil = new DateUtil();
		this.PIC_MAX_WIDTH = picMaxWidth;
		DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
		this.density = density;
	}

	@Override
	public int getCount() {
		return mWeiboList.size() != 0 ? mWeiboList.size() : 0;
	}

	@Override
	public Object getItem(int arg0) {
		return mWeiboList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder viewHolder = null;

		if (convertView == null) {
			convertView = flater.inflate(R.layout.personal_weibo_item_list, null);
			viewHolder = new ViewHolder();
			viewHolder.contentTv = (TextViewFixTouchConsume) convertView.findViewById(R.id.content_txt);
			viewHolder.rawContentTv = (TextViewFixTouchConsume) convertView.findViewById(R.id.rawcontent_txt);
			viewHolder.dateTv = (TextView) convertView.findViewById(R.id.datetime_txt);
			viewHolder.thumbnailPic = (ImageView) convertView.findViewById(R.id.thumbnailPic);
			viewHolder.forwardThumbnailPic = (ImageView) convertView.findViewById(R.id.forwardThumbnailPic);
			viewHolder.rawcontentlayout = (LinearLayout) convertView.findViewById(R.id.rawcontentlayout);
			viewHolder.thumbnailFl = (FrameLayout) convertView.findViewById(R.id.thumbnailFl);
			viewHolder.forwardThumbnailFl = (FrameLayout) convertView.findViewById(R.id.forwardThumbnailFl);
			viewHolder.thumbnailGridView = (GridView) convertView.findViewById(R.id.thumbnailGridview);
			viewHolder.forwardThumbnailGridView = (GridView) convertView.findViewById(R.id.forwardThumbnailGridview);
			viewHolder.rootLl = (LinearLayout) convertView.findViewById(R.id.rootLl);
			viewHolder.blankView = (View) convertView.findViewById(R.id.blank_view);
			viewHolder.seperateLineLl = (LinearLayout) convertView.findViewById(R.id.seperateLl);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		map = mWeiboList.get(position);
		if (null != map) {
			ImageLoader imageLoader = ImageLoader.getInstance();
			int type = MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD;
			String userSid = "";
			long datetime = 0;
			boolean isNeedShowTime = true;
			NewNoticeList result = null;
			if (null != map.get("result")) {
				result = (NewNoticeList) map.get("result");
			}
			if (null != map.get("type")) {
				type = (Integer) map.get("type");
			}
			if (null != map.get("sid")) {
				userSid = (String) map.get("sid");
			}
			if (null != map.get("createDate")) {
				datetime = (Long) map.get("createDate");
			}
			if (null != map.get("isNeedShowTime")) {
				isNeedShowTime = (Boolean) map.get("isNeedShowTime");
			} else {
				isNeedShowTime = false;
			}
			if (datetime > 0) {
				if (!isNeedShowTime) {
					viewHolder.dateTv.setVisibility(View.INVISIBLE);
					viewHolder.blankView.setVisibility(View.GONE);
					viewHolder.seperateLineLl.setVisibility(View.VISIBLE);
				} else {
					viewHolder.dateTv.setVisibility(View.VISIBLE);
					viewHolder.blankView.setVisibility(View.VISIBLE);
					viewHolder.seperateLineLl.setVisibility(View.GONE);
					DateFormat format1 = new SimpleDateFormat("ddM月");
					Date date = new Date(datetime);
					String datesString = format1.format(date);
					StringBuffer datebuffer = new StringBuffer();
					datebuffer.append(datesString.substring(0, 2));
					switch (date.getMonth()) {
					case 0:
						datebuffer.append("一月");
						break;
					case 1:
						datebuffer.append("二月");
						break;
					case 2:
						datebuffer.append("三月");
						break;
					case 3:
						datebuffer.append("四月");
						break;
					case 4:
						datebuffer.append("五月");
						break;
					case 5:
						datebuffer.append("六月");
						break;
					case 6:
						datebuffer.append("七月");
						break;
					case 7:
						datebuffer.append("八月");
						break;
					case 8:
						datebuffer.append("九月");
						break;
					case 9:
						datebuffer.append("十月");
						break;
					case 10:
						datebuffer.append("十一月");
						break;
					case 11:
						datebuffer.append("十二月");
						break;
					default:
						break;
					}
					viewHolder.dateTv.setText(datebuffer.toString());
					Spannable span = new SpannableString(viewHolder.dateTv.getText());
					span.setSpan(new AbsoluteSizeSpan((int) ct.getResources().getDimension(R.dimen.personal_msg_date_textsize1)),
							0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					span.setSpan(new AbsoluteSizeSpan((int) ct.getResources().getDimension(R.dimen.personal_msg_date_textsize2)),
							2, viewHolder.dateTv.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					viewHolder.dateTv.setText(span);
				}
			} else {
				viewHolder.dateTv.setText("");
			}
			String objectId = "";
			if (null != map.get("objectId")) {
				objectId = (String) map.get("objectId");
			}
			String content = "";
			if (null != map.get("content")) {
				content = (String) map.get("content");
			}
			final int pos = position;
			if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD || type == MessageBoards.MESSAGE_TYPE_ADD_NEWMSG
					|| type == MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE) {
				if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD || type == MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {
					viewHolder.rootLl.setBackgroundResource(R.drawable.room_list_item_bacg);
				} else {
					viewHolder.rootLl.setBackgroundResource(R.color.white);
				}
				viewHolder.thumbnailPic.setClickable(true);
				PicList[] picList = null;
				Object messageBoardMembersObject = map.get("atMembers");
				if (!TextUtils.isEmpty(content)) {
					SpannableString span;
					if (null != messageBoardMembersObject) {
						AtMemmber[] messageBoardMembers = (AtMemmber[]) messageBoardMembersObject;
						if (messageBoardMembers.length > 0) {
							span = getSpannableString(content, messageBoardMembers);
						} else {
							span = getNoAtSpannedString(null, content);
						}
					} else {
						span = getNoAtSpannedString(null, content);
					}
					//				SpannableStringBuilder style = transferUrl(content);
					transferUrl.transferUrl(span);
					viewHolder.contentTv.setText(span);
					viewHolder.contentTv.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());
				} else {
					viewHolder.contentTv.setText("");
				}

				boolean isForwardRenhe = false;
				String forward_objectId = "";
				int Id = -1;
				String forward_name = "";
				String forward_sid = "";
				String forward_content = "";
				PicList[] forwardPicList = null;
				if (null != map.get("ForwardMessageBoardInfo_isForwardRenhe")) {
					isForwardRenhe = (Boolean) map.get("ForwardMessageBoardInfo_isForwardRenhe");
				}
				if (null != map.get("ForwardMessageBoardInfo_ObjectId")) {
					forward_objectId = (String) map.get("ForwardMessageBoardInfo_ObjectId");
				}
				if (null != map.get("ForwardMessageBoardInfo_Id")) {
					Id = (Integer) map.get("ForwardMessageBoardInfo_Id");
				}
				if (null != map.get("ForwardMessageBoardInfo_Name")) {
					forward_name = (String) map.get("ForwardMessageBoardInfo_Name");
				}
				if (null != map.get("ForwardMessageBoardInfo_Sid")) {
					forward_sid = (String) map.get("ForwardMessageBoardInfo_Sid");
				}
				if (null != map.get("ForwardMessageBoardInfo_Content")) {
					forward_content = (String) map.get("ForwardMessageBoardInfo_Content");
				}
				if (null != map.get("picList")) {
					picList = (PicList[]) map.get("picList");
				}
				AtMemmber[] forwardMessageBoardMembers = null;
				if (!TextUtils.isEmpty(forward_content)) {
					viewHolder.rawcontentlayout.setVisibility(View.VISIBLE);
					if (null != map.get("ForwardMessageBoardInfo_PicList")) {
						forwardPicList = (PicList[]) map.get("ForwardMessageBoardInfo_PicList");
					}
					Object forwardMessageBoardMembersObject = map.get("ForwardMessageBoardInfo_AtMembers");
					SpannableString span;
					if (null != forwardMessageBoardMembersObject) {
						forwardMessageBoardMembers = (AtMemmber[]) forwardMessageBoardMembersObject;
						if (forwardMessageBoardMembers.length > 0) {
							span = getSpannableString(forward_content, forwardMessageBoardMembers);
						} else {
							span = getNoAtSpannedString(null, forward_content);
						}
					} else {
						span = getNoAtSpannedString(null, forward_content);
					}
					transferUrl.transferUrl(span);
					viewHolder.rawContentTv.setText(span);
					viewHolder.rawContentTv.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());

					final boolean isForwardRenheFinal = isForwardRenhe;
					final String forwardObjectIdFinal = forward_objectId;
					final String nameFinal = forward_name;
					final String sIdFinal = forward_sid;
					final String contentFinal = forward_content;
					final int IdFinal = Id;
					final PicList[] piclistFinal = forwardPicList;
					final AtMemmber[] atMemberFinal = forwardMessageBoardMembers;
					viewHolder.rawcontentlayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if (isForwardRenheFinal && !TextUtils.isEmpty(forwardObjectIdFinal)) {
								Bundle bundle = new Bundle();
								NewNoticeList forwardNewNoticeListItem = new NewNoticeList();
								ContentInfo contentInfo = new ContentInfo();
								contentInfo.setId(IdFinal);
								contentInfo.setObjectId(forwardObjectIdFinal);
								contentInfo.setContent(contentFinal);
								contentInfo.setAtMembers(atMemberFinal);
								contentInfo.setPicList(piclistFinal);
								forwardNewNoticeListItem.setContentInfo(contentInfo);
								bundle.putSerializable("forwardNewNoticeListItem", forwardNewNoticeListItem);
								bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_FORWARD);
								Intent intent = new Intent(ct, TwitterShowMessageBoardActivity.class);
								intent.putExtras(bundle);
								((Activity) ct).startActivity(intent);
								((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
							}
						}
					});
					if (null != forwardPicList && userInfo.getBoolean("roomshowpic", true)) {
						if (forwardPicList.length > 1) {
							viewHolder.forwardThumbnailFl.setVisibility(View.VISIBLE);
							viewHolder.forwardThumbnailGridView.setVisibility(View.VISIBLE);
							viewHolder.forwardThumbnailPic.setVisibility(View.GONE);
							RoomRemotePicGridAdapter adapter = new RoomRemotePicGridAdapter(ct, forwardPicList);
							viewHolder.forwardThumbnailGridView.setAdapter(adapter);
							final CharSequence[] middlePics = new CharSequence[forwardPicList.length];
							for (int i = 0; i < forwardPicList.length; i++) {
								middlePics[i] = forwardPicList[i].getBmiddlePicUrl();
							}
							if (null != middlePics && middlePics.length > 0) {
								viewHolder.forwardThumbnailGridView.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
										Intent intent = new Intent(ct, ViewPhotoActivity.class);
										intent.putExtra("ID", arg2);
										intent.putExtra("middlePics", middlePics);
										((Activity) ct).startActivity(intent);
										((Activity) ct).overridePendingTransition(R.anim.zoom_enter, 0);

									}
								});
							}
						} else if (forwardPicList.length == 1) {
							viewHolder.forwardThumbnailFl.setVisibility(View.VISIBLE);
							viewHolder.forwardThumbnailGridView.setVisibility(View.GONE);
							viewHolder.forwardThumbnailPic.setVisibility(View.VISIBLE);

							String thumbnailPicUrl = forwardPicList[0].getThumbnailPicUrl();
							if (thumbnailPicUrl.indexOf("/livingroom/images/") > -1
									|| thumbnailPicUrl.indexOf("/events/logos/new/") > -1
									|| thumbnailPicUrl.indexOf("/news_images/") > -1) {
								LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
								linearParams.height = DensityUtil.dip2px(ct,
										ct.getResources().getDimension(R.dimen.addmsg_grid_item_width));
								linearParams.width = linearParams.height;
								viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
								try {
									if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {

										DisplayImageOptions options = new DisplayImageOptions.Builder()
												.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
												.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
												.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
												.build();
										ImageLoader imageLoader2 = ImageLoader.getInstance();
										try {
											imageLoader2.displayImage(forwardPicList[0].getThumbnailPicUrl(),
													viewHolder.forwardThumbnailPic, options, new AnimateFirstDisplayListener());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
										viewHolder.forwardThumbnailPic
												.setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								//								try {
								//									if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {
								//										AsyncImageLoader
								//												.getInstance()
								//												.populateData(ct, email, false, true, false)
								//												.loadPic(viewHolder.forwardThumbnailPic, null,
								//														forwardPicList[0].getThumbnailPicUrl(), null, null,
								//														MatrixUtil.getPostMatrix((Activity) ct), true);
								//									}
								//									if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
								//										viewHolder.forwardThumbnailPic.setOnClickListener(new clickPic(forwardPicList[0]
								//												.getBmiddlePicUrl()));
								//									}
								//								} catch (Exception e) {
								//									e.printStackTrace();
								//								}
							} else {

								int width = forwardPicList[0].getBmiddlePicWidth();
								int height = forwardPicList[0].getBmiddlePicHeight();
								if (width > 0 && height > 0) {
									float ratio = ((float) width / (float) height);
									if (width <= PIC_MAX_WIDTH && height <= PIC_MAX_WIDTH) {
									} else if (width <= PIC_MAX_WIDTH && height > PIC_MAX_WIDTH) {
										height = (int) (PIC_MAX_WIDTH);
										width = (int) (height * ratio);
										//									viewHolder.thumbnailPic.setLayoutParams(linearParams);
									} else if (width > PIC_MAX_WIDTH && height <= PIC_MAX_WIDTH) {
										width = (int) (PIC_MAX_WIDTH);
										height = (int) (width / ratio);
									} else {
										height = (int) (PIC_MAX_WIDTH);
										width = (int) (height * ratio);
									}
									if (width != 0 && height != 0) {
										LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
										linearParams.height = height;
										linearParams.width = width;
										viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
									} else {
										LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
										linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
										linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
										viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
									}

									try {
										if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {

											DisplayImageOptions options = new DisplayImageOptions.Builder()
													.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
													.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
													.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
													.build();
											ImageLoader imageLoader2 = ImageLoader.getInstance();
											try {
												imageLoader2.displayImage(forwardPicList[0].getThumbnailPicUrl(),
														viewHolder.forwardThumbnailPic, options,
														new AnimateFirstDisplayListener());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
											viewHolder.forwardThumbnailPic
													.setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
									linearParams.height = DensityUtil.dip2px(ct,
											ct.getResources().getDimension(R.dimen.addmsg_grid_item_width));
									linearParams.width = linearParams.height;
									viewHolder.forwardThumbnailPic.setLayoutParams(linearParams);
									try {
										if (!TextUtils.isEmpty(forwardPicList[0].getThumbnailPicUrl())) {

											DisplayImageOptions options = new DisplayImageOptions.Builder()
													.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
													.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
													.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
													.build();
											ImageLoader imageLoader2 = ImageLoader.getInstance();
											try {
												imageLoader2.displayImage(forwardPicList[0].getThumbnailPicUrl(),
														viewHolder.forwardThumbnailPic, options,
														new AnimateFirstDisplayListener());
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										if (!TextUtils.isEmpty(forwardPicList[0].getBmiddlePicUrl())) {
											viewHolder.forwardThumbnailPic
													.setOnClickListener(new clickPic(forwardPicList[0].getBmiddlePicUrl()));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						} else {
							viewHolder.forwardThumbnailFl.setVisibility(View.GONE);
						}
					} else {
						viewHolder.forwardThumbnailFl.setVisibility(View.GONE);
					}
				} else {
					viewHolder.rawcontentlayout.setVisibility(View.GONE);
				}
				if (null != picList && userInfo.getBoolean("roomshowpic", true)) {
					if (picList.length > 1) {
						viewHolder.thumbnailFl.setVisibility(View.VISIBLE);
						viewHolder.thumbnailGridView.setVisibility(View.VISIBLE);
						viewHolder.thumbnailPic.setVisibility(View.GONE);
						LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
						linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
						linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
						viewHolder.thumbnailPic.setLayoutParams(linearParams);
						RoomRemotePicGridAdapter adapter = new RoomRemotePicGridAdapter(ct, picList);
						viewHolder.thumbnailGridView.setAdapter(adapter);
						final CharSequence[] middlePics = new CharSequence[picList.length];
						for (int i = 0; i < picList.length; i++) {
							middlePics[i] = picList[i].getBmiddlePicUrl();
						}
						if (null != middlePics && middlePics.length > 0) {
							viewHolder.thumbnailGridView.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
									Intent intent = new Intent(ct, ViewPhotoActivity.class);
									intent.putExtra("ID", arg2);
									intent.putExtra("middlePics", middlePics);
									((Activity) ct).startActivity(intent);
									((Activity) ct).overridePendingTransition(R.anim.zoom_enter, 0);

								}
							});
						}
					} else if (picList.length == 1) {
						viewHolder.thumbnailFl.setVisibility(View.VISIBLE);
						viewHolder.thumbnailGridView.setVisibility(View.GONE);
						viewHolder.thumbnailPic.setVisibility(View.VISIBLE);
						String thumbnailPicUrl = picList[0].getThumbnailPicUrl();
						if (thumbnailPicUrl.indexOf("/livingroom/images/") > -1
								|| thumbnailPicUrl.indexOf("/events/logos/new/") > -1
								|| thumbnailPicUrl.indexOf("/news_images/") > -1) {
							LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
							linearParams.height = DensityUtil.dip2px(ct,
									ct.getResources().getDimension(R.dimen.addmsg_grid_item_width));
							;
							linearParams.width = linearParams.height;
							viewHolder.thumbnailPic.setLayoutParams(linearParams);
							try {
								if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

									DisplayImageOptions options = new DisplayImageOptions.Builder()
											.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
											.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
											.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
									ImageLoader imageLoader1 = ImageLoader.getInstance();
									try {
										imageLoader1.displayImage(picList[0].getThumbnailPicUrl(), viewHolder.thumbnailPic,
												options, new AnimateFirstDisplayListener());
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									viewHolder.thumbnailFl.setVisibility(View.GONE);
									viewHolder.thumbnailGridView.setVisibility(View.GONE);
									viewHolder.thumbnailPic.setVisibility(View.GONE);
								}
								if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
									viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							//								try {
							//									LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
							//									linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
							//									linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
							//									viewHolder.thumbnailPic.setLayoutParams(linearParams);
							//									if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {
							//										AsyncImageLoader
							//												.getInstance()
							//												.populateData(ct, email, false, true, false)
							//												.loadPic(viewHolder.thumbnailPic, null, picList[0].getThumbnailPicUrl(), null,
							//														null, MatrixUtil.getPostMatrix((Activity) ct), true);
							//									} else {
							//										viewHolder.thumbnailFl.setVisibility(View.GONE);
							//										viewHolder.thumbnailGridView.setVisibility(View.GONE);
							//										viewHolder.thumbnailPic.setVisibility(View.GONE);
							//
							//									}
							//									if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
							//										viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
							//									}
							//								} catch (Exception e) {
							//									e.printStackTrace();
							//								}

						} else {

							int width = picList[0].getBmiddlePicWidth();
							int height = picList[0].getBmiddlePicHeight();
							if (width > 0 && height > 0) {
								float ratio = ((float) width / (float) height);
								if (width <= PIC_MAX_WIDTH && height <= PIC_MAX_WIDTH) {
								} else if (width <= PIC_MAX_WIDTH && height > PIC_MAX_WIDTH) {
									height = (int) (PIC_MAX_WIDTH);
									width = (int) (height * ratio);
									//									viewHolder.thumbnailPic.setLayoutParams(linearParams);
								} else if (width > PIC_MAX_WIDTH && height <= PIC_MAX_WIDTH) {
									width = (int) (PIC_MAX_WIDTH);
									height = (int) (width / ratio);
								} else {
									height = (int) (PIC_MAX_WIDTH);
									width = (int) (height * ratio);
								}
								if (width != 0 && height != 0) {
									LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
									linearParams.height = height;
									linearParams.width = width;
									viewHolder.thumbnailPic.setLayoutParams(linearParams);
								} else {
									LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
									linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
									linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
									viewHolder.thumbnailPic.setLayoutParams(linearParams);
								}
								try {
									if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

										DisplayImageOptions options = new DisplayImageOptions.Builder()
												.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
												.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
												.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
												.build();
										ImageLoader imageLoader1 = ImageLoader.getInstance();
										try {
											imageLoader1.displayImage(picList[0].getThumbnailPicUrl(), viewHolder.thumbnailPic,
													options, new AnimateFirstDisplayListener());
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										viewHolder.thumbnailFl.setVisibility(View.GONE);
										viewHolder.thumbnailGridView.setVisibility(View.GONE);
										viewHolder.thumbnailPic.setVisibility(View.GONE);
									}
									if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
										viewHolder.thumbnailPic.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							} else {
								if (type == MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE) {
									if (density < 2) {
										LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
										linearParams.height = LOW_DENSITY_MAX_WIDTH;
										linearParams.width = LOW_DENSITY_MAX_WIDTH;
										viewHolder.thumbnailPic.setLayoutParams(linearParams);
									} else {
										LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
										linearParams.height = HIGH_DENSITY_MAX_WIDTH;
										linearParams.width = HIGH_DENSITY_MAX_WIDTH;
										viewHolder.thumbnailPic.setLayoutParams(linearParams);
									}
									try {
										if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

											DisplayImageOptions options = new DisplayImageOptions.Builder()
													.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
													.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
													.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
													.build();
											ImageLoader imageLoader1 = ImageLoader.getInstance();
											try {
												imageLoader1.displayImage(picList[0].getThumbnailPicUrl(),
														viewHolder.thumbnailPic, options, new AnimateFirstDisplayListener());
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else {
											viewHolder.thumbnailFl.setVisibility(View.GONE);
											viewHolder.thumbnailGridView.setVisibility(View.GONE);
											viewHolder.thumbnailPic.setVisibility(View.GONE);
										}
										if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
											viewHolder.thumbnailPic
													.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
									linearParams.height = DensityUtil.dip2px(ct,
											ct.getResources().getDimension(R.dimen.addmsg_grid_item_width));
									linearParams.width = linearParams.height;
									viewHolder.thumbnailPic.setLayoutParams(linearParams);
									try {
										if (!TextUtils.isEmpty(picList[0].getThumbnailPicUrl())) {

											DisplayImageOptions options = new DisplayImageOptions.Builder()
													.showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
													.showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
													.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true)
													.build();
											ImageLoader imageLoader1 = ImageLoader.getInstance();
											try {
												imageLoader1.displayImage(picList[0].getThumbnailPicUrl(),
														viewHolder.thumbnailPic, options, new AnimateFirstDisplayListener());
											} catch (Exception e) {
												e.printStackTrace();
											}
										} else {
											viewHolder.thumbnailFl.setVisibility(View.GONE);
											viewHolder.thumbnailGridView.setVisibility(View.GONE);
											viewHolder.thumbnailPic.setVisibility(View.GONE);
										}
										if (!TextUtils.isEmpty(picList[0].getBmiddlePicUrl())) {
											viewHolder.thumbnailPic
													.setOnClickListener(new clickPic(picList[0].getBmiddlePicUrl()));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}

					} else {
						LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
						linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
						linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
						viewHolder.thumbnailPic.setLayoutParams(linearParams);
						viewHolder.thumbnailFl.setVisibility(View.GONE);
					}
				} else {
					LayoutParams linearParams = (LayoutParams) viewHolder.thumbnailPic.getLayoutParams();
					linearParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
					linearParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
					viewHolder.thumbnailPic.setLayoutParams(linearParams);
					viewHolder.thumbnailFl.setVisibility(View.GONE);
				}

			}
		}

		return convertView;
	}

	public static class ViewHolder {
		public TextViewFixTouchConsume contentTv;
		public TextViewFixTouchConsume rawContentTv;
		public TextView dateTv;
		public ImageView thumbnailPic;
		public ImageView forwardThumbnailPic;
		public LinearLayout rawcontentlayout;
		public FrameLayout thumbnailFl;
		public FrameLayout forwardThumbnailFl;
		public GridView thumbnailGridView;
		public GridView forwardThumbnailGridView;
		public LinearLayout rootLl;
		public View blankView;
		public LinearLayout seperateLineLl;
	}

	class clickPic implements OnClickListener {
		String picUrl;

		public clickPic(String url) {
			picUrl = url;
		}

		@Override
		public void onClick(View v) {
			if (null != picUrl && !picUrl.equals("")) {
				CharSequence[] middlePics = new CharSequence[1];
				middlePics[0] = picUrl;
				Intent intent = new Intent(ct, ViewPhotoActivity.class);
				intent.putExtra("ID", 0);
				intent.putExtra("middlePics", middlePics);
				ct.startActivity(intent);
				((Activity) ct).overridePendingTransition(R.anim.zoom_enter, 0);
			}
		}
	}

	@SuppressWarnings("unused")
	@SuppressLint("SimpleDateFormat")
	private void string2Date(long date, TextView dateTv) {
		long DAY = 24L * 60L * 60L * 1000L;
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date now = new Date();
		Date date2 = new Date(date);
		if (null != date2) {
			long diff = now.getTime() - date2.getTime();
			dateTv.setText(DateUtil.formatToGroupTagByDay(ct, date2));
			dateTv.setText("2013年12月12日");
		} else {
			dateTv.setText("");
		}

	}

	protected SpannableString getSpannableString(String content, AtMemmber[] messageBoardMembers) {
		SpannableString span = new SpannableString(content);
		char c = '@';
		char[] chars = content.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (c == chars[i]) {
				String itemName = "";
				int index;
				String tempContent = content.substring(i + 1, content.length());
				int at = tempContent.indexOf("@");
				int seleparator = tempContent.indexOf("//");
				int halfAngleColon = tempContent.indexOf(":");
				int fullAngleColon = tempContent.indexOf("：");

				if (at < 0 && seleparator < 0 && halfAngleColon < 0 && fullAngleColon < 0) {
					if (tempContent.indexOf(" ") < 0) {
						itemName = tempContent;
					} else {
						itemName = tempContent.substring(0, tempContent.indexOf(" "));
					}
				} else {
					if (halfAngleColon < 0) {
						if (fullAngleColon != -1) {
							halfAngleColon = tempContent.indexOf("：");
						} else {
							halfAngleColon = 1000000;
						}
					}
					if (seleparator < 0) {
						seleparator = 1000000;
					}
					if (at < 0) {
						at = 1000000;
					}

					index = at < seleparator
							? (at < halfAngleColon ? at : (seleparator < halfAngleColon ? seleparator : halfAngleColon))
							: (seleparator < halfAngleColon ? seleparator : halfAngleColon);
					int blankindex = tempContent.indexOf(" ");
					if (blankindex != -1) {
						index = index > blankindex ? blankindex : index;
					}
					itemName = tempContent.substring(0, index);
				}
				for (AtMemmber member : messageBoardMembers) {
					if (null != member && null != itemName && null != member.getMemberName()
							&& member.getMemberName().trim().equals(itemName.trim())) {
						span.setSpan(new MessageMemberSpanClick(member.getMemberSid()), i, i + itemName.length() + 1,
								Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						span.setSpan(new ForegroundColorSpan(ct.getResources().getColor(R.color.room_at_color)), i,
								i + itemName.length() + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

						break;
					}
				}
				getNoAtSpannedString(span, content);
			}
		}
		return span;
	}

	protected SpannableString getNoAtSpannedString(SpannableString span, String teString) {
//		String[] zh = ct.getResources().getStringArray(R.array.face_zh);
//		String[] en = ct.getResources().getStringArray(R.array.face_en);
		if (null == span) {
			span = new SpannableString(teString);
		}
//		for (int i = 0; i < zh.length; i++) {
//			if (count(teString, zh[i]) != null) {
//				int[] a = count(teString, zh[i]);
//				if (a != null && a.length > 0) {
//					for (int f : a) {
//						int id = ct.getResources().getIdentifier(en[i], "drawable", ct.getPackageName()); //name:图片的名，defType：资源类型（drawable，string。。。），defPackage:工程的包名
//						Drawable drawable = ct.getResources().getDrawable(id);
//						BitmapDrawable bd = (BitmapDrawable) drawable;
//						bd.setBounds(0, 0, bd.getIntrinsicWidth() / 2, bd.getIntrinsicHeight() / 2);
//						span.setSpan(new ImageSpan(bd), f, f + zh[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//					}
//				}
//			}
//		}
		return span;
	}

	protected int[] count(String text, String sub) {
		int count = 0, start = 0;
		while ((start = text.indexOf(sub, start)) >= 0) {
			start += sub.length();
			count++;
		}
		if (count == 0) {
			return null;
		}
		int a[] = new int[count];
		int count2 = 0;
		while ((start = text.indexOf(sub, start)) >= 0) {
			a[count2] = start;
			start += sub.length();
			count2++;
		}
		return a;
	}

	class MessageMemberSpanClick extends ClickableSpan implements OnClickListener {
		String id;

		public MessageMemberSpanClick(String id) {
			this.id = id;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false); //去掉下划线
		}

		@Override
		public void onClick(View v) {
			if (null != id && !"".equals(id)) {
				Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
				intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, id);
				ct.startActivity(intent);
				((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		}

	}

	public void createDialog(Context context, final String userId, final String msgId, final String msgObjectId,
			final String senderSid) {
		RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.report_shield_dialog, null);

		Builder mDialog = new AlertDialog.Builder(context);
		//		mDialog.setView(view,0,0,0,0);
		LinearLayout reportLl = (LinearLayout) view.findViewById(R.id.reportLl);
		LinearLayout shieldLl = (LinearLayout) view.findViewById(R.id.shieldLl);
		mAlertDialog = mDialog.create();
		mAlertDialog.setView(view, 0, 0, 0, 0);
		mAlertDialog.setCanceledOnTouchOutside(true);
		mAlertDialog.show();
		reportLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
				Intent intent = new Intent(ct, WebViewActivityForReport.class);
				intent.putExtra("sid", userId);
				intent.putExtra("type", 1);
				intent.putExtra("entityId", msgId);
				intent.putExtra("entityObjectId", msgObjectId);
				ct.startActivity(intent);
				if (null != ((Activity) ct).getParent()) {
					((Activity) ct).getParent().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			}
		});
		shieldLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null != mAlertDialog) {
					mAlertDialog.dismiss();
				}
				new BlockMessageboardMemberTask(ct) {
					public void doPre() {
					};

					public void doPost(MessageBoardOperation result) {
						if (null != result) {
						}
					};
				}.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
						RenheApplication.getInstance().getUserInfo().getAdSId(), senderSid);
			}
		});
	}

	class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 1000);
					displayedImages.add(imageUri);
				}
			}
		}

	}

}

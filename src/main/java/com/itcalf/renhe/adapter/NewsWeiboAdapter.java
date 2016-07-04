package com.itcalf.renhe.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.view.TextViewFixTouchConsume;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息提醒
 * @author wangning
 * 
 */
public class NewsWeiboAdapter extends BaseAdapter {
	private String email;
	private Context ct;
	private ListView mListView;
	com.itcalf.renhe.view.TextViewFixTouchConsume rawcontentTv;
	com.itcalf.renhe.view.TextViewFixTouchConsume contentTv;
	TextView nameTv;

	Map<String, Object> map;
	// 留言显示数据
	private List<Map<String, Object>> mWeiboList = new ArrayList<Map<String, Object>>();
	SharedPreferences userInfo;
	Editor editor;
	private LayoutInflater flater;

	@SuppressWarnings("unchecked")
	public NewsWeiboAdapter(Context context, List<? extends Map<String, ?>> data, String email, ListView listView) {
		this.flater = LayoutInflater.from(context);
		this.mWeiboList = (List<Map<String, Object>>) data;
		this.email = email;
		this.ct = context;
		this.mListView = listView;
		userInfo = ct.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
		editor = userInfo.edit();
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

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = flater.inflate(R.layout.news_weibo_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.avatar_img);
			viewHolder.clientTv = (TextView) convertView.findViewById(R.id.client_txt);
			viewHolder.contentTv = (TextViewFixTouchConsume) convertView.findViewById(R.id.content_txt);
			viewHolder.rawContentTv = (TextViewFixTouchConsume) convertView.findViewById(R.id.rawcontent_txt);
			viewHolder.dateTv = (TextView) convertView.findViewById(R.id.datetime_txt);
			viewHolder.nameTv = (TextView) convertView.findViewById(R.id.username_txt);
			viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
			viewHolder.realnameIv = (ImageView) convertView.findViewById(R.id.realnameImage);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		map = (Map<String, Object>) getItem(position);
		if (null != map) {
			String name = (String) map.get("senderUsername");
			;
			viewHolder.nameTv.setText(name);
			int type = (Integer) map.get("type");
			String userface = (String) map.get("userface");
			Object dateObject = map.get("datetime");

			//				Object accountObject = map.get("accountType");
			//				Object realNameObject = map.get("isRealName");
			//				int accountType = 0;
			//				boolean isRealName = false;
			//				if (null != accountObject) {
			//					accountType = (Integer) map.get("accountType");//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
			//				}
			//				if (null != realNameObject) {
			//					isRealName = (Boolean) map.get("isRealName");//是否是实名认证的会员
			//				}
			//				switch (accountType) {
			//				case 0:
			//					viewHolder.vipIv.setVisibility(View.GONE);
			//					break;
			//				case 1:
			//					viewHolder.vipIv.setVisibility(View.VISIBLE);
			//					//					vipIv.setImageResource(R.drawable.vip_1);
			//					viewHolder.vipIv.setImageResource(R.drawable.archive_vip_1);
			//					break;
			//				case 2:
			//					viewHolder.vipIv.setVisibility(View.VISIBLE);
			//					//					vipIv.setImageResource(R.drawable.vip_2);
			//					viewHolder.vipIv.setImageResource(R.drawable.archive_vip_2);
			//					break;
			//				case 3:
			//					viewHolder.vipIv.setVisibility(View.VISIBLE);
			//					//					vipIv.setImageResource(R.drawable.vip_3);
			//					viewHolder.vipIv.setImageResource(R.drawable.archive_vip_3);
			//					break;
			//
			//				default:
			//					break;
			//				}
			//				if (isRealName && accountType <= 0) {
			//					viewHolder.realnameIv.setVisibility(View.VISIBLE);
			//					//					realNameIv.setImageResource(R.drawable.realname);
			//					viewHolder.realnameIv.setImageResource(R.drawable.archive_realname);
			//				} else {
			//					viewHolder.realnameIv.setVisibility(View.GONE);
			//				}
			String content = (String) map.get("replyContent");
			String mrawContent = (String) map.get("sourceContent");
			String senderSid = (String) map.get("senderSid");
			if (content == null) {
				content = "";
			}
			if (type == 1 || type == 2 || type == 3) {//回复
				viewHolder.contentTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			} else if (type == 6) {//赞
				content = "赞了您的客厅";
				viewHolder.contentTv.setCompoundDrawablesWithIntrinsicBounds(ct.getResources().getDrawable(R.drawable.good_blue),
						null, null, null);
				viewHolder.contentTv.setCompoundDrawablePadding(10);
			} else if (type == 5) {//@
				viewHolder.contentTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			} else if (type == 4) {//转发
				viewHolder.contentTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			if (null != content && !"".equals(content)) {
				SpannableString spannableString = getNoAtSpannedString(null, content);
				viewHolder.contentTv.setText(spannableString);
			}
			if (null != mrawContent && !"".equals(mrawContent)) {
				SpannableString spannableString2 = getNoAtSpannedString(null, mrawContent);
				viewHolder.rawContentTv.setText(spannableString2);
			}
			if (null != dateObject) {
				String date = dateObject.toString();
				string2Date(date, viewHolder.dateTv);
			}
			if (!TextUtils.isEmpty(userface) && null != viewHolder.avatarIv) {
				ImageLoader imageLoader = ImageLoader.getInstance();
				try {
					imageLoader.displayImage(userface, (ImageView) viewHolder.avatarIv, CacheManager.options,
							CacheManager.animateFirstDisplayListener);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//					if (position == 0) {
				//						this.notifyDataSetChanged();
				//					}
				final String finalSenderSid = senderSid;
				viewHolder.avatarIv.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
						intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, finalSenderSid);
						ct.startActivity(intent);
						((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
				});
			}
		}
		return convertView;
	}

	@SuppressLint("SimpleDateFormat")
	private void string2Date(String date, TextView dateTv) {
		long DAY = 24L * 60L * 60L * 1000L;
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date now = new Date();
		Date date2 = null;
		try {
			date2 = format1.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (null != date2) {
			long diff = now.getTime() - date2.getTime();
			dateTv.setText(DateUtil.formatToGroupTagByDay(ct, date2));
			//			if (diff > DAY * 7) {
			//				dateTv.setTextColor(ct.getResources().getColor(R.color.notice_list_sourcecontent_color));
			//			} else {
			//				dateTv.setTextColor(ct.getResources().getColor(R.color.blog_item_time_text_new));
			//			}
		} else {
			dateTv.setText(date);
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
					itemName = tempContent.substring(0, index);
				}
				for (AtMemmber member : messageBoardMembers) {

					if (member.getMemberName().trim().equals(itemName.trim())) {
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
			}
		}

	}

	public static class ViewHolder {
		public TextViewFixTouchConsume contentTv;
		public TextViewFixTouchConsume rawContentTv;
		public ImageView avatarIv;
		public TextView nameTv;
		public ImageView vipIv;
		public ImageView realnameIv;
		public TextView dateTv;
		public TextView clientTv;
	}
}

package com.itcalf.renhe.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.DateUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
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

/**
 * @author 王宁
 */
public class ReplyListAdapter extends SimpleAdapter {
    private Context ct;

    TextView infoTv;
    TextView timeTv;
    private ListView listView;
    Map<String, Object> map;
    // 留言显示数据
    private List<Map<String, Object>> mWeiboList = new ArrayList<Map<String, Object>>();
    private int DEFAULT_IMAGE;
    private DateUtil dateUtil;

    @SuppressWarnings("unchecked")
    public ReplyListAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to,
                            ListView listView) {
        super(context, data, resource, from, to);
        this.mWeiboList = (List<Map<String, Object>>) data;
        this.ct = context;
        this.listView = listView;
        DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
        this.dateUtil = new DateUtil();
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (null != view) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            ImageView avartar = (ImageView) view.findViewById(R.id.avatar_img);
            infoTv = (TextView) view.findViewById(R.id.infoTv);
            ImageView replyIcon = (ImageView) view.findViewById(R.id.reply_icon);
            if (position == 0) {
                //				line.setVisibility(View.GONE);
                replyIcon.setVisibility(View.VISIBLE);
            } else {
                //				line.setVisibility(View.VISIBLE);
                replyIcon.setVisibility(View.INVISIBLE);
            }
            map = (Map<String, Object>) getItem(position);
            if (null != map) {
                String userface = (String) map.get("senderUserface");
                //				avartar.setTag(userface + position);
                if (null != userface && !TextUtils.isEmpty(userface)) {
                    avartar.setTag(userface + position);
                    final int pos = position;
                    try {
                        imageLoader.displayImage(userface, avartar, CacheManager.circleImageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                TextView namView = (TextView) view.findViewById(R.id.nameTv);
                //
                TextView huifuView = (TextView) view.findViewById(R.id.huifuTv);
                TextView name2View = (TextView) view.findViewById(R.id.nameTv2);
                String senderName = "";
                String reRenderName = "";
                String senderSid = "";
                String resenderSid = "";
                if (null != map.get("senderName")) {
                    senderName = (String) map.get("senderName");
                }
                if (null != map.get("reSenderMemberName")) {
                    reRenderName = (String) map.get("reSenderMemberName");
                }
                if (null != map.get("senderSid")) {
                    senderSid = (String) map.get("senderSid");
                }
                if (null != map.get("reSenderSid")) {
                    resenderSid = (String) map.get("reSenderSid");
                }
                if (!TextUtils.isEmpty(reRenderName)) {
                    namView.setText(senderName + "");
                    huifuView.setVisibility(View.VISIBLE);
                    name2View.setVisibility(View.VISIBLE);
                    name2View.setText(reRenderName);
                } else {
                    namView.setText(senderName);
                    huifuView.setVisibility(View.GONE);
                    name2View.setVisibility(View.GONE);
                }
                final String msenderSid = senderSid;
                final String mrsenderSid = resenderSid;
                namView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, msenderSid);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });
                name2View.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, mrsenderSid);
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });

                ImageView vipIv = (ImageView) view.findViewById(R.id.vipImage);
                ImageView realNameIv = (ImageView) view.findViewById(R.id.realnameImage);

                Object accountObject = map.get("accountType");
                Object realNameObject = map.get("isRealName");
                int accountType = 0;
                boolean isRealName = false;
                if (null != accountObject) {
                    accountType = (Integer) map.get("accountType");
                }
                if (null != realNameObject) {
                    isRealName = (Boolean) map.get("isRealName");//是否是实名认证的会员
                }
                switch (accountType) {
                    case 0:
                        vipIv.setVisibility(View.GONE);
                        break;
                    case 1:
                        vipIv.setVisibility(View.GONE);
                        //					vipIv.setImageResource(R.drawable.reply_item_vip1);
                        vipIv.setImageResource(R.drawable.archive_vip_1);
                        break;
                    case 2:
                        vipIv.setVisibility(View.GONE);
                        //					vipIv.setImageResource(R.drawable.reply_item_vip2);
                        vipIv.setImageResource(R.drawable.archive_vip_2);
                        break;
                    case 3:
                        vipIv.setVisibility(View.GONE);
                        //					vipIv.setImageResource(R.drawable.reply_item_vip3);
                        vipIv.setImageResource(R.drawable.archive_vip_3);
                        break;

                    default:
                        break;
                }
                if (isRealName && accountType <= 0) {
                    realNameIv.setVisibility(View.GONE);
                    //					realNameIv.setImageResource(R.drawable.realname);
                    realNameIv.setImageResource(R.drawable.archive_realname);
                } else {
                    realNameIv.setVisibility(View.GONE);
                }
                String info = "";
                if (null != map.get("content")) {
                    info = map.get("content").toString();
                }
                info = ToDBC(info);
                if (info.length() >= 2 && info.lastIndexOf("\n") == info.length() - 1) {
                    info = info.substring(0, info.length() - 2);
                }
                infoTv.setText(getSpannedString(info));
                timeTv = (TextView) view.findViewById(R.id.timeTv);
                long time = 0;
                if (null != map.get("createdDateSeconds")) {
                    time = (Long) map.get("createdDateSeconds");

                }
                string2Date(time, timeTv);
                dateUtil.string2Date(ct, time, timeTv);
                avartar.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, msenderSid);
                        ct.startActivity(intent);
                        ((BaseActivity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    }
                });
            }
        }
        return view;
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
        } else {
            dateTv.setText("");
        }

    }

    protected SpannableString getSpannedString(String teString) {
        String[] zh = ct.getResources().getStringArray(R.array.face_zh);
        String[] en = ct.getResources().getStringArray(R.array.face_en);
        SpannableString spannableString = new SpannableString(teString);
        for (int i = 0; i < zh.length; i++) {
            if (count(teString, zh[i]) != null) {
                int[] a = count(teString, zh[i]);
                if (a != null && a.length > 0) {
                    for (int f : a) {
                        int id = ct.getResources().getIdentifier(en[i], "drawable", ct.getPackageName()); //name:图片的名，defType：资源类型（drawable，string。。。），defPackage:工程的包名
                        Drawable drawable = ct.getResources().getDrawable(id);
                        BitmapDrawable bd = (BitmapDrawable) drawable;
                        bd.setBounds(0, 0, bd.getIntrinsicWidth() / 3, bd.getIntrinsicHeight() / 3);
                        spannableString.setSpan(new ImageSpan(bd), f, f + zh[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        return spannableString;
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

    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
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

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
            if (null != view) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(DEFAULT_IMAGE);
            }

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            super.onLoadingCancelled(imageUri, view);
            if (null != view) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(DEFAULT_IMAGE);
            }
        }

    }
}
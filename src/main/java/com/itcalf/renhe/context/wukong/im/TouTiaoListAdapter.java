package com.itcalf.renhe.context.wukong.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiao;
import com.itcalf.renhe.dto.TouTiaoOperation.TouTiaoList;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author 王宁
 */
public class TouTiaoListAdapter extends BaseAdapter {
    private Context ct;
    TextView infoTv;
    TextView timeTv;
    // 留言显示数据
    private List<TouTiaoList> touTiaoLists = new ArrayList<TouTiaoList>();
    private LayoutInflater flater;
    private int DEFAULT_IMAGE;

    @SuppressWarnings("unchecked")
    public TouTiaoListAdapter(Context context, List<TouTiaoList> data) {
        this.flater = LayoutInflater.from(context);
        this.touTiaoLists = (List<TouTiaoList>) data;
        this.ct = context;
        DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = flater.inflate(R.layout.toutiao_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.timeTv = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.itemListView = (ListView) convertView.findViewById(R.id.toutiao_list_item_listview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TouTiaoList mTouTiaoList = touTiaoLists.get(position);
        if (null != mTouTiaoList) {
            viewHolder.timeTv.setText(DateUtil.newFormatByDay(ct, new Date(mTouTiaoList.getCreatedDate())));
            final TouTiao[] mTouTiaos = mTouTiaoList.getToutiaoList();
            if (null != mTouTiaos && mTouTiaos.length > 0) {
                TouTiaoListItemAdapter tiaoListItemAdapter = new TouTiaoListItemAdapter(ct, mTouTiaos);
                viewHolder.itemListView.setAdapter(tiaoListItemAdapter);
                setListViewHeight(viewHolder.itemListView);
                viewHolder.itemListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        Intent intent = new Intent();
                        intent.setClass(ct, WebViewActWithTitle.class);
                        intent.putExtra("picture", mTouTiaos[arg2].getImage());
                        intent.putExtra("title", mTouTiaos[arg2].getTitle());
                        intent.putExtra("url", mTouTiaos[arg2].getUrl());
                        intent.putExtra("login", "&adSid=" + RenheApplication.getInstance().getUserInfo().getAdSId() + "&sid="
                                + RenheApplication.getInstance().getUserInfo().getSid());
                        intent.putExtra("type", "share");
                        ct.startActivity(intent);
                        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        StatisticsUtil.statisticsCustomClickEvent(ct.getString(R.string.android_btn_menu2_hyttdetail_click), 0, "", null);
                    }

                });
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return touTiaoLists.size();
    }

    @Override
    public Object getItem(int arg0) {
        return touTiaoLists.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public static class ViewHolder {
        public TextView timeTv;
        public ListView itemListView;

    }

    /**
     * 设置Listview的高度
     */

    public void setListViewHeight(ListView listView) {
        android.widget.ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null) {

            return;

        }

        int totalHeight = 0;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);

            listItem.measure(0, 0);

            totalHeight += listItem.getMeasuredHeight();

        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);

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

    private boolean isViewExist(ViewGroup parent, int id, int messageId) {
        boolean flag = false;
        String childViewTag = id + "";
        if (null != parent && parent.getTag() != null && parent.getTag().toString().equals(messageId + "")) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                Object viewTag = parent.getChildAt(i).getTag();
                if (null != viewTag.toString() && viewTag.toString().equals(childViewTag)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
}
package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.dto.RecommendedUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Title: RecommendGridlistAdapter.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-12 上午10:52:40 <br>
 *
 * @author wangning
 */
public class RecommendGridlistAdapter extends BaseAdapter {
    private LayoutInflater flater;
    private Context ct;
    private List<RecommendedUser> recommendedUserList = new ArrayList<RecommendedUser>();
    private ListView listView;

    public RecommendGridlistAdapter(Context ct, List<RecommendedUser> recommendedUserList, ListView listView) {
        super();
        this.flater = LayoutInflater.from(ct);
        this.ct = ct;
        this.recommendedUserList = recommendedUserList;
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return recommendedUserList.size() != 0 ? recommendedUserList.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        return recommendedUserList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = flater.inflate(R.layout.register_recommend_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.avatarIv = (ImageView) convertView.findViewById(R.id.avatar_img);
            viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);
            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.username_tv);
            viewHolder.mTitleTv = (TextView) convertView.findViewById(R.id.usertitle_tv);
            viewHolder.isCheckedIv = (ImageView) convertView.findViewById(R.id.check_iv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        RecommendedUser recommendedUser = recommendedUserList.get(position);
        if (null != recommendedUser) {
            viewHolder.nameTv.setText(recommendedUser.getName());
            viewHolder.mTitleTv.setText(recommendedUser.getTitle());
            if (!TextUtils.isEmpty(recommendedUser.getTitle())) {
                viewHolder.mTitleTv.setText(recommendedUser.getTitle());
            }
            if (!TextUtils.isEmpty(recommendedUser.getCompany())) {
                if (!TextUtils.isEmpty(viewHolder.mTitleTv.getText().toString())) {
                    viewHolder.mTitleTv.setText(viewHolder.mTitleTv.getText().toString() + " / " + recommendedUser.getCompany().trim());
                } else {
                    viewHolder.mTitleTv.setText(recommendedUser.getCompany().trim());
                }
            }
            if (TextUtils.isEmpty(recommendedUser.getTitle()) && TextUtils.isEmpty(recommendedUser.getCompany())) {
                viewHolder.mTitleTv.setVisibility(View.GONE);
            } else {
                viewHolder.mTitleTv.setVisibility(View.VISIBLE);
            }
            int accountType = recommendedUser.getAccountType();

            viewHolder.vipIv.setVisibility(View.GONE);
            switch (accountType) {
                case 0:
                    if (recommendedUser.isRealname()) {
                        viewHolder.vipIv.setVisibility(View.VISIBLE);
                        viewHolder.vipIv.setImageResource(R.drawable.archive_realname);
                    }
                    break;
                case 1:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_1);
                    break;
                case 2:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_2);
                    break;
                case 3:
                    viewHolder.vipIv.setVisibility(View.VISIBLE);
                    viewHolder.vipIv.setImageResource(R.drawable.archive_vip_3);
                    break;
                default:
                    break;
            }
            if (recommendedUser.isChecked()) {
                viewHolder.isCheckedIv.setVisibility(View.VISIBLE);
            } else {
                viewHolder.isCheckedIv.setVisibility(View.GONE);
            }
            String avatarUrl = recommendedUser.getUserface();
            if (!TextUtils.isEmpty(avatarUrl)) {
                viewHolder.isCheckedIv.setTag(avatarUrl + position);
                ImageLoader imageLoader = ImageLoader.getInstance();
                try {
                    imageLoader.displayImage(avatarUrl, viewHolder.avatarIv, CacheManager.circleImageOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return convertView;
    }

    public static class ViewHolder {
        public TextView nameTv;
        public ImageView avatarIv;
        public TextView mTitleTv;
        public ImageView vipIv;
        public ImageView isCheckedIv;
    }
}

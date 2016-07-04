package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.po.Contact;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ContactSearchAdapter extends BaseAdapter {
    Context context;
    List<Contact> contactslist;

    public ContactSearchAdapter(Context _cxt, List<Contact> _cl) {
        this.context = _cxt;
        this.contactslist = _cl;
    }

    @Override
    public int getCount() {
        return contactslist.size();
    }

    @Override
    public Object getItem(int arg0) {
        return contactslist.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder viewhold = null;
        if (convertView == null) {
            viewhold = new ViewHolder();
            convertView = (View) LayoutInflater.from(context).inflate(R.layout.contact_list_item, null);
            viewhold.avatarView = (ImageView) convertView.findViewById(R.id.avatar_img);// 头像
            viewhold.username_txt = (TextView) convertView.findViewById(R.id.username_txt);// 联系人名字
            viewhold.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewhold.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
            viewhold.job_txt = (TextView) convertView.findViewById(R.id.job_txt);// 联系人工作
            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }

        Contact ct = contactslist.get(position);
        if (null != ct) {
            String userface = ct.getContactface();
            int accountType = ct.getAccountType();
            boolean isRealName = ct.isRealname();
            String username = ct.getName();
            String userjob = ct.getJob();
            String userCompany = ct.getCompany();

            // 头像显示
            if (!TextUtils.isEmpty(userface) && null != viewhold.avatarView) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                try {
                    imageLoader.displayImage(userface, viewhold.avatarView, CacheManager.circleImageOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            viewhold.username_txt.setText(username);
            if (!TextUtils.isEmpty(userjob)) {
                viewhold.job_txt.setText(userjob);
            }
            if (!TextUtils.isEmpty(userCompany)) {
                if (!TextUtils.isEmpty(viewhold.job_txt.getText().toString())) {
                    viewhold.job_txt.setText(viewhold.job_txt.getText().toString() + " / " + userCompany.trim());
                } else {
                    viewhold.job_txt.setText(userCompany.trim());
                }
            }
            if (TextUtils.isEmpty(userjob) && TextUtils.isEmpty(userCompany)) {
                viewhold.job_txt.setVisibility(View.GONE);
            } else {
                viewhold.job_txt.setVisibility(View.VISIBLE);
            }

            // Vip标志显示
            switch (accountType) {
                case 0:
                    viewhold.vipIv.setVisibility(View.GONE);
                    break;
                case 1:
                    viewhold.vipIv.setVisibility(View.VISIBLE);
                    //				viewhold.vipIv.setImageResource(R.drawable.vip_1);
                    viewhold.vipIv.setImageResource(R.drawable.archive_vip_1);
                    break;
                case 2:
                    viewhold.vipIv.setVisibility(View.VISIBLE);
                    //				viewhold.vipIv.setImageResource(R.drawable.vip_2);
                    viewhold.vipIv.setImageResource(R.drawable.archive_vip_2);
                    break;
                case 3:
                    viewhold.vipIv.setVisibility(View.VISIBLE);
                    //				viewhold.vipIv.setImageResource(R.drawable.vip_3);
                    viewhold.vipIv.setImageResource(R.drawable.archive_vip_3);
                    break;

                default:
                    viewhold.vipIv.setVisibility(View.GONE);
                    break;
            }
            // 实名标志
            if (isRealName && accountType <= 0) {
                viewhold.realNameIv.setVisibility(View.VISIBLE);
                //				viewhold.realNameIv.setImageResource(R.drawable.realname);
                viewhold.realNameIv.setImageResource(R.drawable.archive_realname);
            } else {
                viewhold.realNameIv.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView avatarView;
        TextView username_txt;
        ImageView vipIv;
        ImageView realNameIv;
        TextView job_txt;
    }
}

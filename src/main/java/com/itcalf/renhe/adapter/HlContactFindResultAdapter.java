package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactItem;
import com.itcalf.renhe.bean.HlContacts;
import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @author chan
 * @createtime 2014-10-20
 * @功能说明 联系人列表适配器带字母
 */
public class HlContactFindResultAdapter extends BaseAdapter {
    private ImageLoader imageLoader;
    protected List<HlContacts> hlFindResultContactsList;
    private Context context;

    public HlContactFindResultAdapter(Context context, List<HlContacts> hlFindResultContactsList) {
        this.context = context;
        imageLoader = ImageLoader.getInstance();
        this.hlFindResultContactsList = hlFindResultContactsList;
    }

    @Override
    public int getCount() {
        return hlFindResultContactsList.size();
    }

    @Override
    public Object getItem(int position) {
        return hlFindResultContactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.hl_contact_find_result_list_item, null);
            viewHolder.listitem_ll = (LinearLayout) convertView.findViewById(R.id.listitem_ll);
            viewHolder.avatarView = (ImageView) convertView.findViewById(R.id.avatar_img);// 头像
            viewHolder.avatar_txt = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewHolder.username_txt = (TextView) convertView.findViewById(R.id.username_txt);// 联系人名字
            viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewHolder.job_txt = (TextView) convertView.findViewById(R.id.job_txt);// 联系人工作
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HlContacts hlContacts = hlFindResultContactsList.get(position);
        if (null != hlContacts) {
            int type = hlContacts.getType();
            String userface = null;
            int accountType = 0;
            boolean isRealName = false;
            String username = null;
            String userjob = null;
            String userCompany = null;
            String shortName = null;
            int colorIndex;
            switch (type) {
                case HlContacts.HLCONTACTS_RENHE_MEMBER_TYPE:
                    userface = hlContacts.getHlContactRenheMember().getUserface();
                    accountType = hlContacts.getHlContactRenheMember().getAccountType();
                    isRealName = hlContacts.getHlContactRenheMember().isRealname();
                    username = hlContacts.getHlContactRenheMember().getName();
                    userjob = hlContacts.getHlContactRenheMember().getTitle();
                    userCompany = hlContacts.getHlContactRenheMember().getCompany();

                    viewHolder.avatarView.setVisibility(View.VISIBLE);
                    viewHolder.avatar_txt.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(userface)) {
                        try {
                            imageLoader.displayImage(userface, viewHolder.avatarView, CacheManager.circleImageOptions);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HlContacts.HLCONTACTS_CONTACT_MEMBER_TYPE:
                    username = hlContacts.getHlContactContactMember().getName();
                    colorIndex = hlContacts.getHlContactContactMember().getColorIndex();
                    shortName = hlContacts.getHlContactContactMember().getShortName();

                    viewHolder.avatarView.setVisibility(View.GONE);
                    viewHolder.avatar_txt.setVisibility(View.VISIBLE);
                    if (colorIndex < Constants.AVATARBG.length)
                        viewHolder.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                    viewHolder.avatar_txt.setText(shortName);
                    break;
                case HlContacts.HLCONTACTS_CARD_MEMBER_TYPE:
                    username = hlContacts.getHlContactCardMember().getName();
                    colorIndex = hlContacts.getHlContactCardMember().getColorIndex();
                    shortName = hlContacts.getHlContactCardMember().getShortName();
                    viewHolder.avatarView.setVisibility(View.GONE);
                    viewHolder.avatar_txt.setVisibility(View.VISIBLE);
                    if (colorIndex < Constants.AVATARBG.length)
                        viewHolder.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                    viewHolder.avatar_txt.setText(shortName);
                    break;
            }
            viewHolder.username_txt.setText(username);

            if (!TextUtils.isEmpty(userjob)) {
                viewHolder.job_txt.setText(userjob);
            }
            if (!TextUtils.isEmpty(userCompany)) {
                if (!TextUtils.isEmpty(viewHolder.job_txt.getText().toString())) {
                    viewHolder.job_txt.setText(viewHolder.job_txt.getText().toString() + " / " + userCompany.trim());
                } else {
                    viewHolder.job_txt.setText(userCompany.trim());
                }
            }
            if (TextUtils.isEmpty(userjob) && TextUtils.isEmpty(userCompany)) {
                viewHolder.job_txt.setVisibility(View.GONE);
            } else {
                viewHolder.job_txt.setVisibility(View.VISIBLE);
            }
            // Vip标志显示
            viewHolder.vipIv.setVisibility(View.GONE);
            switch (accountType) {
                case 0:
                    if (isRealName) {
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
            }
        }
        return convertView;
    }

    public class ViewHolder {
        LinearLayout listitem_ll;
        ImageView avatarView;// 头像
        TextView avatar_txt;
        TextView username_txt;// 联系人名字
        ImageView vipIv;// Vip标志
        TextView job_txt;// 联系人工作
    }
}
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
import com.itcalf.renhe.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @author chan
 * @createtime 2014-10-20
 * @功能说明 联系人列表适配器带字母
 */
public class HlContactArrayAdapter extends BaseAdapter implements PinnedSectionListAdapter {
    public final static String IS_NEWFRIEND_ID = "new";
    public final static String IS_CIRCLE_ID = "circle";
    public final static String IS_CONTACT_ID = "contact";
    private ImageLoader imageLoader;
    protected List<HlContactItem> hlContactItemList;
    private Context context;

    public HlContactArrayAdapter(Context context, List<HlContactItem> hlContactItemList) {
        this.context = context;
        imageLoader = ImageLoader.getInstance();
        this.hlContactItemList = hlContactItemList;
    }

    protected void prepareSections(int sectionsNumber) {
    }

    // 列表添加项
    protected void onSectionAdded(HlContactItem section, int sectionPosition) {
    }

    @Override
    public int getCount() {
        return hlContactItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return hlContactItemList.get(position);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.hl_contact_list_item, null);
            //标题栏
            viewHolder.title_txt = (LinearLayout) convertView.findViewById(R.id.title_txt_container);
            viewHolder.titleTxt = (TextView) convertView.findViewById(R.id.title_txt);
            viewHolder.contactDivider = convertView.findViewById(R.id.contact_divider);
            //通讯录好友
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

        HlContactItem item = hlContactItemList.get(position);
        HlContacts hlContacts = item.getHlContacts();
        if (item.type == HlContactItem.SECTION) {
            viewHolder.listitem_ll.setVisibility(View.GONE);
            viewHolder.title_txt.setVisibility(View.VISIBLE);
            viewHolder.titleTxt.setText(item.getText());
        } else {
            viewHolder.title_txt.setVisibility(View.GONE);
            viewHolder.listitem_ll.setVisibility(View.VISIBLE);
            if (position < getCount() - 1) {
                HlContactItem tmpItem = hlContactItemList.get(position + 1);
                if (tmpItem.type == HlContactItem.SECTION)
                    viewHolder.contactDivider.setVisibility(View.GONE);
                else {
                    viewHolder.contactDivider.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.contactDivider.setVisibility(View.GONE);
            }
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
        }
        return convertView;
    }

    public class ViewHolder {
        //标题栏
        LinearLayout title_txt;
        TextView titleTxt;
        View contactDivider;
        //通讯录好友
        LinearLayout listitem_ll;
        ImageView avatarView;// 头像
        TextView avatar_txt;
        TextView username_txt;// 联系人名字
        ImageView vipIv;// Vip标志
        TextView job_txt;// 联系人工作
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return hlContactItemList.get(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == HlContactItem.SECTION;
    }

}
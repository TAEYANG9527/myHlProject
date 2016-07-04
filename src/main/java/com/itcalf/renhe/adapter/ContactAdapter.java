package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlMemberSaveToMail;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.utils.PinyinUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * description :联系人Adapter
 * Created by Chans Renhenet
 * 2015/12/23
 */
public class ContactAdapter extends BaseAdapter implements SectionIndexer {
    private Context mContext;
    private List<HlMemberSaveToMail> contacts;// 好友信息
    private ImageLoader imageLoader;

    public ContactAdapter(Context mContext, List<HlMemberSaveToMail> contacts) {
        this.mContext = mContext;
        this.contacts = contacts;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return contacts == null ? 0 : contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, parent, false);
            //标题栏
            viewHolder.titleLl = (LinearLayout) convertView.findViewById(R.id.title_txt_container);
            viewHolder.titleTxt = (TextView) convertView.findViewById(R.id.title_txt);
            //通讯录好友
            viewHolder.listItemLl = (LinearLayout) convertView.findViewById(R.id.list_item_ll);
            viewHolder.avatarView = (ImageView) convertView.findViewById(R.id.avatar_img);// 头像
            viewHolder.avatarTxt = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewHolder.userNameTxt = (TextView) convertView.findViewById(R.id.username_txt);// 联系人名字
            viewHolder.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewHolder.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
            viewHolder.jobTxt = (TextView) convertView.findViewById(R.id.job_txt);// 联系人工作
            viewHolder.companyTxt = (TextView) convertView.findViewById(R.id.company_txt);// 联系人所在的公司
            viewHolder.contactDivider = convertView.findViewById(R.id.contact_divider);

            viewHolder.checkCB = (CheckBox) convertView.findViewById(R.id.check_CB);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //加载数据
        HlMemberSaveToMail contact = contacts.get(position);
        if (null == contact)
            return convertView;
        //字母tag
        String catalog = PinyinUtil.cn2FirstSpell(contact.getHlContactRenheMember().getName())
                .substring(0, 1);
        if (position == 0) {
            viewHolder.titleLl.setVisibility(View.VISIBLE);
            viewHolder.titleTxt.setText(catalog);
            viewHolder.contactDivider.setVisibility(View.GONE);
        } else {
            HlMemberSaveToMail nextContact = contacts.get(position - 1);
            String lastCatalog = PinyinUtil.cn2FirstSpell(nextContact.getHlContactRenheMember().getName()).substring(0, 1);
            if (catalog.equals(lastCatalog)) {
                viewHolder.titleLl.setVisibility(View.GONE);
                viewHolder.contactDivider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.titleLl.setVisibility(View.VISIBLE);
                viewHolder.titleTxt.setText(catalog);
                viewHolder.contactDivider.setVisibility(View.GONE);
            }
        }
        String userFace = contact.getHlContactRenheMember().getUserface();
        int accountType = contact.getHlContactRenheMember().getAccountType();
        boolean isRealName = contact.getHlContactRenheMember().isRealname();
        String userName = contact.getHlContactRenheMember().getName();
        String userJob = contact.getHlContactRenheMember().getTitle();
        String userCompany = contact.getHlContactRenheMember().getCompany();
//        String shortName = contact.getShortName();
//        int colorIndex = contact.getColorIndex();
        //头像
        viewHolder.avatarView.setVisibility(View.VISIBLE);
        viewHolder.avatarTxt.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(userFace)) {
            try {
                imageLoader.displayImage(userFace, viewHolder.avatarView, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //名字
        viewHolder.userNameTxt.setText(userName);
        //职位
        if (!TextUtils.isEmpty(userJob)) {
            viewHolder.jobTxt.setVisibility(View.VISIBLE);
            viewHolder.jobTxt.setText(userJob);
        } else {
            viewHolder.jobTxt.setVisibility(View.GONE);
        }
        //公司
        if (!TextUtils.isEmpty(userCompany)) {
            viewHolder.companyTxt.setVisibility(View.VISIBLE);
            viewHolder.companyTxt.setText(userCompany);
        } else {
            viewHolder.companyTxt.setVisibility(View.GONE);
        }
        // Vip标志显示
        switch (accountType) {
            case 0:
                viewHolder.vipIv.setVisibility(View.GONE);
                break;
            case 1:
                viewHolder.vipIv.setVisibility(View.VISIBLE);
                viewHolder.vipIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.archive_vip_1));
                break;
            case 2:
                viewHolder.vipIv.setVisibility(View.VISIBLE);
                viewHolder.vipIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.archive_vip_2));
                break;
            case 3:
                viewHolder.vipIv.setVisibility(View.VISIBLE);
                viewHolder.vipIv.setImageDrawable(mContext.getResources().getDrawable(R.drawable.archive_vip_3));
                break;
            default:
                viewHolder.vipIv.setVisibility(View.GONE);
                break;
        }
        // 实名标志
        if (isRealName && accountType <= 0) {
            viewHolder.realNameIv.setVisibility(View.VISIBLE);
            viewHolder.realNameIv.setImageResource(R.drawable.archive_realname);
        } else {
            viewHolder.realNameIv.setVisibility(View.GONE);
        }
        //是否选中
        boolean isSelect = contact.isSelect();
        viewHolder.checkCB.setSelected(isSelect);
        return convertView;
    }

    public class ViewHolder {
        //标题栏tag
        LinearLayout titleLl;
        TextView titleTxt;
        //通讯录好友
        LinearLayout listItemLl;
        ImageView avatarView;// 头像
        TextView avatarTxt;
        TextView userNameTxt;// 联系人名字
        ImageView vipIv;// Vip标志
        ImageView realNameIv;// 实名标志
        TextView jobTxt;// 联系人工作
        TextView companyTxt;// 联系人所在的公司

        CheckBox checkCB;//选择框
        View contactDivider;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < contacts.size(); i++) {
            HlMemberSaveToMail contact = contacts.get(i);
            String l = PinyinUtil.cn2FirstSpell(contact.getHlContactRenheMember().getName()).substring(0, 1);
            char firstChar = l.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}

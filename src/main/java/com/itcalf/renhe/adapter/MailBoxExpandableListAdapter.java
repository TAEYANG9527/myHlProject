package com.itcalf.renhe.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.RenheMemberInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.contacts.MailBoxList;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * description :邮箱联系人，二级支持展开列表
 * Created by Chans Renhenet
 * 2015/10/12
 */
public class MailBoxExpandableListAdapter extends BaseExpandableListAdapter {

    private List<String> group;
    private List<List<MailBoxContact>> cList;
    private Context context;
    private ImageLoader imageLoader;

    public MailBoxExpandableListAdapter(Context mContext, List<String> group, List<List<MailBoxContact>> cList) {
        this.context = mContext;
        this.group = group;
        this.cList = cList;
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getGroupCount() {
        return group.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return cList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return cList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.expandable_list_group_item, null);
            holder = new GroupViewHolder();
            holder.textView = (TextView) convertView
                    .findViewById(R.id.title_txt);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.textView.setText(group.get(groupPosition));
        return convertView;
    }

    class GroupViewHolder {
        TextView textView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mobile_contacts_list_item, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.title_txt);
            holder.checkCB = (CheckBox) convertView.findViewById(R.id.check_CB);
            holder.contactAvatarRl = (RelativeLayout) convertView.findViewById(R.id.avatar_img_ll);
            holder.avatar = (ImageView) convertView.findViewById(R.id.contactAvatar_img);
            holder.avatarTv = (TextView) convertView.findViewById(R.id.avatar_txt);
            holder.name = (TextView) convertView.findViewById(R.id.contactName_txt);
            holder.isrenheIcon = (ImageView) convertView.findViewById(R.id.isrenheIcon);
            holder.number = (TextView) convertView.findViewById(R.id.contactPhone_txt);
            holder.add_btn = (Button) convertView.findViewById(R.id.add_btn);
            holder.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MailBoxContact cv = cList.get(groupPosition).get(childPosition);
        if (cv != null) {
            String name = cv.getName().trim();
            String email = cv.getEmail();
            boolean isRenheMember = cv.isRenheMember();

            int colorIndex = cv.getColorIndex();
            String shortName = cv.getShortName();

            if (isRenheMember) {
                holder.checkCB.setVisibility(View.GONE);
                holder.isrenheIcon.setVisibility(View.GONE);
                holder.added_txt.setVisibility(View.VISIBLE);
                holder.add_btn.setVisibility(View.VISIBLE);
                holder.add_btn.setBackgroundResource(R.drawable.btn_bg_color8ec73f_rectangle_empty_corner_style);
                Resources resource = context.getResources();
                ColorStateList csl = resource.getColorStateList(R.color.c9_textcolor_selector);
                if (csl != null) {
                    holder.add_btn.setTextColor(csl);
                }

                holder.avatar.setVisibility(View.VISIBLE);
                holder.avatarTv.setVisibility(View.GONE);
                if (null != cv.getRenheMemberInfo()) {
                    //头像
                    String userFace = cv.getRenheMemberInfo().getUserface();
                    //公司，职位
                    String company = cv.getRenheMemberInfo().getCompany();
                    company = TextUtils.isEmpty(company) ? "" : company;
                    String title = cv.getRenheMemberInfo().getTitle();
                    title = TextUtils.isEmpty(title) ? "" : title;

                    // 头像显示
                    if (!TextUtils.isEmpty(userFace)) {
                        try {
                            imageLoader.displayImage(userFace, holder.avatar, CacheManager.options,
                                    CacheManager.animateFirstDisplayListener);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!TextUtils.isEmpty(company) && !TextUtils.isEmpty(title)) {
                        holder.number.setText(title + " / " + company);
                    } else if (TextUtils.isEmpty(company) && TextUtils.isEmpty(title)) {
                        holder.number.setText(email);
                    } else {
                        holder.number.setText(title + company);
                    }
                    boolean isSelf = cv.getRenheMemberInfo().isSelf();//是否是自己
                    boolean isConnection = cv.getRenheMemberInfo().isConnection();//是否是好友
                    boolean isInvite = cv.getRenheMemberInfo().isInvite();
                    if (isSelf) {
                        holder.add_btn.setVisibility(View.GONE);
                    } else {
                        holder.add_btn.setVisibility(View.VISIBLE);
                        if (isConnection) {
                            holder.add_btn.setVisibility(View.GONE);
                            holder.added_txt.setVisibility(View.VISIBLE);
                            holder.added_txt.setText("已添加");
                        } else {
                            if (isInvite) {
                                holder.add_btn.setVisibility(View.GONE);
                                holder.added_txt.setVisibility(View.VISIBLE);
                                holder.added_txt.setText("已邀请");//已发出邀请
                            } else {
                                holder.add_btn.setVisibility(View.VISIBLE);
                                holder.added_txt.setVisibility(View.GONE);
                                holder.add_btn.setText("添加");
                            }
                        }
                    }
                }
            } else {
                holder.checkCB.setVisibility(View.VISIBLE);
                holder.isrenheIcon.setVisibility(View.GONE);
                holder.added_txt.setVisibility(View.GONE);
                holder.add_btn.setVisibility(View.VISIBLE);
                holder.add_btn.setText("邀请");
                holder.add_btn.setBackgroundResource(R.drawable.btn_cl_stroke_bc3_p_selector);
                Resources resource = context.getResources();
                ColorStateList csl = resource.getColorStateList(R.color.cl_textcolor_selected);
                if (csl != null) {
                    holder.add_btn.setTextColor(csl);
                }
                if (cv.getSelect() == 1) {
                    holder.checkCB.setSelected(true);
                } else {
                    holder.checkCB.setSelected(false);
                }
                holder.avatar.setVisibility(View.GONE);
                holder.avatarTv.setVisibility(View.VISIBLE);
                holder.avatarTv.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                holder.avatarTv.setText(shortName);
                holder.number.setText(email);
            }
            holder.name.setText(name);

            holder.add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //发邮件
                    if (!cv.isRenheMember()) {
                        String email = cv.getEmail();
                        if (email.length() == 0) {
                            ToastUtil.showErrorToast(context, context.getResources().getString(R.string.mailnotnull));
                            return;
                        }
                        if (!email.matches("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")
                                && !email.matches("^(13[0-9]|15[0-9]|18[0-9])\\d{8}$")) {
                            ToastUtil.showErrorToast(context, R.string.error_mail_format);
                            return;
                        }
                        ((MailBoxList) context).inviteMailContact("" + cv.getContactId());
                    }
                    //发送添加信息
                    else {
                        RenheMemberInfo memberInfo = cv.getRenheMemberInfo();
                        if (memberInfo != null) {
                            String sid = memberInfo.getMemberSId();
                            String name = cv.getName().trim();
                            Intent i = new Intent(context, AddFriendAct.class);
                            i.putExtra("mSid", sid);
                            i.putExtra("friendName", name);
                            i.putExtra("position", childPosition);//只是人和会员的位置
                            i.putExtra("addfriend_from", "email");
                            context.startActivity(i);
                        }
                    }
                }
            });
        }
        return convertView;
    }

    public class ViewHolder {
        TextView alpha;
        CheckBox checkCB;
        ImageView avatar;
        TextView name;
        TextView number;
        public Button add_btn;
        public TextView added_txt;
        RelativeLayout contactAvatarRl;
        TextView avatarTv;
        ImageView isrenheIcon;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

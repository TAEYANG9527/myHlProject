package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.HlContactRenheMember;
import com.itcalf.renhe.bean.HlContactRenheMemberItem;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.view.PinnedSectionListView.PinnedSectionListAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author chan
 * @createtime 2014-10-20
 * @功能说明 联系人列表适配器带字母
 */
public class HlContactRenheMemberContactArrayAdapter extends ArrayAdapter<HlContactRenheMemberItem> implements PinnedSectionListAdapter {
    public final static String IS_NEWFRIEND_ID = "new";
    public final static String IS_CIRCLE_ID = "circle";
    public final static String IS_CONTACT_ID = "contact";
    private ImageLoader imageLoader;

    public HlContactRenheMemberContactArrayAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        imageLoader = ImageLoader.getInstance();
    }

    protected void prepareSections(int sectionsNumber) {
    }

    // 列表添加项
    protected void onSectionAdded(HlContactRenheMemberItem section, int sectionPosition) {
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            //标题栏
            viewHolder.title_txt = (LinearLayout) view.findViewById(R.id.title_txt_container);
            viewHolder.titleTxt = (TextView) view.findViewById(R.id.title_txt);
            //新的好友
            viewHolder.newfitem_ll = (RelativeLayout) view.findViewById(R.id.newfitem_ll);
            viewHolder.newFri_txt = (TextView) view.findViewById(R.id.newFri_txt);
            viewHolder.newFri_numb = (TextView) view.findViewById(R.id.newFri_numb);
            viewHolder.contactDivider = view.findViewById(R.id.contact_divider);
            //圈子
            viewHolder.contact_separate_line = view.findViewById(R.id.contact_separate_line);
            viewHolder.myCircle_Rl = (RelativeLayout) view.findViewById(R.id.mycircle_Rl);
            viewHolder.myCircle_txt = (TextView) view.findViewById(R.id.mycircle_txt);
            viewHolder.myCircle_numb = (TextView) view.findViewById(R.id.mycircle_numb);
            //通讯录好友
            viewHolder.listitem_ll = (LinearLayout) view.findViewById(R.id.listitem_ll);
            viewHolder.avatarView = (ImageView) view.findViewById(R.id.avatar_img);// 头像
            viewHolder.avatar_txt = (TextView) view.findViewById(R.id.avatar_txt);
            viewHolder.username_txt = (TextView) view.findViewById(R.id.username_txt);// 联系人名字
            viewHolder.vipIv = (ImageView) view.findViewById(R.id.vipImage);// Vip标志
            viewHolder.realNameIv = (ImageView) view.findViewById(R.id.realnameImage);// 实名标志
            viewHolder.job_txt = (TextView) view.findViewById(R.id.job_txt);// 联系人工作
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        HlContactRenheMemberItem item = getItem(position);
        if (item.type == HlContactRenheMemberItem.SECTION) {
            viewHolder.listitem_ll.setVisibility(View.GONE);
            if (item.id.equals(IS_NEWFRIEND_ID) || item.id.equals(IS_CIRCLE_ID) || item.id.equals(IS_CONTACT_ID)) {
                viewHolder.title_txt.setVisibility(View.GONE);
            } else {
                viewHolder.title_txt.setVisibility(View.VISIBLE);
                if (viewHolder.titleTxt.getText().toString().equals(Constants.COMMONCONTACT)) {
                    viewHolder.titleTxt.setText("常用联系人");
                }
            }
            viewHolder.newfitem_ll.setVisibility(View.GONE);
            viewHolder.contact_separate_line.setVisibility(View.GONE);
            viewHolder.myCircle_Rl.setVisibility(View.GONE);
        } else {
            viewHolder.title_txt.setVisibility(View.GONE);
            if (item.id.equals(IS_NEWFRIEND_ID)) {
                viewHolder.listitem_ll.setVisibility(View.GONE);
                viewHolder.newfitem_ll.setVisibility(View.VISIBLE);//移除新朋友
                viewHolder.contact_separate_line.setVisibility(View.GONE);
                viewHolder.myCircle_Rl.setVisibility(View.GONE);

                String tx = item.getText().toString();
                String numb;
                if (!tx.isEmpty() && tx.contains("(")) {
                    numb = tx.substring(tx.indexOf("(") + 1, tx.indexOf(")"));
                    if (numb.length() > 2) {
                        numb = "99";
                    }
                    viewHolder.newFri_txt.setText(tx.substring(0, tx.indexOf("(")));
                    viewHolder.newFri_numb.setText(numb);
                } else {
                    viewHolder.newFri_txt.setText(tx);
                    viewHolder.newFri_numb.setVisibility(View.GONE);
                }
            } else if (item.id.equals(IS_CIRCLE_ID)) {
                viewHolder.listitem_ll.setVisibility(View.GONE);
                viewHolder.newfitem_ll.setVisibility(View.GONE);
                viewHolder.contact_separate_line.setVisibility(View.VISIBLE);
                viewHolder.myCircle_Rl.setVisibility(View.VISIBLE);
                String tx = item.getText().toString();
                if (!tx.isEmpty() && tx.contains("(n)")) {
                    viewHolder.myCircle_txt.setText("");
                } else {
                    viewHolder.myCircle_txt.setText(tx);
                    viewHolder.myCircle_numb.setVisibility(View.GONE);
                }
            } else if (item.id.equals(IS_CONTACT_ID)) {
                viewHolder.listitem_ll.setVisibility(View.GONE);
                viewHolder.newfitem_ll.setVisibility(View.GONE);
                viewHolder.contact_separate_line.setVisibility(View.GONE);
                viewHolder.myCircle_Rl.setVisibility(View.GONE);
            } else {
                viewHolder.listitem_ll.setVisibility(View.VISIBLE);
                viewHolder.newfitem_ll.setVisibility(View.GONE);
                viewHolder.contact_separate_line.setVisibility(View.GONE);
                viewHolder.myCircle_Rl.setVisibility(View.GONE);
                //4 0123
                if (position < getCount() - 1) {
                    HlContactRenheMemberItem tmpItem = getItem(position + 1);
                    if (tmpItem.type == HlContactRenheMemberItem.SECTION)
                        viewHolder.contactDivider.setVisibility(View.GONE);
                    else {
                        viewHolder.contactDivider.setVisibility(View.VISIBLE);
                    }
                } else {
                    viewHolder.contactDivider.setVisibility(View.GONE);
                }
            }
            //数据加载
            HlContactRenheMember ct = item.getHlContactRenheMember();
            if (null != ct) {
                /**0: 好友;1:手机通讯录;2:名片;3:手机通讯录和名片中非好友的Renhe会员**/
                int type = 3;

                String userface = ct.getUserface();
                int accountType = ct.getAccountType();
                boolean isRealName = ct.isRealname();
                String username = ct.getName();
                String userjob = ct.getTitle();
                String userCompany = ct.getCompany();
                String shortName = ct.getName();
                int colorIndex = 0;

                // 头像显示
                switch (type) {
                    case 0:
                    case 3:
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
                    case 1:
                    case 2:
                        viewHolder.avatarView.setVisibility(View.GONE);
                        viewHolder.avatar_txt.setVisibility(View.VISIBLE);
                        viewHolder.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                        viewHolder.avatar_txt.setText(shortName);
                        break;
                    default:
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
                switch (accountType) {
                    case 0:
                        viewHolder.vipIv.setVisibility(View.GONE);
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
            }
        }
        return view;
    }

    public class ViewHolder {
        //标题栏
        LinearLayout title_txt;
        TextView titleTxt;
        //新的好友
        RelativeLayout newfitem_ll;
        TextView newFri_txt;
        public TextView newFri_numb;
        View contactDivider;
        //圈子
        View contact_separate_line;
        RelativeLayout myCircle_Rl;
        TextView myCircle_txt;
        TextView myCircle_numb;
        //通讯录好友
        LinearLayout listitem_ll;
        ImageView avatarView;// 头像
        TextView avatar_txt;
        TextView username_txt;// 联系人名字
        ImageView vipIv;// Vip标志
        ImageView realNameIv;// 实名标志
        TextView job_txt;// 联系人工作
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == HlContactRenheMemberItem.SECTION;
    }

}
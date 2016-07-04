package com.itcalf.renhe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.NFuserInfo;
import com.itcalf.renhe.bean.NewFriendsInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.innermsg.ReceiveFriend;
import com.itcalf.renhe.dto.ReceiveAddFriend;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/11/26
 */
public class GuessInterestAdapter extends BaseAdapter {
    Context context;
    List<NewFriendsInfo> contactslist;
    Button accept_btn;
    TextView added_txt;
    int inviteType = 0;
    int inviteId = 0;
    int fromType = 0;
    private ImageLoader imageLoader;

    public GuessInterestAdapter(Context _cxt, List<NewFriendsInfo> _cl) {
        this.context = _cxt;
        this.contactslist = _cl;
        this.imageLoader = ImageLoader.getInstance();
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
    public View getView(final int position, View convertView, ViewGroup arg2) {
        ViewHolder viewhold;
        if (convertView == null) {
            viewhold = new ViewHolder();
            convertView = (View) LayoutInflater.from(context).inflate(R.layout.newfriend_listitem, null);
            viewhold.listitem_ll = (RelativeLayout) convertView.findViewById(R.id.listitem_rl);
            viewhold.avatarView = (ImageView) convertView.findViewById(R.id.avatar_img);// 头像
            viewhold.avatar_txt = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewhold.username_txt = (TextView) convertView.findViewById(R.id.username_txt);// 联系人名字
            viewhold.comefromTv = (TextView) convertView.findViewById(R.id.comefromTv);
            viewhold.comefromTv.setVisibility(View.GONE);
            viewhold.vipIv = (ImageView) convertView.findViewById(R.id.vipImage);// Vip标志
            viewhold.realNameIv = (ImageView) convertView.findViewById(R.id.realnameImage);// 实名标志
            viewhold.addpurpose_txt = (TextView) convertView.findViewById(R.id.addpurpose_txt);// 添加目的
            viewhold.addmessage_txt = (TextView) convertView.findViewById(R.id.addmessage_txt);// 添加请求语句
            viewhold.accept_btn = (Button) convertView.findViewById(R.id.accept_btn);
            viewhold.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            viewhold.diliver = (View) convertView.findViewById(R.id.newfriend_item_diliver);
            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }

        if (position == contactslist.size() - 1) {
            viewhold.diliver.setVisibility(View.GONE);
        } else {
            viewhold.diliver.setVisibility(View.VISIBLE);
        }

        final NewFriendsInfo ct = contactslist.get(position);
        if (null != ct) {
            final int status = ct.getStatus();// 添加好友的邀请状态：0代表未处理，可以接受邀请；1代表已处理，已接受邀请；2代表已处理，已拒绝邀请
            inviteType = ct.getInviteType();//邀请类型，用于接受邀请的接口调用
            String purpose = ct.getPurpose();// 添加好友的目的
            fromType = ct.getFromType();
            final NFuserInfo userInfo = ct.getUserInfo();// 会员信息
            if (null != userInfo) {
                final String userName = userInfo.getName();
                String userface = userInfo.getUserface();
                int accountType = userInfo.getAccountType();
                boolean isRealName = userInfo.isRealName();
                String title = userInfo.getTitle();
                String company = userInfo.getCompany();
                String industry = userInfo.getIndustry();

                int type = userInfo.getType();
                int colorIndex = userInfo.getColorIndex();
                String shortName = userInfo.getShortName();
                switch (type) {
                    case 1:
                    case 2:
                        viewhold.avatarView.setVisibility(View.GONE);
                        viewhold.avatar_txt.setVisibility(View.VISIBLE);
                        viewhold.avatar_txt.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                        viewhold.avatar_txt.setText(shortName);
                        break;
                    case 3:
                    default:
                        viewhold.avatarView.setVisibility(View.VISIBLE);
                        viewhold.avatar_txt.setVisibility(View.GONE);
                        // 头像显示
                        if (!TextUtils.isEmpty(userface)) {
                            try {
                                imageLoader.displayImage(userface, viewhold.avatarView, CacheManager.circleImageOptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
                // 名字
                viewhold.username_txt.setText(userName);

                // Vip标志显示
                switch (accountType) {
                    case 0:
                        viewhold.vipIv.setVisibility(View.GONE);
                        break;
                    case 1:
                        viewhold.vipIv.setVisibility(View.VISIBLE);
                        viewhold.vipIv.setImageResource(R.drawable.archive_vip_1);
                        break;
                    case 2:
                        viewhold.vipIv.setVisibility(View.VISIBLE);
                        viewhold.vipIv.setImageResource(R.drawable.archive_vip_2);
                        break;
                    case 3:
                        viewhold.vipIv.setVisibility(View.VISIBLE);
                        viewhold.vipIv.setImageResource(R.drawable.archive_vip_3);
                        break;

                    default:
                        viewhold.vipIv.setVisibility(View.GONE);
                        break;
                }
                // 实名标志
                if (isRealName && accountType <= 0) {
                    viewhold.realNameIv.setVisibility(View.VISIBLE);
                    viewhold.realNameIv.setImageResource(R.drawable.archive_realname);
                } else {
                    viewhold.realNameIv.setVisibility(View.GONE);
                }

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(company)) {
                    viewhold.addpurpose_txt.setText(title + company);//修改成职位加公司
                } else {
                    viewhold.addpurpose_txt.setText(title + "/" + company);//修改成职位加公司
                }
                viewhold.addpurpose_txt.setVisibility(TextUtils.isEmpty(title + company) ? View.GONE : View.VISIBLE);
                accept_btn = viewhold.accept_btn;
                added_txt = viewhold.added_txt;
                accept_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        inviteId = ct.getId();
                        inviteType = ct.getInviteType();// 邀请类型，用于接受邀请的接口调用
                        if (status == 0) {//接受邀请
                            MobclickAgent.onEvent(context, context.getResources().getString(R.string.newFriend_guessInterest_item_received));
                            acceptAction(position);
                        } else if (status == 4) {//去添加
                            MobclickAgent.onEvent(context, context.getResources().getString(R.string.newFriend_guessInterest_item_add));
                            inviteAction(position, fromType, userInfo);
                        }
                    }
                });
                // 按钮类型
                switch (status) {
                    case 0:// 0代表未处理，可以接受邀请；
                        viewhold.accept_btn.setVisibility(View.VISIBLE);
                        viewhold.accept_btn.setBackgroundResource(R.drawable.blue_im_chat_bt_selected);
                        ColorStateList csl = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                        viewhold.accept_btn.setTextColor(csl);
                        viewhold.accept_btn.setText("接受");
                        viewhold.added_txt.setVisibility(View.GONE);
                        break;
                    case 1:// 1代表已处理，已接受邀请；
                        viewhold.accept_btn.setVisibility(View.GONE);
                        viewhold.added_txt.setVisibility(View.VISIBLE);
                        viewhold.added_txt.setText("已添加");
                        break;
                    case 2:// 2代表已处理，已拒绝邀请
                        viewhold.accept_btn.setVisibility(View.GONE);
                        viewhold.added_txt.setVisibility(View.VISIBLE);
                        viewhold.added_txt.setText("已拒绝");
                        break;
                    case 3:// 3代表已添加，等待验证
                        viewhold.accept_btn.setVisibility(View.GONE);
                        viewhold.added_txt.setVisibility(View.VISIBLE);
                        viewhold.added_txt.setText("已邀请");
                        break;
                    case 4:// 4代表 添加
                        viewhold.accept_btn.setVisibility(View.VISIBLE);
                        viewhold.accept_btn.setBackgroundResource(R.drawable.btn_bp3_rectangle_shape);
                        ColorStateList csl1 = context.getResources().getColorStateList(R.color.archive_editbt_textcolor_selected);
                        viewhold.accept_btn.setTextColor(csl1);
                        viewhold.accept_btn.setText("添加");
                        viewhold.added_txt.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                // 添加的消息
                viewhold.addmessage_txt.setText(industry);//修改成行业
                viewhold.addmessage_txt.setVisibility(TextUtils.isEmpty(industry) ? View.GONE : View.VISIBLE);
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        RelativeLayout listitem_ll;
        ImageView avatarView;
        TextView avatar_txt;
        TextView username_txt;
        TextView comefromTv;
        ImageView vipIv;
        ImageView realNameIv;
        TextView addpurpose_txt;
        TextView addmessage_txt;
        public Button accept_btn;
        public TextView added_txt;
        View diliver;
    }

    //接受好友邀请
    void acceptAction(final int position) {
        new ReceiveFriend(context) {
            @Override
            public void doPre() {
                ((Activity) context).showDialog(2);
            }

            @Override
            public void doPost(ReceiveAddFriend result) {
                ((Activity) context).removeDialog(2);
                if (result == null) {
                    ToastUtil.showErrorToast(context, context.getString(R.string.connect_server_error));
                } else if (result.getState() == 1) {
                    contactslist.get(position).setStatus(1);
                    notifyDataSetChanged();
                    //更新本地联系人列表，maxid
                    new ContactsUtil(context).SyncContacts();

                } else if (result.getState() == -1) {
                    ToastUtil.showErrorToast(context, R.string.lack_of_privilege);
                } else if (result.getState() == -2) {
                    ToastUtil.showErrorToast(context, R.string.sorry_of_unknow_exception);
                } else if (result.getState() == -3) {
                    ToastUtil.showErrorToast(context, "邀请序号不存在！");
                } else if (result.getState() == -4) {
                    ToastUtil.showErrorToast(context, "邀请类型不存在！");
                } else if (result.getState() == -5) {
                    ToastUtil.showErrorToast(context, "接受类型不存在！");
                } else if (result.getState() == -6) {
                    ToastUtil.showErrorToast(context, R.string.no_permission_do);
                } else if (result.getState() == -7) {
                    ToastUtil.showErrorToast(context, "您已经通过该请求了！");
                } else if (result.getState() == -8) {
                    ToastUtil.showErrorToast(context, "您已经拒绝过该请求！");
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), "" + inviteId, "" + inviteType, "true");
    }

    //去添加
    void inviteAction(final int position, int fromType, NFuserInfo userInfo) {
        Intent i = new Intent(context, AddFriendAct.class);
        i.putExtra("mSid", userInfo.getSid());
        i.putExtra("friendName", userInfo.getName());
        i.putExtra("from", fromType);
        i.putExtra("addfriend_from", "guessInterest");
        i.putExtra("position", position);
        context.startActivity(i);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
}

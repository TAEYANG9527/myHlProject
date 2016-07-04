package com.itcalf.renhe.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.ContactResultByMailBox.MailBoxContact;
import com.itcalf.renhe.bean.RenheMemberInfo;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.contacts.MailBoxList;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chan
 * @createtime 2015-1-7
 * @功能说明 邮箱联系人适配器
 */
public class MailBoxListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<MailBoxContact> list;
    private Context context;

    //	private int[] avatarbg = { R.drawable.btn_bg_color5c6bc0_circular_solid_style,
    //			R.drawable.btn_bg_color5ec9f6_circular_solid_style, R.drawable.btn_bg_color78c06e_circular_solid_style,
    //			R.drawable.btn_bg_colorf65e5e_circular_solid_style, R.drawable.btn_bg_colorf65e8d_circular_solid_style,
    //			R.drawable.btn_bg_colorff8e6d_circular_solid_style, R.drawable.btn_bg_colorf6bf26_circular_solid_style,
    //			R.drawable.btn_bg_colorc5cb63_circular_solid_style };

    public MailBoxListAdapter(Context context, List<MailBoxContact> list) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.mobile_contacts_list_item, null);
            holder = new ViewHolder();
            holder.contactAvatarFl = (FrameLayout) convertView.findViewById(R.id.contactAvatar_imgFl);
            holder.avatarTv = (TextView) convertView.findViewById(R.id.contactAvatar_Tv);
            holder.alpha = (TextView) convertView.findViewById(R.id.title_txt);
            holder.avatar = (ImageView) convertView.findViewById(R.id.contactAvatar_img);
            holder.name = (TextView) convertView.findViewById(R.id.contactName_txt);
            holder.isrenheIcon = (ImageView) convertView.findViewById(R.id.isrenheIcon);
            holder.number = (TextView) convertView.findViewById(R.id.contactPhone_txt);
            holder.add_btn = (Button) convertView.findViewById(R.id.add_btn);
            holder.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MailBoxContact cv = list.get(position);
        if (cv != null) {
            String firstName = "";
            String name = cv.getName().trim();
            String email = cv.getEmail();
            boolean isRenheMember = cv.isRenheMember();
            if (isRenheMember) {
                holder.isrenheIcon.setVisibility(View.VISIBLE);
                holder.added_txt.setVisibility(View.VISIBLE);
                holder.add_btn.setVisibility(View.VISIBLE);
                holder.add_btn.setBackgroundResource(R.drawable.btn_bg_color36d67b_rectangle_empty_corner_style);
                holder.add_btn.setTextColor(0xff36d67b);

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
            } else {
                holder.isrenheIcon.setVisibility(View.GONE);
                holder.added_txt.setVisibility(View.GONE);
                holder.add_btn.setVisibility(View.VISIBLE);
                holder.add_btn.setText("邀请");
                holder.add_btn.setBackgroundResource(R.drawable.btn_bg_color2292dc_rectangle_empty_corner_style);
                holder.add_btn.setTextColor(0xff2292dc);
            }

            //头像随机背景
            //			int count = (int) (Math.random() * avatarbg.length);//取0-length的随机数;
            //			holder.avatar.setBackgroundResource(avatarbg[count]);

            if (name.length() > 0) {
                //判断name是否含中文
                if (name.length() == 1) {
                    firstName = name;
                } else {
                    if (isContainChinese(name)) {
                        firstName = name.substring(name.length() - 2, name.length());
                    } else {
                        firstName = name.substring(0, 2);
                    }
                }
                holder.avatarTv.setText(firstName);
                holder.name.setText(name);
            }
            holder.number.setText(email);

            String currentStr = MailBoxList.getAlpha(cv);
            String previewStr = (position - 1) >= 0 ? MailBoxList.getAlpha(list.get(position - 1)) : " ";
            if (!previewStr.equals(currentStr)) {
                holder.alpha.setVisibility(View.VISIBLE);
                if ("#".equals(currentStr)) {
                    currentStr = "和聊会员";
                }
                holder.alpha.setText(currentStr);
            } else {
                holder.alpha.setVisibility(View.GONE);
            }

            holder.add_btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //发邮件
                    if (!list.get(position).isRenheMember()) {
                        String email = list.get(position).getEmail();
                        if (email.length() == 0) {
                            ToastUtil.showErrorToast(context, context.getResources().getString(R.string.mailnotnull));
                            return;
                        }
                        if (!email.matches("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")
                                && !email.matches("^(13[0-9]|15[0-9]|18[0-9])\\d{8}$")) {
                            ToastUtil.showErrorToast(context, R.string.error_mail_format);
                            return;
                        }
                        //						String content = "【人和网】您的好友" + RenheApplication.getInstance().getUserInfo().getName()
                        //								+ "邀请您加入他的人脉圈。点击下载客户端" + context.getResources().getString(R.string.app_download) + "马上加入。";
                        String content = context.getResources().getString(R.string.app_download);

                        Intent data = new Intent(Intent.ACTION_SENDTO);
                        data.setData(Uri.parse("mailto:" + email + ""));
                        data.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.mail_list_source_from_hl));
                        data.putExtra(Intent.EXTRA_TEXT, content);
                        context.startActivity(Intent.createChooser(data, "Choose Email Client"));
                        //						Intent emailintent = new Intent(android.content.Intent.ACTION_SEND);
                        //						emailintent.setType("plain/text");
                        //						String[] emailReciver = new String[] { email };
                        //						//设置邮件默认地址
                        //						emailintent.putExtra(android.content.Intent.EXTRA_EMAIL, emailReciver);
                        //						//设置邮件默认标题
                        //						emailintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "来自人和网的好友邀请");
                        //						//设置要默认发送的内容
                        //						emailintent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                        //						//调用系统的邮件系统
                        //						context.startActivity(Intent.createChooser(emailintent, "请选择邮件发送软件"));
                    }
                    //发送添加信息
                    else {
                        RenheMemberInfo memberInfo = list.get(position).getRenheMemberInfo();
                        if (memberInfo != null) {
                            String sid = memberInfo.getMemberSId();
                            String name = list.get(position).getName().trim();
                            Intent i = new Intent(context, AddFriendAct.class);
                            i.putExtra("mSid", sid);
                            i.putExtra("friendName", name);
                            i.putExtra("position", position);
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
        ImageView avatar;
        TextView name;
        TextView number;
        public Button add_btn;
        public TextView added_txt;
        FrameLayout contactAvatarFl;
        TextView avatarTv;
        ImageView isrenheIcon;
    }

    /**
     * 判断是否包含中文
     */
    public boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}

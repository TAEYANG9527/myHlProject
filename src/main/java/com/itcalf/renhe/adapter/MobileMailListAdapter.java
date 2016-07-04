package com.itcalf.renhe.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ContactsReturn.ContactResult;
import com.itcalf.renhe.bean.RenheMemberInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendAct;
import com.itcalf.renhe.context.contacts.MobileMailList;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author chan
 * @createtime 2015-1-7
 * @功能说明 联系人适配器
 */
public class MobileMailListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<ContactResult> list;
    private Context context;
    private String pageEndChar = " ";
    private ImageLoader imageLoader;

    public MobileMailListAdapter(Context context, List<ContactResult> list, String pageEndChar) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.pageEndChar = pageEndChar;
        imageLoader = ImageLoader.getInstance();
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
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.mobile_contacts_list_item, null);
            holder = new ViewHolder();
            holder.listitem_ll = (LinearLayout) convertView.findViewById(R.id.listitem_ll);
            holder.alpha = (TextView) convertView.findViewById(R.id.title_txt);
            holder.contactAvatarRl = (RelativeLayout) convertView.findViewById(R.id.avatar_img_ll);
            holder.avatar = (ImageView) convertView.findViewById(R.id.contactAvatar_img);
            holder.avatarTv = (TextView) convertView.findViewById(R.id.avatar_txt);
            holder.name = (TextView) convertView.findViewById(R.id.contactName_txt);
            holder.isrenheIcon = (ImageView) convertView.findViewById(R.id.isrenheIcon);
            holder.number = (TextView) convertView.findViewById(R.id.contactPhone_txt);
            holder.add_btn = (Button) convertView.findViewById(R.id.add_btn);
            holder.added_txt = (TextView) convertView.findViewById(R.id.added_txt);
            holder.check_CB = (CheckBox) convertView.findViewById(R.id.check_CB);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ContactResult cv = list.get(position);
        if (cv != null) {
            int state = cv.getSelectState();
            final String name = cv.getName().trim();
            String[] mobile = cv.getMobileItems();
            String[] telephones = cv.getTelephoneItems();
            String[] otherphones = cv.getTelephoneOtherItems();
            boolean isRenheMember = cv.isRenheMember();
            int colorIndex = cv.getColorIndex();
            String shortName = cv.getShortName();

            if (isRenheMember) {
                holder.check_CB.setVisibility(View.GONE);
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
                    holder.number.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(company) && !TextUtils.isEmpty(title)) {
                        holder.number.setText(title + " / " + company);
                    } else if (TextUtils.isEmpty(company) && TextUtils.isEmpty(title)) {
                        if (mobile.length > 0) {
                            holder.number.setText(mobile[0]);
                        } else if (telephones.length > 0) {
                            holder.number.setText(telephones[0]);
                        } else if (otherphones.length > 0) {
                            holder.number.setText(otherphones[0]);
                        } else {
                            holder.number.setVisibility(View.GONE);
                        }
                    } else {
                        holder.number.setText(title + company);
                    }
                    boolean isSelf = cv.getRenheMemberInfo().isSelf();//是否是自己
                    boolean isConnection = cv.getRenheMemberInfo().isConnection();//是否是好友
                    boolean isInvite = cv.getRenheMemberInfo().isInvite();
                    if (isSelf) {
                        holder.add_btn.setVisibility(View.GONE);
                        holder.added_txt.setVisibility(View.GONE);
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
                if (state == 0) {
                    holder.check_CB.setVisibility(View.GONE);
                } else if (state == 1) {
                    holder.check_CB.setVisibility(View.VISIBLE);
                    holder.check_CB.setSelected(true);
                } else {
                    holder.check_CB.setVisibility(View.VISIBLE);
                    holder.check_CB.setSelected(false);
                }
                holder.avatar.setVisibility(View.GONE);
                holder.avatarTv.setVisibility(View.VISIBLE);
                holder.avatarTv.setBackgroundResource(Constants.AVATARBG[colorIndex]);
                holder.avatarTv.setText(shortName);

                holder.number.setVisibility(View.VISIBLE);
                if (mobile.length > 0) {
                    holder.number.setText(mobile[0]);
                } else if (telephones.length > 0) {
                    holder.number.setText(telephones[0]);
                } else if (otherphones.length > 0) {
                    holder.number.setText(otherphones[0]);
                } else {
                    holder.number.setVisibility(View.GONE);
                }
            }
            holder.name.setText(name);

            String currentStr = MobileMailList.getAlpha(cv);
            String previewStr = (position - 1) >= 0 ? MobileMailList.getAlpha(list.get(position - 1)) : pageEndChar;
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
                    //发短信
                    if (!list.get(position).isRenheMember()) {
                        String[] mobile = list.get(position).getMobileItems();
                        if (mobile.length > 0) {
                            String m = mobile[0];
                            //调接口发短信
                            new AsyncTask<String, Void, MessageBoardOperation>() {
                                @Override
                                protected MessageBoardOperation doInBackground(String... params) {
                                    Map<String, Object> reqParams = new HashMap<>();
                                    reqParams.put("sid", params[0]);
                                    reqParams.put("adSId", params[1]);
                                    reqParams.put("mobile", params[2]);
                                    reqParams.put("userName", params[3]);
                                    try {
                                        return (MessageBoardOperation) HttpUtil.doHttpRequest(
                                                Constants.Http.INVITE_MOBILE_CONTACTS, reqParams, MessageBoardOperation.class,
                                                null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                }

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                }

                                @Override
                                protected void onPostExecute(MessageBoardOperation result) {
                                    super.onPostExecute(result);
                                    if (result != null) {
                                        switch (result.getState()) {
                                            case 1:
                                                ToastUtil.showToast(context, "已发送短信邀请");
                                                break;
                                            case -2:
                                                ToastUtil.showToast(context, "手机号码为空");
                                                break;
                                            case -4:
                                                ToastUtil.showToast(context, "手机号码格式不对");
                                                break;
                                            case -5:
                                                ToastUtil.showToast(context, "你今天已经邀请过好友");
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }
                            }.executeOnExecutor(Executors.newCachedThreadPool(),
                                    RenheApplication.getInstance().getUserInfo().getSid(),
                                    RenheApplication.getInstance().getUserInfo().getAdSId(), m, name);
                            //邀请按钮点击的日志统计
                            String writeContent = "5.2|1|" + System.currentTimeMillis() + "|"
                                    + RenheApplication.getInstance().getUserInfo().getSid() + "|" + m + "|"
                                    + list.get(position).getName();
                            LoggerFileUtil.writeFile(writeContent, true);
                        } else {
                            ToastUtil.showErrorToast(context, R.string.error_mobile_format);
                        }
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
                            i.putExtra("addfriend_from", "mobile");
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
        TextView name;
        TextView number;
        public Button add_btn;
        public TextView added_txt;
        RelativeLayout contactAvatarRl;
        ImageView avatar;
        TextView avatarTv;
        ImageView isrenheIcon;
        public CheckBox check_CB;
        LinearLayout listitem_ll;
    }
}

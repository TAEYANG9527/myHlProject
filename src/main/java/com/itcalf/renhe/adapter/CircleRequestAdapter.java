package com.itcalf.renhe.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CircleJoinRequestListInfo;
import com.itcalf.renhe.bean.CircleJoinUserInfo;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.wukong.im.ActivityCircleJoinRequest;
import com.itcalf.renhe.utils.DateUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class CircleRequestAdapter extends BaseAdapter {
    private ArrayList<CircleJoinRequestListInfo> array = new ArrayList<>();
    private Context mContext;

    public CircleRequestAdapter(Context context, ArrayList<CircleJoinRequestListInfo> array, String userConversationName,
                                String imConversationId) {
        this.mContext = context;
        this.array = array;
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.activity_circle_apply_listitem, null, false);
            vh.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            vh.tx_company = (TextView) convertView.findViewById(R.id.tx_company);
            vh.tx_name = (TextView) convertView.findViewById(R.id.tx_name);
            vh.tx_city = (TextView) convertView.findViewById(R.id.tx_city);
            vh.tx_title = (TextView) convertView.findViewById(R.id.tx_title);
            vh.bt_consent = (Button) convertView.findViewById(R.id.bt_consent);
            vh.bt_forget = (Button) convertView.findViewById(R.id.bt_forget);
            vh.ll_user_detail = (LinearLayout) convertView.findViewById(R.id.ly_detail);
            vh.tx_time = (TextView) convertView.findViewById(R.id.tx_time);
            vh.setOnClick();
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.pos = position;
        CircleJoinUserInfo memberInfo = array.get(position).getMemberInfo();
        CircleJoinUserInfo invitationMemberInfo = array.get(position).getInvitationMemberInfo();
        if (memberInfo != null) {
            vh.tx_name.setText(memberInfo.getName());
            String title = memberInfo.getTitle();
            title = TextUtils.isEmpty(title) ? "" : title;
            String company = memberInfo.getCompany();
            company = TextUtils.isEmpty(company) ? "" : company;
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(company)) {
                vh.tx_company.setText(title + "" + company);
            } else {
                vh.tx_company.setText(title + " / " + company);
            }
            vh.tx_time.setText(DateUtil.getFormatDate(Long.parseLong(array.get(position).getCreatedDate()), "MM月d日"));
            vh.tx_title.setText("附言:" + (array.get(position).isInvitationMemberExists()
                    ? (invitationMemberInfo.getName() + "邀请" + memberInfo.getName() + "加入圈子") : array.get(position).getPurpose()));
            if (TextUtils.isEmpty(vh.tx_title.getText().toString().trim())) {
                vh.tx_title.setVisibility(View.GONE);
            } else {
                vh.tx_title.setVisibility(View.VISIBLE);
            }
            ImageLoader.getInstance().displayImage(memberInfo.getUserfaceUrl(), vh.iv_avatar, CacheManager.circleImageOptions);
            int state = array.get(position).getApproveState();
            switch (state) {
                case 0://0代表未处理
                    vh.bt_consent.setText("同意");
                    vh.bt_consent.setEnabled(true);
                    vh.bt_consent.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.import_btn_shape));
                    vh.bt_consent.setTextColor(mContext.getResources().getColorStateList(R.color.cl_textcolor_selected));
                    vh.bt_forget.setVisibility(View.VISIBLE);
                    break;
                case 1://1代表申请通过
                    vh.bt_consent.setText("已通过");
                    vh.bt_consent.setEnabled(false);
                    vh.bt_consent.setBackgroundDrawable(null);
                    vh.bt_consent.setTextColor(mContext.getResources().getColor(R.color.C2));
                    vh.bt_forget.setVisibility(View.GONE);
                    break;
                case 2://2代表申请被拒绝
                    vh.bt_consent.setText("已忽略");
                    vh.bt_consent.setEnabled(false);
                    vh.bt_consent.setBackgroundDrawable(null);
                    vh.bt_consent.setTextColor(mContext.getResources().getColor(R.color.C2));
                    vh.bt_forget.setVisibility(View.GONE);
                    break;
            }
        }
        return convertView;
    }

    class ViewHolder {
        private int pos;

        private ImageView iv_avatar;
        private TextView tx_name;
        private TextView tx_company;
        private TextView tx_city;
        private TextView tx_title;
        public Button bt_consent;
        public LinearLayout ll_join_result;
        public LinearLayout ll_user_detail;
        private TextView tx_time;
        private Button bt_forget;

        void setOnClick() {
            //同意
            bt_consent.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityCircleJoinRequest) mContext).approveCircleJoinRequest(1, array.get(pos).getId(), pos);
                }
            });
            //忽略
            bt_forget.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ActivityCircleJoinRequest) mContext).approveCircleJoinRequest(2, array.get(pos).getId(), pos);
                }
            });
            iv_avatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MyHomeArchivesActivity.class);
                    intent.putExtra("name", array.get(pos).getMemberInfo().getName());
                    intent.putExtra("flag", true);
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[7]);
                    intent.putExtra("openId", Long.parseLong(String.valueOf(array.get(pos).getMemberInfo().getImId())));
                    mContext.startActivity(intent);
                    ((Activity) mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            });
            ll_user_detail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    iv_avatar.performClick();
                }
            });
        }
    }
}

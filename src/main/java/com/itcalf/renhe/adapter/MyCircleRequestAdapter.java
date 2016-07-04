package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CirCleInfo;
import com.itcalf.renhe.bean.CircleJoinRequestListInfo;
import com.itcalf.renhe.bean.CircleJoinUserInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class MyCircleRequestAdapter extends BaseAdapter {
	private ArrayList<CircleJoinRequestListInfo> array = new ArrayList<CircleJoinRequestListInfo>();
	private Context mContext;
	private float x_scale = 1f, y_scale = 1f;

	public MyCircleRequestAdapter(Context context, ArrayList<CircleJoinRequestListInfo> array) {
		this.mContext = context;
		this.array = array;
		this.x_scale = 1f * context.getResources().getDisplayMetrics().widthPixels / 750;
		this.y_scale = 1f * context.getResources().getDisplayMetrics().heightPixels / 1335;
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
			LinearLayout.LayoutParams iv_avatarParams = new LinearLayout.LayoutParams((int) (130 * x_scale),
					(int) (130 * x_scale));
			iv_avatarParams.setMargins((int) (20 * x_scale), (int) (20 * x_scale), (int) (20 * x_scale), 0);

			LinearLayout.LayoutParams ly_auditParams = new LinearLayout.LayoutParams((int) (130 * x_scale),
					LayoutParams.MATCH_PARENT);
			ly_auditParams.gravity = Gravity.CENTER;

			LinearLayout.LayoutParams tx_nameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			tx_nameParams.setMargins(0, (int) (25 * x_scale), 0, (int) (10 * x_scale));

			LinearLayout.LayoutParams tx_cityParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			tx_cityParams.setMargins(0, (int) (10 * x_scale), 0, (int) (10 * x_scale));

			LinearLayout.LayoutParams tx_titleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			tx_titleParams.setMargins(0, 0, 0, (int) (25 * x_scale));

			vh = new ViewHolder();
			convertView = LayoutInflater.from(this.mContext).inflate(R.layout.activity_circle_join_request_list_item, null,
					false);
			vh.iv_avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
			vh.ly_audit = (LinearLayout) convertView.findViewById(R.id.ly_audit);
			vh.tx_join_type = (TextView) convertView.findViewById(R.id.tx_join_type);
			vh.tx_company = (TextView) convertView.findViewById(R.id.tx_company);
			vh.tx_name = (TextView) convertView.findViewById(R.id.tx_name);
			vh.tx_city = (TextView) convertView.findViewById(R.id.tx_city);
			vh.tx_title = (TextView) convertView.findViewById(R.id.tx_title);

			vh.iv_avatar.setLayoutParams(iv_avatarParams);
			vh.ly_audit.setLayoutParams(ly_auditParams);
			vh.tx_name.setLayoutParams(tx_nameParams);
			vh.tx_city.setLayoutParams(tx_cityParams);
			vh.tx_title.setLayoutParams(tx_titleParams);
			vh.setOnClick();
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.pos = position;
		CirCleInfo circleInfo = array.get(position).getCircleInfo();
		CircleJoinUserInfo memberInfo = array.get(position).getMemberInfo();
		CircleJoinUserInfo invitationMemberInfo = array.get(position).getInvitationMemberInfo();
		if (circleInfo != null) {
			vh.tx_name.setText("圈子名称 : " + circleInfo.getName());
			vh.tx_company.setText("圈号 : " + circleInfo.getId());
			vh.tx_city.setText("公告 : " + circleInfo.getNote());
			vh.tx_title.setText(array.get(position).isInvitationMemberExists()
					? (invitationMemberInfo.getName() + "邀请" + memberInfo.getName() + "加入圈子") : array.get(position).getPurpose());
			ImageLoader.getInstance().displayImage(circleInfo.getAvatar(), vh.iv_avatar);

			vh.tx_join_type.setText(array.get(position).getApproveState() != 0
					? (array.get(position).getApproveState() == 1 ? "已通过" : "被拒绝") : "未处理");
			vh.tx_join_type.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	class ViewHolder {
		private int pos;
		private ImageView iv_avatar;
		private TextView tx_join_type;
		private TextView tx_name;
		private TextView tx_company;
		private TextView tx_city;
		private TextView tx_title;
		private LinearLayout ly_audit;

		void setOnClick() {
			//			iv_avatar.setOnClickListener(new OnClickListener()
			//			{
			//				@Override
			//				public void onClick(View v)
			//				{
			//					Intent intent = new Intent(mContext, SummaryArchieveActivity.class);
			//					intent.putExtra("name", array.get(pos).getMemberInfo().getName());
			//					intent.putExtra("openId", array.get(pos).getMemberInfo().getImId());
			//					mContext.startActivity(intent);
			//					((Activity) mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			//				}
			//			});
		}
	}
}

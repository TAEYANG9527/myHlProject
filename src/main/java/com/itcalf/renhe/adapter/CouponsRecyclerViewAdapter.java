package com.itcalf.renhe.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.CouponsInfo;
import com.itcalf.renhe.context.more.CouponsListActivity;
import com.itcalf.renhe.context.more.CouponsOverdueListActivity;
import com.itcalf.renhe.utils.DateUtil;

import java.util.List;

/**
 * description :优惠券RecyclerView的适配
 * Created by Chans Renhenet
 * 2015/10/16
 */
public class CouponsRecyclerViewAdapter extends RecyclerView.Adapter {

    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_BODY = 0;

    private Context mContext;
    private List<CouponsInfo> mCouponsList;
    private boolean isOverdue;

    //构造函数
    public CouponsRecyclerViewAdapter(Context context, List<CouponsInfo> coupons, boolean isOverdue) {
        this.mContext = context;
        this.mCouponsList = coupons;
        this.isOverdue = isOverdue;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View footView = LayoutInflater.from(mContext).inflate(R.layout.coupons_more, parent, false);
            return new CouponsFootViewHolder(footView);
        } else if (viewType == TYPE_BODY) {
            if (isOverdue) {
                View inflatedView = LayoutInflater.from(mContext).inflate(R.layout.coupons_overdue_list_item, parent, false);
                return new CouponsViewHolder(inflatedView);
            } else {
                View inflatedView = LayoutInflater.from(mContext).inflate(R.layout.coupons_list_item, parent, false);
                return new CouponsViewHolder(inflatedView);
            }
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CouponsFootViewHolder) {
            if (isOverdue) {
                ((CouponsFootViewHolder) holder).couponNoneTip.setText(R.string.coupons_no_overdue);
                ((CouponsFootViewHolder) holder).overdueCoupon.setVisibility(View.GONE);
            } else {
                ((CouponsFootViewHolder) holder).couponNoneTip.setText(R.string.coupons_no_more);
                ((CouponsFootViewHolder) holder).overdueCoupon.setVisibility(View.VISIBLE);
            }
        } else if (holder instanceof CouponsViewHolder) {
            String cost = mCouponsList.get(position).getCost();
            String name = mCouponsList.get(position).getName();
            long expireDate = mCouponsList.get(position).getExpireDate();
            String expire = DateUtil.getFormatDate(expireDate, "yyyy.MM.dd");
            String description = mCouponsList.get(position).getDescribes();
            ((CouponsViewHolder) holder).couponAmount.setText(cost);
            ((CouponsViewHolder) holder).useConditions.setText(description);
            ((CouponsViewHolder) holder).validityPeriod.setText("有效期：" + expire);
//            ((CouponsViewHolder) holder).exchangeCode.setText(description);
        }
    }

    @Override
    public int getItemCount() {
        return mCouponsList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_BODY;
        }
    }

    //body 布局
    public class CouponsViewHolder extends RecyclerView.ViewHolder {
        TextView couponAmount;
        TextView useConditions;
        TextView validityPeriod;
        TextView exchangeCode;

        public CouponsViewHolder(View itemView) {
            super(itemView);
            couponAmount = (TextView) itemView.findViewById(R.id.coupon_amount);
            useConditions = (TextView) itemView.findViewById(R.id.coupon_use_conditions);
            validityPeriod = (TextView) itemView.findViewById(R.id.coupon_validity_period);
            exchangeCode = (TextView) itemView.findViewById(R.id.coupon_exchange_code);
        }
    }

    //foot 布局
    public class CouponsFootViewHolder extends RecyclerView.ViewHolder {
        TextView couponNoneTip;
        TextView overdueCoupon;


        public CouponsFootViewHolder(View itemView) {
            super(itemView);
            overdueCoupon = (TextView) itemView.findViewById(R.id.overdue_coupons_Tv);
            couponNoneTip = (TextView) itemView.findViewById(R.id.coupon_none_tip_Tv);
            overdueCoupon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, CouponsOverdueListActivity.class));
                    ((CouponsListActivity) mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            });
        }
    }
}




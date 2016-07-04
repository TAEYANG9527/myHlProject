package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.itcalf.renhe.R;

/**
 * Created by wangning on 2015/10/21.
 */
public class WalletHeaderViewHolder extends RenmaiQuanViewHolder {
    private com.itcalf.renhe.view.TextView moneyTv;
    private LinearLayout crashLl;

    public WalletHeaderViewHolder(final Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        moneyTv = (com.itcalf.renhe.view.TextView) itemView.findViewById(R.id.money_tv);
        crashLl = (LinearLayout) itemView.findViewById(R.id.crash_ll);
    }

    @Override
    public void initView(RecyclerHolder holder, Object item, int position) {
    }
}

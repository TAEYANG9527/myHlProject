package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.itcalf.renhe.R;

import java.util.List;

import cn.renhe.heliao.idl.money.trade.HeliaoTrade;

/**
 * 红包相关
 *
 * @author wangning
 * @createtime 2016-5-3
 * @功能说明 红包被抢详情列表适配器
 */
public class MyWalletDetailListAdapter extends BaseAdapter {
    private List<HeliaoTrade.TradeRecord> redRecordItems;
    private Context context;

    public MyWalletDetailListAdapter(Context _context, List<HeliaoTrade.TradeRecord> redRecordItems) {
        this.redRecordItems = redRecordItems;
        this.context = _context;
    }

    @Override
    public int getCount() {
        return redRecordItems.size();
    }

    @Override
    public Object getItem(int position) {
        return redRecordItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewhold = null;
        if (convertView == null) {
            viewhold = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.walletdetail_list_item, null);
            viewhold.nameTv = (TextView) convertView.findViewById(R.id.name_tv);
            viewhold.timeTv = (TextView) convertView.findViewById(R.id.time_tv);
            viewhold.moneyTv = (TextView) convertView.findViewById(R.id.money_tv);
            viewhold.stateTv = (TextView) convertView.findViewById(R.id.state_tv);
            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }
        HeliaoTrade.TradeRecord redRecordItem = (HeliaoTrade.TradeRecord) getItem(position);
        if (null != redRecordItem) {
            viewhold.nameTv.setText(redRecordItem.getTitle());
            viewhold.timeTv.setText(redRecordItem.getTime());
            if (redRecordItem.getType() == 0) {//是收入
                viewhold.moneyTv.setTextColor(context.getResources().getColor(R.color.luckymoney_detail_rob_level_color));
                viewhold.moneyTv.setText("+" + redRecordItem.getAmount() + "元");
            } else {//支出
                viewhold.moneyTv.setTextColor(context.getResources().getColor(R.color.C1));
                viewhold.moneyTv.setText("-" + redRecordItem.getAmount() + "元");
            }
            viewhold.stateTv.setVisibility(View.VISIBLE);
            viewhold.stateTv.setText(redRecordItem.getSubTitle());
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView nameTv;//名字
        TextView timeTv;//抢到红包的时间
        TextView moneyTv;//抢到的金额
        TextView stateTv;//状态，比如已领取
    }
}
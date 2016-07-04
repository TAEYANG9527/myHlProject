package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.renhe.heliao.idl.money.red.HeliaoRobRed;

/**
 * 红包相关
 *
 * @author wangning
 * @createtime 2016-5-3
 * @功能说明 红包被抢详情列表适配器
 */
public class LuckyMoneyDetailListAdapter extends BaseAdapter {
    private List<HeliaoRobRed.RedRecordItem> redRecordItems;
    private Context context;
    private ImageLoader imageLoader;

    public LuckyMoneyDetailListAdapter(Context _context, List<HeliaoRobRed.RedRecordItem> redRecordItems) {
        this.redRecordItems = redRecordItems;
        this.context = _context;
        imageLoader = ImageLoader.getInstance();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.luckymoney_list_item, null);
            viewhold.memberAvatar = (ImageView) convertView.findViewById(R.id.avatar_iv);
            viewhold.memberName = (TextView) convertView.findViewById(R.id.name_tv);
            viewhold.robTime = (TextView) convertView.findViewById(R.id.time_tv);
            viewhold.robMoney = (TextView) convertView.findViewById(R.id.money_tv);
            viewhold.robLevelTv = (TextView) convertView.findViewById(R.id.rob_level_tv);
            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }
        HeliaoRobRed.RedRecordItem redRecordItem = (HeliaoRobRed.RedRecordItem) getItem(position);
        if (null != redRecordItem) {
            try {
                imageLoader.displayImage(redRecordItem.getUserface(), viewhold.memberAvatar, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewhold.memberName.setText(redRecordItem.getName());
            viewhold.robTime.setText(redRecordItem.getTime());
//            if (redRecordItem.getSid().equals(RenheApplication.getInstance().getUserInfo().getSid())) {//是自己
//                viewhold.robMoney.setTextColor(context.getResources().getColor(R.color.luckymoney_detail_rob_level_color));
//            } else {
//            viewhold.robMoney.setTextColor(context.getResources().getColor(R.color.C1));
//            }
            viewhold.robMoney.setText(redRecordItem.getAmount() + "元");
            if (redRecordItem.getBest()) {//是手气最佳
                viewhold.robLevelTv.setVisibility(View.VISIBLE);
                viewhold.robLevelTv.setText(context.getResources().getString(R.string.lucky_money_detail_best));
            } else {
                viewhold.robLevelTv.setVisibility(View.INVISIBLE);
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView memberAvatar;//头像
        TextView memberName;//名字
        TextView robTime;//抢到红包的时间
        TextView robMoney;//抢到的金额
        TextView robLevelTv;//状态，比如手气最佳
    }
}
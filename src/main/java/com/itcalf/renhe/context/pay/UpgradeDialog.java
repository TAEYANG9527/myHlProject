package com.itcalf.renhe.context.pay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.utils.StatisticsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提醒用户升级弹出的Dialog
 *
 * @author 王宁
 */
public class UpgradeDialog extends Dialog {

    private Context context;
    private List<View> viewList;
    //    private int[] images = new int[]{R.drawable.img_up, R.drawable.img_up, R.drawable.img_up};
    private int[] titles = new int[]{R.string.upgrade_guide_item1_title, R.string.upgrade_guide_item2_title, R.string.upgrade_guide_item3_title};
    private int[] tips = new int[]{R.string.upgrade_guide_item1_tip, R.string.upgrade_guide_item2_tip, R.string.upgrade_guide_item3_tip};

    public UpgradeDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    private ViewPager mPager;
    private LinearLayout mDotsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_pop_window);
        initListener();
        initData();
    }

    private void initData() {
        mPager = (ViewPager) findViewById(R.id.guide_viewpager);
        mDotsLayout = (LinearLayout) findViewById(R.id.guide_dots);
        initPager();
        mPager.setAdapter(new ViewPagerAdapter(viewList));
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                    if (i == arg0) {
                        mDotsLayout.getChildAt(i).setSelected(true);
                    } else {
                        mDotsLayout.getChildAt(i).setSelected(false);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    private void initPager() {
        viewList = new ArrayList<View>();

        for (int i = 0; i < titles.length; i++) {
            viewList.add(initView(titles[i], tips[i]));

        }
        initDots(titles.length);
    }

    private void initDots(int count) {
        for (int j = 0; j < count; j++) {
            mDotsLayout.addView(initDot());
        }
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private View initDot() {
        return LayoutInflater.from(context).inflate(R.layout.upgrade_layout_dot, null);
    }

    private View initView(int title, int tip) {
        View view = LayoutInflater.from(context).inflate(R.layout.upgrade_guide_item, null);
        TextView titleTv = (TextView) view.findViewById(R.id.guide_title_tv);
        TextView tipTv = (TextView) view.findViewById(R.id.guide_tip_tv);
//        imageView.setImageResource(image);
        titleTv.setText(context.getString(title));
        tipTv.setText(context.getString(tip));
        return view;
    }

    private void initListener() {
        findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, UpgradeActivity.class));
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                if (isShowing()) {
                    dismiss();
                }
                Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                statisticsMap.put("type", "6");
                StatisticsUtil.statisticsCustomClickEvent(context.getString(R.string.android_btn_pop_upgrade_click), 0, "", statisticsMap);
            }
        });

        findViewById(R.id.cancle_tv).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {

        private List<View> data;

        public ViewPagerAdapter(List<View> data) {
            super();
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(data.get(position));
            return data.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(data.get(position));
        }

    }
}
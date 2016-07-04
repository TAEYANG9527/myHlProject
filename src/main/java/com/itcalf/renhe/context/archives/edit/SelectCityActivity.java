package com.itcalf.renhe.context.archives.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.service.RenheService;

/**
 * description :选择城市
 * Created by Chans Renhenet
 * 2015/12/17
 */
public class SelectCityActivity extends BaseActivity {

    private RelativeLayout chinaTabRl, foreignTabRl;
    private TextView chinaTabTxt, foreignTabTxt;
    private View chinaTabDivide, foreignTabDivide;
    private ViewPager viewPager;
    private FragmentPagerAdapter mAdapter;
    private boolean isShowAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.select_city_activity);
    }

    @Override
    protected void findView() {
        super.findView();
        chinaTabRl = (RelativeLayout) findViewById(R.id.china_tab_rl);
        foreignTabRl = (RelativeLayout) findViewById(R.id.foreign_tab_rl);
        chinaTabTxt = (TextView) findViewById(R.id.china_tab_txt);
        foreignTabTxt = (TextView) findViewById(R.id.foreign_tab_txt);
        chinaTabDivide = findViewById(R.id.china_tab_divide);
        foreignTabDivide = findViewById(R.id.foreign_tab_divide);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue("选择城市");
        isShowAll = getIntent().getBooleanExtra("isShowAll", false);

        mAdapter = new CityFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);

        viewPager.setCurrentItem(0);
        setCurrentTab(0);
    }

    @Override
    protected void initListener() {
        super.initListener();
        chinaTabRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
                setCurrentTab(0);
            }
        });
        foreignTabRl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                setCurrentTab(1);
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setCurrentTab(int index) {
        chinaTabTxt.setTextColor(index == 0 ? getResources().getColor(R.color.BP_1) : getResources().getColor(R.color.C2));
        chinaTabDivide.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        foreignTabTxt.setTextColor(index == 1 ? getResources().getColor(R.color.BP_1) : getResources().getColor(R.color.C2));
        foreignTabDivide.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }

    public class CityFragmentPagerAdapter extends FragmentPagerAdapter {

        private String titles[] = new String[]{"国内", "海外"};

        public CityFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    SelectChinaCityFragment chinaCityFragment = new SelectChinaCityFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isShowSearch", false);
                    bundle.putBoolean("isShowAll", isShowAll);
                    chinaCityFragment.setArguments(bundle);
                    return chinaCityFragment;
                case 1:
                    SelectForeignCityFragment foreignCityFragment = new SelectForeignCityFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putBoolean("isShowSearch", false);
                    foreignCityFragment.setArguments(bundle2);
                    return foreignCityFragment;
            }
            return null;
        }

        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

    }

    /**
     * 获取当前所在的城市
     */
    public String getLocatedCity() {
        //重启service定位
        startService(new Intent(this, RenheService.class));
        String locationCity = RenheService.cityName;
        return locationCity;
    }
}

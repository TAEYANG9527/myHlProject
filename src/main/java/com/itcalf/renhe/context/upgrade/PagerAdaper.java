package com.itcalf.renhe.context.upgrade;

/**
 * Created by wangning on 2016/3/28.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.itcalf.renhe.R;

import java.util.ArrayList;
import java.util.List;

public class PagerAdaper extends FragmentPagerAdapter {
    private Context mContext;
    private List<Fragment> mFragments = new ArrayList<>();
    /**
     * 标题栏
     */
    private String[] mTitles;

    private static final int[] mImgIds = {R.drawable.pt, R.drawable.gold, R.drawable.vip};

    public PagerAdaper(FragmentManager fm, Context context, List<Fragment> mFragments, String[] mTitles) {
        super(fm);
        this.mContext = context;
        this.mFragments = mFragments;
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    //设置标题
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

}

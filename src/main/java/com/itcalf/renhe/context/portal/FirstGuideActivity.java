package com.itcalf.renhe.context.portal;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.register.RegisterAuthActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FirstGuideActivity extends BaseActivity {

    private ViewPager mPager;
    private LinearLayout mDotsLayout;
    //	private ImageButton mBtn;
    private Button registerBtn, loginBtn;
    private List<View> viewList;
    private int[] colors = new int[]{R.color.BG_1, R.color.BG_2, R.color.BG_3, R.color.BG_4};
    private String[] colorStrings = new String[]{"9d5ad1", "20c0f1", "f5b641", "e9514d"};
    private int[] texts = new int[]{R.drawable.guide_text_01, R.drawable.guide_text_02, R.drawable.guide_text_03,
            R.drawable.guide_text_04};
    private int[] images = new int[]{R.drawable.guide_image_01, R.drawable.guide_image_02, R.drawable.guide_image_03,
            R.drawable.guide_image_04};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mPager = (ViewPager) findViewById(R.id.guide_viewpager);
        mDotsLayout = (LinearLayout) findViewById(R.id.guide_dots);
        //		mBtn = (ImageButton)findViewById(R.id.guide_btn);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);

        //注册按钮的默认颜色
        registerBtn.setTextColor(Color.parseColor("#" + colorStrings[0]));
        ButtonColorChange(0);

        initPager();
        mPager.setAdapter(new ViewPagerAdapter(viewList));
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                    if (i == arg0) {
                        mDotsLayout.getChildAt(i).setSelected(true);
                        registerBtn.setTextColor(Color.parseColor("#" + colorStrings[i]));
                        ButtonColorChange(i);

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

        registerBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(RegisterAuthActivity.class);
                finish();
            }
        });
        loginBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (null != getIntent()) {
                    intent = getIntent();
                }
                intent.setClass(FirstGuideActivity.this, LoginActivity.class);
                intent.putExtra(Constants.DATA_LOGOUT, true);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();
            }
        });
    }

    //改变注册按钮的颜色
    @SuppressLint("ClickableViewAccessibility")
    private void ButtonColorChange(final int i) {
        registerBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((Button) v).setTextColor(Color.parseColor("#50" + colorStrings[i]));
                        break;
                    case MotionEvent.ACTION_UP:
                        ((Button) v).setTextColor(Color.parseColor("#" + colorStrings[i]));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        ((Button) v).setTextColor(Color.parseColor("#50" + colorStrings[i]));
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("引导页"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("引导页"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    private void initPager() {
        viewList = new ArrayList<View>();

        for (int i = 0; i < images.length; i++) {
            viewList.add(initView(colors[i], texts[i], images[i]));

        }
        initDots(images.length);
    }

    private void initDots(int count) {
        for (int j = 0; j < count; j++) {
            mDotsLayout.addView(initDot());
        }
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private View initDot() {
        return LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_dot, null);
    }

    private View initView(int color, int textImage, int image) {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_guide, null);
        RelativeLayout itemGuideRl = (RelativeLayout) view.findViewById(R.id.item_guide_rl);
        ImageView textImageView = (ImageView) view.findViewById(R.id.iguide_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.iguide_img);

        itemGuideRl.setBackgroundResource(color);

        InputStream is1 = getResources().openRawResource(textImage);
        BitmapDrawable bd1 = new BitmapDrawable(is1);
        Bitmap bitmap1 = bd1.getBitmap();
        cacheBitmapList.add(bitmap1);

        InputStream is2 = getResources().openRawResource(image);
        BitmapDrawable bd2 = new BitmapDrawable(is2);
        Bitmap bitmap2 = bd2.getBitmap();
        cacheBitmapList.add(bitmap2);

        textImageView.setImageBitmap(bitmap1);
        imageView.setImageBitmap(bitmap2);
        return view;
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

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewList.clear();
        viewList = null;
    }
}

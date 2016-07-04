package com.itcalf.renhe.context.room.addMessage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;

import java.util.ArrayList;
import java.util.List;

public class PhotoActivity extends BaseActivity {

    private ArrayList<View> listViews = null;
    private ViewPager pager;
    private MyPageAdapter adapter;
    private int count;

    public List<Bitmap> bmp = new ArrayList<Bitmap>();
    public List<String> drr = new ArrayList<String>();
    public List<String> del = new ArrayList<String>();
    public int max;

    private int currentItem = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_photo);
        findViewById(R.id.title_right).setVisibility(View.GONE);
        Intent intent = getIntent();
        final int id = intent.getIntExtra("ID", 0);
        currentItem = id + 1;
        for (int i = 0; i < Bimp.bmp.size(); i++) {
            bmp.add(Bimp.bmp.get(i));
        }
        for (int i = 0; i < Bimp.drr.size(); i++) {
            drr.add(Bimp.drr.get(i));
        }
        max = Bimp.max;

        pager = (ViewPager) findViewById(R.id.viewpager);
        for (int i = 0; i < bmp.size(); i++) {
            initListViews(bmp.get(i));//
        }

        //		numTextView.setText(id + 1+"/"+listViews.size());
        adapter = new MyPageAdapter(listViews);// 构造adapter
        pager.setAdapter(adapter);// 设置适配器

        pager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                currentItem = arg0 + 1;
                count = arg0;
                freshFlag();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        pager.setCurrentItem(id);
        freshFlag();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem sendItem = menu.findItem(R.id.item_delete);
        sendItem.setVisible(true);
        sendItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_delete) {
            MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(PhotoActivity.this);
            materialDialogsUtil.getBuilder(R.string.renmaiquan_publish_photo_delete_tip).callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    if (listViews.size() == 1) {
                        Bimp.bmp.clear();
                        Bimp.drr.clear();
                        Bimp.max = 0;
                        FileUtils.deleteDir();
                        listViews.remove(count);
                        adapter.setListViews(listViews);
                        adapter.notifyDataSetChanged();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String newStr = drr.get(count).substring(drr.get(count).lastIndexOf("/") + 1,
                                drr.get(count).lastIndexOf("."));
                        Bimp.bmp.remove(count);
                        drr.remove(count);
                        del.add(newStr);
                        max--;
                        pager.removeAllViews();
                        listViews.remove(count);
                        adapter.setListViews(listViews);
                        adapter.notifyDataSetChanged();
                        freshFlag();
                        Bimp.drr = drr;
                        Bimp.max = max;
                        for (int i = 0; i < del.size(); i++) {
                            FileUtils.delFile(del.get(i) + ".JPEG");
                        }
                    }
                }

                @Override
                public void onNeutral(MaterialDialog dialog) {
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                }
            });
            materialDialogsUtil.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void freshFlag() {
        if (currentItem <= max) {
            setTextValue(R.id.title_txt, currentItem + "/" + max);
        } else {
            setTextValue(R.id.title_txt, currentItem + "/" + max);
        }
    }

    private void initListViews(Bitmap bm) {
        if (listViews == null)
            listViews = new ArrayList<View>();
        ImageView img = new ImageView(this);// 构造textView对象
        img.setBackgroundColor(0xff000000);
        img.setImageBitmap(bm);
        img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        listViews.add(img);// 添加view
    }

    class MyPageAdapter extends PagerAdapter {

        private ArrayList<View> listViews;// content

        private int size;// 页数

        public MyPageAdapter(ArrayList<View> listViews) {// 构造函数
            // 初始化viewpager的时候给的一个页面
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public void setListViews(ArrayList<View> listViews) {// 自己写的一个方法用来添加数据
            this.listViews = listViews;
            size = listViews == null ? 0 : listViews.size();
        }

        public int getCount() {// 返回数量
            return size;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {// 销毁view对象
            if (size > 0) {
                ((ViewPager) arg0).removeView(listViews.get(arg1 % size));
            }
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {// 返回view对象
            try {
                ((ViewPager) arg0).addView(listViews.get(arg1 % size), 0);

            } catch (Exception e) {
            }
            return listViews.get(arg1 % size);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}

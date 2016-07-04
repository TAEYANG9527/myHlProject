package com.itcalf.renhe.context.imageselector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.widget.photoview.PhotoView;
import com.itcalf.renhe.widget.photoview.PhotoViewAttacher;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FragmentSelectPhotoActivity extends BaseActivity {
    public static final String EXTRA_RESULT = "select_result";
    public static final String VIEW_EXTRA_RESULT = "view_result";
    private static final int RESULT_SEND = 102;
    private ArrayList<View> listViews = null;
    private ViewPager pager;
    private MyPageAdapter adapter;
    public int max;

    private int currentItem = 0;
    private List<Image> imageList;
    private LayoutInflater inflater;
    private int DEFAULT_IMAGE;
    private FragmentImageConfig imageConfig;
    private TextView submitButton;
    private ImageView photoCheckIv;
    private TextView photoCheckTv;
    private RelativeLayout footerRl;
    private ArrayList<String> resultList = new ArrayList<>();//用于存储已经被选择的图片
    private List<Image> selectedImageList = new ArrayList<>();//用于存储已经被选择的图片
    private boolean isCameraShot;//用来标示是从拍照跳转过去的

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_photo);
    }

    @Override
    protected void findView() {
        super.findView();
        inflater = getLayoutInflater();
        DEFAULT_IMAGE = R.drawable.imageselector_photo;
        pager = (ViewPager) findViewById(R.id.viewpager);
        submitButton = (TextView) findViewById(R.id.title_right);
        footerRl = (RelativeLayout) findViewById(R.id.footer_layout);
        photoCheckIv = (ImageView) findViewById(R.id.photo_check);
        photoCheckTv = (TextView) findViewById(R.id.select_tv);
    }

    @Override
    protected void initData() {
        super.initData();
        imageConfig = FragmentImageSelector.getImageConfig();
        Bundle bundle = getIntent().getExtras();
        currentItem = bundle.getInt("index", 0);
        imageList = (List<Image>) bundle.getSerializable("pathArray");
        isCameraShot = bundle.getBoolean("isCameraShot", false);
        footerRl.setVisibility(isCameraShot ? View.GONE : View.VISIBLE);
        max = imageList.size();
        resultList = imageConfig.getPathList();
        setDefaultSelected(resultList);
        adapter = new MyPageAdapter(listViews);// 构造adapter
        pager.setAdapter(adapter);// 设置适配器
        pager.setCurrentItem(currentItem);
        freshFlag();
        if (!isCameraShot) {
            if (resultList == null || resultList.size() <= 0) {
                submitButton.setText(R.string.finish);
                submitButton.setEnabled(false);
            } else {
                submitButton.setText((getResources().getText(R.string.finish)) + "(" + resultList.size() + "/" + imageConfig.getMaxSize() + ")");
                submitButton.setEnabled(true);
            }
        } else {
            submitButton.setText(R.string.finish);
            submitButton.setEnabled(true);
        }
    }

    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String filePath : resultList) {
            Image image = getImageByPath(filePath);
            if (image != null && !selectedImageList.contains(image)) {
                selectedImageList.add(image);
            }
        }
    }

    private Image getImageByPath(String filePath) {
        if (imageList != null && imageList.size() > 0) {
            for (Image image : imageList) {
                if (image.path.equalsIgnoreCase(filePath)) {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    protected void initListener() {
        super.initListener();
        pager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                currentItem = arg0;
                freshFlag();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList != null && resultList.size() > 0) {
                    submitButton.setEnabled(false);
                    if (isCameraShot) {
                        String cameraPath = imageList.get(0).path;
                        resultList.clear();
                        resultList.add(cameraPath);
                    }
                    setResult(RESULT_SEND);
                    finish();
                }
            }
        });
        photoCheckIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem < imageList.size()) {
                    Image image = imageList.get(currentItem);
                    selectImageFromGrid(image);
                }
            }
        });
        photoCheckTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoCheckIv.performClick();
            }
        });
    }

    private void selectImageFromGrid(Image image) {
        if (image != null) {
            if (resultList.contains(image.path)) {
                photoCheckIv.setImageResource(R.drawable.icon_recommend_unsel);
                resultList.remove(image.path);
            } else {
                if (imageConfig.getMaxSize() == resultList.size()) {
                    Toast.makeText(this, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                    return;
                }
                photoCheckIv.setImageResource(R.drawable.icon_recommend_sel);
                resultList.add(image.path);
            }
            if (selectedImageList.contains(image)) {
                selectedImageList.remove(image);
            } else {
                selectedImageList.add(image);
            }
            if (resultList == null || resultList.size() <= 0) {
                submitButton.setText(R.string.finish);
                submitButton.setEnabled(false);
            } else {
                submitButton.setText((getResources().getText(R.string.finish)) + "(" + resultList.size() + "/" + imageConfig.getMaxSize() + ")");
                submitButton.setEnabled(true);
            }
        }
    }

    private void freshFlag() {
        if (currentItem <= max) {
            setTextValue(R.id.title_txt, ((currentItem + 1) > max ? max : (currentItem + 1)) + "/" + max);
        } else {
            setTextValue(R.id.title_txt, max + "/" + max);
        }

        if (currentItem < imageList.size()) {
            if (selectedImageList.contains(imageList.get(currentItem))) {
                photoCheckIv.setImageResource(R.drawable.icon_recommend_sel);
            } else {
                photoCheckIv.setImageResource(R.drawable.icon_recommend_unsel);
            }
        }
    }

    class MyPageAdapter extends PagerAdapter {
        private int size;// 页数
        private DisplayImageOptions options;

        public MyPageAdapter(ArrayList<View> listViews) {// 构造函数
            // 初始化viewpager的时候给的一个页面
            // this.listViews =
            // listViews;
            // size = listViews == null ? 0 : listViews.size();
            size = imageList.size();
            options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                    .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(true).cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
        }

        public int getCount() {// 返回数量
            return size;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {// 销毁view对象
            ((ViewPager) arg0).removeView((View) arg2);
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {// 返回view对象
            String item = imageList.get(arg1).path;
            String url = item;
            final View imageLayout = inflater.inflate(R.layout.item_pager_image, null);
            final PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);
            imageLayout.findViewById(R.id.saveImgBtn).setVisibility(View.GONE);
            final PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
            attacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
//                    finish();
                    handleToolbar();
                }
            });
            attacher.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });
            attacher.setRotatable(false);
            attacher.setToRightAngle(true);

            imageView.setTag(item);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            loadImage(imageView, url, spinner);
            ((ViewPager) arg0).addView(imageLayout, 0);
            return imageLayout;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    private void handleToolbar() {
        if (null != getSupportActionBar()) {
            if (getSupportActionBar().isShowing()) {
                getSupportActionBar().hide();
            } else {
                getSupportActionBar().show();
            }
        }
    }

    void loadImage(ImageView imageView, String mImageUrl, ProgressBar progressBar) {
//        Glide.with(this)
//                .load(mImageUrl)
//                .placeholder(com.yancy.imageselector.R.mipmap.imageselector_photo)
//                .centerCrop()
//                .into(imageView);
//        imageLoader.displayImage("file://" + mImageUrl, imageView);
        AsyncImageLoader.getInstance()
                .populateData(this, ((RenheApplication) getApplicationContext()).getUserInfo().getEmail(), false, true, false)
                .loadPic2(imageView, progressBar, "file://" + mImageUrl);
    }

    @Override
    public void onBackPressed() {
        if (isCameraShot) {
            if (resultList.contains(imageList.get(0).path)) {
                resultList.remove(imageList.get(0).path);
            }
            if (selectedImageList.contains(imageList.get(0))) {
                selectedImageList.remove(imageList.get(0));
            }
        } else {
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable(VIEW_EXTRA_RESULT, (Serializable) selectedImageList);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

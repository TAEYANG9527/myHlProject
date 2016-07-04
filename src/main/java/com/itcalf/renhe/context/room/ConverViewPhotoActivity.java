package com.itcalf.renhe.context.room;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.cover.UploadMemberCoverListTask;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ConverViewPhotoActivity extends BaseActivity {

    private ArrayList<View> listViews = null;
    private ViewPager pager;
    private MyPageAdapter adapter;
    private int count;

    public List<Bitmap> bmp = new ArrayList<Bitmap>();
    public List<String> drr = new ArrayList<String>();
    public List<String> del = new ArrayList<String>();
    public int max;

    private int currentItem = 0;
    private CharSequence[] middlePics;
    private LayoutInflater inflater;
    private int DEFAULT_IMAGE;
    private boolean isToCover;
    private DisplayImageOptions options;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_photo);
    }

    @Override
    protected void findView() {
        super.findView();
        isToCover = getIntent().getBooleanExtra("isTocover", false);
        inflater = getLayoutInflater();
        findViewById(R.id.title_right).setVisibility(View.GONE);
        DEFAULT_IMAGE = R.drawable.room_pic_default_bcg;
        Intent intent = getIntent();
        final int id = intent.getIntExtra("ID", 0);
        middlePics = intent.getCharSequenceArrayExtra("middlePics");
        currentItem = id + 1;
        max = middlePics.length;
        pager = (ViewPager) findViewById(R.id.viewpager);
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

    private void freshFlag() {
        if (currentItem <= max) {
            setTextValue(R.id.title_txt, currentItem + "/" + max);
        } else {
            setTextValue(R.id.title_txt, currentItem + "/" + max);
        }
    }

    private void initListViews(String middleUrl) {
        if (listViews == null)
            listViews = new ArrayList<View>();
        ImageView img = new ImageView(this);// 构造textView对象
        img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        listViews.add(img);// 添加view
    }

    class MyPageAdapter extends PagerAdapter {

        private int size;// 页数

        public MyPageAdapter(ArrayList<View> listViews) {// 构造函数
            // 初始化viewpager的时候给的一个页面
            // this.listViews =
            // listViews;
            // size = listViews == null ? 0 : listViews.size();
            size = middlePics.length;
            options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                    .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(false).cacheOnDisk(true)
                    .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(true).build();
        }

        public int getCount() {// 返回数量
            return size;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {// 销毁view对象
            // if (size > 0) {
            // ((ViewPager) arg0).removeView(listViews.get(arg1 % size));
            // }
            ((ViewPager) arg0).removeView((View) arg2);
        }

        public void finishUpdate(View arg0) {
        }

        public Object instantiateItem(View arg0, int arg1) {// 返回view对象
            String item = middlePics[arg1].toString();
            String url = item;
            int coverId = -1;//会员作品id
            if (-1 != item.indexOf("#")) {
                url = url.substring(0, item.indexOf("#"));
                String idTemp = item.substring(item.indexOf("#") + 1, item.length());
                try {
                    coverId = Integer.parseInt(idTemp);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            final View imageLayout = inflater.inflate(R.layout.cover_member_item_pager_image, null);
            final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            ImageButton saveImgBtn = (ImageButton) imageLayout.findViewById(R.id.saveImgBtn);
            RelativeLayout updateCoverRl = (RelativeLayout) imageLayout.findViewById(R.id.update_cover_rl);
            Button sureBt = (Button) imageLayout.findViewById(R.id.editBt);
            if (isToCover) {
//                saveImgBtn.setVisibility(View.VISIBLE);
                updateCoverRl.setVisibility(View.VISIBLE);
            } else {
//                saveImgBtn.setVisibility(View.VISIBLE);
                updateCoverRl.setVisibility(View.GONE);
            }
            imageView.setTag(item);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            loadImage(imageView, url, spinner);
            //			saveImgBtn.setVisibility(View.VISIBLE);
            saveImgBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    File file = null;
                    try {
                        // 4.0+貌似不能直接创建文件，得先创建文件夹，不然会有异常
                        File f = new File(Constants.CACHE_PATH.PICTUREPATH);
                        if (!f.exists()) {
                            f.mkdirs();
                        } else {
                            file = new File(Constants.CACHE_PATH.PICTUREPATH + System.currentTimeMillis() + ".png");
                            FileOutputStream out = new FileOutputStream(file);
                            BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
                            if (null != bmd) {
                                Bitmap bm = bmd.getBitmap();
                                if (bm != null) {
                                    bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    ToastUtil.showToast(ConverViewPhotoActivity.this, "图片已保存" + Constants.CACHE_PATH.PICTUREPATH);
                                }
                            }
                            out.flush();
                            out.close();
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    if (file != null) {
                        //						MediaScannerConnection.scanFile(ConverViewPhotoActivity.this,
                        //								new String[] { Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
                        //										+ "/" + file.getParentFile().getAbsolutePath() }, null, null);
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(file);
                        intent.setData(uri);
                        ConverViewPhotoActivity.this.sendBroadcast(intent);
                    }
                }
            });
            final int mCoverID = coverId;
            final String mUrl = url;
            sureBt.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mCoverID >= 0) {
                        new UploadMemberCoverListTask(ConverViewPhotoActivity.this) {
                            public void doPre() {
                                showDialog(1);
                            }

                            ;

                            public void doPost(com.itcalf.renhe.dto.UploadCover result) {
                                if (null != result && result.getState() == 1) {
                                    Intent intent = new Intent(BroadCastAction.UPLOAD_COVER_ARCHIEVE_ACTION);
                                    intent.putExtra("cover", result.getCover());
                                    intent.putExtra("cover_path", mUrl);
                                    sendBroadcast(intent);
                                    new Handler().postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            removeDialog(1);
                                            setResult(RESULT_OK);
                                            finish();
                                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                                        }
                                    }, 2000);
                                }
                            }

                            ;
                        }.executeOnExecutor(Executors.newCachedThreadPool(),
                                RenheApplication.getInstance().getUserInfo().getSid(),
                                RenheApplication.getInstance().getUserInfo().getAdSId(), mCoverID + "");
                    }
                }
            });
            ((ViewPager) arg0).addView(imageLayout, 0);
            return imageLayout;
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    void loadImage(ImageView imageView, String mImageUrl, ProgressBar progressBar) {
//        ImageLoader imageLoader = ImageLoader.getInstance();
//        try {
//            imageLoader.displayImage(mImageUrl, imageView, CacheManager.imageOptions,
//                    new AnimateFirstDisplayListener(progressBar));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        AsyncImageLoader.getInstance()
                .populateData(this, ((RenheApplication) getApplicationContext()).getUserInfo().getEmail(), false, true, false)
                .loadPic2(imageView, progressBar, mImageUrl);

    }

    class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        ProgressBar pb;

        public AnimateFirstDisplayListener(ProgressBar pb) {
            this.pb = pb;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                if (null != pb) {
                    pb.setVisibility(View.GONE);
                }
                ImageView imageView = (ImageView) view;
                imageView.setBackgroundDrawable(new BitmapDrawable(loadedImage));
                if (null != imageView && null != cacheImageViewList) {
                    cacheImageViewList.add(imageView);
                }
            }
        }

        //		@Override
        //		public void onLoadingStarted(String imageUri, View view) {
        //			super.onLoadingStarted(imageUri, view);
        //			if(null != pb){
        //				pb.setVisibility(View.VISIBLE);
        //			}
        //			System.out.println("onLoadingStarted");
        //		}
        //
        //		@Override
        //		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        //			super.onLoadingFailed(imageUri, view, failReason);
        //			if (null != view) {
        //				ImageView imageView = (ImageView) view;
        //				imageView.setImageResource(DEFAULT_IMAGE);
        //			}
        //			System.out.println("onLoadingFailed");
        //
        //		}
        //
        //		@Override
        //		public void onLoadingCancelled(String imageUri, View view) {
        //			super.onLoadingCancelled(imageUri, view);
        //			if (null != view) {
        //				ImageView imageView = (ImageView) view;
        //				imageView.setImageResource(DEFAULT_IMAGE);
        //			}
        //			System.out.println("onLoadingCancelled");
        //		}

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.switching).cancelable(false).build();
            default:
                return null;
        }
    }
}

package com.itcalf.renhe.context.room;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.Result;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.cover.UploadMemberCoverListTask;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.widget.photoview.PhotoView;
import com.itcalf.renhe.widget.photoview.PhotoViewAttacher;
import com.itcalf.renhe.zxing.decoding.DecodeImageCallback;
import com.itcalf.renhe.zxing.decoding.DecodeImageThread;
import com.itcalf.renhe.zxing.ui.ActivityQrcodeResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ViewPhotoActivity extends BaseActivity {

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
    private MaterialDialog materialDialog;
    /**
     * 识别图中二维码
     */
    private TextView recognizeTv;
    public static final int MSG_DECODE_SUCCEED = 1;
    public static final int MSG_DECODE_FAIL = 2;
    private Executor mQrCodeExecutor;
    private Handler mHandler;
    private String resultString;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.activity_photo);
        if (null != toolbar) {
            toolbar.setVisibility(View.GONE);
        }
        isToCover = getIntent().getBooleanExtra("isTocover", false);
        inflater = getLayoutInflater();
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
        mQrCodeExecutor = Executors.newSingleThreadExecutor();
        mHandler = new WeakHandler(this);
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
        img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        listViews.add(img);// 添加view
    }

    class MyPageAdapter extends PagerAdapter {

        private int size;// 页数
        private DisplayImageOptions options;


        public MyPageAdapter(ArrayList<View> listViews) {// 构造函数
            // 初始化viewpaprivate DisplayImageOptions options;ger的时候给的一个页面
            // this.listViews =
            // listViews;
            // size = listViews == null ? 0 : listViews.size();
            size = middlePics.length;
            options = new DisplayImageOptions.Builder().showImageOnLoading(DEFAULT_IMAGE).showImageForEmptyUri(DEFAULT_IMAGE)
                    .showImageOnFail(DEFAULT_IMAGE).cacheInMemory(true).cacheOnDisk(true)
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
            final View imageLayout = inflater.inflate(R.layout.item_pager_image, null);
            final PhotoView imageView = (PhotoView) imageLayout.findViewById(R.id.image);
            final PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
            attacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });
            attacher.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    createDialog(imageView);
                    createCustomDialog(imageView);
                    confignizePhotoQrcode(imageView);
                    return true;
                }
            });
            attacher.setRotatable(false);
            attacher.setToRightAngle(true);

            ImageButton saveImgBtn = (ImageButton) imageLayout.findViewById(R.id.saveImgBtn);
            RelativeLayout updateCoverRl = (RelativeLayout) imageLayout.findViewById(R.id.update_cover_rl);
            Button sureBt = (Button) imageLayout.findViewById(R.id.editBt);
            if (isToCover) {
//                saveImgBtn.setVisibility(View.GONE);
                updateCoverRl.setVisibility(View.VISIBLE);
            } else {
//                saveImgBtn.setVisibility(View.VISIBLE);
                updateCoverRl.setVisibility(View.GONE);
            }
            imageView.setTag(item);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            loadImage(imageView, url, spinner);
//            saveImgBtn.setVisibility(View.VISIBLE);
            saveImgBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    File file = null;
                    try {
                        // 4.0+貌似不能直接创建文件，得先创建文件夹，不然会有异常
                        File f = new File(Constants.CACHE_PATH.PICTUREPATH);
                        if (!f.exists()) {
                            f.mkdir();
                        } else {
                            file = new File(Constants.CACHE_PATH.PICTUREPATH + System.currentTimeMillis() + ".png");
                            FileOutputStream out = new FileOutputStream(file);
                            BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
                            if (null != bmd) {
                                Bitmap bm = bmd.getBitmap();
                                if (bm != null) {
                                    bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    ToastUtil.showToast(ViewPhotoActivity.this, "图片已保存" + Constants.CACHE_PATH.PICTUREPATH);
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
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(file);
                        intent.setData(uri);
                        ViewPhotoActivity.this.sendBroadcast(intent);
                    }
                }
            });
            final int mCoverID = coverId;
            final String mUrl = url;
            sureBt.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (mCoverID >= 0) {
                        new UploadMemberCoverListTask(ViewPhotoActivity.this) {
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
                        }.execute(RenheApplication.getInstance().getUserInfo().getSid(),
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
        AsyncImageLoader.getInstance()
                .populateData(this, ((RenheApplication) getApplicationContext()).getUserInfo().getEmail(), false, true, false)
                .loadPic2(imageView, progressBar, mImageUrl);
    }

    class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 1000);
                    displayedImages.add(imageUri);
                }
            }
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
            if (null != view) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(DEFAULT_IMAGE);
            }

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            super.onLoadingCancelled(imageUri, view);
            if (null != view) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(DEFAULT_IMAGE);
            }
        }

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

    public void createDialog(final ImageView image) {
        if (null == materialDialogsUtil)
            materialDialogsUtil = new MaterialDialogsUtil(ViewPhotoActivity.this);
        materialDialogsUtil.showSelectList(R.array.photo_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        savePhoto(image);
                        break;
                    case 1:
                        if (!TextUtils.isEmpty(text) && text.toString().contains("二维码")) {
                            if (image == null) {
                                ToastUtil.showToast(ViewPhotoActivity.this, "图片不存在");
                            }
                            Intent intent = new Intent(ViewPhotoActivity.this, ActivityQrcodeResult.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("result", resultString);
                            intent.putExtras(bundle);
                            startHlActivity(intent);
                        } else {
                            break;
                        }
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }
            }
        });
        materialDialogsUtil.show();
    }

    private void savePhoto(ImageView image) {
        if (image == null) {
            ToastUtil.showToast(ViewPhotoActivity.this, "图片不存在");
        }
        File file = null;
        try {
            // 4.0+貌似不能直接创建文件，得先创建文件夹，不然会有异常
            File f = new File(Constants.CACHE_PATH.PICTUREPATH);
            if (!f.exists()) {
                f.mkdirs();
            } else {
                file = new File(Constants.CACHE_PATH.PICTUREPATH + System.currentTimeMillis() + ".png");
                FileOutputStream out = new FileOutputStream(file);
                BitmapDrawable bmd = (BitmapDrawable) image.getDrawable();
                Bitmap bm = bmd.getBitmap();
                if (bm != null) {
                    bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                    ToastUtil.showToast(ViewPhotoActivity.this, "图片已保存" + Constants.CACHE_PATH.PICTUREPATH);
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
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            sendBroadcast(intent);
        }
    }

    private void confignizePhotoQrcode(ImageView imageView) {
        if (imageView == null) {
//            ToastUtil.showToast(ViewPhotoActivity.this, "图片不存在");
            return;
        }
        BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
        Bitmap bm = bmd.getBitmap();
        if (null != mQrCodeExecutor && null != bm) {
            mQrCodeExecutor.execute(new DecodeImageThread(bm, mDecodeImageCallback));
        }
    }

    private void createCustomDialog(final ImageView imageView) {
        View dialoglayout = inflater.inflate(R.layout.view_photo_dialog_customview, null);
        materialDialog = materialDialogsUtil.getCustomViewBuilderByView(dialoglayout,
                0, 0)//不需要确定/取消按钮
                .canceledOnTouchOutside(true)
                .build();
        recognizeTv = (TextView) dialoglayout.findViewById(R.id.recognize_tv);
        recognizeTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewPhotoActivity.this, ActivityQrcodeResult.class);
                Bundle bundle = new Bundle();
                bundle.putString("result", resultString);
                intent.putExtras(bundle);
                startHlActivity(intent);
                materialDialog.dismiss();
            }
        });
        dialoglayout.findViewById(R.id.save_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
                savePhoto(imageView);
            }
        });
        dialoglayout.findViewById(R.id.cancle_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDialog.dismiss();
            }
        });
        materialDialog.show();
    }

    private class WeakHandler extends Handler {
        private WeakReference<ViewPhotoActivity> mWeakQrCodeActivity;

        public WeakHandler(ViewPhotoActivity imagePickerActivity) {
            super();
            this.mWeakQrCodeActivity = new WeakReference<>(imagePickerActivity);
        }

        @Override
        public void handleMessage(Message msg) {
//            ViewPhotoActivity qrCodeActivity = mWeakQrCodeActivity.get();
            switch (msg.what) {
                case MSG_DECODE_SUCCEED:
                    Result result = (Result) msg.obj;
//                    if (null == result) {
//                        ToastUtil.showToast(ViewPhotoActivity.this, R.string.recognize_photo_qrcode_error);
//                    } else {
                    if (null != result) {
                        resultString = result.getText();
                        if (null != recognizeTv)
                            recognizeTv.setVisibility(View.VISIBLE);
                    }
//                    }
                    break;
                case MSG_DECODE_FAIL:
//                    ToastUtil.showToast(ViewPhotoActivity.this, R.string.recognize_photo_qrcode_error);
                    break;
            }
            super.handleMessage(msg);
        }

    }

    private DecodeImageCallback mDecodeImageCallback = new DecodeImageCallback() {
        @Override
        public void decodeSucceed(Result result) {
            Logger.e("decodeSucceed");
            mHandler.obtainMessage(MSG_DECODE_SUCCEED, result).sendToTarget();
        }

        @Override
        public void decodeFail(int type, String reason) {
            Logger.e("decodeFail");
            mHandler.sendEmptyMessage(MSG_DECODE_FAIL);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

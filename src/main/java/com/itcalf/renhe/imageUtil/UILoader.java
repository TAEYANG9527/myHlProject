package com.itcalf.renhe.imageUtil;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.imageselector.ImageLoader;

/**
 * UILoader，即是universalimageloader 用于imageselector的图片下载
 * Created by wangning on 2015/12/23.
 */
public class UILoader implements ImageLoader {
    /**
     * 图片加载参数
     */
//    public DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
//            .showImageOnLoading(com.yancy.imageselector.R.mipmap.imageselector_photo).showImageForEmptyUri(com.yancy.imageselector.R.mipmap.imageselector_photo)
//            .showImageOnFail(com.yancy.imageselector.R.mipmap.imageselector_photo).cacheInMemory(true).cacheOnDisk(true)
//            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).considerExifParams(false)
//            .build();
    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
//        FragmentImageLoader.getInstance().displayImage("file://" + path, imageView, imageOptions);
        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.imageselector_photo)
                .centerCrop()
                .into(imageView);
    }

}
/*
 *   ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 *     ┃　　　┃
 *     ┃　　　┃
 *     ┃　　　┗━━━┓
 *     ┃　　　　　　　┣┓
 *     ┃　　　　　　　┏┛
 *     ┗┓┓┏━┳┓┏┛
 *       ┃┫┫　┃┫┫
 *       ┗┻┛　┗┻┛
 *        神兽保佑
 *        代码无BUG!
 */
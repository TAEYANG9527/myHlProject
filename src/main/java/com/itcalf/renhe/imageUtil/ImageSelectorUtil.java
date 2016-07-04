package com.itcalf.renhe.imageUtil;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.imageselector.FragmentImageConfig;
import com.itcalf.renhe.context.imageselector.FragmentImageSelector;
import com.itcalf.renhe.context.imageselector.ImageConfig;
import com.itcalf.renhe.context.imageselector.ImageSelector;

import java.util.ArrayList;

/**
 * 开启图片选择器
 * 因为startActivityForResult的使用问题，做了activity和fragment的区分
 * Created by wangning on 2015/12/23.
 */
public class ImageSelectorUtil {
    /**
     * 在activity中使用
     *
     * @param context
     * @param selectedImagePath
     */
    public static void initActivityImageSelector(Context context, ArrayList<String> selectedImagePath, int maxSize) {
        ImageConfig imageConfig
                = new ImageConfig.Builder((Activity) context
                , new UILoader())// UILoader 可用自己用的缓存库(目前用的是Glide)
                // 如果在 4.4 以上，则修改状态栏颜色 （默认黑色）
                .steepToolBarColor(context.getResources().getColor(R.color.BP_1))
                // 标题的背景颜色 （默认黑色）
                .titleBgColor(context.getResources().getColor(R.color.BP_1))
                // 提交按钮字体的颜色  （默认白色）
                .titleSubmitTextColor(context.getResources().getColor(R.color.white))
                // 标题颜色 （默认白色）
                .titleTextColor(context.getResources().getColor(R.color.white))
                // 开启多选   （默认为多选）  (单选 为 singleSelect)
//                        .singleSelect()
//                .crop()
                // 多选时的最大数量   （默认 9 张）
                .mutiSelectMaxSize(maxSize)
                // 已选择的图片路径
                .pathList(selectedImagePath)
                // 拍照后存放的图片路径
                .filePath(Constants.CACHE_PATH.HL_IMAGESELECTOR_CAMERA_PATH)
//                .filePath("/renhe/MyFragmentImageSelector/Pictures")
                // 开启拍照功能 （默认开启）
                .showCamera()
                .build();
        ImageSelector.open(imageConfig);   // 开启图片选择器
    }
    /**
     * 在上传/更新头像的activity中使用
     *
     * @param context
     */
    public static void initUpdateAvatarActivityImageSelector(Context context) {
        ImageConfig imageConfig
                = new ImageConfig.Builder((Activity) context
                , new UILoader())// UILoader 可用自己用的缓存库(目前用的是Glide)
                // 如果在 4.4 以上，则修改状态栏颜色 （默认黑色）
                .steepToolBarColor(context.getResources().getColor(R.color.BP_1))
                // 标题的背景颜色 （默认黑色）
                .titleBgColor(context.getResources().getColor(R.color.BP_1))
                // 提交按钮字体的颜色  （默认白色）
                .titleSubmitTextColor(context.getResources().getColor(R.color.white))
                // 标题颜色 （默认白色）
                .titleTextColor(context.getResources().getColor(R.color.white))
                // 开启多选   （默认为多选）  (单选 为 singleSelect)
//                        .singleSelect()
//                .crop()
                .singleSelect()
                // 拍照后存放的图片路径
                .filePath(Constants.CACHE_PATH.HL_IMAGESELECTOR_CAMERA_PATH)
//                .filePath("/renhe/MyFragmentImageSelector/Pictures")
                // 开启拍照功能 （默认开启）
                .showCamera()
                .build();
        ImageSelector.open(imageConfig);   // 开启图片选择器
    }

    /**
     * 在fragment中使用
     *
     * @param fragment
     * @param selectedImagePath
     */
    public static void initFragmentImageSelector(Fragment fragment, ArrayList<String> selectedImagePath) {
        FragmentImageConfig imageConfig
                = new FragmentImageConfig.Builder(fragment
                , new UILoader())// UILoader 可用自己用的缓存库(目前用的是Glide)
                // 如果在 4.4 以上，则修改状态栏颜色 （默认黑色）
                .steepToolBarColor(fragment.getResources().getColor(R.color.BP_1))
                // 标题的背景颜色 （默认黑色）
                .titleBgColor(fragment.getResources().getColor(R.color.BP_1))
                // 提交按钮字体的颜色  （默认白色）
                .titleSubmitTextColor(fragment.getResources().getColor(R.color.white))
                // 标题颜色 （默认白色）
                .titleTextColor(fragment.getResources().getColor(R.color.white))
                // 开启多选   （默认为多选）  (单选 为 singleSelect)
//                        .singleSelect()
//                        .crop()
                // 多选时的最大数量   （默认 9 张）
                .mutiSelectMaxSize(9)
                // 已选择的图片路径
                .pathList(selectedImagePath)
                // 拍照后存放的图片路径
                .filePath(Constants.CACHE_PATH.HL_IMAGESELECTOR_CAMERA_PATH)
//                .filePath("/renhe/MyFragmentImageSelector/Pictures")
                // 开启拍照功能 （默认开启）
                .showCamera()
                .build();
        FragmentImageSelector.open(imageConfig);   // 开启图片选择器
    }
}

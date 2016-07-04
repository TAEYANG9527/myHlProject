package com.itcalf.renhe.context.imageselector;

import android.content.Intent;
import android.widget.Toast;

import com.itcalf.renhe.R;

/**
 * Desction
 * Created by Yancy on 2015/12/6.
 */
public class FragmentImageSelector {


    public static final int IMAGE_REQUEST_CODE = 1002;
    public static final int IMAGE_CROP_CODE = 1003;

    private static FragmentImageConfig mImageConfig;

    public static FragmentImageConfig getImageConfig() {
        return mImageConfig;
    }

    public static void open(FragmentImageConfig config) {
        if (config == null) {
            return;
        }
        mImageConfig = config;

        if (config.getImageLoader() == null) {
            Toast.makeText(config.getFagment().getActivity(), R.string.open_camera_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Utils.existSDCard()) {
            Toast.makeText(config.getFagment().getActivity(), R.string.empty_sdcard, Toast.LENGTH_SHORT).show();
            return;
        }


        Intent intent = new Intent(config.getFagment().getActivity(), FragmentImageSelectorActivity.class);
        config.getFagment().startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

}
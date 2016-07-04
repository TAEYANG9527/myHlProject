package com.itcalf.renhe.context.imageselector;

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * FragmentImageLoader
 * Created by Yancy on 2015/12/6.
 */
public interface FragmentImageLoader extends Serializable {
    void displayImage(Context context, String path, ImageView imageView);
}
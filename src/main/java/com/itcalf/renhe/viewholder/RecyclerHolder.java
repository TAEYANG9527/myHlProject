package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.cache.CacheManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by wangning on 2015/10/12.
 */
public abstract class RecyclerHolder<T> extends RecyclerView.ViewHolder {
    private final SparseArray<View> mViews;
    public ImageLoader imageLoader;
    public Context context;
    public RecyclerView.Adapter adapter;
    public RecyclerHolder(Context context,View itemView,RecyclerView.Adapter adapter) {
        super(itemView);
        this.context = context;
        //一般不会超过8个吧
        this.mViews = new SparseArray<View>(25);
        this.imageLoader = ImageLoader.getInstance();
        this.adapter = adapter;
    }

    public SparseArray<View> getAllView() {
        return mViews;
    }
    public abstract void initView(RecyclerHolder holder, Object item, int position);
    /**
     * 通过控件的Id获取对于的控件，如果没有则加入views
     *
     * @param viewId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId
     * @param text
     * @return
     */
    public RecyclerHolder setText(int viewId, String text) {
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param drawableId
     * @return
     */
    public RecyclerHolder setImageResource(int viewId, int drawableId) {
        ImageView view = getView(viewId);
        view.setImageResource(drawableId);

        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param bm
     * @return
     */
    public RecyclerHolder setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId
     * @param url
     * @return
     */
    public RecyclerHolder setImageByUrl(int viewId, String url, DisplayImageOptions options) {
        ImageView view = getView(viewId);
        try {
            imageLoader.displayImage(url, view, options,
                    CacheManager.animateFirstDisplayListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}

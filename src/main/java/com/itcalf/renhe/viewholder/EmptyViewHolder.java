package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by wangning on 2015/10/21.
 */
public class EmptyViewHolder extends RecyclerHolder {

    public EmptyViewHolder(Context context, View itemView, RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
    }

    @Override
    public void initView(RecyclerHolder holder, Object item, int position) {

    }
}

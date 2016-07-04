package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;

/**
 * Created by wangning on 2015/10/21.
 */
public class FooterViewHolder extends RecyclerHolder {

    private ProgressBar progressBar;
    private TextView textView;

    public FooterViewHolder(Context context, View itemView, RecyclerView.Adapter adapter, int loadType) {
        super(context, itemView, adapter);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
        textView = (TextView) itemView.findViewById(R.id.text);
        switch (loadType) {
            case Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_LOADING:
                progressBar.setVisibility(View.VISIBLE);
                textView.setText(context.getString(R.string.xlistview_header_hint_loading));
                break;
            case Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_LOADING_WITHOUT_TEXT:
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
                break;
            case Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_READY:
                progressBar.setVisibility(View.GONE);
                textView.setText(context.getString(R.string.recyclerview_footer_hint_ready));
                break;
            case Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_END:
                progressBar.setVisibility(View.GONE);
                textView.setText(context.getString(R.string.recyclerview_footer_hint_end));
                break;
        }
    }

    @Override
    public void initView(RecyclerHolder holder, Object item, int position) {

    }
}

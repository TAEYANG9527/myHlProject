package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.TransferUrl2Drawable;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanNormalTextViewHolder extends RenmaiQuanViewHolder {

    public RenmaiQuanUtils renmaiQuanUtils;
    public TransferUrl2Drawable transferUrl;

    private AisenTextView contentTv;
    private TextView seeTotalContentTv;
    private ArrayList<MessageBoards.NewNoticeList> datas;
    private int contentWidth;

    public RenmaiQuanNormalTextViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        contentTv = (AisenTextView) itemView.findViewById(R.id.content_txt);
        seeTotalContentTv = (TextView) itemView.findViewById(R.id.circle_item_more);
        renmaiQuanUtils = new RenmaiQuanUtils(context);
        transferUrl = new TransferUrl2Drawable(context);
        this.contentWidth = TransferUrl2Drawable.getScreenWidth(context) - TransferUrl2Drawable.dipToPX(context, 20);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, final int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            if (null != adapter && adapter instanceof RecyclerRenmaiQuanItemAdapter) {
                RecyclerRenmaiQuanItemAdapter adapter1 = (RecyclerRenmaiQuanItemAdapter) adapter;
                datas = (ArrayList<MessageBoards.NewNoticeList>) adapter1.getDatasList();
            }
            final String content = contentInfo.getContent().trim();//正文内容
            if (!TextUtils.isEmpty(content)) {
                contentTv.setAtMemmbers(contentInfo.getAtMembers());
                contentTv.setContent(content);
                packUpTextViewWhenMostLines(position, newNoticeList.getSenderInfo().getName(), content);
            } else {
                contentTv.setText("");
            }

            seeTotalContentTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //方式一
//                    if (newNoticeList.getPackUpState() == 1) {
//                        contentTv.setMaxLines(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE);
//                        seeTotalContentTv.setText(context.getString(R.string.rmq_see_full_msg));
//                        newNoticeList.setPackUpState(0);
//                    } else {
//                        contentTv.setMaxLines(Integer.MAX_VALUE);
//                        seeTotalContentTv.setText(context.getString(R.string.rmq_see_packup_msg));
//                        newNoticeList.setPackUpState(1);
//                    }
//                    datas.set(position, newNoticeList);

                    //方式二
                    if (v.getId() == R.id.circle_item_more) {
                        TextView tv = (TextView) v;
                        TextView context = (TextView) v.getTag(R.id.item_cricle);
                        if (tv.getText().toString().equals("全文")) {
                            tv.setText("收起");
                            context.setMaxLines(100);
                        } else {
                            tv.setText("全文");
                            context.setMaxLines(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE);
                        }
                        if (newNoticeList.getPackUpState() == 1) {
                            newNoticeList.setPackUpState(0);
                        } else {
                            newNoticeList.setPackUpState(1);
                        }
                        datas.set(position, newNoticeList);
                    }
                }
            });
        }
    }

    private void packUpTextViewWhenMostLines(int position, String name, String content) {
        //方式一
//        contentTv.post(new Runnable() {
//            @Override
//            public void run() {
//                int linecount = contentTv.getLineCount();
//                if (linecount > Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE) {
//                    seeTotalContentTv.setVisibility(View.VISIBLE);
//                    if (newNoticeList.getPackUpState() == 1) {
//                        contentTv.setMaxLines(Integer.MAX_VALUE);
//                        seeTotalContentTv.setText(context.getString(R.string.rmq_see_packup_msg));
//                    } else {
//                        contentTv.setMaxLines(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE);
//                        seeTotalContentTv.setText(context.getString(R.string.rmq_see_full_msg));
//                    }
//                } else {
//                    seeTotalContentTv.setVisibility(View.GONE);
//                }
//
//            }
//        });
        //方式二
        if (newNoticeList.getPackUpState() == 0) {
            contentTv.setMaxLines(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE);
            seeTotalContentTv.setVisibility(View.GONE);
            seeTotalContentTv.setText("全文");
            measureLineCount(seeTotalContentTv, contentTv, content, position);
        } else {
            contentTv.setMaxLines(100);
            seeTotalContentTv.setVisibility(View.VISIBLE);
            seeTotalContentTv.setText("收起");
        }
        seeTotalContentTv.setTag(R.id.circle_item_more, position);
        seeTotalContentTv.setTag(R.id.item_cricle, contentTv);

    }

    private void measureLineCount(TextView tx_more, AisenTextView tx, String content, int position) {
        NumberFormat f = NumberFormat.getNumberInstance();
        f.setMaximumFractionDigits(2);
        double d;
        if (newNoticeList.getLineNum() <= 0) {
            int width = (int) TransferUrl2Drawable.getTextViewLengthWithWrap(tx, content, contentWidth);
            d = width * 1.0 / contentWidth;
            newNoticeList.setLineNum(d);
            datas.set(position, newNoticeList);
        } else {
            d = newNoticeList.getLineNum();
        }

        if (new BigDecimal(f.format(d)).compareTo(new BigDecimal(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE)) > 0) {
            tx.setMaxLines(Constants.RENMAIQUAN_CONSTANTS.RMQ_CONTENT_MAX_LINE);
            tx_more.setVisibility(View.VISIBLE);
        } else {
            tx_more.setVisibility(View.GONE);
        }
    }
}

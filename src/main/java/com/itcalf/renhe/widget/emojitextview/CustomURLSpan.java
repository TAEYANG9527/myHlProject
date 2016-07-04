package com.itcalf.renhe.widget.emojitextview;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.umeng.analytics.MobclickAgent;

import org.aisen.android.R;
import org.aisen.android.common.utils.Logger;
import org.aisen.android.support.textspan.MyURLSpan;
import org.aisen.android.ui.activity.basic.BaseActivity;
import org.aisen.android.ui.widget.MToast;

public class CustomURLSpan extends MyURLSpan {
    private static String USER_INFO_SCHEMA = "org.renhe.userinfo";
    private final String mURL;

    private int color;
    private MessageBoards.AtMemmber[] atMemmbers;
    private boolean isUnderLine = false;//设置链接是否需要下划线，默认是不需要

    public CustomURLSpan(String url, MessageBoards.AtMemmber[] atMemmbers, boolean isUnderLine) {
        super(url);
        mURL = url;
        this.atMemmbers = atMemmbers;
        this.isUnderLine = isUnderLine;
    }

    public CustomURLSpan(Parcel src) {
        super(src);
        mURL = src.readString();
    }

    public int getSpanTypeId() {
        return 11;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mURL);
    }

    public String getURL() {
        return mURL;
    }

    @Override
    public void onClick(View widget) {
        Logger.v(CustomURLSpan.class.getSimpleName(), String.format("the link(%s) was clicked ", getURL()));
        Context context = widget.getContext();
        Uri uri = Uri.parse(getURL());
        if (uri.getScheme().equals(USER_INFO_SCHEMA) && !TextUtils.isEmpty(uri.toString())
                && uri.toString().length() > USER_INFO_SCHEMA.length() + 4) {
            String uriStr = uri.toString();
            //使用USER_INFO_SCHEMA.length() + 4是因为原始的uri是org.renhe.userinfo://@王宁，需要加上“://@”这4个字符
            String userName = uriStr.substring(USER_INFO_SCHEMA.length() + 4, uriStr.length());
            if (null != atMemmbers) {
                for (MessageBoards.AtMemmber atMemmber : atMemmbers) {
                    if (atMemmber.getMemberName().equals(userName)) {
                        Intent intent = new Intent(BaseActivity.getRunningActivity(), MyHomeArchivesActivity.class);
                        intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, atMemmber.getMemberSid());
                        context.startActivity(intent);
                        ((Activity)context).overridePendingTransition(com.itcalf.renhe.R.anim.in_from_right, com.itcalf.renhe.R.anim.out_to_left);
                        break;
                    }
                }
            }
        } else {
            MobclickAgent.onEvent(context, "点击人脉圈的“网页链接”");
            // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            // context.startActivity(intent);
            Intent i = new Intent();
            i.setClass(context, WebViewActWithTitle.class);
            i.putExtra("url", getURL());
            context.startActivity(i);
            ((Activity)context).overridePendingTransition(com.itcalf.renhe.R.anim.in_from_right, com.itcalf.renhe.R.anim.out_to_left);
        }
//		if (uri.getScheme().startsWith("http")) {
//			Intent intent = new Intent();
//			intent.setAction("android.intent.action.VIEW");
//			intent.setData(uri);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(intent);
//		} else {
//			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//			intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
//			context.startActivity(intent);
//		}
    }

    public void onLongClick(View widget) {
        Uri data = Uri.parse(getURL());
        if (data != null) {
            String d = data.toString();
            String newValue = "";
            if (d.startsWith("org.aisen.android.ui")) {
                int index = d.lastIndexOf("/");
                newValue = d.substring(index + 1);
            } else if (d.startsWith("http")) {
                newValue = d;
            }
            if (!TextUtils.isEmpty(newValue)) {
                ClipboardManager cm = (ClipboardManager) widget.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("ui", newValue));
                MToast.showMessage(String.format(widget.getContext().getString(R.string.comm_hint_copied), newValue));
            }
        }
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (color == 0) {
            int[] attrs = new int[]{R.attr.colorPrimary};
            TypedArray ta = BaseActivity.getRunningActivity().obtainStyledAttributes(attrs);
            tp.setColor(ta.getColor(0, Color.BLUE));
        } else {
            tp.setColor(color);
        }
        if (isUnderLine)
            tp.setUnderlineText(true);
    }

    public void setColor(int color) {
        this.color = color;
    }
}

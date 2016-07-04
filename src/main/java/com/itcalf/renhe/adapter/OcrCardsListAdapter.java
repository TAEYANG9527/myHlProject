package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.OcrColorCard;
import com.itcalf.renhe.bean.OcrLocalCard;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.utils.PinyinUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.ocrsdk.uploadSdk.OcrCard;

public class OcrCardsListAdapter extends BaseAdapter {
    private Context context;
    private List<OcrColorCard> cardlist;
    private ImageLoader imageLoader;

    public OcrCardsListAdapter(Context _cxt, List<OcrColorCard> cardlist) {
        this.context = _cxt;
        this.cardlist = cardlist;
        this.imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return null != cardlist ? cardlist.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {
        return cardlist.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        ViewHolder viewhold;
        if (convertView == null) {
            viewhold = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.ocr_card_listitem, null);
            viewhold.listitem_ll = (RelativeLayout) convertView.findViewById(R.id.listitem_rl);
            viewhold.avatarView = (TextView) convertView.findViewById(R.id.avatar_txt);
            viewhold.nameTv = (TextView) convertView.findViewById(R.id.name_txt);
            viewhold.stateTv = (TextView) convertView.findViewById(R.id.state_txt);
            viewhold.cardIv = (ImageView) convertView.findViewById(R.id.card_iv);
            convertView.setTag(viewhold);
        } else {
            viewhold = (ViewHolder) convertView.getTag();
        }
        OcrColorCard ocrColorCard = cardlist.get(position);
        OcrCard ocrCard = ocrColorCard.getOcrCard();
        OcrLocalCard ocrLocalCard = ocrColorCard.getOcrLocalCard();
        if (null != ocrCard) {
            viewhold.avatarView.setBackgroundResource(Constants.AVATARBG[ocrColorCard.getAvatarbgIndex()]);
            imageLoader.displayImage(ocrCard.logo, viewhold.cardIv, CacheManager.ocrCardImageOptions);
            if (ocrCard.audit <= 1) {//是否无法识别，大于1就是无法识别
                if (ocrCard.fields.equals("100")) {//已完成字段，"100"为全部完成
                    viewhold.avatarView.setText(PinyinUtil.getAvatarName(ocrCard.name));
                    viewhold.nameTv.setText(ocrCard.name);
                    viewhold.stateTv.setText("识别成功，待确认");
                    viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.BC_6));
                } else {
                    if (!TextUtils.isEmpty(ocrCard.name)) {
                        viewhold.avatarView.setText(PinyinUtil.getAvatarName(ocrCard.name));
                        viewhold.nameTv.setText(ocrCard.name);
                    } else {
                        viewhold.avatarView.setText(context.getString(R.string.new_friend_list_vcard));
                        viewhold.nameTv.setText(context.getString(R.string.new_friend_list_vcard));
                    }
                    if (!TextUtils.isEmpty(ocrCard.fields))
                        viewhold.stateTv.setText(ocrCard.fields + "%");
                    else
                        viewhold.stateTv.setText("正在识别");
                    viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.C2));
                }
            } else {
                viewhold.avatarView.setText(context.getString(R.string.new_friend_list_vcard));
                viewhold.nameTv.setText("无法识别");
                viewhold.stateTv.setText("识别失败");
                viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.C2));
            }
        } else if (null != ocrLocalCard) {
            viewhold.avatarView.setBackgroundResource(Constants.AVATARBG[ocrColorCard.getAvatarbgIndex()]);
            imageLoader.displayImage(ocrLocalCard.getLogo(), viewhold.cardIv, CacheManager.ocrCardImageOptions);
            if (ocrLocalCard.getAudit() <= 1) {//是否无法识别，大于1就是无法识别
                if (!TextUtils.isEmpty(ocrLocalCard.getFields()) && ocrLocalCard.getFields().equals("100")) {//已完成字段，"100"为全部完成
                    viewhold.avatarView.setText(PinyinUtil.getAvatarName(ocrLocalCard.getName()));
                    viewhold.nameTv.setText(ocrLocalCard.getName());
                    viewhold.stateTv.setText("识别成功，待确认");
                    viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.BC_6));
                } else {
                    if (!TextUtils.isEmpty(ocrLocalCard.getName())) {
                        viewhold.avatarView.setText(PinyinUtil.getAvatarName(ocrLocalCard.getName()));
                        viewhold.nameTv.setText(ocrLocalCard.getName());
                    } else {
                        viewhold.avatarView.setText(context.getString(R.string.new_friend_list_vcard));
                        viewhold.nameTv.setText(context.getString(R.string.new_friend_list_vcard));
                    }
                    if (!TextUtils.isEmpty(ocrLocalCard.getFields()))
                        viewhold.stateTv.setText(ocrLocalCard.getFields() + "%");
                    else
                        viewhold.stateTv.setText("正在识别");
                    viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.C2));
                }
            } else {
                viewhold.avatarView.setText(context.getString(R.string.new_friend_list_vcard));
                viewhold.nameTv.setText("无法识别");
                viewhold.stateTv.setText("识别失败");
                viewhold.stateTv.setTextColor(context.getResources().getColor(R.color.C2));
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        RelativeLayout listitem_ll;
        TextView avatarView;
        TextView nameTv;
        TextView stateTv;
        ImageView cardIv;
    }
}

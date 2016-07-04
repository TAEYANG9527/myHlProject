package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.widget.emojitextview.Emotion;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.aisen.android.component.bitmaploader.BitmapLoader;
import org.aisen.android.component.bitmaploader.core.MyBitmap;

import java.util.List;

public class FaceGVAdapter extends BaseAdapter {
    private List<Emotion> list;
    private Context mContext;
    private GridView gridview;

    public FaceGVAdapter(List<Emotion> list, Context mContext, GridView gridview) {
        super();
        this.list = list;
        this.mContext = mContext;
        this.gridview = gridview;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.face_image, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.face_img);
            holder.tv = (TextView) convertView.findViewById(R.id.face_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int height = gridview.getHeight() - DensityUtil.dip2px(mContext, 144);
        if (height > 0) {
            int space = height / (4 + 1);
            gridview.setVerticalSpacing(space);
        }

        Emotion em = list.get(position);
        if (null != em) {
            MyBitmap mb = BitmapLoader.getInstance().getImageCache().getEmotionFromMemCache(em.getKey(), null);
            Bitmap b;
            if (mb != null) {
                b = mb.getBitmap();
            } else {
                b = BitmapFactory.decodeByteArray(em.getData(), 0, em.getData().length);
//                    int size = getEmojiSize();
//                    b = BitmapUtil.zoomBitmap(b, size);
                // 添加到内存中
                BitmapLoader.getInstance().getImageCache().addEmotionToMemCache(em.getKey(), null, new MyBitmap(b, em.getKey()));
            }
            holder.iv.setImageBitmap(b);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv;
        TextView tv;
    }
}

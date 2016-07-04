package com.itcalf.renhe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.view.TextView;

public class RenmaiQuanLikedGridAdapter extends BaseAdapter {
    private Context context;
    private MessageBoards.LikedList[] likedList;
    private LayoutInflater inflater; // 视图容器

    public RenmaiQuanLikedGridAdapter(Context context, MessageBoards.LikedList[] likedList) {
        this.context = context;
        this.likedList = likedList;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return likedList.length;
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {

        return 0;
    }

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.room_good_list_item, parent, false);
            holder = new ViewHolder();
            holder.image = (TextView) convertView.findViewById(R.id.dou);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String url = likedList[position].getUserface();
//        if (!TextUtils.isEmpty(url)) {
//            FragmentImageLoader imageLoader = FragmentImageLoader.getInstance();
//            try {
//                imageLoader.displayImage(url, holder.image);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        holder.image.setText("赞");
        return convertView;
    }

    public class ViewHolder {
        public TextView image;
    }
}
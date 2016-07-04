package com.itcalf.renhe.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.bean.MeItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.util.List;

import cn.renhe.heliao.idl.config.ModuleConfig;

public class MeGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater; // 视图容器

    private List<MeItem> datas;

    public MeGridAdapter(Context context, List<MeItem> datas) {
        this.context = context;
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return datas.size();
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
            convertView = inflater.inflate(R.layout.me_grid_item_layout, parent, false);
            holder = new ViewHolder();
            holder.logoIv = (ImageView) convertView.findViewById(R.id.logo);
            holder.nameTv = (TextView) convertView.findViewById(R.id.name_tv);
            holder.tipIv = (TextView) convertView.findViewById(R.id.tip_tv);
            holder.iscompleteIv = (ImageView) convertView.findViewById(R.id.iscomplete_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String picUrl = null;
        if (position < datas.size()) {
            MeItem meItem = datas.get(position);
//            if (!TextUtils.isEmpty(meItem.getLogo())) {
//                ImageLoader imageLoader = ImageLoader.getInstance();
//                try {
//                    imageLoader.displayImage(meItem.getLogo(), holder.logoIv, CacheManager.options);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            if (meItem.getType() != 0) {
                picUrl = meItem.getLogo();
//                switch (meItem.getType()) {
////                case MeItem.ME_HECAIFU:
////                    holder.logoIv.setImageResource(R.drawable.icon_me_hecaifu);
////                    break;
////                case MeItem.ME_ZANFUWU:
////                    holder.logoIv.setImageResource(R.drawable.icon_me_zanfuwu);
////                    break;
//                    case MeItem.ME_LUCKYMONEY:
//                        holder.logoIv.setImageResource(R.drawable.icon_me_money);
//                        break;
//                    case MeItem.ME_AUTH:
//                        holder.logoIv.setImageResource(R.drawable.icon_me_limit);
//                        break;
//                    case MeItem.ME_COMPLETE_PROFILE:
//                        holder.logoIv.setImageResource(R.drawable.icon_me_data);
//                        break;
//                    case MeItem.ME_IDENTIFY:
//                        holder.logoIv.setImageResource(R.drawable.icon_me_id);
//                        break;
//                    case MeItem.ME_SETTING:
//                        holder.logoIv.setImageResource(R.drawable.icon_me_setting);
//                        break;
//                    default:
//                        break;
//                }
                if (!TextUtils.isEmpty(meItem.getName()))
                    holder.nameTv.setText(meItem.getName());
                if (!TextUtils.isEmpty(meItem.getTip()))
                    holder.tipIv.setText(meItem.getTip());
                if (meItem.getType() == MeItem.ME_COMPLETE_PROFILE) {
                    if (!meItem.isArchiveComplete())
                        holder.iscompleteIv.setVisibility(View.VISIBLE);
                    else
                        holder.iscompleteIv.setVisibility(View.GONE);
                } else {
                    holder.iscompleteIv.setVisibility(View.GONE);
                }
            } else {
                holder.iscompleteIv.setVisibility(View.GONE);
                if (null != meItem.getModuleItem()) {
                    ModuleConfig.ModuleItem moduleItem = meItem.getModuleItem();
                    picUrl = moduleItem.getLogo();
//                    ImageLoader imageLoader = ImageLoader.getInstance();
//                    try {
//                        imageLoader.displayImage(moduleItem.getLogo(), holder.logoIv);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    if (!TextUtils.isEmpty(moduleItem.getName()))
                        holder.nameTv.setText(moduleItem.getName());
                    if (!TextUtils.isEmpty(moduleItem.getNote()))
                        holder.tipIv.setText(moduleItem.getNote());
                } else {
                    Logger.w("item.>>>" + meItem.getType() + meItem.getName());
                    holder.logoIv.setVisibility(View.INVISIBLE);
                    holder.nameTv.setVisibility(View.INVISIBLE);
                    holder.tipIv.setVisibility(View.INVISIBLE);
                    holder.iscompleteIv.setVisibility(View.INVISIBLE);
                }
            }
            ImageLoader imageLoader = ImageLoader.getInstance();
            try {
                imageLoader.displayImage(picUrl, holder.logoIv);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            else {
//                Logger.w("item.>>>" + meItem.getType() + meItem.getName());
//                holder.logoIv.setVisibility(View.INVISIBLE);
//                holder.nameTv.setVisibility(View.INVISIBLE);
//                holder.tipIv.setVisibility(View.INVISIBLE);
//                holder.iscompleteIv.setVisibility(View.INVISIBLE);
//            }
        }

        return convertView;
    }

    public class ViewHolder {
        public ImageView logoIv;
        public TextView nameTv;
        public TextView tipIv;
        public ImageView iscompleteIv;
    }

}
package com.itcalf.renhe.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

/**
 * 新注册用户提示框
 *
 * @author chong
 */
public class TipBox extends PopupWindow {
    private Context context;
    private int from = 0;//0扫一扫提示 1圈子提示 2附近的人脉提示 3高级人脉搜索提示 4人脉圈搜索提示
    private final int[] tips = {R.string.scan_tip, R.string.circle_tip, R.string.nearby_tip, R.string.advanced_search_tip,
            R.string.messageboard_search_tip, R.string.scan_tip};
    private TextView tipTv, ISeeTv;
    private OnItemClickListener mClickListener;
    private RelativeLayout selectPhotoRl;
    private ImageView imageView;
    private String imagePath;
    private ImageLoader imageLoader;

    public TipBox(Context context, int from, OnItemClickListener l) {
        this.context = context;
        this.from = from;
        this.mClickListener = l;

        setTouchable(true);
        setFocusable(false);
        if (from == Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM) {
            setOutsideTouchable(true);
            //			setWidth(DensityUtil.dip2px(context,context.getResources().getDimension(R.dimen.im_select_photo_latest_width)));
            //			setHeight(DensityUtil.dip2px(context,context.getResources().getDimension(R.dimen.im_select_photo_latest_height)));
        } else {
            setOutsideTouchable(false);
        }
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        setBackgroundDrawable(new ColorDrawable());

        init();
    }

    public TipBox(Context context, int from, OnItemClickListener l, String imagePath) {
        this(context, from, l);
        this.imagePath = imagePath;
        this.imageLoader = ImageLoader.getInstance();
        showImageview();
    }

    public void init() {
        final View view;
        final LayoutInflater inflater = LayoutInflater.from(context);
        if (from == 0 || from == 4) {
            view = inflater.inflate(R.layout.new_member_tip_box_left, null);
            setContentView(view);
            findView(view);
        } else if (from == Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM) {
            view = inflater.inflate(R.layout.im_select_photo_latest, null);
            setContentView(view);
            findView(view, Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM);
        } else {
            view = inflater.inflate(R.layout.new_member_tip_box_right, null);
            setContentView(view);
            findView(view);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mClickListener.onItemClick();
            }
        });
        initListener();

    }

    private void showImageview() {
        if (from == Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM) {
            File file = new File(imagePath);
            Uri uri = Uri.fromFile(file);
            imageView.setImageURI(uri);
            //图片加载器的使用代码，就这一句代码即可实现图片的加载。请注意这里的uri地址，因为我们现在实现的是获取本地图片，所以使用"file://"+图片的存储地址。如果要获取网络图片，这里的uri就是图片的网络地址。
            //			imageLoader.displayImage("file://" + imagePath, imageView,
            //					CacheManager.imageOptions);
        }
    }

    public void findView(View v) {
        tipTv = (TextView) v.findViewById(R.id.tip_tv);
        ISeeTv = (TextView) v.findViewById(R.id.i_see_tv);
        tipTv.setText(context.getResources().getString(tips[from]));
    }

    public void findView(View v, int from) {
        if (from == Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM) {
            selectPhotoRl = (RelativeLayout) v.findViewById(R.id.select_photo_rl);
            imageView = (ImageView) v.findViewById(R.id.imageView);
        }
    }

    public void initListener() {
        if (null != ISeeTv) {
            ISeeTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    mClickListener.onItemClick();
                }
            });
        }
        if (from == Constants.TIP_BOX_FROM_TYPE.TIP_BOX_FROM_IM && null != selectPhotoRl) {
            selectPhotoRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    mClickListener.onItemClick();
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick();
    }
}

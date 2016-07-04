package com.itcalf.renhe.context.auth;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.NameAuthRes;
import com.itcalf.renhe.context.auth.NameAuthTask.NameAuthTaskListener;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.zxing.card.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executors;

public class NameAuthStepThree extends NameAuthFragment {

    private ImageView iv_photo;
    private Button btn_submit;

    private Bitmap bit;
    private RelativeLayout rootLayout;
    private RequestDialog requestDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (fragmentView != null) {
            return fragmentView;
        }
        fragmentView = inflater.inflate(R.layout.fragment_nameauth_stepthree, null);
        initView();
        setListener();
        return fragmentView;
    }

    private void initView() {
        iv_photo = (ImageView) fragmentView.findViewById(R.id.iv_photo);
        btn_submit = (Button) fragmentView.findViewById(R.id.btn_submit);
        btn_submit.setEnabled(false);
        rootLayout = (RelativeLayout) fragmentView.findViewById(R.id.ly_root);
        requestDialog = new RequestDialog(mNameAuthAct, "提交中");
    }

    private void setListener() {
        btn_submit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                submitAuth();
                StatisticsUtil.statisticsCustomClickEvent(getString(R.string.android_btn_pop_realname_photo_commit_click), 0, "", null);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setPhoto(mNameAuthAct.photoFile);
    }

    public void setPhoto(String photofile) {
        if (TextUtils.isEmpty(photofile)) {
            mNameAuthAct.changeStep(2);
            return;
        }

        File srcFile = new File(photofile);
        File destFile = new File(FileUtil.getImageTempDirectory(getActivity()), "tempVCard.jpeg");
        mNameAuthAct.photoFile = destFile.getAbsolutePath();
        try {
            DisplayMetrics metric = new DisplayMetrics();
            mNameAuthAct.getWindowManager().getDefaultDisplay().getMetrics(metric);
            int limitwidth = metric.widthPixels * 3 / 5;
            int limitheight = metric.heightPixels * 3 / 5;
            if (!FileUtil.createImageThumbnail(srcFile, destFile, limitwidth, limitheight)) {
                mNameAuthAct.changeStep(2);
                return;
            }
            BitmapDrawable drawable = (BitmapDrawable) BitmapDrawable
                    .createFromStream(new FileInputStream(destFile.getAbsolutePath()), "src");
            bit = drawable.getBitmap();
            if (bit.getHeight() >= limitheight) {
                Matrix matrix = new Matrix();
                matrix.postScale(0.6f, 0.6f);
                bit = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
            }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) iv_photo.getLayoutParams();
            lp.width = bit.getWidth();
            lp.height = bit.getHeight();
            iv_photo.setLayoutParams(lp);
            iv_photo.setImageBitmap(bit);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_submit.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        if (bit != null && !bit.isRecycled()) {
            bit.recycle();
            bit = null;
            iv_photo.setImageBitmap(null);
        }
    }

    //提交审核
    public void submitAuth() {
        mNameAuthAct.iscanBack = false;
        requestDialog.addFade(rootLayout);
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        String adsid = RenheApplication.getInstance().getUserInfo().getAdSId();
        new NameAuthTask(getActivity(), new NameAuthTaskListener() {

            @Override
            public void postExecute(NameAuthRes result) {
                if (null != getActivity() && isAdded()) {
                    if (result != null) {
                        if (result.state == 1) {
                            mNameAuthAct.dealAuthResult(0);//审核中
                        } else {
                            mNameAuthAct.dealAuthError(result.state);
                        }
                    } else {
                        ToastUtil.showToast(mNameAuthAct, "认证失败，请检查网络稍后再试");
                    }
                    requestDialog.removeFade(rootLayout);
                    mNameAuthAct.iscanBack = true;
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), sid, adsid, "1", mNameAuthAct.name, mNameAuthAct.personalId,
                mNameAuthAct.photoFile);
    }

}

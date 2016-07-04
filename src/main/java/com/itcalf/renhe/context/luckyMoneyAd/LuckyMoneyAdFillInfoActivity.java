package com.itcalf.renhe.context.luckyMoneyAd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.cropImage.CropAdLogoImage;
import com.itcalf.renhe.context.imageselector.ImageSelector;
import com.itcalf.renhe.context.imageselector.ImageSelectorActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.AdUploadPhoto;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.imageUtil.ImageSelectorUtil;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.squareup.okhttp.Request;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.renhe.heliao.idl.money.red.HeliaoSendAdRed;

/**
 * #红包广告#
 * 填写红包广告信息页面，包括广告语、广告logo、广告链接等
 * Created by wangning on 2016/6/3.
 */
public class LuckyMoneyAdFillInfoActivity extends BaseActivity {
    @BindView(R.id.ad_content_et)
    EditText adContentEt;
    @BindView(R.id.ad_content_maxlength_tv)
    TextView adContentMaxlengthTv;
    @BindView(R.id.ad_logo_iv)
    ImageView adLogoIv;
    @BindView(R.id.ad_logo_ll)
    LinearLayout adLogoLl;
    @BindView(R.id.ad_link_tv)
    TextView adLinkTv;
    @BindView(R.id.ad_link_ll)
    LinearLayout adLinkLl;
    @BindView(R.id.ad_link_tip_tv)
    TextView adLinkTipTv;
    @BindView(R.id.next_bt)
    Button nextBt;

    /**
     * 常量
     */
    public static final int AD_CONTENT_MAX_LENGTH_DEFAULT = 300;//广告语默认最大字数是300
    public static final int AD_URL_MAX_LENGTH_DEFAULT = 500;//广告链接默认最大字数是500
    private int ID_TASK_LUCKY_AD_CONFIG = TaskManager.getTaskId();//发红包之前获取配置信息
    private int ID_TASK_LUCKY_PRE_BUILD = TaskManager.getTaskId();//预生成红包广告
    private static final int REQUEST_CODE_FILL_URL = 1;//添加广告链接的回调
    private static final int REQUEST_CODE_LUCKY_AD_SEND = 2;//添加发布广告红包后的回调
    private static final int REQUEST_CODE_CROP_LOGO = 3;//添加裁剪广告红包logo后的回调
    /**
     * 变量
     */
    private int adContentMaxLength = AD_CONTENT_MAX_LENGTH_DEFAULT;
    private int adUrlMaxLength = AD_URL_MAX_LENGTH_DEFAULT;
    private String conversationId;//悟空会话的conversationId
    private int conversationGroupNum;//悟空会话的类型 群聊总人数
    private String cropLogoPath;
    private int luckyMoneyMaxCount, luckyMoneyMinCount;// 最大、最小红包个数
    private String minTotalLuckyMoneyStr;//最小红包起步总金额
    private String logoUrl;//logo上传到服务器后返回的logo地址
    private String luckyAdSid;//广告Id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.luckymoney_ad_fillinfo_layout);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem helpItem = menu.findItem(R.id.item_help);
        helpItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        helpItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_help:
                Intent intent = new Intent(LuckyMoneyAdFillInfoActivity.this, WebViewActWithTitle.class);
                intent.putExtra("url", Constants.LUCKYAD_INVITE_HELP_URL);
                startHlActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue("发红包广告");
        initAdUi();
        checkNextBtEnable();
    }

    @Override
    protected void initData() {
        super.initData();
        conversationId = getIntent().getStringExtra("conversationId");
        conversationGroupNum = getIntent().getIntExtra("conversationGroupNum", 1);
        if (!TextUtils.isEmpty(conversationId)) {
            getLuckyMoneyAdConfig(conversationId);
        } else {
            ToastUtil.showToast(this, getString(R.string.lucky_money_send_infoerror_tip));
            finish();
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        adContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (adContentMaxLength - s.toString().trim().length() <= 10) {
                    adContentMaxlengthTv.setVisibility(View.VISIBLE);
                    adContentMaxlengthTv.setText((adContentMaxLength - s.toString().trim().length()) + "");
                } else {
                    adContentMaxlengthTv.setVisibility(View.INVISIBLE);
                }
                checkNextBtEnable();
            }
        });
    }

    /**
     * 初始化一些提示UI，比如输入框最大字数
     */
    private void initAdUi() {
//        adContentEt.setHint(MessageFormat.format(getString(R.string.ad_content_hint), adContentMaxLength + ""));
        //设置最大字数
        InputFilter[] filters = {new InputFilter.LengthFilter(adContentMaxLength)};
        adContentEt.setFilters(filters);
        adLinkTipTv.setText(MessageFormat.format(getString(R.string.ad_link_tip), adUrlMaxLength + ""));
    }

    /**
     * 获取红包广告配置
     */
    private void getLuckyMoneyAdConfig(String converstaionId) {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_AD_CONFIG)) {
            showMaterialLoadingDialog();
            grpcController.getLuckyMoneyAdConfig(ID_TASK_LUCKY_AD_CONFIG, converstaionId);
        }
    }

    /**
     * 获取红包广告配置
     */
    private void preBuildLuckyMoneyAd(String adContent, String logoUrl, String adLink) {
        if (checkGrpcBeforeInvoke(ID_TASK_LUCKY_PRE_BUILD)) {
            showMaterialLoadingDialog(R.string.waitting, false);
            if (TextUtils.isEmpty(logoUrl))
                logoUrl = "";
            grpcController.preBuildLuckyMoneyAd(ID_TASK_LUCKY_PRE_BUILD, adContent, logoUrl, adLink);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        hideMaterialLoadingDialog();
        if (null != result) {
            if (result instanceof HeliaoSendAdRed.AdRedConfigResponse) {
                HeliaoSendAdRed.AdRedConfigResponse adRedConfigResponse = (HeliaoSendAdRed.AdRedConfigResponse) result;
                luckyMoneyMaxCount = adRedConfigResponse.getMaxRedCount();
                luckyMoneyMinCount = adRedConfigResponse.getMinRedCount();
                minTotalLuckyMoneyStr = adRedConfigResponse.getMinTotalRedAmount();
                adContentMaxLength = adRedConfigResponse.getAdContentMaxLength();
                initAdUi();
            } else if (result instanceof HeliaoSendAdRed.BuildAdRedResponse) {
                HeliaoSendAdRed.BuildAdRedResponse buildAdRedResponse = (HeliaoSendAdRed.BuildAdRedResponse) result;
                luckyAdSid = buildAdRedResponse.getAdSid();
                jumpToNextActivity();
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        hideMaterialLoadingDialog();
        ToastUtil.showToast(this, msg);
        if (type == ID_TASK_LUCKY_AD_CONFIG) {//如果配置信息获取异常，关闭页面
            finish();
        }
    }

    @OnClick({R.id.ad_logo_ll, R.id.ad_link_ll, R.id.next_bt})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ad_logo_ll:
                ImageSelectorUtil.initUpdateAvatarActivityImageSelector(LuckyMoneyAdFillInfoActivity.this);
                break;
            case R.id.ad_link_ll:
                Intent intent = new Intent(LuckyMoneyAdFillInfoActivity.this, LuckyMoneyAdFillUrlActivity.class);
                intent.putExtra("adUrlMaxLength", adUrlMaxLength);
                intent.putExtra("adUrl", getAdUrl());
                startHlActivityForResult(intent, REQUEST_CODE_FILL_URL);
                break;
            case R.id.next_bt:
                preBuildLuckyMoneyAd(adContentEt.getText().toString().trim(), logoUrl, adLinkTv.getText().toString().trim());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILL_URL:
                    adLinkTv.setText(data.getStringExtra("adUrl"));
                    break;
                case ImageSelector.IMAGE_REQUEST_CODE://用imageselector选择好的图片回调
                    if (null != data) {
                        List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
                        if (null != pathList && !pathList.isEmpty() && !TextUtils.isEmpty(pathList.get(0))) {
                            toCropLogo(pathList.get(0));
                        } else {
                            ToastUtil.showToast(this, R.string.ad_logo_not_exist);
                        }
                    }
                    break;
                case REQUEST_CODE_LUCKY_AD_SEND:
                    finish();
                    break;
                case REQUEST_CODE_CROP_LOGO:
                    cropLogoPath = data.getStringExtra("cropImagePath");
                    if (!TextUtils.isEmpty(cropLogoPath)) {
//                        ThumbnailUtil.compressAndRotateToThumbFileWithdestinationPath(LuckyMoneyAdFillInfoActivity.this,
//                                cropLogoPath, FileUtils.SDPATH);//压缩图片
                        Bitmap imageBmp = BitmapFactory.decodeFile(cropLogoPath);
                        if (null != imageBmp)
                            adLogoIv.setImageBitmap(imageBmp);
                        uploadLogoPhoto();
                    } else {
                        ToastUtil.showToast(this, R.string.ad_logo_not_exist);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 调用自定义裁剪工具裁剪图片方法实现
     *
     * @param path
     */
    public void toCropLogo(String path) {
        Intent intent = new Intent(this, CropAdLogoImage.class);
        // 设置裁剪
        intent.putExtra("path", path);
        startActivityForResult(intent, REQUEST_CODE_CROP_LOGO);
    }

    private void jumpToNextActivity() {
        Intent intent2 = new Intent(LuckyMoneyAdFillInfoActivity.this, LuckyMoneyAdSendActivity.class);
        intent2.putExtra("conversationId", conversationId);
        intent2.putExtra("luckyAdSid", luckyAdSid);
        intent2.putExtra("conversationGroupNum", conversationGroupNum);
        intent2.putExtra("luckyMoneyMaxCount", luckyMoneyMaxCount);
        intent2.putExtra("luckyMoneyMinCount", luckyMoneyMinCount);
        intent2.putExtra("minTotalLuckyMoney", minTotalLuckyMoneyStr);
        startHlActivityForResult(intent2, REQUEST_CODE_LUCKY_AD_SEND);
    }

    private String getAdUrl() {
        return adLinkTv.getText().toString().trim();
    }

    /**
     * 根据情况更新按钮状态
     */
    private void checkNextBtEnable() {
        if (TextUtils.isEmpty(adContentEt.getText().toString().trim())) {
            nextBt.setEnabled(false);
        } else {
            nextBt.setEnabled(true);
        }
    }

    /**
     * 上传logo
     */
    private void uploadLogoPhoto() {
        showMaterialLoadingDialog(R.string.ad_logo_uploading, true);
        if (!TextUtils.isEmpty(cropLogoPath)) {
            File file = new File(cropLogoPath);
            if (null != file && file.exists()) {
                final Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
                reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                OkHttpClientManager.postPhotoAsyn(Constants.Http.AD_UPLOAD_ADCOVER,
                        "image", file, reqParams, AdUploadPhoto.class,
                        new OkHttpClientManager.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                ToastUtil.showToast(LuckyMoneyAdFillInfoActivity.this, R.string.ad_logo_upload_fail);
                                hideMaterialLoadingDialog();
                                //删除本地的裁剪缓存图片crop.jpg
                                FileUtil.deleteFile(cropLogoPath);
                            }

                            @Override
                            public void onResponse(Object response) {
                                hideMaterialLoadingDialog();
                                if (null != response) {
                                    AdUploadPhoto adUploadPhoto = (AdUploadPhoto) response;
                                    if (adUploadPhoto.getState() == 1) {
                                        logoUrl = adUploadPhoto.getCover();
                                        ToastUtil.showToast(LuckyMoneyAdFillInfoActivity.this, R.string.ad_logo_upload_success);
                                    } else {
                                        ToastUtil.showToast(LuckyMoneyAdFillInfoActivity.this, R.string.ad_logo_upload_fail);
                                    }
                                } else {

                                }
                                //删除本地的裁剪缓存图片crop.jpg
                                FileUtil.deleteFile(cropLogoPath);
                            }
                        }, "luckyMoneyAd");
            } else {
                hideMaterialLoadingDialog();
                ToastUtil.showToast(this, R.string.ad_logo_not_exist);
            }
        } else {
            hideMaterialLoadingDialog();
            ToastUtil.showToast(this, R.string.ad_logo_upload_fail);
        }
    }
}

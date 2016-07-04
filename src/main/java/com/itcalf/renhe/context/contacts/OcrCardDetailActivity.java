package com.itcalf.renhe.context.contacts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.OcrLocalCard;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.OcrCardOperation;
import com.itcalf.renhe.eventbusbean.FinishOcrActivityEvent;
import com.itcalf.renhe.eventbusbean.RefreshNewFriendListEvent;
import com.itcalf.renhe.task.SaveOcrCardTask;
import com.itcalf.renhe.utils.ContactsUtil;
import com.itcalf.renhe.utils.OcrLocalCardUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.EditText;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import cn.ocrsdk.uploadSdk.OcrActivityCamera;
import cn.ocrsdk.uploadSdk.OcrBackCards;
import cn.ocrsdk.uploadSdk.OcrBackPicture;
import cn.ocrsdk.uploadSdk.OcrCard;
import cn.ocrsdk.uploadSdk.OcrErrorCode;
import cn.ocrsdk.uploadSdk.OcrServer;
import de.greenrobot.event.EventBus;

/**
 * Created by wangning on 2015/11/26.
 */
public class OcrCardDetailActivity extends BaseActivity {
    //初始化server
    private OcrServer maiKeXunServer;
    private boolean isMaiKeXunInit;
    private ImageLoader imageLoader;
    private OcrCard ocrCard;
    private String uuid;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler;
    //初始化VIEW
    private MenuItem optionItem;
    private ImageView cardIv;
    private EditText nameEt;
    private EditText jobEt;
    private EditText companyEt;
    private EditText mobile1Et;
    private EditText mobile2Et;
    private EditText tel1Et;
    private EditText tel2Et;
    private EditText emailEt;
    private EditText qqEt;
    private EditText websiteEt;
    private EditText addressEt;
    private LinearLayout mobile2Ll;
    private LinearLayout tel2Ll;
    private TextView scanProgressTv;

    /**
     * 是否正在识别、失败失败，因为是循环调用获取详情，有可能其中某次调用出现异常，导致正在识别中的名片提示>网络中断，无法获取名片，然后又现实正常
     */
    private boolean hasIdenResult = false;
    private LinearLayout idenSuccessLl;
    private LinearLayout idenFailLl;

    private OcrLocalCard ocrLocalCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.ocr_card_detail);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        optionItem = menu.findItem(R.id.item_save);
        optionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        optionItem.setIcon(null);
        optionItem.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBack();
                return true;
            case R.id.item_save:
                if (item.getTitle().equals(getString(R.string.ocr_card_actionbar_option_item_save))) {//保存
                    MobclickAgent.onEvent(OcrCardDetailActivity.this, getString(R.string.ocl_card_detail_save));
                    saveOcrCard(uuid);
                } else if (item.getTitle().equals(getString(R.string.ocr_card_actionbar_option_item_delete))) {//删除
                    MobclickAgent.onEvent(OcrCardDetailActivity.this, getString(R.string.ocl_card_detail_delete));
                    deleteOcrCard(uuid);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(getString(R.string.ocr_card_indenting));
        maiKeXunServer = RenheApplication.getInstance().getMaiKeXunServer();//脉可寻 上传名片server
        //判断是否通过验证
        isMaiKeXunInit = (null != maiKeXunServer && maiKeXunServer.isAuth());//账户是否初始化（key，secret）
        imageLoader = ImageLoader.getInstance();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1://识别中
                        idenSuccessLl.setVisibility(View.VISIBLE);
                        idenFailLl.setVisibility(View.GONE);
                        hasIdenResult = true;
                        if (null != ocrCard) {
                            if (!ocrCard.fields.equals("100")) {//还未识别完成
                                setTextValue(getString(R.string.ocr_card_indenting));
                                scanProgressTv.setVisibility(View.VISIBLE);
                                scanProgressTv.setText(ocrCard.fields + "%");
                                if (null == timer)
                                    startTimer();
                            } else {
                                scanProgressTv.setVisibility(View.GONE);
                                setTextValue(getString(R.string.ocr_card_indent_success));
                                if (null != optionItem) {
                                    optionItem.setVisible(true);
                                    optionItem.setTitle(getString(R.string.ocr_card_actionbar_option_item_save));
                                }
                                if (null != timer) {
                                    timer.cancel();
                                }
                            }
                        }
                        initInfoView();
                        break;
                    case 2://识别失败
                        idenSuccessLl.setVisibility(View.GONE);
                        idenFailLl.setVisibility(View.VISIBLE);
                        hasIdenResult = true;
                        setTextValue(getString(R.string.ocr_card_indent_error));
                        scanProgressTv.setVisibility(View.VISIBLE);
                        scanProgressTv.setText(getString(R.string.ocr_card_indent_error_tip1));
                        if (null != optionItem) {
                            optionItem.setVisible(true);
                            optionItem.setTitle(getString(R.string.ocr_card_actionbar_option_item_delete));
                        }
                        initInfoView();
                        break;
                    case 3://获取名片信息失败
                        idenSuccessLl.setVisibility(View.VISIBLE);
                        idenFailLl.setVisibility(View.GONE);
                        setTextValue(getString(R.string.ocr_card_indent_error));
                        if (!hasIdenResult) {
                            scanProgressTv.setVisibility(View.VISIBLE);
                            scanProgressTv.setText(getString(R.string.ocr_card_indent_error_tip2));
                        }
                        break;
                }
                return true;
            }
        });
        if (null != getIntent().getStringExtra("uuid")) {
            uuid = getIntent().getStringExtra("uuid");
            if (null != getIntent().getSerializableExtra("ocrLocalCard"))
                ocrLocalCard = (OcrLocalCard) getIntent().getSerializableExtra("ocrLocalCard");
            initLocalCardInfoView();
            if (!TextUtils.isEmpty(uuid)) {
                getCardImage(uuid);
                getCardDetail(uuid);
            }
        }
    }

    @Override
    protected void findView() {
        super.findView();
        cardIv = (ImageView) findViewById(R.id.card_iv);
        nameEt = (EditText) findViewById(R.id.name_et);
        jobEt = (EditText) findViewById(R.id.job_et);
        companyEt = (EditText) findViewById(R.id.company_et);
        mobile1Et = (EditText) findViewById(R.id.mobile1_et);
        mobile2Et = (EditText) findViewById(R.id.mobile2_et);
        tel1Et = (EditText) findViewById(R.id.tel1_et);
        tel2Et = (EditText) findViewById(R.id.tel2_et);
        emailEt = (EditText) findViewById(R.id.email_et);
        qqEt = (EditText) findViewById(R.id.qq_et);
        websiteEt = (EditText) findViewById(R.id.website_et);
        addressEt = (EditText) findViewById(R.id.address_et);
        mobile2Ll = (LinearLayout) findViewById(R.id.mobile2_ll);
        tel2Ll = (LinearLayout) findViewById(R.id.tel2_ll);
        scanProgressTv = (TextView) findViewById(R.id.card_scan_progress_tv);
        idenSuccessLl = (LinearLayout) findViewById(R.id.iden_success_ll);
        idenFailLl = (LinearLayout) findViewById(R.id.iden_fail_ll);
    }

    @Override
    protected void initListener() {
        super.initListener();
        //注册EventBus
        EventBus.getDefault().register(this);
        scanProgressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != ocrCard) {
                    if (ocrCard.audit > 1) {
                        Intent intent = new Intent(OcrCardDetailActivity.this, OcrActivityCamera.class);
                        startActivity(intent);
//                        setResult(Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE);
//                        finish();
                    }
                } else {
                    scanProgressTv.setVisibility(View.GONE);
                    getCardDetail(uuid);
                }
            }
        });
    }

    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (null != ocrCard) {
                    getCardDetail(uuid);
                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, Constants.MAI_KE_XUN.DETAIL_TIMER_DELAY);//开启定时器,每3秒执行一次
    }

    private void getCardImage(String uuid) {
        if (isMaiKeXunInit) {
            maiKeXunServer.getCardImage(uuid, new OcrBackPicture() {
                @Override
                public void onBack(File file) {
                    if (null != file) {
                        Uri uri = Uri.fromFile(file);
                        imageLoader.displayImage("file://" + uri.getPath(), cardIv, CacheManager.ocrCardImageOptions);
//                        cardIv.setImageURI(uri);
//                        imageLoader.loadImage("file://" + uri.getPath(), new ImageAnimateFirstDisplayListener());
                    }
                }
            });
        }
    }

    /**
     * 获取脉可寻名片数据
     */
    private void getCardDetail(String uuid) {
        if (isMaiKeXunInit) {
            String[] uuids = new String[]{uuid};
            maiKeXunServer.getDataWithUUID(uuids, new OcrBackCards() {
                @Override
                public void onBack(int code, String info, OcrCard[] cards) {
                    if (code == OcrErrorCode.CODE_SUCCESS) {
                        if (null != cards && cards.length > 0) {
                            ocrCard = cards[0];
                            Logger.d("获取数据成功！===> " + ocrCard.fields);
                            if (null != ocrCard) {
                                // audit 是否无法识别，大于1就是无法识别
                                if (ocrCard.audit <= 1) {
                                    Message message = new Message();
                                    message.what = 1;
                                    handler.sendMessage(message);
                                } else {
                                    Message message = new Message();
                                    message.what = 2;
                                    handler.sendMessage(message);
                                }
                            } else {
                                Message message = new Message();
                                message.what = 3;
                                handler.sendMessage(message);
                            }
                        } else {
                            Logger.w("获取的数据成功，但为空！");
                        }
                    } else {
                        Logger.e("脉可寻获取数据失败！错误信息是=====》 " + info);
                        Message message = new Message();
                        message.what = 3;
                        handler.sendMessage(message);
                    }
                }
            });
        } else {
            Logger.e("脉可寻未验证！");
        }
    }

    private void initLocalCardInfoView() {
        if (null != ocrLocalCard) {
            if (!TextUtils.isEmpty(ocrLocalCard.getName()))
                nameEt.setText(ocrLocalCard.getName());
            if (!TextUtils.isEmpty(ocrLocalCard.getDuty()))
                jobEt.setText(ocrLocalCard.getDuty());
            if (!TextUtils.isEmpty(ocrLocalCard.getCname()))
                companyEt.setText(ocrLocalCard.getCname());
            if (!TextUtils.isEmpty(ocrLocalCard.getMobile1()))
                mobile1Et.setText(ocrLocalCard.getMobile1());
            if (!TextUtils.isEmpty(ocrLocalCard.getMobile2())) {
                mobile2Ll.setVisibility(View.VISIBLE);
                mobile2Et.setText(ocrLocalCard.getMobile2());
            }
            if (!TextUtils.isEmpty(ocrLocalCard.getTel1()))
                tel1Et.setText(ocrLocalCard.getTel1());
            if (!TextUtils.isEmpty(ocrLocalCard.getTel2())) {
                tel2Ll.setVisibility(View.VISIBLE);
                tel2Et.setText(ocrLocalCard.getTel2());
            }
            if (!TextUtils.isEmpty(ocrLocalCard.getEmail()))
                emailEt.setText(ocrLocalCard.getEmail());
            if (!TextUtils.isEmpty(ocrLocalCard.getFax()))
                qqEt.setText(ocrLocalCard.getFax());
            if (!TextUtils.isEmpty(ocrLocalCard.getWebsite()))
                websiteEt.setText(ocrLocalCard.getWebsite());
            if (!TextUtils.isEmpty(ocrLocalCard.getAddress()))
                addressEt.setText(ocrLocalCard.getAddress());
        }
    }

    private void initInfoView() {
        if (null != ocrCard) {
            if (!TextUtils.isEmpty(ocrCard.name))
                nameEt.setText(ocrCard.name);
            if (!TextUtils.isEmpty(ocrCard.duty))
                jobEt.setText(ocrCard.duty);
            if (!TextUtils.isEmpty(ocrCard.cname))
                companyEt.setText(ocrCard.cname);
            if (!TextUtils.isEmpty(ocrCard.mobile1))
                mobile1Et.setText(ocrCard.mobile1);
            if (!TextUtils.isEmpty(ocrCard.mobile2)) {
                mobile2Ll.setVisibility(View.VISIBLE);
                mobile2Et.setText(ocrCard.mobile2);
            }
            if (!TextUtils.isEmpty(ocrCard.tel1))
                tel1Et.setText(ocrCard.tel1);
            if (!TextUtils.isEmpty(ocrCard.tel2)) {
                tel2Ll.setVisibility(View.VISIBLE);
                tel2Et.setText(ocrCard.tel2);
            }
            if (!TextUtils.isEmpty(ocrCard.email))
                emailEt.setText(ocrCard.email);
            if (!TextUtils.isEmpty(ocrCard.fax))
                qqEt.setText(ocrCard.fax);
            if (!TextUtils.isEmpty(ocrCard.website))
                websiteEt.setText(ocrCard.website);
            if (!TextUtils.isEmpty(ocrCard.address))
                addressEt.setText(ocrCard.address);
        }
    }

    /**
     * 保存名片
     *
     * @param uuid 需要上传名片的uuid
     */
    private void saveOcrCard(final String uuid) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("uuid", uuid);
        reqParams.put("name", nameEt.getText().toString());
        reqParams.put("address", addressEt.getText().toString());
        reqParams.put("email", emailEt.getText().toString());
        reqParams.put("title", jobEt.getText().toString());
        reqParams.put("company", companyEt.getText().toString());
        reqParams.put("mobile", mobile1Et.getText().toString() + "," + mobile2Et.getText().toString());
        reqParams.put("url", ocrCard.logo);
        reqParams.put("fax", qqEt.getText().toString());
        reqParams.put("tel", tel1Et.getText().toString() + "," + tel2Et.getText().toString());
        reqParams.put("webAddress", websiteEt.getText().toString());
        new SaveOcrCardTask(this, reqParams, new SaveOcrCardTask.IBack() {
            @Override
            public void onPre() {
                if (null != materialDialogsUtil) {
                    materialDialogsUtil.showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
                    materialDialogsUtil.show();
                }
            }

            @Override
            public void doPost(OcrCardOperation result) {
                if (null != materialDialogsUtil)
                    materialDialogsUtil.dismiss();
                if (null != result) {
                    if (result.getState() == 1) {
                        EventBus.getDefault().post(new RefreshNewFriendListEvent());//通过eventbus通知新的好友界面加载新数据
                        //更新本地联系人列表，maxid
                        new ContactsUtil(OcrCardDetailActivity.this).SyncContacts();
                        Logger.d("名片保存成功！uuid是==》 " + uuid);
                        OcrLocalCardUtil.delete(uuid);
//                        Intent intent = new Intent();
//                        intent.putExtra("deletedUuid", uuid);
//                        setResult(RESULT_OK, intent);
//                        finish();
//                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

                        Intent intent = new Intent(OcrCardDetailActivity.this, NewFriendsAct.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        setResult(Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE);
                        finish();
                    }
                } else {
                    ToastUtil.showToast(OcrCardDetailActivity.this, R.string.network_error_message);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool());
    }

    /**
     * 删除名片
     *
     * @param uuid 需要上传名片的uuid
     */
    private void deleteOcrCard(final String uuid) {
        Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("uuid", uuid);
        reqParams.put("type", 2);//1.表示上传，2.表示删除
        OkHttpClientManager.postAsyn(Constants.Http.UPLOADORDELETE_OCR_CARD, reqParams, OcrCardOperation.class, new OkHttpClientManager.ResultCallback() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                if (null != materialDialogsUtil) {
                    materialDialogsUtil.showIndeterminateProgressDialog(R.string.deleting).cancelable(false).build().show();
                }
            }

            @Override
            public void onError(Request request, Exception e) {

            }

            @Override
            public void onResponse(Object response) {
                if (null != response && response instanceof OcrCardOperation) {
                    OcrCardOperation result = (OcrCardOperation) response;
                    if (result.getState() == 1) {
                        Logger.d("名片删除成功！uuid是==》 " + uuid);
                        OcrLocalCardUtil.delete(uuid);
                        Intent intent = new Intent();
                        intent.putExtra("deletedUuid", uuid);
                        setResult(RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                    }
                }
            }

            @Override
            public void onAfter() {
                super.onAfter();
                if (null != materialDialogsUtil)
                    materialDialogsUtil.dismiss();
            }
        });
    }

    /**
     * 图片加载第一次显示监听器
     *
     * @author Administrator
     */
    class ImageAnimateFirstDisplayListener extends SimpleImageLoadingListener {

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                int width = loadedImage.getWidth();
                int height = loadedImage.getHeight();
                if (width >= height) {
                    cardIv.setImageBitmap(loadedImage);
                } else {
                    rotateImage(loadedImage, cardIv);
                }
            }
        }
    }

    private void rotateImage(Bitmap bitmap, ImageView imageView) {
        Matrix matrix = new Matrix();
        matrix.setRotate(90); //设置翻转的角度
        //重新绘制翻转后的图片
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(bitmap);
    }

    private void goBack() {
        if (null == materialDialogsUtil)
            return;
        if (null != optionItem && optionItem.isVisible() && optionItem.getTitle().equals(getString(R.string.ocr_card_actionbar_option_item_save))) {//保存
            materialDialogsUtil.getBuilder(R.string.material_dialog_title, "退出此次编辑？", R.string.material_dialog_sure,
                    R.string.material_dialog_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {

                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }
                    });
            materialDialogsUtil.show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * eventBus 接收发送消息成功之后的回调
     *
     * @param event
     */
    public void onEventMainThread(FinishOcrActivityEvent event) {
        Logger.e("event bus onEventMainThread 刷新新的好友列表");
        setResult(Constants.MAI_KE_XUN.CARD_DETAIL_RESULT_CODE);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != timer) {
            timer.cancel();//销毁定时器
        }
        EventBus.getDefault().unregister(this);//反注册EventBus
    }
}

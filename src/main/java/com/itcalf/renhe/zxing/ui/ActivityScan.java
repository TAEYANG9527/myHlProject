package com.itcalf.renhe.zxing.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.more.GetSelfTwoDimenCodeTask;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.SelfTwoDimenCodeMessageBoardOperation;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;
import com.itcalf.renhe.zxing.camera.CameraManager;
import com.itcalf.renhe.zxing.card.FileUtil;
import com.itcalf.renhe.zxing.decoding.CaptureActivityHandler;
import com.itcalf.renhe.zxing.decoding.InactivityTimer;
import com.itcalf.renhe.zxing.decoding.RGBLuminanceSource;
import com.itcalf.renhe.zxing.view.ViewfinderView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.Executors;

public class ActivityScan extends BaseActivity implements View.OnClickListener, Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private TextView tv_qrcode, tv_vcard, tv_left, tv_right;
    private Button btn_takepicture;

    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private String photo_path;
    private Bitmap scanBitmap;

    private boolean playBeep;
    private boolean isFlicker = false; //判断闪光灯是否开启
    private boolean isQrcode = false; // 判断切换到哪个模块
    private boolean hasSurface;
    private static final float BEEP_VOLUME = 0.10f;
    private static final int PARSE_BARCODE_SUC = 300;
    private static final int PARSE_BARCODE_FAIL = 303;
    private MaterialDialogsUtil materialDialogsUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        getTemplate().doInActivity(this, R.layout.activity_scan);
    }

    @Override
    protected void findView() {
        super.findView();

        //相机初始化
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

        hasSurface = false;
        //		inactivityTimer = new InactivityTimer(this);
        tv_qrcode = (TextView) findViewById(R.id.tv_qrcode);
        tv_vcard = (TextView) findViewById(R.id.tv_vcard);
        tv_left = (TextView) findViewById(R.id.tv_left);
        tv_right = (TextView) findViewById(R.id.tv_right);
        btn_takepicture = (Button) findViewById(R.id.btn_takepicture);

        tv_qrcode.setOnClickListener(this);
        tv_vcard.setOnClickListener(this);
        btn_takepicture.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        btnChange(getIntent().getIntExtra("state", 0));
        getMyQrcode(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem addfriendsItem2 = menu.findItem(R.id.item_pic);
        addfriendsItem2.setTitle("相册");
        addfriendsItem2.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menu) {
        if (menu.getItemId() == R.id.item_pic) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(menu);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (null != materialDialogsUtil)
                materialDialogsUtil.dismiss();
            switch (msg.what) {
                case PARSE_BARCODE_SUC:
                    onResultHandler((String) msg.obj, scanBitmap);
                    break;
                case PARSE_BARCODE_FAIL:
                    Toast.makeText(ActivityScan.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    /**
     * 跳转到显示的页面
     */
    private void onResultHandler(String resultString, Bitmap bitmap) {
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(ActivityScan.this, "不是有效的二维码照片!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Intent intent = new Intent(ActivityScan.this, ActivityQrcodeResult.class);
            Bundle bundle = new Bundle();
            bundle.putString("result", resultString);
            //		bundle.putParcelable("bitmap", bitmap);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /**
     * 解析手机自带的二维码图片
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        inactivityTimer = new InactivityTimer(this);
        btn_takepicture.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        } else {
            CameraManager.get().setContinuousFocusParams(false);
            CameraManager.get().stopPreview();
        }
        CameraManager.get().closeDriver();
        isFlicker = false;
        invalidateOptionsMenu();
        inactivityTimer.shutdown();
    }

    @Override
    protected void onDestroy() {
        //		inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        if (isQrcode) {
            inactivityTimer.onActivity();
            //		playBeepSoundAndVibrate();
            String resultString = result.getText();
            if (resultString.equals("")) {
                Toast.makeText(ActivityScan.this, "二维码已损坏", Toast.LENGTH_SHORT).show();
            } else {
                onResultHandler(resultString, barcode);
            }
            ActivityScan.this.finish();
            //和聊统计
            String content = "5.136" + LoggerFileUtil.getConstantInfo(ActivityScan.this);
            LoggerFileUtil.writeFile(content, true);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (Exception e) {
            cameraInitFail();
            return;
        }

        changeCameraState(isQrcode);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        CameraManager.get().closeDriver();

    }

    private void changeCameraState(boolean isqrcode) {

        CameraManager.get().stopPreview();

        if (isqrcode) {
            CameraManager.get().setContinuousFocusParams(false);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
            }
        } else {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            CameraManager.get().startPreview();
            CameraManager.get().setContinuousFocusParams(true);
        }
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    //	private static final long VIBRATE_DURATION = 200L;

    //	private void playBeepSoundAndVibrate() {
    //		if (playBeep && mediaPlayer != null) {
    //			mediaPlayer.start();
    //		}
    //		if (vibrate) {
    //			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    //			vibrator.vibrate(VIBRATE_DURATION);
    //		}
    //	}

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    Uri uri = data.getData();
                    if (!TextUtils.isEmpty(uri.getAuthority())) {
                        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null,
                                null);
                        if (null == cursor) {
                            return;
                        }
                        cursor.moveToFirst();
                        photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        if (photo_path.endsWith("jpg") || photo_path.endsWith(".JPG") || photo_path.endsWith("png")) {

                            if (isQrcode) {
                                materialDialogsUtil = new MaterialDialogsUtil(this);
                                materialDialogsUtil.showIndeterminateProgressDialog(R.string.scaning).cancelable(false).build();
                                materialDialogsUtil.show();

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Result result = scanningImage(photo_path);
                                        if (result != null) {
                                            Message m = mHandler.obtainMessage();
                                            m.what = PARSE_BARCODE_SUC;
                                            m.obj = result.getText();
                                            mHandler.sendMessage(m);
                                        } else {
                                            Message m = mHandler.obtainMessage();
                                            m.what = PARSE_BARCODE_FAIL;
                                            m.obj = "不是有效的二维码照片!";
                                            mHandler.sendMessage(m);
                                        }
                                    }
                                }).start();
                            }
                        } else {
                            ToastUtil.showToast(ActivityScan.this, "你选择的不是有效的图片");
                        }
                    }
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_qrcode:
                MobclickAgent.onEvent(ActivityScan.this, "scan_qrcode");
                if (!isQrcode) {
                    inactivityTimer.onActivity();
                    btnChange(0);
                    changeCameraState(true);
                }
                break;
            case R.id.tv_vcard:
                MobclickAgent.onEvent(ActivityScan.this, "scan_card");
                if (isQrcode) {
                    inactivityTimer.onActivity();
                    btnChange(1);
                    changeCameraState(false);
                }
                break;
            case R.id.btn_takepicture:
                btn_takepicture.setEnabled(false);
                break;
            default:
                break;
        }
    }

    /**
     * 改变布局状态
     */
    private void btnChange(int state) {
//		isQrcode = state == 0;
        isQrcode = true;
        setTextValue(1, isQrcode ? "扫二维码" : "拍名片");
        tv_left.setVisibility(!isQrcode ? View.VISIBLE : View.GONE);
        tv_right.setVisibility(!isQrcode ? View.GONE : View.VISIBLE);
        btn_takepicture.setEnabled(!isQrcode);
        viewfinderView.setFlag(!isQrcode);
        if (!isQrcode)
            hintScanVCard();
    }

    /**
     * 读取相册图片转成byte数组
     */
    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[2048];
        int len = 0;
        while ((len = is.read(b, 0, 2048)) != -1) {
            baos.write(b, 0, len);
        }
        baos.flush();
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    private boolean savePhoto(byte[] data, String path) throws IOException {
        // 将二进制数据转换为文件的函数
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            out.write(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null)
                out.close();
        }
    }

    private boolean savePhoto(byte[] data, Camera camera, File file) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
            FileOutputStream filecon = new FileOutputStream(file);
            //按4:3来缩放图片
            int width = image.getWidth();
            int height = image.getHeight();
            int iWidth;
            int iHeigth;
            iHeigth = (width * 3) / 4;
            if (iHeigth >= height) {
                iHeigth = height;
                iWidth = (height * 4) / 3;
            } else {
                iWidth = width;
            }
            image.compressToJpeg(new Rect(0, 0, iWidth, iHeigth), 100, filecon);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void hintScanVCard() {
        SharedPreferences prefs = getSharedPreferences("first_guide_setting_info", 0);
        if (!prefs.getBoolean("scanvcard_hint", true))
            return;
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder(R.string.material_dialog_title, R.string.scan_vcard_hint, R.string.hint_iknow).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            }
        }).cancelable(false);
        materialDialogsUtil.show();
        prefs.edit().putBoolean("scanvcard_hint", false).commit();
    }

    private static String myQrcodeurl;

    //网络方式获取我的二维码（可以考虑使用本地zxing生成）
    private static void getMyQrcode(Context context) {
        new GetSelfTwoDimenCodeTask(context) {
            public void doPre() {
            }

            ;

            public void doPost(SelfTwoDimenCodeMessageBoardOperation result) {
                if (null != result && result.getState() == 1) {
                    myQrcodeurl = result.getQrcode();
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.loadImage(result.getQrcode(), CacheManager.animateFirstDisplayListener);
                }
            }

            ;
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), RenheApplication.getInstance().getUserInfo().getSid());
    }

    //显示我的二维码
    public static void showMyQrcode(Context context) {
        getMyQrcode(context);
        String name = RenheApplication.getInstance().getUserInfo().getName();
        String userFaceUrl = RenheApplication.getInstance().getUserInfo().getUserface();
        String job = RenheApplication.getInstance().getUserInfo().getTitle();
        String company = RenheApplication.getInstance().getUserInfo().getCompany();
        String addressCity = RenheApplication.getInstance().getUserInfo().getLocation();

        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.custom_two_dimencode_dialog, null);
        Builder mDialog = new AlertDialog.Builder(context);
        // mDialog.setView(view,0,0,0,0);
        AlertDialog mAlertDialog = mDialog.create();
        mAlertDialog.setView(view, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(true);

        ImageView dialogAvatarIv = (ImageView) view.findViewById(R.id.avatar_img);
        ImageView dialogTwoCodeIv = (ImageView) view.findViewById(R.id.showImg);
        TextView dialogNameTv = (TextView) view.findViewById(R.id.nickname_txt);
        TextView dialogJobTv = (TextView) view.findViewById(R.id.company_txt);
        TextView dialogAddressTv = (TextView) view.findViewById(R.id.job_txt);

        dialogNameTv.setText(name);

        dialogJobTv.setVisibility(View.VISIBLE);
        dialogAddressTv.setVisibility(View.VISIBLE);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(userFaceUrl, dialogAvatarIv, CacheManager.options, CacheManager.animateFirstDisplayListener);
        imageLoader.displayImage(myQrcodeurl, dialogTwoCodeIv, CacheManager.imageOptions,
                CacheManager.imageAnimateFirstDisplayListener);

        if (!TextUtils.isEmpty(job)) {
            dialogJobTv.setText(job);
        }
        if (company != null && company != "") {
            dialogJobTv.setText(dialogJobTv.getText().toString().trim() + " / " + company);
        }
        if (TextUtils.isEmpty(dialogJobTv.getText().toString().trim())) {
            dialogJobTv.setVisibility(View.GONE);
        } else {
            dialogJobTv.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(addressCity)) {
            dialogAddressTv.setText(addressCity);
        } else {
            dialogAddressTv.setVisibility(View.GONE);
        }
        mAlertDialog.show();
    }

    private void cameraInitFail() {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.getBuilder(R.string.material_dialog_title, R.string.scan_camerafail_hint, R.string.hint_close).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
            }
        }).cancelable(false);
        materialDialogsUtil.show();
    }
}

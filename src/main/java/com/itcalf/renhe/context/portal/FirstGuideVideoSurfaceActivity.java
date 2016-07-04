package com.itcalf.renhe.context.portal;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.register.RegisterAuthActivity;
import com.orhanobut.logger.Logger;

import java.io.File;

/**
 * description :初次进入应用，引导视频
 * Created by Chans Renhenet
 * 2015/12/11
 */
public class FirstGuideVideoSurfaceActivity extends Activity implements SurfaceHolder.Callback {
    /**
     * Called when the activity is first created.
     */
    MediaPlayer player;
    SurfaceView surface;
    SurfaceHolder surfaceHolder;
    private File file;
    private Button registerBtn, loginBtn;
    private String sid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        setContentView(R.layout.activity_guide_surface_video);
        findView();
        initData();
        initListener();
    }

    protected void findView() {
        surface = (SurfaceView) findViewById(R.id.surfaceView);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        loginBtn = (Button) findViewById(R.id.loginBtn);
    }

    protected void initData() {
        Intent intent = getIntent();
        if (null != intent) {
            sid = intent.getStringExtra("sid");
        }
        surfaceHolder = surface.getHolder();//SurfaceHolder是SurfaceView的控制接口
    }

    protected void initListener() {
        registerBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                isDisplay = true;
                startActivity(new Intent(FirstGuideVideoSurfaceActivity.this, RegisterAuthActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                isDisplay = true;
                Intent intent = new Intent();
                if (null != getIntent()) {
                    intent = getIntent();
                }
                intent.setClass(FirstGuideVideoSurfaceActivity.this, LoginActivity.class);
                intent.putExtra(Constants.DATA_LOGOUT, true);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != surfaceHolder && surfaceHolder.isCreating())
            surfaceHolder.removeCallback(this);
        surfaceHolder.addCallback(this); //因为这个类实现了SurfaceHolder.Callback接口，所以回调参数直接this
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        //必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
        player = MediaPlayer.create(this, R.raw.hl_guide_video);
        if (null == player)
            return;
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(surfaceHolder);
//        Uri uri = Uri.parse("android.resource://com.itcalf.renhe/raw/hl_guide_video.mp4");
//        if (null == file)
//            file = new File(uri.getPath());
//        if (!file.exists()) {
//            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_SHORT).show();
//            return;
//        }
        //设置显示视频显示在SurfaceView上
        try {
//            player.setDataSource(this,uri);
//            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.seekTo(0);
                player.start();
            }
        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logger.e("播放视频error");
                Intent intent = new Intent();
                intent.putExtra("sid", sid);
                intent.setClass(FirstGuideVideoSurfaceActivity.this, FirstGuideActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();
                return true;
            }
        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (null == player)
            return;
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

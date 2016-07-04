package com.itcalf.renhe.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 监听程序是前后台运行Service
 */
public class RenheService extends Service {
    private static final String TAG = "RenheService";
    private ActivityManager activityManager;
    private String packageName;
    private boolean isRunning = true;//判断服务是否在运行
    private boolean flag = true;//程序第一次或再次前台运行，更新地址
    private LocationClient mLocationClient;
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;//设置定位模式
    private String tempcoor = "gcj02";//返回的定位结果是百度经纬度，默认值gcj02
    public MyLocationListener mMyLocationListener;
    public static String longitude, latitude, cityName;//经纬度

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        packageName = this.getPackageName();
        new Thread() {
            public void run() {
                try {
                    while (isRunning) {
                        Thread.sleep(5000);
                        if (isAppOnForeground()) {
                            if (flag) {
                                Log.v(TAG, "开始前台运行");
                                handler.sendEmptyMessage(1);
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
//        return START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    mLocationClient = new LocationClient(RenheApplication.getInstance());
                    mMyLocationListener = new MyLocationListener();
                    mLocationClient.registerLocationListener(mMyLocationListener);
                    initLocation();
                    mLocationClient.start();

                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);// 设置定位模式
        option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType() == 61 || location.getLocType() == 161) {//定位成功
                longitude = location.getLongitude() + "";
                latitude = location.getLatitude() + "";
                String city = location.getCity();
                if (city != null && !TextUtils.isEmpty(city))
                    cityName = city.substring(0, city.length() - 1);
                mLocationClient.stop();
                new UpdateLocationTask().executeOnExecutor(Executors.newCachedThreadPool(),
                        ((RenheApplication) RenheService.this.getApplicationContext()).getUserInfo().getSid(),
                        ((RenheApplication) RenheService.this.getApplicationContext()).getUserInfo().getAdSId(), latitude,
                        longitude);
            } else {
                mLocationClient.stop();
            }
        }
    }

    private class UpdateLocationTask extends AsyncTask<String, Void, MessageBoardOperation> {

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            reqParams.put("lat", params[2]);
            reqParams.put("lng", params[3]);
            try {
                MessageBoardOperation mbo = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.UPDATE_LOCATION,
                        reqParams, MessageBoardOperation.class, RenheService.this);
                return mbo;
            } catch (Exception e) {
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(MessageBoardOperation result) {
            super.onPostExecute(result);
            if (null != result && result.getState() == 1) {
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}

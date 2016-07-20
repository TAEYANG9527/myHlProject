package com.itcalf.renhe.context.register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.archives.edit.SelectCityActivity;
import com.itcalf.renhe.context.relationship.AdvanceSearchSelectCityMainActivity;
import com.itcalf.renhe.context.relationship.AdvanceSearchUtil;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.SearchCity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ManifestUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.Button;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * description :注册，完善资料 step2
 * Created by Chans Renhenet
 * 2015/8/21
 */
public class RegisterPerfectInfoActivity extends BaseActivity {

    private EditText nameEdt;
    private TextView locationTv;
    private RelativeLayout locationRl;
    private TextView locationTip;
    private TextView industryTv;
    private EditText companyTv;
    private EditText positionTv;
    private LinearLayout cpLl, ilLl;

    private Button nextBtn;
    private RelativeLayout rootLl;
    private RequestDialog requestDialog;

    private int cityCode = -1, locationCode = -1;
    private int industryCode = -1;
    private String industry;
    private String mobile;
    private boolean isSimplify = false;//是否走简化流程（默认不是）

    private final static int REGISTER_SELECT_LOCATION_CALLBACK = 10;
    private final static int REGISTER_SELECT_INDUSTRY_CALLBACK = 11;

    //定位
    private LocationClient mLocationClient;
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private String tempcoor = "gcj02";
    public GeofenceClient mGeofenceClient;
    public MyLocationListener mMyLocationListener;
    private double longitude = 0.0;
    private double latitude = 0.0;
    private String locatedCity = "";
    private SQLiteDatabase db;
    private Handler handler;
    private SearchCity currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.register_perfect_info);
    }

    @Override
    protected void findView() {
        super.findView();
        nameEdt = (EditText) findViewById(R.id.name_edt);
        companyTv = (EditText) findViewById(R.id.company_edt);
        positionTv = (EditText) findViewById(R.id.position_edt);
        locationRl = (RelativeLayout) findViewById(R.id.location_Rl);
        locationTip = (TextView) findViewById(R.id.location_tip_tv);
        locationTv = (TextView) findViewById(R.id.location_tv);
        industryTv = (TextView) findViewById(R.id.industry_tv);
        nextBtn = (Button) findViewById(R.id.register_next_btn);
        rootLl = (RelativeLayout) findViewById(R.id.rootLl);
        cpLl = (LinearLayout) findViewById(R.id.cpLl);
        ilLl = (LinearLayout) findViewById(R.id.ilLl);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "完善资料");

        mobile = getIntent().getStringExtra("mobile");
        isSimplify = getIntent().getBooleanExtra("isSimplify", false);
        requestDialog = new RequestDialog(this, "正在注册...");
        //走简化流程
        if (isSimplify) {
            ilLl.setVisibility(View.GONE);
        } else {
            ilLl.setVisibility(View.VISIBLE);
            //注册定位
            mLocationClient = new LocationClient(this.getApplicationContext());
            mGeofenceClient = new GeofenceClient(getApplicationContext());
            mMyLocationListener = new MyLocationListener();
            mLocationClient.registerLocationListener(mMyLocationListener);
            InitLocation();
            mLocationClient.start();//开始定位
        }
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                switch (arg0.what) {
                    case 2:// 查询当前城市回调
                        if (currentCity != null) {
                            locationTv.setText(currentCity.getName().toString().trim());
                            locationTv.setHint("");
                            locationTip.setVisibility(View.VISIBLE);
                            locationCode = currentCity.getId();
                            cityCode = locationCode;
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    @Override
    protected void initListener() {
        super.initListener();

        industryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterPerfectInfoActivity.this, SelectIndustryExpandableListActivity.class);
                intent.putExtra("isFromArcheveEdit", false);
                intent.putExtra("selectedId", industryCode);
                intent.putExtra("selectedIndustry", industry);
                startActivityForResult(intent, REGISTER_SELECT_INDUSTRY_CALLBACK);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        locationRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(RegisterPerfectInfoActivity.this, SelectCityWithChinaAndForeignActivity.class);
                Intent intent = new Intent(RegisterPerfectInfoActivity.this, SelectCityActivity.class);
                startActivityForResult(intent, REGISTER_SELECT_LOCATION_CALLBACK);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceUitl.hideSoftInput(nextBtn);
                //验证姓名
                String nameStr = nameEdt.getText().toString().trim();
                if (nameStr.equals("")) {
                    ToastUtil.showToast(RegisterPerfectInfoActivity.this, getResources().getString(R.string.namenotnull));
                    nameEdt.requestFocus();
                    return;
                }

                if ("".equals(companyTv.getText().toString())) {
                    ToastUtil.showToast(RegisterPerfectInfoActivity.this, "请输入公司");
                    return;
                }
                if ("".equals(positionTv.getText().toString())) {
                    ToastUtil.showToast(RegisterPerfectInfoActivity.this, "请输入职位");
                    return;
                }
                //走完整流程
                if (!isSimplify) {
                    if ("".equals(industryTv.getText().toString())) {
                        ToastUtil.showToast(RegisterPerfectInfoActivity.this, "请选择行业");
                        return;
                    }
                    if ("".equals(locationTv.getText().toString())) {
                        ToastUtil.showToast(RegisterPerfectInfoActivity.this, "请选择地区");
                        return;
                    }
                }
                if (-1 != NetworkUtil.hasNetworkConnection(RegisterPerfectInfoActivity.this)) {
                    new RegisterTask().executeOnExecutor(Executors.newCachedThreadPool(), mobile, nameStr,
                            positionTv.getText().toString(), companyTv.getText().toString());
                } else {
                    ToastUtil.showNetworkError(RegisterPerfectInfoActivity.this);
                }
            }
        });
    }

    /**
     * @description 新注册接口
     */
    class RegisterTask extends AsyncTask<String, Void, UserInfo> {

        @Override
        protected UserInfo doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("mobile", params[0]);
            reqParams.put("name", params[1]);
            reqParams.put("title", params[2]);
            reqParams.put("company", params[3]);
            if (!isSimplify) {
                reqParams.put("industry", industryCode);
                reqParams.put("city", cityCode);
                reqParams.put("lat", latitude);
                reqParams.put("lng", longitude);
            }
            reqParams.put("token", Constants.PUSH_DFVICE_TOKEN);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
            reqParams.put("channelCode", ManifestUtil.getUmengChannel(RegisterPerfectInfoActivity.this));
            reqParams.put("bundle", Constants.JPUSH_APP_BUNDLE);// 说明：设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
            reqParams.put("version", DeviceUitl.getAppVersion());// 说明：设备版本号：例如3.0.1
            try {
                if (isSimplify) {
                    return (UserInfo) HttpUtil.doHttpRequest(Constants.Http.REGISTER_MOBILE_V7, reqParams, UserInfo.class,
                            RegisterPerfectInfoActivity.this);
                } else {
                    return (UserInfo) HttpUtil.doHttpRequest(Constants.Http.REGISTER_MOBILE_COMPLETE_V7, reqParams,
                            UserInfo.class, RegisterPerfectInfoActivity.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            requestDialog.addFade(rootLl);
        }

        @Override
        protected void onPostExecute(UserInfo result) {
            super.onPostExecute(result);
            requestDialog.removeFade(rootLl);

            if (null != result) {
                // 1. state int 说明：1 注册成功；-1 Email地址已被注册；-2 Email地址格式错误；-3
                // 名字不规范；-4 密码长度不正确；-5 两次密码不相同；
                if (1 == result.getState()) {
                    RenheApplication.getInstance().setFromLoginIn(true);
                    //设置信鸽推送
                    PushUtil.deletePush();//为了防止用户卸载和聊后（信鸽是不知道用户已卸载，所以没有对该账户和设备接触绑定）再次安装，并且使用另外一个账户登录，导致推送消息错乱
                    PushUtil.registerPush(result);
                    if (!TextUtils.isEmpty(result.getMobile())) {
                        result.setLoginAccountType(result.getMobile());
                    } else {
                        result.setLoginAccountType(mobile);
                    }
                    //是否是初次进入应用
                    SharedPreferences msp = getSharedPreferences("first_guide_setting_info", 0);
                    SharedPreferences.Editor editor1 = msp.edit();
                    editor1.putBoolean("ifFirst", false);
                    editor1.commit();

                    //刚注册用户导航图
                    SharedPreferences msp2 = getSharedPreferences("regiser_guide_setting_info", 0);
                    SharedPreferences.Editor editor2 = msp2.edit();
                    editor2.putBoolean("regiser_messageboard" + result.getSid(), true);
                    editor2.putBoolean("regiser_conversation" + result.getSid(), true);
                    editor2.putBoolean("regiser_contacts" + result.getSid(), true);
                    editor2.putBoolean("regiser_search" + result.getSid(), true);
                    editor2.putBoolean("regiser_messageboard_search" + result.getSid(), true);
                    editor2.commit();

                    // 注册成功，记录是登录状态
                    SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("islogined", true);
                    editor.commit();

                    result.setRemember(true);
                    result.setLogintime(DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString());
                    getRenheApplication().getUserCommand().insertOrUpdate(result);
                    getRenheApplication().setUserInfo(result);
                    getRenheApplication().setLogin(1);

                    startActivity(ImportMoblieMailActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

                    //阿里云统计：在用户注册成功之后，可使用userRegister 完成用户注册埋点。
                    MANService manService = MANServiceProvider.getService();
                    if (null != manService && null != manService.getMANAnalytics())
                        // 注册用户埋点
                        manService.getMANAnalytics().userRegister(result.getSid());
                    //Growing IO 设置自定义维度
                    StatisticsUtil.setGrowingIOCS();
                } else if (2 == result.getState()) {
                    //资料完善成功，并且是需要导入通讯录功能的渠道号；
                } else {
                    ToastUtil.showToast(RegisterPerfectInfoActivity.this, result.getErrorInfo());
                }
            } else {
                ToastUtil.showNetworkError(RegisterPerfectInfoActivity.this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 结果码不等于取消时候
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //地区返回结果
                case REGISTER_SELECT_LOCATION_CALLBACK:
                    String yourCity = data.getStringExtra("yourcity");
                    String yourCityCodetemp = data.getStringExtra("yourcitycode");
                    if (yourCity != null && yourCityCodetemp != null) {
                        locationTv.setText(yourCity);
                        int codeId = Integer.parseInt(yourCityCodetemp);
                        if (codeId != -1 && locationCode == codeId) {
                            locationTv.setHint("");
                            locationTip.setVisibility(View.VISIBLE);
                        } else {
                            locationTip.setVisibility(View.GONE);
                        }
                        cityCode = codeId;
                    }
                    break;
                //行业返回结果
                case REGISTER_SELECT_INDUSTRY_CALLBACK:
                    String yourIndustry = data.getStringExtra("yourindustry");
                    String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
                    if (yourIndustry != null && yourIndustryCodetemp != null) {
                        industryTv.setText(yourIndustry);
                        industryCode = Integer.parseInt(yourIndustryCodetemp);
                        industry = yourIndustry;
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* 初始化定位参数 */
    private void InitLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);// 设置定位模式
        option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
        int span = 1000;
        option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    /**
     * 实现实位回调监听
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location) {
                //Receive Location
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    //GPS定位
                    locatedCity = location.getCity();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    //网络定位
                    locatedCity = location.getCity();
                }
                if (null != locatedCity && !TextUtils.isEmpty(locatedCity)) {
                    String string = String.valueOf(locatedCity.charAt(locatedCity.length() - 1));
                    if (string.equals("市")) {
                        locatedCity = locatedCity.substring(0, locatedCity.length() - 1);
                    }
                }
                if (null != locatedCity && !TextUtils.isEmpty(locatedCity)) {
                    mLocationClient.stop();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AdvanceSearchUtil.copyDB(RegisterPerfectInfoActivity.this,
                                        AdvanceSearchSelectCityMainActivity.DBNAME);
                                if (db == null) {
                                    db = SQLiteDatabase.openOrCreateDatabase(
                                            AdvanceSearchSelectCityMainActivity.path + AdvanceSearchSelectCityMainActivity.DBNAME,
                                            null);
                                }
                                currentCity = AdvanceSearchUtil.getCurrentCity(db, AdvanceSearchSelectCityMainActivity.TABLE_NAME,
                                        locatedCity);
                                handler.sendEmptyMessage(2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } else {
                mLocationClient.start();
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.stop();
        }
    }
}

package com.itcalf.renhe.context.relationship;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.more.AccountLimitUpgradeActivity;
import com.itcalf.renhe.context.relationship.NearbyTask.IDataBack;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.NearbyPeople;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class NearbyActivity extends BaseActivity {
    private ListView mNearbyList;
    private TextView noDataTv;
    private List<Map<String, Object>> mData;//用于接收返回数据
    private List<Map<String, Object>> showData;//用于显示数据
    private NearbyAdapter mAdapter;
    private int pageNo = 1;
    private int pageSize = 200;//一次性请求200条数据
    private View mFooterView;
    private FadeUitl fadeUitl;
    private RelativeLayout rootRl;
    private LinearLayout noLocation;
    private int index = -1;//记录行业所选的position
    private int industryId = -1;//行业id,默认为全部行业
    private LocationClient mLocationClient;
    private LocationMode tempMode = LocationMode.Hight_Accuracy;//设置定位模式
    private String tempcoor = "gcj02";//返回的定位结果是百度经纬度，默认值gcj02
    public MyLocationListener mMyLocationListener;
    private String longitude, latitude, cityName;//经纬度
    /**
     * 判断数据是否加载完整，加载完成则右上角menu按钮可点*
     */
    private boolean isclick = false;
    private int filterPrivilege = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.nearby_people);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("附近的人脉"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("附近的人脉"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        noLocation = (LinearLayout) findViewById(R.id.no_location);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        mNearbyList = (ListView) findViewById(R.id.nearby_list);
        mFooterView = LayoutInflater.from(this).inflate(R.layout.room_footerview, null);
        mFooterView.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "附近的人脉");
        mData = new ArrayList<Map<String, Object>>();
        showData = new ArrayList<Map<String, Object>>();
        mAdapter = new NearbyAdapter();
        mNearbyList.addFooterView(mFooterView, null, false);
        mNearbyList.setAdapter(mAdapter);
        if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                .getBoolean("fastscroll", false)) {
            mNearbyList.setFastScrollEnabled(true);
        }
        if (isOpen(this)) {
            fadeUitl = new FadeUitl(this, getResources().getString(R.string.loading));
            fadeUitl.addFade(rootRl);
            mLocationClient = new LocationClient(this);
            mMyLocationListener = new MyLocationListener();
            mLocationClient.registerLocationListener(mMyLocationListener);
            initLocation();
            mLocationClient.start();
        } else {
            noLocation.setVisibility(View.VISIBLE);
            mNearbyList.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initListener() {
        super.initListener();
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo++;
                toggleFooterView(true);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (showData != null) {
                            showData.clear();
                            if (20 * pageNo < mData.size()) {
                                showData.addAll(mData.subList(0, 20 * pageNo));
                                toggleFooterView(false);
                            } else {
                                showData.addAll(mData);
                                mFooterView.setVisibility(View.GONE);
                            }
                        }
                    }
                }, 500);
            }
        });
        mNearbyList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mData.size() && null != mData.get(position)) {
                    Intent intent = new Intent(NearbyActivity.this, MyHomeArchivesActivity.class);
                    intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, (String) mData.get(position).get("sid"));
                    intent.putExtra("from", Constants.ADDFRIENDTYPE[5]);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    //添加日志统计
                    String content = "5.3|1|" + System.currentTimeMillis() + "|"
                            + RenheApplication.getInstance().getUserInfo().getSid() + "|" + cityName + "|" + industryId;
                    LoggerFileUtil.writeFile(content, true);
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isOpen(this)) {
            MenuItem moreItem = menu.findItem(R.id.menu_more);
            moreItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_more:
                if (isclick)
                    showDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearch(int industryId, String lat, String lng, String cityName, final boolean hideFooter) {
        new NearbyTask(this, new IDataBack() {
            @Override
            public void onPre() {

                if (hideFooter) {
                    mFooterView.setVisibility(View.GONE);
                } else {
                    toggleFooterView(true);
                }
            }

            @Override
            public void onPost(NearbyPeople result) {
                if (hideFooter) {
                    if (fadeUitl != null) {
                        fadeUitl.removeFade(rootRl);
                        fadeUitl = null;
                    }
                } else {
                    toggleFooterView(false);
                }
                if (null != result) {
                    isclick = true;
                    filterPrivilege = result.getFilterPrivilege();

                    if (1 == result.getState()) {
                        noDataTv.setVisibility(View.GONE);
                        mNearbyList.setVisibility(View.VISIBLE);
                        if (mData != null && pageNo == 1) {
                            mData.clear();
                        }
                        if (null != result.getMemberList() && result.getMemberList().length > 0) {
                            List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
                            for (int i = 0; i < result.getMemberList().length; i++) {
                                final Map<String, Object> map = new LinkedHashMap<String, Object>();
                                map.put("avatar_path", result.getMemberList()[i].getAvatar());
                                map.put("memberId", result.getMemberList()[i].getMemberId());
                                map.put("sid", result.getMemberList()[i].getSid());
                                map.put("name", result.getMemberList()[i].getName());
                                map.put("title", result.getMemberList()[i].getCurTitle());
                                map.put("company", result.getMemberList()[i].getCurCompany());
                                map.put("industry", result.getMemberList()[i].getIndustry());
                                map.put("distance", result.getMemberList()[i].getDistance());
                                rsList.add(map);
                            }
                            mData.addAll(rsList);
                            if (mData.size() > 20) {
                                mFooterView.setVisibility(View.VISIBLE);
                                showData.clear();
                                showData.addAll(mData.subList(0, 20));
                            } else {
                                mFooterView.setVisibility(View.GONE);
                                showData.clear();
                                showData.addAll(mData.subList(0, mData.size()));
                            }
                            mAdapter.notifyDataSetChanged();
                            mNearbyList.setSelection(0);//列表返回顶部
                        }
                    } else if (-7 == result.getState()) {
                        isclick = false;
                        ToastUtil.showToast(NearbyActivity.this, "没有按照行业过滤的权限");
                    }
                } else {

                    isclick = false;

                    noDataTv.setVisibility(View.VISIBLE);
                    mNearbyList.setVisibility(View.GONE);
                }
            }

        }).executeOnExecutor(Executors.newCachedThreadPool(), industryId, lat, lng, cityName, pageNo, pageSize);
    }

    private void toggleFooterView(boolean isShow) {
        if (isShow) {
            ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("加载中...");
            mFooterView.findViewById(R.id.waitPb).setVisibility(View.VISIBLE);
        } else {
            ((TextView) mFooterView.findViewById(R.id.titleTv)).setText("查看更多");
            mFooterView.findViewById(R.id.waitPb).setVisibility(View.GONE);
        }
    }

    class NearbyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return showData.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(NearbyActivity.this).inflate(R.layout.nearby_list_item, null);
                holder.headImage = (ImageView) convertView.findViewById(R.id.headImage);
                holder.nameTv = (TextView) convertView.findViewById(R.id.nameTv);
                holder.distanceTv = (TextView) convertView.findViewById(R.id.distanceTv);
                holder.titleTv = (TextView) convertView.findViewById(R.id.titleTv);
                holder.separatorTv = (TextView) convertView.findViewById(R.id.separatorTv);
                holder.infoTv = (TextView) convertView.findViewById(R.id.infoTv);
                holder.industryTv = (TextView) convertView.findViewById(R.id.industryTv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String picPath = (String) showData.get(position).get("avatar_path");
            if (null != picPath && !"".equals(picPath)) {
                if (null != holder.headImage) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    try {
                        imageLoader.displayImage(picPath, holder.headImage, CacheManager.options,
                                CacheManager.animateFirstDisplayListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                holder.headImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar));
            }

            holder.nameTv.setText((String) showData.get(position).get("name"));
            holder.distanceTv.setText((String) showData.get(position).get("distance"));
            String title = (String) showData.get(position).get("title");
            String company = (String) showData.get(position).get("company");
            String industry = (String) showData.get(position).get("industry");

            holder.titleTv.setVisibility(View.VISIBLE);
            holder.separatorTv.setVisibility(View.VISIBLE);
            holder.infoTv.setVisibility(View.VISIBLE);
            holder.industryTv.setVisibility(View.VISIBLE);

            if (TextUtils.isEmpty(title)) {
                holder.titleTv.setVisibility(View.GONE);
                holder.separatorTv.setVisibility(View.GONE);
            } else {
                holder.titleTv.setText(title);
            }
            if (TextUtils.isEmpty(company)) {
                holder.infoTv.setVisibility(View.GONE);
            } else {
                holder.infoTv.setText(company);
            }
            if (TextUtils.isEmpty(industry)) {
                holder.industryTv.setVisibility(View.GONE);
            } else {
                holder.industryTv.setText(industry);
            }

            return convertView;
        }

    }

    class ViewHolder {
        ImageView headImage;
        TextView nameTv, distanceTv, titleTv, separatorTv, infoTv, industryTv;
    }

    private void showDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null);
        final Dialog dialog = new Dialog(this, R.style.TranslucentUnfullwidthWinStyle);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = (int) (dm.widthPixels * 0.8);
        dialogWindow.setAttributes(lp);
        dialog.show();

        TextView allTv = (TextView) view.findViewById(R.id.all_tv);
        TextView industryTv = (TextView) view.findViewById(R.id.industry_tv);
        TextView clearTv = (TextView) view.findViewById(R.id.clear_tv);
        TextView cancelTv = (TextView) view.findViewById(R.id.cancel_tv);
        allTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NearbyActivity.this, "附近的人脉——查看全部");
                industryId = -1;
                pageNo = 1;
                fadeUitl = new FadeUitl(NearbyActivity.this, getResources().getString(R.string.loading));
                fadeUitl.addFade(rootRl);
                mNearbyList.setVisibility(View.GONE);
                noDataTv.setVisibility(View.GONE);
                initSearch(industryId, latitude, longitude, cityName, true);
                dialog.dismiss();
            }

        });
        industryTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(NearbyActivity.this, "附近的人脉——按行业筛选");
                if (filterPrivilege == -1) {
                    //没有权限，弹出dialog
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(NearbyActivity.this);
                    materialDialogsUtil.getNotitleBuilder(R.string.nearby_filter_auth_error, R.string.open_now, R.string.hint_iknow).callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            Intent i = new Intent(NearbyActivity.this, AccountLimitUpgradeActivity.class);
                            i.putExtra("update", Constants.ACCOUNTLIMITUPGRADE[4]);
                            startActivity(i);
                            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                            //和聊统计
                            String content = "5.158.1" + LoggerFileUtil.getConstantInfo(NearbyActivity.this) + "|5";
                            LoggerFileUtil.writeFile(content, true);
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                        }
                    });
                    materialDialogsUtil.show();
                    //和聊统计
                    String content = "5.158" + LoggerFileUtil.getConstantInfo(NearbyActivity.this) + "|5";
                    LoggerFileUtil.writeFile(content, true);
                } else {
                    Intent intent = new Intent(NearbyActivity.this, SelectIndustryListActivity.class);
                    intent.putExtra("selected_position", index);
                    startActivityForResult(intent, 1);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
                dialog.dismiss();
            }
        });
        clearTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //				Toast.makeText(NearbyActivity.this, "清除位置信息并退出", Toast.LENGTH_SHORT).show();
                MobclickAgent.onEvent(NearbyActivity.this, "附近的人脉——清除位置信息并退出");
                new CleanLocationTask().executeOnExecutor(Executors.newCachedThreadPool(),
                        ((RenheApplication) NearbyActivity.this.getApplicationContext()).getUserInfo().getSid(),
                        ((RenheApplication) NearbyActivity.this.getApplicationContext()).getUserInfo().getAdSId());
                dialog.dismiss();
                finish();
            }

        });
        cancelTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                industryId = Integer.parseInt(data.getStringExtra("industry_code"));
                index = data.getIntExtra("selected_position", -1);
                if (industryId >= -1) {
                    pageNo = 1;
                    fadeUitl = new FadeUitl(NearbyActivity.this, getResources().getString(R.string.loading));
                    fadeUitl.addFade(rootRl);
                    mNearbyList.setVisibility(View.GONE);
                    noDataTv.setVisibility(View.GONE);
                    initSearch(industryId, latitude, longitude, cityName, true);
                }
                //和聊统计
                String content = "5.143" + LoggerFileUtil.getConstantInfo(NearbyActivity.this) + "|" + industryId;
                LoggerFileUtil.writeFile(content, true);
            }
        }
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
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
                if (!TextUtils.isEmpty(city))
                    cityName = city.substring(0, city.length() - 1);
                if (TextUtils.isEmpty(cityName)) {
                    if (!TextUtils.isEmpty(RenheApplication.getInstance().getUserInfo().getLocation())) {
                        cityName = RenheApplication.getInstance().getUserInfo().getLocation();
                    }
                }
                //				System.out.println("城市:" + cityName);
                mLocationClient.stop();
                initSearch(industryId, latitude, longitude, cityName, true);
            } else {
                mLocationClient.stop();
            }
        }
    }

    private class CleanLocationTask extends AsyncTask<String, Void, MessageBoardOperation> {

        @Override
        protected MessageBoardOperation doInBackground(String... params) {
            Map<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("sid", params[0]);
            reqParams.put("adSId", params[1]);
            try {
                MessageBoardOperation mbo = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.CLEAN_LOCATION,
                        reqParams, MessageBoardOperation.class, NearbyActivity.this);
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
}

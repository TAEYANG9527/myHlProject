package com.itcalf.renhe.context.archives;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.Constants.BroadCastAction;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.cache.ExternalStorageUtil;
import com.itcalf.renhe.context.archives.edit.EditEdus;
import com.itcalf.renhe.context.archives.edit.EditProvideGetInfo;
import com.itcalf.renhe.context.archives.edit.EditSelfInfoEditName;
import com.itcalf.renhe.context.archives.edit.EditSpecialties;
import com.itcalf.renhe.context.archives.edit.EditSummaryInfo;
import com.itcalf.renhe.context.archives.edit.EditWorks;
import com.itcalf.renhe.context.archives.edit.EditWorksItem;
import com.itcalf.renhe.context.archives.edit.SelectCityActivity;
import com.itcalf.renhe.context.archives.edit.task.EditSelfInfoTask;
import com.itcalf.renhe.context.cropImage.CropImage;
import com.itcalf.renhe.context.imageselector.ImageSelector;
import com.itcalf.renhe.context.imageselector.ImageSelectorActivity;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.AimTagInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.EduExperienceInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.PreferredTagInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.SpecialtiesInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.SummaryInfo;
import com.itcalf.renhe.dto.Profile.UserInfo.WorkExperienceInfo;
import com.itcalf.renhe.dto.UploadAvatar;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.imageUtil.ImageSelectorUtil;
import com.itcalf.renhe.utils.FadeUitl;
import com.itcalf.renhe.utils.FileUtil;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.UserInfoUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import butterknife.BindView;
import cn.renhe.heliao.idl.member.MyModuleNotice;

import static android.view.View.VISIBLE;
import static android.view.View.inflate;

/**
 * 编辑个人档案
 */
public class EditMyHomeArchivesActivity extends BaseActivity {

    public static String FLAG_INTENT_DATA = "profileSid";
    public static String FLAG_INTENT_USEINFO = "useinfo";
    @BindView(R.id.sex_empty_iv)
    ImageView sexEmptyIv;
    @BindView(R.id.provide_empty_iv)
    ImageView provideEmptyIv;
    @BindView(R.id.want_empty_iv)
    ImageView wantEmptyIv;
    @BindView(R.id.introduction_empty_iv)
    ImageView introductionEmptyIv;
    @BindView(R.id.work_empty_iv)
    ImageView workEmptyIv;
    @BindView(R.id.edu_empty_iv)
    ImageView eduEmptyIv;
    @BindView(R.id.specialties_empty_iv)
    ImageView specialtiesEmptyIv;

    private ImageView mAvatarImgV;
    private TextView mNameTv;// 姓名
    private TextView mSexTv;// 性别
    private TextView mAreaTv;// 公司地址
    private TextView mIndustryTv;// 公司地址
    private TextView mCompanyTv; //当前公司
    private TextView mTitleTv; //当前职位
    private TextView mProvideTv;// 能提供
    private TextView mWantTv;// 想得到
    private TextView mSelfIntroductionTv;// 个人简介
    private TextView mWorkTv;// 工作经历
    private TextView mEduTv;// 教育经历
    private TextView mSpecialtiesTv;// 技能专长
    private LinearLayout avartarLl;
    private LinearLayout nameLl;
    private LinearLayout sexLl;
    private LinearLayout areaLl;
    private LinearLayout companyLl;
    private LinearLayout titleLl;
    private LinearLayout industryll;
    private LinearLayout provideLl;
    private LinearLayout wantLl;
    private LinearLayout selfIntroductionLl;
    private LinearLayout workLl;
    private LinearLayout eduLl;
    private LinearLayout specialtiesLl;
    private ImageView nameArrow;
    private ImageView sexArrow;

    private ScrollView mScrollView;
    private String mOtherSid;
    private Profile mProfile;
    private String userFaceUrl;
    private String userName;
    private String userCompany;
    private String userIndustry;
    private String userDesp;
    private int accountType;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealName;//是否是实名认证的会员

    private TextView bindPhoneTv;
    private Button newEditButton;
    private RelativeLayout rootRl;

    private LinearLayout selfInfoLl;

    private static final int PROFESSIONAL_REQUEST_CODE = 10;
    private static final int SPECIALTIES_REQUEST_CODE = 11;
    private static final int PROVIDE_REQUEST_CODE = 12;
    private static final int GET_REQUEST_CODE = 13;
    private static final int ADDWORK_REQUEST_CODE = 14;
    private static final int EDITWORK_REQUEST_CODE = 15;
    private static final int ADDEDU_REQUEST_CODE = 16;
    private static final int EDITEDU_REQUEST_CODE = 17;
    private static final int EDITSELF_REQUEST_CODE = 18;
    private static final int EDITORGANSITION_REQUEST_CODE = 19;
    private static final int EDITINTEREST_REQUEST_CODE = 20;
    private static final int EDITAWARD_REQUEST_CODE = 21;
    private static final int EDITCONTACT_REQUEST_CODE = 22;
    private static final int EDITWEBSITE_REQUEST_CODE = 23;
    private static final int EDITALLOTHERINFO_REQUEST_CODE = 24;

    // 选择本地图片回调标识
    public static final int REQUEST_CODE_CHOOSE_PICTURE = 1001;//选本地
    public static final int REQUEST_CODE_CAPTURE_CUT = 1003;
    public static final int REQUEST_CODE_CHOOSE_CAPTURE = 1004;//照相机
    private String mFilePath = "";
    public static final String IMAGE_FILE_NAME = "faceImage.jpg";
    public static final int REQUEST_CODE_CHOOSE_PICTURE_KITKAT = 2002;//选本地kitkat

    private final static int AREA_REQUEST_CODE = 2010;
    private final static int INDUSTRY_REQUEST_CODE = 2011;
    private final static int NAME_REQUEST_CODE = 2012;
    private String industry;
    private int industryId;
    private String address;
    private int addressId;
    private String name;
    private int gender;//0代表女；1代表男
    private String tempIndustry;
    private int tempIndustryId;
    private String tempAddress;
    private int tempAddressId;
    private String tempName;
    private int tempGender;//0代表女；1代表男

    private Bitmap bitmap1 = null;
    private Bitmap bitmap = null;
    private static final int AVATAR_POPUP_TAG = 10001;
    private static final int SEX_POPUP_TAG = 10002;
    private RefreshArchieveReceiver reFreshArchieveReceiver;
    public static final String REFRESH_ARCHIEVE_RECEIVER_ACTION = "com.renhe.refresh_editmyhome";
    private FadeUitl fadeUitl;
    private LinearLayout lv_match;
    private LinearLayout noNetorkLl;
    private int ID_TASK_COMPLETE_ARCHIVE_PROMPT_DATA = TaskManager.getTaskId();//获取完善资料页面哪些item需要显示小红点
    private MyModuleNotice.PromptDataResponse promptDataResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenheApplication.getInstance().addActivity(this);
        new ActivityTemplate().doInActivity(this, R.layout.archives_edit_new);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("编辑档案"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("编辑档案"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        mAvatarImgV = (ImageView) findViewById(R.id.avatar_image);
        mAvatarImgV.setEnabled(false);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mSexTv = (TextView) findViewById(R.id.sex_tv);
        mAreaTv = (TextView) findViewById(R.id.area_tv);
        mIndustryTv = (TextView) findViewById(R.id.industry_tv);
        mCompanyTv = (TextView) findViewById(R.id.company_tv);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mProvideTv = (TextView) findViewById(R.id.provide_tv);
        mWantTv = (TextView) findViewById(R.id.want_tv);
        mSelfIntroductionTv = (TextView) findViewById(R.id.self_introduction_tv);
        mWorkTv = (TextView) findViewById(R.id.work_tv);
        mEduTv = (TextView) findViewById(R.id.edu_tv);
        mSpecialtiesTv = (TextView) findViewById(R.id.specialties_tv);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        rootRl = (RelativeLayout) findViewById(R.id.rootRl);
        selfInfoLl = (LinearLayout) findViewById(R.id.layout01);

        avartarLl = (LinearLayout) findViewById(R.id.avartar_ll);
        nameLl = (LinearLayout) findViewById(R.id.name_ll);
        sexLl = (LinearLayout) findViewById(R.id.sex_ll);
        areaLl = (LinearLayout) findViewById(R.id.area_ll);
        industryll = (LinearLayout) findViewById(R.id.industry_ll);
        companyLl = (LinearLayout) findViewById(R.id.company_ll);
        titleLl = (LinearLayout) findViewById(R.id.title_ll);
        provideLl = (LinearLayout) findViewById(R.id.provide_ll);
        wantLl = (LinearLayout) findViewById(R.id.want_ll);
        selfIntroductionLl = (LinearLayout) findViewById(R.id.self_introduction_ll);
        workLl = (LinearLayout) findViewById(R.id.work_ll);
        eduLl = (LinearLayout) findViewById(R.id.edu_ll);
        specialtiesLl = (LinearLayout) findViewById(R.id.specialties_ll);
        nameArrow = (ImageView) findViewById(R.id.name_arrow);
        sexArrow = (ImageView) findViewById(R.id.sex_arrow);
        lv_match = (LinearLayout) findViewById(R.id.lv_match);
        noNetorkLl = (LinearLayout) findViewById(R.id.no_network_ll);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "更新档案");
        reFreshArchieveReceiver = new RefreshArchieveReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(REFRESH_ARCHIEVE_RECEIVER_ACTION);
        registerReceiver(reFreshArchieveReceiver, intentFilter);
        //判断是否从我的页面中 点击完善资料选项
        String useInfo = getIntent().getStringExtra(FLAG_INTENT_USEINFO);
        if (!TextUtils.isEmpty(useInfo) && useInfo.equals(FLAG_INTENT_USEINFO)) {
            lv_match.setVisibility(View.VISIBLE);
            fadeUitl = new FadeUitl(this, "加载中...");
            mOtherSid = getIntent().getStringExtra(FLAG_INTENT_DATA);
            fadeUitl.addFade(rootRl);
            new EditProfileTask(true, false).execute(mOtherSid,
                    RenheApplication.getInstance().getUserInfo().getSid(),
                    RenheApplication.getInstance().getUserInfo().getAdSId());
        } else {
            mOtherSid = getIntent().getStringExtra(FLAG_INTENT_DATA);
            mOtherSid = getRenheApplication().getUserInfo().getSid();
            mProfile = (Profile) getIntent().getSerializableExtra("Profile");
            if (null != mProfile) {
                populateData(mProfile);
            }
        }
        completeArchivePromptData();
    }

    private void populateData(Profile result) {
        name = result.getUserInfo().getName();
        gender = result.getUserInfo().getGender();
        address = result.getUserInfo().getLocation();
        addressId = result.getUserInfo().getAddressId();
        industry = result.getUserInfo().getIndustry();
        industryId = result.getUserInfo().getIndustryId();
        tempAddressId = addressId;
        tempAddress = address;
        tempIndustryId = industryId;
        tempIndustry = industry;
        tempName = name;
        tempGender = gender;
        this.userFaceUrl = result.getUserInfo().getUserface();
        this.userName = result.getUserInfo().getName().trim();
        if (null != result.getUserInfo().getTitle()) {
            this.userCompany = result.getUserInfo().getTitle().trim() + " ";
        }
        if (null != result.getUserInfo().getLocation()) {
            this.userIndustry = result.getUserInfo().getLocation().trim() + " ";
        }
        if (null != result.getUserInfo().getIndustry()) {
            this.userIndustry = mIndustryTv.getText().toString() + result.getUserInfo().getIndustry().trim();
        }
        this.userDesp = this.userCompany + this.userIndustry;
        this.accountType = result.getUserInfo().getAccountType();
        this.isRealName = result.getUserInfo().isRealName();
        mAvatarImgV.setEnabled(true);
        ImageLoader imageLoader = ImageLoader.getInstance();
        try {
            imageLoader.displayImage(result.getUserInfo().getUserface(), mAvatarImgV, CacheManager.options,
                    CacheManager.animateFirstDisplayListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != result.getUserInfo().getName()) {
            mNameTv.setText(result.getUserInfo().getName().trim());
        }
        if (isRealName) {
            nameLl.setEnabled(false);
            nameArrow.setVisibility(View.INVISIBLE);
            sexLl.setEnabled(false);
            sexArrow.setVisibility(View.INVISIBLE);
        }
        if (null != result.getUserInfo().getLocation()) {
            mAreaTv.setText(result.getUserInfo().getLocation().trim() + " ");
        }
        if (null != result.getUserInfo().getIndustry()) {
            mIndustryTv.setText(result.getUserInfo().getIndustry().trim());
        }
        if (gender == 0) {
            mSexTv.setText("女");
        } else if (gender == 1) {
            mSexTv.setText("男");
        } else {
            mSexTv.setText("");
        }

        if (result != null && result.getUserInfo() != null) {
            UserInfo userInfo = result.getUserInfo();
            String currentCompany = userInfo.getCompany();
            if (!TextUtils.isEmpty(currentCompany)) {
                mCompanyTv.setText(currentCompany.trim());
            } else {
                mCompanyTv.setText("");
            }
            String currentTitle = userInfo.getTitle();
            if (!TextUtils.isEmpty(currentTitle)) {
                mTitleTv.setText(currentTitle.trim());
            } else {
                mTitleTv.setText("");
            }
            PreferredTagInfo[] preferredTagInfo = userInfo.getPreferredTagInfo();
            if (preferredTagInfo != null) {
                int preferredTagNum = preferredTagInfo.length;
                String preferredTag = "";
                for (int i = 0; i < preferredTagNum; i++) {
                    preferredTag = preferredTagInfo[i].getTitle();
                    if (!TextUtils.isEmpty(preferredTag)) {
                        break;
                    }
                }
                if (preferredTagNum > 1 && !TextUtils.isEmpty(preferredTag)) {
                    mProvideTv.setText(preferredTag + "等" + preferredTagNum + "个标签");
                } else if (!TextUtils.isEmpty(preferredTag)) {
                    mProvideTv.setText(preferredTag + " " + preferredTagNum + "个标签");
                } else {
                    mProvideTv.setText("");
                }
            } else {
                mProvideTv.setText("");
            }
            AimTagInfo[] aimTagInfos = userInfo.getAimTagInfo();
            if (aimTagInfos != null) {
                int aimTagNum = aimTagInfos.length;
                String aimTag = "";
                for (int i = 0; i < aimTagNum; i++) {
                    aimTag = aimTagInfos[i].getTitle();
                    if (!TextUtils.isEmpty(aimTag)) {
                        break;
                    }
                }
                if (aimTagNum > 1 && !TextUtils.isEmpty(aimTag)) {
                    mWantTv.setText(aimTag + "等" + aimTagNum + "个标签");
                } else if (!TextUtils.isEmpty(aimTag)) {
                    mWantTv.setText(aimTag + " " + aimTagNum + "个标签");
                } else {
                    mWantTv.setText("");
                }
            } else {
                mWantTv.setText("");
            }
            SummaryInfo sInfo = userInfo.getSummaryInfo();
            if (sInfo != null) {
                String professional = sInfo.getProfessional();
                if (!TextUtils.isEmpty(professional)) {
                    mSelfIntroductionTv.setText(professional);
                } else {
                    mSelfIntroductionTv.setText("");
                }
            } else {
                mSelfIntroductionTv.setText("");
            }
            WorkExperienceInfo workExperienceInfo[] = userInfo.getWorkExperienceInfo();
            if (workExperienceInfo != null) {
                int workNum = workExperienceInfo.length;
                String company = "";
                for (int i = 0; i < workNum; i++) {
                    company = workExperienceInfo[i].getCompany();
                    if (!TextUtils.isEmpty(company)) {
                        break;
                    }
                }
                if (workNum > 1 && !TextUtils.isEmpty(company)) {
                    mWorkTv.setText(company + "等" + workNum + "个工作");
                } else if (!TextUtils.isEmpty(company)) {
                    mWorkTv.setText(company + " " + workNum + "个工作");
                } else {
                    mWorkTv.setText("");
                }
            } else {
                mWorkTv.setText("");
            }
            EduExperienceInfo[] eduExperienceInfo = userInfo.getEduExperienceInfo();
            if (eduExperienceInfo != null) {
                int eduNum = eduExperienceInfo.length;
                String school = "";
                for (int i = 0; i < eduNum; i++) {
                    school = eduExperienceInfo[i].getSchoolName();
                    if (!TextUtils.isEmpty(school)) {
                        break;
                    }
                }
                if (eduNum > 1 && !TextUtils.isEmpty(school)) {
                    mEduTv.setText(school + "等" + eduNum + "个学校");
                } else if (!TextUtils.isEmpty(school)) {
                    mEduTv.setText(school + " " + eduNum + "个学校");
                } else {
                    mEduTv.setText("");
                }
            } else {
                mEduTv.setText("");
            }
            SpecialtiesInfo[] specialtiesInfos = userInfo.getSpecialtiesInfo();
            if (specialtiesInfos != null) {
                int specialtiesNum = specialtiesInfos.length;
                String title = "";
                for (int i = 0; i < specialtiesNum; i++) {
                    title = specialtiesInfos[i].getTitle();
                    if (!TextUtils.isEmpty(title)) {
                        break;
                    }
                }
                if (specialtiesNum > 1 && !TextUtils.isEmpty(title)) {
                    mSpecialtiesTv.setText(title + "等" + specialtiesNum + "个标签");
                } else if (!TextUtils.isEmpty(title)) {
                    mSpecialtiesTv.setText(title + " " + specialtiesNum + "个标签");
                } else {
                    mSpecialtiesTv.setText("");
                }
            } else {
                mSpecialtiesTv.setText("");
            }

        }
        result = null;
    }

    @Override
    protected void initListener() {
        super.initListener();

        avartarLl.setClickable(true);
        avartarLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mProfile.isSelf()) {
//                    new PopupWindows(EditMyHomeArchivesActivity.this, avartarLl, AVATAR_POPUP_TAG);
                    ImageSelectorUtil.initUpdateAvatarActivityImageSelector(EditMyHomeArchivesActivity.this);
                }
            }
        });
        areaLl.setClickable(true);
        areaLl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
//                Intent intent = new Intent(EditMyHomeArchivesActivity.this, SelectCityWithChinaAndForeignActivity.class);
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, SelectCityActivity.class);
                startActivityForResult(intent, AREA_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        industryll.setClickable(true);
        industryll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, SelectIndustryExpandableListActivity.class);
                intent.putExtra("isFromArcheveEdit", true);
                intent.putExtra("selectedId", industryId);
                intent.putExtra("selectedIndustry", industry);
                startActivityForResult(intent, INDUSTRY_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        nameLl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String school = mNameTv.getText().toString().trim();
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditSelfInfoEditName.class);
                intent.putExtra("name", school);
                startActivityForResult(intent, NAME_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        sexLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new PopupWindows(EditMyHomeArchivesActivity.this, sexLl, SEX_POPUP_TAG);
            }
        });
        companyLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditWorksItem.class);
                intent.putExtra("company", mCompanyTv.getText().toString());
                intent.putExtra("title", mTitleTv.getText().toString());
                intent.putExtra("simple", true);//只编辑公司和职位
                intent.putExtra("isAdd", false);
                startActivityForResult(intent, EDITWORK_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        titleLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditWorksItem.class);
                intent.putExtra("company", mCompanyTv.getText().toString());
                intent.putExtra("title", mTitleTv.getText().toString());
                intent.putExtra("simple", true);//只编辑公司和职位
                intent.putExtra("isAdd", false);
                startActivityForResult(intent, EDITWORK_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        provideLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditProvideGetInfo.class);
                intent.putExtra("toProvide", true);
                intent.putExtra("Profile", mProfile);
                startActivityForResult(intent, PROVIDE_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        wantLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditProvideGetInfo.class);
                intent.putExtra("toGet", true);
                intent.putExtra("Profile", mProfile);
                startActivityForResult(intent, GET_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        selfIntroductionLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditSummaryInfo.class);
                intent.putExtra("toSpecialties", true);
                SummaryInfo sInfo = mProfile.getUserInfo().getSummaryInfo();
                if (null != sInfo && sInfo.getProfessional() != null && !TextUtils.isEmpty(sInfo.getProfessional().trim())) {
                    intent.putExtra("professionals", sInfo.getProfessional().trim());
                }
                intent.putExtra("Profile", mProfile);
                startActivityForResult(intent, PROFESSIONAL_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        workLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditWorks.class);
                intent.putExtra("Profile", mProfile);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        eduLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditEdus.class);
                intent.putExtra("Profile", mProfile);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        specialtiesLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditMyHomeArchivesActivity.this, EditSpecialties.class);
                intent.putExtra("Profile", mProfile);
                startActivityForResult(intent, SPECIALTIES_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        mSexTv.addTextChangedListener(new CustomTextChangeListener(mNameTv.getId()));
        mProvideTv.addTextChangedListener(new CustomTextChangeListener(mProvideTv.getId()));
        mWantTv.addTextChangedListener(new CustomTextChangeListener(mWantTv.getId()));
        mSelfIntroductionTv.addTextChangedListener(new CustomTextChangeListener(mSelfIntroductionTv.getId()));
        mWorkTv.addTextChangedListener(new CustomTextChangeListener(mWorkTv.getId()));
        mEduTv.addTextChangedListener(new CustomTextChangeListener(mEduTv.getId()));
        mSpecialtiesTv.addTextChangedListener(new CustomTextChangeListener(mSpecialtiesTv.getId()));
    }

    private boolean checkPreSave() {
        if (TextUtils.isEmpty(tempName)) {
            ToastUtil.showToast(EditMyHomeArchivesActivity.this, "姓名不能为空");
            return false;
        }
        //		if (TextUtils.isEmpty(tempAddress)) {
        //			ToastUtil.showToast(EditMyHomeArchivesActivity.this, "所在地不能为空");
        //			return false;
        //		}
        //		if (TextUtils.isEmpty(tempIndustry)) {
        //			ToastUtil.showToast(EditMyHomeArchivesActivity.this, "从事行业不能为空");
        //			return false;
        //		}
        return true;
    }

    /**
     * 修改姓名，性别，地区，行业
     *
     * @param which 姓名 1，性别2，地区3，行业4
     */
    public void goSave(final int which) {
        if (!checkPreSave()) {
            return;
        }
        new EditSelfInfoTask(EditMyHomeArchivesActivity.this) {
            public void doPre() {
                showDialog(2);
            }

            public void doPost(MessageBoardOperation result) {

                super.doPost(result);

                if (result == null) {
                    removeDialog(2);
                    ToastUtil.showToast(EditMyHomeArchivesActivity.this, "网络异常，请重试");
                } else if (result.getState() == 1) {
                    removeDialog(2);
                    UserInfo tempUserInfo = mProfile.getUserInfo();
                    switch (which) {
                        case 1:
                            name = tempName;
                            mNameTv.setText(name);
                            tempUserInfo.setName(name);
                            UserInfoUtil.chengeUserInfo(UserInfoUtil.NAME, name);
                            break;
                        case 2:
                            gender = tempGender;
                            if (gender == 0) {
                                mSexTv.setText("女");
                            } else {
                                mSexTv.setText("男");
                            }
                            tempUserInfo.setGender(gender);
                            break;
                        case 3:
                            address = tempAddress;
                            addressId = tempAddressId;
                            mAreaTv.setText(address);
                            tempUserInfo.setAddressId(addressId);
                            tempUserInfo.setLocation(address);
                            UserInfoUtil.chengeUserInfo(UserInfoUtil.LOCATION, address);
                            break;
                        case 4:
                            industry = tempIndustry;
                            industryId = tempIndustryId;
                            mIndustryTv.setText(industry);
                            tempUserInfo.setIndustry(industry);
                            tempUserInfo.setIndustryId(industryId);
                            break;
                        default:
                            break;
                    }
                    mProfile.setUserInfo(tempUserInfo);
                    Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    brocastIntent.putExtra("Profile", mProfile);
                    sendBroadcast(brocastIntent);
                } else if (result.getState() == -3) {
                    removeDialog(2);
                    ToastUtil.showToast(EditMyHomeArchivesActivity.this, "姓名不能为空");
                } else if (result.getState() == -4) {
                    removeDialog(2);
                    ToastUtil.showToast(EditMyHomeArchivesActivity.this, "性别不能为空");
                } else if (result.getState() == -5) {
                    removeDialog(2);
                    ToastUtil.showToast(EditMyHomeArchivesActivity.this, "从事行业不能为空");
                } else {
                    removeDialog(2);
                    ToastUtil.showToast(EditMyHomeArchivesActivity.this, "所在地不能为空");
                }

            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                getRenheApplication().getUserInfo().getAdSId(), tempName, tempGender + "", tempIndustryId + "",
                tempAddressId + "");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            return true;
        }
        return super.onKeyDown(keyCode, event); // 最后，一定要做完以后返回
        // true，或者在弹出菜单后返回true，其他键返回super，让其他键默认
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(), mOtherSid,
                            getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId());
                    Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    sendBroadcast(brocastIntent);
                }
                break;
            case PROFESSIONAL_REQUEST_CODE:
            case SPECIALTIES_REQUEST_CODE:
            case PROVIDE_REQUEST_CODE:
            case GET_REQUEST_CODE:
            case EDITWORK_REQUEST_CODE:
            case ADDWORK_REQUEST_CODE:
            case ADDEDU_REQUEST_CODE:
            case EDITSELF_REQUEST_CODE:
            case EDITORGANSITION_REQUEST_CODE:
            case EDITINTEREST_REQUEST_CODE:
            case EDITAWARD_REQUEST_CODE:
            case EDITCONTACT_REQUEST_CODE:
            case EDITWEBSITE_REQUEST_CODE:
            case EDITALLOTHERINFO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data.getSerializableExtra("Profile") != null) {
                        mProfile = (Profile) data.getSerializableExtra("Profile");
                        populateData(mProfile);
                        //保存数据
                        com.itcalf.renhe.dto.UserInfo userInfo2 = RenheApplication.getInstance().getUserInfo();
                        userInfo2.setName(mProfile.getUserInfo().getName().trim());
                        userInfo2.setCompany(mProfile.getUserInfo().getCompany().trim());
                        userInfo2.setTitle(mProfile.getUserInfo().getTitle().trim());
                        //					userInfo2.setLocation(pf.getUserInfo().getLocation().trim());
                        RenheApplication.getInstance().setUserInfo(userInfo2);//更新sharePerfrance
                        RenheApplication.getInstance().getUserCommand().insertOrUpdate(userInfo2);//更新本地数据表
                        Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                        brocastIntent.putExtra("Profile", mProfile);
                        sendBroadcast(brocastIntent);
                    } else {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(), mOtherSid,
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId());
                    }
                }
                break;
            // 回调照片本地浏览
            case REQUEST_CODE_CHOOSE_PICTURE:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        //					Bitmap bm = null;
                        ContentResolver resolver = getContentResolver();
                        Uri originalUri = data.getData(); //获得图片的uri
                        //						bm = MediaStore.Images.Media.getBitmap(resolver, originalUri); //显得到bitmap图片
                        // 这里开始的第二部分，获取图片的路径：
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                        //按我个人理解 这个是获得用户选择的图片的索引值
                        if (cursor != null) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            //最后根据索引值获取图片路径
                            String path = cursor.getString(column_index);
                            startCustomPhotoZoom(path);
                        }
                    }
                }
                break;
            case REQUEST_CODE_CHOOSE_PICTURE_KITKAT:
                if (resultCode == RESULT_OK) {
                    startCustomPhotoZoom(getPath(this, data.getData()));
                }
                break;
            // 回调照片剪切
            case REQUEST_CODE_CAPTURE_CUT:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        final String path = data.getStringExtra("cropImagePath");
                        if (!TextUtils.isEmpty(path)) {
                            final File file = new File(path);
                            bitmap = BitmapFactory.decodeFile(path);
                            if (null != file && file.exists()) {
                                final String url = Constants.Http.MEMBER_UPLOADIMG + "?sid="
                                        + RenheApplication.getInstance().getUserInfo().getSid() + "&adSId="
                                        + RenheApplication.getInstance().getUserInfo().getAdSId();
                                new AsyncTask<Object, Void, UploadAvatar>() {

                                    @Override
                                    protected UploadAvatar doInBackground(Object... params) {
                                        try {
                                            return (UploadAvatar) HttpUtil.uploadFile(url, file, "image", UploadAvatar.class);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        //									getActivity().showDialog(1);
                                        showDialog(1);
                                    }

                                    @Override
                                    protected void onPostExecute(UploadAvatar result) {
                                        super.onPostExecute(result);
                                        removeDialog(1);
                                        if (null != result && (1 == result.getState())) {
                                            /*************头像上传成功，保存，发广播通知上一级页面更新*****************/
                                            // 头像另存为
                                            Intent intent0 = new Intent(BroadCastAction.UPDATE_AVATAR_ACTION);
                                            intent0.putExtra("userface", result.getUserface());
                                            sendBroadcast(intent0);
                                            /*********add by chan 2014.12.5*************/

                                            Intent intent2 = new Intent(BroadCastAction.UPLOAD_AVARTAR_ARCHIEVE_ACTION);
                                            intent2.putExtra("userface", result.getUserface());
                                            sendBroadcast(intent2);

                                            ExternalStorageUtil.delCacheAvatarPath(EditMyHomeArchivesActivity.this,
                                                    RenheApplication.getInstance().getUserInfo().getEmail());

                                            ToastUtil.showToast(EditMyHomeArchivesActivity.this, R.string.update_avatar_success);
                                            UserInfoUtil.chengeUserInfo(UserInfoUtil.USERFACE, result.getUserface());

                                            boolean flag = false;

                                            if (!TextUtils.isEmpty(path)) {
                                                File file = new File(path);
                                                if (null != file && file.exists()) {
                                                    flag = true;
                                                    Uri uri = Uri.fromFile(file);
//                                                    mAvatarImgV.setImageURI(uri);
                                                    Bitmap imageBmp = BitmapFactory.decodeFile(path);
                                                    if (null != imageBmp)
                                                        mAvatarImgV.setImageBitmap(imageBmp);
                                                    //删除本地的裁剪缓存图片crop.jpg
                                                    FileUtil.deleteFile(path);
                                                }
                                            }
                                            if (!flag) {
                                                String userFace = result.getUserface();
                                                if (!TextUtils.isEmpty(userFace)) {
                                                    ImageLoader imageLoader = ImageLoader.getInstance();
                                                    try {
                                                        imageLoader.displayImage(userFace, mAvatarImgV);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        } else {
                                            ToastUtil.showToast(EditMyHomeArchivesActivity.this, R.string.update_avatar_fail);
                                        }
                                    }

                                }.executeOnExecutor(Executors.newCachedThreadPool());
                            } else {
                                ToastUtil.showToast(EditMyHomeArchivesActivity.this, R.string.update_avatar_fail);
                            }
                        }
                    }
                }
                break;
            // 回调照相机
            case REQUEST_CODE_CHOOSE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    File tempFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
                    startCustomPhotoZoom(tempFile.getPath());
                }
                break;
            case AREA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String yourCity = data.getStringExtra("yourcity");
                    String yourCityCodetemp = data.getStringExtra("yourcitycode");
                    if (yourCity != null && yourCityCodetemp != null) {
                        tempAddressId = Integer.parseInt(yourCityCodetemp);
                        tempAddress = yourCity;
                    }
                    goSave(3);
                }
                break;
            case INDUSTRY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String yourIndustry = data.getStringExtra("yourindustry");
                    String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
                    if (yourIndustry != null && yourIndustryCodetemp != null) {
                        tempIndustryId = Integer.parseInt(yourIndustryCodetemp);
                        tempIndustry = yourIndustry;
                    }
                    goSave(4);
                }
                break;
            case NAME_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String yourName = data.getStringExtra("name");
                    if (!TextUtils.isEmpty(yourName)) {
                        tempName = yourName;
                    }
                    goSave(1);

                }
                break;
            case ImageSelector.IMAGE_REQUEST_CODE://用imageselector选择好的图片回调
                if (null != data) {
                    List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
//                    for (String path : pathList) {
//                        Logger.d("ImagePathList-->" + path);
//                    }
                    if (null != pathList && !pathList.isEmpty()) {
                        Logger.d("ImagePathList-->" + pathList.get(0));
                        startCustomPhotoZoom(pathList.get(0));
                    }
                }
                break;
            default:
                break;
        }
    }

    private void completeArchivePromptData() {
        if (checkGrpcBeforeInvoke(ID_TASK_COMPLETE_ARCHIVE_PROMPT_DATA))
            grpcController.completeArchivePromptData(ID_TASK_COMPLETE_ARCHIVE_PROMPT_DATA);
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (null != result) {
            promptDataResponse = (MyModuleNotice.PromptDataResponse) result;
            initArchivePromptView();
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
    }

    private void initArchivePromptView() {
        if (mScrollView.getVisibility() == VISIBLE && null != promptDataResponse) {
            Map<String, Boolean> map = promptDataResponse.getPromptMap();
            if (null != map && !map.isEmpty()) {
                if (map.get("preferredTag")) {
                    provideEmptyIv.setVisibility(VISIBLE);
                } else {
                    provideEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("aimTag")) {
                    wantEmptyIv.setVisibility(VISIBLE);
                } else {
                    wantEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("experience")) {
                    workEmptyIv.setVisibility(VISIBLE);
                } else {
                    workEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("gender")) {
                    sexEmptyIv.setVisibility(VISIBLE);
                } else {
                    sexEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("professional")) {
                    introductionEmptyIv.setVisibility(VISIBLE);
                } else {
                    introductionEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("specialties")) {
                    specialtiesEmptyIv.setVisibility(VISIBLE);
                } else {
                    specialtiesEmptyIv.setVisibility(View.GONE);
                }
                if (map.get("education")) {
                    eduEmptyIv.setVisibility(VISIBLE);
                } else {
                    eduEmptyIv.setVisibility(View.GONE);
                }
            }
        }
    }

    class CustomTextChangeListener implements TextWatcher {

        int viewId;

        public CustomTextChangeListener(int viewId) {
            this.viewId = viewId;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())) {
                switch (viewId) {
                    case R.id.sex_tv:
                        sexEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.provide_tv:
                        provideEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.want_tv:
                        wantEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.self_introduction_tv:
                        introductionEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.work_tv:
                        workEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.edu_tv:
                        eduEmptyIv.setVisibility(View.GONE);
                        break;
                    case R.id.specialties_tv:
                        specialtiesEmptyIv.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }

    class ProfileTask extends AsyncTask<String, Void, Profile> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Profile doInBackground(String... params) {
            try {
                return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
                        EditMyHomeArchivesActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);

            try {
                dismissDialog(2);
                removeDialog(2);
            } catch (Exception e) {
            }
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    mProfile = result;
                    populateData(mProfile);
                    Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    brocastIntent.putExtra("Profile", mProfile);
                    sendBroadcast(brocastIntent);
                }
                mScrollView.setVisibility(VISIBLE);
            } else {
                ToastUtil.showNetworkError(EditMyHomeArchivesActivity.this);
            }
        }
    }

    public class PopupWindows extends PopupWindow {

        @SuppressWarnings("deprecation")
        public PopupWindows(Context mContext, View parent, int tag) {
            final View view;
            LinearLayout ll_popup;
            if (tag == AVATAR_POPUP_TAG) {
                view = inflate(mContext, R.layout.item_popupwindows, null);
                ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            } else {
                view = inflate(mContext, R.layout.sex_select_popup, null);
                ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            }

            view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

            setWidth(LayoutParams.FILL_PARENT);
            setHeight(LayoutParams.WRAP_CONTENT);
            ColorDrawable cd = new ColorDrawable(0000);
            setBackgroundDrawable(cd);
            //			setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();
            view.setOnTouchListener(new OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = view.findViewById(R.id.ll_popup).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
            if (tag == AVATAR_POPUP_TAG) {
                Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_camera);
                Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_Photo);
                Button bt3 = (Button) view.findViewById(R.id.item_popupwindows_cancel);
                bt1.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        MobclickAgent.onEvent(EditMyHomeArchivesActivity.this, "refresh_avartar_camera");
                        //					photo();
                        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // 判断存储卡是否可以用，可用进行存储
                        //                            if (hasSdcard()) {
                        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
                        //                            }
                        startActivityForResult(intentFromCapture, REQUEST_CODE_CHOOSE_CAPTURE);
                        dismiss();
                    }
                });
                bt2.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        MobclickAgent.onEvent(EditMyHomeArchivesActivity.this, "refresh_avartar_pic");
                        //					startActivity(TestPicActivity.class);
                        if (Build.VERSION.SDK_INT < 19) {
                            Intent intentFromGallery = new Intent();
                            intentFromGallery.setType("image/*"); // 设置文件类型
                            intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICTURE);
                        } else {
                            Intent intentFromGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
                            intentFromGallery.setType("image/*");
                            startActivityForResult(intentFromGallery, REQUEST_CODE_CHOOSE_PICTURE_KITKAT);
                        }
                        dismiss();
                    }
                });
                bt3.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            } else {
                Button bt1 = (Button) view.findViewById(R.id.item_popupwindows_female);
                Button bt2 = (Button) view.findViewById(R.id.item_popupwindows_male);
                bt1.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        tempGender = 0;
                        goSave(2);
                        dismiss();
                    }
                });
                bt2.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        tempGender = 1;
                        goSave(2);
                        dismiss();
                    }
                });
            }

        }
    }

    /**
     * 调用自定义裁剪工具裁剪图片方法实现
     *
     * @param path
     */
    public void startCustomPhotoZoom(String path) {
        Intent intent = new Intent(EditMyHomeArchivesActivity.this, CropImage.class);
        // 设置裁剪
        intent.putExtra("path", path);
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_CUT);
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.avater_uploading).cancelable(false)
                        .build();
            case 2:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        if (bitmap1 != null && !bitmap1.isRecycled()) {
            bitmap1.recycle();
            bitmap1 = null;
            mAvatarImgV.setImageBitmap(bitmap1);
        }
        unregisterReceiver(reFreshArchieveReceiver);
    }

    class RefreshArchieveReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            mProfile = (Profile) arg1.getSerializableExtra("Profile");
            if (mProfile != null) {
                populateData(mProfile);
            } else {
                new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(), mOtherSid,
                        RenheApplication.getInstance().getUserInfo().getSid(),
                        RenheApplication.getInstance().getUserInfo().getAdSId());
            }

        }
    }

    class EditProfileTask extends AsyncTask<String, Void, Profile> {
        private boolean isRefresh = false;
        private boolean isByOpenId = false;

        public EditProfileTask(boolean isRefresh, boolean isByOpenId) {
            this.isRefresh = isRefresh;
            this.isByOpenId = isByOpenId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isRefresh) {
                mScrollView.setVisibility(View.GONE);
                newEditButton.setVisibility(View.GONE);
            }
        }

        @Override
        protected Profile doInBackground(String... params) {
            try {
                if (!isByOpenId) {
                    return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
                            EditMyHomeArchivesActivity.this);
                } else {
                    return getRenheApplication().getProfileCommand().showIMNoFriendsProfile(params[0], params[1], params[2],
                            EditMyHomeArchivesActivity.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            try {
                fadeUitl.removeFade(rootRl);
                lv_match.setVisibility(View.GONE);
            } catch (Exception e) {
            }
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    mOtherSid = result.getUserInfo().getSid();
                    mProfile = result;
                    if (null != mProfile) {
                        populateData(mProfile);
                        noNetorkLl.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                        initArchivePromptView();
                    }
                }
            } else {
                mScrollView.setVisibility(View.GONE);
                noNetorkLl.setVisibility(View.VISIBLE);
            }
        }
    }
}

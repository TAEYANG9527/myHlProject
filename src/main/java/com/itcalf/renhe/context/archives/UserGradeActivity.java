package com.itcalf.renhe.context.archives;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.view.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.renhe.heliao.idl.member.MemberGrade;

/**
 * 给好友打分的界面
 * Created by wangning on 2016/2/29.
 */
public class UserGradeActivity extends BaseActivity implements RatingBar.OnRatingBarChangeListener {
    @BindView(R.id.behaviour_ratingBar)
    RatingBar behaviourRatingBar;
    @BindView(R.id.resource_ratingBar)
    RatingBar resourceRatingBar;
    @BindView(R.id.power_ratingBar)
    RatingBar powerRatingBar;
    @BindView(R.id.contentEdt)
    EditText contentEdt;

    private Profile mProfile;
    private MemberGrade.MemberGradeInfo memberGradeInfo;//获取到的打分、评价
    private int ID_TASK_GRADE_BEHAVIOUR = TaskManager.getTaskId();//为人打分
    private int ID_TASK_GRADE_RESOURCE = TaskManager.getTaskId();//资源打分
    private int ID_TASK_GRADE_POWER = TaskManager.getTaskId();//能力打分
    private int ID_TASK_COMMENT = TaskManager.getTaskId();//评价
    private int ID_TASK_GET_GRADE_AND_COMMENT = TaskManager.getTaskId();//获取打分、评价

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.user_grade_activity_layout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void findView() {
        super.findView();
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        super.initData();
        if (null != getIntent().getSerializableExtra("profile")) {
            mProfile = (Profile) getIntent().getSerializableExtra("profile");
            setTextValue("为" + mProfile.getUserInfo().getName() + "打分");
        } else {
            setTextValue("为好友打分");
        }
        if (null != getIntent().getSerializableExtra("memberGradeInfo")) {
            memberGradeInfo = (MemberGrade.MemberGradeInfo) getIntent().getSerializableExtra("memberGradeInfo");
            initViewByInfo(memberGradeInfo);
        }
        if (null != mProfile)
            getGradeAndComment(mProfile.getUserInfo().getId());
    }

    @Override
    protected void initListener() {
        super.initListener();
        behaviourRatingBar.setOnRatingBarChangeListener(this);
        resourceRatingBar.setOnRatingBarChangeListener(this);
        powerRatingBar.setOnRatingBarChangeListener(this);
    }

    @Override
    public void onSuccess(int type, Object result) {
        super.onSuccess(type, result);
        if (null != result) {
            if (result instanceof MemberGrade.MemberGradeGainResponse) {
                MemberGrade.MemberGradeGainResponse response = (MemberGrade.MemberGradeGainResponse) result;
                memberGradeInfo = response.getMemberGradeInfo();
                initViewByInfo(response.getMemberGradeInfo());
            }
        }
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
    }

    @Override
    public void onBackPressed() {
        if (null != mProfile) {
            userComment(mProfile.getUserInfo().getId(), contentEdt.getText().toString());
        }
        Intent intent = new Intent();
        if (null != memberGradeInfo) {
            intent.putExtra("memberGradeInfo", memberGradeInfo);
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void initViewByInfo(MemberGrade.MemberGradeInfo memberGradeInfo) {
        behaviourRatingBar.setRating(memberGradeInfo.getBehaviour() / 2);
        resourceRatingBar.setRating(memberGradeInfo.getRecource() / 2);
        powerRatingBar.setRating(memberGradeInfo.getAbility() / 2);
        contentEdt.setText(memberGradeInfo.getComment());
        if (!TextUtils.isEmpty(memberGradeInfo.getComment()))
            contentEdt.setSelection(memberGradeInfo.getComment().length());
    }

    private void userGradeBehaviour(MemberGrade.MemberGradeRequest.Type type, int userId, int score) {
        if (TaskManager.getInstance().exist(ID_TASK_GRADE_BEHAVIOUR)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_GRADE_BEHAVIOUR);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.userGrade(ID_TASK_GRADE_BEHAVIOUR, type, userId, score);
    }

    private void userGradeResource(MemberGrade.MemberGradeRequest.Type type, int userId, int score) {
        if (TaskManager.getInstance().exist(ID_TASK_GRADE_RESOURCE)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_GRADE_RESOURCE);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.userGrade(ID_TASK_GRADE_RESOURCE, type, userId, score);
    }

    private void userGradePower(MemberGrade.MemberGradeRequest.Type type, int userId, int score) {
        if (TaskManager.getInstance().exist(ID_TASK_GRADE_POWER)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_GRADE_POWER);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.userGrade(ID_TASK_GRADE_POWER, type, userId, score);
    }

    private void userComment(int userId, String comment) {
        if (TaskManager.getInstance().exist(ID_TASK_COMMENT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_COMMENT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.userEvaluate(ID_TASK_COMMENT, userId, comment);
    }

    private void getGradeAndComment(int userId) {
        if (TaskManager.getInstance().exist(ID_TASK_GET_GRADE_AND_COMMENT)) {
            return;
        }
        TaskManager.getInstance().addTask(this, ID_TASK_GET_GRADE_AND_COMMENT);
        if (null == grpcController)
            grpcController = new GrpcController();
        grpcController.getGradeAndComment(ID_TASK_GET_GRADE_AND_COMMENT, userId);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (null == mProfile || !fromUser)
            return;
        switch (ratingBar.getId()) {
            case R.id.behaviour_ratingBar:
                userGradeBehaviour(MemberGrade.MemberGradeRequest.Type.BEHAVIOUR, mProfile.getUserInfo().getId(), (int) (rating * 2));
                break;
            case R.id.resource_ratingBar:
                userGradeResource(MemberGrade.MemberGradeRequest.Type.RESOURCE, mProfile.getUserInfo().getId(), (int) (rating * 2));
                break;
            case R.id.power_ratingBar:
                userGradePower(MemberGrade.MemberGradeRequest.Type.ABILITY, mProfile.getUserInfo().getId(), (int) (rating * 2));
                break;
        }
    }
}

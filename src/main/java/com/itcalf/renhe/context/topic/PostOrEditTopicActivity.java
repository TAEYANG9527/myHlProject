package com.itcalf.renhe.context.topic;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.EditText;
import com.itcalf.renhe.view.WebViewForIndustryCircle;

import butterknife.BindView;
import cn.renhe.heliao.idl.team.TeamTopic;

/**
 * Created by wangning on 2016/4/19.
 */
public class PostOrEditTopicActivity extends BaseActivity {
    @BindView(R.id.title_et)
    EditText titleEt;
    @BindView(R.id.content_et)
    EditText contentEt;
    //常量
    private int ID_TASK_POST_TOPIC = TaskManager.getTaskId();//发布话题
    private int ID_TASK_EDIT_TOPIC = TaskManager.getTaskId();//编辑话题

    //DATA
    private int circleId;//发布话题所需要的circleId
    private int topicId;//编辑话题所需要的话题id
    private int postId;//编辑话题所需要
    private String topicTitle;
    private String topicContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.post_topic_layout);
    }

    @Override
    protected void findView() {
        super.findView();
    }

    @Override
    protected void initData() {
        super.initData();
        circleId = getIntent().getIntExtra("circleId", -1);
        topicId = getIntent().getIntExtra("topicId", -1);
        postId = getIntent().getIntExtra("postId", -1);
        if (circleId >= 0) {//发布话题
            setTextValue("发话题");
        } else if (topicId >= 0 && postId >= 0) {//编辑话题
            setTextValue("修改话题");
        }
        topicTitle = getIntent().getStringExtra("topicTitle");
        topicContent = getIntent().getStringExtra("topicContent");
        if (!TextUtils.isEmpty(topicTitle))
            titleEt.setText(topicTitle);
        if (!TextUtils.isEmpty(topicContent))
            contentEt.setText(topicContent);
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_send:
                save();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem publishItem = menu.findItem(R.id.item_send);
        publishItem.setTitle("发布");
        publishItem.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        topicTitle = titleEt.getText().toString().trim();
        topicContent = contentEt.getText().toString().trim();
        if (!TextUtils.isEmpty(topicTitle) || !TextUtils.isEmpty(topicContent)) {
            materialDialogsUtil
                    .getBuilder(R.string.material_dialog_title, getString(R.string.topic_is_save), R.string.material_dialog_sure,
                            R.string.material_dialog_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {

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

    private boolean save() {
        topicTitle = titleEt.getText().toString().trim();
        topicContent = contentEt.getText().toString().trim();
        if (TextUtils.isEmpty(topicTitle)) {
            ToastUtil.showToast(this, R.string.topic_title_empy);
            return true;
        }
        if (TextUtils.isEmpty(topicContent)) {
            ToastUtil.showToast(this, R.string.topic_content_empy);
            return true;
        }

        if (circleId >= 0) {//发布话题
            postTopic(circleId, topicTitle, topicContent);
        } else if (topicId >= 0 && postId >= 0) {//编辑话题
            editTopic(topicId, postId, topicTitle, topicContent);
        }
        return false;
    }

    /**
     * 发布话题
     */
    private void postTopic(int circleId, String title, String content) {
        if (checkGrpcBeforeInvoke(ID_TASK_POST_TOPIC)) {
            materialDialogsUtil.showIndeterminateProgressDialog(R.string.posting).cancelable(false).build();
            materialDialogsUtil.show();
            grpcController.postTopic(ID_TASK_POST_TOPIC, circleId, title, content);
        }
    }

    /**
     * 编辑话题
     */
    private void editTopic(int topicId, int postId, String title, String content) {
        if (checkGrpcBeforeInvoke(ID_TASK_EDIT_TOPIC)) {
            materialDialogsUtil.showIndeterminateProgressDialog(R.string.posting).cancelable(false).build();
            materialDialogsUtil.show();
            grpcController.editTopic(ID_TASK_EDIT_TOPIC, topicId, postId, title, content);
        }
    }

    @Override
    public void onSuccess(int taskId, Object result) {
        super.onSuccess(taskId, result);
        materialDialogsUtil.dismiss();

        Intent intent = new Intent();
        if (null != result) {
            if (result instanceof TeamTopic.PostResponse) {
                TeamTopic.PostResponse postResponse = (TeamTopic.PostResponse) result;
                if (!TextUtils.isEmpty(postResponse.getUrl())) {
                    Intent mintent = new Intent();
                    mintent.setClass(this, WebViewForIndustryCircle.class);
                    mintent.putExtra("url", postResponse.getUrl());
                    if (postResponse.getUrl().contains("renhe")) {
                        String fix = "?";
                        if (postResponse.getUrl().contains("?")) {
                            fix = "&";
                        }
                        mintent.putExtra("login",
                                fix + "adSid=" + RenheApplication.getInstance().getUserInfo().getAdSId() + "&sid="
                                        + RenheApplication.getInstance().getUserInfo().getSid());
                    }
                    startActivity(mintent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);



                    intent.putExtra("topicDetailUrl", postResponse.getUrl());
                }
            }
        }
        setResult(RESULT_OK, intent);
        finish();
//        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onFailure(int type, String msg) {
        super.onFailure(type, msg);
        materialDialogsUtil.dismiss();
        ToastUtil.showToast(this, msg);
    }
}

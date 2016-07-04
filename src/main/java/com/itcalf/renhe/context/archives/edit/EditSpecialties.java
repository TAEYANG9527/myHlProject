package com.itcalf.renhe.context.archives.edit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.EditSummaryInfoSpecialtiesTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.SpecialtiesInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.widget.FlowLayout;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class EditSpecialties extends BaseActivity {
    private FlowLayout specialtiesGroup;
    private View addItemView;
    private final static int CAN_EDIT_COUNT = 13;
    private int specialtiesCount = 0;
    private boolean isModify;
    private boolean isSpecialtiesNull;
    private Dialog mAlertDialog;
    private Profile mProfile;
    private SpecialtiesInfo[] specialtiesInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.edit_specialties);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("技能专长");
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("技能专长");
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        setTextValue(R.id.title_txt, "技能专长");
        specialtiesGroup = (FlowLayout) findViewById(R.id.specialties_group);
        specialtiesGroup.removeAllViews();
        createSpecialtiesButton();
    }

    @Override
    protected void initData() {
        super.initData();
        mProfile = (Profile) getIntent().getSerializableExtra("Profile");
        if (mProfile != null) {
            specialtiesInfo = mProfile.getUserInfo().getSpecialtiesInfo();
            if (null != specialtiesInfo) {
                for (int i = 0; i < specialtiesInfo.length; i++) {
                    if (!TextUtils.isEmpty(specialtiesInfo[i].getTitle())) {
                        if (specialtiesCount < CAN_EDIT_COUNT) {
                            View itemView = LayoutInflater.from(EditSpecialties.this).inflate(R.layout.specialties_item, null);
                            Button item = (Button) itemView.findViewById(R.id.item);
                            item.setText(specialtiesInfo[i].getTitle());
                            item.setOnClickListener(new RemoveItemListener(specialtiesGroup, itemView));
                            specialtiesGroup.addView(itemView, specialtiesGroup.getChildCount() - 1);
                            specialtiesCount++;
                        }
                        if (specialtiesCount == CAN_EDIT_COUNT) {
                            if (null != specialtiesGroup && null != addItemView) {
                                specialtiesGroup.removeView(addItemView);
                                addItemView = null;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem saveItem = menu.findItem(R.id.item_save);
        saveItem.setVisible(true);
        saveItem.setTitle("保存");
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_save) {
            goSave();
        } else if (item.getItemId() == android.R.id.home) {
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBack() {
        if (isModify) {
            MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
            materialDialog
                    .getBuilder(R.string.material_dialog_title, R.string.is_save, R.string.material_dialog_sure,
                            R.string.material_dialog_cancel, R.string.material_dialog_give_up)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            finish();
                            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            goSave();
                        }
                    });
            materialDialog.show();
        } else {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    private void goSave() {
        String[] specialties = null;
        List<String> tempList = new ArrayList<String>();
        if (null != specialtiesGroup) {
            if (specialtiesCount >= CAN_EDIT_COUNT) {
                for (int i = 0; i < CAN_EDIT_COUNT - 1; i++) {
                    String s = ((Button) specialtiesGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isSpecialtiesNull = false;
                        tempList.add(s);
                    }
                }
            } else {
                for (int i = 0; i < specialtiesCount - 1; i++) {
                    String s = ((Button) specialtiesGroup.getChildAt(i).findViewById(R.id.item)).getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        isSpecialtiesNull = false;
                        tempList.add(s);
                    }
                }
            }

        }
        if (isSpecialtiesNull) {
            ToastUtil.showToast(EditSpecialties.this, "技能专长不能为空");
            return;
        } else {
            specialties = new String[tempList.size()];
            for (int i = 0; i < tempList.size(); i++) {
                specialties[i] = tempList.get(i);
            }
        }
        if (null == specialties || specialties.length <= 0) {
            ToastUtil.showToast(EditSpecialties.this, "技能专长不能为空");
            return;
        }
        final SpecialtiesInfo[] finalspecialtiesInfo = new SpecialtiesInfo[specialties.length];
        for (int i = 0; i < specialties.length; i++) {
            SpecialtiesInfo mInfo = new SpecialtiesInfo();
            mInfo.setTitle(specialties[i]);
            finalspecialtiesInfo[i] = mInfo;
        }

        new EditSummaryInfoSpecialtiesTask(EditSpecialties.this, specialties) {
            @Override
            public void doPre() {
                super.doPre();
                showDialog(1);

            }

            @Override
            public void doPost(MessageBoardOperation result) {
                super.doPost(result);
                if (result == null) {
                    removeDialog(1);
                    ToastUtil.showToast(EditSpecialties.this, R.string.network_anomaly);
                } else if (result.getState() == 1) {
                    new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                            getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                            getRenheApplication().getUserInfo().getAdSId());
                } else if (result.getState() == -3) {
                    removeDialog(1);
                    ToastUtil.showToast(EditSpecialties.this, "技能专长不能为空");
                } else if (result.getState() == -4) {
                    removeDialog(1);
                    ToastUtil.showToast(EditSpecialties.this, "技能专长单项长度过长，长度不能超过15个字");
                } else if (result.getState() == -5) {
                    removeDialog(1);
                    ToastUtil.showToast(EditSpecialties.this, "技能专长数量过多，不能超过12个");
                } else {
                    removeDialog(1);
                    ToastUtil.showToast(EditSpecialties.this, R.string.sorry_of_unknow_exception);
                }
            }

        }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                getRenheApplication().getUserInfo().getAdSId());

    }

    private void createSpecialtiesButton() {
        if (specialtiesCount < CAN_EDIT_COUNT) {
            if (null == addItemView) {
                createAddBtn();
                specialtiesCount++;
            }
        }
    }

    private void createAddBtn() {
        addItemView = LayoutInflater.from(EditSpecialties.this).inflate(R.layout.specialties_item, null);
        Button item = (Button) addItemView.findViewById(R.id.item);
        item.setText("+添加");
        item.setBackgroundResource(R.drawable.archive_provide_get_item_shape);
        Resources resource = getBaseContext().getResources();
        ColorStateList csl = resource.getColorStateList(R.color.add_btn_textcolor_selector);
        item.setTextColor(csl);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showEditTextDialog(R.string.please_input, R.string.specialties_tip);
            }
        });
        specialtiesGroup.addView(addItemView, specialtiesGroup.getChildCount());
    }

    private void mcreateSpecialtiesButton() {
        if (specialtiesCount < CAN_EDIT_COUNT) {
            if (null == addItemView) {
                createAddBtn();
            }
        }
    }

    private void showEditTextDialog(int title, int message) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
        materialDialog.getBuilder(title, message)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                        | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .inputMaxLength(Constants.SPECIALTY).cancelable(false).input(0, 0, true, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
                String name = input.toString();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(name.trim())) {
                    ToastUtil.showToast(EditSpecialties.this,
                            getResources().getString(R.string.editprovideget_content_unempty));
                    return;
                }
                if (name.length() > 15) {
                    ToastUtil.showToast(EditSpecialties.this,
                            getResources().getString(R.string.editprovideget_content_limit));
                    return;
                }
                dialog.dismiss();

                if (specialtiesCount < CAN_EDIT_COUNT) {
                    View itemView = LayoutInflater.from(EditSpecialties.this).inflate(R.layout.specialties_item, null);
                    Button item = (Button) itemView.findViewById(R.id.item);
                    item.setText(name);
                    item.setOnClickListener(new RemoveItemListener(specialtiesGroup, itemView));
                    specialtiesGroup.addView(itemView, specialtiesGroup.getChildCount() - 1);
                    specialtiesCount++;
                    isModify = true;
                }
                if (specialtiesCount == CAN_EDIT_COUNT) {
                    if (null != specialtiesGroup && null != addItemView) {
                        specialtiesGroup.removeView(addItemView);
                        addItemView = null;
                    }
                }
            }
        });
        materialDialog.show();
    }

    class RemoveItemListener implements View.OnClickListener {
        FlowLayout groupView;
        View itemView;

        public RemoveItemListener(FlowLayout groupView, View itemView) {
            this.groupView = groupView;
            this.itemView = itemView;
        }

        @Override
        public void onClick(View arg0) {
            isModify = true;
            createDialog(EditSpecialties.this, groupView, itemView);
        }

    }

    public void createDialog(Context context, final FlowLayout groupView, final View mItemView) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(context);
        materialDialog.showSelectList(R.array.delete_item).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        if (null != groupView) {
                            groupView.removeView(mItemView);
                        }

                        specialtiesCount--;
                        if (specialtiesCount < CAN_EDIT_COUNT) {
                            mcreateSpecialtiesButton();
                        }
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });
        materialDialog.show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
            default:
                return null;
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
                        EditSpecialties.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Profile result) {
            super.onPostExecute(result);
            removeDialog(1);
            if (null != result) {
                if (1 == result.getState() && null != result.getUserInfo()) {
                    Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    brocastIntent.putExtra("Profile", result);
                    sendBroadcast(brocastIntent);
                    Intent intent = new Intent();
                    intent.putExtra("Profile", result);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            } else {
                ToastUtil.showNetworkError(EditSpecialties.this);
            }
        }
    }

}

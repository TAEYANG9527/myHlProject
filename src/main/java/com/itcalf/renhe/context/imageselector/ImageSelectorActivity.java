package com.itcalf.renhe.context.imageselector;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.BaseActivity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageSelectorActivity
 * Created by Yancy on 2015/12/2.
 */
public class ImageSelectorActivity extends BaseActivity implements ImageSelectorFragment.Callback {
    public static final String EXTRA_RESULT = "select_result";
    private static final int REQUEST_VIEW_PHOTO = 101;
    private static final int RESULT_SEND = 102;
    private ArrayList<String> pathList = new ArrayList<>();
    private ImageConfig imageConfig;
    private TextView submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.imageselector_activity);
        setTextValue("图片");
        imageConfig = ImageSelector.getImageConfig();

//        Utils.hideTitleBar(this, R.id.imageselector_activity_layout, imageConfig.getSteepToolBarColor());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this, ImageSelectorFragment.class.getName(), null))
                .commit();

        submitButton = (TextView) super.findViewById(R.id.title_right);
        init();
    }

    private void init() {
        if (!imageConfig.isMutiSelect()) {
            submitButton.setVisibility(View.INVISIBLE);
        }
        pathList = imageConfig.getPathList();
        if (pathList == null || pathList.size() <= 0) {
            submitButton.setText(R.string.done);
            submitButton.setEnabled(false);
        } else {
            submitButton.setText((getResources().getText(R.string.done)) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            submitButton.setEnabled(true);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pathList != null && pathList.size() > 0) {
                    submitButton.setEnabled(false);
                    Intent data = new Intent();
                    data.putStringArrayListExtra(EXTRA_RESULT, pathList);
                    setResult(RESULT_OK, data);
                    exit();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageSelector.IMAGE_CROP_CODE) {
                Intent intent = new Intent();
                pathList.add(cropImagePath);
                intent.putStringArrayListExtra(EXTRA_RESULT, pathList);
                setResult(RESULT_OK, intent);
                exit();
            }
        } else if (resultCode == RESULT_SEND) {
            if (requestCode == REQUEST_VIEW_PHOTO) {
                submitButton.performClick();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void exit() {
        finish();
    }

    private String cropImagePath;

    private void crop(String imagePath, int aspectX, int aspectY, int outputX, int outputY) {
        File file;
        if (Utils.existSDCard()) {
            file = new File(Environment.getExternalStorageDirectory() + imageConfig.getFilePath(), Utils.getImageName());
        } else {
            file = new File(getCacheDir(), Utils.getImageName());
        }
        cropImagePath = file.getAbsolutePath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, ImageSelector.IMAGE_CROP_CODE);
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (imageConfig.isCrop()) {
            crop(path, imageConfig.getAspectX(), imageConfig.getAspectY(), imageConfig.getOutputX(), imageConfig.getOutputY());
        } else {
            Intent data = new Intent();
            pathList.add(path);
            data.putStringArrayListExtra(EXTRA_RESULT, pathList);
            setResult(RESULT_OK, data);
            exit();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!pathList.contains(path)) {
            pathList.add(path);
        }
        if (pathList.size() > 0) {
            submitButton.setText((getResources().getText(R.string.done)) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            if (!submitButton.isEnabled()) {
                submitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(String path) {
        if (pathList.contains(path)) {
            pathList.remove(path);
            submitButton.setText((getResources().getText(R.string.done)) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        } else {
            submitButton.setText((getResources().getText(R.string.done)) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        }
        if (pathList.size() == 0) {
            submitButton.setText(R.string.done);
            submitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            if (imageConfig.isCrop()) {
                crop(imageFile.getAbsolutePath(), imageConfig.getAspectX(), imageConfig.getAspectY(), imageConfig.getOutputX(), imageConfig.getOutputY());
            } else {
//                Intent data = new Intent();
                pathList.add(imageFile.getAbsolutePath());
//                data.putStringArrayListExtra(EXTRA_RESULT, pathList);
//                setResult(RESULT_OK, data);
//                exit();

                String path = imageFile.getAbsolutePath();
                String name = imageFile.getName();
                long dateTime = System.currentTimeMillis();
                Image image = new Image(path, name, dateTime);
                List<Image> imageList = new ArrayList<>();
                imageList.add(image);
                Intent intent = new Intent(this, SelectPhotoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("index", 0);
                bundle.putSerializable("pathArray", (Serializable) imageList);
                bundle.putBoolean("isCameraShot", true);//用来标示是从拍照跳转过去的
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_VIEW_PHOTO);
            }
        }

    }

    @Override
    public void changeTitle(String text, boolean isButtonEnable) {
        submitButton.setText(text);
        submitButton.setEnabled(isButtonEnable);
    }

    @Override
    public void setResult() {
        submitButton.performClick();
    }
}
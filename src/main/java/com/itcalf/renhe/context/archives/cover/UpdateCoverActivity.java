package com.itcalf.renhe.context.archives.cover;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.cropImage.CoverCropImage;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

public class UpdateCoverActivity extends BaseActivity {
    private RelativeLayout galleryRl;
    private RelativeLayout cameraRl;
    private RelativeLayout memberRl;
    //更新封面
    public static final int COVER_REQUEST_CODE_CHOOSE_PICTURE = 2001;
    public static final int COVER_REQUEST_CODE_CHOOSE_PICTURE_KITKAT = 2002;
    public static final int COVER_REQUEST_CODE_CAPTURE_CUT = 2003;
    public static final int COVER_REQUEST_CODE_CHOOSE_CAPTURE = 2004;
    public static final String COVER_IMAGE_FILE_NAME = "cover_faceImage.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.update_cover);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("更换档案封面"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("更换档案封面"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        galleryRl = (RelativeLayout) findViewById(R.id.add_friend_rl1);
        cameraRl = (RelativeLayout) findViewById(R.id.add_friend_rl2);
        memberRl = (RelativeLayout) findViewById(R.id.contact_rl1);

    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "更换档案封面");
    }

    @Override
    protected void initListener() {
        super.initListener();
        galleryRl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                MobclickAgent.onEvent(UpdateCoverActivity.this, "self_refresh_cover_gallery");
                //					startActivity(TestPicActivity.class);
                if (Build.VERSION.SDK_INT < 19) {
                    Intent intentFromGallery = new Intent();
                    intentFromGallery.setType("image/*"); // 设置文件类型
                    intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intentFromGallery, COVER_REQUEST_CODE_CHOOSE_PICTURE);
                } else {
                    Intent intentFromGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
                    intentFromGallery.setType("image/*");
                    startActivityForResult(intentFromGallery, COVER_REQUEST_CODE_CHOOSE_PICTURE_KITKAT);
                }

            }
        });
        cameraRl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                MobclickAgent.onEvent(UpdateCoverActivity.this, "self_refresh_cover_camera");
                //					photo();
                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 判断存储卡是否可以用，可用进行存储
                //                            if (hasSdcard()) {

                //                            }
                intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory(), COVER_IMAGE_FILE_NAME)));
                startActivityForResult(intentFromCapture, COVER_REQUEST_CODE_CHOOSE_CAPTURE);

            }
        });
        memberRl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intentFromGallery = new Intent(UpdateCoverActivity.this, MemberCoverActivity.class);
                startActivityForResult(intentFromGallery, COVER_REQUEST_CODE_CAPTURE_CUT);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.gc();
        switch (requestCode) {
            // 回调照片本地浏览
            case COVER_REQUEST_CODE_CHOOSE_PICTURE:
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
                        if (null != cursor) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            //最后根据索引值获取图片路径
                            String path = cursor.getString(column_index);
                            startCustomPhotoZoom(path, true);
                        }
                    }
                }
                break;
            case COVER_REQUEST_CODE_CHOOSE_PICTURE_KITKAT:
                if (resultCode == RESULT_OK) {
                    startCustomPhotoZoom(getPath(UpdateCoverActivity.this, data.getData()), true);
                }
                break;
            // 回调照片剪切
            case COVER_REQUEST_CODE_CAPTURE_CUT:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
                break;
            // 回调照相机
            case COVER_REQUEST_CODE_CHOOSE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    File tempFile = new File(Environment.getExternalStorageDirectory(), COVER_IMAGE_FILE_NAME);
                    startCustomPhotoZoom(tempFile.getPath(), true);
                }
                break;
        }

    }

    /**
     * 调用自定义裁剪工具裁剪图片方法实现
     *
     * @param uri
     */
    public void startCustomPhotoZoom(String path, boolean isToCover) {

        Intent intent = new Intent(this, CoverCropImage.class);
        // 设置裁剪
        intent.putExtra("path", path);
        intent.putExtra("istocover", true);
        startActivityForResult(intent, COVER_REQUEST_CODE_CAPTURE_CUT);
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

                // TODO handle non-primary volumes
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
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.corver_uploading).cancelable(false).build();
            default:
                return null;
        }
    }
}

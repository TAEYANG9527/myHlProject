package com.itcalf.renhe.context.wukong.im;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.contacts.ToShareWithRecentContactsActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.DownLoadFile;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

/**
 * Title: FilePrviewActivity.java<br>
 * Description:IM接收文件预览 <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-9 下午1:14:36 <br>
 *
 * @author wangning
 */
public class FilePrviewActivity extends BaseActivity {
    private Button openButton;
    private Button shareButton;
    private TextView fileNameTv;
    private TextView fileSizeTv;
    private TextView downloadFileTv;
    private ProgressBar downloadPb;
    private String fileUrl;
    private String fileName;
    private long fileSize;
    private String fileType;
    private DownLoadFile downLoadFile;
    private String messageId;
    private ImageView closeIv;
    private RelativeLayout progressbarRl;
    private TextView fileTipTv;
    private ImageView fileTypeIv;
    private int type;//判断是来自自己发送的，还是对方发送的,1代表自己发的
    private String localPath;//文件的本地路径，在type=1时有用
    private Message message;
    private String path = null;//要打开的文件路径
    private ImageView previewIv;
    private LinearLayout descpInfoLl;
    private TextView previewTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.file_preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("文件预览"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("文件预览"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @SuppressLint("NewApi")
    @Override
    protected void findView() {
        super.findView();
        setTextValue(R.id.title_txt, "文件预览");
        fileNameTv = (TextView) findViewById(R.id.filenameTv);
        fileSizeTv = (TextView) findViewById(R.id.filesizeTv);
        openButton = (Button) findViewById(R.id.openBt);
        shareButton = (Button) findViewById(R.id.shareBt);
        downloadPb = (ProgressBar) findViewById(R.id.download_pb);
        downloadFileTv = (TextView) findViewById(R.id.download_tv);
        closeIv = (ImageView) findViewById(R.id.progressbar_close_iv);
        progressbarRl = (RelativeLayout) findViewById(R.id.progressbar_rl);
        fileTipTv = (TextView) findViewById(R.id.filetipTv);
        fileTypeIv = (ImageView) findViewById(R.id.filetypeIv);
        previewIv = (ImageView) findViewById(R.id.previewIv);
        descpInfoLl = (LinearLayout) findViewById(R.id.descpInfoLl);
        previewTv = (TextView) findViewById(R.id.previewTv);
    }

    @Override
    protected void initData() {
        super.initData();
        fileUrl = getIntent().getStringExtra("fileUrl");
        fileName = getIntent().getStringExtra("fileName");
        fileType = getIntent().getStringExtra("fileType");
        fileSize = getIntent().getLongExtra("fileSize", 0L);
        messageId = getIntent().getStringExtra("messageId");
        localPath = getIntent().getStringExtra("fileLocalPath");
        type = getIntent().getIntExtra("type", 0);
        if (getIntent().getSerializableExtra("message") != null)
            message = (Message) getIntent().getSerializableExtra("message");
        if (!TextUtils.isEmpty(fileName)) {
            fileNameTv.setText(fileName);
        }
        fileSizeTv.setText(FileSizeUtil.FormetFileSize(fileSize));

        if (type == 1) {
            path = localPath;
            if (!TextUtils.isEmpty(path)) {
                File mFile = new File(path);
                if (!mFile.exists()) {
                    path = Constants.CACHE_PATH.IM_DOWNLOAD_PATH + messageId + fileName;
                }
            } else {
                path = Constants.CACHE_PATH.IM_DOWNLOAD_PATH + messageId + fileName;
            }
        } else {
            path = Constants.CACHE_PATH.IM_DOWNLOAD_PATH + messageId + fileName;
        }
        File file = new File(path);
        if (file.exists()) {//如果文件已存在，直接预览
            progressbarRl.setVisibility(View.GONE);
            fileSizeTv.setVisibility(View.VISIBLE);
            fileTipTv.setVisibility(View.VISIBLE);
            openButton.setEnabled(true);
            prviewFile(path, fileType);
        } else {//否则下载
            if (!TextUtils.isEmpty(fileUrl)) {
                progressbarRl.setVisibility(View.VISIBLE);
                openButton.setEnabled(false);
                downLoadFile = new DownLoadFile();
                downLoadFile.setId(messageId);
                downLoadFile.setName(fileName);
                downLoadFile.setUrl(fileUrl);
                //			downLoadFile.setSize(fileSize);
                downLoadFile.setProgressBar(downloadPb);
                downLoadFile.setDownLoadTv(downloadFileTv);
                downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_WAITTIGN);
                RenheApplication.getInstance().getDownloadService().startDownload(downLoadFile);
            }
        }
        if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_TXT)) {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_txt);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_DOC)) {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_word);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_EXCEL)) {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_excel);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PPT)) {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_ppt);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PDF)) {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_pdf);
        } else {
            fileTypeIv.setImageResource(R.drawable.icon_filetype_unknown);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        openButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (openButton.getText().toString().contains("下载原文件")) {
                    openButton.setEnabled(false);
                    openButton.setText("用其他应用打开");
                    progressbarRl.setVisibility(View.VISIBLE);
                    fileSizeTv.setVisibility(View.GONE);
                    if (null != downLoadFile)
                        RenheApplication.getInstance().getDownloadService().startDownload(downLoadFile);
                } else {
                    openFileByOtherApp(path, fileType);
                }
            }
        });
        downloadFileTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("下载完成")) {
                    openButton.setEnabled(true);
                    openButton.setText("用其他应用打开");
                    progressbarRl.setVisibility(View.GONE);
                    fileSizeTv.setVisibility(View.VISIBLE);
                    fileTipTv.setVisibility(View.VISIBLE);
                    if (type != 1) {
                        sendBroadcast(new Intent(Constants.BroadCastAction.IM_CHAT_REFRESH_ACTION));
                    }
                    prviewFile(path, fileType);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        closeIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openButton.setEnabled(true);
                openButton.setText("下载原文件");
                progressbarRl.setVisibility(View.GONE);
                fileSizeTv.setVisibility(View.VISIBLE);
                if (null != downLoadFile) {
                    if (downLoadFile.getState() == Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_DOWNLOADING)
                        ToastUtil.showToast(FilePrviewActivity.this, getString(R.string.file_preview_download_stop));
                    downLoadFile.setState(Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_WAITTIGN);
                }
            }
        });
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("message", message);
                bundle.putInt("type", Constants.ConversationShareType.CONVERSATION_SEND_FROM_FILE_FORWARD);
                Intent intent = new Intent(FilePrviewActivity.this, ToShareWithRecentContactsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    /**
     * 打开一个文件
     *
     * @param filePath 文件的绝对路径
     */
    private void openFileByOtherApp(String filePath, String fileType) {
        try {
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(fileType);
            if (!TextUtils.isEmpty(mime)) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(filePath);
                intent.setDataAndType(Uri.fromFile(file), mime);
                startActivity(intent);
            } else {
                ToastUtil.showToast(this, "无法打开该文件！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(this, "无法打开该文件！");
        }

    }

    /**
     * @param fileType 文件类型
     */
    private void prviewFile(final String filePath, String fileType) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mime = mimeTypeMap.getMimeTypeFromExtension(fileType);
        if (!TextUtils.isEmpty(mime)) {
            if (mime.contains("image")) {//图片类型的文件
                descpInfoLl.setVisibility(View.GONE);
                previewIv.setVisibility(View.VISIBLE);
                File file = new File(filePath);
                Uri uri = Uri.fromFile(file);
                previewIv.setImageURI(uri);
            }
//            else if (mime.contains("text")) {//txt文件 txt文档过大时，应用会卡死，故注释掉，应用不再解析txt文档
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        StringBuffer sb = new StringBuffer();
//                        File file = new File(filePath);
//                        try {
//                            FileInputStream is = new FileInputStream(file);
//                            byte[] b = new byte[is.available()];
//                            is.read(b);
//                            final String result = new String(b);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    previewTv.setText(result);
//                                    descpInfoLl.setVisibility(View.GONE);
//                                    previewTv.setVisibility(View.VISIBLE);
//                                }
//                            });
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != downLoadFile) {
            RenheApplication.getInstance().getDownloadService().remove(downLoadFile.getUrl());
            if (downLoadFile.getState() == Constants.FILE_DOWNLOAD_STATE.DOWNLOAD_STATE_DOWNLOADING)
                ToastUtil.showToast(this, getString(R.string.file_preview_download_stop));
        }
    }
}

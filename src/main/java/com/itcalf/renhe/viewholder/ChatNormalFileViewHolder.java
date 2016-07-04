package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.wukong.im.FilePrviewActivity;
import com.itcalf.renhe.utils.FileSizeUtil;
import com.itcalf.renhe.view.TextView;

import java.io.File;
import java.util.Map;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalFileViewHolder extends ChatViewHolder {

    private TextView fileTitleTv;
    private ImageView filePicIv;
    private TextView fileStateTv;
    private TextView fileSizeTv;
    public ProgressBar fileProgressBar;
    public MessageContent.FileContent messageContent;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String fileDownLoadUrl;
    private String fileLocalPath;//文件的本地路径（如果文件已经存在的话）

    public ChatNormalFileViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                    RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        fileTitleTv = ((TextView) itemView.findViewById(R.id.file_title_tv));
        filePicIv = ((ImageView) itemView.findViewById(R.id.file_pic_iv));
        fileStateTv = ((TextView) itemView.findViewById(R.id.file_state_tv));
        fileSizeTv = ((TextView) itemView.findViewById(R.id.file_size_tv));
        fileProgressBar = ((ProgressBar) itemView.findViewById(R.id.file_progressbar));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.FileContent) message.messageContent();
        if (null == messageContent)
            return;
        fileName = messageContent.fileName();
        fileType = messageContent.fileType();
        fileSize = messageContent.size();
        fileDownLoadUrl = messageContent.url();
        Map<String, String> localPathMap = message.localExtras();
        if (null != localPathMap && !localPathMap.isEmpty()) {
            fileLocalPath = localPathMap.get("localPath");
        }
        if (isSenderIsSelf()) {
            fileStateTv.setVisibility(View.GONE);
            if (message.status() == Message.MessageStatus.OFFLINE) {
                fileProgressBar.setVisibility(View.GONE);
            } else if (message.status() == Message.MessageStatus.SENDING) {
                fileProgressBar.setVisibility(View.VISIBLE);
                fileProgressBar.setProgress(message.sendProgress());
            } else {
                fileProgressBar.setVisibility(View.GONE);
            }
        } else {
            fileStateTv.setVisibility(View.VISIBLE);
            fileProgressBar.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(fileLocalPath)) {
                File localFile = new File(fileLocalPath);
                if (localFile.exists()) {
                    fileStateTv.setText("已下载");
                } else {
                    fileStateTv.setText("未下载");
                }
            } else {
                fileStateTv.setText("未下载");
            }
        }
        fileTitleTv.setText(fileName);
        fileSizeTv.setText(FileSizeUtil.FormetFileSize(fileSize));
        if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_TXT)) {
            filePicIv.setImageResource(R.drawable.icon_filetype_txt);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_DOC)) {
            filePicIv.setImageResource(R.drawable.icon_filetype_word);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_EXCEL)) {
            filePicIv.setImageResource(R.drawable.icon_filetype_excel);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PPT)) {
            filePicIv.setImageResource(R.drawable.icon_filetype_ppt);
        } else if (fileType.contains(Constants.FILE_TYPE.FILE_TYPE_PDF)) {
            filePicIv.setImageResource(R.drawable.icon_filetype_pdf);
        } else {
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(fileType);
            if (!TextUtils.isEmpty(mime)) {
                if (mime.contains("image")) {//图片类型的文件
                    String imageUrl = ((MessageContent.FileContent) message.messageContent()).url();//图片下载地址
                    try {
                        imageLoader.displayImage(imageUrl, filePicIv, CacheManager.imageOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                        filePicIv.setImageResource(R.drawable.icon_filetype_unknown);
                    }
                } else {
                    filePicIv.setImageResource(R.drawable.icon_filetype_unknown);
                }
            } else {
                filePicIv.setImageResource(R.drawable.icon_filetype_unknown);
            }
        }


//        titleTv.setText(TextUtils.isEmpty(linkedContentTitle) ? linkedContentMsg : linkedContentTitle);
//        loadImage(rmqPicIv, linkedContentPicUrl);
//        rmqInfoTv.setText(linkedContentMsg);
    }

    @Override
    public void onContentRlClickListener() {
        if (isSenderIsSelf()) {
            if (message.status() == Message.MessageStatus.SENT && !TextUtils.isEmpty(fileDownLoadUrl) || !TextUtils.isEmpty(fileLocalPath)) {
                Intent intent = new Intent();
                intent.setClass(context, FilePrviewActivity.class);
                intent.putExtra("fileUrl", fileDownLoadUrl);
                intent.putExtra("fileName", fileName);
                intent.putExtra("messageId", message.messageId() + "");
                intent.putExtra("fileSize", fileSize);
                intent.putExtra("fileType", fileType);
                intent.putExtra("fileLocalPath", fileLocalPath);
                intent.putExtra("type", 1);
                intent.putExtra("message", message);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(context, FilePrviewActivity.class);
            intent.putExtra("fileUrl", fileDownLoadUrl);
            intent.putExtra("fileName", fileName);
            intent.putExtra("messageId", message.messageId() + "");
            intent.putExtra("fileSize", fileSize);
            intent.putExtra("fileType", fileType);
            intent.putExtra("message", message);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    @Override
    public void onContentRlLongClickListener() {
        if (null != chatUtils) {
            chatUtils.createForwardDialog(context, message);
        }
    }

}


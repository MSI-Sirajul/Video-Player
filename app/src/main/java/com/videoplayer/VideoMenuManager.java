package com.videoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.ArrayList;

public class VideoMenuManager {

    private Context context;
    private VideoDatabaseHelper dbHelper;

    public VideoMenuManager(Context context) {
        this.context = context;
        this.dbHelper = new VideoDatabaseHelper(context);
    }

    public void showVideoMenu(View anchorView, VideoItem video, VideoAdapter adapter, int position, ArrayList<VideoItem> currentList) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_video_menu);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                context.getResources().getDisplayMetrics().widthPixels - 100, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.menu_video_title);
        tvTitle.setText(video.getTitle());

        // --- ACTIONS ---

        // 1. PLAY
        dialog.findViewById(R.id.menu_item_play).setOnClickListener(v -> {
            dialog.dismiss();
            PlayerActivity.videoList = currentList; 
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("position", position);
            context.startActivity(intent);
        });

        // 2. PROPERTIES (INFO)
        dialog.findViewById(R.id.menu_item_info).setOnClickListener(v -> {
            dialog.dismiss();
            showVideoInfoDialog(video);
        });

        // NOTE: 'menu_item_path' এখান থেকে রিমুভ করা হয়েছে কারণ এটি এখন INFO ডায়ালগের ভেতরে আছে।
        // আগে এখানে কোড থাকার কারণেই ক্রাশ করছিল।

        // 3. DELETE
        dialog.findViewById(R.id.menu_item_delete).setOnClickListener(v -> {
            dialog.dismiss();
            
            // Permission Check for Android 11+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    Toast.makeText(context, "Please allow 'All Files Access' to delete videos", Toast.LENGTH_LONG).show();
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        context.startActivity(intent);
                    }
                    return; 
                }
            }
            
            showDeleteDialog(video, adapter, position);
        });

        dialog.show();
    }

    // --- INFO DIALOG LOGIC ---
    private void showVideoInfoDialog(VideoItem video) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_video_info);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvName = dialog.findViewById(R.id.info_name);
        TextView tvPath = dialog.findViewById(R.id.info_path);
        TextView tvSize = dialog.findViewById(R.id.info_size);
        TextView tvDuration = dialog.findViewById(R.id.info_duration);
        TextView tvResolution = dialog.findViewById(R.id.info_resolution);
        TextView tvFormat = dialog.findViewById(R.id.info_format);
        TextView btnOk = dialog.findViewById(R.id.btn_close_dialog);

        tvName.setText(video.getTitle());
        tvPath.setText(video.getPath());
        tvDuration.setText(video.getDuration());

        File file = new File(video.getPath());
        if (file.exists()) {
            tvSize.setText(Formatter.formatFileSize(context, file.length()));
        }

        // PATH CLICK LISTENER (Folder ওপেন করার লজিক এখানে আনা হয়েছে)
        tvPath.setOnClickListener(v -> {
            dialog.dismiss();
            openFileLocation(video.getPath());
        });

        // Meta Data Extraction
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(video.getPath());
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            
            if (width != null && height != null) {
                tvResolution.setText(width + "x" + height);
            } else {
                tvResolution.setText("Unknown");
            }
            
            String path = video.getPath();
            String format = path.substring(path.lastIndexOf(".") + 1).toUpperCase();
            tvFormat.setText(format);
            retriever.release();
        } catch (Exception e) {
            tvResolution.setText("Unknown");
            tvFormat.setText("Unknown");
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- OPEN FILE LOCATION (FIXED: Opens Folder) ---
    private void openFileLocation(String path) {
        try {
            File videoFile = new File(path);
            File folder = videoFile.getParentFile();

            if (folder == null || !folder.exists()) {
                Toast.makeText(context, "Folder not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", folder);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "resource/folder"); 
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(intent, "Open Folder"));
            } else {
                intent.setDataAndType(uri, "*/*");
                context.startActivity(Intent.createChooser(intent, "Open Folder"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Cannot open file manager", Toast.LENGTH_SHORT).show();
        }
    }

    // --- DELETE LOGIC ---
    private void showDeleteDialog(VideoItem video, VideoAdapter adapter, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Video")
                .setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteVideoFile(video, adapter, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteVideoFile(VideoItem video, VideoAdapter adapter, int position) {
        try {
            long id = Long.parseLong(video.id);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
            
            int rows = context.getContentResolver().delete(contentUri, null, null);
            
            if (rows > 0) {
                dbHelper.deleteVideo(video.getPath());
                adapter.removeItem(position);
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                File file = new File(video.getPath());
                if (file.delete()) {
                    dbHelper.deleteVideo(video.getPath());
                    adapter.removeItem(position);
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Could not delete file", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    IntentSender intentSender = exception.getUserAction().getActionIntent().getIntentSender();
                    try {
                        ((Activity) context).startIntentSenderForResult(intentSender, 123, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(context, "Permission Denied: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
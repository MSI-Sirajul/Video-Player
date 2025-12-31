package com.videoplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.ArrayList;

public class VideoMenuManager {

    private Context context;
    private VideoDatabaseHelper dbHelper;

    // কনস্ট্রাক্টর
    public VideoMenuManager(Context context) {
        this.context = context;
        this.dbHelper = new VideoDatabaseHelper(context);
    }

    
    // Method Updated: Uses Dialog instead of PopupWindow
    public void showVideoMenu(View anchorView, VideoItem video, VideoAdapter adapter, int position, ArrayList<VideoItem> currentList) {
        
        // 1. Dialog তৈরি
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_video_menu); // নতুন লেআউট ফাইল

        // 2. উইন্ডো ব্যাকগ্রাউন্ড ট্রান্সপারেন্ট করা (যাতে CardView এর রাউন্ড কর্নার দেখা যায়)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // ডায়ালগের প্রস্থ ঠিক করা (মার্জিন সহ)
            dialog.getWindow().setLayout(
                context.getResources().getDisplayMetrics().widthPixels - 100, // একটু প্যাডিং রাখা
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }

        // 3. টাইটেল সেট করা
        TextView tvTitle = dialog.findViewById(R.id.menu_video_title);
        tvTitle.setText(video.getTitle());

        // --- ACTIONS ---

        // PLAY
        dialog.findViewById(R.id.menu_item_play).setOnClickListener(v -> {
            dialog.dismiss();
            PlayerActivity.videoList = currentList; 
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("position", position);
            context.startActivity(intent);
        });

        // PROPERTIES (INFO)
        dialog.findViewById(R.id.menu_item_info).setOnClickListener(v -> {
            dialog.dismiss();
            showVideoInfoDialog(video);
        });

        // FILE PATH
        dialog.findViewById(R.id.menu_item_path).setOnClickListener(v -> {
            dialog.dismiss();
            openFileLocation(video.getPath());
        });

        // DELETE
        dialog.findViewById(R.id.menu_item_delete).setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteDialog(video, adapter, position);
        });

        // 4. ডায়ালগ দেখানো
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

        // Basic Info
        tvName.setText(video.getTitle());
        tvPath.setText(video.getPath());
        tvDuration.setText(video.getDuration());

        // Size Calculation
        File file = new File(video.getPath());
        if (file.exists()) {
            tvSize.setText(Formatter.formatFileSize(context, file.length()));
        }

        // Advanced Info (Resolution & Format) via MediaMetadataRetriever
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(video.getPath());
            
            String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            
            if (width != null && height != null) {
                tvResolution.setText(width + "x" + height);
            } else {
                tvResolution.setText("Unknown");
            }
            
            // Format extraction from path extension
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

    // --- OPEN FILE PATH LOGIC ---
    private void openFileLocation(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Android N+ এ FileProvider ব্যবহার করতে হয়
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "resource/folder"); // অনেক ফাইল ম্যানেজার এটি সাপোর্ট করে
            
            // যদি ফোল্ডার ওপেন করার স্পেসিফিক ইনটেন্ট কাজ না করে, তবে ফাইলটি সিলেক্ট করতে বলি
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                intent.setDataAndType(uri, "video/*"); // ডিফল্ট ভিউয়ার
            }
            
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Open File Location"));
            
        } catch (Exception e) {
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
        File file = new File(video.getPath());
        boolean deleted = false;

        // 1. Try deleting via File API
        if (file.exists()) {
            deleted = file.delete();
        }

        // 2. If failed (Android 10+), try via MediaStore
        if (!deleted) {
            try {
                long id = Long.parseLong(video.id); // Assuming ID is stored in VideoItem
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                int rows = context.getContentResolver().delete(contentUri, null, null);
                deleted = rows > 0;
            } catch (Exception e) {
                // Android 10+ এ পারমিশন চাইতে পারে (RecoverableSecurityException)
                // আপাতত সিম্পল ডিলিট লজিক রাখা হলো। কমপ্লেক্স লজিকের জন্য onActivityResult দরকার।
                Toast.makeText(context, "System Permission Needed to Delete", Toast.LENGTH_SHORT).show();
            }
        }

        if (deleted) {
            // 3. Remove from Database
            dbHelper.deleteVideo(video.getPath()); // এই মেথডটি হেল্পারে থাকতে হবে
            
            // 4. Remove from List and Update UI
            adapter.removeItem(position);
            Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show();
        } else {
            // যদি ডিলিট না হয় (যেমন সিস্টেম ফাইল বা পারমিশন নেই)
             Toast.makeText(this.context, "Failed to delete. Try from File Manager.", Toast.LENGTH_SHORT).show();
        }
    }
}
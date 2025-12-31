package com.videoplayer;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private Context context;
    private ArrayList<String> folderList;
    private OnFolderClickListener listener;
    private VideoDatabaseHelper dbHelper;
    private int currentViewMode = SettingsManager.VIEW_LIST;
    private int lastPosition = -1;

    public interface OnFolderClickListener {
        void onFolderClick(String folderName);
    }

    public FolderAdapter(Context context, ArrayList<String> folderList, OnFolderClickListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
        
        this.dbHelper = new VideoDatabaseHelper(context);
        SettingsManager settingsManager = new SettingsManager(context);
        this.currentViewMode = settingsManager.getViewMode();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (currentViewMode == SettingsManager.VIEW_GRID) {
            // গ্রিড ভিউ (Icon + Name + Count)
            view = LayoutInflater.from(context).inflate(R.layout.item_folder_grid, parent, false);
        } else {
            // লিস্ট ভিউ (Detailed Card)
            view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        }
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        String folderName = folderList.get(position);

        holder.tvFolderName.setText(folderName);
        
        // --- ডাটাবেস থেকে পরিসংখ্যান বের করা ---
        long[] stats = dbHelper.getFolderStats(folderName);
        long count = stats[0];
        long sizeBytes = stats[1];

        // ভিডিও কাউন্ট সেট করা
        if (holder.tvVideoCount != null) {
            holder.tvVideoCount.setText(count + " Videos");
        }

        // লিস্ট ভিউ হলে বিস্তারিত দেখাবে
        if (currentViewMode == SettingsManager.VIEW_LIST) {
            // সাইজ
            if (holder.tvFolderSize != null) {
                holder.tvFolderSize.setText(Formatter.formatFileSize(context, sizeBytes));
            }
            
            // স্টোরেজ টাইপ (Internal/SD Card)
            // এটি চেক করার জন্য আমরা ওই ফোল্ডারের যেকোনো একটি ভিডিওর পাথ চেক করতে পারি
            // অথবা সহজ লজিক: ফোল্ডার পাথ যদি 'emulated' হয় তবে ইন্টারনাল
            // আপাতত আমরা "Internal" হার্ডকোড না করে ডাটাবেস থেকে পাথ এনে চেক করতে পারি
            // সিম্পলিসিটির জন্য:
            if (holder.tvStorageType != null) {
                 // এই লজিকটি পারফেক্ট করতে হলে ফোল্ডার লিস্টে পাথও রাখা লাগত
                 // আপাতত ডিফল্ট বা ডামি টেক্সট রাখা হচ্ছে, অথবা আপনি চাইলে dbHelper থেকে পাথ এনে চেক করতে পারেন
                 holder.tvStorageType.setText("Storage"); 
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folderName));
        
        setAnimation(holder.itemView, position);
    }
    
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            int animRes = (currentViewMode == SettingsManager.VIEW_GRID) ? android.R.anim.fade_in : android.R.anim.slide_in_left;
            Animation animation = AnimationUtils.loadAnimation(context, animRes);
            animation.setDuration(300);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName, tvVideoCount, tvFolderSize, tvStorageType;
        ImageView imgIcon;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            tvVideoCount = itemView.findViewById(R.id.tv_video_count);
            imgIcon = itemView.findViewById(R.id.img_folder_icon);
            
            // শুধুমাত্র লিস্ট ভিউতে এইগুলো আছে
            tvFolderSize = itemView.findViewById(R.id.tv_folder_size);
            tvStorageType = itemView.findViewById(R.id.tv_storage_type);
        }
    }
}
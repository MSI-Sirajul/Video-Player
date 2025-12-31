package com.videoplayer;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private ArrayList<VideoItem> videoList;
    private ArrayList<VideoItem> sourceList;
    private OnItemClickListener listener;
    private int currentViewMode;
    private int lastPosition = -1;
    
    // হাইলাইট করার জন্য ভেরিয়েবল
    private int currentPlayingPosition = -1; 

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public VideoAdapter(Context context, ArrayList<VideoItem> videoList, OnItemClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.sourceList = new ArrayList<>(videoList);
        this.listener = listener;
        SettingsManager settingsManager = new SettingsManager(context);
        this.currentViewMode = settingsManager.getViewMode();
    }
    
    // হাইলাইট পজিশন আপডেট করার মেথড
    public void setCurrentPlayingPosition(int position) {
        this.currentPlayingPosition = position;
        notifyDataSetChanged(); // UI রিফ্রেশ
    }

    public void updateViewMode(int viewMode) {
        this.currentViewMode = viewMode;
        notifyDataSetChanged();
    }
    
    public void updateList(ArrayList<VideoItem> newList) {
        this.videoList = new ArrayList<>(newList);
        this.sourceList = new ArrayList<>(newList); 
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < videoList.size()) {
            videoList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, videoList.size());
        }
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (currentViewMode == SettingsManager.VIEW_GRID) {
            view = LayoutInflater.from(context).inflate(R.layout.item_video_grid, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        }
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem video = videoList.get(position);

        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getDuration());

        // --- HIGHLIGHT LOGIC ---
        if (position == currentPlayingPosition) {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.colorAccent)); // Playing
        } else {
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.primary_text)); // Normal
        }
        // -----------------------

        if (holder.tvSize != null) {
            try {
                long sizeBytes = Long.parseLong(video.getSize());
                holder.tvSize.setText(Formatter.formatFileSize(context, sizeBytes));
            } catch (Exception e) {
                holder.tvSize.setText(video.getSize());
            }
        }

        if (holder.tvDate != null) {
            String dateString = DateFormat.format("dd MMM, yyyy", new Date(video.getDateAdded() * 1000)).toString();
            holder.tvDate.setText(dateString);
        }
        
        if (holder.tvFolderName != null) {
            holder.tvFolderName.setText(video.folderName);
        }

        Object imageSource;
        if (video.getThumbPath() != null && new File(video.getThumbPath()).exists()) {
            imageSource = video.getThumbPath();
        } else {
            imageSource = video.getPath();
        }

        Glide.with(context)
                .load(imageSource)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.color.secondary_bg)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> listener.onClick(position));

        if (holder.imgMenu != null) {
            holder.imgMenu.setOnClickListener(v -> {
                VideoMenuManager menuManager = new VideoMenuManager(context);
                menuManager.showVideoMenu(v, video, this, position, videoList);
            });
        }

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
        return videoList.size();
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    public void filter(String text) {
        videoList.clear();
        if (text.isEmpty()) {
            videoList.addAll(sourceList);
        } else {
            text = text.toLowerCase();
            for (VideoItem item : sourceList) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    videoList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail, imgMenu;
        TextView tvTitle, tvDuration, tvSize, tvDate, tvFolderName;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            imgMenu = itemView.findViewById(R.id.img_menu_more);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            
            tvSize = itemView.findViewById(R.id.tv_size);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvFolderName = itemView.findViewById(R.id.tv_folder_name_item);
        }
    }
}
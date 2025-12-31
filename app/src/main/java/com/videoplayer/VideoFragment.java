package com.videoplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private VideoAdapter videoAdapter;
    private ArrayList<VideoItem> videoList = new ArrayList<>();
    private VideoDatabaseHelper dbHelper;
    private SettingsManager settingsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_videos);
        tvEmpty = view.findViewById(R.id.tv_empty);
        
        dbHelper = new VideoDatabaseHelper(getContext());
        settingsManager = new SettingsManager(getContext());

        // প্রথমবার লোড হবে
        loadVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
        // FIX: এখানে loadVideos() কল করবেন না। এটি রিসেট করে দেয়।
        // MainActivity প্রয়োজন হলে রিফ্রেশ করবে।
    }

    public void loadVideos() {
        if (getContext() == null) return;

        // যদি অ্যাডাপ্টার আগে থেকেই থাকে এবং লিস্ট খালি না হয়, তবে শুধু ডাটা আপডেট করব
        // এটি স্ক্রল পজিশন ঠিক রাখতে সাহায্য করে
        
        int sortType = settingsManager.getSortType();
        videoList = dbHelper.getAllVideos(sortType);

        if (videoList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            int viewMode = settingsManager.getViewMode();
            
            // লেআউট ম্যানেজার সেট করা (যদি আগে না থাকে)
            if (recyclerView.getLayoutManager() == null) {
                if (viewMode == SettingsManager.VIEW_GRID) {
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }

            if (videoAdapter == null) {
                videoAdapter = new VideoAdapter(getContext(), videoList, (pos) -> {
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("position", pos);
                    PlayerActivity.videoList = videoList; 
                    startActivity(intent);
                });
                recyclerView.setAdapter(videoAdapter);
            } else {
                // শুধু ডাটা আপডেট, স্ক্রল পজিশন রিসেট হবে না
                videoAdapter.updateViewMode(viewMode);
                videoAdapter.updateList(videoList);
            }
        }
    }
    
    // নতুন মেথড: হাইলাইট আপডেট করার জন্য
    public void updateHighlight(String currentPath) {
        if (videoAdapter != null && videoList != null) {
            for (int i = 0; i < videoList.size(); i++) {
                if (videoList.get(i).getPath().equals(currentPath)) {
                    videoAdapter.setCurrentPlayingPosition(i);
                    // অপশনাল: যদি হাইলাইট করা ভিডিও সামনে আনতে চান
                    // recyclerView.scrollToPosition(i); 
                    break;
                }
            }
        }
    }
}
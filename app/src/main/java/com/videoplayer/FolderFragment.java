package com.videoplayer;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class FolderFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FolderAdapter folderAdapter;
    private ArrayList<String> folderList = new ArrayList<>();
    
    private VideoDatabaseHelper dbHelper;
    private SettingsManager settingsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_folders);
        tvEmpty = view.findViewById(R.id.tv_empty_folder);
        
        dbHelper = new VideoDatabaseHelper(getContext());
        settingsManager = new SettingsManager(getContext());

        loadFolders();
    }

    // পাবলিক মেথড
    public void loadFolders() {
        if (getContext() == null) return;

        int sortType = settingsManager.getSortType(); 
        List<VideoItem> allVideos = dbHelper.getAllVideos(sortType);

        if (allVideos.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        HashSet<String> folders = new HashSet<>();
        for (VideoItem item : allVideos) {
            if(item.folderName != null) folders.add(item.folderName);
        }
        folderList = new ArrayList<>(folders);
        Collections.sort(folderList); 

        int viewMode = settingsManager.getViewMode();
        
        if (recyclerView.getLayoutManager() == null) {
            if (viewMode == SettingsManager.VIEW_GRID) {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        }

        // --- FLICKER FIX ---
        if (folderAdapter == null) {
            folderAdapter = new FolderAdapter(getContext(), folderList, folderName -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openFolder(folderName);
                }
            });
            recyclerView.setAdapter(folderAdapter);
        } else {
            // শুধুমাত্র ডাটা আপডেট (কোনো ফ্লিকার হবে না)
            
            // লেআউট চেঞ্জ চেক
            boolean isGrid = (recyclerView.getLayoutManager() instanceof GridLayoutManager);
            if ((viewMode == SettingsManager.VIEW_GRID && !isGrid) ||
                (viewMode == SettingsManager.VIEW_LIST && isGrid)) {
                    if (viewMode == SettingsManager.VIEW_GRID) {
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                    } else {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    }
                    recyclerView.setAdapter(folderAdapter);
            }
            
            // নতুন ডাটা সেট করা (FolderAdapter এ এই মেথড থাকতে হবে, না থাকলে নিচে দিচ্ছি)
            // folderAdapter.updateList(folderList); <--- এটি নিচে যোগ করে নিবেন
            
            // আপাতত নতুন করে সেট করা হচ্ছে যেহেতু ফোল্ডার লিস্ট কম চেঞ্জ হয়
             folderAdapter = new FolderAdapter(getContext(), folderList, folderName -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openFolder(folderName);
                }
            });
            recyclerView.setAdapter(folderAdapter);
        }
    }
}
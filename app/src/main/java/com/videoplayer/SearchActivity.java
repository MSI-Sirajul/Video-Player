package com.videoplayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvNoResult;
    private ImageView btnBack;
    
    private VideoAdapter videoAdapter;
    private ArrayList<VideoItem> searchList = new ArrayList<>();
    private VideoDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Theme Apply (Optional if needed explicitly)
        // new SettingsManager(this).applyTheme(); // যদি বেস থিম কাজ না করে
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new VideoDatabaseHelper(this);
        initViews();
        
        // ওপেন হওয়ার সাথে সাথে কিবোর্ড ওপেন করা
        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search_field);
        recyclerView = findViewById(R.id.recycler_search);
        tvNoResult = findViewById(R.id.tv_no_result);
        btnBack = findViewById(R.id.btn_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Initial Empty Adapter
        videoAdapter = new VideoAdapter(this, searchList, pos -> {
            // প্লেয়ার চালু করা
            PlayerActivity.videoList = searchList; // সার্চ রেজাল্টকে প্লেলিস্ট হিসেবে পাঠানো
            Intent intent = new Intent(SearchActivity.this, PlayerActivity.class);
            intent.putExtra("position", pos);
            startActivity(intent);
        });
        recyclerView.setAdapter(videoAdapter);

        btnBack.setOnClickListener(v -> {
            hideKeyboard();
            finish();
        });

        // Search Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                } else {
                    searchList.clear();
                    videoAdapter.notifyDataSetChanged();
                    tvNoResult.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        searchList = dbHelper.searchVideos(query);
        
        if (searchList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvNoResult.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoResult.setVisibility(View.GONE);
            
            // অ্যাডাপ্টার আপডেট
            videoAdapter.updateList(searchList);
        }
    }
    
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyboard(); // অ্যাক্টিভিটি পজ হলে কিবোর্ড হাইড হবে
    }
}
package com.videoplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.viewpager2.widget.ViewPager2;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MainPagerAdapter pagerAdapter;
    private ImageView dot1, dot2;

    private TextView tvTitle;
    private EditText searchBar;
    private LottieAnimationView lottieLoading;
    
    // Mini Player Views
    private RelativeLayout miniPlayerLayout;
    private ImageView miniPlay, miniNext, miniPrev, miniClose, miniArt; 
    private TextView miniTitle;
    
    private VideoDatabaseHelper dbHelper;
    private SettingsManager settingsManager;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private PlaybackService playbackService;
    private boolean isBound = false;

    private static final String PERMISSION_READ_MEDIA_VIDEO = "android.permission.READ_MEDIA_VIDEO";
    private static final String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    // Player Listener
    private Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) { updateMiniPlayer(); }
        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason) { updateMiniPlayer(); }
        @Override
        public void onPlaybackStateChanged(int state) {
            if (state == Player.STATE_ENDED) miniPlayerLayout.setVisibility(View.GONE);
            updateMiniPlayer();
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlaybackService.LocalBinder binder = (PlaybackService.LocalBinder) service;
            playbackService = binder.getService();
            isBound = true;
            playbackService.player.addListener(playerListener);
            updateMiniPlayer();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) { isBound = false; }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        applyTheme(); // Apply Theme
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new VideoDatabaseHelper(this);
        initViews();
        checkPermissions();
        
        // Default Home Tab Set
        int defaultHome = settingsManager.getDefaultHome();
        viewPager.setCurrentItem(defaultHome, false);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlaybackService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isBound && playbackService != null) updateMiniPlayer();
        
        // SettingsActivity থেকে ফিরলে যাতে লিস্ট আপডেট হয় (ফিল্টার/সর্টিং অ্যাপ্লাই)
        // আমরা এখানে স্ক্যান চালাচ্ছি কারণ ফিল্টার চেঞ্জ হতে পারে
        scanStorageInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound && playbackService != null) {
            playbackService.player.removeListener(playerListener);
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        dot1 = findViewById(R.id.dot_1);
        dot2 = findViewById(R.id.dot_2);
        
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                if(position == 0) tvTitle.setText("All Videos");
                else tvTitle.setText("Folders");
            }
        });

        tvTitle = findViewById(R.id.tv_app_title);
        searchBar = findViewById(R.id.et_search);
        lottieLoading = findViewById(R.id.lottie_loading);
        
        miniPlayerLayout = findViewById(R.id.miniPlayerLayout);
        miniArt = findViewById(R.id.mini_art);
        miniPlay = findViewById(R.id.mini_play);
        miniNext = findViewById(R.id.mini_next);
        miniPrev = findViewById(R.id.mini_prev);
        miniClose = findViewById(R.id.mini_close); 
        miniTitle = findViewById(R.id.mini_title);

        setupMiniPlayerControls();

        ImageView btnSettings = findViewById(R.id.btn_settings);
        if(btnSettings != null) {
            // FIX: এখানে পুরাতন পপআপ মেথড সরিয়ে সরাসরি SettingsActivity ওপেন করা হচ্ছে
            btnSettings.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            });
        }

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: Implement Search broadcast to fragments
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
    
    private void updateDots(int position) {
        if (position == 0) {
            dot1.setImageResource(R.drawable.tab_indicator_selected);
            dot2.setImageResource(R.drawable.tab_indicator_default);
        } else {
            dot1.setImageResource(R.drawable.tab_indicator_default);
            dot2.setImageResource(R.drawable.tab_indicator_selected);
        }
    }

    // --- REFRESH FRAGMENTS ---
    private void refreshFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof VideoFragment) {
                ((VideoFragment) fragment).loadVideos();
            } else if (fragment instanceof FolderFragment) {
                ((FolderFragment) fragment).loadFolders();
            }
        }
    }
    
    // --- MINI PLAYER LOGIC ---
    private void setupMiniPlayerControls() {
        miniPlay.setOnClickListener(v -> {
            if(playbackService != null && playbackService.player != null) {
                if(playbackService.player.isPlaying()) playbackService.player.pause();
                else playbackService.player.play();
            }
        });
        miniNext.setOnClickListener(v -> {
            if(playbackService != null && playbackService.player != null && playbackService.player.hasNextMediaItem()) {
                playbackService.player.seekToNextMediaItem();
            }
        });
        miniPrev.setOnClickListener(v -> {
            if(playbackService != null && playbackService.player != null && playbackService.player.hasPreviousMediaItem()) {
                playbackService.player.seekToPreviousMediaItem();
            }
        });
        miniClose.setOnClickListener(v -> {
            if(playbackService != null) {
                playbackService.player.pause();
                playbackService.stopBackgroundPlay();
                playbackService.isBackgroundPlayEnabled = false; 
            }
            miniPlayerLayout.setVisibility(View.GONE);
        });
        miniPlayerLayout.setOnClickListener(v -> {
            if (playbackService != null && playbackService.player.getMediaItemCount() > 0) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("position", -1); 
                startActivity(intent);
            }
        });
    }   

    private void updateMiniPlayer() {
        if (playbackService != null && playbackService.player != null 
                && playbackService.player.getMediaItemCount() > 0
                && playbackService.isBackgroundPlayEnabled) { 
            
            miniPlayerLayout.setVisibility(View.VISIBLE);
            MediaItem item = playbackService.player.getCurrentMediaItem();
            if (item != null) {
                if (item.mediaMetadata.title != null) miniTitle.setText(item.mediaMetadata.title);
                if (item.mediaId != null) Glide.with(this).load(item.mediaId).centerCrop().placeholder(R.drawable.exo_icon_play).into(miniArt);
            }
            if (playbackService.player.isPlaying()) miniPlay.setImageResource(R.drawable.exo_icon_pause);
            else miniPlay.setImageResource(R.drawable.exo_icon_play);
        } else {
            miniPlayerLayout.setVisibility(View.GONE);
        }
    }  
     
    // --- DATA LOADING & PERMISSIONS ---
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_MEDIA_VIDEO}, 101);
            } else {
                startAppLogic();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_EXTERNAL_STORAGE}, 101);
            } else {
                startAppLogic();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAppLogic();
        } else {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAppLogic() {
        if (dbHelper.hasData()) {
            showLoading(false);
            scanStorageInBackground();
        } else {
            showLoading(true);
            scanStorageInBackground();
        }
    }

    private void scanStorageInBackground() {
        executorService.execute(() -> {
            ArrayList<VideoItem> scannedVideos = new ArrayList<>();
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED
            };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
            
            // --- SETTINGS VALUES LOAD ---
            boolean showHidden = settingsManager.getShowHidden();
            boolean hideShort = settingsManager.getHideShortVideos();
            
            boolean enableInternal = settingsManager.getEnableInternal();
            boolean enableSdCard = settingsManager.getEnableSdCard();
            
            boolean filterAll = settingsManager.getFilterAll();
            boolean filterMp4 = settingsManager.getFilterMp4();
            boolean filterMkv = settingsManager.getFilterMkv();
            boolean filter3gp = settingsManager.getFilter3gp();
            boolean filterAvi = settingsManager.getFilterAvi();

            // ইন্টারনাল স্টোরেজ রুট পাথ
            String internalRoot = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    String title = cursor.getString(1);
                    String path = cursor.getString(2);
                    String durationStr = cursor.getString(3);
                    String folder = cursor.getString(4);
                    String size = cursor.getString(5);
                    long date = cursor.getLong(6);
                    
                    if (path != null && new File(path).exists()) {
                        File file = new File(path);

                        // --- 1. STORAGE FILTER ---
                        boolean isInternalFile = path.startsWith(internalRoot);
                        
                        if (isInternalFile) {
                            // ইন্টারনাল ফাইল কিন্তু টগল অফ -> বাদ দাও
                            if (!enableInternal) continue;
                        } else {
                            // এসডি কার্ড ফাইল কিন্তু টগল অফ -> বাদ দাও
                            if (!enableSdCard) continue;
                        }

                        // --- 2. HIDDEN FILE FILTER ---
                        // ফাইল ডট দিয়ে শুরু হলে সেটি হিডেন
                        boolean isHidden = file.getName().startsWith(".");
                        // যদি 'Show Hidden' অফ থাকে এবং ফাইলটি হিডেন হয় -> বাদ দাও
                        if (!showHidden && isHidden) {
                            continue;
                        }

                        // --- 3. DURATION FILTER ---
                        long durationMillis = durationStr != null ? Long.parseLong(durationStr) : 0;
                        // যদি 'Hide Short' অন থাকে এবং ভিডিও ৩০ সেকেন্ড কম হয় -> বাদ দাও
                        if (hideShort && durationMillis < 30000) {
                            continue;
                        }

                        // --- 4. EXTENSION FILTER ---
                        if (!filterAll) { 
                            // যদি 'All Video' অফ থাকে, তখন চেক করব স্পেসিফিক ফরম্যাট
                            String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                            boolean keep = false;

                            if (filterMp4 && ext.equals("mp4")) keep = true;
                            if (filterMkv && ext.equals("mkv")) keep = true;
                            if (filter3gp && ext.equals("3gp")) keep = true;
                            if (filterAvi && ext.equals("avi")) keep = true;
                            
                            // যদি কোনো চেকবক্সের সাথেই ম্যাচ না করে -> বাদ দাও
                            if (!keep) continue;
                        }

                        // সব ফিল্টার পাস করলে লিস্টে অ্যাড হবে
                        String formattedDuration = formatTime(durationMillis);
                        scannedVideos.add(new VideoItem(id, title, path, formattedDuration, folder, null, size, date));
                    }
                }
                cursor.close();
            }

            // ডাটাবেস আপডেট (শূন্য লিস্ট হলেও আপডেট হবে, যাতে আগের ডাটা মুছে যায়)
            dbHelper.addVideos(scannedVideos);

            runOnUiThread(() -> {
                showLoading(false);
                refreshFragments(); 
            });
        });
    }
    public void openFolder(String folderName) {
        Intent intent = new Intent(MainActivity.this, FolderActivity.class);
        intent.putExtra("folderName", folderName);
        startActivity(intent);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            lottieLoading.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
        } else {
            lottieLoading.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        }
    }
    
    private void applyTheme() {
        int theme = settingsManager.getAppTheme();
        if (theme == SettingsManager.THEME_DARK) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (theme == SettingsManager.THEME_LIGHT) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
    }

    @Override
    public void onBackPressed() {
        if (viewPager != null && viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0); 
        } else {
            super.onBackPressed();
        }
    }
}
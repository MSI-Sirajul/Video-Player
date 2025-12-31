package com.videoplayer;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SettingsManager settingsManager;

    // Toggles
    private SwitchCompat switchSortName, switchSortDate, switchSortSize, switchSortDuration;
    private SwitchCompat switchViewList, switchViewGrid;
    private SwitchCompat switchHomeAll, switchHomeFolder;
    private SwitchCompat switchShowHidden, switchHideShort;
    private SwitchCompat switchFilterAll, switchFilterMp4, switchFilterMkv, switchFilter3gp, switchFilterAvi;
    private SwitchCompat switchStorageInternal, switchStorageSd;
    private SwitchCompat switchThemeAuto, switchThemeDark, switchThemeLight;

    // Description TextViews
    private TextView descSortName, descSortDate, descSortSize, descSortDuration;
    private TextView descViewList, descViewGrid;
    private TextView descHomeAll, descHomeFolder;
    private TextView descShowHidden, descHideShort;
    private TextView descFilterAll, descFilterMp4, descFilterMkv, descFilter3gp, descFilterAvi;
    private TextView descStorageInternal, descStorageSd;
    private TextView descThemeAuto, descThemeDark, descThemeLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsManager = new SettingsManager(this);
        applyAppTheme(); // Apply theme before content view
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSavedSettings();
        setupListeners();
    }

    private void initViews() {
        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // --- SORT BY ---
        switchSortName = findViewById(R.id.switch_sort_name);
        descSortName = findViewById(R.id.desc_sort_name);
        
        switchSortDate = findViewById(R.id.switch_sort_date);
        descSortDate = findViewById(R.id.desc_sort_date);
        
        switchSortSize = findViewById(R.id.switch_sort_size);
        descSortSize = findViewById(R.id.desc_sort_size);
        
        switchSortDuration = findViewById(R.id.switch_sort_duration);
        descSortDuration = findViewById(R.id.desc_sort_duration);

        // --- VIEW MODE ---
        switchViewList = findViewById(R.id.switch_view_list);
        descViewList = findViewById(R.id.desc_view_list);
        
        switchViewGrid = findViewById(R.id.switch_view_grid);
        descViewGrid = findViewById(R.id.desc_view_grid);

        // --- HOME PAGE ---
        switchHomeAll = findViewById(R.id.switch_home_all);
        descHomeAll = findViewById(R.id.desc_home_all);
        
        switchHomeFolder = findViewById(R.id.switch_home_folder);
        descHomeFolder = findViewById(R.id.desc_home_folder);

        // --- HIDE/SHOW ---
        switchShowHidden = findViewById(R.id.switch_show_hidden);
        descShowHidden = findViewById(R.id.desc_show_hidden);
        
        switchHideShort = findViewById(R.id.switch_hide_short);
        descHideShort = findViewById(R.id.desc_hide_short);

        // --- FILTERS ---
        switchFilterAll = findViewById(R.id.switch_filter_all);
        descFilterAll = findViewById(R.id.desc_filter_all);
        
        switchFilterMp4 = findViewById(R.id.switch_filter_mp4);
        descFilterMp4 = findViewById(R.id.desc_filter_mp4);
        
        switchFilterMkv = findViewById(R.id.switch_filter_mkv);
        descFilterMkv = findViewById(R.id.desc_filter_mkv);
        
        switchFilter3gp = findViewById(R.id.switch_filter_3gp);
        descFilter3gp = findViewById(R.id.desc_filter_3gp);
        
        switchFilterAvi = findViewById(R.id.switch_filter_avi);
        descFilterAvi = findViewById(R.id.desc_filter_avi);

        // --- STORAGE ---
        switchStorageInternal = findViewById(R.id.switch_storage_internal);
        descStorageInternal = findViewById(R.id.desc_storage_internal);
        
        switchStorageSd = findViewById(R.id.switch_storage_sd);
        descStorageSd = findViewById(R.id.desc_storage_sd);

        // --- THEME ---
        switchThemeAuto = findViewById(R.id.switch_theme_auto);
        descThemeAuto = findViewById(R.id.desc_theme_auto);
        
        switchThemeDark = findViewById(R.id.switch_theme_dark);
        descThemeDark = findViewById(R.id.desc_theme_dark);
        
        switchThemeLight = findViewById(R.id.switch_theme_light);
        descThemeLight = findViewById(R.id.desc_theme_light);

        // --- OTHERS ---
        findViewById(R.id.btn_share_app).setOnClickListener(v -> shareApp());
        findViewById(R.id.btn_app_info).setOnClickListener(v -> showAppInfoDialog());
    }

    private void loadSavedSettings() {
        // 1. Sort
        int sort = settingsManager.getSortType();
        updateSortUI(sort);

        // 2. View Mode
        int view = settingsManager.getViewMode();
        updateViewUI(view);

        // 3. Home
        int home = settingsManager.getDefaultHome();
        updateHomeUI(home);

        // 4. Hide/Show
        boolean hidden = settingsManager.getShowHidden();
        switchShowHidden.setChecked(hidden);
        updateDesc(descShowHidden, hidden, R.string.desc_on_show_hidden, R.string.desc_off_show_hidden);

        boolean shortVideo = settingsManager.getHideShortVideos();
        switchHideShort.setChecked(shortVideo);
        updateDesc(descHideShort, shortVideo, R.string.desc_on_hide_short, R.string.desc_off_hide_short);

        // 5. Filters
        updateFilterUI();

        // 6. Storage
        boolean internal = settingsManager.getEnableInternal();
        switchStorageInternal.setChecked(internal);
        updateDesc(descStorageInternal, internal, R.string.desc_on_storage_internal, R.string.desc_off_storage_internal);

        boolean sd = settingsManager.getEnableSdCard();
        switchStorageSd.setChecked(sd);
        updateDesc(descStorageSd, sd, R.string.desc_on_storage_sd, R.string.desc_off_storage_sd);

        // 7. Theme
        int theme = settingsManager.getAppTheme();
        updateThemeUI(theme);
    }

    private void setupListeners() {
        // --- Sort Listeners ---
        View.OnClickListener sortListener = v -> {
            int sortType = SettingsManager.SORT_DATE_NEW;
            if (v.getId() == R.id.switch_sort_name) sortType = SettingsManager.SORT_NAME_AZ;
            else if (v.getId() == R.id.switch_sort_size) sortType = SettingsManager.SORT_SIZE_LARGE;
            else if (v.getId() == R.id.switch_sort_duration) sortType = SettingsManager.SORT_DURATION;
            
            settingsManager.setSortType(sortType);
            updateSortUI(sortType);
        };
        switchSortName.setOnClickListener(sortListener);
        switchSortDate.setOnClickListener(sortListener);
        switchSortSize.setOnClickListener(sortListener);
        switchSortDuration.setOnClickListener(sortListener);

        // --- View Mode Listeners (UPDATED WITH RESTART) ---
        switchViewList.setOnClickListener(v -> {
            // ডাটা সেভ এবং অ্যাপ রিস্টার্ট
            settingsManager.setViewMode(SettingsManager.VIEW_LIST);
            restartApp();
        });

        switchViewGrid.setOnClickListener(v -> {
            // ডাটা সেভ এবং অ্যাপ রিস্টার্ট
            settingsManager.setViewMode(SettingsManager.VIEW_GRID);
            restartApp();
        });

        // --- Home Page Listeners (UPDATED WITH RESTART) ---
        switchHomeAll.setOnClickListener(v -> {
            // ডাটা সেভ এবং অ্যাপ রিস্টার্ট
            settingsManager.setDefaultHome(0);
            restartApp();
        });

        switchHomeFolder.setOnClickListener(v -> {
            // ডাটা সেভ এবং অ্যাপ রিস্টার্ট
            settingsManager.setDefaultHome(1);
            restartApp();
        });

        // --- Hide/Show Listeners ---
        switchShowHidden.setOnClickListener(v -> {
            boolean isChecked = switchShowHidden.isChecked();
            settingsManager.setShowHidden(isChecked);
            updateDesc(descShowHidden, isChecked, R.string.desc_on_show_hidden, R.string.desc_off_show_hidden);
        });

        switchHideShort.setOnClickListener(v -> {
            boolean isChecked = switchHideShort.isChecked();
            settingsManager.setHideShortVideos(isChecked);
            updateDesc(descHideShort, isChecked, R.string.desc_on_hide_short, R.string.desc_off_hide_short);
        });

        // --- Filter Listeners ---
        switchFilterAll.setOnClickListener(v -> {
            if (switchFilterAll.isChecked()) {
                switchFilterMp4.setChecked(false);
                switchFilterMkv.setChecked(false);
                switchFilter3gp.setChecked(false);
                switchFilterAvi.setChecked(false);
            }
            saveAndRefreshFilters();
        });

        View.OnClickListener extListener = v -> {
            if (((SwitchCompat)v).isChecked()) {
                switchFilterAll.setChecked(false);
            } else {
                if (!switchFilterMp4.isChecked() && !switchFilterMkv.isChecked() && 
                    !switchFilter3gp.isChecked() && !switchFilterAvi.isChecked()) {
                    switchFilterAll.setChecked(true);
                }
            }
            saveAndRefreshFilters();
        };
        switchFilterMp4.setOnClickListener(extListener);
        switchFilterMkv.setOnClickListener(extListener);
        switchFilter3gp.setOnClickListener(extListener);
        switchFilterAvi.setOnClickListener(extListener);

        // --- Storage Listeners ---
        switchStorageInternal.setOnClickListener(v -> {
            boolean isChecked = switchStorageInternal.isChecked();
            settingsManager.setEnableInternal(isChecked);
            updateDesc(descStorageInternal, isChecked, R.string.desc_on_storage_internal, R.string.desc_off_storage_internal);
        });

        switchStorageSd.setOnClickListener(v -> {
            boolean isChecked = switchStorageSd.isChecked();
            settingsManager.setEnableSdCard(isChecked);
            updateDesc(descStorageSd, isChecked, R.string.desc_on_storage_sd, R.string.desc_off_storage_sd);
        });

        // --- Theme Listeners ---
        View.OnClickListener themeListener = v -> {
            int theme = SettingsManager.THEME_AUTO;
            if (v.getId() == R.id.switch_theme_dark) theme = SettingsManager.THEME_DARK;
            else if (v.getId() == R.id.switch_theme_light) theme = SettingsManager.THEME_LIGHT;

            settingsManager.setAppTheme(theme);
            updateThemeUI(theme);
            
            // থিম পরিবর্তন হলে অ্যাক্টিভিটি রিক্রিয়েট করতে হয়
            recreate();
        };
        switchThemeAuto.setOnClickListener(themeListener);
        switchThemeDark.setOnClickListener(themeListener);
        switchThemeLight.setOnClickListener(themeListener);
    }
    
    private void updateDesc(TextView tv, boolean isOn, int resOn, int resOff) {
        tv.setText(isOn ? resOn : resOff);
        tv.setTextColor(isOn ? getColor(R.color.colorAccent) : getColor(R.color.secondary_text));
    }

    private void updateSortUI(int sort) {
        boolean isName = (sort == SettingsManager.SORT_NAME_AZ);
        boolean isDate = (sort == SettingsManager.SORT_DATE_NEW);
        boolean isSize = (sort == SettingsManager.SORT_SIZE_LARGE);
        boolean isDur = (sort == SettingsManager.SORT_DURATION);

        switchSortName.setChecked(isName);
        updateDesc(descSortName, isName, R.string.desc_on_sort_name, R.string.desc_off_sort_name);

        switchSortDate.setChecked(isDate);
        updateDesc(descSortDate, isDate, R.string.desc_on_sort_date, R.string.desc_off_sort_date);

        switchSortSize.setChecked(isSize);
        updateDesc(descSortSize, isSize, R.string.desc_on_sort_size, R.string.desc_off_sort_size);

        switchSortDuration.setChecked(isDur);
        updateDesc(descSortDuration, isDur, R.string.desc_on_sort_duration, R.string.desc_off_sort_duration);
    }

    private void updateViewUI(int view) {
        boolean isList = (view == SettingsManager.VIEW_LIST);
        switchViewList.setChecked(isList);
        updateDesc(descViewList, isList, R.string.desc_on_view_list, R.string.desc_off_view_list);

        switchViewGrid.setChecked(!isList);
        updateDesc(descViewGrid, !isList, R.string.desc_on_view_grid, R.string.desc_off_view_grid);
    }

    private void updateHomeUI(int home) {
        boolean isAll = (home == 0);
        switchHomeAll.setChecked(isAll);
        updateDesc(descHomeAll, isAll, R.string.desc_on_home_all, R.string.desc_off_home_all);

        switchHomeFolder.setChecked(!isAll);
        updateDesc(descHomeFolder, !isAll, R.string.desc_on_home_folder, R.string.desc_off_home_folder);
    }

    private void updateThemeUI(int theme) {
        boolean isAuto = (theme == SettingsManager.THEME_AUTO);
        boolean isDark = (theme == SettingsManager.THEME_DARK);
        boolean isLight = (theme == SettingsManager.THEME_LIGHT);

        switchThemeAuto.setChecked(isAuto);
        updateDesc(descThemeAuto, isAuto, R.string.desc_on_theme_auto, R.string.desc_off_theme_auto);

        switchThemeDark.setChecked(isDark);
        updateDesc(descThemeDark, isDark, R.string.desc_on_theme_dark, R.string.desc_off_theme_dark);

        switchThemeLight.setChecked(isLight);
        updateDesc(descThemeLight, isLight, R.string.desc_on_theme_light, R.string.desc_off_theme_light);
    }

    private void saveAndRefreshFilters() {
        settingsManager.setFilterAll(switchFilterAll.isChecked());
        settingsManager.setFilterMp4(switchFilterMp4.isChecked());
        settingsManager.setFilterMkv(switchFilterMkv.isChecked());
        settingsManager.setFilter3gp(switchFilter3gp.isChecked());
        settingsManager.setFilterAvi(switchFilterAvi.isChecked());
        updateFilterUI();
    }

    private void updateFilterUI() {
        boolean all = settingsManager.getFilterAll();
        switchFilterAll.setChecked(all);
        updateDesc(descFilterAll, all, R.string.desc_on_filter_all, R.string.desc_off_filter_all);

        boolean mp4 = settingsManager.getFilterMp4();
        switchFilterMp4.setChecked(mp4);
        updateDesc(descFilterMp4, mp4, R.string.desc_on_filter_mp4, R.string.desc_off_filter_mp4);

        boolean mkv = settingsManager.getFilterMkv();
        switchFilterMkv.setChecked(mkv);
        updateDesc(descFilterMkv, mkv, R.string.desc_on_filter_mkv, R.string.desc_off_filter_mkv);

        boolean gp3 = settingsManager.getFilter3gp();
        switchFilter3gp.setChecked(gp3);
        updateDesc(descFilter3gp, gp3, R.string.desc_on_filter_3gp, R.string.desc_off_filter_3gp);

        boolean avi = settingsManager.getFilterAvi();
        switchFilterAvi.setChecked(avi);
        updateDesc(descFilterAvi, avi, R.string.desc_on_filter_avi, R.string.desc_off_filter_avi);
    }

    // --- OTHER ACTIONS ---
    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareBody = "Check out this Video Player: https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, "Video Player");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void showAppInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_info);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvVersion = dialog.findViewById(R.id.tv_version);
        TextView tvDevice = dialog.findViewById(R.id.tv_device);
        TextView tvDeveloper = dialog.findViewById(R.id.tv_developer);
        TextView tvLink = dialog.findViewById(R.id.tv_github_link);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("Version: " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Version: Unknown");
        }

        tvDevice.setText(Build.BRAND.toUpperCase() + " : " + Build.MODEL);

        String devText = "Developer: <font color='#4CAF50'>MSI-Sirajul</font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvDeveloper.setText(Html.fromHtml(devText, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvDeveloper.setText(Html.fromHtml(devText));
        }

        tvLink.setOnClickListener(v -> {
            String url = "https://github.com/MSI-Sirajul/Video-Player/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        
        dialog.findViewById(R.id.btn_close_info).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void applyAppTheme() {
        int theme = settingsManager.getAppTheme();
        if (theme == SettingsManager.THEME_DARK) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (theme == SettingsManager.THEME_LIGHT) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
    private void restartApp() {
        // অ্যাপ রিস্টার্ট লজিক
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // শুধুমাত্র অ্যাক্টিভিটি ফিনিশ করুন, প্রসেস কিল করবেন না
    }
    
}
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
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // থিম অ্যাপ্লাই (ভিউ তৈরির আগে)
        settingsManager = new SettingsManager(this);
        applyAppTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSavedSettings();
        setupListeners();
    }

    private void initViews() {
        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Sort
        switchSortName = findViewById(R.id.switch_sort_name);
        switchSortDate = findViewById(R.id.switch_sort_date);
        switchSortSize = findViewById(R.id.switch_sort_size);
        switchSortDuration = findViewById(R.id.switch_sort_duration);

        // View Mode
        switchViewList = findViewById(R.id.switch_view_list);
        switchViewGrid = findViewById(R.id.switch_view_grid);

        // Home
        switchHomeAll = findViewById(R.id.switch_home_all);
        switchHomeFolder = findViewById(R.id.switch_home_folder);

        // Hide/Show
        switchShowHidden = findViewById(R.id.switch_show_hidden);
        switchHideShort = findViewById(R.id.switch_hide_short);

        // Filter
        switchFilterAll = findViewById(R.id.switch_filter_all);
        switchFilterMp4 = findViewById(R.id.switch_filter_mp4);
        switchFilterMkv = findViewById(R.id.switch_filter_mkv);
        switchFilter3gp = findViewById(R.id.switch_filter_3gp);
        switchFilterAvi = findViewById(R.id.switch_filter_avi);

        // Storage
        switchStorageInternal = findViewById(R.id.switch_storage_internal);
        switchStorageSd = findViewById(R.id.switch_storage_sd);

        // Theme
        switchThemeAuto = findViewById(R.id.switch_theme_auto);
        switchThemeDark = findViewById(R.id.switch_theme_dark);
        switchThemeLight = findViewById(R.id.switch_theme_light);

        // Share & Info Click
        findViewById(R.id.btn_share_app).setOnClickListener(v -> shareApp());
        findViewById(R.id.btn_app_info).setOnClickListener(v -> showAppInfoDialog());
    }

    private void loadSavedSettings() {
        // 1. Sort
        int sort = settingsManager.getSortType();
        switchSortName.setChecked(sort == SettingsManager.SORT_NAME_AZ || sort == SettingsManager.SORT_NAME_ZA);
        switchSortDate.setChecked(sort == SettingsManager.SORT_DATE_NEW || sort == SettingsManager.SORT_DATE_OLD);
        switchSortSize.setChecked(sort == SettingsManager.SORT_SIZE_LARGE || sort == SettingsManager.SORT_SIZE_SMALL);
        switchSortDuration.setChecked(sort == SettingsManager.SORT_DURATION);

        // 2. View Mode
        int view = settingsManager.getViewMode();
        switchViewList.setChecked(view == SettingsManager.VIEW_LIST);
        switchViewGrid.setChecked(view == SettingsManager.VIEW_GRID);

        // 3. Home
        int home = settingsManager.getDefaultHome();
        switchHomeAll.setChecked(home == 0);
        switchHomeFolder.setChecked(home == 1);

        // 4. Hide/Show
        switchShowHidden.setChecked(settingsManager.getShowHidden());
        switchHideShort.setChecked(settingsManager.getHideShortVideos());

        // 5. Filters
        switchFilterAll.setChecked(settingsManager.getFilterAll());
        switchFilterMp4.setChecked(settingsManager.getFilterMp4());
        switchFilterMkv.setChecked(settingsManager.getFilterMkv());
        switchFilter3gp.setChecked(settingsManager.getFilter3gp());
        switchFilterAvi.setChecked(settingsManager.getFilterAvi());

        // 6. Storage
        switchStorageInternal.setChecked(settingsManager.getEnableInternal());
        switchStorageSd.setChecked(settingsManager.getEnableSdCard());

        // 7. Theme
        int theme = settingsManager.getAppTheme();
        switchThemeAuto.setChecked(theme == SettingsManager.THEME_AUTO);
        switchThemeDark.setChecked(theme == SettingsManager.THEME_DARK);
        switchThemeLight.setChecked(theme == SettingsManager.THEME_LIGHT);
    }

    private void setupListeners() {
        // --- Sort Listeners ---
        View.OnClickListener sortListener = v -> {
            switchSortName.setChecked(v.getId() == R.id.switch_sort_name);
            switchSortDate.setChecked(v.getId() == R.id.switch_sort_date);
            switchSortSize.setChecked(v.getId() == R.id.switch_sort_size);
            switchSortDuration.setChecked(v.getId() == R.id.switch_sort_duration);

            int sortType = SettingsManager.SORT_DATE_NEW;
            if (v.getId() == R.id.switch_sort_name) sortType = SettingsManager.SORT_NAME_AZ;
            else if (v.getId() == R.id.switch_sort_size) sortType = SettingsManager.SORT_SIZE_LARGE;
            else if (v.getId() == R.id.switch_sort_duration) sortType = SettingsManager.SORT_DURATION;
            
            settingsManager.setSortType(sortType);
        };
        switchSortName.setOnClickListener(sortListener);
        switchSortDate.setOnClickListener(sortListener);
        switchSortSize.setOnClickListener(sortListener);
        switchSortDuration.setOnClickListener(sortListener);

        // --- View Mode Listeners ---
        switchViewList.setOnClickListener(v -> {
            switchViewList.setChecked(true);
            switchViewGrid.setChecked(false);
            settingsManager.setViewMode(SettingsManager.VIEW_LIST);
        });
        switchViewGrid.setOnClickListener(v -> {
            switchViewGrid.setChecked(true);
            switchViewList.setChecked(false);
            settingsManager.setViewMode(SettingsManager.VIEW_GRID);
        });

        // --- Home Page Listeners ---
        switchHomeAll.setOnClickListener(v -> {
            switchHomeAll.setChecked(true);
            switchHomeFolder.setChecked(false);
            settingsManager.setDefaultHome(0);
        });
        switchHomeFolder.setOnClickListener(v -> {
            switchHomeFolder.setChecked(true);
            switchHomeAll.setChecked(false);
            settingsManager.setDefaultHome(1);
        });

        // --- Hide/Show Listeners ---
        switchShowHidden.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.setShowHidden(isChecked));
        switchHideShort.setOnCheckedChangeListener((buttonView, isChecked) -> settingsManager.setHideShortVideos(isChecked));

        // --- FILTER LISTENERS (LOGIC FIXED) ---
        
        // 1. All Video Toggle Logic
        switchFilterAll.setOnClickListener(v -> {
            if (switchFilterAll.isChecked()) {
                // কন্ডিশন: All Video অন করলে বাকি সব অফ হয়ে যাবে
                switchFilterMp4.setChecked(false);
                switchFilterMkv.setChecked(false);
                switchFilter3gp.setChecked(false);
                switchFilterAvi.setChecked(false);
            }
            saveFilters();
        });

        // 2. Specific Extension Toggles Logic
        View.OnClickListener extListener = v -> {
            SwitchCompat s = (SwitchCompat) v;
            if (s.isChecked()) {
                // কন্ডিশন: যেকোনো স্পেসিফিক ফরম্যাট অন করলে All Video অফ হবে
                switchFilterAll.setChecked(false);
            }
            // বাকিরা একসাথে অন থাকতে পারে, তাই অন্য কিছু অফ করার দরকার নেই
            saveFilters();
        };

        switchFilterMp4.setOnClickListener(extListener);
        switchFilterMkv.setOnClickListener(extListener);
        switchFilter3gp.setOnClickListener(extListener);
        switchFilterAvi.setOnClickListener(extListener);

        // --- Storage Listeners ---
        switchStorageInternal.setOnCheckedChangeListener((v, c) -> settingsManager.setEnableInternal(c));
        switchStorageSd.setOnCheckedChangeListener((v, c) -> settingsManager.setEnableSdCard(c));

        // --- Theme Listeners ---
        View.OnClickListener themeListener = v -> {
            switchThemeAuto.setChecked(v.getId() == R.id.switch_theme_auto);
            switchThemeDark.setChecked(v.getId() == R.id.switch_theme_dark);
            switchThemeLight.setChecked(v.getId() == R.id.switch_theme_light);

            int theme = SettingsManager.THEME_AUTO;
            if (v.getId() == R.id.switch_theme_dark) theme = SettingsManager.THEME_DARK;
            else if (v.getId() == R.id.switch_theme_light) theme = SettingsManager.THEME_LIGHT;

            settingsManager.setAppTheme(theme);
            // রি-ক্রিয়েট করলে থিম অ্যাপ্লাই হবে
            recreate(); 
        };
        switchThemeAuto.setOnClickListener(themeListener);
        switchThemeDark.setOnClickListener(themeListener);
        switchThemeLight.setOnClickListener(themeListener);
    }

    private void saveFilters() {
        settingsManager.setFilterAll(switchFilterAll.isChecked());
        settingsManager.setFilterMp4(switchFilterMp4.isChecked());
        settingsManager.setFilterMkv(switchFilterMkv.isChecked());
        settingsManager.setFilter3gp(switchFilter3gp.isChecked());
        settingsManager.setFilterAvi(switchFilterAvi.isChecked());
    }
    

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String shareBody = "Check out this Video Player: https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, "Video Player");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    // --- APP INFO DIALOG ---
    private void showAppInfoDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_info);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvVersion = dialog.findViewById(R.id.tv_version);
        TextView tvDevice = dialog.findViewById(R.id.tv_device);
        TextView tvDeveloper = dialog.findViewById(R.id.tv_developer);
        TextView tvLink = dialog.findViewById(R.id.tv_github_link);

        // Version Info
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("Version: " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Version: Unknown");
        }

        // Device Info
        String device = Build.BRAND.toUpperCase() + " : " + Build.MODEL;
        tvDevice.setText(device);

        // Developer Name with Color (Green)
        String devText = "Developer: <font color='#4CAF50'>MSI-Sirajul</font>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvDeveloper.setText(Html.fromHtml(devText, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tvDeveloper.setText(Html.fromHtml(devText));
        }

        // GitHub Link
        tvLink.setOnClickListener(v -> {
            String url = "https://github.com/MSI-Sirajul/Video-Player-Pro/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        dialog.show();
    }

    private void applyAppTheme() {
        int theme = settingsManager.getAppTheme();
        if (theme == SettingsManager.THEME_DARK) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else if (theme == SettingsManager.THEME_LIGHT) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
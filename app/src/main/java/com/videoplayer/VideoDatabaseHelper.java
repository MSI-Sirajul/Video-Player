package com.videoplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class VideoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "VideoPlayer.db";
    private static final int DATABASE_VERSION = 2; // Version Increased for Schema Change

    private static final String TABLE_NAME = "videos";
    private static final String COL_ID = "id";
    private static final String COL_VIDEO_ID = "video_id";
    private static final String COL_TITLE = "title";
    private static final String COL_PATH = "path";
    private static final String COL_DURATION = "duration";
    private static final String COL_FOLDER = "folder_name";
    private static final String COL_THUMB = "thumb_path";
    // New Columns
    private static final String COL_SIZE = "size";
    private static final String COL_DATE = "date_added";

    public VideoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_VIDEO_ID + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_PATH + " TEXT, " +
                COL_DURATION + " TEXT, " +
                COL_FOLDER + " TEXT, " +
                COL_THUMB + " TEXT, " +
                COL_SIZE + " TEXT, " +
                COL_DATE + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // সহজ আপগ্রেড: টেবিল ড্রপ করে নতুন করে তৈরি করা (যেহেতু ডাটা স্ক্যান করে আসে)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addVideos(List<VideoItem> videoList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_NAME, null, null);

            for (VideoItem video : videoList) {
                ContentValues values = new ContentValues();
                values.put(COL_VIDEO_ID, video.id);
                values.put(COL_TITLE, video.getTitle());
                values.put(COL_PATH, video.getPath());
                values.put(COL_DURATION, video.getDuration());
                values.put(COL_FOLDER, video.folderName);
                values.put(COL_THUMB, video.getThumbPath());
                values.put(COL_SIZE, video.getSize());
                values.put(COL_DATE, video.getDateAdded());

                db.insert(TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public ArrayList<VideoItem> getAllVideos(int sortType) {
        ArrayList<VideoItem> videoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String orderBy;
        switch (sortType) {
            case SettingsManager.SORT_NAME_AZ:
                orderBy = COL_TITLE + " ASC";
                break;
            case SettingsManager.SORT_NAME_ZA:
                orderBy = COL_TITLE + " DESC";
                break;
            case SettingsManager.SORT_DATE_NEW:
                orderBy = COL_DATE + " DESC";
                break;
            case SettingsManager.SORT_DATE_OLD:
                orderBy = COL_DATE + " ASC";
                break;
            case SettingsManager.SORT_SIZE_LARGE:
                orderBy = "CAST(" + COL_SIZE + " AS INTEGER) DESC";
                break;
            case SettingsManager.SORT_SIZE_SMALL:
                orderBy = "CAST(" + COL_SIZE + " AS INTEGER) ASC";
                break;
            case SettingsManager.SORT_DURATION:
                orderBy = COL_DURATION + " DESC"; 
                break;
            default:
                orderBy = COL_DATE + " DESC";
        }

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + orderBy, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_VIDEO_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(COL_DURATION));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOLDER));
                String thumb = cursor.getString(cursor.getColumnIndexOrThrow(COL_THUMB));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE));

                videoList.add(new VideoItem(id, title, path, duration, folder, thumb, size, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return videoList;
    }
    // --- DELETE METHOD ---
    public void deleteVideo(String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COL_PATH + "=?", new String[]{path});
        db.close();
    }

    // 2. নতুন মেথড: সার্চ করার জন্য (এটি getAllVideos এর নিচে বসান)
    public ArrayList<VideoItem> searchVideos(String query) {
        ArrayList<VideoItem> videoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        // টাইটেল এর মধ্যে কি-ওয়ার্ড খুঁজবে
        String selection = COL_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};
        
        // সার্চ রেজাল্ট নতুন ভিডিও আগে দেখাবে
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COL_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_VIDEO_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(COL_DURATION));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOLDER));
                String thumb = cursor.getString(cursor.getColumnIndexOrThrow(COL_THUMB));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE));

                videoList.add(new VideoItem(id, title, path, duration, folder, thumb, size, date));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return videoList;
    }

    public ArrayList<VideoItem> getVideosByFolder(String folderName) {
        ArrayList<VideoItem> videoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // ফোল্ডারের ভিডিওগুলো বাই ডিফল্ট নামের অর্ডারে থাকে
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_FOLDER + "=? ORDER BY " + COL_TITLE + " ASC", new String[]{folderName});

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_VIDEO_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(COL_PATH));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(COL_DURATION));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(COL_FOLDER));
                String thumb = cursor.getString(cursor.getColumnIndexOrThrow(COL_THUMB));
                String size = cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DATE));

                videoList.add(new VideoItem(id, title, path, duration, folder, thumb, size, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return videoList;
    }
    
    public boolean hasData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + TABLE_NAME, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }
    // --- NEW: Folder Statistics (Count & Size) ---
    public long[] getFolderStats(String folderName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long[] stats = new long[2]; // Index 0 = Count, Index 1 = Total Size
        
        try {
            // ভিডিও কাউন্ট এবং সাইজ এর যোগফল বের করা
            Cursor cursor = db.rawQuery(
                "SELECT COUNT(*), SUM(CAST(" + COL_SIZE + " AS INTEGER)) FROM " + TABLE_NAME + " WHERE " + COL_FOLDER + "=?", 
                new String[]{folderName}
            );

            if (cursor.moveToFirst()) {
                stats[0] = cursor.getLong(0); // Total Video Count
                stats[1] = cursor.getLong(1); // Total File Size (Bytes)
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }
}
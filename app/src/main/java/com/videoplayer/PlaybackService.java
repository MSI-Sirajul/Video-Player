package com.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.ui.PlayerNotificationManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class PlaybackService extends Service {

    private final IBinder binder = new LocalBinder();
    public ExoPlayer player;
    private MediaSession mediaSession;
    private PlayerNotificationManager notificationManager;
    
    private static final String CHANNEL_ID = "playback_channel";
    private static final int NOTIFICATION_ID = 111;
    
    public boolean isBackgroundPlayEnabled = false;

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        // ১. প্লেয়ার তৈরি
        player = new ExoPlayer.Builder(this).build();
        
        // ২. মিডিয়া সেশন তৈরি
        mediaSession = new MediaSession.Builder(this, player).build();
        
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Video Player Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Media Control");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startBackgroundPlay(String videoTitle) {
        if (notificationManager == null) {
            notificationManager = new PlayerNotificationManager.Builder(this, NOTIFICATION_ID, CHANNEL_ID)
                    
                    // --- META DATA ADAPTER ---
                    .setMediaDescriptionAdapter(new PlayerNotificationManager.MediaDescriptionAdapter() {
                        @Override
                        public CharSequence getCurrentContentTitle(Player player) {
                            if (player.getCurrentMediaItem() != null && player.getCurrentMediaItem().mediaMetadata.title != null) {
                                return player.getCurrentMediaItem().mediaMetadata.title;
                            }
                            return "Video Player";
                        }

                        @Nullable
                        @Override
                        public PendingIntent createCurrentContentIntent(Player player) {
                            Intent intent = new Intent(PlaybackService.this, PlayerActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            return PendingIntent.getActivity(PlaybackService.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                        }

                        @Nullable
                        @Override
                        public CharSequence getCurrentContentText(Player player) {
                            return null; 
                        }

                        @Nullable
                        @Override
                        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                            // থাম্বনেইল লোডিং (Glide)
                            if (player.getCurrentMediaItem() != null && player.getCurrentMediaItem().mediaId != null) {
                                Glide.with(PlaybackService.this)
                                        .asBitmap()
                                        .load(player.getCurrentMediaItem().mediaId)
                                        .centerCrop()
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                callback.onBitmap(resource);
                                            }
                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {}
                                            @Override
                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                callback.onBitmap(null);
                                            }
                                        });
                            }
                            return null;
                        }
                    })
                    
                    // --- NOTIFICATION LISTENER ---
                    .setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                        @Override
                        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                            if (ongoing) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                                } else {
                                    startForeground(notificationId, notification);
                                }
                            } else {
                                stopForeground(false);
                            }
                        }

                        @Override
                        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                            stopForeground(true);
                            isBackgroundPlayEnabled = false;
                            stopSelf();
                        }
                    })
                    
                    // --- ICONS (FIX: Removed deprecated methods) ---
                    .setSmallIconResourceId(R.drawable.exo_icon_play)
                    .build();

            // ৩. মিডিয়া সেশন সেট করা (সিকবার ফিক্স)
            notificationManager.setMediaSessionToken(mediaSession.getSessionCompatToken());
            
            // ৪. প্লেয়ার সেট করা
            notificationManager.setPlayer(player);
            
            // ৫. অ্যাকশন বাটন কনফিগারেশন
            notificationManager.setUseNextAction(true);
            notificationManager.setUsePreviousAction(true);
            notificationManager.setUsePlayPauseActions(true);
            notificationManager.setUseStopAction(false);
            
            // ক্রোনোমিটার বন্ধ (সিকবার দেখানোর জন্য)
            notificationManager.setUseChronometer(false);
        }
    }

    public void stopBackgroundPlay() {
        if (notificationManager != null) {
            notificationManager.setPlayer(null);
            notificationManager = null;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; 
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
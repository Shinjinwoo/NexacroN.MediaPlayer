package com.tobesoft.plugin.mediaplayerobject;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;
import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_MEDIA_RESOURCE;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_MEDIA_RESOURCE_TYPE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import com.tobesoft.plugin.mediaplayerobject.databinding.ActivityPlayerBinding;

import java.net.MalformedURLException;
import java.net.URL;

public class MediaPlayerActivity extends AppCompatActivity {

    private ExoPlayer mExoPlayer = null;
    private boolean mPlayWhenReady = true;
    private int mCurrentItem;
    private Long mPlaybackPosition = 0L;
    private ActivityPlayerBinding binding = null;
    private final Player.Listener playbackStateListener;
    private String mMediaResourceType = "";

    public static final String DEFAULT_FILEPATH = "file://";

    public String mResource = "";
    public Boolean mIsMediaResourceTypeFile = false;

    private final static String LOG_TAG = "MediaPlayerActivity";

    public MediaPlayerActivity() {
        this.playbackStateListener = playbackStateListener();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extraParam = getIntent().getExtras();


        mResource = extraParam.getString(PARAM_MEDIA_RESOURCE);
        mIsMediaResourceTypeFile = extraParam.getBoolean(PARAM_MEDIA_RESOURCE_TYPE);

        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            initializePlayer(mResource);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        releasePlayer();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        hideSystemUi();
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            initializePlayer(mResource);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        releasePlayer();
        super.onPause();
    }


    private void initializePlayer(String mediaResource) {
        mExoPlayer = new ExoPlayer.Builder(this)
                .build();

        MediaItem mediaItem = null;
        binding.videoView.setPlayer(mExoPlayer);

        if ( mIsMediaResourceTypeFile ) {
             mediaItem = MediaItem.fromUri(DEFAULT_FILEPATH + mediaResource);
        } else {
             mediaItem = MediaItem.fromUri(mediaResource);
        }

        mExoPlayer.setMediaItem(mediaItem);
        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
        mExoPlayer.seekTo(mCurrentItem, mPlaybackPosition);
        mExoPlayer.addAnalyticsListener(new EventLogger());
        mExoPlayer.addListener(playbackStateListener());

        mExoPlayer.prepare();
    }

    private void releasePlayer() {

        ExoPlayer exoPlayer = mExoPlayer;

        if (exoPlayer != null) {
            mPlaybackPosition = exoPlayer.getCurrentPosition();
            mCurrentItem = exoPlayer.getCurrentMediaItemIndex();
            mPlayWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.removeListener(playbackStateListener());
            exoPlayer.release();
        }
        mExoPlayer = null;
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);
        WindowInsetsControllerCompat controllerCompat = new WindowInsetsControllerCompat(this.getWindow(), binding.videoView);
        controllerCompat.hide(WindowInsetsCompat.Type.systemBars());
        controllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }


    private Player.Listener playbackStateListener() {
        return new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case STATE_IDLE:
                        Log.d(LOG_TAG, "::::: STATE_IDLE 플레이어가 인스턴스화.");
                    case STATE_BUFFERING:
                        // 버퍼링된 데이터가 충분하지 않아 플레이어가 현재 위치에서 재생할 수 없습니다..
                        // 버퍼링중...
                        Log.d(LOG_TAG, "::::: STATE_BUFFERING ");
                    case STATE_READY:
                        Log.d(LOG_TAG, "::::: STATE_READY 플레이어가 현재 위치에서 즉시 재생할 수 없습니다.");
                    case STATE_ENDED:
                        Log.d(LOG_TAG, "::::: STATE_ENDED 플레이어가 미디어 재생을 시작 했습니다.");
                    default:
                }
                Player.Listener.super.onPlaybackStateChanged(playbackState);
            }
        };
    }


}
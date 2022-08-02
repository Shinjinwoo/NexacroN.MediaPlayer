package com.tobesoft.plugin.mediaplayerobject;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.CODE_ERROR;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.CODE_SUCCESS;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_HIDE_SYSTEM_UI;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_MEDIA_RESOURCE;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_MEDIA_RESOURCE_TYPE;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.PARAM_MEDIA_START_TIME;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.tobesoft.plugin.mediaplayerobject.databinding.ActivityPlayerBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class MediaPlayerActivity extends AppCompatActivity {

    private ExoPlayer mExoPlayer = null;
    private boolean mPlayWhenReady = true;
    private int mCurrentItem;
    private Long mPlaybackPosition = 0L;
    private ActivityPlayerBinding binding = null;

    //public Long mIsWantToPlayerContinue = 0L;
    public Boolean mIsWantToHideSystemUI = false;

    public static final String DEFAULT_FILEPATH = "file://";

    public String mResource = "";
    public Long mStartTime = 0L;
    public Boolean mIsMediaResourceTypeFile = false;

    private final static String LOG_TAG = "MediaPlayerActivity";

    public MediaPlayerObject mMediaPlayerObject = null;

    public MediaPlayerActivity() {
        mMediaPlayerObject = MediaPlayerObject.getInstance();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extraParam = getIntent().getExtras();

        mResource = extraParam.getString(PARAM_MEDIA_RESOURCE);
        //mStartTime = extraParam.getString(PARAM_MEDIA_START_TIME);

        mPlaybackPosition = extraParam.getLong(PARAM_MEDIA_START_TIME, 0L);
        mIsWantToHideSystemUI = extraParam.getBoolean(PARAM_HIDE_SYSTEM_UI, false);

        mIsMediaResourceTypeFile = extraParam.getBoolean(PARAM_MEDIA_RESOURCE_TYPE);

        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        if (mIsWantToHideSystemUI) {
            hideSystemUi();
        }

        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            if (mStartTime > 0) {
                initializePlayer(mResource, mStartTime);
            } else {
                initializePlayer(mResource);
            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        try {
            releasePlayer();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            releasePlayer();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {

        if (mIsWantToHideSystemUI) {
            hideSystemUi();
        }

        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            if (mStartTime > 0) {
                initializePlayer(mResource, mStartTime);
            } else {
                initializePlayer(mResource);
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            releasePlayer();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    private void initializePlayer(String mediaResource) {
        mExoPlayer = new ExoPlayer.Builder(this)
                .build();

        MediaItem mediaItem = null;
        binding.videoView.setPlayer(mExoPlayer);

        if (mIsMediaResourceTypeFile) {
            mediaItem = MediaItem.fromUri(DEFAULT_FILEPATH + mediaResource);
        } else {
            mediaItem = MediaItem.fromUri(mediaResource);
        }

        mExoPlayer.setMediaItem(mediaItem);
        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
        mExoPlayer.seekTo(mCurrentItem, mPlaybackPosition);
        mExoPlayer.addAnalyticsListener(new EventLogger());
        mExoPlayer.addListener(playbackStateListener());
        mExoPlayer.addListener(playErrorException());

        mExoPlayer.prepare();
    }


    private void initializePlayer(String mediaResource, Long setStartTime) {
        mExoPlayer = new ExoPlayer.Builder(this)
                .build();

        MediaItem mediaItem = null;
        binding.videoView.setPlayer(mExoPlayer);

        if (mIsMediaResourceTypeFile) {
            mediaItem = MediaItem.fromUri(DEFAULT_FILEPATH + mediaResource);
        } else {
            mediaItem = MediaItem.fromUri(mediaResource);
        }

        mExoPlayer.setMediaItem(mediaItem, setStartTime);
        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
        mExoPlayer.seekTo(mCurrentItem, mPlaybackPosition);
        mExoPlayer.addAnalyticsListener(new EventLogger());
        mExoPlayer.addListener(playbackStateListener());
        mExoPlayer.addListener(playErrorException());

        mExoPlayer.prepare();
    }

    private void releasePlayer() throws JSONException {

        ExoPlayer exoPlayer = mExoPlayer;

        if (exoPlayer != null) {
            mPlaybackPosition = exoPlayer.getCurrentPosition();
            mCurrentItem = exoPlayer.getCurrentMediaItemIndex();
            mPlayWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.removeListener(playbackStateListener());


            String duration = String.valueOf(mExoPlayer.getDuration());
            String currentPosition = String.valueOf(mExoPlayer.getCurrentPosition());

            Log.d(LOG_TAG, "::::::::::::::::::::::::::::::::::::::::::::::::" + duration);
            Log.d(LOG_TAG, "::::::::::::::::::::::::::::::::::::::::::::::::" + currentPosition);
            exoPlayer.release();

            JSONObject jsonMediaInfoObject = new JSONObject();

            jsonMediaInfoObject.put("duration", duration);
            jsonMediaInfoObject.put("currentPosition", currentPosition);

            mMediaPlayerObject.send(CODE_SUCCESS, jsonMediaInfoObject);
        }
        mExoPlayer = null;

//        Intent intentR = new Intent();
//        intentR.putExtra("sendText" , ""); //사용자에게 입력받은값 넣기
//        setResult(RESULT_OK,intentR); //결과를 저장
//        finish();

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

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                mMediaPlayerObject.send(CODE_ERROR, error);
                Player.Listener.super.onPlayerError(error);
            }
        };
    }


    private Player.Listener playErrorException() {
        return new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                mMediaPlayerObject.send(CODE_ERROR, error);
                Player.Listener.super.onPlayerError(error);
            }
        };
    }


}
package com.tobesoft.plugin.mediaplayerobject;

import static com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING;
import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.CODE_ERROR;
import static com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject.CODE_SUCCESS;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.tobesoft.plugin.mediaplayerobject.databinding.ActivityPlayerBinding;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.Define;
import com.tobesoft.plugin.mediaplayerobject.util.AnimationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MediaPlayerActivity extends AppCompatActivity {

    private ExoPlayer mExoPlayer = null;
    private boolean mPlayWhenReady = true;
    private int mCurrentItem;
    private Long mPlaybackPosition = 0L;
    private ActivityPlayerBinding binding = null;
    private PictureInPictureParams.Builder mPipBuilder = null;

    public Boolean mIsWantToHideSystemUI = false;
    private Boolean mIsError = false;
    private String mErrorMsg = "";
    private Boolean mIsAlreadyPip = false;


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

        mResource = extraParam.getString(Define.ConstString.PARAM_MEDIA_RESOURCE);
        mPlaybackPosition = extraParam.getLong(Define.ConstString.PARAM_MEDIA_START_TIME, 0L);
        mIsWantToHideSystemUI = extraParam.getBoolean(Define.ConstString.PARAM_HIDE_SYSTEM_UI, false);
        mIsMediaResourceTypeFile = extraParam.getBoolean(Define.ConstString.PARAM_MEDIA_RESOURCE_TYPE);


        // 액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        binding.pipButton.bringToFront();
        binding.pipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPipMode();
            }
        });
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (mIsWantToHideSystemUI) {
                    hideSystemUi();
                }
                if (mStartTime > 0) {
                    initializePlayer(mResource, mStartTime);
                } else {
                    initializePlayer(mResource);
                }

            }
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        try {
            releasePlayer();
            if (mIsError) {
                mMediaPlayerObject.send(CODE_ERROR, mErrorMsg);

                mIsError = false;
                mErrorMsg = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPipBuilder = null;
        mMediaPlayerObject.mIsPipMode = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            releasePlayer();
            if (mIsError) {
                mMediaPlayerObject.send(CODE_ERROR, mErrorMsg);

                mIsError = false;
                mErrorMsg = "";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPipBuilder = null;
        mMediaPlayerObject.mIsPipMode = false;

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (mIsWantToHideSystemUI) {
                    hideSystemUi();
                }
                if (mStartTime > 0) {
                    initializePlayer(mResource, mStartTime);
                } else {
                    initializePlayer(mResource);
                }

            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (isInPictureInPictureMode()) {
                    Log.d(LOG_TAG, "::::::::::is In Pip Mode");
                } else {
                    releasePlayer();
                }
            }

            if (mIsError) {
                mMediaPlayerObject.send(CODE_ERROR, mErrorMsg);

                mIsError = false;
                mErrorMsg = "";
            }
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
        binding.videoView.setControllerVisibilityListener(customControllerVisibilityListener());

        if (mIsMediaResourceTypeFile) {
            mediaItem = MediaItem.fromUri(DEFAULT_FILEPATH + mediaResource);
        } else {
            mediaItem = MediaItem.fromUri(mediaResource);
        }

        mExoPlayer.setMediaItem(mediaItem);
        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
        mExoPlayer.seekTo(mCurrentItem, mPlaybackPosition);
        mExoPlayer.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        //mExoPlayer.addAnalyticsListener(new EventLogger());
        mExoPlayer.addListener(playbackStateListener());

        mExoPlayer.prepare();
    }


    private void initializePlayer(String mediaResource, Long setStartTime) {
        mExoPlayer = new ExoPlayer.Builder(this)
                .build();

        MediaItem mediaItem = null;
        binding.videoView.setPlayer(mExoPlayer);
        binding.videoView.setControllerVisibilityListener(customControllerVisibilityListener());


        if (mIsMediaResourceTypeFile) {
            mediaItem = MediaItem.fromUri(DEFAULT_FILEPATH + mediaResource);
        } else {
            mediaItem = MediaItem.fromUri(mediaResource);
        }

        mExoPlayer.setMediaItem(mediaItem, setStartTime);
        mExoPlayer.setPlayWhenReady(mPlayWhenReady);
        mExoPlayer.seekTo(mCurrentItem, mPlaybackPosition);
        mExoPlayer.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        //mExoPlayer.addAnalyticsListener(new EventLogger());
        mExoPlayer.addListener(playbackStateListener());
        mExoPlayer.getCurrentManifest();

        mExoPlayer.prepare();


    }

    private void releasePlayer() throws JSONException {

        ExoPlayer exoPlayer = mExoPlayer;

        if (exoPlayer != null) {
            mPlaybackPosition = exoPlayer.getCurrentPosition();
            mCurrentItem = exoPlayer.getCurrentMediaItemIndex();
            mPlayWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.removeListener(playbackStateListener());

            if (!mIsError) {
                String duration = String.valueOf(mExoPlayer.getDuration());
                String currentPosition = String.valueOf(mExoPlayer.getCurrentPosition());

                Log.d(LOG_TAG, "::::::::::::::::::::::::::::::::::::::::::::::::" + duration);
                Log.d(LOG_TAG, "::::::::::::::::::::::::::::::::::::::::::::::::" + currentPosition);
                JSONObject jsonMediaInfoObject = new JSONObject();
                jsonMediaInfoObject.put("duration", duration);
                jsonMediaInfoObject.put("currentPosition", currentPosition);
                mMediaPlayerObject.send(CODE_SUCCESS, jsonMediaInfoObject);
                if (mExoPlayer.getDuration() < 0) {
                    mMediaPlayerObject.send(CODE_ERROR, "MediaPlayer Initialize Not Yet");
                }
            }

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

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                mIsError = true;
                mErrorMsg = "Error Message : " + error.getMessage() + "\nError StackTrace : " + Arrays.toString(error.getStackTrace());
                Player.Listener.super.onPlayerError(error);
            }
        };
    }

    private StyledPlayerView.ControllerVisibilityListener customControllerVisibilityListener() {
        return new StyledPlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (!mIsAlreadyPip) {
                    if (visibility == View.GONE) {
                        Log.d(LOG_TAG, "::::::::::::::::컨트롤러 사라짐");
                        AnimationUtils.slideDown(binding.pipButton);
                    } else {
                        binding.pipButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.pipButton.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    private void onPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPipBuilder = new PictureInPictureParams.Builder();
            mPipBuilder.setAspectRatio(new Rational(binding.videoView.getWidth(), binding.videoView.getHeight()));
            this.enterPictureInPictureMode();

            mIsAlreadyPip = true;
            mMediaPlayerObject.mIsPipMode = true;

        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()) {
                mIsAlreadyPip = isInPictureInPictureMode;
                binding.pipButton.setVisibility(View.INVISIBLE);
            } else {
                mIsAlreadyPip = false;
                binding.pipButton.setVisibility(View.VISIBLE);
            }
        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }


}
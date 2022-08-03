package com.example.nexacronmediaplayer;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.nexacro.NexacroActivity;
import com.tobesoft.plugin.mediaplayerobject.MediaPlayerObject;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.Define;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.MediaPlayerInterface;

public class NexacroActivityExt extends NexacroActivity implements MediaPlayerInterface {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    /* BioMetric 연동 코드 ****************************************************************************/
    // private BiometricObject mBiometricObject;

    //    @Override
//    public void setBiometricObject(BiometricObject obj) {
//        this.mBiometricObject = obj;
//    }
//
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        if (mBiometricObject.isActivityResult(requestCode)) {
//            if (mBiometricObject != null) {
//                mBiometricObject.onActivityResult(requestCode, resultCode, intent);
//            }
//        } else
        if (mMediaPlayerObject.isActivityResult(requestCode)) {
            if (mMediaPlayerObject != null) {
                mMediaPlayerObject.onActivityResult(requestCode, resultCode, intent);
            }
        }
    }
    /* BioMetric 연동 코드 ****************************************************************************/

    private MediaPlayerObject mMediaPlayerObject;

    @Override
    public void setMediaPlayerObject(MediaPlayerObject mediaplayerObject) {
        this.mMediaPlayerObject = mediaplayerObject;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        mMediaPlayerObject.onRequestPermissionsResult(requestCode,permission,grantResults);
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
    }



}

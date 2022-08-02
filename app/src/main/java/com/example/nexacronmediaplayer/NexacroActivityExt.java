package com.example.nexacronmediaplayer;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

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

    // 모든 저장소 권한은 특별하게 필요하지 않으므로 주석처리

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
//        if (Build.VERSION.SDK_INT >= 30) {
//            for (String checkPermission : permission) {
//                if (checkPermission.equals("android.permission.READ_EXTERNAL_STORAGE")) {
//                    Intent intent = new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                    startActivityForResult(intent, Define.REQUEST_PERMISSION.APP_STORAGE_ACCESS_REQUEST_CODE);
//                }
//            }
//        }
//        super.onRequestPermissionsResult(requestCode, permission, grantResults);
//    }
}

package com.tobesoft.plugin.mediaplayerobject;

import static android.app.Activity.RESULT_CANCELED;

import static com.kh.plugin.plugincommonlib.constant.PluginConstants.CODE_PERMISSION_ERROR;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import com.kh.plugin.plugincommonlib.info.PermissionRequest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kh.plugin.plugincommonlib.util.PermissionUtil;
import com.nexacro.NexacroActivity;
import com.nexacro.plugin.NexacroPlugin;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.Define;
import com.tobesoft.plugin.mediaplayerobject.plugininterface.MediaPlayerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerObject extends NexacroPlugin {

    private static final String SVCID = "svcid";
    private static final String REASON = "reason";
    private static final String RETVAL = "returnvalue";

    private static final String CALL_BACK = "_oncallback";

    private static final String METHOD_CALLMETHOD = "callMethod";

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR = -1;

    private final static String LOG_TAG = "MediaPlayerObject";

//    public static final String PARAM_MEDIA_RESOURCE_TYPE = "mediaTypeFile";
//    public static final String PARAM_MEDIA_RESOURCE = "data";
//    public static final String PARAM_MEDIA_START_TIME = "startTime";
//    public static final String PARAM_HIDE_SYSTEM_UI = "hideSystemUI";


    public String mServiceId = "";
    public Boolean mIsMediaResourceTypeFile = false;
    public Boolean mIsPipMode = false;


    public static final int MEDIA_ACTIVITY_REQUEST = 123123123;

    MediaPlayerInterface mMediaPlayerInterface = null;
    Activity mActivity = null;

    private static MediaPlayerObject mMediaPlayerObject;

    public static MediaPlayerObject getInstance() {
        return mMediaPlayerObject;
    }

    public MediaPlayerObject(String objectId) {
        super(objectId);

        mMediaPlayerInterface = (MediaPlayerInterface) NexacroActivity.getInstance();
        mMediaPlayerInterface.setMediaPlayerObject(this);
        mActivity = NexacroActivity.getInstance();
        //requestPermissionForMediaPlayer();

        mMediaPlayerObject = this;

    }


    @Override
    public void init(JSONObject paramObject) {

    }

    @Override
    public void release(JSONObject paramObject) {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void execute(String method, JSONObject paramObject) {
        mServiceId = "";
        if (method.equals(METHOD_CALLMETHOD)) {
            try {
                JSONObject params = paramObject.getJSONObject("params");
                mServiceId = params.getString("serviceid");


                if (mServiceId.equals("mediaOpen")) {
                    JSONObject jsonObject = params.getJSONObject("param");


                    List<String> permissions = new ArrayList<>();
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                    //장수석님 퍼미션 유틸
                    List<String> requestPermissions = PermissionUtil.hasPermissions(mActivity, permissions);

                    if (!requestPermissions.isEmpty()) {
                        ActivityCompat.requestPermissions(mActivity, requestPermissions.toArray(new String[requestPermissions.size()]), PermissionRequest.MEDIAPLAYER_PERMISION_REQUST);
                    } else {
                        String mediaResource = jsonObject.getString("mediaResource");

                        //jsonObject.optString의 경우 값이 없으면 두번쨰 파라미터의 값이 Default로 들어가게 된다.
                        String mediaStartTime = jsonObject.optString("mediaStartTime", "0");
                        String hideSystemUI = jsonObject.optString("hideSystemUI", "false");
                        Log.d(LOG_TAG, mediaStartTime + " " + hideSystemUI);

                        if (Patterns.WEB_URL.matcher(mediaResource).matches()) {
                            if (!mediaResource.equals("")) {
                                playByUrl(mediaResource, mediaStartTime, hideSystemUI);
                            } else {
                                send(CODE_ERROR, METHOD_CALLMETHOD + " : no Value for MediaPlayer Resource");
                            }
                        } else {
                            if (!mediaResource.equals("")) {
                                playByFile(mediaResource, mediaStartTime, hideSystemUI);
                            } else {
                                send(CODE_ERROR, METHOD_CALLMETHOD + " : no Value for MediaPlayer Resource");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                send(CODE_ERROR, METHOD_CALLMETHOD + " : " + e.getMessage());
            }
        }
    }


    public void playByUrl(String urlPath, String mediaStartTime, String hideSystemUI) {

        Bundle extraParam = new Bundle();
        mIsMediaResourceTypeFile = false;

        extraParam.putString(Define.ConstString.PARAM_MEDIA_RESOURCE, urlPath);
        extraParam.putBoolean(Define.ConstString.PARAM_MEDIA_RESOURCE_TYPE, mIsMediaResourceTypeFile);
        if (!mediaStartTime.equals("false")) {
            extraParam.putLong(Define.ConstString.PARAM_MEDIA_START_TIME, Long.parseLong(mediaStartTime));
        }

        if (Boolean.parseBoolean(hideSystemUI)) {
            extraParam.putBoolean(Define.ConstString.PARAM_HIDE_SYSTEM_UI, true);
        }

        Intent intent = new Intent(mActivity, MediaPlayerActivity.class);
        intent.putExtras(extraParam);

        mActivity.startActivityForResult(intent, MEDIA_ACTIVITY_REQUEST);
    }

    public void playByFile(String filePath, String mediaStartTime, String hideSystemUI) {

        Bundle extraParam = new Bundle();
        mIsMediaResourceTypeFile = true;

        extraParam.putString(Define.ConstString.PARAM_MEDIA_RESOURCE, filePath);
        extraParam.putBoolean(Define.ConstString.PARAM_MEDIA_RESOURCE_TYPE, mIsMediaResourceTypeFile);
        if (!mediaStartTime.equals("0")) {
            extraParam.putString(Define.ConstString.PARAM_MEDIA_START_TIME, mediaStartTime);
        }

        if (Boolean.parseBoolean(hideSystemUI)) {
            extraParam.putBoolean(Define.ConstString.PARAM_HIDE_SYSTEM_UI, true);
        }

        Intent intent = new Intent(mActivity, MediaPlayerActivity.class);
        intent.putExtras(extraParam);

        mActivity.startActivityForResult(intent, MEDIA_ACTIVITY_REQUEST);
    }


    public boolean send(int reason, Object retval) {
        return send(mServiceId, CALL_BACK, reason, retval);
    }

    public boolean send(String svcid, String callMethod, int reason, Object retval) {

        JSONObject obj = new JSONObject();

        try {
            if (mServiceId != null) {
                obj.put(SVCID, svcid);
                obj.put(REASON, reason);
                obj.put(RETVAL, retval);

                callback(callMethod, obj);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    public static boolean isActivityResult(int requestCode) {
        boolean result = false;
        switch (requestCode) {
            case MEDIA_ACTIVITY_REQUEST:
                result = true;
                break;
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == MEDIA_ACTIVITY_REQUEST) {
            if (intent != null) {
                JSONObject jsonMediaInfoObject = new JSONObject();
                if (resultCode == RESULT_CANCELED) {
                    String errorInfo = intent.getExtras().getString("error");
                    send(CODE_ERROR, METHOD_CALLMETHOD + " : " + errorInfo);
                }
                try {
                    jsonMediaInfoObject.put("duration", intent.getExtras().getString("duration"));
                    jsonMediaInfoObject.put("currentPosition", intent.getExtras().getString("currentPosition"));

                    send(CODE_SUCCESS, jsonMediaInfoObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public static boolean isRequestPermissionsResult(int requestCode) {
//        boolean result = false;
//        switch(requestCode) {
//            case PermissionRequest.MEDIAPLAYER_PERMISION_REQUST:
//                result = true;
//                break;
//        }
//        return result;
//    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionRequest.MEDIAPLAYER_PERMISION_REQUST: {
                boolean isPermissionGranted = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(LOG_TAG, "permissions[" + i + "] = " + permissions[i] + " : PERMISSION_DENIED");
                        isPermissionGranted = false;
                    }
                }
                if (isPermissionGranted) {
                    try {
                        send(CODE_SUCCESS,"Permission Granted");
                    } catch (Exception e) {
                        send(CODE_ERROR, METHOD_CALLMETHOD + ":" + e.getMessage());
                    }
                } else {
                    send(CODE_PERMISSION_ERROR, "permission denied");
                }
                break;
            }
        }
    }


}

package com.tobesoft.plugin.mediaplayerobject.plugininterface;


import com.tobesoft.plugin.mediaplayerobject.BuildConfig;

public interface Define {



    interface REQUEST_PERMISSION {

        int CAMERA = 101;
        int LOCATION = 102;
        int STORAGE = 103;
        int IGNORE_BATTERY = 104;
        int LOCATION_ONE = 105;
//        int CALL_PHONE = 106;
        int CAMERA_GALLERY = 107;

        int APP_STORAGE_ACCESS_REQUEST_CODE = 9301;


    }

    interface requestCode {

        int SELECT_RINGTONE = 201;
        int CAMERA_GALLERY = 202;

    }
}

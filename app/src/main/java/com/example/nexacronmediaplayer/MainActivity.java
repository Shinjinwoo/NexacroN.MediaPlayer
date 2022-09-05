package com.example.nexacronmediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nexacro.NexacroResourceManager;
import com.nexacro.NexacroUpdatorActivity;

public class MainActivity extends NexacroUpdatorActivity {

    public MainActivity() {

        super();

        setBootstrapURL("http://smart.tobesoft.co.kr/NexacroN/MediaPlayerPlugin/_android_/start_android.json");
        setProjectURL("http://smart.tobesoft.co.kr/NexacroN/MediaPlayerPlugin/_android_/");

        this.setStartupClass(NexacroActivityExt.class);
    }


    private String LOG_TAG = this.getClass().toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NexacroResourceManager.createInstance(this);
        NexacroResourceManager.getInstance().setDirect(true);

        Intent intent = getIntent();
        if (intent != null) {
            String bootstrapURL = intent.getStringExtra("bootstrapURL");
            String projectUrl = intent.getStringExtra("projectUrl");
            if (bootstrapURL != null) {
                setBootstrapURL(bootstrapURL);
                setProjectURL(projectUrl);
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }
}
package com.ndzl.uniqueids;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.util.Log;


import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

// androidx JobIntentService replacing IntentService - JobIntentService is fully supported on API30
public class UniqueIDsService extends JobIntentService {

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, UniqueIDsService.class, 3003, work);
    }


    Timer tim;
    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        int TWS_PORT = 8080;
        Log.i("UniqueIDsService", "Staring TinyWebServer - Get Device Serial Number on http://localhost:"+TWS_PORT+"/serial");

        TinyWebServer.startServer("0.0.0.0",TWS_PORT, "/sdcard/Download"); //49403 ok  //8080 ok




    }
}


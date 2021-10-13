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
        Log.i("UniqueIDsService", "Executing work: " + intent);
        /*
        tim = new Timer("JOBSERVICE", false);
        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("UniqueIDsService", "TIMER CALLED");
            }
        }, 3000);

         */



    }
}


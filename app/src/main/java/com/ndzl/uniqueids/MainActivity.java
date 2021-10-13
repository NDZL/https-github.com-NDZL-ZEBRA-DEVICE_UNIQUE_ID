package com.ndzl.uniqueids;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import com.symbol.emdk.EMDKBase;
import com.symbol.emdk.EMDKException;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;


//PER AVVIA DA ADB
//adb shell am start com.ndzl.uniqueids/com.ndzl.uniqueids.MainActivity

public class MainActivity extends Activity implements EMDKManager.EMDKListener, EMDKManager.StatusListener, ProfileManager.DataListener {

    private Button bwf;

    private TextView tv;

    public static Intent service_is;

    private ProfileManager profileManager = null;
    private EMDKManager emdkManager = null;

    Timer tim;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);  //se non chiamato, niente GUI + android:theme="@android:style/Theme.Translucent.NoTitleBar"

        //tv=(TextView)findViewById(R.id.textView);
        //tv.setText("TARGET SDK: "+getTargetSDK());

        tim = new Timer("NIK", false);
        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("main::UniqueIDsService", "TIMER CALLED");

                        //System.exit(0); //ok qui! chiamato da timer dopo 2 sec.
                    }

                });
            }
        }, 2000);

        TinyWebServer.startServer("0.0.0.0",8080, "/sdcard/Download"); //49403 ok  //8080 ok

        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        service_is = new Intent(this, UniqueIDsService.class);
        service_is.putExtra("WORDS_TO_SAY", "HEY NIK!");
        service_is.putExtra("LANGUAGE", "ITA");
        UniqueIDsService.enqueueWork(this, service_is);


        Toast.makeText(getApplicationContext(), "<UniqueID Service app>",Toast.LENGTH_SHORT).show();
        //finish(); //serve! non rimuovere - fa sparire l'activity dalla recent apps list

    }


    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
        String[] modifyData = new String[1];

        try {
            emdkManager.getInstanceAsync(EMDKManager.FEATURE_TYPE.PROFILE,MainActivity.this);
        } catch (EMDKException e) {
            e.printStackTrace();
        }
    }

    String profileToBeApplied = "ACCESSMGR_SERIAL";
    private void ApplyEMDKprofile(){
        if (profileManager != null) {
            String[] modifyData = new String[1];

            final EMDKResults results = profileManager.processProfileAsync(profileToBeApplied,
                    ProfileManager.PROFILE_FLAG.SET, modifyData);

            //the EMDK PROFILE CONTAINS THE PEM B64 CERTIFICATE OF THE "DEBUG KEY" VERSION OF THIS APK
            //TO EXPORT THE CERTIFICATE IN BINARY PEM
            //C:\Users\CXNT48\.android> "C:\Program Files\Java\jdk-15.0.1\bin\keytool" -exportcert -alias androiddebugkey -keystore debug.keystore -file debug.crt
            //IF THE APK WILL BE SIGNED with a different key (e.g. code recompiled on a different machine),
            //THE PROFILE CONTENT MUST CHANGE ACCORDINGLY
            // in a future version, using getHexAPKsignature() below, it would be possible to load the binary certificate dinamically

            String sty = results.statusCode.toString();
        }
    }

    @Override
    public void onStatus(EMDKManager.StatusData statusData, EMDKBase emdkBase) {
        if(statusData.getResult() == EMDKResults.STATUS_CODE.SUCCESS) {
            if(statusData.getFeatureType() == EMDKManager.FEATURE_TYPE.PROFILE)
            {
                profileManager = (ProfileManager)emdkBase;
                profileManager.addDataListener(this);
                ApplyEMDKprofile();
            }
        }
    }

    @Override
    public void onClosed() {

    }

    public static String DEVICE_SERIAL_NUMBER="n/a";
    @Override
    public void onData(ProfileManager.ResultData resultData) {
        EMDKResults result = resultData.getResult();
        if(result.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
            String responseXML = result.getStatusString();
            //Toast.makeText(MainActivity.this, "RESPONSE="+responseXML, Toast.LENGTH_LONG).show();
            Log.i("UniqueIDsService", "EMDK PROFILE OK");
        } else if(result.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            //Toast.makeText(MainActivity.this, "ERROR IN PROFILE APPLICATION", Toast.LENGTH_LONG).show();
            Log.i("UniqueIDsService", "EMDK PROFILE ERROR");
        }

        String serialNumber = RetrieveOEMInfo(Uri.parse(URI_SERIAL),  false);       //  Build.getSerial()
        //Log.i("UniqueIDsService", "AFTER EMDK SERIAL NUMBER="+serialNumber);
        DEVICE_SERIAL_NUMBER = serialNumber;

        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //TinyWebServer.stopServer();
    }

    String TAG = "UniqueIDsService";
    String URI_SERIAL = "content://oem_info/oem.zebra.secure/build_serial";
    String URI_IMEI = "content://oem_info/wan/imei";
    private String RetrieveOEMInfo(Uri uri,  boolean isIMEI) {
        //  For clarity, this code calls ContentResolver.query() on the UI thread but production code should perform queries asynchronously.
        //  See https://developer.android.com/guide/topics/providers/content-provider-basics.html for more information

        String status="N/A";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null || cursor.getCount() < 1)
        {
            String errorMsg = "ERROR #1";//Error: This app does not have access to call OEM service. " +"Please assign access to " + uri + " through MX.  See ReadMe for more information";
            //Log.d(TAG, errorMsg);
            status = errorMsg;
            return status;
        }
        while (cursor.moveToNext()) {
            if (cursor.getColumnCount() == 0)
            {
                //  No data in the cursor.  I have seen this happen on non-WAN devices
                String errorMsg = "ERROR #2";// "Error: " + uri + " does not exist on this device";
                //Log.d(TAG, errorMsg);
                if (isIMEI)
                    errorMsg = "Error: Could not find IMEI.  Is device WAN capable?";
                status = errorMsg;
            }
            else{
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    //Log.v(TAG, "column " + i + "=" + cursor.getColumnName(i));
                    try {
                        String data = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)));
                        //Log.i(TAG, "Column Data " + i + "=" + data);
                        status = data;
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "Exception reading data for column " + cursor.getColumnName(i));
                    }
                }
            }
        }
        cursor.close();
        return status;
    }

    void getHexAPKsignature(){
        SigningInfo signingInfo = new SigningInfo();
        try {
            signingInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES).signingInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        android.content.pm.Signature[] sigs = signingInfo.getApkContentsSigners();
        for (Signature sig : sigs)
        {
            Log.d("UniqueIDsService", "Signature : " + sig.toCharsString() + " Length: " + sig.toCharsString().length());
            //THE BINARY VERSION OF THIS HEX SIGNATURE CAN BE PASTED INTO EMDK ACCESS MANAGER
            // <parm name="CallerSignature" value="MIIC5DCCAcwCAQEwDQYJKo...
            // use https://holtstrom.com/michael/tools/hextopem.php to convert or any other service
            //byte[] encodedHexB64 = Base64.codeBase64(decodedHex);  ??
        }
    }

}

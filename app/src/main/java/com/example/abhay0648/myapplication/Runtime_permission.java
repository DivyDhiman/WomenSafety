package com.example.abhay0648.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Runtime_permission extends AppCompatActivity{

    private int current_api_Version = Build.VERSION.SDK_INT, code;
    private Context context;
    private String get_result;
    private MyCallbacks myCallbacks;

    public int Current_Build_version(){
        return current_api_Version;
    }


    public void check_permissions(Context context, MyCallbacks myCallbacks, String permission,String get_result, int code) {
        this.context = context;
        this.myCallbacks = myCallbacks;
        this.get_result = get_result;
        this.code = code;
        Log.e("code","code"+code);
        Log.e("get_result","get_result"+get_result);

        if (current_api_Version >= Build.VERSION_CODES.LOLLIPOP) {
            if (isAllowed(permission)) {
                myCallbacks.getPermissionValue(get_result);
            } else {
                requestPermission(permission);
            }
        } else {
            myCallbacks.getPermissionValue(get_result);
        }
    }

    private boolean isAllowed(String permission) {

        //  Getting the permission status
        int result = ContextCompat.checkSelfPermission(context, permission);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
        }

        ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("call",":call");
        //Checking the request code of our request
        if (requestCode == code) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("get_result","get_result"+get_result);
                myCallbacks.getPermissionValue(get_result);
            }
        }
    }
}

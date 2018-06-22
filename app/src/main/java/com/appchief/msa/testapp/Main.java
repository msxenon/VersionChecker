package com.appchief.msa.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.appchief.msa.appchiefvc.ACVerionController;
import com.appchief.msa.appchiefvc.CheckStatus;
import com.appchief.msa.appchiefvc.VersionCheckListener;

/**
 * Created by mac on 3/7/18.
 */

public class Main extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ACVerionController().init(this).Check(new VersionCheckListener() {
            @Override
            public void CheckSuccess(CheckStatus checkStatus,String msg,String link) {
                Log.e("ms",checkStatus.name()+" "+msg+" "+link);
            }

            @Override
            public void CheckError(String msg) {
                Log.e("ms","CheckError "+msg);

            }
        });
    }
}

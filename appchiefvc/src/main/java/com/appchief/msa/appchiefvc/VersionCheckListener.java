package com.appchief.msa.appchiefvc;

/**
 * Created by mac on 3/7/18.
 */

public interface VersionCheckListener {
    void CheckSuccess(CheckStatus checkStatus,String message,String link);
    void CheckError(String msg);
}

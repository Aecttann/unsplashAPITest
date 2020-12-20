package com.aectann.unsplashapitest.additional;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppPreferences {

    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mPrefsEditor;

    //сохранить / получить access key
    public static String getAccessKey(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("accessKey", "");
    }
    public static void setAccessKey(Context ctx, String accessKey) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("accessKey", accessKey);
        mPrefsEditor.apply();
    }

    //сохранить / получить Secret key
//    public static String getSecretKey(Context ctx) {
//        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//        return mPrefs.getString("secretKey", "");
//    }
//    public static void setSecretKey(Context ctx, String secretKey) {
//        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
//        mPrefsEditor = mPrefs.edit();
//        mPrefsEditor.putString("secretKey", secretKey);
//        mPrefsEditor.apply();
//    }

    //сохранить / получить searchingStatus
    public static String getSearchingStatus(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("searchingStatus", "");
    }
    public static void setSearchingStatus(Context ctx, String searchingStatus) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("searchingStatus", searchingStatus);
        mPrefsEditor.apply();
    }

    //set get isDownloadAvailable
    public static String getIsDownloadAvailable(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return mPrefs.getString("isDownloadAvailable", "");
    }
    public static void setIsDownloadAvailable(Context ctx, String isDownloadAvailable) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefsEditor = mPrefs.edit();
        mPrefsEditor.putString("isDownloadAvailable", isDownloadAvailable);
        mPrefsEditor.apply();
    }

}

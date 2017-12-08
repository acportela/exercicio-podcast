package br.ufpe.cin.if710.podcast.Extras;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by acpr on 08/12/17.
 */

public class SharedPreferencesUtil {
    public static boolean getBooleanFromSharedPreferences(String key, Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(key, false);
    }
    public static String getStringFromSharedPreferences(String key, Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(key, null);
    }
    public static void setBooleanOnSharedPreferences(String key, boolean value, Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }
    public static void setStringOnSharedPreferences(String key, String value, Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public static boolean propertyExistsInSharedPreferences(String key, Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.contains(key);
    }
}

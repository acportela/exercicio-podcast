package br.ufpe.cin.if710.podcast.Extras;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acpr on 08/12/17.
 */

public class Permissions {

    public static final int REQUEST_CODE_SAVE_PODCAST_TO_DISK = 0;
    public static final int REQUEST_CODE_JUST_CHECK_AND_ASK = 1;

    public static boolean checkPermissionIsGranted(Activity activity, int requestCode){

        List<String> permissions = new ArrayList<>();

        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissions.isEmpty()){
            String [] array = new String[permissions.size()];
            permissions.toArray(array);
            ActivityCompat.requestPermissions(activity, array, requestCode);
        }
        return permissions.isEmpty();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}

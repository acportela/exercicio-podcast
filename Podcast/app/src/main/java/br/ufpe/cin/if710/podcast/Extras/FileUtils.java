package br.ufpe.cin.if710.podcast.Extras;

import android.os.Environment;

import java.io.File;

/**
 * Created by acpr on 09/12/17.
 */

public class FileUtils {

    public static void deleteAllFilesFromPuclicDirectory(String publicDir){
        File root = Environment.getExternalStoragePublicDirectory(publicDir);
        root.mkdir();
        if(root.exists()){
            File[] files = root.listFiles();
            for(File file: files)
                if (!file.isDirectory())
                    file.delete();
        }
    }

}

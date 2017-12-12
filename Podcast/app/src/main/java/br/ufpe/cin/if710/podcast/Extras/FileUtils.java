package br.ufpe.cin.if710.podcast.Extras;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

/**
 * Created by acpr on 09/12/17.
 */

public class FileUtils {

    public static void deleteAllFilesFromPuclicDirectory(String publicDir, Context ctx){
        File root = Environment.getExternalStoragePublicDirectory(publicDir);
        root.mkdir();
        if(root.exists()){
            File[] files = root.listFiles();
            if(files != null){
                for(File file: files)
                    if (!file.isDirectory())
                        file.delete();
            }
            else {
                Toast.makeText(ctx,"Não há podcast salvo",Toast.LENGTH_LONG).show();

            }
        }
    }
}

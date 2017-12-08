package br.ufpe.cin.if710.podcast.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;

/**
 * Created by acpr on 12/10/17.
 */

public class DownloadPodcastService extends IntentService implements PodcastDMLCommandReport{

    public static final String DOWNLOAD_HAS_ENDED_ACTION = "br.ufpe.cin.if710.podcast.action.DownloadHasEnded";
    private final String UPDATE_SELECTION_CLAUSE = PodcastDBHelper.EPISODE_LINK + " = ?";
    public static final String INTENT_KEY_PAGE_LINK = "link";
    public static final String INTENT_KEY_DOWN_LINK = "downLink";

    public DownloadPodcastService() {
        super("DownloadPodcastService");
    }

    @Override
    protected void onHandleIntent(Intent i) {
        try {
            //checar se tem permissao... Android 6.0+
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
            root.mkdirs();

            String fileName = i.getData().getLastPathSegment();
            String pagelink = i.getExtras().getString(INTENT_KEY_PAGE_LINK);
            String downLink = i.getExtras().getString(INTENT_KEY_DOWN_LINK);

            File output = new File(root,fileName);
            if (output.exists()) {
                output.delete();
            }
            URL url = new URL(downLink);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            FileOutputStream fos = new FileOutputStream(output.getPath());
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try {
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[8192];
                int len = 0;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            finally {
                fos.getFD().sync();
                out.close();
                c.disconnect();
            }

            updateFileURIOnDatabase(this,pagelink,fileName);

        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }

    //ATUALIZA O FILEURI DA LINHA DA TABELA QUE TENHA O LINK DA PÁGINA IGUAL AO ARGUMENTO PASSADO
    private void updateFileURIOnDatabase(Context c, String argument, String fileName){
        if(argument != null){
            String[] arg = {argument};
            ContentValues cv = new ContentValues();
            cv.put(PodcastDBHelper.EPISODE_FILE_URI,fileName);
            PodcastSQLiteDML.getInstance().updatePodcasts(c,this,UPDATE_SELECTION_CLAUSE,arg,cv);
        }
    }

    @Override
    public void onDmlUpdateFineshed(Cursor cursor) {
        sendBroadcast(new Intent(DOWNLOAD_HAS_ENDED_ACTION));
        stopSelf();
    }
    @Override
    public void onDmlQueryFineshed(Cursor cursor) {

    }
    @Override
    public void onDmlInsertFineshed(Cursor cursor) {

    }
}

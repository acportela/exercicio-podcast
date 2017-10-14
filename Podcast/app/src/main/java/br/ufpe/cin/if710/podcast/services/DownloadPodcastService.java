package br.ufpe.cin.if710.podcast.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import android.support.v4.content.LocalBroadcastManager;
/**
 * Created by acpr on 12/10/17.
 */

public class DownloadPodcastService extends IntentService {

    private final String DOWNLOAD_HAS_ENDED_ACTION = "br.ufpe.cin.if710.podcast.action.DownloadHasEnded";

    public DownloadPodcastService() {
        super("DownloadPodcastService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent i) {
        try {
            //checar se tem permissao... Android 6.0+
            File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
            root.mkdirs();
            File output = new File(root, i.getData().getLastPathSegment());
            if (output.exists()) {
                output.delete();
            }
            URL url = new URL(i.getData().toString());
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

            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DOWNLOAD_HAS_ENDED_ACTION));
            //FileUtils.copyURLToFile(url,root);

        } catch (IOException e2) {
            Log.e(getClass().getName(), "Exception durante download", e2);
        }
    }
}

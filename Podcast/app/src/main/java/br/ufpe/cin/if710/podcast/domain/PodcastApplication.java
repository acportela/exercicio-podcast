package br.ufpe.cin.if710.podcast.domain;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StrictMode;

import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import br.ufpe.cin.if710.podcast.BuildConfig;
import br.ufpe.cin.if710.podcast.Extras.FileUtils;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.services.DownloadFeedJob;

/**
 * Created by acpr on 12/10/17.
 */

public class PodcastApplication extends Application {

    private static List<ItemFeed> newfeedList;

    @Override
    public void onCreate() {
        super.onCreate();

        //Inicializando Leak Canary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        //Inicializando AndroidDevMetrics
        if (BuildConfig.DEBUG) {
            AndroidDevMetrics.initWith(this);
        }

        newfeedList = new ArrayList<>();

        //Inicializando Job pra baixar o feed de forma periódica
        scheduleDownloadFeedJob(getApplicationContext());
    }

    public static List<ItemFeed> getNewfeedList() {
        return newfeedList;
    }

    public static void setNewfeedList(List<ItemFeed> newfeedList) {
        PodcastApplication.newfeedList = newfeedList;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //Por padrão, a classe HttpsURLConnection já vem com o header gzip
            //Mas a HttpUrlConnection não. Logo, precisamos acrescentá-lo
            //O gzip diminui o tamanho da resposta
            conn.setRequestProperty("Accept-Encoding", "gzip");
            in = new GZIPInputStream(conn.getInputStream());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }

    public static ItemFeed[] podcastListToArray(List<ItemFeed> list){
        return list.toArray(new ItemFeed[0]);
    }

    public static void updateModelFromCursor(Cursor cursor){
        newfeedList = PodcastSQLiteDML.getFeedFromCursor(cursor);
    }

    public static void scheduleDownloadFeedJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, DownloadFeedJob.class);

        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        //30 minutos
        builder.setPeriodic(1000*60*30); //No Nougat só de 15 em 15 minutos

        //Algumas opções

        //Só pode setar se não for periódico
        //builder.setMinimumLatency(1 * 1000); // wait at least
        //builder.setOverrideDeadline(3 * 1000); // maximum delay

        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);

        //Melhor alternativa, mas só API >= 23
        //JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.schedule(builder.build());
    }
}

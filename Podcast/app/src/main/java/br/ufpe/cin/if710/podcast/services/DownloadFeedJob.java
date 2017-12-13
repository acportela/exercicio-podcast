package br.ufpe.cin.if710.podcast.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.Extras.Constantes;
import br.ufpe.cin.if710.podcast.Extras.SharedPreferencesUtil;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

/**
 * Created by acpr on 12/12/17.
 */

public class DownloadFeedJob extends JobService {

    public static final String ACTION_FEED_UPDATED = "br.ufpe.cin.if710.podcast.action.FeedWasUpdated";

    //Runs on the mainThread!!!!
    @Override
    public boolean onStartJob(JobParameters params) {
        //True if your service needs to process the work (on a separate thread). False if there's no more work to be done for this job
        return baixarFeed();
    }

    //Called if the system ever needs to stop this job before it has finished
    @Override
    public boolean onStopJob(JobParameters params) {
        //Return true to reschedule the job after the system stops it
        return true;
    }

    private boolean baixarFeed(){
        //Starta service para baixar o feed
        if(PodcastApplication.isNetworkAvailable(this)){
            //Só atualiza a lista se não estiver baixando nada
            if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                Intent updateFeedIntent = new Intent(getApplicationContext(),UpdateFeedService.class);
                updateFeedIntent.setData(Uri.parse(Constantes.RSS_FEED));
                startService(updateFeedIntent);
            }
            return true;
        }
        return false;
    }
}

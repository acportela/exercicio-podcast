package br.ufpe.cin.if710.podcast.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

/**
 * Created by acpr on 12/10/17.
 */

public class UpdateFeedService extends IntentService {

    public static final String ACTION_FEED_UPDATED = "br.ufpe.cin.if710.podcast.action.FeedWasUpdated";

    public UpdateFeedService() {
        super("UpdateFeedService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri = intent.getData();
        List<ItemFeed> newList = getItemsFromServer(uri.toString());
        if(newList != null && !newList.isEmpty()){
            PodcastApplication.setNewfeedList(newList);
            sendBroadcast(new Intent(ACTION_FEED_UPDATED));
        }
        stopSelf();
    }

    private List<ItemFeed> getItemsFromServer(String url){
        List<ItemFeed> itemList = new ArrayList<>();
        try {
            itemList = XmlFeedParser.parse(PodcastApplication.getRssFeed(url));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return itemList;
    }
}

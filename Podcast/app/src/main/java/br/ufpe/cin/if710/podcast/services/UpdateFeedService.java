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
            PodcastApplication.feedList = newList;
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

    /*private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";
        try {
            URL url = new URL(feed);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }*/
}

package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;
import br.ufpe.cin.if710.podcast.listeners.PodcastItemClickListener;
import br.ufpe.cin.if710.podcast.services.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.services.UpdateFeedService;
import br.ufpe.cin.if710.podcast.ui.adapter.PodcastCursorAdapter;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity implements PodcastDMLCommandReport, PodcastItemClickListener {

    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";

    private ListView items;
    private DownloadXmlTask task = null;
    private PodcastCursorAdapter cursorAdapter;
    private XmlFeedAdapter xmlAdapter;
    private LinearLayout layoutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = (ListView) findViewById(R.id.items);
        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        layoutProgress.setVisibility(View.VISIBLE);

        updateViewFromList();

        if(PodcastApplication.isNetworkAvailable(this)){
            task = new DownloadXmlTask();
            task.execute(RSS_FEED);
        }
        else {
            //Pegar do banco
            Cursor c = updateViewFromDatabase();
            updateModelFromDatabase(c);
            layoutProgress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(xmlAdapter != null) xmlAdapter.clear();
        if(PodcastApplication.isNetworkAvailable(this)){
            Intent updateFeedIntent = new Intent(getApplicationContext(),UpdateFeedService.class);
            updateFeedIntent.setData(Uri.parse(RSS_FEED));
            startService(updateFeedIntent);
        }
    }

    @Override
    public void onDmlQueryFineshed(Cursor cursor) {

    }

    public void updateViewFromList() {
        if(PodcastApplication.feedList != null){
            xmlAdapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, PodcastApplication.feedList,this);
            this.items.setAdapter(xmlAdapter);
            layoutProgress.setVisibility(View.GONE);
            PodcastSQLiteDML.getInstance().deletePodcasts(this,this,"1",null);
        }
    }


    @Override
    public void onDmlDeleteFineshed(Cursor cursor) {
        PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,PodcastApplication.feedList);
    }

    @Override
    public void onDmlInsertFineshed(Cursor cursor) {
        //updateModelFromDatabase(cursor);
    }

    ////////////////////////////// AÇÕES RELATIVAS AO CLICK NO ITEM DA LISTA


    //AÇÃO BAIXAR OU TOCAR
    @Override
    public void userRequestedPodcastItemAction(String currentTitle, int position) {
        //Baixar ou Tocar Episódio
        if(currentTitle.equals(this.getString(R.string.action_download))){
            //BAIXAR
            ItemFeed item = PodcastApplication.feedList.get(position);
            if(item.getDownloadLink() != null){
                Intent downloadService = new Intent(getApplicationContext(),DownloadPodcastService.class);
                downloadService.setData(Uri.parse(item.getDownloadLink()));
                startService(downloadService);
            }
        }
        else {
            //TOCAR
        }
    }

    //AÇÃO DETALHES
    @Override
    public void userRequestedEpisodeDetails(int position) {
        //Abrir EpisodeDetailsActivity
    }
    //Usado quando não há conexão com a internet
    private Cursor updateViewFromDatabase(){
        Cursor c = PodcastSQLiteDML.getInstance().queryPodcasts(this,"1",null, PodcastProviderContract.EPISODE_DATE);
        c.getCount();
        if(c != null){

            if(cursorAdapter == null){
                cursorAdapter = new PodcastCursorAdapter(this,this,c,getLayoutInflater());
            }
            else {
                cursorAdapter.changeCursor(c);
            }
            items.setAdapter(cursorAdapter);
        }
        return c;
    }

    //Utilitário para converter dados do cursor para uma lista
    private void updateModelFromDatabase(Cursor cursor){
        PodcastApplication.feedList = PodcastSQLiteDML.getFeedFromCursor(cursor);
    }

    //FAZ O DOWNLOAD DO XML DO RSS
    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(PodcastApplication.getRssFeed(params[0]));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            PodcastApplication.feedList = feed;
            updateViewFromList();
            task = null;
        }
    }
}

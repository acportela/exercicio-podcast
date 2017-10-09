package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;
import br.ufpe.cin.if710.podcast.listeners.PodcastItemClickListener;
import br.ufpe.cin.if710.podcast.ui.adapter.PodcastCursorAdapter;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity implements PodcastDMLCommandReport, PodcastItemClickListener {

    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";

    private ListView items;
    private ItemFeed[] feedItems;
    private DownloadXmlTask task = null;
    private PodcastCursorAdapter cursorAdapter;
    private XmlFeedAdapter xmlAdapter;
    private ProgressBar progressBar;

    //FLUXO COM CONEXÃO
    //1 - O XML é baixado
    //2 - A view é atualizada com o XMLAdapter enquanto, em segundo plano:
        //3 - O conteúdo do banco é apagado (retorna para o onDmlDeleteFineshed)
        //4 - É inserido o conteúdo novo (retorna para o onDMLInsertFinished)
        //5 - O model é atualizado com o conteúdo novo e "updateModelFromDatabase"

    //FLUXO SEM CONEXÃO
    //1 - É feita a consulta no banco
    //2 - A listView é atualizada com o PodcastCursorAdapter em "updateViewFromDatabase"
    //3 - O model é atualizado com o cursor da consulta em "updateModelFromDatabase"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = findViewById(R.id.items);
        progressBar = findViewById(R.id.progressBar);


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
        progressBar.setVisibility(View.VISIBLE);
        if(isNetworkAvailable()){
            task = new DownloadXmlTask();
            task.execute(RSS_FEED);
        }
        else {
            //Pegar do banco
            Cursor c = updateViewFromDatabase();
            updateModelFromDatabase(c);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(xmlAdapter != null) xmlAdapter.clear();
    }

    @Override
    public void onDmlQueryFineshed(Cursor cursor) {

    }


    //FAZ O DOWNLOAD DO XML DO RSS
    private class DownloadXmlTask extends AsyncTask<String, Void, List<ItemFeed>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext(), "iniciando...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<ItemFeed> doInBackground(String... params) {
            List<ItemFeed> itemList = new ArrayList<>();
            try {
                itemList = XmlFeedParser.parse(getRssFeed(params[0]));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return itemList;
        }

        @Override
        protected void onPostExecute(List<ItemFeed> feed) {
            xmlFeedDownloadFinished(feed);
            task = null;
        }
    }

    private void xmlFeedDownloadFinished(List<ItemFeed> items){
        xmlAdapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, items,this);
        this.items.setAdapter(xmlAdapter);
        progressBar.setVisibility(View.GONE);
        PodcastSQLiteDML.getInstance().deletePodcasts(this,this,"1",null);
    }

    @Override
    public void onDmlDeleteFineshed(Cursor cursor) {
        PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,feedItems);
    }

    @Override
    public void onDmlInsertFineshed(Cursor cursor) {
        updateModelFromDatabase(cursor);
    }


    ////////////////////////////// AÇÕES RELATIVAS AO CLICK NO ITEM DA LISTA

    //AÇÃO BAIXAR OU TOCAR
    @Override
    public void userRequestedPodcastItemAction(String currentTitle, int position) {
        //Baixar ou Tocar Episódio
    }
    //AÇÃO DETALHES
    @Override
    public void userRequestedEpisodeDetails(int position) {
        //Abrir EpisodeDetailsActivity
    }

    private String getRssFeed(String feed) throws IOException {
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
        List<ItemFeed> list = PodcastSQLiteDML.getFeedFromCursor(cursor);
        this.feedItems = list.toArray(new ItemFeed[0]);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;
import br.ufpe.cin.if710.podcast.listeners.PodcastItemClickListener;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity implements PodcastDMLCommandReport, PodcastItemClickListener {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast

    private ListView items;
    private ItemFeed[] feedItems;
    private AsyncTask task = null;
    private CursorAdapter cursorAdapter;
    private XmlFeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = findViewById(R.id.items);

        cursorAdapter =
                new SimpleCursorAdapter(
                        this,
                        R.layout.itemlista,
                        null,
                        new String[] { PodcastDBHelper.EPISODE_TITLE, PodcastDBHelper.EPISODE_DATE},
                        new int[] { R.id.item_title, R.id.item_date},
                        0
                );
        items.setAdapter(cursorAdapter);

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
        new DownloadXmlTask().execute(RSS_FEED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //task = new QueryAllTask().execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
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
            Toast.makeText(getApplicationContext(), "terminando...", Toast.LENGTH_SHORT).show();


            //Adapter Personalizado
            //TODO MUDAR O XmlFeedAdapter para chamar os métodos de retorno da lista
            adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, feed);
            //items.setAdapter(adapter);

            xmlFeedDownloadFinished(feed);

        }
    }

    //XML FOI BAIXADO -> REMOVE O QUE TEM NO BD
    private void xmlFeedDownloadFinished(List<ItemFeed> items){
        feedItems = items.toArray(new ItemFeed[0]);
        PodcastSQLiteDML.getInstance().deletePodcasts(this,this,"1",null);
    }

    @Override
    public void onDmlInsertFineshed(Cursor cursor) {
        cursorAdapter.changeCursor(cursor);
        //cursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDmlDeleteFineshed(Cursor cursor) {
        PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,feedItems);
    }

    //AÇÕES RELATIVAS AO CLICK NO ITEM DA LISTA
    @Override
    public void userRequestedPodcastItemAction(String currentTitle, int position) {
        //Baixar ou Tocar Episódio
    }

    @Override
    public void userRequestedEpisodeDetails(int position) {
        //Abrir EpisodeDetailsActivity
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //TODO Opcional - pesquise outros meios de obter arquivos da internet
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
}

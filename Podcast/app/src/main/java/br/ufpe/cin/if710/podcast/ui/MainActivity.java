package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
import br.ufpe.cin.if710.podcast.listeners.FeedWasDownloadedReceiver;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;
import br.ufpe.cin.if710.podcast.listeners.PodcastItemClickListener;
import br.ufpe.cin.if710.podcast.services.DownloadPodcastService;
import br.ufpe.cin.if710.podcast.services.UpdateFeedService;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

public class MainActivity extends Activity implements PodcastDMLCommandReport, PodcastItemClickListener {

    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    private ListView items;
    private XmlFeedAdapter xmlAdapter;
    private LinearLayout layoutProgress;
    private BroadcastReceiver feedReceiver;

    //FLUXO RESUMIDO
    /*
    * 1 - Sempre confiro se há algo na base para ser exibido enquanto que o feed é baixado
    * 2 - Uma vez terminado o download do RSS
    *   2.1 Se já ouver algo no banco, faço um update baseado no link da página do podcast
    *   2.2 Se o link já existir não insiro
    * 3 - Se usuário clicar em fazer o download, o ep é baixado
    *     infelizmente a ListView só é atualizada quando se toca na notificação
    *
    * Todos os métodos de gerenciamento do banco possuem um callback
    * Que tanto esta activity quanto o service de Download implementam (PodcastDMLCommandReport)
    *
    * Existe uma lista (model) global de feeds em PodcastApplication que é
    * atualizada sempre que retorna algo do banco
    * -> PodcastApplication.newfeedList
    *
    * A ListView e o a lista de dados (model) são atualizadas em:
    * updateViewAndModelFromCurrentDatabase() ou
    * updateViewAndModelFromCursor()
    * e são chamadaas sempre que um dos callbacks do banco for chamado
    *
    * PodcastSQLiteDML é uma classe que usa AsyncTasks para chamar atualizar o banco
    * chama o PodcastProvider no doInBackground
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        items = (ListView) findViewById(R.id.items);
        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);

        //CRIEI UM RECEIVER PARA ISOLAR O DOWNLOAD DO FEED EM UM SERVICE
        feedReceiver = new FeedWasDownloadedReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                xmlFeedWasDownloaded();
            }
        };

        Toast.makeText(getApplicationContext(), "Infelizmente não estou checando as permissões", Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(feedReceiver,new IntentFilter(UpdateFeedService.ACTION_FEED_UPDATED));

        layoutProgress.setVisibility(View.VISIBLE);

        //Preencho a lista com o conteúdo da base
        updateViewAndModelFromCurrentDatabase();

        //Service para baixar o feed
        if(PodcastApplication.isNetworkAvailable(this)){
            Intent updateFeedIntent = new Intent(getApplicationContext(),UpdateFeedService.class);
            updateFeedIntent.setData(Uri.parse(RSS_FEED));
            startService(updateFeedIntent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(xmlAdapter != null) xmlAdapter.clear();
        unregisterReceiver(feedReceiver);
    }

    @Override
    public void onDmlQueryFineshed(Cursor cursor) {

    }

    //Para atualizar a ListView
    public void updateViewFromList() {
        if (PodcastApplication.newfeedList != null) {
            xmlAdapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, PodcastApplication.newfeedList, this);
            this.items.setAdapter(xmlAdapter);
        }
    }

    //Retorno do PodcastSQLiteDML para o inserir
    @Override
    public void onDmlInsertFineshed(Cursor cursor) {
        updateViewAndModelFromCursor(cursor);
    }
    //Retorno do PodcastSQLiteDML para o atualizar
    @Override
    public void onDmlUpdateFineshed(Cursor cursor) {
        updateViewAndModelFromCursor(cursor);
    }

    ////////////////////////////// AÇÕES RELATIVAS AO CLICK NO ITEM DA LISTA
    @Override
    public void userRequestedPodcastItemAction(String currentTitle, int position) {
        //Baixar ou Tocar Episódio
        if(currentTitle.equals(this.getString(R.string.action_download))){
            //BAIXAR
            ItemFeed item = PodcastApplication.newfeedList.get(position);
            if(item.getDownloadLink() != null){
                Intent downloadService = new Intent(getApplicationContext(),DownloadPodcastService.class);
                downloadService.setData(Uri.parse(item.getDownloadLink()));
                downloadService.putExtra(DownloadPodcastService.INTENT_KEY_PAGE_LINK,item.getLink());
                downloadService.putExtra(DownloadPodcastService.INTENT_KEY_DOWN_LINK,item.getDownloadLink());
                startService(downloadService);
                Toast.makeText(getApplicationContext(), "Baixando, aguarde... (demora um bocado)", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "A lista só é atualizada após clicar na notificação", Toast.LENGTH_LONG).show();
            }
        }
        else {
            //TOCAR
            // Faltando
        }
    }

    //Chamado pelo receiver do download do feed
    private void xmlFeedWasDownloaded(){
        PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,PodcastApplication.newfeedList);
    }

    //ABRIR TELA DETALHES
    @Override
    public void userRequestedEpisodeDetails(int position) {
        ItemFeed pod = PodcastApplication.newfeedList.get(position);
        Intent detailsIntent = new Intent(this,EpisodeDetailActivity.class);
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_TITLE_KEY,pod.getTitle());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_DATE_KEY,pod.getPubDate());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_DESC_KEY,pod.getDescription());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_LINK_KEY,pod.getLink());
        startActivity(detailsIntent);
    }

    private void updateViewAndModelFromCurrentDatabase(){
        Cursor c = PodcastSQLiteDML.getInstance().queryPodcasts(this,"1",null, PodcastProviderContract.EPISODE_DATE);
        if(c.getCount() != 0){
            PodcastApplication.updateModelFromCursor(c);
           /* if(cursorAdapter == null){
                cursorAdapter = new PodcastCursorAdapter(this,this,c,getLayoutInflater());
            }
            else {
                cursorAdapter.changeCursor(c);
            }
            items.setAdapter(cursorAdapter);*/
            updateViewFromList();
            layoutProgress.setVisibility(View.GONE);
        }
    }

    private void updateViewAndModelFromCursor(Cursor c){
        if(c.getCount() != 0){
            PodcastApplication.updateModelFromCursor(c);
          /*  if(cursorAdapter == null){
                cursorAdapter = new PodcastCursorAdapter(this,this,c,getLayoutInflater());
            }
            else {
                cursorAdapter.changeCursor(c);
            }
            items.setAdapter(cursorAdapter);*/
            updateViewFromList();
            layoutProgress.setVisibility(View.GONE);
        }
    }
}

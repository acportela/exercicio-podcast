package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import br.ufpe.cin.if710.podcast.Extras.Constantes;
import br.ufpe.cin.if710.podcast.Extras.FileUtils;
import br.ufpe.cin.if710.podcast.Extras.Permissions;
import br.ufpe.cin.if710.podcast.Extras.PodcastItemCurrentState;
import br.ufpe.cin.if710.podcast.Extras.SharedPreferencesUtil;
import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.db.PodcastSQLiteDML;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
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
    private DownloadFinishedReceiver downloadFinishedReceiver;
    private ItemFeed currentItemToDownload;

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
        feedReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                xmlFeedWasDownloaded();
            }
        };
        //Outro para ser notificado quando o download do podcast acabar
        downloadFinishedReceiver = new DownloadFinishedReceiver();

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
        else if(id == R.id.action_delete_all_data){
            PodcastSQLiteDML.getInstance().deletePodcasts(getApplicationContext(),null,"1",null);
            FileUtils.deleteAllFilesFromPuclicDirectory(Environment.DIRECTORY_PODCASTS);
            baixarFeed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(feedReceiver,new IntentFilter(UpdateFeedService.ACTION_FEED_UPDATED));
        registerReceiver(downloadFinishedReceiver,new IntentFilter(DownloadPodcastService.DOWNLOAD_HAS_ENDED_ACTION));

        layoutProgress.setVisibility(View.VISIBLE);

        //Preencho a lista com o conteúdo da base
        updateViewAndModelFromCurrentDatabase();
        //Tento baixar o feed
        baixarFeed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(xmlAdapter != null) xmlAdapter.clear();
        unregisterReceiver(feedReceiver);
        unregisterReceiver(downloadFinishedReceiver);
    }

    @Override
    public void onDmlQueryFineshed(Cursor cursor) {

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
    public void userRequestedPodcastItemAction(PodcastItemCurrentState currentState, int position) {
        //Baixar ou Tocar Episódio
        if(currentState == PodcastItemCurrentState.INTHECLOUD){
            //BAIXAR
            currentItemToDownload = PodcastApplication.newfeedList.get(position);
            if(currentItemToDownload.getDownloadLink() != null){
                //Checa se há permissão para escrita no external storage
                if(Permissions.checkPermissionIsGranted(this,Permissions.REQUEST_CODE_SAVE_PODCAST_TO_DISK)){
                    //Checa se já não está baixando um podcast
                    if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                        baixarPodcast();
                    }
                }
            }
        }
        else {
            //TOCAR
            // Faltando
        }
    }

    private void baixarFeed(){
        //Service para baixar o feed
        if(PodcastApplication.isNetworkAvailable(this)){
            //Só atualiza a lista se não estiver baixando nada
            if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                Intent updateFeedIntent = new Intent(getApplicationContext(),UpdateFeedService.class);
                updateFeedIntent.setData(Uri.parse(RSS_FEED));
                startService(updateFeedIntent);
            }
        }
        else {
            //Aviso sem rede
        }
    }

    private void baixarPodcast(){
        if(PodcastApplication.isNetworkAvailable(this)){
            currentItemToDownload.setCurrentState(PodcastItemCurrentState.DOWNLOADING);
            updateViewFromList();
            Intent downloadService = new Intent(getApplicationContext(),DownloadPodcastService.class);
            downloadService.setData(Uri.parse(currentItemToDownload.getDownloadLink()));
            downloadService.putExtra(DownloadPodcastService.INTENT_KEY_PAGE_LINK,currentItemToDownload.getLink());
            downloadService.putExtra(DownloadPodcastService.INTENT_KEY_DOWN_LINK,currentItemToDownload.getDownloadLink());
            SharedPreferencesUtil.setBooleanOnSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,true,this);
            startService(downloadService);
            Toast.makeText(getApplicationContext(), "Baixando, aguarde... (demora um bocado)", Toast.LENGTH_LONG).show();
        }
    }

    //Chamado pelo receiver do download do feed
    private void xmlFeedWasDownloaded(){
        PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,PodcastApplication.newfeedList);
    }

    //Chamado pelo receiver do download do podcast
    private void podcastWasDownloaded(){
        SharedPreferencesUtil.setBooleanOnSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,false,this);
        currentItemToDownload.setCurrentState(PodcastItemCurrentState.DOWNLOADED);
        updateViewFromList();
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


    //PARTE PARA ATUALIZAR VIEWS E MODELS

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

    //Para atualizar a ListView
    public void updateViewFromList() {
        if (PodcastApplication.newfeedList != null) {
            xmlAdapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, PodcastApplication.newfeedList, this);
            this.items.setAdapter(xmlAdapter);
        }
    }



    //TRATA PERMISSÕES
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Permissions.REQUEST_CODE_SAVE_PODCAST_TO_DISK){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                    baixarPodcast();
                }
            }
        }
    }

    public class DownloadFinishedReceiver extends BroadcastReceiver {

        private static final int MY_NOTIFICATION_ID=2;
        NotificationManager notificationManager;
        Notification myNotification;

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent myIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    myIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            myNotification = new NotificationCompat.Builder(context)
                    .setContentTitle("Podcast")
                    .setContentText("Podcast Baixado!")
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setVibrate(new long[] {0, 1000, 1000, 1000, 1000 })
                    .setSmallIcon(R.drawable.ic_headset_black_24dp)
                    .build();

            notificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(MY_NOTIFICATION_ID, myNotification);

            podcastWasDownloaded();
        }
    }
}

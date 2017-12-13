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
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
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

    private ListView listView;
    private XmlFeedAdapter xmlAdapter;
    private LinearLayout layoutProgress;
    private BroadcastReceiver feedReceiver;
    private DownloadFinishedReceiver downloadFinishedReceiver;
    private ItemFeed currentItemToDownload;

    //FLUXO RESUMIDO
    //TO-DO


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.items);
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

        registerReceiver(feedReceiver,new IntentFilter(UpdateFeedService.ACTION_FEED_UPDATED));
        registerReceiver(downloadFinishedReceiver,new IntentFilter(DownloadPodcastService.DOWNLOAD_HAS_ENDED_ACTION));
        SharedPreferencesUtil.setBooleanOnSharedPreferences(Constantes.KEY_INSERTED_IN_CURRENT_SESSION,false,this);


        //Baixando o feed. Se não houver conexão, pega do banco
        layoutProgress.setVisibility(View.VISIBLE);
        if(!baixarFeed()){
            setView();
        }
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
            if(Permissions.checkPermissionIsGranted(this,Permissions.REQUEST_CODE_DELETE_ALL_PODCASTS)){
                if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                    PodcastSQLiteDML.getInstance().deletePodcasts(getApplicationContext(),null,"1",null);
                    FileUtils.deleteAllFilesFromPuclicDirectory(Environment.DIRECTORY_PODCASTS,this);
                    baixarFeed();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Baixando um podcast, aguarde", Toast.LENGTH_LONG).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart() {
        super.onStart();

        //Limpo a lista caso tenha pouca memória
        //e o app esteja em background
        if(listView == null){

            listView = (ListView) findViewById(R.id.items);

            layoutProgress.setVisibility(View.VISIBLE);
            if(!baixarFeed()){
                setView();
            }
        }
    }
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        ///ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        //boolean isLowRam = ActivityManagerCompat.isLowRamDevice(am);

        //Em primeiro plano. Logo, não posso limpar a listView
        if( (level == TRIM_MEMORY_RUNNING_CRITICAL) || (level == TRIM_MEMORY_RUNNING_LOW) ){
            PodcastDBHelper.getInstance(this).getReadableDatabase().releaseMemory();
        }
        //Testei também o TRIM_MEMORY_UI_HIDDEN, mas era chamado toda vez que entrava em segundo plano
        else if(level == TRIM_MEMORY_BACKGROUND){
            if(xmlAdapter != null) xmlAdapter.clear();
            listView = null;
        }
        //TRIM_MEMORY_COMPLETE - Background e será um dos primeiros a morrer
        else if( level == TRIM_MEMORY_COMPLETE){
            PodcastDBHelper.getInstance(this).getReadableDatabase().releaseMemory();
            if(xmlAdapter != null) xmlAdapter.clear();
            listView = null;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(feedReceiver);
        unregisterReceiver(downloadFinishedReceiver);
        super.onDestroy();
    }





    //Aqui se encerra os métodos do ciclo de vida



    @Override
    public void onDmlQueryFinished(Cursor cursor) {
    }
    //Retorno do PodcastSQLiteDML para o inserir
    @Override
    public void onDmlInsertFinished(Cursor cursor) {
        SharedPreferencesUtil.setBooleanOnSharedPreferences(Constantes.KEY_INSERTED_IN_CURRENT_SESSION,true,this);
    }
    //Retorno do PodcastSQLiteDML para o atualizar
    @Override
    public void onDmlUpdateFinished(Cursor cursor) {
    }

    ////////////////////////////// AÇÕES RELATIVAS AO CLICK NO ITEM DA LISTA
    @Override
    public void userRequestedPodcastItemAction(PodcastItemCurrentState currentState, int position) {
        //Baixar ou Tocar Episódio
        if(currentState == PodcastItemCurrentState.INTHECLOUD){
            if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                currentItemToDownload = PodcastApplication.getNewfeedList().get(position);
                if(currentItemToDownload.getDownloadLink() != null){
                    if(Permissions.checkPermissionIsGranted(this,Permissions.REQUEST_CODE_SAVE_PODCAST_TO_DISK)){
                        baixarPodcast();
                    }
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Só é possível baixar um podcast por vez", Toast.LENGTH_LONG).show();
            }
        }
        else {
            //TOCAR TO-DO
        }
    }

    private boolean baixarFeed(){
        //Service para baixar o feed
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

    private void baixarPodcast(){
        if(PodcastApplication.isNetworkAvailable(this)){
            currentItemToDownload.setCurrentState(PodcastItemCurrentState.DOWNLOADING);
            setView();
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
        setView();
        if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_INSERTED_IN_CURRENT_SESSION,this) == false){
            PodcastSQLiteDML.getInstance().insertPodcastBatch(this,this,PodcastApplication.getNewfeedList(),true);
        }
    }

    //Chamado pelo receiver do download do podcast
    private void podcastWasDownloaded(){
        SharedPreferencesUtil.setBooleanOnSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,false,this);
        currentItemToDownload.setCurrentState(PodcastItemCurrentState.DOWNLOADED);
        setView();
    }

    //ABRIR TELA DETALHES
    @Override
    public void userRequestedEpisodeDetails(int position) {
        ItemFeed pod = PodcastApplication.getNewfeedList().get(position);
        Intent detailsIntent = new Intent(this,EpisodeDetailActivity.class);
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_TITLE_KEY,pod.getTitle());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_DATE_KEY,pod.getPubDate());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_DESC_KEY,pod.getDescription());
        detailsIntent.putExtra(EpisodeDetailActivity.INTENT_DETAILS_LINK_KEY,pod.getLink());
        startActivity(detailsIntent);
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
                else {
                    Toast.makeText(getApplicationContext(), "Só é possível baixar um podcast por vez", Toast.LENGTH_LONG).show();
                }
            }
        }
        else if(requestCode == Permissions.REQUEST_CODE_DELETE_ALL_PODCASTS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(SharedPreferencesUtil.getBooleanFromSharedPreferences(Constantes.KEY_DOWNLOADING_PODCAST,this) == false){
                    PodcastSQLiteDML.getInstance().deletePodcasts(getApplicationContext(),null,"1",null);
                    FileUtils.deleteAllFilesFromPuclicDirectory(Environment.DIRECTORY_PODCASTS,this);
                    baixarFeed();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Baixando podcast, aguarde", Toast.LENGTH_LONG).show();
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
                    .setContentTitle("SciPod")
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


    //Esta função escolhe da onde deve vir os dados para alimentar a view
    //Se não existir nada em memória, ela atualiza o model pelo cursor ou banco
    //E depois atualiza a view
    private void setView(){
        if(PodcastApplication.getNewfeedList() != null && PodcastApplication.getNewfeedList().size() > 0){
            updateViewFromList();
        }
        else {
            updateViewAndModelFromCurrentDatabase();
        }
        layoutProgress.setVisibility(View.GONE);
    }



    private void updateViewAndModelFromCurrentDatabase(){
        Cursor c = PodcastSQLiteDML.getInstance().queryPodcasts(this,"1",null, PodcastProviderContract.EPISODE_DATE);
        if(c.getCount() != 0){
            PodcastApplication.updateModelFromCursor(c);
            updateViewFromList();
        }
    }

    //Para atualizar a ListView
    public void updateViewFromList() {
        if (PodcastApplication.getNewfeedList() != null) {
            xmlAdapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, PodcastApplication.getNewfeedList(), this);
            this.listView.setAdapter(xmlAdapter);
        }
    }
}

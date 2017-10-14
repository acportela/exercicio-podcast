package br.ufpe.cin.if710.podcast.db;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.PodcastApplication;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;

/**
 * Created by acpr on 04/10/17.
 */

public class PodcastSQLiteDML implements PodcastDMLInterface {

    private static PodcastSQLiteDML podDML;
    private AsyncTask t = null;

    private PodcastSQLiteDML(){

    }

    public static PodcastSQLiteDML getInstance(){
        if(podDML == null){
            podDML = new PodcastSQLiteDML();
        }
        return podDML;
    }

    @Override
    public void insertPodcastBatch(Context context, PodcastDMLCommandReport listener, List<ItemFeed> itens) {
        t = new InsertBatchTask(PodcastDBHelper.getInstance(context),listener);
        t.execute(PodcastApplication.podcastListToArray(itens));
    }

    @Override
    public void deletePodcasts(Context context, PodcastDMLCommandReport listener,String where, String[] whereArgs) {
        t = new RemoveTask(PodcastDBHelper.getInstance(context),listener,where,whereArgs);
        ItemFeed[] feed = {new ItemFeed("","","","","")};
        t.execute(feed);
    }

    //Síncrono
    @Override
    public Cursor queryPodcasts(Context context, String where, String[] whereArgs, String sortOrder) {
        PodcastProvider provider = new PodcastProvider();
        return provider.query(PodcastProviderContract.EPISODE_LIST_URI,PodcastDBHelper.columns,where,whereArgs,sortOrder);
    }

    //Insere o XML do RSS
    private class InsertBatchTask extends BaseTask<ItemFeed> {

        public InsertBatchTask(PodcastDBHelper h, PodcastDMLCommandReport l){
            super(h,l);
        }

        @Override
        protected Cursor doInBackground(ItemFeed... itens) {

            PodcastProvider provider = new PodcastProvider();

            for (ItemFeed item: itens) {
                ContentValues cv = new ContentValues();
                cv.put(PodcastDBHelper.EPISODE_TITLE,item.getTitle());
                cv.put(PodcastDBHelper.EPISODE_DATE,item.getPubDate());
                cv.put(PodcastDBHelper.EPISODE_LINK,item.getLink());
                cv.put(PodcastDBHelper.EPISODE_DESC,item.getDescription());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,item.getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_FILE_URI,"TO BE FOUND");
                //helper.getWritableDatabase().insert(PodcastDBHelper.EPISODE_TABLE,null,cv);
                provider.insert(PodcastProviderContract.EPISODE_LIST_URI,cv);
            }

            return doQuery();
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlInsertFineshed(result);
            t = null;
        }
    }

    private class RemoveTask extends BaseTask<ItemFeed> {

        private String where;
        private String[] whereArgs;

        public RemoveTask(PodcastDBHelper h, PodcastDMLCommandReport l, String w, String[] wa){
            super(h,l);
            where = w;
            whereArgs = wa;
        }

        @Override
        protected Cursor doInBackground(ItemFeed... itens) {
            //Coloquei o count só pra verificar a deleção
            //int count = helper.getWritableDatabase().delete(helper.EPISODE_TABLE, where, whereArgs);
            PodcastProvider provider = new PodcastProvider();
            provider.delete(PodcastProviderContract.EPISODE_LIST_URI,where,whereArgs);
            return(doQuery());
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlDeleteFineshed(result);
            t = null;
        }
    }


    //Todas as DML herdam de BaseTask, para executar o doQuery e atualizar o CursorAdapter
    private abstract class BaseTask<T> extends AsyncTask<T, Void, Cursor> {

        protected PodcastDBHelper helper;
        protected PodcastDMLCommandReport listener;

        public BaseTask(PodcastDBHelper h, PodcastDMLCommandReport l){
            this.helper = h;
            this.listener = l;
        }

        Cursor doQuery() {

            PodcastProvider provider = new PodcastProvider();
            Cursor result = provider.query(PodcastProviderContract.EPISODE_LIST_URI,PodcastDBHelper.columns,null,null,PodcastProviderContract.EPISODE_DATE);
            /*Cursor result=
                    helper.getReadableDatabase()
                            .query(PodcastDBHelper.EPISODE_TABLE,
                                    PodcastDBHelper.columns,
                                    null,
                                    null,
                                    null,
                                    null,
                                    helper.EPISODE_DATE);*/

            int count = result.getCount();
            return result;
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlQueryFineshed(result);
            t = null;
        }
    }

    public static List<ItemFeed> getFeedFromCursor(Cursor cursor){

        List<ItemFeed> itemList = new ArrayList<>();

        while (cursor.moveToNext()){

            String title = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_TITLE));
            String date = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_DATE));
            String episodeFileURI = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_FILE_URI));
            String id = cursor.getString(cursor.getColumnIndex(PodcastDBHelper._ID));
            String link = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_LINK));
            String description = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_DESC));
            String downloadLink = cursor.getString(cursor.getColumnIndex(PodcastDBHelper.EPISODE_DOWNLOAD_LINK));

            itemList.add(new ItemFeed(title,link,date,description,downloadLink,episodeFileURI,id));
        }

        return itemList;
    }
}

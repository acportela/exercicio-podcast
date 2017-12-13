package br.ufpe.cin.if710.podcast.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
    private static AsyncTask t = null;

    private PodcastSQLiteDML(){

    }

    public static PodcastSQLiteDML getInstance(){
        if(podDML == null){
            podDML = new PodcastSQLiteDML();
        }
        return podDML;
    }

    @Override
    public void insertPodcastBatch(Context context, PodcastDMLCommandReport listener, List<ItemFeed> itens, boolean shouldBulkInsert) {
        t = new InsertBatchTask(PodcastDBHelper.getInstance(context),listener,shouldBulkInsert);
        t.execute(PodcastApplication.podcastListToArray(itens));
    }

    @Override
    public void deletePodcasts(Context context, PodcastDMLCommandReport listener,String where, String[] whereArgs) {
        t = new RemoveTask(PodcastDBHelper.getInstance(context),listener,where,whereArgs);
        ItemFeed[] feed = {new ItemFeed("","","","","")};
        t.execute(feed);
    }

    @Override
    public void updatePodcasts(Context context, PodcastDMLCommandReport listener, String selection, String[] selectionArgs, ContentValues cv) {
        t = new EditaTask(PodcastDBHelper.getInstance(context),listener,selection,selectionArgs,cv);
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
    private static class InsertBatchTask extends BaseTask<ItemFeed> {

        private boolean bulkInsert;

        public InsertBatchTask(PodcastDBHelper h, PodcastDMLCommandReport l, boolean bulkInsert){
            super(h,l);
            this.bulkInsert = bulkInsert;
        }

        @Override
        protected Cursor doInBackground(ItemFeed... itens) {

            PodcastProvider provider = new PodcastProvider();

            ContentValues[] values = new ContentValues[itens.length];

            for (int i = 0; i < itens.length; i++) {
                ContentValues cv = new ContentValues();
                cv.put(PodcastDBHelper.EPISODE_TITLE,itens[i].getTitle());
                cv.put(PodcastDBHelper.EPISODE_DATE,itens[i].getPubDate());
                cv.put(PodcastDBHelper.EPISODE_LINK,itens[i].getLink());
                cv.put(PodcastDBHelper.EPISODE_DESC,itens[i].getDescription());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,itens[i].getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_FILE_URI,"");
                values[i] = cv;
            }
            if(bulkInsert){
                //Debug.startMethodTracing("bulkInsertTest4");
                provider.bulkInsert(PodcastProviderContract.EPISODE_LIST_URI,values);
                //Debug.stopMethodTracing();
            }
            else {
                //Debug.startMethodTracingSampling("regularInsertSampling2",1024*1024*512,5000);
                regularInsert(values,provider);
                //Debug.stopMethodTracing();
            }

            return doQuery();
        }

        private void regularInsert(ContentValues[] values, PodcastProvider provider){
            for(ContentValues cv : values){
                provider.insert(PodcastProviderContract.EPISODE_LIST_URI,cv);
            }
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlInsertFinished(result);
            t = null;
        }
    }

    private static class EditaTask extends BaseTask<ItemFeed> {

        private String selection;
        private String[] selectionArgs;
        private ContentValues content;

        public EditaTask(PodcastDBHelper h, PodcastDMLCommandReport l, String s, String[] sa,ContentValues toBeUpdated){
            super(h,l);
            selection = s;
            selectionArgs = sa;
            content = toBeUpdated;
        }

        @Override
        protected Cursor doInBackground(ItemFeed... itens) {

            PodcastProvider provider = new PodcastProvider();
            provider.update(PodcastProviderContract.EPISODE_LIST_URI,content,selection,selectionArgs);

            return doQuery();
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlUpdateFinished(result);
            t = null;
        }
    }

     private static class RemoveTask extends BaseTask<ItemFeed> {

        private String where;
        private String[] whereArgs;

        public RemoveTask(PodcastDBHelper h, PodcastDMLCommandReport l, String w, String[] wa){
            super(h,l);
            where = w;
            whereArgs = wa;
        }

        @Override
        protected Cursor doInBackground(ItemFeed... params) {
            //Coloquei o count só pra verificar a deleção
            PodcastProvider provider = new PodcastProvider();
            int count = provider.delete(PodcastProviderContract.EPISODE_LIST_URI,where,whereArgs);
            return null;

        }

        @Override
        public void onPostExecute(Cursor result) {
            t = null;
        }
    }

    //Todas as DML herdam de BaseTask, para executar o doQuery e atualizar o CursorAdapter
    private static abstract class BaseTask<T> extends AsyncTask<T, Void, Cursor> {

        private PodcastDBHelper helper;
        protected PodcastDMLCommandReport listener;

        public BaseTask(PodcastDBHelper h, PodcastDMLCommandReport l){
            this.helper = h;
            this.listener = l;
        }

        Cursor doQuery() {

            PodcastProvider provider = new PodcastProvider();
            Cursor result = provider.query(PodcastProviderContract.EPISODE_LIST_URI,PodcastDBHelper.columns,null,null,PodcastProviderContract.EPISODE_DATE);

            result.getCount();
            return result;
        }

        @Override
        public void onPostExecute(Cursor result) {
            listener.onDmlQueryFinished(result);
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

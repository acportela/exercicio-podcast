package br.ufpe.cin.if710.podcast.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.util.List;
import java.util.Objects;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;

/**
 * Created by acpr on 04/10/17.
 */

public class PodcastSQLiteDML implements PodcastDMLInterface {

    private static PodcastSQLiteDML podDML;

    private PodcastSQLiteDML(){

    }

    public static PodcastSQLiteDML getInstance(){
        if(podDML == null){
            podDML = new PodcastSQLiteDML();
        }
        return podDML;
    }

    @Override
    public void insertBatch(SQLiteOpenHelper helper, PodcastDMLCommandReport listener,ItemFeed... itens) {
        new InsertBatchTask((PodcastDBHelper) helper,listener).execute(itens);
    }

    private class InsertBatchTask extends AsyncTask<ItemFeed, Void, Void> {

        private PodcastDBHelper helper;
        private PodcastDMLCommandReport listener;

        public InsertBatchTask(PodcastDBHelper h, PodcastDMLCommandReport l){
            this.helper = h;
            this.listener = l;
        }

        @Override
        protected Void doInBackground(ItemFeed... itens) {
            for (ItemFeed item: itens) {
                ContentValues cv = new ContentValues();
                cv.put(PodcastDBHelper.EPISODE_TITLE,item.getTitle());
                cv.put(PodcastDBHelper.EPISODE_DATE,item.getPubDate());
                cv.put(PodcastDBHelper.EPISODE_LINK,item.getLink());
                cv.put(PodcastDBHelper.EPISODE_DESC,item.getDescription());
                cv.put(PodcastDBHelper.EPISODE_DOWNLOAD_LINK,item.getDownloadLink());
                cv.put(PodcastDBHelper.EPISODE_FILE_URI,"TO BE FOUND");
                helper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE,null,cv);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.onInsertFinished();
        }
    }
}

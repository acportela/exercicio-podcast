package br.ufpe.cin.if710.podcast.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;

/**
 * Created by acpr on 04/10/17.
 */

public interface PodcastDMLInterface {
    void insertPodcastBatch(Context context, PodcastDMLCommandReport listener, List<ItemFeed> itens);
    void deletePodcasts(Context context, PodcastDMLCommandReport listener, String where, String[] whereArgs);
    Cursor queryPodcasts(Context context, String where, String[] whereArgs, String sortOrder);
}

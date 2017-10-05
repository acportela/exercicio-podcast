package br.ufpe.cin.if710.podcast.db;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.listeners.PodcastDMLCommandReport;

/**
 * Created by acpr on 04/10/17.
 */

public interface PodcastDMLInterface {
    void insertBatch(SQLiteOpenHelper helper, PodcastDMLCommandReport listener,ItemFeed... itens);
}

package br.ufpe.cin.if710.podcast.listeners;

import android.database.Cursor;

/**
 * Created by acpr on 04/10/17.
 */

public interface PodcastDMLCommandReport {
    void onDmlQueryFinished(Cursor cursor);
    void onDmlInsertFinished(Cursor cursor);
    void onDmlUpdateFinished(Cursor cursor);
}

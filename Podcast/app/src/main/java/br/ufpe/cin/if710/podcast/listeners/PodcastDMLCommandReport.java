package br.ufpe.cin.if710.podcast.listeners;

import android.database.Cursor;

import br.ufpe.cin.if710.podcast.db.DMLCommandsEnum;

/**
 * Created by acpr on 04/10/17.
 */

public interface PodcastDMLCommandReport {
    void onDmlQueryFineshed(Cursor cursor);
    void onDmlInsertFineshed(Cursor cursor);
    void onDmlDeleteFineshed(Cursor cursor);
}

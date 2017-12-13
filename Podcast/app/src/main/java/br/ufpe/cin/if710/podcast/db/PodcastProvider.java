package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

public class PodcastProvider extends ContentProvider {

    PodcastDBHelper db;

    public PodcastProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(isPodcastFeedUri(uri)){
            return db.getInstance(getContext()).getWritableDatabase().delete(PodcastDBHelper.EPISODE_TABLE,selection,selectionArgs);
        }
        else return 0;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if (isPodcastFeedUri(uri)) {

            long id = db.getInstance(getContext()).getWritableDatabase().insertWithOnConflict(PodcastDBHelper.EPISODE_TABLE,null,values, SQLiteDatabase.CONFLICT_IGNORE);

            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }
        else return null;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        int numInserted = 0;

        SQLiteDatabase sqlDB = db.getInstance(getContext()).getWritableDatabase();
        sqlDB.beginTransaction();

        try {

            for (ContentValues cv : values) {
                long newID = sqlDB.insertWithOnConflict(PodcastDBHelper.EPISODE_TABLE, null, cv,SQLiteDatabase.CONFLICT_IGNORE);
                if (newID > 0) numInserted++;
            }
            sqlDB.setTransactionSuccessful();
            numInserted = values.length;

        } finally {
            sqlDB.endTransaction();
        }
        return numInserted;
    }

    @Override
    public boolean onCreate() {
        db.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;

        if (isPodcastFeedUri(uri)) {
            cursor = db.getInstance(getContext()).getReadableDatabase().query(PodcastDBHelper.EPISODE_TABLE,projection, selection, selectionArgs,null,null,sortOrder);
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (isPodcastFeedUri(uri)) {
            return db.getInstance(getContext()).getWritableDatabase().update(PodcastDBHelper.EPISODE_TABLE, values, selection, selectionArgs);
        }
        else return 0;
    }

    private boolean isPodcastFeedUri(Uri uri) {
        return uri.getLastPathSegment().equals(PodcastProviderContract.EPISODE_TABLE);
    }

}

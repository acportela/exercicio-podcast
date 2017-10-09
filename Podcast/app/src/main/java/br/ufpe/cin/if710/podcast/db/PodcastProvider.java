package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import br.ufpe.cin.if710.podcast.domain.ItemFeed;

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
            long id = db.getInstance(getContext()).getWritableDatabase().insert(PodcastDBHelper.EPISODE_TABLE,null,values);
            return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(id));
        }
        else return null;
    }

    /*@Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        if (isPodcastFeedUri(uri)) {
            for (ContentValues cv : values) {
                db.getWritableDatabase().insert(PodcastDBHelper.EPISODE_TABLE,null,cv);
            }

        }
        return 0;
    }*/


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

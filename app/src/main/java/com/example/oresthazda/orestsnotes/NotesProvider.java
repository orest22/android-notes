package com.example.oresthazda.orestsnotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by oresthazda on 08/09/16.
 */
public class NotesProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.oresthazda.orestnotes.notesprovider";
    private static final String BASE_PATH = "notes";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    // Constant to identify the requested operation
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;


    private static final UriMatcher uriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Note";


    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH+"/notes", NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH+ "/notes/#", NOTES_ID);
    }

    private SQLiteDatabase database;


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBIOpenHelper.TABLE_NOTES, selection, selectionArgs);
    }

    @Override
    public boolean onCreate() {
        DBIOpenHelper dbHelper = new DBIOpenHelper(getContext());
        database = dbHelper.getWritableDatabase();
        return true;
    }

     @Override
    public Cursor query(Uri uri, String[] prijection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) == NOTES_ID) {

            selection = DBIOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBIOpenHelper.TABLE_NOTES, DBIOpenHelper.ALL_NOTES_COLUMNS, selection, null, null, null, DBIOpenHelper.NOTE_CREATED + " DESC");
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id = database.insert(DBIOpenHelper.TABLE_NOTES, null, contentValues);
        return Uri.parse(BASE_PATH+"/"+id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return database.update(DBIOpenHelper.TABLE_NOTES, contentValues, selection, selectionArgs);
    }
}

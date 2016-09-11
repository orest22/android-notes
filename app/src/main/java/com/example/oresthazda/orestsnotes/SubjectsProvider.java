package com.example.oresthazda.orestsnotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by oresthazda on 09/09/16.
 */
public class SubjectsProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.oresthazda.orestnotes.subjectsprovider";
    private static final String BASE_PATH = "subjects";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH );

    private static final int SUBJECTS = 3;
    private static final int SUBJECT_ID = 4;


    private static final UriMatcher uriMatcher =  new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Subject";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH+ "/subjects", SUBJECTS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH+ "/subjects/#", SUBJECT_ID);
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

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String selection, String[] strings1, String s1) {

        if (uriMatcher.match(uri) == SUBJECT_ID) {

            selection = DBIOpenHelper.SUBJECT_ID + "=" + uri.getLastPathSegment();
        }

        return database.query(DBIOpenHelper.TABLE_SUBJECTS, DBIOpenHelper.ALL_SUBJECT_COLUMNS, selection, null, null, null, DBIOpenHelper.SUBJECT_NAME + " ASC");
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id = database.insert(DBIOpenHelper.TABLE_SUBJECTS, null, contentValues);
        return Uri.parse(BASE_PATH+"/"+id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        return database.update(DBIOpenHelper.TABLE_SUBJECTS, contentValues, selection, selectionArgs);
    }
}

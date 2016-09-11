package com.example.oresthazda.orestsnotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by oresthazda on 08/09/16.
 */

public class DBIOpenHelper extends SQLiteOpenHelper {

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_CREATED = "noteCreated";
    public static final String NOTE_SUBJECT_ID = "noteSubjectId";
    public static final String NOTE_LATITUDE = "noteLatitude";
    public static final String NOTE_LONGITUDE = "noteLongitude";

    public static  final  String[] ALL_NOTES_COLUMNS = { NOTE_ID, NOTE_TEXT, NOTE_CREATED, NOTE_LATITUDE, NOTE_LONGITUDE};


    //Constants Subject Table
    public static final  String TABLE_SUBJECTS = "subjects";
    public static final String SUBJECT_ID = "_id";
    public static final String SUBJECT_NAME = "subjectName";
    public static  final  String[] ALL_SUBJECT_COLUMNS = { SUBJECT_ID, SUBJECT_NAME};



    //Create subjects table

    private  static final String TABLE_SUBJECTS_CREATE =
            "CREATE TABLE "+ TABLE_SUBJECTS + " (" +
                    SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SUBJECT_NAME + " TEXT NOT NULL" +
                    ")";

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_SUBJECT_ID + " INTEGER REFERENCES "+ TABLE_SUBJECTS +"("+ SUBJECT_ID +"), " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_LATITUDE + " TEXT, " +
                    NOTE_LONGITUDE + " TEXT, " +
                    NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";


    public DBIOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(TABLE_SUBJECTS_CREATE);
        sqLiteDatabase.execSQL(TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);

        onCreate(sqLiteDatabase);
    }
}
